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
import mil.emp3.api.utils.GeoLibrary;
import mil.emp3.core.editors.ControlPoint;
import mil.emp3.mapengine.interfaces.IMapInstance;

/**
 * This class implemets the editor for the following graphics.
 *
 * Tactical Graphics
 *      Fire Support
 *          Areas
 *              Command and Control Areas
 *                  Fire Support Area (FSA)
 *                      Fire Support Area (FSA) Rectangular
 *                  Airspace Coordination Area (ACA)
 *                      Airspace Coordination Area (ACA) Rectangular
 *                  Free Fire Area (FFA)
 *                      Free Fire Area (FFA) Rectangular
 *                  No-Fire Area (NFA) Shapes
 *                      No-Fire Area (NFA) Rectangular
 *                  Restrictive Fire Area (RFA) Shapes
 *                      Restrictive Fire Area (RFA) Rectangular
 *                  Position Area for Artillery (PAA)
 *                      Position Area for Artillery (PAA) Rectangular
 *              Target Acquisition Zones
 *                  Artillery Target Intelligence (ATI) Shapes
 *                      Artillery Target Intelligence (ATI) Zone Rectangular
 *                  Call for Fire Zone
 *                      Call For Fire Zone (CFFZ) Rectangular
 *                  Censor Shapes
 *                      Censor Zone Rectangular
 *                  Critical Friendly Zone Shapes
 *                      Critical Friendly Zone (CFZ) Rectangular
 *
 * 2525C Only
 * Tactical Graphics
 *      Fire Support
 *          Areas
 *              Command and Control Areas
 *                  Sensor Zone Shapes
 *                      Sensor Zone Rectangular
 *                  Dead Space Area
 *                      Dead Space Area (DA) Rectangular
 *                  Zone Of Responsibility (ZOR)
 *                      Zone Of Responsibility (ZOR) Rectangular
 *                  Target Build-Up Area (TBA)
 *                      Target Build-Up Area (TBA) Rectangular
 *                  Target Value Area (TVAR)
 *                      Target Value Area (TVAR) Rectangular
 *              Kill Box
 *                  Blue
 *                      Kill Box Blue Rectangular
 *                  Purple
 *                      Kill Box Purple Rectangular
 *
 *
 *
 * 2525B Only
 * Tactical Graphics
 *      Fire Support
 *          Areas
 *              Target Acquisition Zones
 *                  Sensor Zone Shapes
 *                      Sensor Zone Rectangular
 *                  Dead Space Area
 *                      Dead Space Area (DA) Rectangular
 *                  Zone Of Responsibility (ZOR)
 *                      Zone Of Responsibility (ZOR) Rectangular
 *                  Target Build-Up Area (TBA)
 *                      Target Build-Up Area (TBA) Rectangular
 *                  Target Value Area (TVAR)
 *                      Target Value Area (TVAR) Rectangular
 */
public class MilStdDCTwoPointRectangularParamAutoShape extends AbstractMilStdMultiPointEditor {

    private float minWidth;
    private float width;

    public MilStdDCTwoPointRectangularParamAutoShape(IMapInstance map, MilStdSymbol feature, IEditEventListener oEventListener, armyc2.c2sd.renderer.utilities.SymbolDef symDef) throws EMP_Exception {
        super(map, feature, oEventListener, symDef);
        this.checkAMModifier();
        this.initializeEdit();
    }

    public MilStdDCTwoPointRectangularParamAutoShape(IMapInstance map, MilStdSymbol feature, IDrawEventListener oEventListener, armyc2.c2sd.renderer.utilities.SymbolDef symDef, boolean newFeature) throws EMP_Exception {
        super(map, feature, oEventListener, symDef, newFeature);
        this.checkAMModifier();
        this.initializeDraw();
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

        if (posList.size() == this.getMinPoints()) {
            // The feature has enough points.
            return;
        } else if (posList.size() > this.getMaxPoints()) {
            // There are to many positions. Removed them all.
            int[] intArray = new int[posList.size()];
            for (int index = posList.size() - 1; posList.size() > this.getMaxPoints(); index--) {
                intArray[index] = index;
                posList.remove(index);
            }
            this.addUpdateEventData(FeatureEditUpdateTypeEnum.COORDINATE_DELETED, intArray);
        }
        // 0 Positions. Create all.

        // Add P1 for side1
        pos = new GeoPosition();
        pos.setAltitude(0);
        pos.setLatitude(cameraPos.getLatitude());
        pos.setLongitude(cameraPos.getLongitude());
        posList.add(pos);

        // Add P2 for side2
        pos = new GeoPosition();
        GeoLibrary.computePositionAt(90, (cameraPos.getAltitude() / 6.0) / 2.0 , posList.get(0), pos);
        pos.setAltitude(0);
        posList.add(pos);

        this.addUpdateEventData(FeatureEditUpdateTypeEnum.COORDINATE_ADDED, new int[]{0,1});
    }

    @Override
    protected void assembleControlPoints() {
        final List<IGeoPosition> posList = this.getPositions();

        // Add the left side control point.
        final ControlPoint controlPoint1 = new ControlPoint(ControlPoint.CPTypeEnum.POSITION_CP, 0, -1);
        controlPoint1.setPosition(posList.get(0));
        this.addControlPoint(controlPoint1);

        // Add the right side control point.
        final ControlPoint controlPoint2 = new ControlPoint(ControlPoint.CPTypeEnum.POSITION_CP, 1, -1);
        controlPoint2.setPosition(posList.get(1));
        this.addControlPoint(controlPoint2);

        // Add width control point.
        final double bearing = GeoLibrary.computeBearing(controlPoint1.getPosition(), controlPoint2.getPosition());
        final double distance = GeoLibrary.computeDistanceBetween(controlPoint1.getPosition(), controlPoint2.getPosition());

        final IGeoPosition midPoint = GeoLibrary.midPointBetween(controlPoint1.getPosition(), controlPoint2.getPosition());

        final GeoPosition pos = new GeoPosition();
        GeoLibrary.computePositionAt(bearing - 90, distance / 2, midPoint, pos);

        final ControlPoint controlPointWidth = new ControlPoint(ControlPoint.CPTypeEnum.WIDTH_CP, 1, -1);
        controlPointWidth.setPosition(pos);

        this.addControlPoint(controlPointWidth);
    }

    @Override
    protected boolean doControlPointMoved(ControlPoint oCP, IGeoPosition dragPosition) {
        switch (oCP.getCPType()) {
            case POSITION_CP: { // A side was moved.
                final ControlPoint side1CP = this.findControlPoint(ControlPoint.CPTypeEnum.POSITION_CP, 0, -1);
                final ControlPoint side2CP = this.findControlPoint(ControlPoint.CPTypeEnum.POSITION_CP, 1, -1);
                final ControlPoint widthCP = this.findControlPoint(ControlPoint.CPTypeEnum.WIDTH_CP, 1, -1);

                final IGeoPosition sidePos = oCP.getPosition();

                // Set the center new position.
                sidePos.setLatitude(dragPosition.getLatitude());
                sidePos.setLongitude(dragPosition.getLongitude());

                // Move the width CP.
                final double bearing = GeoLibrary.computeBearing(side1CP.getPosition(), side2CP.getPosition());

                final IGeoPosition midPoint = GeoLibrary.midPointBetween(side1CP.getPosition(), side2CP.getPosition());
                GeoLibrary.computePositionAt(bearing - 90, this.width / 2, midPoint, widthCP.getPosition());

                this.addUpdateEventData(FeatureEditUpdateTypeEnum.COORDINATE_MOVED, new int[]{oCP.getCPIndex()});
                break;
            }
            case WIDTH_CP: { // The width CP was moved.
                final ControlPoint side1CP = this.findControlPoint(ControlPoint.CPTypeEnum.POSITION_CP, 0, -1);
                final ControlPoint side2CP = this.findControlPoint(ControlPoint.CPTypeEnum.POSITION_CP, 1, -1);

                if (GeoLibrary.isLeft(side1CP.getPosition(), side2CP.getPosition(), dragPosition)) {
                    break; // we wish to keep the CP on one side
                }

                final double bearing = GeoLibrary.computeBearing(side1CP.getPosition(), side2CP.getPosition());
                final IGeoPosition midPoint = GeoLibrary.midPointBetween(side1CP.getPosition(), side2CP.getPosition());

                final float newWidth = Math.round(GeoLibrary.computeDistanceBetween(midPoint, dragPosition));
                if ((newWidth * 2) < minWidth) {
                    break;
                }

                // set the width CP new position.
                GeoLibrary.computePositionAt(bearing - 90, newWidth, midPoint, oCP.getPosition());

                // Save the new value
                this.setWidthModifier(newWidth * 2);

                this.addUpdateEventData(IGeoMilSymbol.Modifier.DISTANCE);
                break;
            }
        }
        return true;
    }

    private void checkAMModifier() {
        IGeoPosition cameraPos = this.getMapCameraPosition();
        float tempDistance = (float) (cameraPos.getAltitude() / 6.0);

        if (tempDistance > 20000) {
            // we can change this later.
            // Make sure its not greater than some threshold.
            tempDistance = 20000;
        }

        this.width = this.oFeature.getNumericModifier(IGeoMilSymbol.Modifier.DISTANCE, 0);
        if (Float.isNaN(this.width)) {
            // No AM0 modifier provided.

            this.width = tempDistance / 2;
            this.oFeature.setModifier(IGeoMilSymbol.Modifier.DISTANCE, 0, this.width);
        }

        // Remove any additional AM values.
        while (!Float.isNaN(this.oFeature.getNumericModifier(IGeoMilSymbol.Modifier.DISTANCE, 1))) {
            this.oFeature.setModifier(IGeoMilSymbol.Modifier.DISTANCE, 1, Float.NaN);
        }

        this.minWidth = 10;
    }

    private void setWidthModifier(float value) {
        this.width = value;
        this.oFeature.setModifier(IGeoMilSymbol.Modifier.DISTANCE, Float.NaN);
        this.oFeature.setModifier(IGeoMilSymbol.Modifier.DISTANCE, value);
    }
}
