package mil.emp3.core.editors;

import android.util.Log;

import org.cmapi.primitives.GeoPosition;
import org.cmapi.primitives.GeoPositionGroup;
import org.cmapi.primitives.GeoStrokeStyle;
import org.cmapi.primitives.IGeoAltitudeMode;
import org.cmapi.primitives.IGeoColor;
import org.cmapi.primitives.IGeoPosition;
import org.cmapi.primitives.IGeoPositionGroup;
import org.cmapi.primitives.IGeoStrokeStyle;

import java.util.List;

import mil.emp3.api.Path;
import mil.emp3.api.enums.EditorMode;
import mil.emp3.api.enums.MapFreehandEventEnum;
import mil.emp3.api.enums.MapMotionLockEnum;
import mil.emp3.api.enums.UserInteractionEventEnum;
import mil.emp3.api.exceptions.EMP_Exception;
import mil.emp3.api.listeners.IFreehandEventListener;
import mil.emp3.api.utils.EmpGeoColor;
import mil.emp3.api.utils.UUIDSet;
import mil.emp3.core.events.MapFreehandEvent;
import mil.emp3.core.storage.MapStatus;
import mil.emp3.mapengine.api.FeatureVisibility;
import mil.emp3.mapengine.api.FeatureVisibilityList;
import mil.emp3.mapengine.events.MapInstanceFeatureUserInteractionEvent;
import mil.emp3.mapengine.events.MapInstanceUserInteractionEvent;

/**
 * This class implements the freehand drawing editor. It handles DRAG and DRAG_COMPLETE events to
 * build a list of positions for the line (Path) object. Then the map is placed in freehand draw mode:
 * 1) The map is set to smart lock mode.
 * 2) We listen for map and feature user interaction DRAG and DRAG_COMPLETE events.
 * 3) When the 2nd drag event occurs the draw start event is generated. And the line is drawn on the map
 * in the style indicated.
 * 4) When additional drag event are received we generate drag update eevents and the line feature is
 * updated on the map while the positions are accumulated.
 * 5) When the DRAG_COMPLETE is received we issue the line draw end.
 * 6) Once the line draw end is sent the position list is emptied and the process is allowed to start over.
 * 7) When the complete is received, the editor exits freehand draw mode.
 */
public class FreehandDrawEditor extends AbstractEditor {
    private final static String TAG = FreehandDrawEditor.class.getSimpleName();

    private final double    DEFAULT_STROKE_WIDTH    = 4.0;
    private final double    DEFAULT_ALPHA           = 1.0;
    private final int       DEFAULT_RED             = 255;
    private final int       DEFAULT_GREEN           = 255;
    private final int       DEFAULT_BLUE            = 255;

    // We keep a stroke style and copy values to it.
    private final IGeoStrokeStyle drawStrokeStyle   = new GeoStrokeStyle();
    // This is the color object for the stroke style.
    private final IGeoColor drawColor               = new EmpGeoColor(DEFAULT_ALPHA, DEFAULT_RED, DEFAULT_GREEN, DEFAULT_BLUE);
    private final IFreehandEventListener clientEventListener;
    // The list of position for the line.
    private final IGeoPositionGroup positionGroup   = new GeoPositionGroup();
    // The Path that will be used to draw.
    private final Path lineDrawObject;
    private final List<IGeoPosition> positionList = this.positionGroup.getPositions();
    private MapStatus mapStatus;

    // This is set to true on cancel, complete or exit of the editor.
    boolean finishing = false;

    public FreehandDrawEditor(MapStatus mapStatus, IGeoStrokeStyle initialStyle, IFreehandEventListener listener) throws EMP_Exception {
        super(mapStatus.getMapInstance(), new Path(), EditorMode.FREEHAND_MODE);

        this.mapStatus = mapStatus;

        this.lineDrawObject = (Path) this.oFeature;
        this.lineDrawObject.setAltitudeMode(IGeoAltitudeMode.AltitudeMode.CLAMP_TO_GROUND);
        this.lineDrawObject.getPositions().clear();
        this.lineDrawObject.getPositions().addAll(this.positionGroup.getPositions());
        this.clientEventListener = listener;
        this.drawStrokeStyle.setStrokeColor(this.drawColor);
        this.positionGroup.setAltitudeMode(IGeoAltitudeMode.AltitudeMode.CLAMP_TO_GROUND);

        // If the initial style is not provided we use the default values, else we copy the values.
        if (initialStyle == null) {
            this.drawStrokeStyle.setStrokeWidth(DEFAULT_STROKE_WIDTH);
        } else {
            this.copyStyle(initialStyle);
        }

        this.lineDrawObject.setStrokeStyle(this.drawStrokeStyle);
        this.issueDrawEvent(MapFreehandEventEnum.MAP_ENTERED_FREEHAND_DRAW_MODE);
    }

    public void setFreehandDrawStyle(IGeoStrokeStyle style) {
        this.copyStyle(style);
        this.lineDrawObject.setStrokeStyle(this.drawStrokeStyle);
    }

    private void copyStyle(IGeoStrokeStyle style) {
        if (style != null) {
            if (style.getStrokeWidth() != Double.NaN) {
                this.drawStrokeStyle.setStrokeWidth(style.getStrokeWidth());
            } else {
                this.drawStrokeStyle.setStrokeWidth(DEFAULT_STROKE_WIDTH);
            }

            this.drawStrokeStyle.setStipplingFactor(style.getStipplingFactor());
            this.drawStrokeStyle.setStipplingPattern(style.getStipplingPattern());

            if (style.getStrokeColor() != null) {
                if (style.getStrokeColor().getAlpha() != Double.NaN) {
                    this.drawColor.setAlpha(style.getStrokeColor().getAlpha());
                } else {
                    this.drawColor.setAlpha(DEFAULT_ALPHA);
                }
                if (style.getStrokeColor().getRed() != Double.NaN) {
                    this.drawColor.setRed(style.getStrokeColor().getRed());
                } else {
                    this.drawColor.setRed(DEFAULT_RED);
                }
                if (style.getStrokeColor().getGreen() != Double.NaN) {
                    this.drawColor.setGreen(style.getStrokeColor().getGreen());
                } else {
                    this.drawColor.setGreen(DEFAULT_GREEN);
                }
                if (style.getStrokeColor().getBlue() != Double.NaN) {
                    this.drawColor.setBlue(style.getStrokeColor().getBlue());
                } else {
                    this.drawColor.setBlue(DEFAULT_BLUE);
                }
            }
        }
    }

    /**
     * The freehand drawing process only handles the drag and drag complete event to generate the list of
     * positions. If operates the same if the drag was initiated on a feature or the map.
     * @param eEvent
     * @param eventPosition
     * @return
     */
    private boolean handleEvent(UserInteractionEventEnum eEvent, IGeoPosition eventPosition) {
        boolean bRet = false;
        MapFreehandEventEnum eFreehandEvent;
        IGeoPosition newPos;

        switch (eEvent) {
            case CLICKED:
            case DOUBLE_CLICKED:
            case LONG_PRESS:
                break;

            case DRAG:
                if(null == eventPosition) {
                    // If Camera is tilted and user reaches the horizon during free draw then eventPosition will be null.
                    // We should ignore this event but treat it as if it was handled.
                    bRet = true;
                    break;
                }
                newPos = new GeoPosition();

                newPos.setLatitude(eventPosition.getLatitude());
                newPos.setLongitude(eventPosition.getLongitude());
                newPos.setAltitude(0.0);

                if (this.positionList.size() < 2) {
                    // If there is less than 2 positions we hav not generated the start event yet.
                    eFreehandEvent = MapFreehandEventEnum.MAP_FREEHAND_LINE_DRAW_START;
                } else {
                    // There is 2 or more position in the list. We issue an update.
                    eFreehandEvent = MapFreehandEventEnum.MAP_FREEHAND_LINE_DRAW_UPDATE;
                }
                this.positionList.add(newPos);
                bRet = true;

                // We cant generate an event until there is at least 2 positions on the list.
                if (this.positionList.size() > 1) {
                    this.issueDrawEvent(eFreehandEvent);
                }
                break;

            case DRAG_COMPLETE:
                if (this.positionList.size() > 1) {
                    this.issueDrawEvent(MapFreehandEventEnum.MAP_FREEHAND_LINE_DRAW_END);
                }
                this.positionList.clear();
                bRet = true;
                break;

            default:
                Log.e(TAG, "handleEvent received unsupported event " + eEvent);
        }
        return bRet;
    }

    @Override
    public boolean onEvent(MapInstanceFeatureUserInteractionEvent oEvent) {
        oEvent.setEventConsumed(this.handleEvent(oEvent.getEvent(), oEvent.getCoordinate()));
        return oEvent.isEventConsumed();
    }

    @Override
    public boolean onEvent(MapInstanceUserInteractionEvent oEvent) {
        oEvent.setEventConsumed(this.handleEvent(oEvent.getEvent(), oEvent.getCoordinate()));
        return oEvent.isEventConsumed();
    }

    @Override
    public void Complete() {
        setFinishing(true);
        this.issueDrawEvent(MapFreehandEventEnum.MAP_EXIT_FREEHAND_DRAW_MODE);
    }

    /**
     * This method issues the events to the event manager for those that have register on the map for freehand
     * draw event and to the listener provided in the call to freehandDraw if one was provided.
     * @param eEvent
     */
    private void issueDrawEvent(MapFreehandEventEnum eEvent) {
        FeatureVisibilityList oList;
        FeatureVisibility oItem;

        switch (eEvent) {
            case MAP_ENTERED_FREEHAND_DRAW_MODE:
                // Upon startup set the map to smart lock.
                mapStatus.setLockMode_(MapMotionLockEnum.SMART_LOCK);
                break;
            case MAP_FREEHAND_LINE_DRAW_START:
            case MAP_FREEHAND_LINE_DRAW_UPDATE:
                // The map does not render lines yet but it will soon.
                // Here we add/update the line feature on the map.
                this.lineDrawObject.getPositions().clear();
                this.lineDrawObject.getPositions().addAll(positionList);

                oList = new FeatureVisibilityList();
                oItem = new FeatureVisibility(this.lineDrawObject, true);
                oList.add(oItem);
                mapInstance.addFeatures(oList, null);
                break;
            case MAP_FREEHAND_LINE_DRAW_END: {
                // Here we remove the line feature from the map.
                UUIDSet removeSet = new UUIDSet();
                removeSet.add(this.lineDrawObject.getGeoId());
                this.mapInstance.removeFeatures(removeSet, null);
                break;
            }
            case MAP_EXIT_FREEHAND_DRAW_MODE:
                break;
        }

        if (this.clientEventListener != null) {
            try {
                switch (eEvent) {
                    case MAP_ENTERED_FREEHAND_DRAW_MODE:
                        this.clientEventListener.onEnterFreeHandDrawMode(this.oClientMap);
                        break;
                    case MAP_FREEHAND_LINE_DRAW_START:
                        this.clientEventListener.onFreeHandLineDrawStart(this.oClientMap, this.positionGroup);
                        break;
                    case MAP_FREEHAND_LINE_DRAW_UPDATE:
                        this.clientEventListener.onFreeHandLineDrawUpdate(this.oClientMap, this.positionGroup);
                        break;
                    case MAP_FREEHAND_LINE_DRAW_END:
                        this.clientEventListener.onFreeHandLineDrawEnd(this.oClientMap, this.drawStrokeStyle, this.positionGroup);
                        break;
                    case MAP_EXIT_FREEHAND_DRAW_MODE:
                        this.clientEventListener.onExitFreeHandDrawMode(this.oClientMap);
                        break;
                }
            } catch(Exception ex) {
                Log.e(TAG, "issueDrawEvent", ex);
            }
        }

        MapFreehandEvent event = new MapFreehandEvent(eEvent, this.oClientMap, this.drawStrokeStyle, this.positionGroup);
        eventManager.generateFreehandDrawEvent(event);

        if (eEvent == MapFreehandEventEnum.MAP_EXIT_FREEHAND_DRAW_MODE) {
            // Upon exit unlock the map.
            mapStatus.setLockMode_(MapMotionLockEnum.UNLOCKED);
        }
    }

    @Override
    public boolean isFinishing() {
        return finishing;
    }

    private void setFinishing(boolean finishing) {
        this.finishing = finishing;
    }
}
