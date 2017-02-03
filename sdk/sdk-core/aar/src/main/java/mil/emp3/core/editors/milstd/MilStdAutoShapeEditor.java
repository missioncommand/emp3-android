package mil.emp3.core.editors.milstd;

import org.cmapi.primitives.GeoPosition;
import org.cmapi.primitives.IGeoPosition;

import mil.emp3.api.MilStdSymbol;
import mil.emp3.api.enums.FeatureEditUpdateTypeEnum;
import mil.emp3.api.exceptions.EMP_Exception;
import mil.emp3.api.listeners.IDrawEventListener;
import mil.emp3.api.listeners.IEditEventListener;
import mil.emp3.api.utils.GeoLibrary;
import mil.emp3.core.editors.ControlPoint;
import mil.emp3.core.utils.CoreMilStdUtilities;
import mil.emp3.mapengine.interfaces.IMapInstance;

/**
 * This class implements the editor for auto shape multi point TG.
 *
 * Tactical Graphics
 *      Tasks
 *          Isolate *
 *          Occupy *
 *          Retain *
 *          Secure *
 *          Security
 *              Screen
 *              Guard
 *              Cover
 *          Cordon and Search *
 *          Cordon and Knock *
 *      Command and Control and General Maneuver
 *          General
 *              Areas
 *                  Search Area
 *                      Reconnaissance Area
 *      Mobility-Survivability
 *          Obstacles
 *              Mines
 *                  Mine Cluster
 *          Obstacle Bypass
 *              Crossing Site-Water Crossing
 *                  Lane
 *          Nuclear, Biological, and Chemical
 *              Minimum Safe Distance Zones *
 *
 */
public class MilStdAutoShapeEditor extends AbstractMilStdMultiPointEditor {
    private java.util.List<ControlPoint> cpList = new java.util.ArrayList<>();

    public MilStdAutoShapeEditor(IMapInstance map, MilStdSymbol feature, IEditEventListener oEventListener, armyc2.c2sd.renderer.utilities.SymbolDef symDef) throws EMP_Exception {
        super(map, feature, oEventListener, symDef);
        this.initializeEdit();
    }

    public MilStdAutoShapeEditor(IMapInstance map, MilStdSymbol feature, IDrawEventListener oEventListener, armyc2.c2sd.renderer.utilities.SymbolDef symDef) throws EMP_Exception {
        super(map, feature, oEventListener, symDef);
        initializeDraw();
    }

    @Override
    protected void prepareForDraw() throws EMP_Exception {
        IGeoPosition cameraPos = this.getMapCameraPosition();
        java.util.List<IGeoPosition> posList = this.getPositions();
        // We set the radius to 2/5 of the camera altitude.
        double distance = cameraPos.getAltitude() / 5.0;
        IGeoPosition pos;

        if (distance > 2609340.0) {
            // If its to large set it to 1000 miles which makes the segment 2000 miles long.
            distance = 2609340.0;
        }

        // For drawing we remove all positions and start from scratch.
        posList.clear();

        switch (this.symbol.getBasicSymbol()) {
            case CoreMilStdUtilities.TASK_CORDON_SEARCH:
            case CoreMilStdUtilities.TASK_CORDON_KNOCK:
            case CoreMilStdUtilities.TASK_ISOLATE:
            case CoreMilStdUtilities.TASK_OCCUPY:
            case CoreMilStdUtilities.TASK_RETIAN:
            case CoreMilStdUtilities.TASK_SECURE: {
                // 0 Positions. Create all.
                pos = new GeoPosition();

                // The first point is in the center.
                pos.setAltitude(0);
                pos.setLatitude(cameraPos.getLatitude());
                pos.setLongitude(cameraPos.getLongitude());
                posList.add(pos);

                // The 2nd position is at the distance at 270 bearing.
                pos = new GeoPosition();
                GeoLibrary.computePositionAt(270.0, distance, cameraPos, pos);
                pos.setAltitude(0);
                posList.add(pos);

                this.addUpdateEventData(FeatureEditUpdateTypeEnum.COORDINATE_ADDED, new int[]{0, 1});
                break;
            }
            case CoreMilStdUtilities.MS_NBC_MINIMUM_SAFE_DISTANCE_ZONES: {
                double delta = Math.rint(cameraPos.getAltitude() / 10.0);

                distance = delta;

                pos = new GeoPosition();
                pos.setLatitude(cameraPos.getLatitude());
                pos.setLongitude(cameraPos.getLongitude());
                pos.setAltitude(0);
                posList.add(pos);

                for (int index = 0; index < 3; index++) {
                    pos = new GeoPosition();
                    GeoLibrary.computePositionAt(90.0, distance, cameraPos, pos);
                    pos.setAltitude(0);
                    posList.add(pos);
                    distance += delta;
                }
                break;
            }
            default:
                // All others get their position by tapping.
                break;
        }

    }

    @Override
    protected void assembleControlPoints() {
        java.util.List<IGeoPosition> posList = this.getPositions();
        int posCnt = posList.size();
        IGeoPosition pos;
        ControlPoint controlPoint;

        switch (this.symbol.getBasicSymbol()) {
            case CoreMilStdUtilities.TASK_CORDON_SEARCH:
            case CoreMilStdUtilities.TASK_CORDON_KNOCK:
            case CoreMilStdUtilities.TASK_ISOLATE:
            case CoreMilStdUtilities.TASK_OCCUPY:
            case CoreMilStdUtilities.TASK_RETIAN:
            case CoreMilStdUtilities.TASK_SECURE:
                for (int index = 0; index < posCnt; index++) {
                    if (index == 1) {
                        controlPoint = new ControlPoint(ControlPoint.CPTypeEnum.RADIUS_CP, index, -1);
                    } else {
                        controlPoint = new ControlPoint(ControlPoint.CPTypeEnum.POSITION_CP, index, -1);
                    }
                    controlPoint.setPosition(posList.get(index));
                    this.addControlPoint(controlPoint);
                }
                break;
            case CoreMilStdUtilities.MS_NBC_MINIMUM_SAFE_DISTANCE_ZONES: {
                for (int index = 0; index < posCnt; index++) {
                    if (index == 0) {
                        controlPoint = new ControlPoint(ControlPoint.CPTypeEnum.POSITION_CP, index, -1);
                    } else {
                        controlPoint = new ControlPoint(ControlPoint.CPTypeEnum.RADIUS_CP, index, -1);
                    }
                    controlPoint.setPosition(posList.get(index));
                    this.addControlPoint(controlPoint);
                }
                break;
            }
            default:
                for (int index = 0; index < posCnt; index++) {
                    controlPoint = new ControlPoint(ControlPoint.CPTypeEnum.POSITION_CP, index, -1);
                    controlPoint.setPosition(posList.get(index));
                    this.addControlPoint(controlPoint);
                }
                break;
        }
    }

    @Override
    protected java.util.List<ControlPoint> doAddControlPoint(IGeoPosition eventPos) {
        java.util.List<IGeoPosition> posList = this.getPositions();

        this.cpList.clear();

        if (posList.size() < this.getMaxPoints()) {
            IGeoPosition pos = new GeoPosition();
            ControlPoint controlPoint = new ControlPoint(ControlPoint.CPTypeEnum.POSITION_CP, posList.size(), -1);

            pos.setLatitude(eventPos.getLatitude());
            pos.setLongitude(eventPos.getLongitude());
            pos.setAltitude(0);
            controlPoint.setPosition(pos);
            this.cpList.add(controlPoint);
            posList.add(pos);
            this.addUpdateEventData(FeatureEditUpdateTypeEnum.COORDINATE_ADDED, new int[]{controlPoint.getCPIndex()});
        }
        return this.cpList;
    }

    private void moveCircleCP(ControlPoint controlPoint, IGeoPosition eventPos) {

        switch (controlPoint.getCPIndex()) {
            case 0: {
                // The center was moved. We also have to move the 2nd position.
                IGeoPosition pos = controlPoint.getPosition();
                ControlPoint cp2 = this.findControlPoint(ControlPoint.CPTypeEnum.POSITION_CP, 1, -1);
                double distance = GeoLibrary.computeDistanceBetween(pos, eventPos);
                double bearing = GeoLibrary.computeBearing(pos, eventPos);

                // Move center position.
                pos.setLatitude(eventPos.getLatitude());
                pos.setLongitude(eventPos.getLongitude());

                if (cp2 == null) {
                    cp2 = this.findControlPoint(ControlPoint.CPTypeEnum.RADIUS_CP, 1, -1);
                    if (cp2 == null) {
                        // Now move the 2nd position.
                        cp2.moveControlPoint(bearing, distance);
                        cpList.add(cp2);
                    }
                } else {
                    // Now move the 2nd position.
                    cp2.moveControlPoint(bearing, distance);
                    cpList.add(cp2);
                }

                cpList.add(controlPoint);

                // Add update data.
                this.addUpdateEventData(FeatureEditUpdateTypeEnum.COORDINATE_MOVED, new int[]{0,1});
                break;
            }
            case 1: {
                IGeoPosition pos = controlPoint.getPosition();

                // Move the position.
                pos.setLatitude(eventPos.getLatitude());
                pos.setLongitude(eventPos.getLongitude());
                cpList.add(controlPoint);

                // Add update data.
                this.addUpdateEventData(FeatureEditUpdateTypeEnum.COORDINATE_MOVED, new int[]{1});
                break;
            }
        }
    }

    private void moveConcentricCircle(ControlPoint controlPoint, IGeoPosition eventPos) {
        java.util.List<IGeoPosition> posList = this.getPositions();
        int[] intList = new int[posList.size()];
        ControlPoint tempCP;

        switch (controlPoint.getCPIndex()) {
            case 0: {
                double distanceOfMotion = GeoLibrary.computeDistanceBetween(controlPoint.getPosition(), eventPos);
                double bearingOfMotion = GeoLibrary.computeBearing(controlPoint.getPosition(), eventPos);

                controlPoint.getPosition().setLatitude(eventPos.getLatitude());
                controlPoint.getPosition().setLongitude(eventPos.getLongitude());
                intList[0] = 0;
                this.cpList.add(controlPoint);

                for (int index = 1; index < posList.size(); index++) {
                    tempCP = this.findControlPoint(ControlPoint.CPTypeEnum.RADIUS_CP, index, -1);
                    if (tempCP != null) {
                        tempCP.moveControlPoint(bearingOfMotion, distanceOfMotion);
                        intList[index] = index;
                        this.cpList.add(tempCP);
                    }
                }
                this.addUpdateEventData(FeatureEditUpdateTypeEnum.COORDINATE_MOVED, intList);

                break;
            }
            default: {
                double previousRange = 0;
                double nextRange = Double.POSITIVE_INFINITY;
                double newRange = GeoLibrary.computeDistanceBetween(posList.get(0), eventPos);
                int index = controlPoint.getCPIndex();
                int previousIndex = (index + posList.size() - 1) % posList.size();
                int nextIndex = (index + 1) % posList.size();

                if ((previousIndex > 0) && (previousIndex < index)) {
                    // There is a prior range.
                    previousRange = GeoLibrary.computeDistanceBetween(posList.get(0), posList.get(previousIndex));
                }

                if (nextIndex > index) {
                    // There is a next range.
                    nextRange = GeoLibrary.computeDistanceBetween(posList.get(0), posList.get(nextIndex));
                }

                if ((previousRange + 20) < newRange) {
                    // The new range must be greater then the previous range + X
                    if (newRange < (nextRange - 20)) {
                        // The new range must be smaller than the next range - X.
                        GeoLibrary.computePositionAt(90.0, newRange, posList.get(0), controlPoint.getPosition());
                        this.cpList.add(controlPoint);
                        this.addUpdateEventData(FeatureEditUpdateTypeEnum.COORDINATE_MOVED, new int[]{index});
                    }
                }
                break;
            }
        }
    }

    @Override
    protected java.util.List<ControlPoint> doControlPointMoved(ControlPoint oCP, IGeoPosition eventPos) {
        this.cpList.clear();

        switch (this.symbol.getBasicSymbol()) {
            case CoreMilStdUtilities.TASK_ISOLATE:
            case CoreMilStdUtilities.TASK_OCCUPY:
            case CoreMilStdUtilities.TASK_RETIAN:
            case CoreMilStdUtilities.TASK_SECURE:
            case CoreMilStdUtilities.TASK_CORDON_SEARCH:
            case CoreMilStdUtilities.TASK_CORDON_KNOCK:
                this.moveCircleCP(oCP, eventPos);
                break;
            case CoreMilStdUtilities.MS_NBC_MINIMUM_SAFE_DISTANCE_ZONES:
                this.moveConcentricCircle(oCP, eventPos);
                break;
            default: {
                IGeoPosition pos = oCP.getPosition();

                // Move the position.
                pos.setLatitude(eventPos.getLatitude());
                pos.setLongitude(eventPos.getLongitude());
                cpList.add(oCP);

                // Add update data.
                this.addUpdateEventData(FeatureEditUpdateTypeEnum.COORDINATE_MOVED, new int[]{oCP.getCPIndex()});
                break;
            }
        }

        return this.cpList;
    }
}
