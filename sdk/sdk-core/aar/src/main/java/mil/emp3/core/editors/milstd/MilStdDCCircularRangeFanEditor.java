package mil.emp3.core.editors.milstd;

import org.cmapi.primitives.GeoPosition;
import org.cmapi.primitives.IGeoMilSymbol;
import org.cmapi.primitives.IGeoPosition;

import mil.emp3.api.MilStdSymbol;
import mil.emp3.api.enums.FeatureEditUpdateTypeEnum;
import mil.emp3.api.exceptions.EMP_Exception;
import mil.emp3.api.listeners.IDrawEventListener;
import mil.emp3.api.listeners.IEditEventListener;
import mil.emp3.api.utils.GeoLibrary;
import mil.emp3.core.editors.ControlPoint;
import mil.emp3.mapengine.interfaces.IMapInstance;

/**
 * This class implements the editor for circular range fan tactical graphics.
 * It uses 1 position, 1 - 3 AM distances for ranges.
 *
 * The center POSITION control point index is the index of the position in the features position list. And
 * its subindex is -1.
 * The index of the RANGE control point id the index of the range in the DISTANCE modifier and the subindex is -1.
 *
 * This graphic requies at least 1 AM range value and allows upto 3 range AM values.
 *
 */
public class MilStdDCCircularRangeFanEditor extends AbstractMilStdMultiPointEditor {
    private final java.util.List<ControlPoint> cpList = new java.util.ArrayList<>();
    private final java.util.List<Float> rangeList = new java.util.ArrayList<>();
    private static final int MIN_RANGES = 1;
    private static final int MAX_RANGES = 3;

    public MilStdDCCircularRangeFanEditor(IMapInstance map, MilStdSymbol feature, IEditEventListener oEventListener, armyc2.c2sd.renderer.utilities.SymbolDef symDef) throws EMP_Exception {
        super(map, feature, oEventListener, symDef);
        this.initializeEdit();
    }

    public MilStdDCCircularRangeFanEditor(IMapInstance map, MilStdSymbol feature, IDrawEventListener oEventListener, armyc2.c2sd.renderer.utilities.SymbolDef symDef) throws EMP_Exception {
        super(map, feature, oEventListener, symDef);
        this.initializeDraw();
    }

    private void checkAMModifier() {
        for (int index = 0; !Float.isNaN(this.symbol.getNumericModifier(IGeoMilSymbol.Modifier.DISTANCE, index)); index++) {
            this.rangeList.add(this.symbol.getNumericModifier(IGeoMilSymbol.Modifier.DISTANCE, index));
        }

        if (this.rangeList.isEmpty()) {
            // No AM modifier provided.
            float tempDistance = 3219; // ~ 2 miles.

            this.insertRangeValue(0, tempDistance);

            this.addUpdateEventData(IGeoMilSymbol.Modifier.DISTANCE);
        } else {
            // Delete all additional ranges.
            while (this.rangeList.size() > MAX_RANGES) {
                this.symbol.setModifier(IGeoMilSymbol.Modifier.DISTANCE, this.rangeList.size() - 1, Float.NaN);
                this.rangeList.remove(this.rangeList.size() - 1);
            }
        }
    }

    @Override
    protected void prepareForDraw() throws EMP_Exception {
        IGeoPosition cameraPos = this.getMapCameraPosition();
        java.util.List<IGeoPosition> posList = this.getPositions();
        IGeoPosition pos;

        posList.clear();

        // Delete all ranges.
        while (!Float.isNaN(this.symbol.getNumericModifier(IGeoMilSymbol.Modifier.DISTANCE, 0))) {
            this.symbol.setModifier(IGeoMilSymbol.Modifier.DISTANCE, 0, Float.NaN);
        }

        this.checkAMModifier();

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
    }

    private void positionRangeCP(int index) {
        IGeoPosition pos;
        ControlPoint controlPoint;
        float range = this.symbol.getNumericModifier(IGeoMilSymbol.Modifier.DISTANCE, index);
        IGeoPosition centerPos = this.getPositions().get(0);

        // Get range CP.
        controlPoint = this.findControlPoint(ControlPoint.CPTypeEnum.RANGE_CP, index, -1);
        GeoLibrary.computePositionAt(90.0, range, centerPos, controlPoint.getPosition());
    }

    private void createRange(int index) {
        IGeoPosition pos;
        ControlPoint controlPoint;
        float range = this.symbol.getNumericModifier(IGeoMilSymbol.Modifier.DISTANCE, index);
        IGeoPosition centerPos = this.getPositions().get(0);

        // Create range CP.
        pos = new GeoPosition();
        controlPoint = new ControlPoint(ControlPoint.CPTypeEnum.RANGE_CP, index, -1);
        controlPoint.setPosition(pos);
        GeoLibrary.computePositionAt(90.0, range, centerPos, pos);
        this.addControlPoint(controlPoint);
        this.cpList.add(controlPoint);

    }

    @Override
    protected void assembleControlPoints() {
        java.util.List<IGeoPosition> posList = this.getPositions();
        ControlPoint controlPoint;

        // Add the center control point.
        controlPoint = new ControlPoint(ControlPoint.CPTypeEnum.POSITION_CP, 0, -1);
        controlPoint.setPosition(posList.get(0));
        this.addControlPoint(controlPoint);

        // Now add the range CPs.
        for (int index = 0; index < this.rangeList.size(); index++) {
            this.createRange(index);
        }
    }

    private float getPreviousRangeValue(int index) {
        float prevRange = 0;
        float tempRange = this.symbol.getNumericModifier(IGeoMilSymbol.Modifier.DISTANCE, index - 1);

        // Get the previous range value if there is one.
        if ((index > 0) && (!Float.isNaN(tempRange))) {
            prevRange = tempRange;
        }

        return prevRange;
    }

    private float getNextRangeValue(int index) {
        float nextRange = Float.MAX_VALUE;
        float tempRange = this.symbol.getNumericModifier(IGeoMilSymbol.Modifier.DISTANCE, index + 1);

        // Get the next range value if there is one.
        if (!Float.isNaN(tempRange)) {
            nextRange = tempRange;
        }

        return nextRange;
    }

    private void setRangeValue(int index, float value) {
        this.rangeList.set(index, value);
        this.symbol.setModifier(IGeoMilSymbol.Modifier.DISTANCE, index, Float.NaN);
        this.symbol.setModifier(IGeoMilSymbol.Modifier.DISTANCE, index, value);
    }

    private void insertRangeValue(int index, float value) {
        this.rangeList.add(index, value);
        this.symbol.setModifier(IGeoMilSymbol.Modifier.DISTANCE, index, value);
    }

    private void removeRangeValue(int index) {
        this.rangeList.remove(index);
        this.symbol.setModifier(IGeoMilSymbol.Modifier.DISTANCE, index, Float.NaN);
    }

    @Override
    protected java.util.List<ControlPoint> doControlPointMoved(ControlPoint oCP, IGeoPosition dragPosition) {
        int cpIndex = oCP.getCPIndex();
        int cpSubIndex = oCP.getCPSubIndex();

        this.cpList.clear();
        switch (oCP.getCPType()) {
            case POSITION_CP: {
                // The center was moved.
                double distanceMoved = GeoLibrary.computeDistanceBetween(oCP.getPosition(), dragPosition);
                double bearingMoved = GeoLibrary.computeBearing(oCP.getPosition(), dragPosition);

                // Move all control points.
                oCP.getPosition().setLatitude(dragPosition.getLatitude());
                oCP.getPosition().setLongitude(dragPosition.getLongitude());

                for (int index = 0; index < this.rangeList.size(); index++) {
                    this.positionRangeCP(index);
                }

                this.addUpdateEventData(FeatureEditUpdateTypeEnum.COORDINATE_MOVED, new int[]{0});
                break;
            }
            case RANGE_CP: {
                // A range CP has been moved.
                float newRange;
                float prevRange = this.getPreviousRangeValue(cpIndex);
                float nextRange = this.getNextRangeValue(cpIndex);
                IGeoPosition centerPos = this.getPositions().get(0);

                // Calculate the new range.
                newRange = (float) GeoLibrary.computeDistanceBetween(centerPos, dragPosition);

                // Now we need to make sure that the new range is not < (prevRange + threshold)
                if (newRange < (prevRange + 100)) {
                    newRange = prevRange + 100; // This is so they don't end up onto of each other.
                }

                // Now we need to make sure that the new range is not > (nextRange - threshold)
                if (newRange > (nextRange - 100)) {
                    newRange = nextRange - 100; // This is so they don't end up onto of each other.
                }

                this.setRangeValue(cpIndex, newRange);
                GeoLibrary.computePositionAt(90.0, newRange, centerPos, oCP.getPosition());
                this.addUpdateEventData(IGeoMilSymbol.Modifier.DISTANCE);

                break;
            }
        }
        cpList.add(oCP);

        return this.cpList;
    }

    private int getIndexForNewRange(float newRange) {
        int index;

        // The ranges should be sorted by distance from smallest to larges.
        for (index = 0; index < this.rangeList.size(); index++) {
            if (newRange < this.rangeList.get(index)) {
                break;
            }
        }
        return index;
    }

    protected java.util.List<ControlPoint> doAddControlPoint(IGeoPosition newPosition) {
        if (this.rangeList.size() == MAX_RANGES) {
            // Once we have the max # of ranges we cant add any more.
            return null;
        }

        IGeoPosition centerPos = this.getPositions().get(0);
        float newRange = (float) GeoLibrary.computeDistanceBetween(centerPos, newPosition);
        int newRangeIndex = this.getIndexForNewRange(newRange);

        this.cpList.clear();

        // Shift the range indexes for the ranges
        this.changeControlPointIndexes(ControlPoint.CPTypeEnum.RANGE_CP, newRangeIndex, 1);

        // Add the new range value
        this.insertRangeValue(newRangeIndex, newRange);

        this.createRange(newRangeIndex);
        this.addUpdateEventData(IGeoMilSymbol.Modifier.DISTANCE);

        return this.cpList;
    }

    protected java.util.List<ControlPoint>  doDeleteControlPoint(ControlPoint oCP) {
        int cpIndex = oCP.getCPIndex();

        this.cpList.clear();

        switch (oCP.getCPType()) {
            case RANGE_CP: {
                if (this.rangeList.size() == MIN_RANGES) {
                    // We cant delete beyond the min range.
                    return null;
                }

                IGeoPosition centerPos = this.getPositions().get(0);

                this.removeRangeValue(cpIndex);
                this.changeControlPointIndexes(ControlPoint.CPTypeEnum.RANGE_CP, cpIndex, -1);
                this.cpList.add(oCP);
                break;
            }
        }
        return this.cpList;
    }
}
