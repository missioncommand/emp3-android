package mil.emp3.core.editors;

import org.cmapi.primitives.GeoPosition;
import org.cmapi.primitives.IGeoAltitudeMode;
import org.cmapi.primitives.IGeoPosition;

import java.util.ArrayList;
import java.util.List;

import mil.emp3.api.Ellipse;
import mil.emp3.api.enums.FeatureEditUpdateTypeEnum;
import mil.emp3.api.enums.FeaturePropertyChangedEnum;
import mil.emp3.api.exceptions.EMP_Exception;
import mil.emp3.api.listeners.IDrawEventListener;
import mil.emp3.api.listeners.IEditEventListener;
import mil.emp3.api.utils.GeoLibrary;
import mil.emp3.mapengine.interfaces.IMapInstance;

/**
 * This class implements the Ellipse feature editor.
 */

public class EllipseEditor extends AbstractSinglePointEditor<Ellipse> {
    private static final int SEMI_MAJOR_CP_INDEX = 0;
    private static final int SEMI_MINOR_CP_INDEX = 1;

    private double semiMajor;
    private double semiMinor;
    private double originalAzimuth;

    public EllipseEditor(IMapInstance map, Ellipse feature, IEditEventListener oEventListener)
            throws EMP_Exception {
        super(map, feature, oEventListener, true);
        semiMajor = feature.getSemiMajor();
        semiMinor = feature.getSemiMinor();
        originalAzimuth = feature.getAzimuth();
        this.initializeEdit();
    }

    public EllipseEditor(IMapInstance map, Ellipse feature, IDrawEventListener oEventListener)
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

        this.oFeature.setSemiMajor(tempRadius);
        this.oFeature.setSemiMinor(tempRadius / 2.0);
        this.oFeature.setAzimuth(0);
    }

    @Override
    protected void restoreOnCancel() {
        super.restoreOnCancel();
        this.oFeature.setSemiMajor(this.semiMajor);
        this.oFeature.setSemiMinor(this.semiMinor);
        this.oFeature.setAzimuth(this.originalAzimuth);
    }

    @Override
    protected void assembleControlPoints() {
        List<IGeoPosition> posList = this.getPositions();
        IGeoPosition pos;
        ControlPoint controlPoint;
        double azimuth = this.oFeature.getAzimuth();

        // Add the center control point.
        controlPoint = new ControlPoint(ControlPoint.CPTypeEnum.POSITION_CP, 0, -1);
        controlPoint.setPosition(posList.get(0));
        this.addControlPoint(controlPoint);

        // Add semi Major control point.
        pos = new GeoPosition();
        // Place it to the right of the center at the semi major distance along the semi major axis.
        GeoLibrary.computePositionAt(90.0 + azimuth, this.oFeature.getSemiMajor(), posList.get(0), pos);
        controlPoint = new ControlPoint(ControlPoint.CPTypeEnum.RADIUS_CP, EllipseEditor.SEMI_MAJOR_CP_INDEX, -1);
        controlPoint.setPosition(pos);
        this.addControlPoint(controlPoint);

        // Add semi Minor control point.
        pos = new GeoPosition();
        // Place it above of the center at the semi minor distance along the semi minor axis.
        GeoLibrary.computePositionAt(azimuth, this.oFeature.getSemiMinor(), posList.get(0), pos);
        controlPoint = new ControlPoint(ControlPoint.CPTypeEnum.RADIUS_CP, EllipseEditor.SEMI_MINOR_CP_INDEX, -1);
        controlPoint.setPosition(pos);
        this.addControlPoint(controlPoint);

        // Add azimuth control point.
        pos = new GeoPosition();
        // Place it to the left of the center at the semi major distance along the semi major axis.
        GeoLibrary.computePositionAt(azimuth - 90.0, this.oFeature.getSemiMajor(), posList.get(0), pos);
        controlPoint = new ControlPoint(ControlPoint.CPTypeEnum.AZIMUTH_CP, 0, -1);
        controlPoint.setPosition(pos);
        this.addControlPoint(controlPoint);
    }

    private void moveAllCP(IGeoPosition centerPos) {
        double azimuth = this.oFeature.getAzimuth();
        ControlPoint semiMajorCP = this.findControlPoint(ControlPoint.CPTypeEnum.RADIUS_CP, EllipseEditor.SEMI_MAJOR_CP_INDEX, -1);
        ControlPoint semiMinorCP = this.findControlPoint(ControlPoint.CPTypeEnum.RADIUS_CP, EllipseEditor.SEMI_MINOR_CP_INDEX, -1);
        ControlPoint azimuthCP = this.findControlPoint(ControlPoint.CPTypeEnum.AZIMUTH_CP, 0, -1);
        ControlPoint centerCP = this.findControlPoint(ControlPoint.CPTypeEnum.POSITION_CP, 0, -1);

        centerCP.getPosition().setLatitude(centerPos.getLatitude());
        centerCP.getPosition().setLongitude(centerPos.getLongitude());

        GeoLibrary.computePositionAt(azimuth + 90.0, this.oFeature.getSemiMajor(), centerPos, semiMajorCP.getPosition());
        GeoLibrary.computePositionAt(azimuth, this.oFeature.getSemiMinor(), centerPos, semiMinorCP.getPosition());
        GeoLibrary.computePositionAt(azimuth - 90.0, this.oFeature.getSemiMajor(), centerPos, azimuthCP.getPosition());
    }

    @Override
    protected boolean doFeatureMove(double dBearing, double dDistance) {
        super.doFeatureMove(dBearing, dDistance);
        this.moveAllCP(this.oFeature.getPosition());
        return true;
    }

    @Override
    protected boolean doControlPointMoved(ControlPoint oCP, IGeoPosition newPosition) {
        double azimuth = this.oFeature.getAzimuth();
        ControlPoint semiMajorCP;
        ControlPoint semiMinorCP;
        ControlPoint azimuthCP;
        ControlPoint centerCP;
        boolean moved = false;

        switch (oCP.getCPType()) {
            case RADIUS_CP:
                // The radius was moved.
                centerCP = this.findControlPoint(ControlPoint.CPTypeEnum.POSITION_CP, 0, -1);
                double newRadius = Math.rint(GeoLibrary.computeDistanceBetween(centerCP.getPosition(), newPosition));

                switch (oCP.getCPIndex()) {
                    case EllipseEditor.SEMI_MAJOR_CP_INDEX:
                        // set the semi major CP new position.
                        GeoLibrary.computePositionAt(azimuth + 90.0, newRadius, centerCP.getPosition(), oCP.getPosition());
                        // Store the new value.
                        this.oFeature.setSemiMajor(newRadius);

                        azimuthCP = this.findControlPoint(ControlPoint.CPTypeEnum.AZIMUTH_CP, 0, -1);
                        if (azimuthCP != null) {
                            // Move the azimuth CP.
                            GeoLibrary.computePositionAt(azimuth - 90.0, this.oFeature.getSemiMajor(), centerCP.getPosition(), azimuthCP.getPosition());
                            moved = true;
                        }
                        this.addUpdateEventData(FeaturePropertyChangedEnum.SEMI_MAJOR_PROPERTY_CHANGED);
                        break;
                    case EllipseEditor.SEMI_MINOR_CP_INDEX:
                        // set the semi major CP new position.
                        GeoLibrary.computePositionAt(azimuth, newRadius, centerCP.getPosition(), oCP.getPosition());
                        // Store the new value.
                        this.oFeature.setSemiMinor(newRadius);

                        azimuthCP = this.findControlPoint(ControlPoint.CPTypeEnum.AZIMUTH_CP, 0, -1);
                        if (azimuthCP != null) {
                            // Move the azimuth CP.
                            GeoLibrary.computePositionAt(azimuth - 90.0, this.oFeature.getSemiMajor(), centerCP.getPosition(), azimuthCP.getPosition());
                            moved = true;
                        }
                        this.addUpdateEventData(FeaturePropertyChangedEnum.SEMI_MINOR_PROPERTY_CHANGED);
                        break;
                }

                moved = true;
                break;
            case POSITION_CP:
                // The center was moved.
                IGeoPosition centerPos = oCP.getPosition();

                // Set the center new position.
                centerPos.setLatitude(newPosition.getLatitude());
                centerPos.setLongitude(newPosition.getLongitude());
                this.addUpdateEventData(FeatureEditUpdateTypeEnum.COORDINATE_MOVED, new int[]{0});

                // Move the semi major CP.
                semiMajorCP = this.findControlPoint(ControlPoint.CPTypeEnum.RADIUS_CP, EllipseEditor.SEMI_MAJOR_CP_INDEX, -1);
                GeoLibrary.computePositionAt(azimuth + 90.0, this.oFeature.getSemiMajor(), centerPos, semiMajorCP.getPosition());

                // Move the semi minor CP.
                semiMinorCP = this.findControlPoint(ControlPoint.CPTypeEnum.RADIUS_CP, EllipseEditor.SEMI_MINOR_CP_INDEX, -1);
                GeoLibrary.computePositionAt(azimuth, this.oFeature.getSemiMinor(), centerPos, semiMinorCP.getPosition());

                // Move the azimuth CP.
                azimuthCP = this.findControlPoint(ControlPoint.CPTypeEnum.AZIMUTH_CP, 0, -1);
                GeoLibrary.computePositionAt(azimuth - 90.0, this.oFeature.getSemiMajor(), centerPos, azimuthCP.getPosition());
                moved = true;

                break;
            case AZIMUTH_CP:
                // The azimuth CP was moved.
                centerCP = this.findControlPoint(ControlPoint.CPTypeEnum.POSITION_CP, 0, -1);
                double newAzimuth = GeoLibrary.computeBearing(centerCP.getPosition(), newPosition) + 90.0;
                if (newAzimuth > 360.0) {
                    newAzimuth -= 360.0;
                }

                this.oFeature.setAzimuth(newAzimuth);
                GeoLibrary.computePositionAt(newAzimuth - 90.0, this.oFeature.getSemiMajor(), centerCP.getPosition(), oCP.getPosition());
                this.addUpdateEventData(FeaturePropertyChangedEnum.AZIMUTH_PROPERTY_CHANGED);

                // Move the semi major CP.
                semiMajorCP = this.findControlPoint(ControlPoint.CPTypeEnum.RADIUS_CP, EllipseEditor.SEMI_MAJOR_CP_INDEX, -1);
                GeoLibrary.computePositionAt(newAzimuth + 90.0, this.oFeature.getSemiMajor(), centerCP.getPosition(), semiMajorCP.getPosition());

                // Move the semi minor CP.
                semiMinorCP = this.findControlPoint(ControlPoint.CPTypeEnum.RADIUS_CP, EllipseEditor.SEMI_MINOR_CP_INDEX, -1);
                GeoLibrary.computePositionAt(newAzimuth, this.oFeature.getSemiMinor(), centerCP.getPosition(), semiMinorCP.getPosition());
                moved = true;
                break;
        }
        return moved;
    }
}
