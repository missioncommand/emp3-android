package mil.emp3.core.mapgridlines;

import android.content.res.Resources;
import android.graphics.Point;
import android.util.Log;

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
import mil.emp3.api.interfaces.ICamera;
import mil.emp3.api.interfaces.IFeature;
import mil.emp3.api.utils.EmpBoundingBox;
import mil.emp3.api.utils.FontUtilities;
import mil.emp3.api.utils.GeoLibrary;
import mil.emp3.mapengine.events.MapInstanceViewChangeEvent;
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

    // The map instance where the map grid is displayed.
    protected final IMapInstance mapInstance;

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
    private final EmpBoundingBox boundingBox;
    // The meters per pixel across the center.
    private double metersPerPixel;
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
            while (NotDone) {
                try {
                    this.processEvent.acquire();

                    long startTS = System.currentTimeMillis();
                    processViewChange(boundingBox, currentCamera, metersPerPixel);
                    Log.i(TAG, "feature generation in " + (System.currentTimeMillis() - startTS) + " ms. " + featureList.size() + " features.");
                    previousCamera.copySettingsFrom(currentCamera);
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
    public void mapViewChange(IGeoBounds mapBounds, ICamera camera, int viewWidth, int viewHeight) {
        if ((null == mapBounds) || (null == camera)) {
            clearFeatureList();
            this.mapInstance.scheduleMapRedraw();
            return;
        }
        Point westPoint;
        Point eastPoint;
        double viewWidthInMeters;
        IGeoPosition centerWest = new GeoPosition();
        IGeoPosition centerEast = new GeoPosition();

        this.boundingBox.copyFrom(mapBounds);
        this.currentCamera.copySettingsFrom(camera);

        centerWest.setLatitude(this.boundingBox.centerLatitude());
        centerWest.setLongitude(this.boundingBox.getWest());
        centerWest.setAltitude(0.0);

        centerEast.setLatitude(centerWest.getLatitude());
        centerEast.setLongitude(mapBounds.getEast());
        centerEast.setAltitude(0.0);

        westPoint = this.mapInstance.geoToContainer(centerWest);
        eastPoint = this.mapInstance.geoToContainer(centerEast);

        viewWidthInMeters = GeoLibrary.computeRhumbDistance(centerWest, centerEast);
        this.metersPerPixel = viewWidthInMeters / (eastPoint.x - westPoint.x);

        this.generationThread.scheduleProcessing();
    }

    /**
     * The sub class must implement this method to process the view change events generated by the map. It will not
     * get called if the bounds or the camera are null.
     * @param mapBounds      The bounding box of the map's viewing area.
     * @param camera         The current camera.
     * @param metersPerPixel The meters per pixel across the center of the map.
     */
    protected abstract void processViewChange(EmpBoundingBox mapBounds, ICamera camera, double metersPerPixel);

    /**
     * The sub class must implement this method to set the attributes of the path.
     * @param path              The path feature created by the sub class.
     * @param gridObjectType    The object type provided by the sub class.
     */
    protected abstract void setPathAttributes(Path path, String gridObjectType);

    /**
     * The sub class must implement this method to set the attributes of the label it creates.
     * @param label             The Text feature created by the sub class.
     * @param gridObjectType    The object type indicated by the sub class.
     */
    protected abstract void setLabelAttributes(Text label, String gridObjectType);

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

    protected void displayGridLabel(String label, EmpBoundingBox mapBounds, String objectStyleType, double metersPerPixel) {
        IGeoPosition labelPos = this.mapInstance.containerToGeo(new Point(PIXELS_PER_INCH / 8, PIXELS_PER_INCH / 8));
        if (null == labelPos) {
            double charPixelWidth = getCharacterPixelWidth(objectStyleType) * metersPerPixel;
            labelPos = new GeoPosition();
            labelPos.setLatitude(mapBounds.getNorth());
            labelPos.setLongitude(mapBounds.getWest());
            GeoLibrary.computePositionAt(135.0, charPixelWidth, labelPos, labelPos);
        }

        addFeature(createLabelFeature(labelPos, label, objectStyleType));
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
}
