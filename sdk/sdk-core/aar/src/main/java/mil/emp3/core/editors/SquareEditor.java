package mil.emp3.core.editors;


import android.util.Log;

import org.cmapi.primitives.GeoPosition;
import org.cmapi.primitives.IGeoAltitudeMode;
import org.cmapi.primitives.IGeoBounds;
import org.cmapi.primitives.IGeoPosition;

import mil.emp3.api.Square;
import mil.emp3.api.enums.FeaturePropertyChangedEnum;
import mil.emp3.api.exceptions.EMP_Exception;
import mil.emp3.api.interfaces.ICamera;
import mil.emp3.api.listeners.IDrawEventListener;
import mil.emp3.api.listeners.IEditEventListener;
import mil.emp3.api.utils.EmpGeoPosition;
import mil.emp3.api.utils.GeoLibrary;
import mil.emp3.mapengine.interfaces.IMapInstance;

public class SquareEditor extends AbstractBasicShapesDrawEditEditor<Square> {
    private static String TAG = RectangleEditor.class.getSimpleName();

    // All the following multiplier are based experimenting with various camera setting. There was no mathematical
    // formulae for arriving at these numbers.

    // Following two are used if bounds is null, which should never happen???
    private final double lengthMultiplier = .20;    // When drawing first time used to multiply camera altitude to get width
    private final double minLengthMultiplier = (lengthMultiplier/5);

    // Following is used when bounds are used to calculate initial lenght and minimum length
    private final double boundsLenghtMultiplier = 0.10;
    private final double boundsScaleFatcor = 1.5;
    private final double boundsMinLenghtMultiplier = (boundsLenghtMultiplier/5);
    private double currentLength = 0;

    // Following are used to restore the feature state. Note that base class will restore the position.
    private double originalLength;

    public SquareEditor(IMapInstance map, Square feature, IEditEventListener oEventListener) throws EMP_Exception {
        super(map, feature, oEventListener, true);
        this.initializeEdit();
    }

    public SquareEditor(IMapInstance map, Square feature, IDrawEventListener oEventListener) throws EMP_Exception {
        super(map, feature, oEventListener, true);
        this.initializeDraw();
    }

    @Override
    protected void prepareForDraw() throws EMP_Exception {
        super.prepareForDraw();

        IGeoPosition centerPos = getCenter();
        IGeoBounds bounds = mapInstance.getMapBounds();
        if (null != bounds) {
            currentLength = GeoLibrary.computeDistanceBetween(new EmpGeoPosition(bounds.getNorth(), bounds.getWest()),
                    new EmpGeoPosition(bounds.getSouth(), bounds.getEast())) * .10 * 1.5;
        } else {
            ICamera camera = oClientMap.getCamera();
            currentLength = 2 * camera.getAltitude() * lengthMultiplier;
        }
        currentBearing = 0.0;

        this.oFeature.setPosition(centerPos);
        this.oFeature.setWidth(currentLength);
        this.oFeature.setAzimuth(currentBearing);
        this.oFeature.setAltitudeMode(IGeoAltitudeMode.AltitudeMode.CLAMP_TO_GROUND);
    }

    @Override
    protected  void prepareForEdit() throws EMP_Exception {
        super.prepareForEdit();

        currentLength = this.oFeature.getWidth();
        currentBearing = (this.oFeature.getAzimuth() + 360) % 360;
    }

    /**
     * This invoked by the base class when initializeDraw or initializeEdit is invoked from the constructor.
     * It will setup the control point for the center of the rectangle
     *
     * We provide following control points:
     *     LENGTH_CP - Used to length (width attribute in the Square class)
     *     AZIMUTH_CP - can be used for bearing adjustment
     */
    @Override
    protected void assembleControlPoints() {
        super.assembleControlPoints();

        IGeoPosition centerPos = new GeoPosition();
        centerPos.setLatitude(this.oFeature.getPosition().getLatitude());
        centerPos.setLongitude(this.oFeature.getPosition().getLongitude());

        this.addControlPoint(getCP(ControlPoint.CPTypeEnum.LENGTH_CP, centerPos, true));
        this.addControlPoint(getCP(ControlPoint.CPTypeEnum.AZIMUTH_CP, centerPos, true));
    }

    /**
     * This is called when EDIT is cancelled
     */

    protected void saveOriginalState() {
        originalLength = this.oFeature.getWidth();
        originalAzimuth = this.oFeature.getAzimuth();
    }

    @Override
    protected void restoreOnCancel() {
        super.restoreOnCancel();
        this.oFeature.setWidth(originalLength);
        this.oFeature.setAzimuth(originalAzimuth);
    }
    /**
     * Creates a new control point if createIt is set to true, otherwise looks for an existing cp.
     * Once CP is created or fund, recalculate it's position using center and length.
     *
     * @param cpType - Type of control point
     * @param center - Center of rectangle
     * @param createIt
     * @return
     */
    private ControlPoint getCP(ControlPoint.CPTypeEnum cpType, IGeoPosition center, boolean createIt) {
        ControlPoint cp;
        if(!createIt) {
            cp = findControlPoint(cpType, 0, -1);
        } else {
            cp = new ControlPoint(cpType, 0, -1, this.oFeature.getAltitudeMode());
        }

        if(null != cp) {
            IGeoPosition pos = null;
            switch(cpType) {
                case LENGTH_CP:
                    pos = GeoLibrary.calculateRhumbPositionAt(currentBearing + 90.0, currentLength/2, center);
                    break;
                case AZIMUTH_CP:
                    pos = GeoLibrary.calculateRhumbPositionAt(currentBearing - 90.0, currentLength/2, center);
                    break;
                default:
                    Log.e(TAG, "getCP illegal CPType " + cpType.toString());
            }
            cp.setPosition(pos);
        } else {
            Log.e(TAG, "getCP returning null CP for " + cpType.toString());
        }
        return cp;
    }

    /**
     * Recomputes positions of all control points given center. Presumes that control points already exist.
     * @param center
     */
    @Override
    protected void recompute(IGeoPosition center ) {
        getCP(ControlPoint.CPTypeEnum.LENGTH_CP, center, false);
        getCP(ControlPoint.CPTypeEnum.AZIMUTH_CP, center, false);
    }

    @Override
    protected double getMinDistance(double multiplier) {
        double minDistance = -1.0;

        IGeoBounds bounds = mapInstance.getMapBounds();
        if(null != bounds) {
            minDistance = GeoLibrary.computeDistanceBetween(new EmpGeoPosition(bounds.getNorth(), bounds.getWest()),
                    new EmpGeoPosition(bounds.getSouth(), bounds.getEast())) * boundsMinLenghtMultiplier;
            if(mapInstance.getCamera().getTilt() > 30) {
                minDistance *= 3;
            }
        }
        return minDistance;
    }

    private void adjustWidth(ControlPoint oCP, IGeoPosition oLatLon) {
        Log.d(TAG, oCP.getCPType().toString() + " B4 calc " + currentLength + " " + currentBearing);
        currentLength = 2 * GeoLibrary.computeDistanceBetween(this.oFeature.getPosition(), oLatLon);

        Log.d(TAG, oCP.getCPType().toString() + currentLength + " " + currentBearing);
        recompute(this.oFeature.getPosition());
        this.oFeature.setWidth(currentLength);
        this.oFeature.apply();

        addUpdateEventData(FeaturePropertyChangedEnum.WIDTH_PROPERTY_CHANGED);
        issueUpdateEvent();

    }

    /**
     * Invoked when user drags any of the control points. Recalculates the control point positions and moves the rectangle
     * as necessary.
     *
     * @param oCP     The control point that was moved.
     * @param oLatLon The new location for the control point.
     * @return
     */

    protected boolean doControlPointMoved(ControlPoint oCP, IGeoPosition oLatLon) {
        Log.d(TAG, "doControlPointMoved");
        boolean moved = false;

        switch (oCP.getCPType()) {
            case LENGTH_CP:
                if ((null == (oLatLon = restrictCPMoveAlongAxis(oLatLon, 180.0, 0.0, 90.0))) ||
                        (!restrictDistanceToCenter(oLatLon, 90.0, minLengthMultiplier, this.oFeature.getWidth()))) {
                    break;
                }
                adjustWidth(oCP, oLatLon);
                moved = true;
                break;

            case AZIMUTH_CP:
                currentBearing = GeoLibrary.computeBearing(this.oFeature.getPosition(), oLatLon) + 90.0;
                currentBearing = (currentBearing + 360) % 360;
                recompute(this.oFeature.getPosition());
                moved = true;
                this.oFeature.setAzimuth(currentBearing);
                this.oFeature.apply();
                addUpdateEventData(FeaturePropertyChangedEnum.AZIMUTH_PROPERTY_CHANGED);
                issueUpdateEvent();
                break;

            default:
                Log.d(TAG, "unsupported control point type " + oCP.getCPType().toString());
        }

        return moved;
    }

    @Override
    protected void setFeaturePosition(IGeoPosition position) {
        this.oFeature.setPosition(position);
    }

    @Override
    protected IGeoPosition getFeaturePosition() {
        return this.oFeature.getPosition();
    }

}
