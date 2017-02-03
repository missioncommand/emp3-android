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
 * This class implements the editor for the circular parameterized auto shape.
 * The graphics have 1 position and 1 AM value used as the radius of the circle.
 *
 * Tactical Graphics
 *      Fire Support
 *          Areas
 *              Area Target
 *                  Circular Target
 *              Command and Control Areas
 *                  Fire Support Area (FSA)
 *                      Fire Support Area (FSA) Circular
 *                  Airspace Coordination Area (ACA)
 *                      Airspace Coordination Area (ACA) Circular
 *                  Free Fire Area (FFA)
 *                      Free Fire Area (FFA) Circular
 *                  No-Fire Area (NFA)
 *                      No-Fire Area (NFA) Circular
 *                  Restrictive Fire Area (RFA)
 *                      Restrictive Fire Area (RFA) Circular
 *                  Position Area for Artillery (PAA)
 *                      Position Area For Artillery (PAA) Circular
 *                  Sensor Zone
 *                      Sensor Zone Circular
 *                  Dead Space Area (DA)
 *                      Dead Space Area (DA) Circular
 *                  Zone of Responsibility (ZOR)
 *                      Zone of Responsibility (ZOR) Circular
 *                  Target Build-Up Area (TBA)
 *                      Target Build-Up Area (TBA) Circular
 *                  Target Value Area (TVAR)
 *                      Target Value Area (TVAR) Circular
 *              Target Acquisition Zones
 *                  Artillery Target Intelligence (ATI)
 *                      Artillery Target Intelligence (ATI) Zone Circular
 *                  Call for Fire Zone (CFFZ)
 *                      Call For Fire Zone (CFFZ) Circular
 *                  Censor Zone
 *                      Censor Zone Circular
 *                  Critical Friendly Zone (CFZ)
 *                      Critical Friendly Zone (CFZ) Circular
 * 2525C
 * Tactical Graphics
 *      Fire Support
 *          Areas
 *              Kill Box
 *                  Blue
 *                      Kill Box Blue Circular
 *                  Purple
 *                      Kill Box Purple Circular
 */
public class MilStdDCCircularParamAutoShapeEditor extends AbstractMilStdMultiPointEditor {

    public MilStdDCCircularParamAutoShapeEditor(IMapInstance map, MilStdSymbol feature, IEditEventListener oEventListener, armyc2.c2sd.renderer.utilities.SymbolDef symDef) throws EMP_Exception {
        super(map, feature, oEventListener, symDef);
        this.initializeEdit();
    }

    public MilStdDCCircularParamAutoShapeEditor(IMapInstance map, MilStdSymbol feature, IDrawEventListener oEventListener, armyc2.c2sd.renderer.utilities.SymbolDef symDef) throws EMP_Exception {
        super(map, feature, oEventListener, symDef);
        this.initializeDraw();
    }

    private float getRadius() {
        return this.oFeature.getNumericModifier(IGeoMilSymbol.Modifier.DISTANCE, 0);
    }

    private void setRadius(double value) {
        if (!Float.isNaN(this.getRadius())) {
            // If there is a radius, delete it first.
            this.oFeature.setModifier(IGeoMilSymbol.Modifier.DISTANCE, 0, Float.NaN);
        }
        this.oFeature.setModifier(IGeoMilSymbol.Modifier.DISTANCE, 0, (float) Math.rint(value));
    }

    private void checkRadiusModifier() {
        if (Float.isNaN(this.getRadius())) {
            // No AM modifier provided.
            IGeoPosition cameraPos = this.getMapCameraPosition();
            float tempRadius = (float) Math.rint(cameraPos.getAltitude() / 6.0);

            if (tempRadius > 10000) {
                // we can change this later.
                // Make sure its not greater than some threshold.
                tempRadius = 10000;
            }

            this.setRadius(tempRadius);
        }

        // Remove any additional AM values.
        while (!Float.isNaN(this.oFeature.getNumericModifier(IGeoMilSymbol.Modifier.DISTANCE, 1))) {
            this.oFeature.setModifier(IGeoMilSymbol.Modifier.DISTANCE, 1, Float.NaN);
        }
    }

    @Override
    protected void prepareForDraw() throws EMP_Exception {
        IGeoPosition cameraPos = this.getMapCameraPosition();
        List<IGeoPosition> posList = this.getPositions();
        IGeoPosition pos;

        // For drawing we remove all positions and start from scratch.
        posList.clear();

        // Remove all AM values.
        while (!Float.isNaN(this.oFeature.getNumericModifier(IGeoMilSymbol.Modifier.DISTANCE, 0))) {
            this.oFeature.setModifier(IGeoMilSymbol.Modifier.DISTANCE, 0, Float.NaN);
        }

        this.checkRadiusModifier();
        // Add P1 at the center.
        pos = new GeoPosition();
        pos.setAltitude(0);
        pos.setLatitude(cameraPos.getLatitude());
        pos.setLongitude(cameraPos.getLongitude());
        posList.add(pos);

        this.addUpdateEventData(FeatureEditUpdateTypeEnum.COORDINATE_ADDED, new int[]{0});
    }

    @Override
    protected void prepareForEdit() throws EMP_Exception {
        this.checkRadiusModifier();
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
        GeoLibrary.computePositionAt(90.0, this.getRadius(), posList.get(0), pos);
        controlPoint = new ControlPoint(ControlPoint.CPTypeEnum.RADIUS_CP, 0, -1);
        controlPoint.setPosition(pos);
        this.addControlPoint(controlPoint);
    }

    @Override
    protected boolean doControlPointMoved(ControlPoint oCP, IGeoPosition oLatLon) {
        boolean moved = false;
        switch (oCP.getCPType()) {
            case POSITION_CP: {
                // The center was moved.
                ControlPoint radiusCP = this.findControlPoint(ControlPoint.CPTypeEnum.RADIUS_CP, 0, -1);
                IGeoPosition centerPos = oCP.getPosition();

                // Set the center new position.
                centerPos.setLatitude(oLatLon.getLatitude());
                centerPos.setLongitude(oLatLon.getLongitude());
                // Move the radius CP.
                GeoLibrary.computePositionAt(90.0, this.getRadius(), centerPos, radiusCP.getPosition());
                this.addUpdateEventData(FeatureEditUpdateTypeEnum.COORDINATE_MOVED, new int[]{0});
                moved = true;
                break;
            }
            case RADIUS_CP: {
                // The radius was moved.
                ControlPoint centerCP = this.findControlPoint(ControlPoint.CPTypeEnum.POSITION_CP, 0, -1);
                double newRadius = Math.rint(GeoLibrary.computeDistanceBetween(centerCP.getPosition(), oLatLon));

                // set the radius CP new position.
                GeoLibrary.computePositionAt(90.0, newRadius, centerCP.getPosition(), oCP.getPosition());

                // Set the modifier.
                this.setRadius(newRadius);

                moved = true;
                this.addUpdateEventData(IGeoMilSymbol.Modifier.DISTANCE);
                break;
            }
        }

        return moved;
    }
}
