package mil.emp3.api.mock;

import android.graphics.Point;
import android.util.Log;

import org.cmapi.primitives.GeoPosition;
import org.cmapi.primitives.IGeoAltitudeMode;
import org.cmapi.primitives.IGeoPosition;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;

import mil.emp3.api.enums.UserInteractionEventEnum;
import mil.emp3.api.interfaces.ICamera;
import mil.emp3.api.interfaces.IFeature;
import mil.emp3.core.editors.ControlPoint;

/**
 * This class contains methods used by the unit tests to verify results and simulate Gestures.
 */
public class MockMapInstance extends MockMapInstance_ {

    private static String TAG = MockMapInstance.class.getSimpleName();

    public MockMapInstance(BlockingQueue<IFeature> addFeatureQueue, BlockingQueue<UUID> removeFeatureQueue,
                           BlockingQueue<ICamera> setCameraQueue, BlockingQueue<IFeature> selectFeatureQueue,
                           BlockingQueue deselectFeatureQueue) {
        super(addFeatureQueue, removeFeatureQueue, setCameraQueue, selectFeatureQueue, deselectFeatureQueue);
    }

    public boolean validateAddFeatures(IFeature ... feature) throws InterruptedException {
        java.util.List<IFeature> features = new ArrayList<>();
        for(IFeature aFeature : feature) {
            features.add(aFeature);
        }
        return validateAddFeatures(features);
    }

    public boolean validateAddFeatures(IFeature feature) throws InterruptedException {
        java.util.List<IFeature> features = new ArrayList<>();
        features.add(feature);
        return validateAddFeatures(features);
    }

    public boolean validateAddFeatures(List<IFeature> features) throws InterruptedException {
        if((null == features) || (0 == features.size())) {
            if(addFeatureQueue.size() == 0) return true;
            else {
                Log.e(TAG, "addFeatureQueue should but empty but has " + removeFeatureQueue.size() + " elements");
                return false;
            }
        }

        while(!addFeatureQueue.isEmpty()) {
            IFeature queuedFeature = addFeatureQueue.take();
            if(features.contains(queuedFeature)) {
                features.remove(queuedFeature);
            } else {
                Log.e(TAG, "Feature " + queuedFeature.getName() + " is in queue but not in feature list");
                return false;
            }
        }

        if(!addFeatureQueue.isEmpty()) {
            Log.e(TAG, "There are remaining queuedFeatures remaining count " + addFeatureQueue.size());
            return false;
        } else if(!features.isEmpty()) {
            Log.e(TAG, "Number of features not found in the addFeaturesQueue count " + features.size());
            return false;
        }
        return true;
    }

    public boolean validateAddTypes(List<String> features) throws InterruptedException {
        if((null == features) || (0 == features.size())) {
            if(addFeatureQueue.size() == 0) return true;
            else {
                Log.e(TAG, "addFeatureQueue should be empty but has " + addFeatureQueue.size() + " elements");
                return false;
            }
        }

        if(features.size() != addFeatureQueue.size()) {
            Log.e(TAG, "validateAddType size does't match " + features.size() + " " + addFeatureQueue.size());
            return false;
        }
        while(!addFeatureQueue.isEmpty()) {
            IFeature queuedFeature = addFeatureQueue.take();
            if(features.contains(queuedFeature.getClass().getName())) {
                features.remove(queuedFeature.getClass().getName());
            } else {
                Log.e(TAG, "Feature " + queuedFeature.getClass().getName() + " is in queue but not in feature list");
                return false;
            }
        }

        if(!addFeatureQueue.isEmpty()) {
            Log.e(TAG, "There are remaining queuedFeatures remaining count " + addFeatureQueue.size());
            return false;
        } else if(!features.isEmpty()) {
            Log.e(TAG, "Number of features not found in the addFeaturesQueue count " + features.size());
            return false;
        }
        return true;
    }

    public List<IFeature> getControlPoints() {
        List<IFeature> cpList = new ArrayList<>();
        for(IFeature f: addFeatureQueue) {
            if(f instanceof ControlPoint) {
                cpList.add(f);
            }
        }
        return cpList;
    }
    public boolean validateRemoveFeatures(UUID ... feature) throws InterruptedException {
        java.util.List<UUID> features = new ArrayList<>();
        for(UUID aFeature : feature) {
            features.add(aFeature);
        }
        return validateRemoveFeatures(features);
    }

    public boolean validateRemoveFeatures(UUID feature) throws InterruptedException {
        java.util.List<UUID> features = new ArrayList<>();
        features.add(feature);
        return validateRemoveFeatures(features);
    }

    public boolean validateRemoveFeatures(List<UUID> features) throws InterruptedException {
        if((null == features) || (0 == features.size())) {
            if(removeFeatureQueue.size() == 0) return true;
            else {
                Log.e(TAG, "removeFeatureQueue should but empty but has " + removeFeatureQueue.size() + " elements");
                return false;
            }
        }

        while(!removeFeatureQueue.isEmpty()) {
            UUID queuedFeature = removeFeatureQueue.take();
            if(features.contains(queuedFeature)) {
                features.remove(queuedFeature);
            } else {
                Log.d(TAG, "Feature " + queuedFeature.toString() + " is in queue but not in feature list");
                return false;
            }
        }

        if(!removeFeatureQueue.isEmpty()) {
            Log.d(TAG, "There are remaining queuedFeatures remaining count " + removeFeatureQueue.size());
            return false;
        }  else if(!features.isEmpty()) {
            Log.e(TAG, "Number of features not found in the removeFeatureQueue count " + features.size());
            return false;
        }
        return true;
    }

    public boolean validateRemoveCount(int count) throws InterruptedException {
        if(0 == count) {
            if((null == removeFeatureQueue) || (removeFeatureQueue.size() == 0)) { return true; }
            else {
                Log.e(TAG, "removeFeatureQueue should but empty but has " + removeFeatureQueue.size() + " elements");
                return false;
            }
        } else if((null != removeFeatureQueue) && (count == removeFeatureQueue.size())) {
            removeFeatureQueue.clear();
            return true;
        }

        if(null != removeFeatureQueue) {
            removeFeatureQueue.clear();
        }
        return false;
    }

    private boolean areEqual(String what, double a, double b) {
        if(Math.abs(a-b) < .001) {
            return true;
        } else {
            Log.e(TAG, what + " a/b " + a + "/" + b);
            return false;
        }
    }

    public boolean validateCamera(double altitude, IGeoAltitudeMode.AltitudeMode altitudeMode, double latitude,
                                  double longitude, double heading, double roll, double tilt) {

        if((null == setCameraQueue) || (1 != setCameraQueue.size())) {
            Log.e(TAG, "setCameraQueue is null or Empty");
            return false;
        }

        ICamera setCamera = setCameraQueue.remove();
        //boolean result = areEqual("altitude", setCamera.getAltitude(), altitude);
        boolean result = areEqual("latitude", setCamera.getLatitude(), latitude);
        result &= areEqual("longitude", setCamera.getLongitude(), longitude);
        result &= areEqual("heading", setCamera.getHeading(), heading);
        result &= areEqual("roll", setCamera.getRoll(), roll);
        result &= areEqual("tilt", setCamera.getTilt(), tilt);
        result &= (setCamera.getAltitudeMode().equals(altitudeMode));

        return result;
    }
    public boolean validateSetCamera(ICamera expectedCamera) throws InterruptedException {

        if(null == expectedCamera) {
            if ((null == setCameraQueue) || (0 == setCameraQueue.size())) {
                return true;
            } else {
                return false;
            }
        }
        if((null == setCameraQueue) || (1 != setCameraQueue.size())) {
            Log.e(TAG, "setCameraQueue is null or Empty");
            return false;
        }

        ICamera setCamera = setCameraQueue.remove();
        boolean result = areEqual("altitude", setCamera.getAltitude(), expectedCamera.getAltitude());
        result &= areEqual("latitude", setCamera.getLatitude(), expectedCamera.getLatitude());
        result &= areEqual("longitude", setCamera.getLongitude(), expectedCamera.getLongitude());
        result &= areEqual("heading", setCamera.getHeading(), expectedCamera.getHeading());
        result &= areEqual("roll", setCamera.getRoll(), expectedCamera.getRoll());
        result &= areEqual("tilt", setCamera.getTilt(), expectedCamera.getTilt());
        result &= (setCamera.getAltitudeMode().equals(expectedCamera.getAltitudeMode()));

        return result;
    }

    public boolean validateSetCameraCount(int expectedCount) {
        if(0 == expectedCount) {
            if ((null == setCameraQueue) || (0 == setCameraQueue.size())) {
                return true;
            } else {
                setCameraQueue.clear();
                return false;
            }
        } else if((null != setCameraQueue) && (expectedCount == setCameraQueue.size())) {
            setCameraQueue.clear();
            return true;
        } else if(null != setCameraQueue) {
            setCameraQueue.clear();
        }
        return false;
    }

    public void cleanQueues() {
        addFeatureQueue.clear();
        removeFeatureQueue.clear();
        setCameraQueue.clear();
        selectFeatureQueue.clear();
        deselectFeatureQueue.clear();
    }

    public void simulateUserInteractionEvent(UserInteractionEventEnum eventEnum, List<IFeature> pickList, double latitude, double longitude, double altitude) {
        IGeoPosition position = new GeoPosition();
        position.setLatitude(latitude);
        position.setLongitude(longitude);
        position.setAltitude(altitude);

        if(null != pickList) {
            generateFeatureUserInteractionEvent(eventEnum, null, null,
                    pickList, null, position, null);
        }
        generateMapUserInteractionEvent(eventEnum, null, null, null, position, null);
    }

    public void simulateUserInteractionEvent(UserInteractionEventEnum eventEnum, int X, int Y, double latitude, double longitude, double altitude) {
        IGeoPosition position = new GeoPosition();
        position.setLatitude(latitude);
        position.setLongitude(longitude);
        position.setAltitude(altitude);

        Point p = new Point();
        p.set(X, Y);
        generateMapUserInteractionEvent(eventEnum, null, null, p, position, null);
    }

    public void simulateFeatureDrag(UserInteractionEventEnum event, IFeature feature, double oldLat, double oldLon, double oldAlt, double newLat, double newLon, double newAlt) {
        IGeoPosition oldPosition = new GeoPosition();
        oldPosition.setLatitude(oldLat);
        oldPosition.setLongitude(oldLon);
        oldPosition.setAltitude(oldAlt);

        IGeoPosition newPosition = new GeoPosition();
        newPosition.setLatitude(newLat);
        newPosition.setLongitude(newLon);
        newPosition.setAltitude(newAlt);

        List<IFeature> pickList = new ArrayList<>();
        pickList.add(feature);

        generateFeatureUserInteractionEvent(event, null, null, pickList, null, newPosition, oldPosition);
    }

    public boolean validateSelectedFeatures(IFeature ... feature) throws InterruptedException {
        java.util.List<IFeature> features = new ArrayList<>();
        for(IFeature aFeature : feature) {
            features.add(aFeature);
        }
        return validateFeaturesAgainstQueue(features, selectFeatureQueue, "selectFeatureQueue");
    }

    public boolean validateDeselectedFeatures(IFeature ... feature) throws InterruptedException {
        java.util.List<IFeature> features = new ArrayList<>();
        for(IFeature aFeature : feature) {
            features.add(aFeature);
        }
        return validateFeaturesAgainstQueue(features, deselectFeatureQueue, "deselectFeatureQueue");
    }


    public boolean validateFeaturesAgainstQueue(List<IFeature> features, BlockingQueue<IFeature> queue, String queueName) throws InterruptedException {
        if((null == features) || (0 == features.size())) {
            if(queue.size() == 0) return true;
            else {
                Log.e(TAG, queueName + " should but empty but has " + queue.size() + " elements");
                return false;
            }
        }

        while(!queue.isEmpty()) {
            IFeature queuedFeature = queue.take();
            if(features.contains(queuedFeature)) {
                features.remove(queuedFeature);
            } else {
                Log.e(TAG, "Feature " + queuedFeature.getName() + " is in " + queueName + " but not in feature list");
                return false;
            }
        }

        if(!queue.isEmpty()) {
            Log.e(TAG, "There are remaining queuedFeatures in " + queueName + " remaining count " + queue.size());
            return false;
        } else if(!features.isEmpty()) {
            Log.e(TAG, "Number of features not found in the " + queue + " " + features.size());
            return false;
        }
        return true;
    }
}
