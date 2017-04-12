package mil.emp3.worldwind.controller;

import android.content.res.Resources;
import android.graphics.Point;

import android.util.Log;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;

import org.cmapi.primitives.GeoPosition;
import org.cmapi.primitives.IGeoPosition;

import java.util.Date;
import java.util.EnumSet;

import gov.nasa.worldwind.BasicWorldWindowController;
import gov.nasa.worldwind.PickedObject;
import gov.nasa.worldwind.PickedObjectList;
import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.geom.Line;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Vec3;

import mil.emp3.api.enums.MapMotionLockEnum;
import mil.emp3.api.enums.UserInteractionEventEnum;
import mil.emp3.api.enums.UserInteractionKeyEnum;
import mil.emp3.api.enums.UserInteractionMouseButtonEnum;
import mil.emp3.worldwind.MapInstance;
import mil.emp3.worldwind.utils.Conversion;

/**
 * This class handles the WW and android events to process picks which
 * are converted to feature / map user interaction events.
 */
public class PickNavigateController extends BasicWorldWindowController implements View.OnClickListener, View.OnKeyListener{
    private static final String TAG = PickNavigateController.class.getSimpleName();

    private static int DRAG_MIN_DISTANCE_SQUARED = (int) (Resources.getSystem().getDisplayMetrics().densityDpi / 16.0)^2; // 1/16 on an inch squared.

    private static float PICK_WIDTH = (float) (Resources.getSystem().getDisplayMetrics().densityDpi * 0.33); // 1/3 in
    private static float PICK_HALF_WIDTH = PickNavigateController.PICK_WIDTH / 2;

    private Line ray = new Line(); // pre-allocated to avoid memory allocations
    private Vec3 pickPoint = new Vec3();    // pre-allocated to avoid memory allocations
    private IGeoPosition oGeoPosition = new GeoPosition(); // pre-allocated to avoid memory allocations.
    private IGeoPosition oGeoPosition2 = new GeoPosition(); // pre-allocated to avoid memory allocations.
    private Position oPos = new Position(); // pre-allocated to avoid memory allocations.
    private android.graphics.Point oPointCoordinate = new android.graphics.Point(); // pre-allocated to avoid memory allocations.
    private android.graphics.Point oPreviousDragPoint = new android.graphics.Point(); // pre-allocated to avoid memory allocations.

    private final java.util.List<mil.emp3.api.interfaces.IFeature> oFeaturePickList = new java.util.ArrayList<mil.emp3.api.interfaces.IFeature>();
    protected final GestureDetector pickGestureDetector;
    private MapMotionLockEnum eLockMode = MapMotionLockEnum.UNLOCKED;
    private IGeoPosition oPreviousScrollEventPosition = null;
    private boolean actionUpDetected = false;
    private boolean isDragging = false;
    private boolean dragConsumed = false;
    private MotionEvent dragDownEvent = null;
    private Date lastDragComplete = new Date();

    private EnumSet<UserInteractionKeyEnum> oKeys = EnumSet.noneOf(UserInteractionKeyEnum.class);
    private UserInteractionMouseButtonEnum oButton = UserInteractionMouseButtonEnum.NONE;

    public void setLockMode(MapMotionLockEnum eMode) {
        Log.i(TAG, "setLockMode " + eMode);
        this.eLockMode = eMode;
    }

    private final WorldWindow ww;
    private final MapInstance mapInstance;
    private final GestureDetector.SimpleOnGestureListener oGestureListener = new GestureDetector.SimpleOnGestureListener() {

        /**
         * onDown must always return false so that Gesture Handling can be completed. Simply because an item is picked doesn't
         * mean we have consumed the event.
         *
         * @param event
         * @return
         */
        @Override
        public boolean onDown(MotionEvent event) {
            boolean retValue = false;
            //Log.d(TAG, "Action onDown " + MotionEvent.actionToString(event.getActionMasked()) + " PC " + event.getPointerCount());

            // Reset thee previous scroll event.
            PickNavigateController.this.oPointCoordinate.set((int) event.getX(), (int) event.getY());
            PickNavigateController.this.oPreviousScrollEventPosition = null;

            if(event.getPointerCount() == 1) {
                PickNavigateController.this.pick(event);    // Pick the object(s) at the tap location
            } else {
                Log.e(TAG, "This should NEVER happen Action onDown clear pick list");
                PickNavigateController.this.oFeaturePickList.clear();
                PickNavigateController.this.isDragging = false;
                PickNavigateController.this.dragDownEvent = null;
            }

            return retValue;   // By not consuming this event, we allow it to pass on to the navigation gesture handlers
        }

        @Override
        public boolean onSingleTapConfirmed(MotionEvent event) { //onSingleTapUp
            //Log.d(TAG, "Action onSingleTapConfirmed " + MotionEvent.actionToString(event.getActionMasked()) + " PC " + event.getPointerCount());
            // The onSingleTapUp single-tap handler has a faster response time than onSingleTapConfirmed.

            // We do not consume this event; we allow the "up" event to pass on to the navigation gestures,
            // which is required for proper zoom gestures.  Consuming this event will cause the first zoom
            // gesture to be ignored.
            //
            // A drawback to using this callback is that a the first tap of a double-tapping will temporarily
            // deselect an item, only to reselected on the second tap.
            //
            // As an alternative, you can implement onSingleTapConfirmed and consume event as you would expect,
            // with the trade-off being a slight delay in the tap response time.
            return PickNavigateController.this.onSingleTapHandler(event);
        }

        @Override
        public boolean onScroll(MotionEvent downEvent, MotionEvent moveEvent, float distanceX, float distanceY) {
            //Log.d(TAG, "Action onScroll PC " + downEvent.getPointerCount() + " " + moveEvent.getPointerCount());
            return PickNavigateController.this.onScrollHandler(downEvent, moveEvent, distanceX, distanceY);
        }

        @Override
        public boolean onDoubleTap(MotionEvent event) {
            //Log.d(TAG, "Action onDoubleTap " + MotionEvent.actionToString(event.getActionMasked()) + " PC " + event.getPointerCount());
            return PickNavigateController.this.onDoubleTapHandler(event);
        }

        @Override
        public void onLongPress(MotionEvent event) {
            //Log.d(TAG, "Action onLongPress " + MotionEvent.actionToString(event.getActionMasked()) + " PC " + event.getPointerCount());
            PickNavigateController.this.onLongPressHandler(event);
        }

        @Override
        public void onShowPress(MotionEvent event) {
            //Log.d(TAG, "Action onShowPress " + MotionEvent.actionToString(event.getActionMasked()) + " PC " + event.getPointerCount());
            PickNavigateController.this.onShowPressHandler(event);
        }
    };
    
    public PickNavigateController(MapInstance mapInstance, WorldWindow ww) {
        this.ww = ww;
        this.mapInstance = mapInstance;
        this.pickGestureDetector = new GestureDetector(ww.getContext(), this.oGestureListener);
    }

    /**
     * Delegates events to the pick handler or the native World Wind navigation handlers.
     *
     * Here are some basic items that should be considered before making any changes to any logic here
     *
     * 1. Please see comments in MapInstanceEventHandler.java related to DRAG/DRAG_COMPLETE event. It is the decoupling between
     *      MapInstanceFeatureInteractionEvent and FeatureInteractionEvent
     *      MapInstanceUserInteractionEvent and UserInteractionEvent
     *     that allows us to correctly check for consumption of DRAG event and process it correctly. We want the DRAG to be treated as
     *     consumed only when it is consumed my one of the editors in the EMP core. If it is not consumed by one of the editors then
     *     it gets passed to the application, in that case we want to call super.onTouchEvent to handle the DRAG. If application wants to
     *     handle the DRAG event and prevent the map map/zoom/etc then application needs to LOCK the map.
     * 2. Once DRAG starts (it is consumed by the editor), super.onTouchEvent will NOT be invoked until DRAG_COMPLETE is generated, which is on
     *     ACTION_UP. Not doing so will cause jumpy movements on the map. This is controlled by the dragConsumed variable.
     * 3. Changes have been made so that zoom, pan, rotate, tilt operations will continue to work even if a feature is in the pick list
     *     unless you are in one of the ACTIVE Editor Mode. Even in the case where Editor is active, If editor doesn't act on DRAG then zoom/
     *     pan/tilt/rotate continue to work as expected.
     * 4. If the map is LOCKED or SMART_LOCKED by the user then zoom, pan, rotate, tilt are responsibility of the user as we will NOT invoke
     *     super.onTouchEvent here. Remember that LOCK and EDITORs are mutually exclusive.
     *
     * @param event
     * @return 
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        //Log.d(TAG, "In Action onTouchEvent " + MotionEvent.actionToString(event.getActionMasked()) + " PC " + event.getPointerCount());
        this.actionUpDetected = false;
        boolean consumed = false;

        if(event.getPointerCount() > 1) {
            //Log.d(TAG, "onTouchEvent clear pick list and reset dragging");
            oFeaturePickList.clear();
            isDragging = false;
            dragDownEvent = null;
            oPreviousScrollEventPosition = null;
        }

        if ((System.currentTimeMillis() - lastDragComplete.getTime()) < 1000) {
            // After we process the drag complete which occurs with an ACTION_UP while isDragging,
            // we can't process events within a ~ 1 sec because the ACTION_UP that ends the drag that is
            // followed by an ACTION_DOWN then an ACTION_UP in rapid succession causes a double tap
            // to be generated.
            Log.i(TAG, event.getAction() + " evt to close to drag complete. Picks " + this.oFeaturePickList.size() + ".");
            return true;
        }

        // See if its an UP event
        switch (event.getAction()) {
            case MotionEvent.ACTION_UP: {
                // See if we are dragging
                if (this.isDragging) {
                    // The onScroll is not generated by the up action so we need to process it here
                    // to generated the DRAW_COMPLETE event.
                    Log.i(TAG, "ACTION_UP evt. Picks " + this.oFeaturePickList.size() + ".");
                    this.actionUpDetected = true;
                    consumed = this.onScrollHandler(this.dragDownEvent, event, 0, 0);
                    lastDragComplete.setTime(System.currentTimeMillis());
                } else {
                    Log.i(TAG, "ACTION_UP evt NOT dragging. Picks " + this.oFeaturePickList.size() + ".");
                }
                break;
            }
        }

        consumed = this.pickGestureDetector.onTouchEvent(event) || consumed;

        // If event was not consumed by the pick operation, pass it on the globe navigation handlers
        if ((MapMotionLockEnum.UNLOCKED == eLockMode) && !consumed && !dragConsumed) {
            // The super class performs the pan, tilt, rotate and zoom
            //Log.i(TAG, "Forwarding event " + event.getAction() + " to super. Picks " + this.oFeaturePickList.size() + ".");
            consumed = super.onTouchEvent(event);
        }

        //Log.d(TAG, "Out Action onTouchEvent " + MotionEvent.actionToString(event.getActionMasked()) + " PC " + event.getPointerCount() + " consumed " + consumed);

        return consumed;
    }

    /**
     * Performs a pick at the tap location.
     * @param event
     */
    //public void singlePick(MotionEvent event) {
    public void pick(MotionEvent event) {
        PickedObject pickedObject;
        Object oObject;
        mil.emp3.api.interfaces.IFeature oFeature;

        this.oFeaturePickList.clear();
        if (event.getPointerCount() > 1) {
            // For now only generate single point events.
            Log.d(TAG, "Pick multiple pointers.");
            return;
        }

        float X = event.getX() - PickNavigateController.PICK_HALF_WIDTH;
        float Y = event.getY() - PickNavigateController.PICK_HALF_WIDTH;

        // Perform a new pick at the screen x, y
        //PickedObjectList pickList = this.ww.pick(event.getX(), event.getY());
        PickedObjectList pickList = this.ww.pickShapesInRect(X, Y, PickNavigateController.PICK_WIDTH, PickNavigateController.PICK_WIDTH);
        int iPickCount = pickList.count();

        for (int iIndex = 0; iIndex < iPickCount; iIndex++) {
            pickedObject = pickList.pickedObjectAt(iIndex);
            if (pickedObject != null) {
                oObject = pickedObject.getUserObject();
                if (oObject instanceof mil.emp3.api.interfaces.IFeature) {
                    oFeature = (mil.emp3.api.interfaces.IFeature) oObject;
                    if (!this.oFeaturePickList.contains(oFeature)) {
                        Log.d(TAG, "Picked " + oFeature.getClass().getSimpleName());
                        this.oFeaturePickList.add(0, oFeature); // Add it at the begining. WW has the top at the end.
                    }
                }
            }
        }
        //Log.d(TAG, "  Pick " + this.oFeaturePickList.size());
    }

    public boolean onSingleTapHandler(MotionEvent oEvent) {
        //Log.d(TAG, "Single Tap " + this.oFeaturePickList.size());

        try {
            this.generateUserInteractionEvent(UserInteractionEventEnum.CLICKED, oEvent);
        } catch (IllegalArgumentException e) {
            // We have seen this during wall paper launch
            Log.e(TAG, "singleTapHandler ", e);
        }
        this.oFeaturePickList.clear();

        return true;
    }

    public boolean onDoubleTapHandler(MotionEvent oEvent) {
        Log.d(TAG, "Double Tap " + this.oFeaturePickList.size());
        
        this.generateUserInteractionEvent(UserInteractionEventEnum.DOUBLE_CLICKED, oEvent);
        this.oFeaturePickList.clear();
        return true;
    }

    public void onLongPressHandler(MotionEvent oEvent) {
        //this.pick(oEvent);
        
        Log.d(TAG, "Long Press " + this.oFeaturePickList.size());
        
        this.generateUserInteractionEvent(UserInteractionEventEnum.LONG_PRESS, oEvent);
    }
    
    public void onShowPressHandler(MotionEvent oDownEvent) {
        //Log.d(TAG, "Show Press " + this.oFeaturePickList.size());
    }

    public boolean onScrollHandler(MotionEvent downEvent, MotionEvent moveEvent, float distanceX, float distanceY) {
        boolean consumed = false;
        if (this.oFeaturePickList.isEmpty()) {
            if (this.eLockMode == MapMotionLockEnum.UNLOCKED) {
                // If there are no picks and we are UNLOCKED, ignore and allow the map to pan.
                Log.i(TAG, "Scroll Evt not dragging Unlocked. Picks 0.");
                return consumed;
            }
        }

        if (this.dragDownEvent == null) {
            Log.i(TAG, "Scroll Evt Drag start. Picks " + this.oFeaturePickList.size() + ".");
            this.isDragging = true;
            this.dragDownEvent = MotionEvent.obtain(downEvent);
        }

        if (!this.actionUpDetected) {
            // It is not the up notification.
            int diff = (int) moveEvent.getX() - oPreviousDragPoint.x;
            int distanceSq = diff * diff;

            diff = (int) (moveEvent.getY() - oPreviousDragPoint.y);
            distanceSq += (diff * diff);

            if (DRAG_MIN_DISTANCE_SQUARED > distanceSq) {
                // If this point is not at least at the min distance from the previous point dump it.
                Log.i(TAG, "Scroll Evt Drag to close. Picks " + this.oFeaturePickList.size() + ".");
                return true;
            }
            oPreviousDragPoint.set((int) moveEvent.getX(), (int) moveEvent.getY());
        }

        IGeoPosition oTempPosition = null;

        if (this.screenPointToGroundPosition(moveEvent.getX(), moveEvent.getY(), oPos)) {
            oGeoPosition.setLatitude(oPos.latitude);
            oGeoPosition.setLongitude(oPos.longitude);
            oGeoPosition.setAltitude(oPos.altitude);
            oTempPosition = oGeoPosition;
        }

        if (this.oPreviousScrollEventPosition == null) {
            if (this.screenPointToGroundPosition(downEvent.getX(), downEvent.getY(), oPos)) {
                oGeoPosition2.setLatitude(oPos.latitude);
                oGeoPosition2.setLongitude(oPos.longitude);
                oGeoPosition2.setAltitude(oPos.altitude);
                this.oPreviousScrollEventPosition = oGeoPosition2;
            }
        }

        if (this.oFeaturePickList.isEmpty()) {
            PickNavigateController.this.oPointCoordinate.set((int) moveEvent.getX(), (int) moveEvent.getY());
            // We reach here if we are in a lock mode and no picks.
            if(moveEvent.getPointerCount() > 1) {
                if(this.isDragging) {
                    this.isDragging = false;
                    this.dragDownEvent = null;
                    this.dragConsumed = false;
                    this.mapInstance.generateMapUserInteractionEvent(UserInteractionEventEnum.DRAG_COMPLETE, this.oKeys, this.oButton,
                            oPointCoordinate, oTempPosition, this.oPreviousScrollEventPosition);
                    // We will return consumed as false;
                }
            } else if (this.actionUpDetected) {
                Log.i(TAG, "Scroll Evt Drag complete. Pick 0");
                this.isDragging = false;
                this.dragDownEvent = null;
                // We will let the NASA WW consume this event so it know where action up happened, else we will have two issues
                //    Map jumps or First zoom gesture after Action up that (actually moved a feature) will be lost.
                // That is why consumed is not assigned to here.
                
                this.mapInstance.generateMapUserInteractionEvent(UserInteractionEventEnum.DRAG_COMPLETE, this.oKeys, this.oButton,
                        oPointCoordinate, oTempPosition, this.oPreviousScrollEventPosition);
                dragConsumed = false;
            } else {
                Log.i(TAG, "Scroll Evt Drag. Pick 0");
                if(moveEvent.getAction() == MotionEvent.ACTION_MOVE) {
                    if((moveEvent.getPointerCount() > 0) && (moveEvent.getHistorySize() > 0)) {
                        Log.d(TAG, "historical " + moveEvent.getHistoricalX(0, 0) + " " + moveEvent.getHistoricalY(0, 0));
                    } else {
                        Log.e(TAG, "PC/HS " + moveEvent.getPointerCount() + "/" +  moveEvent.getHistorySize());
                    }
                } else {
                    Log.e(TAG, "Why is this not a motion event?");
                }
                consumed = this.mapInstance.generateMapUserInteractionEvent(UserInteractionEventEnum.DRAG, this.oKeys, this.oButton,
                        oPointCoordinate, oTempPosition, this.oPreviousScrollEventPosition);
                dragConsumed = consumed;
            }
        } else {
            if (this.actionUpDetected) {
                Log.i(TAG, "Scroll Evt Drag complete. Picks " + this.oFeaturePickList.size() + ".");
                this.isDragging = false;
                this.dragDownEvent = null;

                // We will let the NASA WW consume this event so it know where action up happened, else we will have two issues
                //    Map jumps or First zoom gesture after Action up that (actually moved a feature) will be lost.
                // That is why consumed is not assigned to here.

                this.mapInstance.generateFeatureUserInteractionEvent(UserInteractionEventEnum.DRAG_COMPLETE,
                        this.oKeys, this.oButton, this.oFeaturePickList, oPointCoordinate, oTempPosition, this.oPreviousScrollEventPosition);
                //Log.d(TAG, "DRAG_COMPLETE generateFeatureUserInteractionEvent consumed " + consumed);
                dragConsumed = false;

            } else {
                Log.i(TAG, "Scroll Evt Drag. Picks " + this.oFeaturePickList.size() + ".");
                consumed = this.mapInstance.generateFeatureUserInteractionEvent(UserInteractionEventEnum.DRAG,
                        this.oKeys, this.oButton, this.oFeaturePickList, oPointCoordinate, oTempPosition, this.oPreviousScrollEventPosition);
                //Log.d(TAG, "DRAG generateFeatureUserInteractionEvent consumed " + consumed);
                dragConsumed = consumed;
            }
        }

        // Store the moveEvent position to be used with the next scroll
        if (this.oPreviousScrollEventPosition != null) {
            oGeoPosition2.setLatitude(oGeoPosition.getLatitude());
            oGeoPosition2.setLongitude(oGeoPosition.getLongitude());
            oGeoPosition2.setAltitude(oGeoPosition.getAltitude());
        }

        return consumed;
    }

    private void generateUserInteractionEvent(UserInteractionEventEnum eEvent, MotionEvent oEvent) {
        IGeoPosition oTempPosition = null;

        oPointCoordinate.set((int) oEvent.getX(), (int) oEvent.getY());
        if (this.screenPointToGroundPosition(oEvent.getX(), oEvent.getY(), oPos)) {
            oGeoPosition.setLatitude(oPos.latitude);
            oGeoPosition.setLongitude(oPos.longitude);
            oGeoPosition.setAltitude(oPos.altitude);
            oTempPosition = oGeoPosition;
        }
        
        if (!this.oFeaturePickList.isEmpty()) {
            this.mapInstance.generateFeatureUserInteractionEvent(eEvent, this.oKeys, this.oButton,
                    this.oFeaturePickList, oPointCoordinate, oTempPosition, null);
        }

        this.mapInstance.generateMapUserInteractionEvent(eEvent, this.oKeys, this.oButton,
                oPointCoordinate, oTempPosition, null);
    }
    
    /**
     * Converts a screen point to the geographic coordinates on the globe.
     *
     * @param screenX X coordinate
     * @param screenY Y coordinate
     * @param result  Pre-allocated Position receives the geographic coordinates
     *
     * @return true if the screen point could be converted; false if the screen point is not on the globe
     */
    public boolean screenPointToGroundPosition(float screenX, float screenY, Position result) {
        return Conversion.screenPointToGroundPosition(this.wwd, new Line(), this.pickPoint, screenX, screenY, result);
    }

    /**
     * Converts a screen point to the geographic coordinates on the globe.
     *
     * @param latitude
     * @param longitude
     * @param result  Pre-allocated Point receives the screen coordinates
     *
     * @return true if the screen point could be converted; false if the screen point is not on the globe
     */
    public boolean groundPositionToScreenPoint(double latitude, double longitude, Point result) {
        return Conversion.groundPositionToScreenPoint(this.wwd, latitude, longitude, result);
    }

//    @Override
//    protected void gestureDidEnd() {
//        // TODO This method was added as a fix to a WW issue. We need to remove it when the patch for 0.2.5 is applied.
//        if (this.activeGestures > 0) {
//            this.activeGestures--;
//        } else {
//            System.out.println(new IllegalStateException("gestureDidEnd called without activeGestures. " + this.lookAt).toString());
//        }
//    }

    @Override
    public void onClick(View v) {
        /* not used for current sprint
        v.getX();
        v.getY();*/
        Log.i(TAG, "got click");
        oButton = UserInteractionMouseButtonEnum.NONE;
    }

    @Override
    public boolean onKey(View v, int keyCode, KeyEvent event) {
        //Log.i(TAG, "Got key event " + keyCode);
        oKeys = EnumSet.noneOf(UserInteractionKeyEnum.class);
        return false;
    }
}
