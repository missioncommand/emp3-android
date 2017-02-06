package mil.emp3.core.editors;

import org.cmapi.primitives.GeoPosition;
import org.cmapi.primitives.IGeoAltitudeMode;
import org.cmapi.primitives.IGeoPosition;

import java.util.ArrayList;
import java.util.List;

import mil.emp3.api.Circle;
import mil.emp3.api.enums.FeatureEditUpdateTypeEnum;
import mil.emp3.api.enums.FeaturePropertyChangedEnum;
import mil.emp3.api.utils.GeoLibrary;
import mil.emp3.api.exceptions.EMP_Exception;
import mil.emp3.api.listeners.IDrawEventListener;
import mil.emp3.api.listeners.IEditEventListener;
import mil.emp3.mapengine.interfaces.IMapInstance;

/**
 * This class implements the Circle feature editor.
 */

public class CircleEditor extends AbstractSinglePointEditor<Circle> {
    private double originalRadius;
    private double originalAzimuth;

    public CircleEditor(IMapInstance map, Circle feature, IEditEventListener oEventListener)
            throws EMP_Exception {
        super(map, feature, oEventListener, true);
        originalRadius = feature.getRadius();
        originalAzimuth = feature.getAzimuth();
        this.initializeEdit();
    }

    public CircleEditor(IMapInstance map, Circle feature, IDrawEventListener oEventListener)
            throws EMP_Exception {
        super(map, feature, oEventListener, true);
        this.initializeDraw();
    }

    @Override
    protected void prepareForDraw() throws EMP_Exception {
        IGeoPosition cameraPos = this.getMapCameraPosition();
        super.prepareForDraw();

        float tempRadius = (float) Math.rint(cameraPos.getAltitude() / 6.0);

        if (tempRadius > 10000) {
            // we can change this later.
            // Make sure its not greater than some threshold.
            tempRadius = 10000;
        }

        this.oFeature.setRadius(tempRadius);
        this.oFeature.setAltitudeMode(IGeoAltitudeMode.AltitudeMode.CLAMP_TO_GROUND);
    }

    @Override
    protected void restoreOnCancel() {
        super.restoreOnCancel();
        this.oFeature.setRadius(this.originalRadius);
        this.oFeature.setAzimuth(this.originalAzimuth);
    }

    @Override
    protected void assembleControlPoints() {
        List<IGeoPosition> posList = this.getPositions();
        IGeoPosition pos;
        ControlPoint controlPoint;

        // Add the center control point.
        controlPoint = new ControlPoint(ControlPoint.CPTypeEnum.POSITION_CP, 0, -1);
        controlPoint.setPosition(posList.get(0));
        this.addControlPoint(controlPoint);

        // Add radius control point.
        pos = new GeoPosition();
        // Place it to the right of the center at the radius distance.
        GeoLibrary.computePositionAt(90.0, this.oFeature.getRadius(), posList.get(0), pos);
        controlPoint = new ControlPoint(ControlPoint.CPTypeEnum.RADIUS_CP, 0, -1);
        controlPoint.setPosition(pos);
        this.addControlPoint(controlPoint);
    }

    @Override
    protected boolean doFeatureMove(double dBearing, double dDistance) {
        ControlPoint centerCP = this.findControlPoint(ControlPoint.CPTypeEnum.POSITION_CP, 0, -1);
        ControlPoint radiusCP = this.findControlPoint(ControlPoint.CPTypeEnum.RADIUS_CP, 0, -1);

        super.doFeatureMove(dBearing, dDistance);
        // Move centerCP
        centerCP.setPosition(this.oFeature.getPosition());
        // Move the radius CP.
        GeoLibrary.computePositionAt(90.0, this.oFeature.getRadius(), this.oFeature.getPosition(), radiusCP.getPosition());

        return true;
    }

    @Override
    protected boolean doControlPointMoved(ControlPoint oCP, IGeoPosition oLatLon) {
        boolean moved = false;

        switch (oCP.getCPType()) {
            case RADIUS_CP:
                // The radius was moved.
                ControlPoint centerCP = this.findControlPoint(ControlPoint.CPTypeEnum.POSITION_CP, 0, -1);
                double newRadius = Math.rint(GeoLibrary.computeDistanceBetween(centerCP.getPosition(), oLatLon));

                // set the radius CP new position.
                GeoLibrary.computePositionAt(90.0, newRadius, centerCP.getPosition(), oCP.getPosition());

                // Set the new radius.
                this.oFeature.setRadius(newRadius);

                moved = true;
                this.addUpdateEventData(FeaturePropertyChangedEnum.RADIUS_PROPERTY_CHANGED);
                break;
            case POSITION_CP:
                // The center was moved.
                ControlPoint radiusCP = this.findControlPoint(ControlPoint.CPTypeEnum.RADIUS_CP, 0, -1);
                IGeoPosition centerPos = oCP.getPosition();

                // Set the center new position.
                centerPos.setLatitude(oLatLon.getLatitude());
                centerPos.setLongitude(oLatLon.getLongitude());
                // Move the radius CP.
                GeoLibrary.computePositionAt(90.0, this.oFeature.getRadius(), centerPos, radiusCP.getPosition());
                this.addUpdateEventData(FeatureEditUpdateTypeEnum.COORDINATE_MOVED, new int[]{0});
                moved = true;
                break;
        }
        return moved;
    }
}
