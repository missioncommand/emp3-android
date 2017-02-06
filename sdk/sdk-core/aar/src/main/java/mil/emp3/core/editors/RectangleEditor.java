package mil.emp3.core.editors;

import android.util.Log;

import org.cmapi.primitives.GeoPosition;
import org.cmapi.primitives.IGeoAltitudeMode;
import org.cmapi.primitives.IGeoPosition;

import java.util.ArrayList;
import java.util.List;

import mil.emp3.api.Rectangle;
import mil.emp3.api.abstracts.Feature;
import mil.emp3.api.enums.EditorMode;
import mil.emp3.api.enums.FeatureEditUpdateTypeEnum;
import mil.emp3.api.enums.FeaturePropertyChangedEnum;
import mil.emp3.api.exceptions.EMP_Exception;
import mil.emp3.api.interfaces.ICamera;
import mil.emp3.api.interfaces.IEditUpdateData;
import mil.emp3.api.listeners.IDrawEventListener;
import mil.emp3.api.listeners.IEditEventListener;
import mil.emp3.api.utils.GeoLibrary;
import mil.emp3.core.events.EditUpdateData;
import mil.emp3.mapengine.interfaces.IMapInstance;

/**
 * Implements editor for Rectangle with six posible control points that allow the user to position and size the rectangle on
 * the map.
 *
 * * NOTES
 * Altitude mode is clamp to ground.
 * Zooming in/out cause the graphic to get re-rendererd.
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


    private double currentWidth = 0;
    private double currentHeight = 0;

    // Following are used to restore the feature state. Note that base class will restore the position.
    private double originalHeight;
    private double originalWidth;

    public RectangleEditor(IMapInstance map, Rectangle feature, IEditEventListener oEventListener) throws EMP_Exception {
        super(map, feature, oEventListener, true);
        this.initializeEdit();
    }

    public RectangleEditor(IMapInstance map, Rectangle feature, IDrawEventListener oEventListener) throws EMP_Exception {
        super(map, feature, oEventListener, true);
        this.initializeDraw();
    }

    @Override
    protected void prepareForDraw() throws EMP_Exception {
        super.prepareForDraw();

        IGeoPosition centerPos = new GeoPosition();
        ICamera camera = oClientMap.getCamera();

        currentWidth = 2 * camera.getAltitude() * widthMultiplier;
        currentHeight = 2 * camera.getAltitude() * heightMultiplier;
        currentBearing = 0.0;

        centerPos.setLatitude(camera.getLatitude());
        centerPos.setLongitude(camera.getLongitude());

        this.oFeature.setPosition(centerPos);
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

    private void adjustWidth(ControlPoint oCP, IGeoPosition oLatLon) {
        Log.d(TAG, oCP.getCPType().toString() + " B4 calc " + currentWidth + " " + currentBearing);
        currentWidth = 2 * GeoLibrary.computeDistanceBetween(this.oFeature.getPosition(), oLatLon);

        Log.d(TAG, oCP.getCPType().toString() + currentWidth + " " + currentBearing);
        recompute(this.oFeature.getPosition());
        this.oFeature.setWidth(currentWidth);
        this.oFeature.apply();

        addUpdateEventData(FeaturePropertyChangedEnum.WIDTH_PROPERTY_CHANGED);
        issueUpdateEvent();

    }

    private void adjustHeight(ControlPoint oCP, IGeoPosition oLatLon) {
        Log.d(TAG, oCP.getCPType().toString() + " B4 calc " + currentWidth + " " + currentBearing);
        currentHeight = 2 * GeoLibrary.computeDistanceBetween(this.oFeature.getPosition(), oLatLon);

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
     * @return
     */

    protected boolean doControlPointMoved(ControlPoint oCP, IGeoPosition oLatLon) {
        Log.d(TAG, "doControlPointMoved");
        boolean moved = false;

        switch (oCP.getCPType()) {
            case WIDTH_CP:
                if ((null == (oLatLon = restrictCPMoveAlongAxis(oLatLon, 180.0, 0.0, 90.0))) ||
                        (!restrictDistanceToCenter(oLatLon, 90.0, minWidthMultiplier,this.oFeature.getWidth()))) {
                    break;
                }
                adjustWidth(oCP, oLatLon);
                moved = true;
                break;

            case HEIGHT_CP:
                if ((null == (oLatLon = restrictCPMoveAlongAxis(oLatLon, 270.0, 90.0, 0.0))) ||
                        (!restrictDistanceToCenter(oLatLon, 0.0, minHeightMultiplier, this.oFeature.getHeight()))) {
                    break;
                }
                adjustHeight(oCP, oLatLon);
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
