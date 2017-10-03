package mil.emp3.core.editors.milstd;

import org.cmapi.primitives.GeoPosition;
import org.cmapi.primitives.IGeoMilSymbol;
import org.cmapi.primitives.IGeoPosition;

import java.util.ArrayList;
import java.util.List;

import mil.emp3.api.MilStdSymbol;
import mil.emp3.api.enums.FeatureEditUpdateTypeEnum;
import mil.emp3.api.exceptions.EMP_Exception;
import mil.emp3.api.listeners.IDrawEventListener;
import mil.emp3.api.listeners.IEditEventListener;
import mil.emp3.api.utils.GeographicLib;
import mil.emp3.core.editors.ControlPoint;
import mil.emp3.mapengine.interfaces.IMapInstance;

/**
 * This class implements the editor for the rectangular parameterized auto shape tactical graphics.
 * It uses only 1 position, 2 AM (Width 0, Length 1) values and 1 AN value for attitude.
 * <b>NOTE: What here is called Width in 2525 is called Length. What is here called Legth in 2525 is called Width.</b>
 * As per the 2525B&C the attitude values for 2525C is in degrees and 2525B is in mils.
 * <b>NOTE: The Attitude is always in degrees.</b>
 *
 * Tactical Graphics
 *      Fire Support
 *          Areas
 *              Area Target
 *                  Rectangular Target
 */
public class MilStdDCRectangularParamAutoShape extends AbstractMilStdMultiPointEditor {

    public MilStdDCRectangularParamAutoShape(IMapInstance map, MilStdSymbol feature, IEditEventListener oEventListener, armyc2.c2sd.renderer.utilities.SymbolDef symDef) throws EMP_Exception {
        super(map, feature, oEventListener, symDef);
        this.initializeEdit();
    }

    public MilStdDCRectangularParamAutoShape(IMapInstance map, MilStdSymbol feature, IDrawEventListener oEventListener, armyc2.c2sd.renderer.utilities.SymbolDef symDef, boolean newFeature) throws EMP_Exception {
        super(map, feature, oEventListener, symDef, newFeature);
        this.initializeDraw();
    }

    private float getAttitudeModifier() {
        float returnValue = this.oFeature.getNumericModifier(IGeoMilSymbol.Modifier.AZIMUTH, 0);
/*
        if (!Float.isNaN(returnValue)) {
            switch (this.symbol.getSymbolStandard()) {
                case MIL_STD_2525B:
                    returnValue = returnValue * 360 / 6400; // Convert from mils to degrees.
                    break;
                case MIL_STD_2525C:
                    break;
            }
        }
*/
        return returnValue;
    }

    private float getWidthModifier() {
        return this.oFeature.getNumericModifier(IGeoMilSymbol.Modifier.DISTANCE, 0);
    }

    private float getLengthModifier() {
        return this.oFeature.getNumericModifier(IGeoMilSymbol.Modifier.DISTANCE, 1);
    }

    private void setAttitudeModifier(float value) {
        float newValue = value;

        if (!Float.isNaN(this.oFeature.getNumericModifier(IGeoMilSymbol.Modifier.AZIMUTH, 0))) {
            // If it exists delete it first.
            this.oFeature.setModifier(IGeoMilSymbol.Modifier.AZIMUTH, 0, Float.NaN);
        }
/*
        if (!Float.isNaN(newValue)) {
            switch (this.symbol.getSymbolStandard()) {
                case MIL_STD_2525B:
                    newValue = newValue * 6400 / 360; // Convert from degrees to mils.
                    break;
                case MIL_STD_2525C:
                    break;
            }
        }
*/
        this.oFeature.setModifier(IGeoMilSymbol.Modifier.AZIMUTH, 0, newValue);
    }

    private void setWidthModifier(float value) {
        if (!Float.isNaN(this.oFeature.getNumericModifier(IGeoMilSymbol.Modifier.DISTANCE, 0))) {
            // If it exists delete it first.
            this.oFeature.setModifier(IGeoMilSymbol.Modifier.DISTANCE, 0, Float.NaN);
        }

        this.oFeature.setModifier(IGeoMilSymbol.Modifier.DISTANCE, 0, value);
    }

    private void setLengthModifier(float value) {
        if (!Float.isNaN(this.oFeature.getNumericModifier(IGeoMilSymbol.Modifier.DISTANCE, 1))) {
            // If it exists delete it first.
            this.oFeature.setModifier(IGeoMilSymbol.Modifier.DISTANCE, 1, Float.NaN);
        }
        this.oFeature.setModifier(IGeoMilSymbol.Modifier.DISTANCE, 1, value);
    }

    private void checkAMModifier() {
        IGeoPosition cameraPos = this.getMapCameraPosition();
        float tempDistance = (float) Math.rint(cameraPos.getAltitude() / 6.0);

        if (tempDistance > 10000) {
            // we can change this later.
            // Make sure its not greater than some threshold.
            tempDistance = 10000;
        }

        if (Float.isNaN(this.getWidthModifier())) {
            // No AM0 modifier provided.
            this.setWidthModifier(2000); // (float) Math.rint(tempDistance / 2));
        }

        if (Float.isNaN(this.getLengthModifier())) {
            // No AM1 modifier provided.
            this.setLengthModifier(9000); //tempDistance);
        }

        // Remove any additional AM values.
        while (!Float.isNaN(this.oFeature.getNumericModifier(IGeoMilSymbol.Modifier.DISTANCE, 2))) {
            this.oFeature.setModifier(IGeoMilSymbol.Modifier.DISTANCE, 2, Float.NaN);
        }
    }

    private void checkANModifier() {
        if (Float.isNaN(this.getAttitudeModifier())) {
            // No AN modifier provided.
            this.setAttitudeModifier(45);
        }

        // Remove all extra AN value.
        while (!Float.isNaN(this.oFeature.getNumericModifier(IGeoMilSymbol.Modifier.AZIMUTH, 1))) {
            this.oFeature.setModifier(IGeoMilSymbol.Modifier.AZIMUTH, 1, Float.NaN);
        }

        // Remove any X values.
        while (!Float.isNaN(this.oFeature.getNumericModifier(IGeoMilSymbol.Modifier.ALTITUDE_DEPTH, 0))) {
            this.oFeature.setModifier(IGeoMilSymbol.Modifier.ALTITUDE_DEPTH, 0, Float.NaN);
        }
    }

    @Override
    protected void prepareForDraw() throws EMP_Exception {

        if (!this.isNewFeature()) {
            // A feature that already exists should have all of its properties set already.
            return;
        }
        IGeoPosition cameraPos = this.getMapCameraPosition();
        List<IGeoPosition> posList = this.getPositions();
        IGeoPosition pos;

        while (!Float.isNaN(this.oFeature.getNumericModifier(IGeoMilSymbol.Modifier.DISTANCE, 0))) {
            this.oFeature.setModifier(IGeoMilSymbol.Modifier.DISTANCE, 0, Float.NaN);
        }
        // Remove all AN value.
        while (!Float.isNaN(this.oFeature.getNumericModifier(IGeoMilSymbol.Modifier.AZIMUTH, 0))) {
            this.oFeature.setModifier(IGeoMilSymbol.Modifier.AZIMUTH, 0, Float.NaN);
        }

        this.checkAMModifier();
        this.checkANModifier();

        // Add P1 is the center.
        pos = new GeoPosition();
        pos.setAltitude(0);
        pos.setLatitude(cameraPos.getLatitude());
        pos.setLongitude(cameraPos.getLongitude());
        posList.add(pos);

        this.addUpdateEventData(FeatureEditUpdateTypeEnum.COORDINATE_ADDED, new int[]{0});
    }

    @Override
    protected void prepareForEdit() throws EMP_Exception {
        this.checkAMModifier();
        this.checkANModifier();
    }

    @Override
    protected void assembleControlPoints() {
        double width = this.getWidthModifier();
        double length = this.getLengthModifier();
        double attitude = this.getAttitudeModifier();
        List<IGeoPosition> posList = this.getPositions();
        IGeoPosition pos;
        ControlPoint controlPoint;

        // Add the center control point.
        controlPoint = new ControlPoint(ControlPoint.CPTypeEnum.POSITION_CP, 0, -1);
        controlPoint.setPosition(posList.get(0));
        this.addControlPoint(controlPoint);

        // Add width control point.
        pos = new GeoPosition();
        // Place it at half the width distance at an azimuth of attitude.
        GeographicLib.computePositionAt(attitude, length / 2, posList.get(0), pos);
        controlPoint = new ControlPoint(ControlPoint.CPTypeEnum.LENGTH_CP, 0, -1);
        controlPoint.setPosition(pos);
        this.addControlPoint(controlPoint);

        // Add length control point.
        pos = new GeoPosition();
        // Place it at the half the length distance at an azimuth of 90 + attitude.
        GeographicLib.computePositionAt((90.0 + attitude) % 360, width / 2, posList.get(0), pos);
        controlPoint = new ControlPoint(ControlPoint.CPTypeEnum.WIDTH_CP, 1, -1);
        controlPoint.setPosition(pos);
        this.addControlPoint(controlPoint);

        // Add attitude control point.
        pos = new GeoPosition();
        // Place it at half the length distance at an azimuth of 270 + attitude.
        GeographicLib.computePositionAt((270.0 + attitude) % 360, width / 2, posList.get(0), pos);
        controlPoint = new ControlPoint(ControlPoint.CPTypeEnum.ATTITUDE_CP, 0, -1);
        controlPoint.setPosition(pos);
        this.addControlPoint(controlPoint);
    }

    private void positionWidthLengthAzimuthCP() {
        double width = this.getWidthModifier();
        double length = this.getLengthModifier();
        double attitude = this.getAttitudeModifier();
        List<IGeoPosition> posList = this.getPositions();
        ControlPoint controlPoint;

        // Get length control point.
        controlPoint = this.findControlPoint(ControlPoint.CPTypeEnum.LENGTH_CP, 0, -1);
        // Place it at half the width distance at an azimuth of attitude.
        GeographicLib.computePositionAt(attitude, length / 2, posList.get(0), controlPoint.getPosition());

        // Get width control point.
        controlPoint = this.findControlPoint(ControlPoint.CPTypeEnum.WIDTH_CP, 1, -1);
        // Place it at the half the width distance at an azimuth of 90 + attitude.
        GeographicLib.computePositionAt((90.0 + attitude) % 360, width / 2, posList.get(0), controlPoint.getPosition());

        // Get attitude control point.
        controlPoint = this.findControlPoint(ControlPoint.CPTypeEnum.ATTITUDE_CP, 0, -1);
        // Place it at half the width distance at an azimuth of 270 + attitude.
        GeographicLib.computePositionAt((270.0 + attitude) % 360, width / 2, posList.get(0), controlPoint.getPosition());
    }

    @Override
    protected boolean doControlPointMoved(ControlPoint oCP, IGeoPosition dragPosition) {
        double attitude = this.getAttitudeModifier();

        switch (oCP.getCPType()) {
            case POSITION_CP: {
                // The center was moved.
                IGeoPosition centerPos = oCP.getPosition();

                // Set the center new position.
                centerPos.setLatitude(dragPosition.getLatitude());
                centerPos.setLongitude(dragPosition.getLongitude());

                this.positionWidthLengthAzimuthCP();

                this.addUpdateEventData(FeatureEditUpdateTypeEnum.COORDINATE_MOVED, new int[]{0});
                break;
            }
            case WIDTH_CP: {
                // The width CP was moved.
                ControlPoint centerCP = this.findControlPoint(ControlPoint.CPTypeEnum.POSITION_CP, 0, -1);
                float newWidth = Math.round(GeographicLib.computeDistanceBetween(centerCP.getPosition(), dragPosition)) * 2;

                // Save the new value
                this.setWidthModifier(newWidth);

                this.positionWidthLengthAzimuthCP();

                this.addUpdateEventData(IGeoMilSymbol.Modifier.DISTANCE);
                break;
            }
            case LENGTH_CP: {
                // The Length CP was moved.
                ControlPoint centerCP = this.findControlPoint(ControlPoint.CPTypeEnum.POSITION_CP, 0, -1);
                float newLength = Math.round(GeographicLib.computeDistanceBetween(centerCP.getPosition(), dragPosition)) * 2;

                // Save the new value
                this.setLengthModifier(newLength);

                this.positionWidthLengthAzimuthCP();

                this.addUpdateEventData(IGeoMilSymbol.Modifier.DISTANCE);
                break;
            }
            case ATTITUDE_CP: {
                // The attitude CP was moved.
                ControlPoint centerCP = this.findControlPoint(ControlPoint.CPTypeEnum.POSITION_CP, 0, -1);
                double bearingNewPosition = GeographicLib.computeBearing(centerCP.getPosition(), dragPosition);

                // The attitude is displayed at a 270 deg of its value.
                // So we must account for it when we set the new value.
                float newAttitude = (float) Math.round(bearingNewPosition + 90.0) % 360;
                this.setAttitudeModifier(newAttitude);

                this.positionWidthLengthAzimuthCP();

                this.addUpdateEventData(IGeoMilSymbol.Modifier.AZIMUTH);
            }
        }
        return true;
    }
}
