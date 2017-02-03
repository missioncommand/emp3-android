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
 * This class implements the editor for sector range fan tactical graphics.
 * It uses 1 position, N AM distances for ranges, and between 2 * (N - 2) to 2 * N AN azimuth.
 *
 * If there are 2 * (N - 2) AN this indicates that the first range is the minimum range and has no
 * AN associated with it.
 *
 * The center POSITION control point index is the index of the position in the features position list. And
 * its subindex is -1.
 * The index of the RANGE control point id the index of the range in the DISTANCE modifier and the subindex is -1.
 * The index of the left and right AZIMUTH control points is the index of the range it applies to. The subindex
 * is the index of the value in the AZIMUTH modifier.
 *
 */
public class MilStdDCSectorRangeFanEditor extends AbstractMilStdMultiPointEditor {
    private final List<Float> rangeList = new ArrayList<>();
    private final List<Float> azimuthList = new ArrayList<>();
    private boolean hasMinimumRange = false;

    public MilStdDCSectorRangeFanEditor(IMapInstance map, MilStdSymbol feature, IEditEventListener oEventListener, armyc2.c2sd.renderer.utilities.SymbolDef symDef) throws EMP_Exception {
        super(map, feature, oEventListener, symDef);
        this.initializeEdit();
    }

    public MilStdDCSectorRangeFanEditor(IMapInstance map, MilStdSymbol feature, IDrawEventListener oEventListener, armyc2.c2sd.renderer.utilities.SymbolDef symDef) throws EMP_Exception {
        super(map, feature, oEventListener, symDef);
        this.initializeDraw();
    }

    private void checkAMModifier() {
        for (int index = 0; !Float.isNaN(this.oFeature.getNumericModifier(IGeoMilSymbol.Modifier.DISTANCE, index)); index++) {
            this.rangeList.add(this.oFeature.getNumericModifier(IGeoMilSymbol.Modifier.DISTANCE, index));
        }

        if (this.rangeList.isEmpty()) {
            // No AM modifier provided.
            float tempDistance = 8046; // ~ 5 miles.

            this.insertRangeValue(0, tempDistance);

            this.addUpdateEventData(IGeoMilSymbol.Modifier.DISTANCE);
        } else {
            // TODO >>> make sure that the entries are sorted.
        }
    }

    private void checkANModifier() {
        for (int index = 0; !Float.isNaN(this.oFeature.getNumericModifier(IGeoMilSymbol.Modifier.AZIMUTH, index)); index++) {
            this.azimuthList.add(this.oFeature.getNumericModifier(IGeoMilSymbol.Modifier.AZIMUTH, index));
        }

        if (this.azimuthList.isEmpty()) {
            // No AN modifier provided.
            this.insertAzimuthValue(0, 315);
            this.insertAzimuthValue(1, 45);

            this.addUpdateEventData(IGeoMilSymbol.Modifier.AZIMUTH);
        } else {
            if (this.azimuthList.size() == (2 * this.rangeList.size())) {
                // There is no minimum range.
                this.hasMinimumRange = false;
            } else if (this.azimuthList.size() == (2 * (this.rangeList.size() - 2))) {
                // There is a minimum range.
                this.hasMinimumRange = true;
            } else if (this.azimuthList.size() > (2 * this.rangeList.size())) {
                // There are to many AN's.
                int index;
                int maxSize = 2 * this.rangeList.size();

                this.hasMinimumRange = false;
                while (this.azimuthList.size() > maxSize) {
                    index = this.azimuthList.size() - 1;
                    this.removeAzimuthValue(index);
                }
                this.addUpdateEventData(IGeoMilSymbol.Modifier.AZIMUTH);
            } else {
                // There to few azimuth.
                int sizeANList = (2 * this.rangeList.size());

                for (int index = this.azimuthList.size(); index < sizeANList; index++) {
                    if ((index % 2) == 0) {
                        // Its event. Left azimuth
                        this.insertAzimuthValue(index, 315);
                    } else {
                        // Its odd. Right azimuth
                        this.insertAzimuthValue(index, 45);
                    }
                }
                this.addUpdateEventData(IGeoMilSymbol.Modifier.AZIMUTH);
            }
        }
    }

    @Override
    protected void prepareForEdit() throws EMP_Exception {
        this.checkAMModifier();
        this.checkANModifier();
    }

    @Override
    protected void prepareForDraw() throws EMP_Exception {
        IGeoPosition cameraPos = this.getMapCameraPosition();
        List<IGeoPosition> posList = this.getPositions();
        IGeoPosition pos;

        posList.clear();

        while (!Float.isNaN(this.oFeature.getNumericModifier(IGeoMilSymbol.Modifier.DISTANCE, 0))) {
            this.oFeature.setModifier(IGeoMilSymbol.Modifier.DISTANCE, 0, Float.NaN);
        }

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

    private int getAzimuthIndexForRange(int rangeIndex) {
        int azimuthIndex;

        if (this.hasMinimumRange) {
            // It has a minimum range.
            if (rangeIndex == 0) {
                // This is the minimum range sector. It does not have azimuths. But we return 0
                // so the range CP can be positioned properly.
                azimuthIndex = 0;
            } else {
                azimuthIndex = (2 * rangeIndex) - 2;
            }
        } else {
            azimuthIndex = (2 * rangeIndex);
        }

        return azimuthIndex;
    }

    private void positionRangeControlPoint(ControlPoint oCP, float range, float leftAzimuth, float rightAzimuth) {
        float rangeCPBearing;
        IGeoPosition centerPos = this.getPositions().get(0);

        // Calculate the bearing to the range CP.
        rangeCPBearing = (leftAzimuth + rightAzimuth) / 2;
        if (leftAzimuth > rightAzimuth) {
            rangeCPBearing = (rangeCPBearing + 180) % 360;
        }
        // Calculate the position for the range CP.
        GeoLibrary.computePositionAt(rangeCPBearing, range, centerPos, oCP.getPosition());
    }

    private void positionRangeAndAzimuths(int index) {
        float leftAzimuth;
        float rightAzimuth;
        IGeoPosition pos;
        ControlPoint controlPoint;
        float prevRange = 0;
        float distanceAzimuthCP;
        int azimuthIndex = this.getAzimuthIndexForRange(index);
        float range = this.oFeature.getNumericModifier(IGeoMilSymbol.Modifier.DISTANCE, index);
        IGeoPosition centerPos = this.getPositions().get(0);

        // Get the left and right azimuth value.
        leftAzimuth = this.oFeature.getNumericModifier(IGeoMilSymbol.Modifier.AZIMUTH, azimuthIndex);
        rightAzimuth = this.oFeature.getNumericModifier(IGeoMilSymbol.Modifier.AZIMUTH, azimuthIndex + 1);

        // Get range CP.
        controlPoint = this.findControlPoint(ControlPoint.CPTypeEnum.RANGE_CP, index, -1);
        this.positionRangeControlPoint(controlPoint, range, leftAzimuth, rightAzimuth);

        if (this.hasMinimumRange && (index == 0)) {
            // It is a minimum range, so we do not add azimuth CPs.
            return;
        }

        if (index > 0) {
            prevRange = this.oFeature.getNumericModifier(IGeoMilSymbol.Modifier.DISTANCE, index - 1);
        }

        distanceAzimuthCP = (range + prevRange) / 2;
        // Get left azimuth CP.
        controlPoint = this.findControlPoint(ControlPoint.CPTypeEnum.LEFT_AZIMUTH_CP, index, azimuthIndex);
        GeoLibrary.computePositionAt(leftAzimuth, distanceAzimuthCP, centerPos, controlPoint.getPosition());

        // Get right azimuth CP.
        controlPoint = this.findControlPoint(ControlPoint.CPTypeEnum.RIGHT_AZIMUTH_CP, index, azimuthIndex + 1);
        GeoLibrary.computePositionAt(rightAzimuth, distanceAzimuthCP, centerPos, controlPoint.getPosition());
    }

    private void createRangeAndAzimuths(int index) {
        float leftAzimuth;
        float rightAzimuth;
        IGeoPosition pos;
        ControlPoint controlPoint;
        float prevRange = 0;
        float distanceAzimuthCP;
        int azimuthIndex = this.getAzimuthIndexForRange(index);
        float range = this.oFeature.getNumericModifier(IGeoMilSymbol.Modifier.DISTANCE, index);
        IGeoPosition centerPos = this.getPositions().get(0);

        // Get the left and right azimuth value.
        leftAzimuth = this.oFeature.getNumericModifier(IGeoMilSymbol.Modifier.AZIMUTH, azimuthIndex);
        rightAzimuth = this.oFeature.getNumericModifier(IGeoMilSymbol.Modifier.AZIMUTH, azimuthIndex + 1);

        // Create range CP.
        pos = new GeoPosition();
        controlPoint = new ControlPoint(ControlPoint.CPTypeEnum.RANGE_CP, index, -1);
        controlPoint.setPosition(pos);
        this.positionRangeControlPoint(controlPoint, range, leftAzimuth, rightAzimuth);
        this.addControlPoint(controlPoint);

        if (this.hasMinimumRange && (index == 0)) {
            // It is a minimum range, so we do not add azimuth CPs.
            return;
        }

        if (index > 0) {
            prevRange = this.oFeature.getNumericModifier(IGeoMilSymbol.Modifier.DISTANCE, index - 1);
        }

        distanceAzimuthCP = (range + prevRange) / 2;
        // Create left azimuth CP.
        pos = new GeoPosition();
        controlPoint = new ControlPoint(ControlPoint.CPTypeEnum.LEFT_AZIMUTH_CP, index, azimuthIndex);
        GeoLibrary.computePositionAt(leftAzimuth, distanceAzimuthCP, centerPos, pos);
        controlPoint.setPosition(pos);
        this.addControlPoint(controlPoint);

        // Create right azimuth CP.
        pos = new GeoPosition();
        controlPoint = new ControlPoint(ControlPoint.CPTypeEnum.RIGHT_AZIMUTH_CP, index, azimuthIndex + 1);
        GeoLibrary.computePositionAt(rightAzimuth, distanceAzimuthCP, centerPos, pos);
        controlPoint.setPosition(pos);
        this.addControlPoint(controlPoint);
    }

    @Override
    protected void assembleControlPoints() {
        List<IGeoPosition> posList = this.getPositions();
        ControlPoint controlPoint;

        // Add the center control point.
        controlPoint = new ControlPoint(ControlPoint.CPTypeEnum.POSITION_CP, 0, -1);
        controlPoint.setPosition(posList.get(0));
        this.addControlPoint(controlPoint);

        // Now add the range CPs.
        for (int index = 0; index < this.rangeList.size(); index++) {
            this.createRangeAndAzimuths(index);
        }
    }

    private float getPreviousRangeValue(int index) {
        float prevRange = 0;
        float tempRange = this.oFeature.getNumericModifier(IGeoMilSymbol.Modifier.DISTANCE, index - 1);

        // Get the previous range value if there is one.
        if ((index > 0) && (!Float.isNaN(tempRange))) {
            prevRange = tempRange;
        }

        return prevRange;
    }

    private float getNextRangeValue(int index) {
        float nextRange = Float.MAX_VALUE;
        float tempRange = this.oFeature.getNumericModifier(IGeoMilSymbol.Modifier.DISTANCE, index + 1);

        // Get the next range value if there is one.
        if (!Float.isNaN(tempRange)) {
            nextRange = tempRange;
        }

        return nextRange;
    }

    private void setRangeValue(int index, float value) {
        this.rangeList.set(index, value);
        this.oFeature.setModifier(IGeoMilSymbol.Modifier.DISTANCE, index, Float.NaN);
        this.oFeature.setModifier(IGeoMilSymbol.Modifier.DISTANCE, index, value);
    }

    private void setAzimuthValue(int index, float value) {
        this.azimuthList.set(index, value);
        this.oFeature.setModifier(IGeoMilSymbol.Modifier.AZIMUTH, index, Float.NaN);
        this.oFeature.setModifier(IGeoMilSymbol.Modifier.AZIMUTH, index, value);
    }

    private void insertRangeValue(int index, float value) {
        this.rangeList.add(index, value);
        this.oFeature.setModifier(IGeoMilSymbol.Modifier.DISTANCE, index, value);
    }

    private void insertAzimuthValue(int index, float value) {
        this.azimuthList.add(index, value);
        this.oFeature.setModifier(IGeoMilSymbol.Modifier.AZIMUTH, index, value);
    }

    private void removeRangeValue(int index) {
        this.rangeList.remove(index);
        this.oFeature.setModifier(IGeoMilSymbol.Modifier.DISTANCE, index, Float.NaN);
    }

    private void removeAzimuthValue(int index) {
        this.azimuthList.remove(index);
        this.oFeature.setModifier(IGeoMilSymbol.Modifier.AZIMUTH, index, Float.NaN);
    }

    @Override
    protected boolean doControlPointMoved(ControlPoint oCP, IGeoPosition dragPosition) {
        int cpIndex = oCP.getCPIndex();
        int cpSubIndex = oCP.getCPSubIndex();

        switch (oCP.getCPType()) {
            case POSITION_CP: {
                // The center was moved.

                // Move control points.
                oCP.getPosition().setLatitude(dragPosition.getLatitude());
                oCP.getPosition().setLongitude(dragPosition.getLongitude());

                // Reposition Range and azimuth CP.
                for (int index = 0; index < this.rangeList.size(); index++) {
                    this.positionRangeAndAzimuths(index);
                }

                this.addUpdateEventData(FeatureEditUpdateTypeEnum.COORDINATE_MOVED, new int[]{0});
                break;
            }
            case RANGE_CP: {
                // A range CP has been moved.
                float leftAzimuth;
                float rightAzimuth;
                float newRange;
                float prevRange = this.getPreviousRangeValue(cpIndex);
                float nextRange = this.getNextRangeValue(cpIndex);
                int azimuthIndex = this.getAzimuthIndexForRange(cpIndex);
                IGeoPosition centerPos = this.getPositions().get(0);
                ControlPoint azimuthCP;

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

                // Get the left and right azimuth value.
                leftAzimuth = this.oFeature.getNumericModifier(IGeoMilSymbol.Modifier.AZIMUTH, azimuthIndex);
                rightAzimuth = this.oFeature.getNumericModifier(IGeoMilSymbol.Modifier.AZIMUTH, azimuthIndex + 1);

                this.positionRangeControlPoint(oCP, newRange, leftAzimuth, rightAzimuth);
                this.addUpdateEventData(IGeoMilSymbol.Modifier.DISTANCE);

                if (!(this.hasMinimumRange && (cpIndex == 0))) {
                    // It is NOT the minimum range CP. So it has azimuth CPs
                    float avgRange = (prevRange + newRange) / 2;
                    // Reposition the left azimuth CP.
                    azimuthCP = this.findControlPoint(ControlPoint.CPTypeEnum.LEFT_AZIMUTH_CP, cpIndex, azimuthIndex);
                    if (null != azimuthCP) {
                        GeoLibrary.computePositionAt(leftAzimuth, avgRange, centerPos, azimuthCP.getPosition());
                    }
                    // Reposition the right azimuth CP.
                    azimuthCP = this.findControlPoint(ControlPoint.CPTypeEnum.RIGHT_AZIMUTH_CP, cpIndex, azimuthIndex + 1);
                    if (null != azimuthCP) {
                        GeoLibrary.computePositionAt(rightAzimuth, avgRange, centerPos, azimuthCP.getPosition());
                    }
                }

                if (this.hasMinimumRange && (cpIndex == 0)) {
                    // This is because a minimum range does not have azimuth. So we -= 2.
                    azimuthIndex -= 2;
                }

                // Now reposition the azimuth CP of the next range if they exits.
                // Get the left azimuth value of the next range.
                leftAzimuth = this.oFeature.getNumericModifier(IGeoMilSymbol.Modifier.AZIMUTH, azimuthIndex + 2);
                if (!Float.isNaN(leftAzimuth)) {
                    float avgRange = (newRange + nextRange) / 2;
                    // Reposition the left azimuth CP.
                    azimuthCP = this.findControlPoint(ControlPoint.CPTypeEnum.LEFT_AZIMUTH_CP, cpIndex + 1, azimuthIndex + 2);
                    if (null != azimuthCP) {
                        GeoLibrary.computePositionAt(leftAzimuth, avgRange, centerPos, azimuthCP.getPosition());
                    }

                    // Now reposition the right azimuth CP.
                    // Get the right azimuth value of the next range.
                    rightAzimuth = this.oFeature.getNumericModifier(IGeoMilSymbol.Modifier.AZIMUTH, azimuthIndex + 3);
                    if (!Float.isNaN(rightAzimuth)) {
                        // Reposition the right azimuth CP.
                        azimuthCP = this.findControlPoint(ControlPoint.CPTypeEnum.RIGHT_AZIMUTH_CP, cpIndex + 1, azimuthIndex + 3);
                        if (null != azimuthCP) {
                            GeoLibrary.computePositionAt(rightAzimuth, avgRange, centerPos, azimuthCP.getPosition());
                        }

                    }
                }

                break;
            }
            case LEFT_AZIMUTH_CP:
            case RIGHT_AZIMUTH_CP:{
                int leftAzimuthIndex = (oCP.getCPType() == ControlPoint.CPTypeEnum.LEFT_AZIMUTH_CP)? cpSubIndex: cpSubIndex - 1;
                ControlPoint rangeCP = this.findControlPoint(ControlPoint.CPTypeEnum.RANGE_CP, cpIndex, -1);
                IGeoPosition centerPos = this.getPositions().get(0);
                float newAzimuth = (float) GeoLibrary.computeBearing(centerPos, dragPosition);
                float range = this.oFeature.getNumericModifier(IGeoMilSymbol.Modifier.DISTANCE, cpIndex);
                float prevRange = this.getPreviousRangeValue(cpIndex);
                float avgRange = (range + prevRange) / 2;

                // Move the azimuth CP.
                GeoLibrary.computePositionAt(newAzimuth, avgRange, centerPos, oCP.getPosition());
                this.setAzimuthValue(cpSubIndex, newAzimuth);

                float leftAzimuth = this.oFeature.getNumericModifier(IGeoMilSymbol.Modifier.AZIMUTH, leftAzimuthIndex);
                float rightAzimuth = this.oFeature.getNumericModifier(IGeoMilSymbol.Modifier.AZIMUTH, leftAzimuthIndex + 1);

                // Reposition the range CP.
                this.positionRangeControlPoint(rangeCP, range, leftAzimuth, rightAzimuth);

                this.addUpdateEventData(IGeoMilSymbol.Modifier.AZIMUTH);
                break;
            }
        }
        return true;
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

    protected List<ControlPoint> doAddControlPoint(IGeoPosition newPosition) {
        IGeoPosition centerPos = this.getPositions().get(0);
        float newRange = (float) GeoLibrary.computeDistanceBetween(centerPos, newPosition);
        int newRangeIndex = this.getIndexForNewRange(newRange);
        int azimuthIndex = this.getAzimuthIndexForRange(newRangeIndex);

        List<ControlPoint> cpList = new ArrayList<>();

        if (this.hasMinimumRange && (newRangeIndex == 0)) {
            // We cant add a range inside the minimum range.
            return cpList;
        }

        // Shift the range indexes for the ranges
        this.changeControlPointIndexes(ControlPoint.CPTypeEnum.RANGE_CP, newRangeIndex, 1);
        // Shift the range indexes for the azimuth
        this.changeControlPointIndexes(ControlPoint.CPTypeEnum.LEFT_AZIMUTH_CP, newRangeIndex, 1);
        this.changeControlPointIndexes(ControlPoint.CPTypeEnum.RIGHT_AZIMUTH_CP, newRangeIndex, 1);

        // Now shift the sub indexes for the azimuths. But there is 2 per range so the delta is 2.
        this.changeControlPointSubIndexes(ControlPoint.CPTypeEnum.LEFT_AZIMUTH_CP, azimuthIndex, 2);
        this.changeControlPointSubIndexes(ControlPoint.CPTypeEnum.RIGHT_AZIMUTH_CP, azimuthIndex, 2);

        // Add the new range value
        this.insertRangeValue(newRangeIndex, newRange);
        // Add new azimuth left and right.
        this.insertAzimuthValue(azimuthIndex, 315);
        this.insertAzimuthValue(azimuthIndex + 1, 45);

        this.createRangeAndAzimuths(newRangeIndex);
        this.addUpdateEventData(IGeoMilSymbol.Modifier.DISTANCE);
        this.addUpdateEventData(IGeoMilSymbol.Modifier.AZIMUTH);

        // Update the CPS for the range before this one if there is one.
        if (newRangeIndex > 0) {
            this.updateControlPointsForRange(newRangeIndex - 1);
        }

        // Update the CPS of the range after this one if there is one.
        if ((newRangeIndex + 1) < this.rangeList.size()) {
            this.updateControlPointsForRange(newRangeIndex + 1);
        }

        return cpList;
    }

    /**
     * This method repositions the control points for the range specified.
     * @param index
     */
    private void updateControlPointsForRange(int index) {
        float leftAzimuth;
        float rightAzimuth;
        ControlPoint controlPoint;
        float prevRange = 0;
        float distanceAzimuthCP;
        int azimuthIndex = this.getAzimuthIndexForRange(index);
        float range = this.oFeature.getNumericModifier(IGeoMilSymbol.Modifier.DISTANCE, index);
        IGeoPosition centerPos = this.getPositions().get(0);

        // Get the left and right azimuth value.
        leftAzimuth = this.oFeature.getNumericModifier(IGeoMilSymbol.Modifier.AZIMUTH, azimuthIndex);
        rightAzimuth = this.oFeature.getNumericModifier(IGeoMilSymbol.Modifier.AZIMUTH, azimuthIndex + 1);

        // Update Range CP
        controlPoint = this.findControlPoint(ControlPoint.CPTypeEnum.RANGE_CP, index, -1);
        this.positionRangeControlPoint(controlPoint, range, leftAzimuth, rightAzimuth);

        if (this.hasMinimumRange && (index == 0)) {
            // It is a minimum range, so we do not add azimuth CPs.
            return;
        }

        if (index > 0) {
            prevRange = this.oFeature.getNumericModifier(IGeoMilSymbol.Modifier.DISTANCE, index - 1);
        }

        distanceAzimuthCP = (range + prevRange) / 2;
        // Update left azimuth CP.
        controlPoint = this.findControlPoint(ControlPoint.CPTypeEnum.LEFT_AZIMUTH_CP, index, azimuthIndex);
        GeoLibrary.computePositionAt(leftAzimuth, distanceAzimuthCP, centerPos, controlPoint.getPosition());

        // Update right azimuth CP.
        controlPoint = this.findControlPoint(ControlPoint.CPTypeEnum.RIGHT_AZIMUTH_CP, index, azimuthIndex + 1);
        GeoLibrary.computePositionAt(rightAzimuth, distanceAzimuthCP, centerPos, controlPoint.getPosition());
    }

    private void updateControlPointPositions() {
        for (int index = 0; index < this.rangeList.size(); index++) {
            this.updateControlPointsForRange(index);
        }
    }

    protected List<ControlPoint>  doDeleteControlPoint(ControlPoint oCP) {
        int cpIndex = oCP.getCPIndex();

        List<ControlPoint> cpList = new ArrayList<>();

        switch (oCP.getCPType()) {
            case RANGE_CP: {
                if (this.rangeList.size() == 1) {
                    // We cant delete the last range.
                    return null;
                } else if (this.hasMinimumRange && (this.rangeList.size() == 2)) {
                    // It has a minimum range. We can't delete the las 2 ranges.
                    return null;
                }

                int azimuthIndex = this.getAzimuthIndexForRange(cpIndex);
                IGeoPosition centerPos = this.getPositions().get(0);

                this.removeRangeValue(cpIndex);
                this.changeControlPointIndexes(ControlPoint.CPTypeEnum.RANGE_CP, cpIndex, -1);
                cpList.add(oCP);

                if (this.hasMinimumRange) {
                    // It has a minimum range.
                    if (cpIndex > 0) {
                        // The Range being deleted is not the minimum range.
                        // We need to delete the azimuths and update the range index of the remaining azimuths.
                        // We also need to update the subIndexes of the remaining azimuths.
                        // The right one first.
                        ControlPoint azimuthCP = this.findControlPoint(ControlPoint.CPTypeEnum.RIGHT_AZIMUTH_CP, cpIndex, azimuthIndex + 1);
                        if (null != azimuthCP) {
                            cpList.add(azimuthCP);
                        }
                        this.removeAzimuthValue(azimuthIndex + 1);

                        // Now the left one.
                        azimuthCP = this.findControlPoint(ControlPoint.CPTypeEnum.LEFT_AZIMUTH_CP, cpIndex, azimuthIndex);
                        if (null != azimuthCP) {
                            cpList.add(azimuthCP);
                        }
                        this.removeAzimuthValue(azimuthIndex);

                        // Update the sub indexes of the remaining azimuth.
                        this.changeControlPointSubIndexes(ControlPoint.CPTypeEnum.RIGHT_AZIMUTH_CP, azimuthIndex + 1, -2);
                        this.changeControlPointSubIndexes(ControlPoint.CPTypeEnum.LEFT_AZIMUTH_CP, azimuthIndex, -2);
                        // Now update the range indexes of the remaining azimuths.
                        this.changeControlPointIndexes(ControlPoint.CPTypeEnum.RIGHT_AZIMUTH_CP, cpIndex, -1);
                        this.changeControlPointIndexes(ControlPoint.CPTypeEnum.LEFT_AZIMUTH_CP, cpIndex, -1);
                    } else {
                        // The minimum range was deleted.
                        this.hasMinimumRange = false;
                        // Now update the range indexes of the remaining azimuths.
                        this.changeControlPointIndexes(ControlPoint.CPTypeEnum.RIGHT_AZIMUTH_CP, cpIndex, -1);
                        this.changeControlPointIndexes(ControlPoint.CPTypeEnum.LEFT_AZIMUTH_CP, cpIndex, -1);
                        // Now we need to reposition the azimuth of the new first range.
                        this.updateControlPointsForRange(0);
                    }
                } else {
                    // It does not have a minimum range. So we need to remove its azimuths.
                    // The right one first.
                    ControlPoint azimuthCP = this.findControlPoint(ControlPoint.CPTypeEnum.RIGHT_AZIMUTH_CP, cpIndex, azimuthIndex + 1);
                    if (null != azimuthCP) {
                        cpList.add(azimuthCP);
                    }
                    this.removeAzimuthValue(azimuthIndex + 1);

                    // Now the left one.
                    azimuthCP = this.findControlPoint(ControlPoint.CPTypeEnum.LEFT_AZIMUTH_CP, cpIndex, azimuthIndex);
                    if (null != azimuthCP) {
                        cpList.add(azimuthCP);
                    }
                    this.removeAzimuthValue(azimuthIndex);

                    // Update the sub indexes of the remaining azimuth.
                    this.changeControlPointSubIndexes(ControlPoint.CPTypeEnum.RIGHT_AZIMUTH_CP, azimuthIndex + 1, -2);
                    this.changeControlPointSubIndexes(ControlPoint.CPTypeEnum.LEFT_AZIMUTH_CP, azimuthIndex, -2);
                    // Update the range indexes of the remaining azimuths.
                    this.changeControlPointIndexes(ControlPoint.CPTypeEnum.RIGHT_AZIMUTH_CP, cpIndex, -1);
                    this.changeControlPointIndexes(ControlPoint.CPTypeEnum.LEFT_AZIMUTH_CP, cpIndex, -1);
                }

                // Reposition the CPs of the range that now has index cpIndex.
                this.updateControlPointsForRange(cpIndex);
                // Reposition the CPs of the range after this one, if there is one.
                if ((cpIndex + 1) < this.rangeList.size()) {
                    this.updateControlPointsForRange(cpIndex + 1);
                }
                break;
            }
            case RIGHT_AZIMUTH_CP:
            case LEFT_AZIMUTH_CP: {
                // Deleting the azimuths of the first range creates a minimum range.
                // Therefore we can only create a minimum range if it does not have one.
                if (this.hasMinimumRange) {
                    return null;
                } else if (this.rangeList.size() == 1) {
                    // To create a minimum range there must be more than 1 range.
                    return null;
                } else if (cpIndex != 0) {
                    // We can only create a minimum range by deleting the azimuth of the first range.
                    // The one being deleted is NOT for the first range.
                    return null;
                }

                int azimuthIndex = this.getAzimuthIndexForRange(cpIndex);

                // Remove the right first.
                ControlPoint azimuthCP = this.findControlPoint(ControlPoint.CPTypeEnum.RIGHT_AZIMUTH_CP, cpIndex, azimuthIndex + 1);
                if (null != azimuthCP) {
                    cpList.add(azimuthCP);
                }
                this.removeAzimuthValue(azimuthIndex + 1);

                // Now the left one..
                azimuthCP = this.findControlPoint(ControlPoint.CPTypeEnum.LEFT_AZIMUTH_CP, cpIndex, azimuthIndex);
                if (null != azimuthCP) {
                    cpList.add(azimuthCP);
                }
                this.removeAzimuthValue(azimuthIndex);
                // Update sub indexes of the remaining azimuth.
                this.changeControlPointSubIndexes(ControlPoint.CPTypeEnum.RIGHT_AZIMUTH_CP, azimuthIndex + 1, -2);
                this.changeControlPointSubIndexes(ControlPoint.CPTypeEnum.LEFT_AZIMUTH_CP, azimuthIndex, -2);

                this.hasMinimumRange = true;

                break;
            }
        }
        return cpList;
    }
}
