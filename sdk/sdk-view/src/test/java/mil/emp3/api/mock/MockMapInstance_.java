package mil.emp3.api.mock;


import android.graphics.Point;
import android.util.Log;
import android.view.View;

import org.cmapi.primitives.IGeoBounds;
import org.cmapi.primitives.IGeoPosition;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;

import mil.emp3.api.Camera;
import mil.emp3.api.enums.FeatureTypeEnum;
import mil.emp3.api.enums.MapMotionLockEnum;
import mil.emp3.api.enums.WMSVersionEnum;
import mil.emp3.api.events.CameraEvent;
import mil.emp3.api.exceptions.EMP_Exception;
import mil.emp3.api.interfaces.ICamera;
import mil.emp3.api.interfaces.ICapture;
import mil.emp3.api.interfaces.IFeature;
import mil.emp3.api.interfaces.ILookAt;
import mil.emp3.api.interfaces.IMapService;
import mil.emp3.api.interfaces.IUUIDSet;
import mil.emp3.api.listeners.ICameraEventListener;
import mil.emp3.mapengine.CoreMapInstance;
import mil.emp3.mapengine.api.Capabilities;
import mil.emp3.mapengine.api.FeatureVisibility;
import mil.emp3.mapengine.api.FeatureVisibilityList;
import mil.emp3.mapengine.interfaces.IMapEngineCapabilities;
import mil.emp3.mapengine.interfaces.IMapEngineProperties;
import mil.emp3.mapengine.interfaces.IMapEngineRequirements;
import mil.emp3.mapengine.interfaces.IMilStdRenderer;
import mil.emp3.mapengine.interfaces.ISetVisibilityList;

public class MockMapInstance_ extends CoreMapInstance {

    private static String TAG = MockMapInstance_.class.getSimpleName();
    protected final BlockingQueue<IFeature> addFeatureQueue;
    protected final BlockingQueue<UUID> removeFeatureQueue;
    protected final BlockingQueue<ICamera> setCameraQueue;
    protected ICamera currentCamera;

    public MockMapInstance_(BlockingQueue<IFeature> addFeatureQueue, BlockingQueue<UUID> removeFeatureQueue,
                           BlockingQueue<ICamera> setCameraQueue) {
        super(TAG, null);
        this.addFeatureQueue = addFeatureQueue;
        this.removeFeatureQueue = removeFeatureQueue;
        this.setCameraQueue = setCameraQueue;
    }

    @Override
    public View getMapInstanceAndroidView() {
        return null;
    }

    @Override
    public void onResume() {

    }

    @Override
    public void onPause() {

    }

    @Override
    public void onDestroy() {

    }

    @Override
    public View onCreateView() {
        return null;
    }

    @Override
    public IMapEngineProperties getMapEngineProperties() {
        return null;
    }

    @Override
    public IMapEngineCapabilities getCapabilities() {
        this.oEngineCapabilities = new Capabilities(
                new FeatureTypeEnum[] {
                        FeatureTypeEnum.GEO_MIL_SYMBOL,
                        FeatureTypeEnum.GEO_POINT,
                        FeatureTypeEnum.GEO_PATH,
                        FeatureTypeEnum.GEO_POLYGON
                },
                new WMSVersionEnum[] {WMSVersionEnum.VERSION_1_1, WMSVersionEnum.VERSION_1_1_1, WMSVersionEnum.VERSION_1_3});
        return this.oEngineCapabilities;
    }

    @Override
    public IMapEngineRequirements getRequirements() {
        return null;
    }

    @Override
    public ICapture getCapture() {
        return null;
    }

    //
    // This method should be removed. Storage Manager should just call addFeature/removeFeature as necessary.
    //

    @Override
    public void setVisibility(ISetVisibilityList visibilityList) {
//        for (UUID featureUUID: visibilityList.keySet()) {
//            if(!visibilityList.get(featureUUID))
//                removeFeatureQueue.add(featureUUID);
//        }
    }

    @Override
    public void addFeatures(FeatureVisibilityList features) {
        Log.d(TAG, "In addFeatures");
        for (FeatureVisibility featureVisibility: features) {
            Log.d(TAG, "In addFeatures " + featureVisibility.feature.getClass().getName() + " " + featureVisibility.feature.getGeoId());
            addFeatureQueue.add(featureVisibility.feature);
        }
    }

    @Override
    public void removeFeatures(IUUIDSet features) {
        Log.d(TAG, "In removeFeatures");
        for (java.util.UUID uniqueId: features) {
            removeFeatureQueue.add(uniqueId);
        }
    }

    @Override
    public void addMapService(IMapService mapService) {

    }

    @Override
    public void removeMapService(IMapService mapService) {

    }

    @Override
    public ICamera getCamera() {
        return currentCamera;
    }

    @Override
    public void setCamera(ICamera oCamera, boolean animate) {

        if(oCamera != this.currentCamera) {
//            ICamera camera = new Camera();
//            camera.copySettingsFrom(oCamera);

            try {
                oCamera.addCameraEventListener(new ICameraEventListener() {
                    @Override
                    public void onEvent(CameraEvent event) {
                        MockMapInstance_.this.setCameraQueue.add(event.getCamera());
                    }
                });
            } catch(EMP_Exception e) {
                Log.e(TAG, "setCamera ", e);
            }
            this.currentCamera = oCamera;

            Log.d(TAG, oCamera.getAltitude() + " altitude");
            Log.d(TAG, oCamera.getAltitudeMode() + " altitudeMode");
            Log.d(TAG, oCamera.getLatitude() + " latitude");
            Log.d(TAG, oCamera.getLongitude() + " longitude");
            Log.d(TAG, oCamera.getHeading() + " heading");
            Log.d(TAG, oCamera.getRoll() + " roll");
            Log.d(TAG, oCamera.getTilt() + " tilt");
        }
        setCameraQueue.add(oCamera);
    }

    @Override
    public void setLookAt(ILookAt oLookAt, boolean animate) {

    }

    @Override
    public ILookAt getLookAt() {
        return null;
    }

    @Override
    public void setBounds(IGeoBounds bounds) {

    }

    @Override
    public void setMotionLockMode(MapMotionLockEnum mode) {

    }

    @Override
    public void registerMilStdRenderer(IMilStdRenderer oRenderer) {

    }

    @Override
    public double getFieldOfView() {
        return 45.0;
    }

    @Override
    public int getViewHeight() {
        return 600;
    }

    @Override
    public int getViewWidth() {
        return 500;
    }

    @Override
    public void setFarDistanceThreshold(double dValue) {
    }

    @Override
    public void setMidDistanceThreshold(double dValue) {
    }

    @Override
    public void getVersionInformation(StringBuilder builder, List<String> buildConfigFields) {

    }

    @Override
    public IGeoBounds getMapBounds() {
        return null;
    }

    @Override
    public Point geoToContainer(IGeoPosition pos) {
        return null;
    }

    @Override
    public IGeoPosition containerToGeo(Point point) {
        return null;
    }

    @Override
    public void selectFeatures(List<IFeature> featureList) {

    }

    @Override
    public void deselectFeatures(List<IFeature> featureList) {

    }
}
