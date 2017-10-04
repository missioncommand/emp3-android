package mil.emp3.core.editors;

import android.util.Log;

import org.cmapi.primitives.GeoPosition;
import org.cmapi.primitives.IGeoAltitudeMode;
import org.cmapi.primitives.IGeoPosition;

import mil.emp3.api.Rectangle;

import mil.emp3.api.enums.FeaturePropertyChangedEnum;
import mil.emp3.api.exceptions.EMP_Exception;
import mil.emp3.api.interfaces.ICamera;
import mil.emp3.api.listeners.IDrawEventListener;
import mil.emp3.api.listeners.IEditEventListener;
import mil.emp3.api.utils.GeographicLib;
import mil.emp3.api.utils.GeoLibrary;
import mil.emp3.mapengine.interfaces.IMapInstance;

/**
 * Implements editor for Rectangle with six three control points that allow the user to position and size of the rectangle on
 * the map.
 *
 * * NOTES
 * Altitude mode is clamp to ground.
 * Zooming in/out cause the graphic to get re-rendered.
 *
 * Requirements
 * Each CP should have one function. Add the azimuth to be consistent with all other editors.
 * The Height should only move the CP along the Y axis of the rectangle and the width along its X axis.
 * The Height and width CP should not be allowed to reach the center.
 *
 * http://www.movable-type.co.uk/scripts/latlong.html
 *
 * Azimuth and bearing are used interchangeably in the code/comment below.
 */
public class RectangleEditor extends AbstractBasicShapesDrawEditEditor<Rectangle> {
    private static String TAG = RectangleEditor.class.getSimpleName();

    private final double widthMultiplier = .20;    // When drawing first time used to multiply camera altitude to get width
    private final double heightMultiplier = .10;   // When drawing first time used to multiply camera altitude to get height
    private final double minWidthMultiplier = (widthMultiplier/5);
    private final double minHeightMultiplier = (heightMultiplier/5);

    private final double minDistanceTiltThreshold = 30.0; // Change minimum distance when camera tilt is above this value
    private final double minDistanceTiltThresholdMultiplier = 3.0; // multiply minimum distance when tilt threshold is crossed.

    private double currentWidth = 0;
    private double currentHeight = 0;

    // Following are used to restore the feature state. Note that base class will restore the position.
    private double originalHeight;
    private double originalWidth;

    public RectangleEditor(IMapInstance map, Rectangle feature, IEditEventListener oEventListener) throws EMP_Exception {
        super(map, feature, oEventListener, true);
        this.initializeEdit();
    }

    public RectangleEditor(IMapInstance map, Rectangle feature, IDrawEventListener oEventListener, boolean newFeature) throws EMP_Exception {
        super(map, feature, oEventListener, true, newFeature);
        this.initializeDraw();
    }

    @Override
    protected void prepareForDraw() throws EMP_Exception {
        super.prepareForDraw();

        if (!this.isNewFeature()) {
            // A feature that already exists should have all of its properties set already.
            return;
        }

        double refDistance = getReferenceDistance();
        if(refDistance > 0) {
            currentHeight = refDistance * heightMultiplier;
            currentWidth = (widthMultiplier/heightMultiplier) * currentHeight;
        } else {
            ICamera camera = oClientMap.getCamera();
            currentWidth = 2 * camera.getAltitude() * widthMultiplier;
            currentHeight = 2 * camera.getAltitude() * heightMultiplier;
        }
        currentBearing = 0.0;
        if(currentHeight < Rectangle.MINIMUM_HEIGHT) {
            currentHeight = Rectangle.MINIMUM_HEIGHT;
        }
        if(currentWidth < Rectangle.MINIMUM_WIDTH) {
            currentWidth = Rectangle.MINIMUM_WIDTH;
        }
        Log.d(TAG, "currentHeight " + currentHeight + " currentWidth " + currentWidth);
        this.oFeature.setWidth(currentWidth);
        this.oFeature.setHeight(currentHeight);
        this.oFeature.setAzimuth(currentBearing);
        this.oFeature.setAltitudeMode(IGeoAltitudeMode.AltitudeMode.CLAMP_TO_GROUND);
    }

    @Override
    protected  void prepareForEdit() throws EMP_Exception {
        super.prepareForEdit();

        currentWidth = this.oFeature.getWidth();
        currentHeight = this.oFeature.getHeight();
        currentBearing = (this.oFeature.getAzimuth() + 360) % 360;
    }
    /**
     * This invoked by the base class when initializeDraw or initializeEdit is invoked from the constructor.
     * It will setup the control point for the center of the rectangle
     *
     * We provide following control points:
     *     WIDTH_CP - Used to adjust width
     *     HEIGHT_CP - Used to adjust height
     *     AZIMUTH_CP - can be used for bearing adjustment
     */
    @Override
    protected void assembleControlPoints() {
        super.assembleControlPoints();

        IGeoPosition centerPos = new GeoPosition();
        centerPos.setLatitude(this.oFeature.getPosition().getLatitude());
        centerPos.setLongitude(this.oFeature.getPosition().getLongitude());

        this.addControlPoint(getCP(ControlPoint.CPTypeEnum.WIDTH_CP, centerPos, true));
        this.addControlPoint(getCP(ControlPoint.CPTypeEnum.HEIGHT_CP, centerPos, true));
        this.addControlPoint(getCP(ControlPoint.CPTypeEnum.AZIMUTH_CP, centerPos, true));
    }

    /**
     * This is called when EDIT is cancelled
     */

    protected void saveOriginalState() {
        originalHeight = this.oFeature.getHeight();
        originalWidth = this.oFeature.getWidth();
        originalAzimuth = this.oFeature.getAzimuth();
    }

    @Override
    protected void restoreOnCancel() {
        super.restoreOnCancel();
        this.oFeature.setHeight(originalHeight);
        this.oFeature.setWidth(originalWidth);
        this.oFeature.setAzimuth(originalAzimuth);
    }
    /**
     * Creates a new control point if createIt is set to true, otherwise looks for an existing cp.
     * Once CP is created or fund, recalculate it's position using center and width/height.
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
                case WIDTH_CP:
                    pos = GeoLibrary.calculateRhumbPositionAt(currentBearing + 90.0, currentWidth/2, center);
                    break;
                case HEIGHT_CP:
                    pos = GeoLibrary.calculateRhumbPositionAt(currentBearing, currentHeight/2, center);
                    break;
                case AZIMUTH_CP:
                    pos = GeoLibrary.calculateRhumbPositionAt(currentBearing - 90.0, currentWidth/2, center);
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
        getCP(ControlPoint.CPTypeEnum.WIDTH_CP, center, false);
        getCP(ControlPoint.CPTypeEnum.HEIGHT_CP, center, false);
        getCP(ControlPoint.CPTypeEnum.AZIMUTH_CP, center, false);
    }

    @Override
    protected double getMinDistance(double multiplier) {
        double minDistance = -1.0;

        double refDistance = getReferenceDistance();
        if(refDistance > 0) {
            minDistance = refDistance * multiplier;
            if(mapInstance.getCamera().getTilt() > minDistanceTiltThreshold) {
                minDistance *= minDistanceTiltThresholdMultiplier;
            }
        }
        return minDistance;
    }

    private void adjustWidth(ControlPoint oCP, IGeoPosition oLatLon) {
        Log.d(TAG, oCP.getCPType().toString() + " B4 calc " + currentWidth + " " + currentBearing);
        currentWidth = 2 * GeographicLib.computeDistanceBetween(this.oFeature.getPosition(), oLatLon);
        if(currentWidth < Rectangle.MINIMUM_WIDTH) {
            currentWidth = Rectangle.MINIMUM_WIDTH;
        }
        Log.d(TAG, oCP.getCPType().toString() + currentWidth + " " + currentBearing);
        recompute(this.oFeature.getPosition());
        this.oFeature.setWidth(currentWidth);
        this.oFeature.apply();

        addUpdateEventData(FeaturePropertyChangedEnum.WIDTH_PROPERTY_CHANGED);
        issueUpdateEvent();

    }

    private void adjustHeight(ControlPoint oCP, IGeoPosition oLatLon) {
        Log.d(TAG, oCP.getCPType().toString() + " B4 calc " + currentWidth + " " + currentBearing);
        currentHeight = 2 * GeographicLib.computeDistanceBetween(this.oFeature.getPosition(), oLatLon);
        if(currentHeight < Rectangle.MINIMUM_HEIGHT) {
            currentHeight = Rectangle.MINIMUM_HEIGHT;
        }
        Log.d(TAG, oCP.getCPType().toString() + currentHeight + " " + currentBearing);
        recompute(this.oFeature.getPosition());
        this.oFeature.setHeight(currentHeight);
        this.oFeature.apply();

        addUpdateEventData(FeaturePropertyChangedEnum.HEIGHT_PROPERTY_CHANGED);
        issueUpdateEvent();
    }

    /**
     * Invoked when user drags any of the control points. Recalculates the control point positions and moves the rectangle
     * as necessary.
     *
     * @param oCP     The control point that was moved.
     * @param oLatLon The new location for the control point.
     * @return Always return true, even if height/width wasn't adjusted based on minimum height/width restriction. Returning true indicates
     * that event was consumed, otherwise Map would move, which is not desirable. (Issue #149)
     * Side effect of this decision is client application will get redundant events.
     */

    protected boolean doControlPointMoved(ControlPoint oCP, IGeoPosition oLatLon) {
        Log.d(TAG, "doControlPointMoved");

        switch (oCP.getCPType()) {
            case WIDTH_CP:
                if ((null == (oLatLon = restrictCPMoveAlongAxis(oLatLon, 180.0, 0.0, 90.0))) ||
                        (!restrictDistanceToCenter(oLatLon, 90.0, minWidthMultiplier,this.oFeature.getWidth()))) {
                    break;
                }
                adjustWidth(oCP, oLatLon);
                break;

            case HEIGHT_CP:
                if ((null == (oLatLon = restrictCPMoveAlongAxis(oLatLon, 270.0, 90.0, 0.0))) ||
                        (!restrictDistanceToCenter(oLatLon, 0.0, minHeightMultiplier, this.oFeature.getHeight()))) {
                    break;
                }
                adjustHeight(oCP, oLatLon);
                break;

            case AZIMUTH_CP:
                currentBearing = GeographicLib.computeBearing(this.oFeature.getPosition(), oLatLon) + 90.0;
                currentBearing = (currentBearing + 360) % 360;
                recompute(this.oFeature.getPosition());
                this.oFeature.setAzimuth(currentBearing);
                this.oFeature.apply();
                addUpdateEventData(FeaturePropertyChangedEnum.AZIMUTH_PROPERTY_CHANGED);
                issueUpdateEvent();
                break;

            default:
                Log.e(TAG, "unsupported control point type " + oCP.getCPType().toString());
        }

        return true;
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
