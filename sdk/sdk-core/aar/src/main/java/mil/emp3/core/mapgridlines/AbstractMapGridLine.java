package mil.emp3.core.mapgridlines;

import android.util.Log;

import org.cmapi.primitives.IGeoAltitudeMode;
import org.cmapi.primitives.IGeoBounds;
import org.cmapi.primitives.IGeoPosition;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Semaphore;

import mil.emp3.api.Camera;
import mil.emp3.api.Path;
import mil.emp3.api.Text;
import mil.emp3.api.interfaces.ICamera;
import mil.emp3.api.interfaces.IFeature;
import mil.emp3.api.utils.EmpBoundingBox;
import mil.emp3.mapengine.events.MapInstanceViewChangeEvent;
import mil.emp3.mapengine.interfaces.ICoreMapGridLineGenerator;
import mil.emp3.mapengine.interfaces.IMapGridLines;
import mil.emp3.mapengine.interfaces.IMapInstance;

/**
 * This abstract class is the base class for all map grid line generator classes.
 */

public abstract class AbstractMapGridLine implements IMapGridLines, ICoreMapGridLineGenerator {
    private static final String TAG = AbstractMapGridLine.class.getSimpleName();

    private Date lastUpdated;
    private final List<IFeature> featureList = new ArrayList<>();
    private final List<IFeature> returnList = new ArrayList<>();
    protected final ICamera currentCamera;
    protected final ICamera previousCamera;
    private final EmpBoundingBox boundingBox;
    protected final IMapInstance mapInstance;
    private int viewWidth;
    private int viewHeight;
    private GridLineGenerationThread generationThread;

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
                    processViewChange(boundingBox, currentCamera, viewWidth, viewHeight);
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

        this.boundingBox.copyFrom(mapBounds);
        this.currentCamera.copySettingsFrom(camera);
        this.viewWidth = viewWidth;
        this.viewHeight = viewHeight;

        this.generationThread.scheduleProcessing();
    }

    /**
     * The sub class must implement this method to process the view change events generated by the map. It will not
     * get called if the bounds or the camera are null.
     * @param mapBounds     The bounding box of the map's viewing area.
     * @param camera        The current camera.
     * @param viewWidth     The width of the map's viewing area in pixels.
     * @param viewHeight    The height of the map's viewing area in pixels.
     */
    protected abstract void processViewChange(EmpBoundingBox mapBounds, ICamera camera, int viewWidth, int viewHeight);

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
     * This method creates a path feature with the positions list provided. Then it calls the setPathAttributes
     * to allow the sub class to set the path's attributes.
     * @param positionList      The list of IGeoPositions.
     * @param gridObjectType    A caller defined string to identify the type of path.
     * @return The IFeature interface of the path created.
     */
    protected IFeature createPathFeature(List<IGeoPosition> positionList, String gridObjectType) {
        Path path = new Path(positionList);

        setPathAttributes(path, gridObjectType);
        path.setAltitudeMode(IGeoAltitudeMode.AltitudeMode.CLAMP_TO_GROUND);

        return path;
    }

    /**
     * This methos creates a Text feature at the position indicated. Once the Text feature is created
     * the setLabelAttribute method is called so the caller can set the labels atrivutes.
     * @param position          The IGeoPosition to place the label.
     * @param text              The text to display on the map.
     * @param gridObjectType    A caller defined string to identify the type of label.
     * @return The IFeature interface of the Text feature created.
     */
    protected IFeature createLabelFeature(IGeoPosition position, String text, String gridObjectType) {
        Text label = new Text(text);

        label.setPosition(position);
        label.setAltitudeMode(IGeoAltitudeMode.AltitudeMode.CLAMP_TO_GROUND);
        setLabelAttributes(label, gridObjectType);
        return label;
    }
}
