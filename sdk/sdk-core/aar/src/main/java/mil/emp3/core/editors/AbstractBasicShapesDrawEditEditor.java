package mil.emp3.core.editors;

import android.util.Log;

import org.cmapi.primitives.IGeoBounds;
import org.cmapi.primitives.IGeoPosition;

import java.util.ArrayList;
import java.util.List;

import mil.emp3.api.enums.FeatureEditUpdateTypeEnum;
import mil.emp3.api.enums.FeaturePropertyChangedEnum;
import mil.emp3.api.exceptions.EMP_Exception;
import mil.emp3.api.interfaces.IEditUpdateData;
import mil.emp3.api.interfaces.IEmpBoundingArea;
import mil.emp3.api.interfaces.IFeature;
import mil.emp3.api.listeners.IDrawEventListener;
import mil.emp3.api.listeners.IEditEventListener;
import mil.emp3.api.utils.GeoLibrary;
import mil.emp3.core.events.EditUpdateData;
import mil.emp3.mapengine.interfaces.IMapInstance;

/**
 * This class is designed as a base class for basic shapes that have single position, e.g. Square, Rectangle, Circle, Ellipse.
 * It is currently used by RectangleEditor and SquareEditor.
 *
 * @param <T>
 */
public abstract class AbstractBasicShapesDrawEditEditor<T extends IFeature> extends AbstractDrawEditEditor<T> {

    private static String TAG = AbstractBasicShapesDrawEditEditor.class.getSimpleName();
    protected  double currentBearing = 0;             // This is wrt to the rectangle axis that is parallel to width. We start with 0
    private final double bearingTolerance = .5;
    protected double originalAzimuth;


    protected AbstractBasicShapesDrawEditEditor(IMapInstance map, T feature, IEditEventListener oEventListener, boolean bUsesCP) throws EMP_Exception {
        super(map, feature, oEventListener, bUsesCP);
    }

    protected AbstractBasicShapesDrawEditEditor(IMapInstance map, T feature, IDrawEventListener oEventListener, boolean bUsesCP) throws EMP_Exception {
        super(map, feature, oEventListener, bUsesCP);
    }

    protected abstract void saveOriginalState();

    @Override
    protected void prepareForDraw() throws EMP_Exception {
        this.oFeature.getPositions().clear();
        this.oFeature.getPositions().add(getCenter());
    }

    @Override
    protected  void prepareForEdit() throws EMP_Exception {
        saveOriginalState();
    }

    /**
     * User will drag the height and width CPs; the drag will not be exactly along the current major/minor axis. This algorithm takes the projection
     * of new position of the CP on major/minor axis to determine the final position of the CP on one of those axis. The 'intersection' method calculates
     * the adjusted position so that it is on one of the axis.
     *
     * Depending on which quadrant of the transformed coordinate system (with respect to the azimuth) user dragged the height or width CP to, we need to calculate the
     * intersection point. To find the intersection with the major axis we have to move the width CP either down (currentBearing + 180) or up (current bearing + 0).
     * Tp find the intersection with minor axis we have to move the height CP left or right with respect to the minor axis.
     * BearingOffsetFirst and bearingOffsetTwo specify these two angles. BearingOffsetThree tells us the direction in which we need to move from the center
     * to find an intersection point.
     *
     * You will need to draw a picture to visualize this or look at the comments in the intersection method.
     *
     * @parm featurePosition - current position of the feature
     * @param oLatLon - position to which user dragged the CP
     * @param bearingOffsetFirst - Try this movement (for oLatLon) first, if it is parallel to the desired axis then will return null
     * @param bearingOffsetTwo - Try this movement (for oLatLon) if previous one returns null.
     * @param bearingOffsetThree - This is the bearing of the major/minor axis.
     * @return
     */
    protected IGeoPosition restrictCPMoveAlongAxis(IGeoPosition oLatLon, double bearingOffsetFirst, double bearingOffsetTwo, double bearingOffsetThree) {
        IGeoPosition adjustedLatLon;
        try {
            Log.d(TAG, "currentBearing " + currentBearing);
            adjustedLatLon = GeoLibrary.intersection(oLatLon, currentBearing + bearingOffsetFirst, getFeaturePosition(), currentBearing + bearingOffsetThree);
            if (null == adjustedLatLon) {
                adjustedLatLon = GeoLibrary.intersection(oLatLon, currentBearing + bearingOffsetTwo, getFeaturePosition(), currentBearing + bearingOffsetThree);
            }
        } catch(Exception e) {
            Log.e(TAG, "restrictCPMoveAlongAxis ", e);
            adjustedLatLon = null;
        }

        Log.d(TAG, "oLatLon " + oLatLon.getLatitude() + " " + oLatLon.getLongitude() + " adjusted " +
                adjustedLatLon.getLatitude() + " " + adjustedLatLon.getLongitude());
        return adjustedLatLon;
    }

    /**
     * We restrict the movement of CPs towards the center based on minimum distance. We use the bearing of the new position to make sure that we haven't moved
     * beyond the center causing a change to azimuth.
     *
     * @param oLatLon
     * @param bearingOffset
     * @param distanceMultiplier
     * @return
     */
    protected boolean restrictDistanceToCenter(IGeoPosition oLatLon, double bearingOffset, double distanceMultiplier, double axisLength) {
        boolean distanceIsAllowed = true;

        try {
            double bearingFromCenterToRight = GeoLibrary.computeBearing(getFeaturePosition(), oLatLon);
            Log.d(TAG, "center to LatLon bearing " + bearingFromCenterToRight + " currentBearing " + currentBearing);

            double compareToThis = (currentBearing + bearingOffset) % 360;
            if (Math.abs(compareToThis - bearingFromCenterToRight) > bearingTolerance) {
                distanceIsAllowed = false;
            } else {
                double distancetoLatLon = GeoLibrary.computeDistanceBetween(getFeaturePosition(), oLatLon);
                double minDistance = getMinDistance(distanceMultiplier);
                if(minDistance < 0) {
                    minDistance = oClientMap.getCamera().getAltitude() * distanceMultiplier;
                }
                Log.d(TAG, "right distance and min distance " + distancetoLatLon + " " + minDistance);
                if ((distancetoLatLon < minDistance) || (distancetoLatLon - (axisLength / 2) > oClientMap.getCamera().getAltitude())) {
                    distanceIsAllowed = false;
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "restrictDistanceToCenter ", e);
            distanceIsAllowed = false;
        }
        return distanceIsAllowed;
    }

    protected double getMinDistance(double multiplier) {
        return -1.0;
    }
    /**
     * This is null operation as we have sendPositionUpdateEvent being sent.
     */
    @Override
    protected void sendUpdateEvent() {

    }

    protected abstract void recompute(IGeoPosition center );
    protected abstract void setFeaturePosition(IGeoPosition position);
    protected abstract IGeoPosition getFeaturePosition();

    @Override
    protected boolean doFeatureMove(double dBearing, double dDistance) {
        IGeoPosition newPosition = GeoLibrary.computePositionAt(dBearing, dDistance, getFeaturePosition());
        setFeaturePosition(newPosition);
        this.oFeature.apply();
        addUpdateEventData(FeatureEditUpdateTypeEnum.POSITION_UPDATED);
        issueUpdateEvent();
        recompute(getFeaturePosition());
        return true;
    }

}
