package mil.emp3.core.utils;

import android.util.Log;

import org.cmapi.primitives.GeoBounds;
import org.cmapi.primitives.GeoPosition;
import org.cmapi.primitives.IGeoAltitudeMode;
import org.cmapi.primitives.IGeoBounds;
import org.cmapi.primitives.IGeoPosition;

import java.util.ArrayList;
import java.util.List;

import mil.emp3.api.enums.FeatureTypeEnum;
import mil.emp3.api.enums.VisibilityStateEnum;
import mil.emp3.api.exceptions.EMP_Exception;
import mil.emp3.api.interfaces.ICamera;
import mil.emp3.api.interfaces.IGeoJSON;
import mil.emp3.api.interfaces.IKML;
import mil.emp3.api.interfaces.core.ICoreManager;
import mil.emp3.api.interfaces.IFeature;
import mil.emp3.api.interfaces.IMap;
import mil.emp3.api.interfaces.core.IStorageManager;
import mil.emp3.api.utils.GeographicLib;
import mil.emp3.api.utils.ManagerFactory;
import mil.emp3.mapengine.interfaces.IMapInstance;

/**
 * This class contains utility methods used by the zoomTo methods in Core Manager
 */
public class ZoomToUtils {
    private static String TAG = ZoomToUtils.class.getSimpleName();
    private static ZoomToUtils instance = new ZoomToUtils();

    final private IStorageManager storageManager = ManagerFactory.getInstance().getStorageManager();
    final private ICoreManager coreManager       = ManagerFactory.getInstance().getCoreManager();
    
    // If there is single visible feature on which zoomTo is invoked then we need some bounding box
    // to calculate the camera altitude. Following is the offset we will use

    public final static double single_point_offset = .01;
    public final static double distance_adjustment_for_edge_case = 1.10; // when features are at the corner of the bounding box
    public static ZoomToUtils getInstance() {
        return instance;
    }

    private ZoomToUtils() {
    }

    /**
     * Procedure:
     *     validate parameters
     *     Create a position list from the visible features
     *     get center lat/long for the feature list
     *     get the bound box for the features
     *     invoke zoomTo to calculate camera altitude and set camera
     * @param clientMap
     * @param featureList
     * @param animate
     */
    public void zoomTo(IMap clientMap, List<IFeature> featureList, boolean animate) {
        if((null == clientMap) || (null == featureList)) {
            Log.e(TAG, "clientMap and featureList must be non null");
            return;
        }
        if(0 == featureList.size()) {
            Log.d(TAG, "no feature to zoomTo");
            return;
        }

        List<IGeoPosition> positionList = new ArrayList<>();
        for(IFeature feature : featureList) {
            if(VisibilityStateEnum.VISIBLE == storageManager.getVisibilityOnMap(clientMap, feature)) {
                if (feature.getFeatureType() == FeatureTypeEnum.GEOJSON) {
                    for (IFeature feat : ((IGeoJSON)feature).getFeatureList()) {
                        if (null != feature.getPositions()) {
                            positionList.addAll(feat.getPositions());
                        }
                    }
                } else if (feature.getFeatureType() == FeatureTypeEnum.KML) {
                    for (IFeature feat : ((IKML)feature).getFeatureList()) {
                        if (null != feature.getPositions()) {
                            positionList.addAll(feat.getPositions());
                        }
                    }
                } else {
                    if (null != feature.getPositions()) {
                        positionList.addAll(feature.getPositions());
                    }
                }
            }
        }

        if(0 == positionList.size()) {
            Log.e(TAG, "Cannot zoom to a feature(s) with no positions");
            return;
        }

        IGeoPosition center = GeographicLib.getCenter(positionList);
        BoundingBox boundingBox = getBoundingBox(positionList);

        if((null != boundingBox) && (boundingBox.isSinglePoint())) {
            double leftLongitude, rightLongitude, bottomLatitude, topLatitude;

            leftLongitude = GeographicLib.wrapLongitude(boundingBox.getLeftLongitude() - single_point_offset);
            rightLongitude = GeographicLib.wrapLongitude(boundingBox.getRightLongitude() + single_point_offset);

            if((boundingBox.getBottomLatitude() + single_point_offset) >  90.0) {
                // An attempt to handle the situation at pole, until we find a better solution
                bottomLatitude = boundingBox.getBottomLatitude() - single_point_offset ;
                topLatitude = boundingBox.getBottomLatitude();
            } else if ((boundingBox.getBottomLatitude() - single_point_offset) < -90.0) {
                // An attempt to handle the situation at pole, until we find a better solution
                bottomLatitude = boundingBox.getBottomLatitude();
                topLatitude = boundingBox.getBottomLatitude() + single_point_offset;
            } else {
                bottomLatitude = boundingBox.getBottomLatitude() - single_point_offset;
                topLatitude = boundingBox.getTopLatitude() + single_point_offset;
            }

            double maxAltitude = boundingBox.getMaxAltitude();
            boundingBox = new BoundingBox(leftLongitude, rightLongitude, bottomLatitude, topLatitude, maxAltitude, true);
        }

        if((null != center) && (null != boundingBox)) {
            zoomTo(clientMap, center, boundingBox, animate);
        } else {
            Log.e(TAG, "center or bounding box is null");
        }
    }

    /**
     * This method is used by CoreManager.setExtent.
     * adjust the bounds if user specified a single point.
     *     First create a list positions which consists of the four corners of th bounding box.
     *     Find the center of those four positions
     *     Create a BoundingBox object usin gbounds
     *     calculate the camera altitude
     *
     * @param clientMap
     * @param bounds
     * @param animate
     */
    public void zoomTo(IMap clientMap, IGeoBounds bounds, boolean animate) {
        List<IGeoPosition> positionList = new ArrayList<>();

        bounds = adjustBounds(bounds);
        positionList.add(getPosition(bounds.getNorth(), bounds.getEast()));
        positionList.add(getPosition(bounds.getNorth(), bounds.getWest()));
        positionList.add(getPosition(bounds.getSouth(), bounds.getEast()));
        positionList.add(getPosition(bounds.getSouth(), bounds.getWest()));
        IGeoPosition center = GeographicLib.getCenter(positionList);

        BoundingBox boundingBox = new BoundingBox(bounds.getWest(), bounds.getEast(), bounds.getSouth(), bounds.getNorth(), 0.0, false);
        if((null != center) && (null != boundingBox)) {
            zoomTo(clientMap, center, boundingBox, animate);
        } else {
            Log.e(TAG, "center or bounding box is null");
        }
    }

    /**
     * Adjust the bounds if we have a single point, offset the longitudes.
     * Caution this still doesn't completely address the
     * situation at the poles
     * @param bounds
     * @return
     */
    private IGeoBounds adjustBounds(IGeoBounds bounds) {
        if((bounds.getEast() != bounds.getWest()) || (bounds.getNorth() != bounds.getSouth())) {
            // no adjustment required.
            return bounds;
        }
        IGeoBounds adjustedBounds = new GeoBounds();

        adjustedBounds.setNorth(bounds.getNorth());
        adjustedBounds.setSouth(bounds.getSouth());
        adjustedBounds.setEast(GeographicLib.wrapLongitude(bounds.getEast() + single_point_offset));
        adjustedBounds.setWest(GeographicLib.wrapLongitude(bounds.getWest() - single_point_offset));

        return adjustedBounds;
    }
    /**
     * Builds a IGeoPosition object from latitude and longitude
     * @param latitude
     * @param longitude
     * @return
     */
    private IGeoPosition getPosition(double latitude, double longitude) {
        IGeoPosition geoPosition = new GeoPosition();
        geoPosition.setLatitude(latitude);
        geoPosition.setLongitude(longitude);
        return geoPosition;
    }

    /**
     * Procedure:
     *     Convert each lat/long pair into a unit-length 3D vector.
     *     Check against current min/max X/Y
     *     If new min/max X/Y is found then update the appropriate mix/max X/Y Index
     *     Use the min/max X/Y index to build a bounding box
     *     Also keeps track of maximum altitude in the list
     * @param positionList
     * @return
     */
    private BoundingBox getBoundingBox(List<IGeoPosition> positionList) {

        BoundingBox boundingBox = null;
        if((null != positionList) && (0 != positionList.size())) {

            if(1 == positionList.size()) {
                boundingBox = new BoundingBox(positionList.get(0).getLongitude(), positionList.get(0).getLongitude(),
                        positionList.get(0).getLatitude(), positionList.get(0).getLatitude(), positionList.get(0).getAltitude(), true);
            } else {
                double maxX = 0;
                double maxY = 0;
                double minX = 0;
                double minY = 0;
                double x = 0;
                double y = 0;
                int maxXIndex = 0;
                int minXIndex = 0;
                int maxYIndex = 0;
                int minYIndex = 0;
                double maxAltitude = 0.0;

                boolean initialized = false;
                int index = 0;
                for(IGeoPosition position: positionList)
                {
                    double latitude = position.getLatitude() * Math.PI / 180;
                    double longitude = position.getLongitude() * Math.PI / 180;

                    x = Math.cos(latitude) * Math.cos(longitude);
                    y = Math.cos(latitude) * Math.sin(longitude);

                    if(!initialized) {
                        initialized = true;
                        maxX = x;
                        minX = x;
                        maxY = y;
                        minY = y;
                        maxAltitude = position.getAltitude();
                    } else {
                        if(maxX < x) {
                            maxX = x;
                            maxXIndex = index;
                        } else if(minX > x) {
                            minX = x;
                            minXIndex = index;
                        }

                        if(maxY < y) {
                            maxY = y;
                            maxYIndex = index;
                        } else if(minY > y) {
                            minY = y;
                            minYIndex = index;
                        }

                        if(maxAltitude < position.getAltitude()) {
                            maxAltitude = position.getAltitude();
                        }
                    }
                    index ++;
                }

                boundingBox = new BoundingBox(positionList.get(minYIndex).getLongitude(), positionList.get(maxYIndex).getLongitude(),
                        positionList.get(minXIndex).getLatitude(), positionList.get(maxXIndex).getLatitude(), maxAltitude, false);

                Log.d(TAG, "BoundingBox with index left/right/botton/top " + boundingBox.getLeftLongitude() + "/" + boundingBox.getRightLongitude() + "/" +
                        boundingBox.getBottomLatitude() + "/" + boundingBox.getTopLatitude());
            }
        }

        return boundingBox;
    }

    /**
     * Holds the bounding box that is used to calculate camera altitude.
     */
    private class BoundingBox {
        final boolean isSinglePoint;
        final double leftLongitude;
        final double rightLongitude;
        final double bottomLatitude;
        final double topLatitude;
        final double maxAltitude;

        BoundingBox(double leftLongitude, double rightLongitude, double bottomLatitide, double topLatitude, double maxAltitude, boolean isSinglePoint) {
            this.leftLongitude = leftLongitude;
            this.rightLongitude = rightLongitude;
            this.bottomLatitude = bottomLatitide;
            this.topLatitude = topLatitude;
            this.isSinglePoint = isSinglePoint;
            this.maxAltitude = maxAltitude;
        }

        public double getLeftLongitude() {
            return leftLongitude;
        }

        public double getRightLongitude() {
            return rightLongitude;
        }

        public double getBottomLatitude() {
            return bottomLatitude;
        }

        public double getTopLatitude() {
            return topLatitude;
        }

        public boolean isSinglePoint() {
            return isSinglePoint;
        }

        public double getMaxAltitude() {
            return maxAltitude;
        }
    }

    /**
     * Calculate camera altitude in meters:
     *     Calculate the distance from center to all four vertices of the bounding box
     *     Pick the largest distance
     *     Adjust the above calculated distance if max altitude is greater than 0
     *     Use the tangent of field-of-view to calculate the required altitude of the camera
     *
     *  Limitations
     *     These are approximate calculations and they err on the side of showing slightly more area than required
     *     Accuracy varies as you move from center to the poles.
     *     bounding box doesn't work at the poles.
     * @param boundingBox
     * @param fieldOfViewRadians
     * @return
     */
    private double zoomToCalcAltitudeInMeters(BoundingBox boundingBox, IGeoPosition center, double fieldOfViewRadians) {
        double altitude = 0;

        // Set up four vertices of the rectangle
        IGeoPosition leftTop = new GeoPosition();
        leftTop.setLatitude(boundingBox.getTopLatitude());
        leftTop.setLongitude(boundingBox.getLeftLongitude());
        IGeoPosition leftBottom = new GeoPosition();
        leftBottom.setLatitude(boundingBox.getBottomLatitude());
        leftBottom.setLongitude(boundingBox.getLeftLongitude());

        IGeoPosition rightTop = new GeoPosition();
        rightTop.setLatitude(boundingBox.getTopLatitude());
        rightTop.setLongitude(boundingBox.getRightLongitude());
        IGeoPosition rightBottom = new GeoPosition();
        rightBottom.setLatitude(boundingBox.getBottomLatitude());
        rightBottom.setLongitude(boundingBox.getRightLongitude());

        // get distance from center to each of the vertices
        double distanceToLeftTop = GeographicLib.computeDistanceBetween(leftTop, center);
        double distanceToLeftBottom = GeographicLib.computeDistanceBetween(leftBottom, center);
        double distanceToRightTop = GeographicLib.computeDistanceBetween(rightTop, center);
        double distanceToRightBottom = GeographicLib.computeDistanceBetween(rightBottom, center);

        // Find the largest distance
        double distanceToUse = distanceToLeftTop;
        if(distanceToUse < distanceToLeftBottom) {
            distanceToUse = distanceToLeftBottom;
        }
        if(distanceToUse < distanceToRightTop) {
            distanceToUse = distanceToRightTop;
        }
        if(distanceToUse < distanceToRightBottom) {
            distanceToUse = distanceToRightBottom;
        }

        // Adjust the distance for the altitude, similar triangles.
        Log.d(TAG, "distanceToUse at 0 altitude " + distanceToUse);

        distanceToUse *= distance_adjustment_for_edge_case;
        Log.d(TAG, "adjusted for edge case " + distanceToUse);
        // Calculate the required altitude
        // tan(a/2) = (s/2) / d => d = (s/2) / tan(a/2)
        // a - field of view in radians
        // s - longer axix in meters
        // d - camera latitude
        altitude = (distanceToUse) / Math.tan(fieldOfViewRadians/2);

        if(boundingBox.getMaxAltitude() > 0) {
            altitude += boundingBox.getMaxAltitude();
            Log.d(TAG, "adjusting altitude by " + boundingBox.getMaxAltitude());
        }
        return altitude;
    }

    /**
     * Move camera to the position specified by center.
     * Set altitude to calculated altitude
     * Set roll, tilt, heading to zero.
     * @param clientMap
     * @param center
     * @param boundingBox
     */
    private void zoomTo(IMap clientMap, IGeoPosition center, BoundingBox boundingBox, boolean animate) {
        IMapInstance mapInstance = storageManager.getMapInstance(clientMap);
        ICamera camera = coreManager.getCamera(clientMap);

        if((null != mapInstance) && (null != camera)) {
            camera.setLatitude(center.getLatitude());
            camera.setLongitude(center.getLongitude());

            double altitudeInMeters = zoomToCalcAltitudeInMeters(boundingBox, center, mapInstance.getFieldOfView() * (Math.PI / 180.0));
            Log.d(TAG, "zoomTo: altitude " + altitudeInMeters);

            camera.setAltitude(altitudeInMeters);
            camera.setAltitudeMode(IGeoAltitudeMode.AltitudeMode.CLAMP_TO_GROUND);
            camera.setHeading(0);
            camera.setRoll(0);
            camera.setTilt(0);

            try {
                // we may need to revisit this
                // don't know if zoom is always a self-event
                coreManager.setCamera(clientMap, camera, animate, null);
            } catch (EMP_Exception e) {
                Log.e(TAG, "zoomTo " + center.getLatitude() + ":" + center.getLongitude(), e);
            }
        } else {
            Log.e(TAG, "zoomTo: camera or mapInstance is null zoomTo failed");
        }
    }
}
