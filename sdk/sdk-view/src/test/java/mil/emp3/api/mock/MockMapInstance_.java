package mil.emp3.api.mock;


import android.graphics.Point;
import android.util.Log;
import android.view.View;

import org.cmapi.primitives.IGeoBounds;
import org.cmapi.primitives.IGeoPosition;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;

import mil.emp3.api.KMLS;
import mil.emp3.api.enums.FeatureTypeEnum;
import mil.emp3.api.enums.FontSizeModifierEnum;
import mil.emp3.api.enums.MapGridTypeEnum;
import mil.emp3.api.enums.MapMotionLockEnum;
import mil.emp3.api.enums.WMSVersionEnum;
import mil.emp3.api.enums.WMTSVersionEnum;
import mil.emp3.api.events.CameraEvent;
import mil.emp3.api.exceptions.EMP_Exception;
import mil.emp3.api.interfaces.ICamera;
import mil.emp3.api.interfaces.ICapture;
import mil.emp3.api.interfaces.IFeature;
import mil.emp3.api.interfaces.IKML;
import mil.emp3.api.interfaces.IKMLS;
import mil.emp3.api.interfaces.ILookAt;
import mil.emp3.api.interfaces.IMapService;
import mil.emp3.api.interfaces.IMapServiceResult;
import mil.emp3.api.interfaces.IUUIDSet;
import mil.emp3.api.listeners.ICameraEventListener;
import mil.emp3.mapengine.abstracts.CoreMapInstance;
import mil.emp3.mapengine.api.Capabilities;
import mil.emp3.mapengine.api.FeatureVisibility;
import mil.emp3.mapengine.api.FeatureVisibilityList;
import mil.emp3.mapengine.interfaces.IMapEngineCapabilities;
import mil.emp3.mapengine.interfaces.IMapEngineProperties;
import mil.emp3.mapengine.interfaces.IMapEngineRequirements;
import mil.emp3.mapengine.interfaces.IMapGridLines;
import mil.emp3.mapengine.interfaces.IMilStdRenderer;
import mil.emp3.mapengine.interfaces.ISetVisibilityList;

public class MockMapInstance_ extends CoreMapInstance {

    private static String TAG = MockMapInstance_.class.getSimpleName();
    protected final BlockingQueue<IFeature> addFeatureQueue;
    protected final BlockingQueue<UUID> removeFeatureQueue;
    protected final BlockingQueue<IFeature> selectFeatureQueue;
    protected final BlockingQueue<IFeature> deselectFeatureQueue;
    protected final BlockingQueue<ICamera> setCameraQueue;
    protected ICamera currentCamera;

    public MockMapInstance_(BlockingQueue<IFeature> addFeatureQueue, BlockingQueue<UUID> removeFeatureQueue,
                           BlockingQueue<ICamera> setCameraQueue, BlockingQueue<IFeature> selectFeatureQueue,
                            BlockingQueue deselectFeatureQueue) {
        super(TAG, null);
        this.addFeatureQueue = addFeatureQueue;
        this.removeFeatureQueue = removeFeatureQueue;
        this.setCameraQueue = setCameraQueue;
        this.selectFeatureQueue = selectFeatureQueue;
        this.deselectFeatureQueue = deselectFeatureQueue;
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
                new WMSVersionEnum[] {WMSVersionEnum.VERSION_1_1, WMSVersionEnum.VERSION_1_1_1,
                        WMSVersionEnum.VERSION_1_3, WMSVersionEnum.VERSION_1_3_0},
                new WMTSVersionEnum[] {WMTSVersionEnum.VERSION_1_0_0});
        return this.oEngineCapabilities;
    }

    @Override
    public IMapEngineRequirements getRequirements() {
        return null;
    }

    @Override
    public void addFeatures(FeatureVisibilityList features, Object userContext) {
        Log.d(TAG, "In addFeatures");
        for (FeatureVisibility featureVisibility: features) {
            Log.d(TAG, "In addFeatures " + featureVisibility.feature.getClass().getName() + " " + featureVisibility.feature.getGeoId());
            if (userContext != null) {
                Log.d(TAG, "In addFeatures user context " + (String)userContext);
            }
            addFeatureQueue.add(featureVisibility.feature);
        }
    }

    @Override
    public void removeFeatures(IUUIDSet features, Object userContext) {
        if (userContext != null) {
            Log.d(TAG, "In removeFeatures user context " + (String)userContext);
        } else {
            Log.d(TAG, "In removeFeatures");
        }

        for (java.util.UUID uniqueId: features) {
            removeFeatureQueue.add(uniqueId);
        }
    }

    private List<FeatureTypeEnum> kmlsFeatureTypes = new ArrayList<>();
    private int kmlsImageCount = 0;
    private IKML kmlFeatureAddedViaService = null;
    private IKML kmlFeatureRemovedViaService = null;

    @Override
    public void addMapService(IMapService mapService) {
        Log.d(TAG, "In addMapService ");
        kmlsFeatureTypes.clear();
        kmlsImageCount = 0;
        if(mapService instanceof IKMLS) {
            IKMLS kmlService = (IKMLS) mapService;
            kmlFeatureAddedViaService = kmlService.getFeature();
            Log.d(TAG, "Added " + kmlFeatureAddedViaService.getGeoId().toString());
            if((null != kmlService.getFeature()) && (null != kmlService.getFeature().getFeatureList())) {
                for(IFeature f: kmlService.getFeature().getFeatureList()) {
                    Log.d(TAG, "KML Feature " + f.getFeatureType().toString());
                    kmlsFeatureTypes.add(f.getFeatureType());
                }
            }

            if(null != kmlService.getFeature().getImageLayerList()) {
                Log.d(TAG, "KMl Image Count " + kmlService.getFeature().getImageLayerList().size());
                kmlsImageCount = kmlService.getFeature().getImageLayerList().size();
            }
        }
    }

    @Override
    public void addMapService(IMapService mapService, IMapServiceResult result) {
        addMapService(mapService);
    }

    public boolean validateAddKmlsFeatureCount(FeatureTypeEnum featureType, int expectedCount) {

        int actualCount = 0;
        for(FeatureTypeEnum fte: kmlsFeatureTypes) {
            if(fte.equals(featureType)) {
                actualCount++;
            }
        }

        if(expectedCount != actualCount) {
            Log.d(TAG, featureType.toString() + " Expected Count " + expectedCount + " Actual " + actualCount);
            return false;
        }
        return true;
    }

    public boolean validateAddKmlsImageCount(int expectedCount) {
        if(expectedCount != kmlsImageCount) {
            Log.d(TAG, "Expected Image Count " + expectedCount + " Actual " + kmlsImageCount);
            return false;
        }
        return true;
    }
    public void cleanKmls() {
        kmlsFeatureTypes.clear();
        kmlsImageCount = 0;
    }

    @Override
    public void removeMapService(IMapService mapService) {
        Log.d(TAG, "In removeMapService ");
        if(mapService instanceof IKMLS) {
            IKMLS kmlService = (IKMLS) mapService;
            kmlFeatureRemovedViaService = kmlService.getFeature();
            Log.d(TAG, "Removed " + kmlFeatureRemovedViaService.getGeoId().toString());
            if(null != kmlFeatureAddedViaService) {
                Log.d(TAG, "Added " + kmlFeatureAddedViaService.getGeoId().toString());
            }

        }
    }

    @Override
    public void removeMapService(IMapService mapService, IMapServiceResult result) {
        removeMapService(mapService);
    }

    public boolean validateRemoveKmlService() {
        boolean status = false;
        if(null != kmlFeatureAddedViaService && null != kmlFeatureRemovedViaService) {
            if(kmlFeatureAddedViaService.getGeoId().equals(kmlFeatureRemovedViaService.getGeoId())) {
                status = true;
            }
        }
        kmlFeatureAddedViaService = null;
        kmlFeatureRemovedViaService = null;
        return status;
    }
    @Override
    public ICamera getCamera() {
        return currentCamera;
    }

    @Override
    public void setCamera(ICamera oCamera, boolean animate, Object userContext) {
        if (userContext != null) {
            Log.d(TAG, "In setCamera user context " + (String)userContext);
        }
        if(oCamera != this.currentCamera) {
//            ICamera camera = new Camera();
//            camera.copySettingsFrom(oCamera);

            try {
                oCamera.addCameraEventListener(new ICameraEventListener() {
                    @Override
                    public void onEvent(CameraEvent event) {
                        MockMapInstance_.this.setCameraQueue.add(event.getCamera());
                        if (event.getUserContext() != null) {
                            Log.d(TAG, "In addCameraEventListener user context " + (String) event.getUserContext());
                        }
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
    public void applyCameraChange(ICamera oCamera, boolean animate, Object userContext) { }

    @Override
    public void setLookAt(ILookAt oLookAt, boolean animate, Object userContext) {

    }

    @Override
    public ILookAt getLookAt() {
        return null;
    }

    @Override
    public void applyLookAtChange(ILookAt oLookAt, boolean animate, Object userContext) { }

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
        Log.d(TAG, "In selectFeatures");
        for (IFeature feature: featureList) {
            Log.d(TAG, "In selectFeatures " + feature.getClass().getName() + " " + feature.getGeoId());
            selectFeatureQueue.add(feature);
        }
    }

    @Override
    public void deselectFeatures(List<IFeature> featureList) {
        Log.d(TAG, "In deselectFeatures");
        for (IFeature feature: featureList) {
            Log.d(TAG, "In deselectFeatures " + feature.getClass().getName() + " " + feature.getGeoId());
            deselectFeatureQueue.add(feature);
        }
    }

    @Override
    public void setFontSizeModifier(FontSizeModifierEnum value) {

    }

    @Override
    public void setBackgroundBrightness(int setting) {
    }

    @Override
    public int getBackgroundBrightness() {
        return 50;
    }

    @Override
    public void setMapGridGenerator(IMapGridLines gridGenerator) {

    }

    @Override
    public void scheduleMapRedraw() {

    }

    @Override
    public android.view.View showMiniMap() {
        return null;
    }

    @Override
    public void hideMiniMap() {

    }
}

