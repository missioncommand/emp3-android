package mil.emp3.core.editors.milstd;

import org.cmapi.primitives.GeoPosition;
import org.cmapi.primitives.IGeoPosition;

import java.util.ArrayList;
import java.util.List;

import mil.emp3.api.MilStdSymbol;
import mil.emp3.api.enums.FeatureEditUpdateTypeEnum;
import mil.emp3.api.exceptions.EMP_Exception;
import mil.emp3.api.interfaces.ICamera;
import mil.emp3.api.listeners.IDrawEventListener;
import mil.emp3.api.listeners.IEditEventListener;
import mil.emp3.api.utils.GeoLibrary;
import mil.emp3.core.editors.ControlPoint;
import mil.emp3.core.utils.CoreMilStdUtilities;
import mil.emp3.mapengine.interfaces.IMapInstance;

/**
 * This class implements the editor for all super auto shape draw category.
 * Tactical Graphics/Tasks
 *      Block Task *
 *      Breach
 *      Bypass
 *      Canalize
 *      Clear *
 *      Contain *
 *      Delay *
 *      Disrupt *
 *      Penetrate *
 *      Relief in Place *
 *      Retirement *
 *      Seize *
 *      Withdraw *
 *          Withdraw Under Pressure *
 *
 * Tactical Graphics/Command and Control and General Maneuver
 *      Deception
 *          Dummy (Deception/Decoy)
 *      Defense
 *          Lines
 *              Principal Direction of Fire (PDF)
 *      Offense
 *          Lines
 *              Infiltration Lane
 *          Areas
 *              Attack by Fire Position
 *              Support by Fire Position
 *      Special
 *          Line
 *              Ambush *
 * Tactical Graphics/Mobility-Survivability
 *      Obstacles
 *          Minefields
 *              Minefields Gap
 *          Obstacle Effect
 *              Block *
 *              Turn *
 *              Disrupt Obstacle Effect
 *          Roadblocks, Craters, and Blown Bridges
 *              RCBB Planned *
 *              RCBB Explosives State of Readiness 1 (Safe) *
 *              RCBB Explosives State of Readiness 2 (Armed - but Passable) *
 *              RCBB Roadblock Complete (Executed) *
 *          Trip Wire *
 *     Obstacle Bypass
 *          Obstacle Bypass Difficulty
 *              Bypass Easy
 *              Bypass Difficult
 *              Bypass Impossible
 *          Crossing Site-Water Crossing
 *              Assault Crossing Area
 *              Bridge or Gap
 *              Ford Easy *
 *              Ford Difficult *
 */
public class MilStdDCSuperAutoShapeEditor extends AbstractMilStdMultiPointEditor {
    private final List<ControlPoint> cpList = new ArrayList<>();

    public MilStdDCSuperAutoShapeEditor(IMapInstance map, MilStdSymbol feature, IEditEventListener oEventListener, armyc2.c2sd.renderer.utilities.SymbolDef symDef) throws EMP_Exception {
        super(map, feature, oEventListener, symDef);
        this.initializeEdit();
    }

    public MilStdDCSuperAutoShapeEditor(IMapInstance map, MilStdSymbol feature, IDrawEventListener oEventListener, armyc2.c2sd.renderer.utilities.SymbolDef symDef, boolean newFeature) throws EMP_Exception {
        super(map, feature, oEventListener, symDef, newFeature);
        this.initializeDraw();
    }

    @Override
    protected void prepareForDraw() throws EMP_Exception {

        if (!this.isNewFeature()) {
            // A feature that already exists should have all of its properties set already.
            return;
        }

        // IGeoPosition cameraPos = this.getMapCameraPosition();
        IGeoPosition centerPos = getCenter();   // Get the center of visible area, Camera is not at the center when tilted.
        List<IGeoPosition> posList = this.getPositions();
        // We set the length to 2/6 of the camera altitude.
        // double distance = cameraPos.getAltitude() / 6.0;
        double distance = getReferenceDistance(); // Based on approximate centerWest and centerEast of the visible area.

        if(distance > 0) {
            distance *= .15;
        } else {
            ICamera camera = oClientMap.getCamera();
            distance = camera.getAltitude() / 6.0;
        }

        double dBrng, dBrngInc;
        IGeoPosition pos;

        if (distance > MAXIMUM_DISTANCE) {
            distance = MAXIMUM_DISTANCE;
        }


        switch (this.oFeature.getBasicSymbol()) {
            case CoreMilStdUtilities.CCGM_OFFENCE_SUPPORT_BY_FIRE_POSITION:
            case CoreMilStdUtilities.TASK_RELIEF_IN_PLACE:
            case CoreMilStdUtilities.MS_MINEFIELD_GAP:
                // The user taps (clicks) to add points for these graphics.
                break;
            case CoreMilStdUtilities.TASK_WITHDRAW_UNDER_PRESURE:
            case CoreMilStdUtilities.TASK_WITHDRAW:
            case CoreMilStdUtilities.TASK_RETIREMENT:
            case CoreMilStdUtilities.TASK_DISRUPT:
            case CoreMilStdUtilities.TASK_DELAY: {
                pos = new GeoPosition();
                GeoLibrary.computePositionAt(270.0, distance, centerPos, pos);
                pos.setAltitude(0);
                posList.add(pos);

                pos = new GeoPosition();
                pos.setLatitude(centerPos.getLatitude());
                pos.setLongitude(centerPos.getLongitude());
                pos.setAltitude(0);
                posList.add(pos);

                pos = new GeoPosition();
                GeoLibrary.computePositionAt(0.0, distance, centerPos, pos);
                pos.setAltitude(0);
                posList.add(pos);

                // Now we add event update data.
                this.addUpdateEventData(FeatureEditUpdateTypeEnum.COORDINATE_ADDED, new int[]{0, 1, 2});
                break;
            }
            default: {
                int index = 0;
                dBrng = 240.0;
                dBrngInc = 360.0 / this.getMinPoints();
                int[] intArray = new int[this.getMinPoints()];

                pos = new GeoPosition();
                GeoLibrary.computePositionAt(dBrng, distance, centerPos, pos);
                pos.setAltitude(0);
                posList.add(pos);

                while (posList.size() <  this.getMinPoints()) {
                    dBrng += dBrngInc;
                    pos = new GeoPosition();
                    GeoLibrary.computePositionAt(dBrng, distance, centerPos, pos);
                    pos.setAltitude(0);
                    posList.add(pos);
                }

                for(index = 0; index < this.getMinPoints(); index++) {
                    intArray[index] = index;
                }

                this.addUpdateEventData(FeatureEditUpdateTypeEnum.COORDINATE_ADDED, intArray);
                break;
            }
        }
    }

    @Override
    protected void assembleControlPoints() {
        List<IGeoPosition> posList = this.getPositions();
        int posCnt = posList.size();
        IGeoPosition pos;
        ControlPoint controlPoint;
        String basicSymbolCode = this.oFeature.getBasicSymbol();

        for (int index = 0; index < posCnt; index++) {
            if ((index == 2) && (
                    basicSymbolCode.equals(CoreMilStdUtilities.CCGM_INFILTRATION_LANE) ||
                    basicSymbolCode.equals(CoreMilStdUtilities.MS_OBSTACLES_RCBB_PLANNED) ||
                    basicSymbolCode.equals(CoreMilStdUtilities.MS_OBSTACLES_RCBB_ESR1_SAFE) ||
                    basicSymbolCode.equals(CoreMilStdUtilities.MS_OBSTACLES_RCBB_ESR2_ARMED_PASSABLE) ||
                    basicSymbolCode.equals(CoreMilStdUtilities.MS_OBSTACLES_TRIP_WIRE) ||
                    basicSymbolCode.equals(CoreMilStdUtilities.MS_OBSTACLES_BYPASS_FORD_EASY) ||
                    basicSymbolCode.equals(CoreMilStdUtilities.MS_OBSTACLES_BYPASS_FORD_DIFFICULT) ||
                    basicSymbolCode.equals(CoreMilStdUtilities.MS_OBSTACLES_RCBB_COMPLETE_EXECUTED)
                )) {
                controlPoint = new ControlPoint(ControlPoint.CPTypeEnum.WIDTH_CP, index, -1);
                controlPoint.setPosition(posList.get(index));
            } else {
                controlPoint = new ControlPoint(ControlPoint.CPTypeEnum.POSITION_CP, index, -1);
                controlPoint.setPosition(posList.get(index));
            }
            this.addControlPoint(controlPoint);
        }
    }

    /**
     * This method Updates the position of the CP that was moved while rotating the 3rd CP such that it
     * keeps its relative position.
     * @param movedCPIndex
     * @param pivitCPIndex
     * @param eventPos
     */
    private void moveCPRotateP3(int movedCPIndex, int pivitCPIndex, IGeoPosition eventPos) {
        ControlPoint movedCP = this.findControlPoint(ControlPoint.CPTypeEnum.POSITION_CP, movedCPIndex, -1);
        ControlPoint pivitCP = this.findControlPoint(ControlPoint.CPTypeEnum.POSITION_CP, pivitCPIndex, -1);
        ControlPoint cp1 = (movedCPIndex == 0)? movedCP: pivitCP;
        ControlPoint cp3 = this.findControlPoint(ControlPoint.CPTypeEnum.POSITION_CP, 2, -1);

        if (cp3 == null) {
            // if its not a position its a width CP.
            cp3 = this.findControlPoint(ControlPoint.CPTypeEnum.WIDTH_CP, 2, -1);
        }

        IGeoPosition centerPos = GeoLibrary.midPointBetween(pivitCP.getPosition(), movedCP.getPosition());
        double distanceCenterToP3 = GeoLibrary.computeDistanceBetween(centerPos, cp3.getPosition());
        double bearingP1ToCenter = GeoLibrary.computeBearing(cp1.getPosition(), centerPos);
        double angleP1P2ToP3 = bearingP1ToCenter - GeoLibrary.computeBearing(centerPos, cp3.getPosition());
        IGeoPosition tempPos = movedCP.getPosition();

        // Set the new position of the moved CP.
        tempPos.setLatitude(eventPos.getLatitude());
        tempPos.setLongitude(eventPos.getLongitude());

        // Calculate the new center.
        centerPos = GeoLibrary.midPointBetween(pivitCP.getPosition(), movedCP.getPosition());
        // The bearing from P1 to the new center.
        bearingP1ToCenter = GeoLibrary.computeBearing(cp1.getPosition(), centerPos);
        // Move P3 to its new position.
        GeoLibrary.computePositionAt(bearingP1ToCenter - angleP1P2ToP3, distanceCenterToP3, centerPos, cp3.getPosition());
        cpList.add(movedCP);
        cpList.add(cp3);
        this.addUpdateEventData(FeatureEditUpdateTypeEnum.COORDINATE_MOVED, new int[]{movedCP.getCPIndex(), cp3.getCPIndex()});
    }

    /**
     * This method moves the CP of an ambush. It a PT1 (index = 0) which must remain perpendicular to
     * the line between P2-P3 (indexes 1 and 2) at the center of the line. Therefore if P2 or P3 are
     * moved, P1 it moved maintaining its relative position from the center of the P2-P3 line.
     * @param oCP
     * @param eventPos
     */
    private void moveGraphicP1PerpendicularP2P3AtCenter(ControlPoint oCP, IGeoPosition eventPos) {
        List<IGeoPosition> posList = this.getPositions();
        int cpIndex = oCP.getCPIndex();
        IGeoPosition centerPos;
        double bearingP2ToP3;
        double bearingCenterToP1;
        double bearingCenterToNewPos;
        double distanceCenterToNewPos;
        double distanceCenterToP1;

        if (posList.size() >= this.getMinPoints()) {
            switch (cpIndex) {
                case 0:
                    // Get the center point between PT2 and PT3.
                    centerPos = GeoLibrary.midPointBetween(posList.get(1), posList.get(2));
                    // Get the bearing from the center point and pt1.
                    bearingCenterToP1 = GeoLibrary.computeBearing(centerPos, posList.get(0));
                    bearingCenterToNewPos = GeoLibrary.computeBearing(centerPos, eventPos);
                    // Get the distance from the center point to the new pos.
                    distanceCenterToNewPos = GeoLibrary.computeDistanceBetween(centerPos, eventPos);
                    // Project the CenterPos-newPos line onto the center Pt-Pt1 line;
                    distanceCenterToNewPos = distanceCenterToNewPos * Math.cos(Math.toRadians(bearingCenterToP1 - bearingCenterToNewPos));
                    // Calculate the new position of Pt1.
                    GeoLibrary.computePositionAt(bearingCenterToP1, distanceCenterToNewPos, centerPos, oCP.getPosition());
                    cpList.add(oCP);
                    this.addUpdateEventData(FeatureEditUpdateTypeEnum.COORDINATE_MOVED, new int[]{cpIndex});
                    break;
                case 1:
                case 2:
                    // Get the bearing between pt2 pt3.
                    bearingP2ToP3 = GeoLibrary.computeBearing(posList.get(1), posList.get(2));
                    // Calculate the center pos.
                    centerPos = GeoLibrary.midPointBetween(posList.get(1), posList.get(2));
                    // Get the distance from the mid point to pt1 (the arrow).
                    distanceCenterToP1 = GeoLibrary.computeDistanceBetween(centerPos, posList.get(0));

                    // Return if its NAN, infinity or < 20 meters.
                    if ((distanceCenterToP1 < 20.0) || Double.isNaN(distanceCenterToP1) || Double.isInfinite(distanceCenterToP1)) {
                        return;
                    }

                    // Get the bearing from the center point and pt1.
                    bearingCenterToP1 = GeoLibrary.computeBearing(centerPos, posList.get(0));
                    // Update the coordinates of th CP that was moved.
                    oCP.getPosition().setLatitude(eventPos.getLatitude());
                    oCP.getPosition().setLongitude(eventPos.getLongitude());
                    // Now get the new bearing of pt2 pt3. And the new mid point.
                    double newBearingP2ToP3 = GeoLibrary.computeBearing(posList.get(1), posList.get(2));
                    // The new center pos.
                    centerPos = GeoLibrary.midPointBetween(posList.get(1), posList.get(2));
                    // Compute the new bearing of pt1. And get the coordinates.
                    double newBearingCenterPt1 = bearingCenterToP1 + (newBearingP2ToP3 - bearingP2ToP3);
                    ControlPoint point1CP = this.findControlPoint(ControlPoint.CPTypeEnum.POSITION_CP, 0, -1);
                    GeoLibrary.computePositionAt(newBearingCenterPt1, distanceCenterToP1, centerPos, point1CP.getPosition());

                    cpList.add(oCP);
                    cpList.add(point1CP);
                    this.addUpdateEventData(FeatureEditUpdateTypeEnum.COORDINATE_MOVED, new int[]{cpIndex, point1CP.getCPIndex()});
                    break;
            }
        } else {
            moveCP(oCP, eventPos);
        }
    }

    /**
     * This method moves CP for graphics that require that PT3 is perpendicular to the line P1-P2 at
     * the center of the line. Moving P3 CP maintains it perpendicular to the P1-P2 line. Moving
     * P1 also rotates P3 around P2, and moving P2 also rotates P3 around P1.
     * @param oCP
     * @param eventPos
     */
    private void moveGraphicP3PerpendicularToP1P2Center(ControlPoint oCP, IGeoPosition eventPos) {
        if (this.getPositions().size() >= this.getMinPoints()) {
            switch (oCP.getCPIndex()) {
                case 0: {
                    // PT1 was moved.
                    moveCPRotateP3(0, 1, eventPos);
                    this.addUpdateEventData(FeatureEditUpdateTypeEnum.COORDINATE_MOVED, new int[]{0, 2});
                    break;
                }
                case 1: {
                    // PT2 was moved.
                    moveCPRotateP3(1, 0, eventPos);
                    this.addUpdateEventData(FeatureEditUpdateTypeEnum.COORDINATE_MOVED, new int[]{1, 2});
                    break;
                }
                case 2: {
                    // PT3 was moved.
                    IGeoPosition cp3Pos = oCP.getPosition();
                    ControlPoint cp1 = this.findControlPoint(ControlPoint.CPTypeEnum.POSITION_CP, 0, -1);
                    ControlPoint cp2 = this.findControlPoint(ControlPoint.CPTypeEnum.POSITION_CP, 1, -1);
                    IGeoPosition centerPos = GeoLibrary.midPointBetween(cp1.getPosition(), cp2.getPosition());
                    double bearingCenterToP3 = GeoLibrary.computeBearing(centerPos, cp3Pos);
                    double distanceCenterToNewPos = GeoLibrary.computeDistanceBetween(centerPos, eventPos);
                    double angleP3CenterNewPos = GeoLibrary.computeBearing(centerPos, eventPos) - bearingCenterToP3;
                    double distanceCenterToNewP3Pos = distanceCenterToNewPos * Math.cos(Math.toRadians(angleP3CenterNewPos));

                    // Move the position of P3.
                    GeoLibrary.computePositionAt(bearingCenterToP3, distanceCenterToNewP3Pos, centerPos, cp3Pos);
                    cpList.add(oCP);

                    // Add update data.
                    this.addUpdateEventData(FeatureEditUpdateTypeEnum.COORDINATE_MOVED, new int[]{2});
                    break;
                }
            }
        } else {
            moveCP(oCP, eventPos);
        }
    }

    private void moveGraphicP3PerpendicularP1P2AtP2(ControlPoint oCP, IGeoPosition eventPos) {
        List<IGeoPosition> posList = this.getPositions();
        int cpIndex = oCP.getCPIndex();

        if (posList.size() >= this.getMinPoints()) {
            switch (cpIndex) {
                case 0: {
                    // Find P2 and P3
                    ControlPoint p2CP = this.findControlPoint(ControlPoint.CPTypeEnum.POSITION_CP, 1, -1);
                    ControlPoint p3CP = this.findControlPoint(ControlPoint.CPTypeEnum.POSITION_CP, 2, -1);

                    // Move the CP.
                    oCP.getPosition().setLatitude(eventPos.getLatitude());
                    oCP.getPosition().setLongitude(eventPos.getLongitude());

                    // Get the bearing of CP with P2 which is the pivot.
                    double bearingP1WithPivot = GeoLibrary.computeBearing(p2CP.getPosition(), oCP.getPosition());
                    // Get thedistance of P3 from P2.
                    double distanceP2P3 = GeoLibrary.computeDistanceBetween(p2CP.getPosition(), p3CP.getPosition());
                    // Rotate P3 by bearingP1WithPivot + 90deg.
                    GeoLibrary.computePositionAt(bearingP1WithPivot + 90.0, distanceP2P3, p2CP.getPosition(), p3CP.getPosition());
                    this.addUpdateEventData(FeatureEditUpdateTypeEnum.COORDINATE_MOVED, new int[]{0, 2});
                    cpList.add(oCP);
                    cpList.add(p3CP);
                    break;
                }
                case 1: {
                    int[] intArray = new int[posList.size()];
                    // Get the distance and beraing of motion.
                    double distanceOfMotion = GeoLibrary.computeDistanceBetween(oCP.getPosition(), eventPos);
                    double bearingOfMotion = GeoLibrary.computeBearing(oCP.getPosition(), eventPos);
                    // Find P1 and P3
                    ControlPoint p1CP = this.findControlPoint(ControlPoint.CPTypeEnum.POSITION_CP, 0, -1);
                    ControlPoint p3CP = this.findControlPoint(ControlPoint.CPTypeEnum.POSITION_CP, 2, -1);

                    // Move the CPs.
                    oCP.moveControlPoint(bearingOfMotion, distanceOfMotion);
                    cpList.add(oCP);
                    if (p1CP != null) {
                        p1CP.moveControlPoint(bearingOfMotion, distanceOfMotion);
                        cpList.add(p1CP);
                    }
                    if (p3CP != null) {
                        p3CP.moveControlPoint(bearingOfMotion, distanceOfMotion);
                        cpList.add(p3CP);
                    }

                    // Now we add event update data.
                    for (int index = 0; index < posList.size(); index++) {
                        intArray[index] = index;
                    }
                    this.addUpdateEventData(FeatureEditUpdateTypeEnum.COORDINATE_MOVED, intArray);

                    break;
                }
                case 2: {
                    // Find P1 and P2
                    ControlPoint p1CP = this.findControlPoint(ControlPoint.CPTypeEnum.POSITION_CP, 0, -1);
                    ControlPoint p2CP = this.findControlPoint(ControlPoint.CPTypeEnum.POSITION_CP, 1, -1);

                    // Move the CP.
                    oCP.getPosition().setLatitude(eventPos.getLatitude());
                    oCP.getPosition().setLongitude(eventPos.getLongitude());

                    // Get the bearing of CP with P2 which is the pivot.
                    double bearingPivotP3 = GeoLibrary.computeBearing(p2CP.getPosition(), oCP.getPosition());
                    // Get the distance of P1 from P2.
                    double distanceP2P1 = GeoLibrary.computeDistanceBetween(p2CP.getPosition(), p1CP.getPosition());
                    // Rotate P1 by bearingPivotP3 - 90deg.
                    GeoLibrary.computePositionAt(bearingPivotP3 - 90.0, distanceP2P1, p2CP.getPosition(), p1CP.getPosition());
                    this.addUpdateEventData(FeatureEditUpdateTypeEnum.COORDINATE_MOVED, new int[]{0, 2});
                    cpList.add(oCP);
                    cpList.add(p1CP);
                    break;
                }
            }
        } else {
            moveCP(oCP, eventPos);
        }
    }

    /* unlike the other methods in this class, the points here are numbered P0, P1, P2, P3
        if user moves P0 or P2, move P1 and P3 to make a new rectangle
        if user moves P1 or P3, move P0 and P2 to make a new rectangle
     */

    private void moveGraphicKeepParallel(ControlPoint oCP, IGeoPosition eventPos) {
        List<IGeoPosition> posList = this.getPositions();
        int cpIndex = oCP.getCPIndex();
        // Find all current control points
        ControlPoint p0CP = this.findControlPoint(ControlPoint.CPTypeEnum.POSITION_CP, 0, -1);
        ControlPoint p1CP = this.findControlPoint(ControlPoint.CPTypeEnum.POSITION_CP, 1, -1);
        ControlPoint p2CP = this.findControlPoint(ControlPoint.CPTypeEnum.POSITION_CP, 2, -1);
        ControlPoint p3CP = this.findControlPoint(ControlPoint.CPTypeEnum.POSITION_CP, 3, -1);

        // Move the CP.
        oCP.getPosition().setLatitude(eventPos.getLatitude());
        oCP.getPosition().setLongitude(eventPos.getLongitude());
        if (posList.size() >= this.getMinPoints()) {
            switch (cpIndex) {
                case 0: {

                    // move P1 so P0P1 is perpendicular to P0P3 at P0

                    double bearing = GeoLibrary.computeBearing(oCP.getPosition(), p3CP.getPosition());
                    double distance = GeoLibrary.computeDistanceBetween(oCP.getPosition(), p1CP.getPosition());
                    IGeoPosition newGP = GeoLibrary.computePositionAt(bearing - 90.0, distance, oCP.getPosition());
                    moveCP(p1CP, newGP);

                    // move P2 to keep lines parallel and equal length

                    newGP = GeoLibrary.computePositionAt(bearing - 90.0, distance, p3CP.getPosition());
                    moveCP(p2CP, newGP);
                    break;
                }
                case 1: {

                    // move P0 so P0P1 is perpendicular to P1P2 at P1

                    double bearing = GeoLibrary.computeBearing(oCP.getPosition(), p2CP.getPosition());
                    double distance = GeoLibrary.computeDistanceBetween(oCP.getPosition(), p0CP.getPosition());
                    IGeoPosition newGP = GeoLibrary.computePositionAt(bearing + 90.0, distance, oCP.getPosition());
                    moveCP(p0CP, newGP);

                    // move P3 to keep lines parallel and equal length

                    newGP = GeoLibrary.computePositionAt(bearing + 90.0, distance, p2CP.getPosition());
                    moveCP(p3CP, newGP);
                    break;
                }
                case 2: {

                    // move P3 so P2P3 is perpendicular to P1P2 at P2

                    double bearing = GeoLibrary.computeBearing(oCP.getPosition(), p1CP.getPosition());
                    double distance = GeoLibrary.computeDistanceBetween(oCP.getPosition(), p3CP.getPosition());
                    IGeoPosition newGP = GeoLibrary.computePositionAt(bearing - 90.0, distance, oCP.getPosition());
                    moveCP(p3CP, newGP);

                    // move P0 so P0P1 is parallel to P2P3 and equal length

                    newGP = GeoLibrary.computePositionAt(bearing - 90.0, distance, p1CP.getPosition());
                    moveCP(p0CP, newGP);
                    break;
                }
                case 3: {

                    // move P2 so P2P3 is perpendicular to P0P3 at P3

                    double bearing = GeoLibrary.computeBearing(oCP.getPosition(), p0CP.getPosition());
                    double distance = GeoLibrary.computeDistanceBetween(oCP.getPosition(), p2CP.getPosition());
                    IGeoPosition newGP = GeoLibrary.computePositionAt(bearing + 90.0, distance, oCP.getPosition());
                    moveCP(p2CP, newGP);

                    // move P1 so P0P1 is parallel to P2P3 and equal length

                    newGP = GeoLibrary.computePositionAt(bearing + 90.0, distance, p0CP.getPosition());
                    moveCP(p1CP, newGP);
                    break;
                }

            }
        } else {
            moveCP(oCP, eventPos);
        }
    }

    private void moveCP(ControlPoint oCP, IGeoPosition eventPos) {
        oCP.getPosition().setLatitude(eventPos.getLatitude());
        oCP.getPosition().setLongitude(eventPos.getLongitude());
        cpList.add(oCP);
        this.addUpdateEventData(FeatureEditUpdateTypeEnum.COORDINATE_MOVED, new int[]{oCP.getCPIndex()});
    }

    @Override
    protected List<ControlPoint> doAddControlPoint(IGeoPosition eventPos) {
        String basicSymbolCode = this.oFeature.getBasicSymbol();

        List<ControlPoint> cpList = new ArrayList<>();

        if (this.inEditMode()) {
            // In Edit mode we do not add CP. The user needs to drag new CP.
            return cpList;
        }

        if (this.getPositions().size() < this.getMaxPoints()) {
            ControlPoint controlPoint;
            IGeoPosition pos;
            List<IGeoPosition> posList = this.getPositions();

            controlPoint = new ControlPoint(ControlPoint.CPTypeEnum.POSITION_CP, posList.size(), -1);
            pos = new GeoPosition();
            pos.setLatitude(eventPos.getLatitude());
            pos.setLongitude(eventPos.getLongitude());
            pos.setAltitude(0);
            controlPoint.setPosition(pos);
            cpList.add(controlPoint);
            posList.add(pos);
            this.addUpdateEventData(FeatureEditUpdateTypeEnum.COORDINATE_ADDED, new int[]{controlPoint.getCPIndex()});
        }

        return cpList;
    }

    @Override
    protected boolean doControlPointMoved(ControlPoint oCP, IGeoPosition eventPos) {
        String basicSymbolCode = this.oFeature.getBasicSymbol();

        switch (basicSymbolCode) {
            case CoreMilStdUtilities.AMBUSH:
                moveGraphicP1PerpendicularP2P3AtCenter(oCP, eventPos);
                break;
            case CoreMilStdUtilities.BLOCK_TASK:
            case CoreMilStdUtilities.TASK_CONTAIN:
            case CoreMilStdUtilities.TASK_PENETRATE:
            case CoreMilStdUtilities.TASK_SEIZE:
            case CoreMilStdUtilities.CCGM_INFILTRATION_LANE:
            case CoreMilStdUtilities.MS_OBSTACLES_EFFECT_BLOCK:
            case CoreMilStdUtilities.MS_OBSTACLES_EFFECT_TURN:
            case CoreMilStdUtilities.MS_OBSTACLES_RCBB_PLANNED:
            case CoreMilStdUtilities.MS_OBSTACLES_RCBB_ESR1_SAFE:
            case CoreMilStdUtilities.MS_OBSTACLES_RCBB_ESR2_ARMED_PASSABLE:
                moveGraphicP3PerpendicularToP1P2Center(oCP, eventPos);
                break;
            case CoreMilStdUtilities.MS_OBSTACLES_BYPASS_BRIDGE_OR_GAP:
                switch (this.oFeature.getSymbolStandard()) {
                    case MIL_STD_2525B:
                        moveCP(oCP, eventPos);
                        break;
                    case MIL_STD_2525C:
                        // 2525C requires the two lines to be parallel
                        moveGraphicKeepParallel(oCP, eventPos);
                        break;
                }
                break;
            case CoreMilStdUtilities.TASK_CLEAR: {
                switch (this.oFeature.getSymbolStandard()) {
                    case MIL_STD_2525B:
                        moveCP(oCP, eventPos);
                        break;
                    case MIL_STD_2525C:
                        moveGraphicP3PerpendicularToP1P2Center(oCP, eventPos);
                        break;
                }
                break;
            }
            case CoreMilStdUtilities.TASK_DELAY:
            case CoreMilStdUtilities.TASK_DISRUPT:
            case CoreMilStdUtilities.TASK_RETIREMENT:
            case CoreMilStdUtilities.TASK_WITHDRAW:
            case CoreMilStdUtilities.TASK_WITHDRAW_UNDER_PRESURE:
                moveGraphicP3PerpendicularP1P2AtP2(oCP, eventPos);
                break;
            default:
                moveCP(oCP, eventPos);
                break;
        }

        return true;
    }
}
