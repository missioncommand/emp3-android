package mil.emp3.core.editors;

import android.util.Log;

import org.cmapi.primitives.GeoPosition;
import org.cmapi.primitives.IGeoMilSymbol;
import org.cmapi.primitives.IGeoPosition;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import mil.emp3.api.enums.EditorMode;
import mil.emp3.api.enums.FeatureDrawEventEnum;
import mil.emp3.api.enums.FeatureEditEventEnum;
import mil.emp3.api.enums.FeatureEditUpdateTypeEnum;
import mil.emp3.api.enums.FeaturePropertyChangedEnum;
import mil.emp3.api.exceptions.EMP_Exception;
import mil.emp3.api.interfaces.IEditUpdateData;
import mil.emp3.api.interfaces.IFeature;
import mil.emp3.api.interfaces.IUUIDSet;
import mil.emp3.api.interfaces.core.IStorageManager;
import mil.emp3.api.listeners.IDrawEventListener;
import mil.emp3.api.listeners.IEditEventListener;
import mil.emp3.api.utils.ManagerFactory;
import mil.emp3.api.utils.UUIDSet;
import mil.emp3.core.events.EditUpdateData;
import mil.emp3.core.events.FeatureDrawEvent;
import mil.emp3.core.events.FeatureEditEvent;
import mil.emp3.api.utils.GeographicLib;
import mil.emp3.mapengine.api.FeatureVisibility;
import mil.emp3.mapengine.api.FeatureVisibilityList;
import mil.emp3.mapengine.events.MapInstanceFeatureUserInteractionEvent;
import mil.emp3.mapengine.events.MapInstanceUserInteractionEvent;
import mil.emp3.mapengine.interfaces.IMapInstance;

/**
 * This class implements the base class of all draw/edit editors. An editor allows the user to change the position
 * and/or shape of a feature while in edit mode or set the position and shape of a feature in draw mode.
 */
public abstract class AbstractDrawEditEditor<T extends IFeature> extends AbstractEditor<T> {
    private final static String TAG = AbstractDrawEditEditor.class.getSimpleName();

    final protected IStorageManager storageManager = ManagerFactory.getInstance().getStorageManager();

    public enum EditorStateEnum {
        IDLE,
        ADDING_CP,
        DELETE_CP,
        DRAGGING_CP,
        DRAGGING_FEATURE
    }

    private EditorStateEnum eEditorState = EditorStateEnum.IDLE;
    private final boolean bUsesControlPoints;
    private final ArrayList<ControlPoint> oControlPointList = new ArrayList<>();
    private final List<IGeoPosition> oOrgPosList = new ArrayList<>();
    private IEditEventListener oEditEventListener = null;
    private IDrawEventListener oDrawEventListener = null;
    // This list is pre-allocated for the use through out.
    protected final List<IEditUpdateData> oUpdateList = new ArrayList<>();

    // This is set to true on cancel, complete or exit of the editor.
    boolean finishing = false;

    /**
     * This constructor initializes the base class to edit a feature that is already on the map.
     *
     * The edit process is as follows.
     *
     * 1) - This base class creates a copy of the original coordinate list which will be restore to the feature
     * in the event the edit is canceled.
     *
     * 2) - This base class calls the prepareForEdit to allow the derived class to perform additional
     * initialization. The derived class can override the prepareForEdit method.
     *
     * 3) - If the derived class indicated that it uses control points (bUsesCP == true) this base class
     * calls the assembleControlPoints method which the derived class must override to create the control
     * points required.
     *
     * 4) - This base class generates a FeatureEditEventEnum.EDIT_START event to indicate that the edit
     * has started.
     *
     * 5) - If the derived class uses control points and the user clicks (taps) on the map this class
     * calls the doAddControlPoint allowing the derived class to handle the add. Map clicks (taps) must
     * not be used to add coordinates to a features position list while in edit mode. The derived class
     * must return the control point added or null if it does not. If the control point is added the
     * base class will generate a FeatureEditEventEnum.EDIT_UPDATE event indicating the addition and
     * add the control point to the map.
     *
     * 6) - If the user double clicks (double taps) on a control point, the base class calls doDeleteControlPoint.
     * The derived class must return true if the control point is to be removed, false otherwise.
     * If the control point is removed the base class generates a FeatureEditEventEnum.EDIT_UPDATE
     * event indicating the deletion, remove the control point from the map and updates the feature on the map.
     *
     * 7) - If the user drags a control point the base class calls doControlPointMoved. The derived class
     * must return a list containing all of the control points that are affected by
     * the action. The base class updates the control points and the feature on the map, and generate a FeatureEditEventEnum.EDIT_UPDATE
     * event.
     *
     * 8) - If the user drags on the feature (not a control point) the base class calls doFeatureMove.
     * The base class generates a FeatureEditEventEnum.EDIT_UPDATE and updates the control points and feature
     * on the map.
     *
     * 9) - If the edit is canceled, the features original coordinates are restored, the control points (if any)
     * are removed from the map. The feature is updated on the map. And the base class generates
     * a FeatureEditEventEnum.EDIT_CANCELED event with the feature in its original form. Then the
     * editor is deleted.
     *
     * 10) - If the edit is completed the control points are removed from the map and the base class generates
     * a FeatureEditEventEnum.EDIT_COMPLETE event and the editor is deleted.
     *
     * @param map The map on which the edit will take place.
     * @param feature The feature that is to edited.
     * @param oEventListener An event listener for the edit events.
     * @param bUsesCP If true the feature requires the use of control points during the edit process.
     * @throws EMP_Exception
     */
    protected AbstractDrawEditEditor(IMapInstance map, T feature, IEditEventListener oEventListener, boolean bUsesCP) throws EMP_Exception {
        super(map, feature, EditorMode.EDIT_MODE, false);

        this.bUsesControlPoints = bUsesCP;
        this.oEditEventListener = oEventListener;
    }

    protected void initializeEdit() throws EMP_Exception {
        IGeoPosition oNewPos;

        // The following makes a copy of the feature coordinate in
        // case the edit is canceled.
        for (IGeoPosition oPos : this.oFeature.getPositions()) {
            oNewPos = new GeoPosition();
            oNewPos.setLatitude(oPos.getLatitude());
            oNewPos.setLongitude(oPos.getLongitude());
            oNewPos.setAltitude(oPos.getAltitude());
            this.oOrgPosList.add(oNewPos);
        }

        this.prepareForEdit();

        if (this.bUsesControlPoints) {
            this.assembleControlPoints();
            this.updateControlPointOnMap();
        }
        this.issueStartEvent();
        this.issueUpdateEvent();
    }

    /**
     * This constructor initializes the base class for a draw operation. A feature that already exists can
     * be placed into draw mode. A derived class can use the isNewFeature() method to determine if the feature
     * being drawn is an existing or a new feature.
     *
     * The draw operation is as follows.
     *
     * 1) - If the feature being drawn already exists, a copy of all its properties is stored, and are
     * reapplied to the feature in the event the draw is canceled.
     *
     * 2) - The base class calls the prepareForDraw. The derived class MUST ensure that the feature has the
     * minimum set of property values required to be rendered.
     *
     * 3) - The base class adds the feature to the map via the storage manager if its not on the map.
     *
     * 4) - If the feature requires control points the assembleControlPoints is called. The derived
     * class must create the minimum number of control points needed to properly draw the feature on
     * the map. The base class adds the control points to the map.
     *
     * 5) - The base class issues a FeatureDrawEventEnum.DRAW_START event.
     *
     * 6) - If the derived class uses control points and the user clicks (taps) on the map this class
     * calls the doAddControlPoint allowing the derived class to handle the add. The derived class
     * must return a list of control points added or null if none are added. If the control point(s) are added the
     * base class will generate a FeatureDrawEventEnum.DRAW_UPDATE event indicating the addition and
     * adds the control point to the map.
     *
     * 7) - If the user double clicks (double taps) on a control point, the base class calls doDeleteControlPoint.
     * The derived class must return a list of the control points removed, null otherwise.
     * If the control point(s) are removed the base class generates a FeatureDrawEventEnum.DRAW_UPDATE
     * event indicating the deletion, remove the control points from the map and updates the feature on the map.
     *
     * 8) - If the user drags a control point the base class calls doControlPointMoved. The derived class
     * must return a list containing all of the control points that are affected by
     * the action. The base class updates the control points and the feature on the map, and generate a
     * FeatureDrawEventEnum.DRAW_UPDATE event.
     *
     * 9) - If the user drags on the feature (not a control point) the base class calls doFeatureMove.
     * The base class generates a FeatureDrawEventEnum.DRAW_UPDATE and updates the control points and feature
     * on the map.
     *
     * 10) - If the draw operation is canceled, all control points are removed from the map. The feature
     * is removed if it is a new feature.  The base class generates a FeatureDrawEventEnum.DRAW_CANCELED
     * event and the editor is deleted. If the feature already existed in the core the stored properties
     * are reapplied to the feature to return it to its original state.
     *
     * 11) - If the operation is completed the base class removes all control points from the map, the feature
     * is removed if it is a new feature, then generates a FeatureDrawEventEnum.DRAW_COMPLETE.
     *
     * @param map The map on which the draw will take place.
     * @param feature The feature that is to drawn.
     * @param oEventListener An event listener for the draw events.
     * @param bUsesCP If true the feature requires the use of control points during the draw process.
     * @throws EMP_Exception
     */
    protected AbstractDrawEditEditor(IMapInstance map, T feature, IDrawEventListener oEventListener, boolean bUsesCP, boolean newFeature) throws EMP_Exception {
        super(map, feature, EditorMode.DRAW_MODE, newFeature);

        this.bUsesControlPoints = bUsesCP;
        this.oDrawEventListener = oEventListener;
    }

    protected void initializeDraw() throws EMP_Exception {
        IGeoPosition oNewPos;

        if (!this.isNewFeature()) {
            // An existing feature was place into draw mode.

            // The following makes a copy of the feature coordinate in
            // case the draW is canceled.
            for (IGeoPosition oPos : this.oFeature.getPositions()) {
                oNewPos = new GeoPosition();
                oNewPos.setLatitude(oPos.getLatitude());
                oNewPos.setLongitude(oPos.getLongitude());
                oNewPos.setAltitude(oPos.getAltitude());
                this.oOrgPosList.add(oNewPos);
            }
        }

        // we need to prepare to draw the object.
        this.prepareForDraw();
        // Set the default altitude mode.
        storageManager.setDefaultAltitudeMode(this.getFeature());
        if (this.isNewFeature()) {
            // We need to plot the object to the map.
            storageManager.addDrawFeature(this.oClientMap, this.getFeature());
        }
        // Add the control points if any.
        if (this.bUsesControlPoints) {
            this.assembleControlPoints();
            this.updateControlPointOnMap();
        }
        this.issueStartEvent();
        this.issueUpdateEvent();
    }

    public EditorStateEnum getState() {
        return this.eEditorState;
    }

    /**
     * The derived class can implement this function.
     * It is called once at the start of a draw operation to allows the derived class to assign
     * geographic coordinates to the new feature that place the feature at the center of the viewing
     * area on the map. It should use the maps camera position to compute its coordinates.
     */
    protected void prepareForDraw() throws EMP_Exception {}

    /**
     * The subclass can implement this function.
     * It is called once at the start of an edit operation,
     * before the assembleControlPoints is called.
     */
    protected void prepareForEdit() throws EMP_Exception {}

    /**
     * This method is call so that the editor can display the features control points.
     */
    protected void assembleControlPoints() {}

    /**
     * This method is call for icon type feature to the editor can
     * move the icon to the specified location.
     * @param oLatLng The location to move to.
     * @retrun True if the feature was moved, false otherwise.
     */
    protected boolean moveIconTo(IGeoPosition oLatLng) {
        return false;
    }

    /**
     * The Editor must override this method to handle adding of control points.
     * If the control point is added it must return the control point.
     * @param oLatLng The Lat lon of the new control point.
     * @return A list of new control points or null if non are added.
     */
    protected List<ControlPoint> doAddControlPoint(IGeoPosition oLatLng) {
        return null;
    }

    /**
     * The editor must override this method to handle control point deletions.
     * @param oCP The control point to delete.
     * @return A list of control points deleted, or null if non are removed.
     */
    protected List<ControlPoint>  doDeleteControlPoint(ControlPoint oCP) {
        return null;
    }

    /**
     * The editor must override this method to handle control point motion
     * @param oCP The control point that was moved.
     * @param oLatLon The new location for the control point.
     * @return A list of control points that have been affected.
     */
    protected boolean doControlPointMoved(ControlPoint oCP, IGeoPosition oLatLon) {
        return false;
    }

    /**
     * The editor must override this method to handle a feature drag if its is not moving all the control points.
     * @param dBearing The bearing of motion.
     * @param dDistance The distance traveled.
     * @return True if the feature was moved and False otherwise.
     */
    protected boolean doFeatureMove(double dBearing, double dDistance) {
        if (this.bUsesControlPoints) {
            for (ControlPoint cp : this.getControlPointList()) {
                cp.moveControlPoint(dBearing, dDistance);
            }
        } else {
            return false;
        }
        return true;
    }

    /**
     * This method is called by the derived class to add control points when they are created in
     * assembleControlPoints.
     * @param oCP The new created control point.
     */
    protected void addControlPoint(ControlPoint oCP) {
        if (!this.oControlPointList.contains(oCP)) {
            this.oControlPointList.add(oCP);
        }
    }

    private void addControlPoint(List<ControlPoint> oCPList) {
        for (ControlPoint oCP: oCPList) {
            if (!this.oControlPointList.contains(oCP)) {
                this.oControlPointList.add(oCP);
            }
        }
    }

    private void removeControlPoint(List<ControlPoint> oCPList) {
        IUUIDSet oList = new UUIDSet();

        for (ControlPoint oCP: oCPList) {
            oList.add(oCP.getGeoId());
            this.oControlPointList.remove(oCP);
        }

        if (oList.size() > 0) {
            // Remove from the map
            this.mapInstance.removeFeatures(oList, null);
        }
    }

    private void removeControlPointsFromMap() {
        IUUIDSet oList = new UUIDSet();

        for (ControlPoint oCP: this.oControlPointList) {
            oList.add(oCP.getGeoId());
        }
        this.oControlPointList.clear();

        if (oList.size() > 0) {
            // Remove from the map
            this.mapInstance.removeFeatures(oList, null);
        }
    }

    private void updateControlPointOnMap() {
        FeatureVisibilityList oList = new FeatureVisibilityList();
        FeatureVisibility oItem;

        for (ControlPoint oCP: this.oControlPointList) {
            oItem = new FeatureVisibility(oCP, true);
            oList.add(oItem);
        }
        if (oList.size() > 0) {
            mapInstance.addFeatures(oList, null);
        }
    }

    protected void removeFeature() {
        // Remove from the map
        try {
            storageManager.removeDrawFeature(this.oClientMap, this.oFeature);
        } catch (EMP_Exception e) {
            e.printStackTrace();
        }
    }

    protected boolean isIdle() {
        return (this.getState() == EditorStateEnum.IDLE);
    }

    protected boolean isAddingCP() {
        return (this.getState() == EditorStateEnum.ADDING_CP);
    }

    protected boolean isDeletingCP() {
        return (this.getState() == EditorStateEnum.DELETE_CP);
    }

    protected boolean isDraggingCP() {
        return (this.getState() == EditorStateEnum.DRAGGING_CP);
    }

    protected boolean isDraggingFeature() {
        return (this.getState() == EditorStateEnum.DRAGGING_FEATURE);
    }

    @Override
    public boolean isFinishing() {
        return finishing;
    }

    private void setFinishing(boolean finishing) {
        this.finishing = finishing;
    }

    @Override
    public boolean onEvent(MapInstanceUserInteractionEvent oEvent) {
        boolean bRet = false;

        switch (oEvent.getEvent()) {
            case CLICKED:
                if (oEvent.getCoordinate() != null) {
                    if (this.bUsesControlPoints) {
                        this.eEditorState = EditorStateEnum.ADDING_CP;
                        List<ControlPoint> oCPList = this.doAddControlPoint(oEvent.getCoordinate());
                        if ((oCPList != null) && (oCPList.size() > 0)) {
                            try {
                                this.storageManager.apply(this.oFeature, false, oEvent.getUserContext());
                            } catch (EMP_Exception ex) {
                                Log.e(TAG, "storageManger.apply failed.", ex);
                            }
                            this.addControlPoint(oCPList);
                            this.updateControlPointOnMap();
                        }
                        this.issueUpdateEvent();
                        this.eEditorState = EditorStateEnum.IDLE;
                    } else {
                        this.moveIconTo(oEvent.getCoordinate());
                        this.sendUpdateEvent(); // This will execute Feature.apply in all cases.
                    }
                }
                bRet = true;
                break;
            case DOUBLE_CLICKED:
                bRet = true;
                break;
            case LONG_PRESS:
                bRet = true;
                break;
            case DRAG:
                bRet = true;
                break;
            case DRAG_COMPLETE:
                bRet = true;
                break;
        }

        return bRet;
    }

    @Override
    public boolean onEvent(MapInstanceFeatureUserInteractionEvent oEvent) {
        boolean bRet = false;
        IFeature oEventedFeature = oEvent.getFeatures().get(0);
        boolean bIsCP = (oEvent.getFeatures().get(0) instanceof ControlPoint);
        boolean bIsNewCP = false;

        if (bIsCP) {
            // If the top feature is a control point, check to see if its a new CP.
            // If it is a new CP, check to see if there is another CP on the list that is NOT a new CP.
            // If there is another control point on the list that is NOT a new CP, generate the event
            // for that control point.
            int iIndex = 1;
            IFeature tempFeature;
            ControlPoint cp = (ControlPoint) oEventedFeature;
            while ((null != cp) && (cp.getCPType() == ControlPoint.CPTypeEnum.NEW_POSITION_CP)) {
                if (iIndex < oEvent.getFeatures().size()) {
                    tempFeature = oEvent.getFeatures().get(iIndex);
                    if (tempFeature instanceof ControlPoint) {
                        cp = (ControlPoint) tempFeature;
                    }
                } else {
                    cp = null;
                }
                iIndex++;
            }

            if (null != cp) {
                if (cp.getCPType() != ControlPoint.CPTypeEnum.NEW_POSITION_CP) {
                    oEventedFeature = cp;
                } else {
                    bIsNewCP = true;
                }
            }
        }

        switch (oEvent.getEvent()) {
            case CLICKED:
                // We only process clicks on Map user interaction events.
/*
                if ((oEvent.getCoordinate() != null) && !bIsCP) {
                    this.eEditorState = EditorStateEnum.ADDING_CP;
                    List<ControlPoint> oCPList = this.doAddControlPoint(oEvent.getCoordinate());
                    if ((oCPList != null) && (oCPList.size() > 0)) {
                        try {
                            this.storageManager.apply(this.oFeature, false, oEvent.getUserContext());
                        } catch (EMP_Exception ex) {
                            Log.e(TAG, "storageManger.apply failed.", ex);
                        }
                        this.addControlPoint(oCPList);
                        this.updateControlPointOnMap();
                    }
                    this.issueUpdateEvent();
                    this.eEditorState = EditorStateEnum.IDLE;
                }
*/
                bRet = true;
                break;
            case DOUBLE_CLICKED:
                if (bIsCP) {
                    if (!bIsNewCP) {
                        this.eEditorState = EditorStateEnum.DELETE_CP;
                        List<ControlPoint> oCPList = this.doDeleteControlPoint((ControlPoint) oEventedFeature);
                        if ((oCPList != null) && (oCPList.size() > 0)) {
                            // The Editor deleted CPs.
                            // TODO add update event generation.
                            try {
                                this.storageManager.apply(this.oFeature, false, oEvent.getUserContext());
                            } catch (EMP_Exception ex) {
                                Log.e(TAG, "storageManger.apply failed.", ex);
                            }
                            this.removeControlPoint(oCPList);
                            this.updateControlPointOnMap();
                        }
                        this.issueUpdateEvent();
                        this.eEditorState = EditorStateEnum.IDLE;
                    }
                    bRet = true;
                }
                break;
            case LONG_PRESS:
                break;
            case DRAG:
                if (oEvent.getCoordinate() != null) {
                    if (!bIsCP) {
                        if (this.oFeature.getGeoId().equals(oEventedFeature.getGeoId())) {
                            // It is not a control point and it is the feature we are editing.
                            double dBearing = GeographicLib.computeBearing(oEvent.getStartCoordinate(), oEvent.getCoordinate());
                            double dDistance = GeographicLib.computeDistanceBetween(oEvent.getStartCoordinate(), oEvent.getCoordinate());

                            //Log.d(TAG, "Dist: " + dDistance + "  Bearing: " + dBearing);
                            this.eEditorState = EditorStateEnum.DRAGGING_FEATURE;
                            if (this.doFeatureMove(dBearing, dDistance)) {
                                // The editor moved the feature. We need to issue the update event.
                                this.sendUpdateEvent();  // sendUpdateEvent will invoke Feature.apply()
                                this.updateControlPointOnMap();
                                bRet = true;
                            }
                            this.eEditorState = EditorStateEnum.IDLE;
                        }
                    } else {
                        this.eEditorState = EditorStateEnum.DRAGGING_CP;
                        if (this.doControlPointMoved((ControlPoint) oEventedFeature, oEvent.getCoordinate())) {
                            try {
                                this.storageManager.apply(this.oFeature, false, oEvent.getUserContext());
                            } catch (EMP_Exception ex) {
                                Log.e(TAG, "storageManger.apply failed.", ex);
                            }
                            this.updateControlPointOnMap();
                            bRet = true;
                        }
                        this.issueUpdateEvent();
                        this.eEditorState = EditorStateEnum.IDLE;
                    }
                }
                break;
            case DRAG_COMPLETE:
                bRet = true;
                break;
        }

        return bRet;
    }

    private void issueDrawEvent(FeatureDrawEventEnum eEvent) throws EMP_Exception {

        try {
            if (this.oDrawEventListener != null) {
                switch (eEvent) {
                    case DRAW_START:
                        this.oDrawEventListener.onDrawStart(this.oClientMap);
                        break;
                    case DRAW_UPDATE:
                        break;
                    case DRAW_CANCELED:
                        this.oDrawEventListener.onDrawCancel(this.oClientMap, this.oFeature);
                        break;
                    case DRAW_COMPLETE:
                        this.oDrawEventListener.onDrawComplete(this.oClientMap, this.oFeature);
                        break;
                }
            }
        } catch(Exception ex) {
            Log.e(TAG, "issueDrawEvent", ex);
        }
        FeatureDrawEvent oEvent = new FeatureDrawEvent(eEvent, this.oFeature, this.oClientMap);
        eventManager.generateFeatureDrawEvent(oEvent);
    }

    private void issueEditEvent(FeatureEditEventEnum eEvent) throws EMP_Exception {

        try {
            if (this.oEditEventListener != null) {
                switch (eEvent) {
                    case EDIT_START:
                        this.oEditEventListener.onEditStart(this.oClientMap);
                        break;
                    case EDIT_UPDATE:
                        break;
                    case EDIT_CANCELED:
                        this.oEditEventListener.onEditCancel(this.oClientMap, this.oFeature);
                        break;
                    case EDIT_COMPLETE:
                        this.oEditEventListener.onEditComplete(this.oClientMap, this.oFeature);
                        break;
                }
            }
        } catch (Exception ex) {
            Log.e(TAG, "issueEditEvent", ex);
        }
        FeatureEditEvent oEvent = new FeatureEditEvent(eEvent, this.oFeature, this.oClientMap);
        eventManager.generateFeatureEditEvent(oEvent);
    }

    private void issueStartEvent() {
        try {
            if (this.inDrawMode()) {
                this.issueDrawEvent(FeatureDrawEventEnum.DRAW_START);
            } else {
                this.issueEditEvent(FeatureEditEventEnum.EDIT_START);
            }
            //coreManager.setMotionLockMode(this.oClientMap, MapMotionLockEnum.SMART_LOCK);
        } catch (EMP_Exception e) {
            Log.e(TAG, "issueStartEvent", e);
        }
    }

    protected void restoreOnCancel() {
        this.oFeature.getPositions().clear();
        this.oFeature.getPositions().addAll(this.oOrgPosList);
    }

    private void issueCanceledEvent() {
        try {
            if (this.inDrawMode()) {
                if (this.isNewFeature()) {
                    // Remove from the map.
                    this.removeFeature();
                } else {
                    this.restoreOnCancel();
                    try {
                        this.storageManager.apply(this.oFeature, false, null);
                    } catch (EMP_Exception ex) {
                        Log.e(TAG, "storageManger.apply failed.", ex);
                    }
                }
                this.issueDrawEvent(FeatureDrawEventEnum.DRAW_CANCELED);
            } else {
                this.restoreOnCancel();
                try {
                    this.storageManager.apply(this.oFeature, false, null);
                } catch (EMP_Exception ex) {
                    Log.e(TAG, "storageManger.apply failed.", ex);
                }
                this.issueEditEvent(FeatureEditEventEnum.EDIT_CANCELED);
            }
            //coreManager.setMotionLockMode(this.oClientMap, MapMotionLockEnum.UNLOCKED);
        } catch (EMP_Exception e) {
            Log.e(TAG, "issueCanceledEvent", e);
        }
    }

    private void issueCompleteEvent() {
        try {
            if (this.inDrawMode()) {
                this.issueDrawEvent(FeatureDrawEventEnum.DRAW_COMPLETE);
                if (this.isNewFeature()) {
                    this.removeFeature();
                }
            } else {
                this.issueEditEvent(FeatureEditEventEnum.EDIT_COMPLETE);
            }
            //coreManager.setMotionLockMode(this.oClientMap, MapMotionLockEnum.UNLOCKED);
        } catch (EMP_Exception e) {
            Log.e(TAG, "issueCompleteEvent", e);
        }
    }

//    public static List<IEditUpdateData> clone(List<IEditUpdateData> t) throws Exception {
//        ByteArrayOutputStream bos = new ByteArrayOutputStream();
//
//        ObjectOutputStream oos = new ObjectOutputStream(bos);
//        oos.writeObject(t);
//        byte[] bytes = bos.toByteArray();
//        ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bytes));
//
//        return (List<IEditUpdateData>)ois.readObject();
//    }

    public static List<IEditUpdateData> clone(List<IEditUpdateData> t) throws Exception {
        List<IEditUpdateData> copy = new ArrayList<>();
        for (IEditUpdateData e : t) {
            copy.add(new EditUpdateData(e));
        }
        return copy;
    }

    protected void issueUpdateEvent() {
        if (this.oUpdateList.size() > 0) {
            try {
                List<IEditUpdateData> clonedList = clone(this.oUpdateList);
                if (this.inDrawMode()) {
                    if (this.oDrawEventListener != null) {
                        try {
                            this.oDrawEventListener.onDrawUpdate(this.oClientMap, this.oFeature, clonedList);
                        } catch (Exception ex) {
                            Log.e(TAG, "issueUpdateEvent", ex);
                        }
                    }
                    FeatureDrawEvent oEvent = new FeatureDrawEvent(this.oFeature, this.oClientMap, clonedList);
                    eventManager.generateFeatureDrawEvent(oEvent);
                } else {
                    if (this.oEditEventListener != null) {
                        try {
                            this.oEditEventListener.onEditUpdate(this.oClientMap, this.oFeature, clonedList);
                        } catch (Exception ex) {
                            Log.e(TAG, "issueUpdateEvent", ex);
                        }
                    }
                    FeatureEditEvent oEvent = new FeatureEditEvent(this.oFeature, this.oClientMap, clonedList);
                    eventManager.generateFeatureEditEvent(oEvent);
                }
            } catch (Exception e) {
                Log.e(TAG, "issueUpdateEvent", e);
            }
            // Clear the list for reuse.
            this.oUpdateList.clear();
        }
    }

    /**
     * Note that there are callers that depend on Feature.apply() being invoked here.
     */

    protected void sendUpdateEvent() {
        int[] aIndex = new int[this.oFeature.getPositions().size()];
        IEditUpdateData oUpdateEntry;
        int iIndex = 0;

        for (IGeoPosition oPos: this.oFeature.getPositions()) {
            aIndex[iIndex] = iIndex;
            iIndex++;
        }

        try {
            oUpdateEntry = new EditUpdateData(FeatureEditUpdateTypeEnum.COORDINATE_MOVED, aIndex);
            oUpdateList.add(oUpdateEntry);
            this.issueUpdateEvent();
        } catch (EMP_Exception e) {
            Log.e(TAG, "sendUpdateEvent", e);
        }
        // Now update the feature on the map.
        try {
            this.storageManager.apply(this.oFeature, false, null);
        } catch (EMP_Exception ex) {
            Log.e(TAG, "storageManger.apply failed.", ex);
        }
    }

    @Override
    public void Cancel() {
        setFinishing(true);
        this.removeControlPointsFromMap();
        this.issueCanceledEvent();
    }

    @Override
    public void Complete() {
        setFinishing(true);
        this.removeControlPointsFromMap();
        this.issueCompleteEvent();
    }

    protected ControlPoint findControlPoint(ControlPoint.CPTypeEnum eType, int index, int subIndex) {
        for (ControlPoint cp: this.oControlPointList) {
            if ((cp.getCPType() == eType) && (index == cp.getCPIndex()) && (subIndex == cp.getCPSubIndex())) {
                return cp;
            }
        }

        return null;
    }

    protected void addUpdateEventData(FeatureEditUpdateTypeEnum eType, int[] aIndexes) {
        try {
            IEditUpdateData updateData = new EditUpdateData(eType, aIndexes);
            this.oUpdateList.add(updateData);
        } catch (EMP_Exception e) {
            Log.e(TAG, "addUpdateEventData", e);
        }
    }

    protected void addUpdateEventData(FeatureEditUpdateTypeEnum eType) {
        try {
            IEditUpdateData updateData = new EditUpdateData(eType);
            this.oUpdateList.add(updateData);
        } catch (EMP_Exception e) {
            Log.e(TAG, "addUpdateEventData", e);
        }
    }

    protected void addUpdateEventData(IGeoMilSymbol.Modifier eModifier) {
        try {
            IEditUpdateData updateData = new EditUpdateData(eModifier);
            this.oUpdateList.add(updateData);
        } catch (EMP_Exception e) {
            Log.e(TAG, eModifier.toString(), e);
        }
    }

    protected void addUpdateEventData(FeaturePropertyChangedEnum eProperty) {
        try {
            IEditUpdateData updateData = new EditUpdateData(eProperty);
            this.oUpdateList.add(updateData);
        } catch(EMP_Exception e) {
            Log.e(TAG, eProperty.toString(), e);
        }
    }

    /**
     * This method increases by 1 all control point indexes equal or greater than startingIndex.
     * @param startIndex
     */
    protected void increaseControlPointIndexes(int startIndex) {
        for (ControlPoint cp: this.oControlPointList) {
            cp.increaseIndexes(startIndex);
        }
    }

    /**
     * This method decreases by 1 all control point indexes equal or greater than startingIndex.
     * @param startIndex
     */
    protected void decreaseControlPointIndexes(int startIndex) {
        for (ControlPoint cp: this.oControlPointList) {
            cp.decreaseIndexes(startIndex);
        }
    }

    /**
     * This method creates a new control point of the specified type index and sub index. And places it
     * in the center of the line between pos1 and pos2.
     * @param pos1
     * @param pos2
     * @param eType
     * @param cpIndex
     * @param cpSubIndex
     * @return
     */
    protected ControlPoint createCPBetween(IGeoPosition pos1, IGeoPosition pos2, ControlPoint.CPTypeEnum eType, int cpIndex, int cpSubIndex) {
        ControlPoint newCP = new ControlPoint(eType, cpIndex, cpSubIndex);
        newCP.moveCPBetween(pos1, pos2);
        this.addControlPoint(newCP);

        return newCP;
    }

    protected List<ControlPoint> getControlPointList() {
        return this.oControlPointList;
    }

    /**
     * This method will change the indexes by the delta provided, of all CP of the specified type, with index values >=
     * to the startIndex.
     * @param eType
     * @param startIndex
     * @param delta
     */
    protected void changeControlPointIndexes(ControlPoint.CPTypeEnum eType, int startIndex, int delta) {
        for (ControlPoint cp: this.getControlPointList()) {
            if (cp.getCPType() == eType) {
                if (cp.getCPIndex() >= startIndex) {
                    cp.setCPIndex(cp.getCPIndex() + delta);
                }
            }
        }
    }

    /**
     * This method will change the sub indexes by the delta provided, of all CP of the specified type, with sub index values >=
     * to the startIndex.
     * @param eType
     * @param startIndex
     * @param delta
     */
    protected void changeControlPointSubIndexes(ControlPoint.CPTypeEnum eType, int startIndex, int delta) {
        for (ControlPoint cp: this.getControlPointList()) {
            if (cp.getCPType() == eType) {
                if (cp.getCPSubIndex() >= startIndex) {
                    cp.setCPSubIndex(cp.getCPSubIndex() + delta);
                }
            }
        }
    }
}
