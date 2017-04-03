package mil.emp3.core.mapgridlines;

import android.content.res.Resources;
import android.graphics.Point;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import org.cmapi.primitives.GeoLabelStyle;
import org.cmapi.primitives.GeoPosition;
import org.cmapi.primitives.IGeoAltitudeMode;
import org.cmapi.primitives.IGeoBounds;
import org.cmapi.primitives.IGeoLabelStyle;
import org.cmapi.primitives.IGeoPosition;
import org.cmapi.primitives.IGeoStrokeStyle;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Semaphore;

import mil.emp3.api.Camera;
import mil.emp3.api.Path;
import mil.emp3.api.Text;
import mil.emp3.api.global;
import mil.emp3.api.interfaces.ICamera;
import mil.emp3.api.interfaces.IFeature;
import mil.emp3.api.interfaces.IEmpBoundingBox;
import mil.emp3.api.utils.EmpBoundingBox;
import mil.emp3.api.utils.EmpGeoColor;
import mil.emp3.api.utils.EmpGeoPosition;
import mil.emp3.api.utils.FontUtilities;
import mil.emp3.api.utils.GeoLibrary;
import mil.emp3.mapengine.interfaces.ICoreMapGridLineGenerator;
import mil.emp3.mapengine.interfaces.IMapGridLines;
import mil.emp3.mapengine.interfaces.IMapInstance;

/**
 * This abstract class is the base class for all map grid line generator classes.
 */

public abstract class AbstractMapGridLine implements IMapGridLines, ICoreMapGridLineGenerator {
    private static final String TAG = AbstractMapGridLine.class.getSimpleName();

    // The pixel density of the device.
    protected static final int PIXELS_PER_INCH = Resources.getSystem().getDisplayMetrics().densityDpi;

    // Object type for abstract components.
    private static final String MAIN_GRID_TYPE_LABEL = "MAIN.gridtype.label";

    // The map instance where the map grid is displayed.
    protected final IMapInstance mapInstance;

    private static final Handler uiLooper = new Handler(Looper.getMainLooper());

    // The most recent time the grid was updated.
    private Date lastUpdated;
    // The list of features that make up the grid.
    private final List<IFeature> featureList = new ArrayList<>();
    // A list that is used to return the feature list to the map.
    private final List<IFeature> returnList = new ArrayList<>();

    // The camera setting when the view change occurred.
    protected final ICamera currentCamera;
    // The camera setting of the previous view change.
    protected final ICamera previousCamera;
    // The bounding box of the map.
    private final IEmpBoundingBox boundingBox;
    // The meters per pixel across the center.
    private double metersPerPixel;
    // Bounds width in pixels.
    private int boundsPixelWidth;
    // Bounds height in pixels.
    private int boundsPixelHeight;
    // The generation thread.
    private GridLineGenerationThread generationThread;

    // Map list that hold the stroke style used by the grid lines.
    private final Map<String, IGeoStrokeStyle> strokeStyleMap;
    // Map list that hold the label styles used by the grid labels.
    private final Map<String, IGeoLabelStyle> labelStyleMap;

    private class GridLineGenerationThread extends java.lang.Thread {
        private final Semaphore processEvent;
        private boolean NotDone = true;

        protected GridLineGenerationThread() {
            this.setName("GridLineGenerationThread");
            this.processEvent = new Semaphore(0);
        }

        @Override
        public void run() {
            IEmpBoundingBox mapBounds = new EmpBoundingBox();
            ICamera camera = new Camera();
            while (NotDone) {
                try {
                    this.processEvent.acquire();

                    mapBounds.copyFrom(boundingBox);
                    camera.copySettingsFrom(currentCamera);

                    long startTS = System.currentTimeMillis();
                    String temp = String.format("North: %1$10.8f South: %2$10.8f West: %3$10.8f East: %4$10.8f", boundingBox.getNorth(), boundingBox.getSouth(), boundingBox.getWest(), boundingBox.getEast());
                    Log.i(TAG, temp);
                    processViewChange(mapBounds, camera, metersPerPixel);
                    Log.i(TAG, "feature generation in " + (System.currentTimeMillis() - startTS) + " ms. " + featureList.size() + " features.");
                    previousCamera.copySettingsFrom(camera);
                    mapInstance.scheduleMapRedraw();

                } catch (InterruptedException e) {
                    NotDone = false;
                }
            }
        }

        public void scheduleProcessing() {
            this.processEvent.release();
        }

        public void exitThread() {
            this.NotDone = false;
            this.interrupt();
            try {
                this.join();
            } catch (InterruptedException e) {
            }
        }
    }

    protected AbstractMapGridLine(IMapInstance mapInstance) {
        this.strokeStyleMap = new HashMap<>();
        this.labelStyleMap = new HashMap<>();
        this.lastUpdated = new Date();
        this.currentCamera = new Camera();
        this.previousCamera = new Camera();
        this.boundingBox = new EmpBoundingBox();
        this.mapInstance = mapInstance;
        this.generationThread = new GridLineGenerationThread();
        this.generationThread.setPriority(this.generationThread.getPriority() + 1);
        this.generationThread.start();

        setStyles();
    }

    // This method loads the styles.
    private void setStyles() {
        EmpGeoColor color;
        IGeoLabelStyle labelStyle;

        // MGRS Grid Label
        labelStyle = new GeoLabelStyle();
        color = new EmpGeoColor(1.0, 200, 200, 200);
        labelStyle.setColor(color);
        labelStyle.setSize(8.0);
        labelStyle.setJustification(IGeoLabelStyle.Justification.CENTER);
        labelStyle.setFontFamily("Ariel");
        labelStyle.setTypeface(IGeoLabelStyle.Typeface.BOLD);
        addLabelStyle(MAIN_GRID_TYPE_LABEL, labelStyle);
    }

    @Override
    public void shutdownGenerator() {
        if (null != this.generationThread) {
            this.generationThread.exitThread();
            this.generationThread = null;
        }
    }

    @Override
    public Date getLastUpdated() {
        return this.lastUpdated;
    }

    @Override
    public List<IFeature> getGridFeatures() {
        // We need to return the feature in the return list so the map can loop thru it with out us
        // runing into a multi threading issue.
        synchronized (this.featureList) {
            this.returnList.clear();
            if (!this.featureList.isEmpty()) {
                this.returnList.addAll(this.featureList);
            }
        }
        return this.returnList;
    }

    protected void clearFeatureList() {
        boolean resetTime = (this.featureList.size() > 0);
        synchronized (this.featureList) {
            this.featureList.clear();
        }

        if (resetTime) {
            this.lastUpdated.setTime(System.currentTimeMillis());
        }
    }

    protected void addFeature(IFeature feature) {
        if (null == feature) {
            throw new InvalidParameterException("feature is null.");
        }
        synchronized (this.featureList) {
            this.featureList.add(feature);
        }
        this.lastUpdated.setTime(System.currentTimeMillis());
    }

    @Override
    public void mapViewChange(IGeoBounds mapBounds, ICamera camera, final int viewWidth, final int viewHeight) {
        if ((null == mapBounds) || (null == camera)) {
            // If there is no bounds or camera we remove the grid. The camera may be looking up.
            clearFeatureList();
            this.mapInstance.scheduleMapRedraw();
            return;
        }

        this.boundingBox.copyFrom(mapBounds);
        this.currentCamera.copySettingsFrom(camera);

        uiLooper.post(new Runnable() {
            @Override
            public void run() {
                double viewWidthInMeters;
                Point westPoint;
                Point eastPoint;
                IGeoPosition centerWest;
                IGeoPosition centerEast;
                Point northPoint;
                Point southPoint;
                IGeoPosition centerNorth;
                IGeoPosition centerSouth;
                ICamera camera = AbstractMapGridLine.this.currentCamera;
                IMapInstance mapInstance = AbstractMapGridLine.this.mapInstance;
                IEmpBoundingBox bBox = AbstractMapGridLine.this.boundingBox;

                centerWest = new EmpGeoPosition(bBox.centerLatitude(), bBox.getWest());
                centerEast = new EmpGeoPosition(centerWest.getLatitude(), bBox.getEast());

                westPoint = mapInstance.geoToContainer(centerWest);
                eastPoint = mapInstance.geoToContainer(centerEast);

                if ((null != westPoint) && (null != eastPoint)) {
                    // Using Pythagoras to compute the pixel distance ( for width and height) of the bounding box.
                    int deltaX = eastPoint.x - westPoint.x;
                    int deltaY = eastPoint.y - westPoint.y;
                    double deltaXe2 = deltaX * deltaX;
                    double deltaYe2 = deltaY * deltaY;
                    double pixelDistance = Math.sqrt(deltaXe2 + deltaYe2);

                    AbstractMapGridLine.this.boundsPixelWidth = (int) pixelDistance;

                    // Calculate the north south pixelsHeight.
                    centerNorth = centerWest;
                    centerSouth = centerEast;

                    centerNorth.setLatitude(bBox.getNorth());
                    centerNorth.setLongitude(bBox.centerLongitude());

                    centerSouth.setLatitude(bBox.getSouth());
                    centerSouth.setLongitude(centerNorth.getLongitude());

                    northPoint = mapInstance.geoToContainer(centerNorth);
                    southPoint = mapInstance.geoToContainer(centerSouth);

                    deltaX = northPoint.x - southPoint.x;
                    deltaY = northPoint.y - southPoint.y;
                    deltaXe2 = deltaX * deltaX;
                    deltaYe2 = deltaY * deltaY;
                    AbstractMapGridLine.this.boundsPixelHeight = (int) Math.sqrt(deltaXe2 + deltaYe2);

                    if (pixelDistance > 0.0) {
                        viewWidthInMeters = GeoLibrary.computeDistanceBetween(centerWest, centerEast);
                        AbstractMapGridLine.this.metersPerPixel = viewWidthInMeters / pixelDistance;

                        AbstractMapGridLine.this.generationThread.scheduleProcessing();
                    }
                }
            }
        });
    }

    /**
     * The sub class must implement this method to process the view change events generated by the map. It will not
     * get called if the bounds or the camera are null.
     * @param mapBounds      The bounding box of the map's viewing area.
     * @param camera         The current camera.
     * @param metersPerPixel The meters per pixel across the center of the map.
     */
    protected abstract void processViewChange(IEmpBoundingBox mapBounds, ICamera camera, double metersPerPixel);

    /**
     * The sub class must override this method to set the properties of the path. If the grid objject type
     * was not created by the sub class it must call the parent method.
     * @param path              The path feature created by the sub class.
     * @param gridObjectType    The object type provided by the sub class.
     */
    protected void setPathAttributes(Path path, String gridObjectType) {
    }

    /**
     * The sub class must override this method to set the properties of the label it creates. If the
     * object class is not created by the sub class it must call the parents method.
     * @param label             The Text feature created by the sub class.
     * @param gridObjectType    The object type indicated by the sub class.
     */
    protected void setLabelAttributes(Text label, String gridObjectType) {
    }

    /**
     * This method creates a path feature with the positions list provided, then set the path attributes.
     * Then it calls the setPathAttributes to allow the sub class to set the path's properties.
     * @param positionList      The list of IGeoPositions.
     * @param gridObjectType    A caller defined string to identify the type of path.
     * @return The IFeature interface of the path created.
     */
    protected IFeature createPathFeature(List<IGeoPosition> positionList, String gridObjectType) {
        Path path = new Path(positionList);

        path.setAltitudeMode(IGeoAltitudeMode.AltitudeMode.CLAMP_TO_GROUND);
        if (containsStrokeStyle(gridObjectType)) {
            path.setStrokeStyle(getStrokeStyle(gridObjectType));
        }
        setPathAttributes(path, gridObjectType);

        return path;
    }

    /**
     * This method creates a Text feature at the position indicated. Once the Text feature is created
     * it sets the label style. Then it calls the setLabelAttribute method so the caller can set the labels properties.
     * @param position          The IGeoPosition to place the label.
     * @param text              The text to display on the map.
     * @param gridObjectType    A caller defined string to identify the type of label.
     * @return The IFeature interface of the Text feature created.
     */
    protected IFeature createLabelFeature(IGeoPosition position, String text, String gridObjectType) {
        Text label = new Text(text);

        label.setPosition(position);
        if (containsLabelStyle(gridObjectType)) {
            label.setLabelStyle(getLabelStyle(gridObjectType));
        }
        label.setAltitudeMode(IGeoAltitudeMode.AltitudeMode.CLAMP_TO_GROUND);
        setLabelAttributes(label, gridObjectType);

        if (this.currentCamera.getHeading() != 0.0) {
            double azimuth = label.getAzimuth() - this.currentCamera.getHeading();

            // This operation converts the Double remainder operation to a modulus operation.
            // It ensures that all label remain aligned with the grid when the heading changes.
            azimuth = global.modulus((azimuth + 180), 360.0) - 180.0;
            label.setAzimuth(azimuth);
        }

        if (this.currentCamera.getRoll() != 0.0) {
            double azimuth = label.getAzimuth() + this.currentCamera.getRoll();

            azimuth = global.modulus((azimuth + 180), 360.0) - 180.0;
            label.setAzimuth(azimuth);
        }

        return label;
    }

    /**
     * This method returns the character width in pixels for a specified feature type.
     * @param featureType a sub class defined value used to access the label syle list.
     * @return The width in pixels of a character in the point size defined in the label style.
     */
    protected int getCharacterPixelWidth(String featureType) {
        IGeoLabelStyle labelStyle = this.labelStyleMap.get(featureType);

        if (null == labelStyle) {
            return 0;
        }

        return FontUtilities.fontPointsToPixels((int) labelStyle.getSize());
    }

    protected void displayGridLabel(String label, IEmpBoundingBox mapBounds, double metersPerPixel) {
        IGeoPosition labelPos;

        double charMetersWidth = getCharacterPixelWidth(MAIN_GRID_TYPE_LABEL) * metersPerPixel / 2.0;
        labelPos = new GeoPosition();
        labelPos.setLatitude(mapBounds.getNorth());
        labelPos.setLongitude(mapBounds.centerLongitude());
        GeoLibrary.computePositionAt(180.0, charMetersWidth, labelPos, labelPos);

        addFeature(createLabelFeature(labelPos, label, MAIN_GRID_TYPE_LABEL));
    }

    protected boolean containsStrokeStyle(String styleType) {
        return this.strokeStyleMap.containsKey(styleType);
    }

    protected void addStrokeStyle(String styleType, IGeoStrokeStyle strokeStyle) {
        this.strokeStyleMap.put(styleType, strokeStyle);
    }

    protected IGeoStrokeStyle getStrokeStyle(String styleType) {
        if (!this.strokeStyleMap.containsKey(styleType)) {
            return null;
        }

        return this.strokeStyleMap.get(styleType);
    }

    protected boolean containsLabelStyle(String styleType) {
        return this.labelStyleMap.containsKey(styleType);
    }

    protected void addLabelStyle(String styleType, IGeoLabelStyle labelStyle) {
        this.labelStyleMap.put(styleType, labelStyle);
    }

    protected IGeoLabelStyle getLabelStyle(String styleType) {
        if (!this.labelStyleMap.containsKey(styleType)) {
            return null;
        }

        return this.labelStyleMap.get(styleType);
    }

    protected int getBoundingBoxPixelWidth() {
        return this.boundsPixelWidth;
    }

    protected int getBoundingBoxPixelHeight() {
        return this.boundsPixelHeight;
    }
}
