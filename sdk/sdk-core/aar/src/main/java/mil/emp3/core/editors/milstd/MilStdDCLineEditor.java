package mil.emp3.core.editors.milstd;

import android.webkit.GeolocationPermissions;

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
import mil.emp3.api.utils.EmpGeoPosition;
import mil.emp3.api.utils.GeoLibrary;
import mil.emp3.core.editors.ControlPoint;
import mil.emp3.core.utils.CoreMilStdUtilities;
import mil.emp3.mapengine.interfaces.IMapInstance;

/**
 * This class implements the MilStd editor for Line draw category TG.
 */
public class MilStdDCLineEditor extends AbstractMilStdMultiPointEditor{

    public MilStdDCLineEditor(IMapInstance map, MilStdSymbol feature, IEditEventListener oEventListener, armyc2.c2sd.renderer.utilities.SymbolDef symDef) throws EMP_Exception {
        super(map, feature, oEventListener, symDef);
        this.initializeEdit();
    }

    public MilStdDCLineEditor(IMapInstance map, MilStdSymbol feature, IDrawEventListener oEventListener, armyc2.c2sd.renderer.utilities.SymbolDef symDef, boolean newFeature) throws EMP_Exception {
        super(map, feature, oEventListener, symDef, newFeature);
        this.initializeDraw();
    }

    private boolean hasWidth() {
        boolean retValue = false;

        switch (this.basicSymbolCode) {
            case CoreMilStdUtilities.AIR_CORRIDOR:
            case CoreMilStdUtilities.MINIMUM_RISK_ROUTE:
            case CoreMilStdUtilities.STANDARD_ARMY_AIRCRAFT_FLIGHT_ROUTE:
            case CoreMilStdUtilities.UBMANNED_AERIAL_VEHICLE_ROUTE:
            case CoreMilStdUtilities.LOW_LEVEL_TRANSIT_ROUTE:
                retValue = true;
                break;
        }

        return retValue;
    }

    private double getWidth() {
        double retValue = Double.NaN;

        if (this.hasWidth()) {
            // As per 2525C COE V2 it is in AM modifier.
            // As per 2525B COE V1 it should be in H2 but everyone seems to put in AM.
            // So we will assume it always in AM.
            if (!Float.isNaN(this.oFeature.getNumericModifier(IGeoMilSymbol.Modifier.DISTANCE, 0))) {
                retValue = (double) this.oFeature.getNumericModifier(IGeoMilSymbol.Modifier.DISTANCE, 0);
            }
/*
            switch (this.symbol.getSymbolStandard()) {
                case MIL_STD_2525C:
                    if (!Float.isNaN(this.symbol.getNumericModifier(IGeoMilSymbol.Modifier.DISTANCE, 0))) {
                        retValue = (double) this.symbol.getNumericModifier(IGeoMilSymbol.Modifier.DISTANCE, 0);
                    }
                    break;
                case MIL_STD_2525B: {
                    String H2 = this.symbol.getStringModifier(IGeoMilSymbol.Modifier.ADDITIONAL_INFO_2);
                    if (H2 != null) {
                        String digits = "";
                        for (int index = 0; index < H2.length(); index++) {
                            char CurrentChar = H2.charAt(index);
                            if (Character.isDigit(CurrentChar)) {
                                digits += CurrentChar;
                            } else {
                                break;
                            }
                        }
                        try {
                            retValue = Float.parseFloat(digits);
                        } catch (NumberFormatException ex) {
                        }
                    }
                    break;
                }
            }
*/
        }
        return retValue;
    }

    private void saveWidth(double value) {

        if (this.hasWidth()) {
            // As per 2525C COE V2 it is in AM modifier.
            // As per 2525B COE V1 it should be in H2 but everyone seems to put in AM.
            // So we will assume it always in AM.
            this.oFeature.setModifier(IGeoMilSymbol.Modifier.DISTANCE, 0, Float.NaN);
            this.oFeature.setModifier(IGeoMilSymbol.Modifier.DISTANCE, 0, (float) value);
/*
            switch (this.symbol.getSymbolStandard()) {
                case MIL_STD_2525C:
                    this.symbol.setModifier(IGeoMilSymbol.Modifier.DISTANCE, 0, Float.NaN);
                    this.symbol.setModifier(IGeoMilSymbol.Modifier.DISTANCE, 0, (float) value);
                    break;
                case MIL_STD_2525B: {
                    this.symbol.setModifier(IGeoMilSymbol.Modifier.ADDITIONAL_INFO_2, ((float) value));
                    this.symbol.setModifier(IGeoMilSymbol.Modifier.DISTANCE, 0, Float.NaN);
                    this.symbol.setModifier(IGeoMilSymbol.Modifier.DISTANCE, 0, (float) value);
                    break;
                }
            }
*/
        }
    }

    private void verifyModifiers() {
        if (this.hasWidth()) {
            // Requires 1 width value.
            // As per 2525C COE V2 it is in AM modifier.
            // As per 2525B COE V1 it should be in H2 but everyone seems to put in AM.
            // So we will assume it always in AM.
            if (Float.isNaN(this.oFeature.getNumericModifier(IGeoMilSymbol.Modifier.DISTANCE, 0))) {
                // There is no AM modifier.
                // We need to add one.
                IGeoPosition cameraPos = this.getMapCameraPosition();
                // We set the width to 1/10 of the camera altitude.
                float distance = (float) (cameraPos.getAltitude() / 10.0);
                this.oFeature.setModifier(IGeoMilSymbol.Modifier.DISTANCE, 0, distance);
            }
/*
            switch (this.symbol.getSymbolStandard()) {
                case MIL_STD_2525C:
                    if (Float.isNaN(this.symbol.getNumericModifier(IGeoMilSymbol.Modifier.DISTANCE, 0))) {
                        // There is no AM modifier.
                        // We need to add one.
                        this.symbol.setModifier(IGeoMilSymbol.Modifier.DISTANCE, 0, 10000); // 10Km wide.
                    }
                    break;
                case MIL_STD_2525B: {
                    String H2 = this.symbol.getStringModifier(IGeoMilSymbol.Modifier.ADDITIONAL_INFO_2);
                    if (H2 == null) {
                        // See if they put it in the AM
                        if (!Float.isNaN(this.symbol.getNumericModifier(IGeoMilSymbol.Modifier.DISTANCE, 0))) {

                        // We need to add it.
                        this.symbol.setModifier(IGeoMilSymbol.Modifier.ADDITIONAL_INFO_2, "10000m");
                        this.symbol.setModifier(IGeoMilSymbol.Modifier.DISTANCE, 0, Float.NaN);
                        this.symbol.setModifier(IGeoMilSymbol.Modifier.DISTANCE, 0, 10000);
                    }
                    if (Float.isNaN(this.symbol.getNumericModifier(IGeoMilSymbol.Modifier.DISTANCE, 0))) {
                        // There is no AM modifier.
                        // We need to add one.
                        this.symbol.setModifier(IGeoMilSymbol.Modifier.DISTANCE, 0, 10000); // 10Km wide.
                    }
                    break;
                }
            }
*/
        }
    }

    @Override
    protected void prepareForDraw() throws EMP_Exception {
        this.verifyModifiers();
    }

    @Override
    protected void prepareForEdit() throws EMP_Exception {
        this.verifyModifiers();
    }

    private void positionWidthControlPoint(ControlPoint widthCP) {
        if (this.hasWidth()) {
            List<IGeoPosition> posList = this.getPositions();
            int posCnt = posList.size();

            if (posCnt > 1) {
                // See if this TG need a width.
                // We can only add the width CP if there are 2 or more positions.
                if (this.hasWidth()) {
                    double width = this.getWidth();
                    // Calculate the distance and bearing of the line from P1 - P2
                    double bearingP1P2 = GeoLibrary.computeBearing(posList.get(0), posList.get(1));
                    double distanceP1P2 = GeoLibrary.computeDistanceBetween(posList.get(0), posList.get(1));
                    // Calculate the reference position at 1/4 distance between P1 - P2.
                    IGeoPosition qtrPos = GeoLibrary.computePositionAt(bearingP1P2, distanceP1P2 / 4.0, posList.get(0));
                    /// Calculate the width CP position at a -90deg from the ref position of the P1-P2 line.
                    GeoLibrary.computePositionAt(bearingP1P2 - 90.0, width / 2.0, qtrPos, widthCP.getPosition());
                }
            }
        }
    }

    @Override
    protected void assembleControlPoints() {
        List<IGeoPosition> posList = this.getPositions();
        int index = 0;
        int posCnt = posList.size();
        IGeoPosition pos;
        IGeoPosition prevPos = null;
        IGeoPosition newPos;
        ControlPoint controlPoint;

        //Add the control points for each position on the list.
        for (index = 0; index < posCnt; index++) {
            pos = posList.get(index);
            if (index > 0) {
                // There ware position before the current one so we add a new CP.
                this.createCPBetween(prevPos, pos, ControlPoint.CPTypeEnum.NEW_POSITION_CP, index - 1, index);
            }
            controlPoint = new ControlPoint(ControlPoint.CPTypeEnum.POSITION_CP, index, -1);
            controlPoint.setPosition(pos);
            this.addControlPoint(controlPoint);
            prevPos = pos;
        }

        if (posCnt > 1) {
            // See if this TG need a width.
            // We can only add the width CP if there are 2 or more positions.
            if (this.hasWidth()) {
                // Create the control point.
                ControlPoint widthCP = new ControlPoint(ControlPoint.CPTypeEnum.WIDTH_CP, 0, -1);

                widthCP.setPosition(new GeoPosition());
                this.positionWidthControlPoint(widthCP);
                this.addControlPoint(widthCP);
            }
        }
    };

    @Override
    protected List<ControlPoint> doAddControlPoint(IGeoPosition oLatLng) {
        ControlPoint controlPoint;
        List<IGeoPosition> posList = this.getPositions();
        IGeoPosition pos;
        int posCnt = posList.size();

        List<ControlPoint> cpList = new ArrayList<>();

        if (this.inEditMode()) {
            // In Edit mode we do not add CP. The user needs to drag new CP.
            return cpList;
        }

        // Compute the position control point.
        pos = new EmpGeoPosition(oLatLng.getLatitude(), oLatLng.getLongitude());
        controlPoint = new ControlPoint(ControlPoint.CPTypeEnum.POSITION_CP, 0, -1);
        controlPoint.setPosition(pos);
        cpList.add(controlPoint);
        // Add the new position to the feature position list.
        posList.add(0, pos);

        this.increaseControlPointIndexes(0);

        if (this.hasWidth() && (posList.size() >= 2)) {
            ControlPoint widthCP;
            if (posList.size() == 2) {
                // We have added the 2nd point so it needs a width CP.
                widthCP = new ControlPoint(ControlPoint.CPTypeEnum.WIDTH_CP, 0, -1);
                widthCP.setPosition(new GeoPosition());
                this.addControlPoint(widthCP);
            } else {
                widthCP = this.findControlPoint(ControlPoint.CPTypeEnum.WIDTH_CP, 1, -1);
                widthCP.setCPIndex(0);
            }
            this.positionWidthControlPoint(widthCP);
        }

        if (posList.size() > 1) {
            // Compute the new CP between the first and 2nd position2.
            controlPoint = this.createCPBetween(oLatLng, posList.get(1), ControlPoint.CPTypeEnum.NEW_POSITION_CP, 0, 1);
            cpList.add(controlPoint);
        }

        // Add the update data
        this.addUpdateEventData(FeatureEditUpdateTypeEnum.COORDINATE_ADDED, new int[]{0});

        return cpList;
    }

    protected List<ControlPoint>  doDeleteControlPoint(ControlPoint oCP) {
        List<IGeoPosition> posList = this.getPositions();
        int lastIndex = posList.size() - 1;

        List<ControlPoint> cpList = new ArrayList<>();

        if (oCP.getCPType() == ControlPoint.CPTypeEnum.WIDTH_CP) {
            // We never delete the width CP.
            return cpList;
        }

        if (posList.size() > 2) {
            // We only remove points beyond the minimum # of positions.

            if (oCP.getCPType() != ControlPoint.CPTypeEnum.POSITION_CP) {
                // We only remove position CP.
                return cpList;
            }

            cpList.add(oCP);

            this.addUpdateEventData(FeatureEditUpdateTypeEnum.COORDINATE_DELETED, new int[]{oCP.getCPIndex()});

            // Calculate the before and after index of the new PC before this CP.
            int beforeIndex = (oCP.getCPIndex() + posList.size() - 1) % posList.size();
            int afterIndex = (oCP.getCPIndex() + 1) % posList.size();

            // Now find and add the before new position CP to the list.
            ControlPoint newCP = this.findControlPoint(ControlPoint.CPTypeEnum.NEW_POSITION_CP, beforeIndex, oCP.getCPIndex());
            if (newCP != null) {
                // The first point does not have a new CP before it.
                cpList.add(newCP);
            }

            // Now find and add the after new position CP to the list.
            newCP = this.findControlPoint(ControlPoint.CPTypeEnum.NEW_POSITION_CP, oCP.getCPIndex(), afterIndex);
            if (newCP != null) {
                // The last point does not have a new CP after it.
                cpList.add(newCP);
            }

            // Remove the position from the feature position list.
            posList.remove(oCP.getCPIndex());

            if ((beforeIndex != lastIndex) && (afterIndex != 0)) {
                // Now we add a new control point between the CP before and after the one removed.
                ControlPoint beforeCP = this.findControlPoint(ControlPoint.CPTypeEnum.POSITION_CP, beforeIndex, -1);
                ControlPoint afterCP = this.findControlPoint(ControlPoint.CPTypeEnum.POSITION_CP, afterIndex, -1);
                this.createCPBetween(beforeCP.getPosition(), afterCP.getPosition(), ControlPoint.CPTypeEnum.NEW_POSITION_CP, beforeIndex, afterIndex);
            }

            this.decreaseControlPointIndexes(oCP.getCPIndex());

            if (oCP.getCPIndex() < 2) {
                ControlPoint widthCP = this.findControlPoint(ControlPoint.CPTypeEnum.WIDTH_CP, 0, -1);
                if (widthCP != null) {
                    this.positionWidthControlPoint(widthCP);
                }
            }
        }

        return cpList;
    }

    protected boolean doControlPointMoved(ControlPoint oCP, IGeoPosition oLatLon) {
        double dDistance;
        double dBearing;
        ControlPoint newCP;
        IGeoPosition newPosition;
        IGeoPosition currentPosition = oCP.getPosition();
        int cpIndex = oCP.getCPIndex();
        int cpSubIndex = oCP.getCPSubIndex();
        List<IGeoPosition> posList = this.getPositions();

        boolean moved = false;

        switch (oCP.getCPType()) {
            case NEW_POSITION_CP: {
                //Get the control point before this new CP.
                ControlPoint beforeCP = this.findControlPoint(ControlPoint.CPTypeEnum.POSITION_CP, cpIndex, -1);
                //Get the control point that is after this new CP.
                ControlPoint afterCP = this.findControlPoint(ControlPoint.CPTypeEnum.POSITION_CP, cpSubIndex, -1);

                //Change the new CP to a position CP and update its position.
                oCP.setCPType(ControlPoint.CPTypeEnum.POSITION_CP);
                oCP.setCPSubIndex(-1);
                currentPosition.setLatitude(oLatLon.getLatitude());
                currentPosition.setLongitude(oLatLon.getLongitude());

                // Increase the indexes of all CP after this one.
                this.increaseControlPointIndexes(cpSubIndex);

                // Set the index of this new position CP.
                oCP.setCPIndex(cpSubIndex);

                // Add this CP position to the features position list.
                posList.add(cpSubIndex, oCP.getPosition());

                // Now that we added the new position we need to create the new CP between them.
                // Create the new CP between the beforeCP and this one.
                // Get the distance and bearing between the beforeCP and this one and create the new CP
                this.createCPBetween(beforeCP.getPosition(), oLatLon, ControlPoint.CPTypeEnum.NEW_POSITION_CP, cpIndex, cpSubIndex);

                // Create the new CP between this one and the afterCP.
                // Get the distance and bearing between this one and the afterCP and create the new CP
                this.createCPBetween(oLatLon, afterCP.getPosition(), ControlPoint.CPTypeEnum.NEW_POSITION_CP, cpSubIndex, cpSubIndex + 1);

                // Now we add event update data.
                this.addUpdateEventData(FeatureEditUpdateTypeEnum.COORDINATE_ADDED, new int[]{oCP.getCPIndex()});

                if (oCP.getCPIndex() < 2) {
                    ControlPoint widthCP = this.findControlPoint(ControlPoint.CPTypeEnum.WIDTH_CP, 0, -1);
                    if (widthCP != null) {
                        this.positionWidthControlPoint(widthCP);
                        moved = true;
                    }
                }
                moved = true;
                break;
            }
            case POSITION_CP: {
                // Set the control points coordinates.
                currentPosition.setLatitude(oLatLon.getLatitude());
                currentPosition.setLongitude((oLatLon.getLongitude()));
                // Add the update data
                this.addUpdateEventData(FeatureEditUpdateTypeEnum.COORDINATE_MOVED, new int[]{oCP.getCPIndex()});
                moved = true;

                // Now we need to move the new CP that may be before and after this one.
                if (cpIndex > 0) {
                    // It is not the first CP so it has a new CP before it and a position CP before that.
                    ControlPoint beforeCP = this.findControlPoint(ControlPoint.CPTypeEnum.POSITION_CP, cpIndex - 1, -1);
                    ControlPoint newBeforeCP = this.findControlPoint(ControlPoint.CPTypeEnum.NEW_POSITION_CP, cpIndex - 1, cpIndex);

                    // Compute and set the location of the newBeforeCP
                    newBeforeCP.moveCPBetween(beforeCP.getPosition(), currentPosition);
                }

                if (cpIndex < (posList.size() - 1)) {
                    // It is not the last one so it has a new CP after it and a position CP after that.
                    ControlPoint afterCP = this.findControlPoint(ControlPoint.CPTypeEnum.POSITION_CP, cpIndex + 1, -1);
                    ControlPoint newAfterCP = this.findControlPoint(ControlPoint.CPTypeEnum.NEW_POSITION_CP, cpIndex, cpIndex + 1);

                    // Compute and set the location of the newAfterCP
                    newAfterCP.moveCPBetween(currentPosition, afterCP.getPosition());
                }

                if (oCP.getCPIndex() < 2) {
                    ControlPoint widthCP = this.findControlPoint(ControlPoint.CPTypeEnum.WIDTH_CP, 0, -1);
                    if (widthCP != null) {
                        this.positionWidthControlPoint(widthCP);
                        moved = true;
                    }
                }
                break;
            }
            case WIDTH_CP: {
                // Calculate the distance between the qtr position and the new position.
                double bearingP1P2 = GeoLibrary.computeBearing(posList.get(0), posList.get(1));
                double distanceP1P2 = GeoLibrary.computeDistanceBetween(posList.get(0), posList.get(1));
                // Calculate the reference position at 1/4 distance between P1 - P2.
                IGeoPosition qtrPos = GeoLibrary.computePositionAt(bearingP1P2, distanceP1P2 / 4.0, posList.get(0));
                double newHalfWidth = GeoLibrary.computeDistanceBetween(qtrPos, oLatLon);

                // Update the value
                this.saveWidth(Math.floor(newHalfWidth * 2));
                this.positionWidthControlPoint(oCP);

                moved = true;
            }
        }

        return moved;
    }
}
