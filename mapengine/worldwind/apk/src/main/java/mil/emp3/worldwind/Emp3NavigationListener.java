package mil.emp3.worldwind;

import android.util.Log;

import org.cmapi.primitives.GeoBounds;
import org.cmapi.primitives.IGeoAltitudeMode;
import org.cmapi.primitives.IGeoBounds;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import gov.nasa.worldwind.Navigator;
import gov.nasa.worldwind.NavigatorEvent;
import gov.nasa.worldwind.NavigatorListener;
import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.geom.Location;
import mil.emp3.api.Camera;
import mil.emp3.api.LookAt;
import mil.emp3.api.enums.MapViewEventEnum;

import mil.emp3.api.interfaces.ICamera;
import mil.emp3.api.interfaces.ILookAt;

import mil.emp3.mapengine.events.MapInstanceViewChangeEvent;
import mil.emp3.worldwind.utils.SystemUtils;

public class Emp3NavigationListener implements NavigatorListener {
    final static private String TAG = Emp3NavigationListener.class.getSimpleName();

    // This will be initialized to wwCamera in the constructor
    private ICamera currentCamera = new Camera();

    // This will be initialized to wwLookAt in the constructor.
    private ILookAt currentLookAt = new LookAt();

    private boolean bLookAtSet = false;        // If application executes setLookAt or apply on curentLookAt then this is set to true
//    private boolean bLookAtUpdated = false;    // TODO This will be used if we decide to go into LookAt mode of operation

    protected final WorldWindow ww;
    protected final MapInstance mapInstance;

    private class EventQueueItem {
        private final MapViewEventEnum eEvent;
        private final gov.nasa.worldwind.geom.Camera oWWCamera;
        private final gov.nasa.worldwind.geom.LookAt oWWLookAT;

        public EventQueueItem(MapViewEventEnum eventEnum, gov.nasa.worldwind.geom.Camera camera,
                              gov.nasa.worldwind.geom.LookAt lookAt) {
            this.eEvent = eventEnum;
            this.oWWCamera = camera;
            this.oWWLookAT = lookAt;
        }

        public MapViewEventEnum getEvent() {
            return this.eEvent;
        }

        public gov.nasa.worldwind.geom.Camera getCamera() {
            return this.oWWCamera;
        }
        public gov.nasa.worldwind.geom.LookAt getLookAt() { return this.oWWLookAT; }
    }
    private class EventProcessingThread extends java.lang.Thread{
        private final BlockingQueue<EventQueueItem> qEventEnumQueue = new LinkedBlockingQueue<>();
        private final Emp3NavigationListener oCameraHandler;
        private final long iEventWaitDelay = 500; // msec
        private MapViewEventEnum ePreviousEventSent = MapViewEventEnum.VIEW_MOTION_STOPPED;
        private EventQueueItem oWaitingQueueItem = new EventQueueItem(MapViewEventEnum.VIEW_MOTION_STOPPED, null, null);

        public EventProcessingThread(Emp3NavigationListener oHandler) {
            this.oCameraHandler = oHandler;
            this.setName("ViewChangeEventHandler");
        }
        
        @Override
        public void run() {
            // bTimerWait is set True if the event is going to be throttle. So we will wait
            // for the next event. We only send the waiting event if the next one does not
            // arrive in lWaitTime.
            //
            // FYI - This algorithm will not throttle itself if the device generates an
            // alternating series of IN_MOTION, MOTION_STOPPED events. However I assume that the
            // probability of it happening is very low. If the assumption is found to be incorrect
            // changes must be made.
            boolean bTimedWait = false;
            long nextEventTime = System.currentTimeMillis() + this.iEventWaitDelay;
            long lWaitTime;
            EventQueueItem oQueueItem = null;
            while (!Thread.interrupted()) {
                try {
                    if (bTimedWait) {
                        lWaitTime = Math.max(nextEventTime - System.currentTimeMillis(), 1);
                        oQueueItem = this.qEventEnumQueue.poll(lWaitTime, TimeUnit.MILLISECONDS);
                        if ((oQueueItem == null) && (this.oWaitingQueueItem != null)) {
                            // We timed out
                            // Lets process the waiting event.
                            //Log.d(TAG, "  Timed out " + this.eWaitingEvent.name());
                            this.oCameraHandler.generateViewChangeEvent(this.oWaitingQueueItem.getEvent(), this.oWaitingQueueItem.getCamera());
                            this.ePreviousEventSent = this.oWaitingQueueItem.getEvent();
                            nextEventTime = System.currentTimeMillis() + this.iEventWaitDelay;
                            this.oWaitingQueueItem = null;
                            bTimedWait = false;
                            continue;
                        }
                        bTimedWait = false;
                    } else {
                        oQueueItem = this.qEventEnumQueue.take();
                    }
                } catch (InterruptedException Ex) {
                    Log.i(TAG, "Thread is interrupted");
                    // interrupted bit is already cleared so just break out of the loop.
                    break;
                } catch (IllegalArgumentException Ex) {
                    // Should never happen. We are going to log it and continue
                    Log.e(TAG, "EventProcessingThread:run ", Ex);
                }
                    
                if (oQueueItem == null) {
                    // We will terminate the thread only if we were interrupted
                    Log.i(TAG, "oQueueItem is null");
                    continue;
                }

                // Log.d(TAG, "  Processing " + eEvent.name());
                switch (oQueueItem.getEvent()) {
                    case VIEW_IN_MOTION:
                        if (this.ePreviousEventSent == MapViewEventEnum.VIEW_MOTION_STOPPED) {
                            // Its the first in motion event. Dont wait just send it.
                            this.oCameraHandler.generateViewChangeEvent(oQueueItem.getEvent(), oQueueItem.getCamera());
                            this.ePreviousEventSent = oQueueItem.getEvent();
                            nextEventTime = System.currentTimeMillis() + this.iEventWaitDelay;
                            bTimedWait = false;
                        } else {
                            // Its another IN MOTION event we wait before we send it.
                            // If another arrives before the wait time expires, we ignore this one.
                            this.oWaitingQueueItem = oQueueItem;
                            bTimedWait = true;
                        }
                        break;
                    case VIEW_MOTION_STOPPED:
                        // I removed the if to force it to send all the stop events we get.
                        //if (this.ePreviousEventSent == MapViewEventEnum.VIEW_IN_MOTION) {
                            // Its the first in motion stopped event. Dont wait just send it.
                            this.oCameraHandler.generateViewChangeEvent(oQueueItem.getEvent(), oQueueItem.getCamera());
                            this.ePreviousEventSent = oQueueItem.getEvent();
                            nextEventTime = System.currentTimeMillis() + this.iEventWaitDelay;

                            // Following code within the if block will be required if we decide to switch to LookAt mode of operation TODO
//                            if(bLookAtSet) {
//                                gov.nasa.worldwind.geom.Camera oWWCamera = oQueueItem.getCamera();
//                                ILookAt newLookAt = LookAtUtil.setupLookAt(oWWCamera.latitude, oWWCamera.longitude, oWWCamera.altitude, currentLookAt.getLatitude(),
//                                        currentLookAt.getLongitude(), currentLookAt.getAltitude());
//                                currentLookAt.copySettingsFrom(newLookAt);
//                                applyLookAtChange(currentLookAt, true);
//                                bLookAtUpdated = true;
//                            }
                            this.oWaitingQueueItem = null;
                            bTimedWait = false;
                        //} else {
                            // Its another MOTION STOPPED event we wait before we send it.
                            // If another arrives before the wait time expires, we ignore this one.
                        //    this.oWaitingQueueItem = oQueueItem;
                        //    bTimedWait = true;
                        //}
                        break;
                }
            }
            Log.i(TAG, "Existing thread Thread is interrupted");
        }
        
        public void queueEvent(MapViewEventEnum eEvent, gov.nasa.worldwind.geom.Camera oWWCamera,
                               gov.nasa.worldwind.geom.LookAt oWWLookAt) {
            this.qEventEnumQueue.add(new EventQueueItem(eEvent, oWWCamera, oWWLookAt));
        }
        
        public void exitThread() {
            this.interrupt();
            try {
                this.join();
            } catch (InterruptedException ex) {
            }
        }
    }
    
    private final EventProcessingThread oEventProcessingThread;
    
    public Emp3NavigationListener(MapInstance mapInstance, WorldWindow ww) {
        this.ww = ww;
        this.mapInstance = mapInstance;
        gov.nasa.worldwind.geom.Camera camera = new gov.nasa.worldwind.geom.Camera();
        gov.nasa.worldwind.geom.LookAt lookAt = new gov.nasa.worldwind.geom.LookAt();

        this.oEventProcessingThread = new EventProcessingThread(this);
        this.oEventProcessingThread.start();

        // Get the default Camera and LookAt setting from NASA WW and make it our currentCamera and currentLookAt setting
        this.ww.getNavigator().getAsCamera(this.ww.getGlobe(), camera);
        this.updateCamera(currentCamera, camera);
        this.ww.getNavigator().getAsLookAt(this.ww.getGlobe(), lookAt);
        this.updateLookAt(currentLookAt, lookAt);
    }
    
    public void Destroy() {
        this.oEventProcessingThread.exitThread();
    }

    /**
     * Sets EMP3 Camera from WW Camera
     * @param oCamera
     * @param oWWCamera
     */
    private void updateCamera(ICamera oCamera, gov.nasa.worldwind.geom.Camera oWWCamera) {
        oCamera.setAltitude(oWWCamera.altitude);

        // oWWCamera.altitudeMode is always ABSOLUTE, this corrupts what was previously set. SO we will ignore it.
        // TODO EMP-2927 created
//        switch(oWWCamera.altitudeMode) {
//            case WorldWind.ABSOLUTE:
//                oCamera.setAltitudeMode(IGeoAltitudeMode.AltitudeMode.ABSOLUTE);
//                break;
//            case WorldWind.CLAMP_TO_GROUND:
//                oCamera.setAltitudeMode(IGeoAltitudeMode.AltitudeMode.CLAMP_TO_GROUND);
//                break;
//            case WorldWind.RELATIVE_TO_GROUND:
//                oCamera.setAltitudeMode(IGeoAltitudeMode.AltitudeMode.RELATIVE_TO_GROUND);
//                break;
//            default:
//                Log.d(TAG, "FIX THIS WorldWind altitudeMode is " + oWWCamera.altitudeMode);
//                oCamera.setAltitudeMode(IGeoAltitudeMode.AltitudeMode.ABSOLUTE);
//        }
        oCamera.setHeading(oWWCamera.heading);
        oCamera.setLatitude(oWWCamera.latitude);
        oCamera.setLongitude(oWWCamera.longitude);
        oCamera.setRoll(oWWCamera.roll);
        oCamera.setTilt(oWWCamera.tilt);
    }

    /**
     * Sets EMP3 LookAt from WW LokAt
     * @param oLookAt
     * @param oWWLookAt
     */
    private void updateLookAt(ILookAt oLookAt, gov.nasa.worldwind.geom.LookAt oWWLookAt) {
        oLookAt.setAltitude(oWWLookAt.altitude);
        // For now lets set the mode to absolute.
        oLookAt.setAltitudeMode(IGeoAltitudeMode.AltitudeMode.ABSOLUTE);
        oLookAt.setRange(oWWLookAt.range);
        oLookAt.setHeading(oWWLookAt.heading);
        oLookAt.setLatitude(oWWLookAt.latitude);
        oLookAt.setLongitude(oWWLookAt.longitude);
        oLookAt.setTilt(oWWLookAt.tilt);
    }

    /**
     * Application will invoke this so that it can change the setting and also add a listener. After changing the setting application
     * can execute apply on the object.
     * @return
     */
    public ICamera getCamera() {
        return this.currentCamera;
    }

    /**
     * Application has created a new Camera object that should become currentCamera. Application can do a getCamera and use that
     * object in the setCamera, but that shouldn't stop setting the currentCamera again.
     * @param oCamera
     * @param animate
     */
    public void setCamera(ICamera oCamera, final boolean animate) {
        this.currentCamera = oCamera;
        if(null == this.currentCamera) {
            this.currentCamera = new mil.emp3.api.Camera();
            gov.nasa.worldwind.geom.Camera wwCamera = new gov.nasa.worldwind.geom.Camera();
            Emp3NavigationListener.this.ww.getNavigator().getAsCamera(this.ww.getGlobe(), wwCamera);
            updateCamera(this.currentCamera, wwCamera);
            moveCamera(this.currentCamera, animate);
        } else {
            this.moveCamera(oCamera, animate);
        }
    }

    /**
     * If application intention is to update the currentCamera then application needs to execute IMap.getCamera, change the settings
     * and the issue apply on the object.
     * @param oCamera
     * @param animate
     */
    public void applyCameraChange(ICamera oCamera, boolean animate) {
        if((null != this.currentCamera) && (0 == oCamera.getGeoId().compareTo(this.currentCamera.getGeoId()))) {
            this.moveCamera(oCamera, animate);
        }
    }

    /**
     * Application will invoke this so that it can change the setting and also add a listener. After changing the setting application
     * can execute apply on the object.
     * @return
     */
    public ILookAt getLookAt() {
        return this.currentLookAt;
    }

    /**
     * Application has created a new LookAt object that should become currentLookAt. Application can do a getLookAt and use that
     * object in the setLookAt, but that shouldn't stop setting the currentLookAt again.
     * NOTE that there is no check for null value for the oLookAt anywhere in Map or CoreManager.
     * @param oLookAt
     * @param animate
     */
    public void setLookAt(ILookAt oLookAt, final boolean animate) {
        this.currentLookAt = oLookAt;

        if(null == this.currentLookAt) {
            if(null != this.currentCamera) {
                this.moveCamera(this.currentCamera, animate);
            } else {
                this.currentCamera = new mil.emp3.api.Camera();
                gov.nasa.worldwind.geom.Camera wwCamera = new gov.nasa.worldwind.geom.Camera();
                Emp3NavigationListener.this.ww.getNavigator().getAsCamera(this.ww.getGlobe(), wwCamera);
                updateCamera(this.currentCamera, wwCamera);
                moveCamera(this.currentCamera, animate);
            }
        } else {
            this.moveLookAt(oLookAt, animate);
        }
    }

    /**
     * If application intention is to update the currentLookAt then application needs to execute IMap.getLookAt, change the settings
     * and the issue apply on the object.
     * @param oLookAt
     * @param animate
     */
    public void applyLookAtChange(ILookAt oLookAt, boolean animate) {
        if((null != this.currentLookAt) && (0 == oLookAt.getGeoId().compareTo(this.currentLookAt.getGeoId()))) {
            this.moveLookAt(oLookAt, animate);
        }
    }

    private void moveCameraEx(ICamera oCamera, boolean animate) {
        int iAltitudeMode = WorldWind.ABSOLUTE;

        switch (oCamera.getAltitudeMode()) {
            case CLAMP_TO_GROUND:
                iAltitudeMode = WorldWind.CLAMP_TO_GROUND;
                break;
            case ABSOLUTE:
                break;
            case RELATIVE_TO_GROUND:
                iAltitudeMode = WorldWind.RELATIVE_TO_GROUND;
                break;
        }
        gov.nasa.worldwind.geom.Camera oWWCamera = new gov.nasa.worldwind.geom.Camera();
        Log.d(TAG, "Setting NASA Camera.");
        oWWCamera.set(oCamera.getLatitude(), oCamera.getLongitude(), oCamera.getAltitude(), iAltitudeMode, oCamera.getHeading(), oCamera.getTilt(), oCamera.getRoll());
        Emp3NavigationListener.this.ww.getNavigator().setAsCamera(Emp3NavigationListener.this.ww.getGlobe(), oWWCamera);
        Emp3NavigationListener.this.ww.requestRedraw();
    }

    public void moveCamera(final ICamera oCamera, final boolean animate) {
        if (!SystemUtils.isCurrentThreadUIThread()) {
            this.ww.post(new Runnable(){
                @Override
                public void run() {
                    Emp3NavigationListener.this.moveCameraEx(oCamera, animate);
                }
            });
        } else {
            this.moveCameraEx(oCamera, animate);
        }
    }

    private void moveLookAtEx(ILookAt oLookAt, final boolean animate) {
        int iAltitudeMode = WorldWind.ABSOLUTE;
        bLookAtSet = true;

        switch (oLookAt.getAltitudeMode()) {
            case CLAMP_TO_GROUND:
                iAltitudeMode = WorldWind.CLAMP_TO_GROUND;
                break;
            case ABSOLUTE:
                break;
            case RELATIVE_TO_GROUND:
                iAltitudeMode = WorldWind.RELATIVE_TO_GROUND;
                break;
        }
        gov.nasa.worldwind.geom.LookAt oWWLookAt = new gov.nasa.worldwind.geom.LookAt();
        Log.d(TAG, "Setting NASA LookAt.");
        oWWLookAt.set(oLookAt.getLatitude(), oLookAt.getLongitude(), oLookAt.getAltitude(), iAltitudeMode, oLookAt.getRange(),
                oLookAt.getHeading(), oLookAt.getTilt(), 0.0);
        Emp3NavigationListener.this.ww.getNavigator().setAsLookAt(Emp3NavigationListener.this.ww.getGlobe(), oWWLookAt);
        Emp3NavigationListener.this.ww.requestRedraw();
    }

    public void moveLookAt(final ILookAt oLookAt, final boolean animate) {
        if (!SystemUtils.isCurrentThreadUIThread()) {
            this.ww.post(new Runnable(){
                @Override
                public void run() {
                    Emp3NavigationListener.this.moveLookAtEx(oLookAt, animate);
                }
            });
        } else {
            this.moveLookAtEx(oLookAt, animate);

        }
    }

    public IGeoBounds getBounds() {
        IGeoBounds oBounds = new GeoBounds();
        Navigator oNav = this.ww.getNavigator();
        double dAlt = oNav.getAltitude();
        Location oCenterLatLon = Location.fromDegrees(oNav.getLatitude(), oNav.getLongitude());
        double metersPerPixel = this.ww.pixelSizeAtDistance(dAlt);
        int mapWidth = this.ww.getWidth();
        int mapHeight = this.ww.getHeight();
        double dRadius = this.ww.getGlobe().getRadiusAt(oNav.getLatitude(), oNav.getLongitude());
        double verticalHalf = mapHeight / 2.0 * metersPerPixel / dRadius;
        double horizontalHalf = mapWidth / 2.0 * metersPerPixel / dRadius;

        Location oLatLon = new Location();
        oCenterLatLon.greatCircleLocation(90.0, horizontalHalf, oLatLon);
        oBounds.setEast(Location.normalizeLongitude(oLatLon.longitude));

        oCenterLatLon.greatCircleLocation(-90.0, horizontalHalf, oLatLon);
        oBounds.setWest(Location.normalizeLongitude(oLatLon.longitude));

        oCenterLatLon.greatCircleLocation(0.0, verticalHalf, oLatLon);
        oBounds.setNorth(Location.normalizeLatitude(oLatLon.latitude));

        oCenterLatLon.greatCircleLocation(180.0, verticalHalf, oLatLon);
        oBounds.setSouth(Location.normalizeLatitude(oLatLon.latitude));

        return oBounds;
    }

    private void generateViewChangeEvent(MapViewEventEnum eEvent, gov.nasa.worldwind.geom.Camera oWWCamera) {
        Log.d(TAG, "   View Change event sent " + eEvent.name());
        WorldWindow wwd = this.ww.getWorldWindowController().getWorldWindow();
        this.updateCamera(this.currentCamera, oWWCamera);
        IGeoBounds oBounds = this.getBounds();
        MapInstanceViewChangeEvent oEvent = new MapInstanceViewChangeEvent(this.mapInstance, eEvent,
                this.currentCamera, this.currentLookAt, oBounds, wwd.getWidth(), wwd.getHeight());
        this.mapInstance.generateViewChangeEvent(oEvent);
    }

    @Override
    public void onNavigatorEvent(WorldWindow worldWindow, NavigatorEvent navigatorEvent) {
        MapViewEventEnum eEvent;
        gov.nasa.worldwind.geom.Camera oWWCamera = new gov.nasa.worldwind.geom.Camera();
        gov.nasa.worldwind.geom.LookAt oWWLookAt = new gov.nasa.worldwind.geom.LookAt();

        this.ww.getNavigator().getAsCamera(this.ww.getGlobe(), oWWCamera);
        this.ww.getNavigator().getAsLookAt(this.ww.getGlobe(), oWWLookAt);

        this.mapInstance.updateMiniMapCamera();

        switch (navigatorEvent.getAction()) {
            case WorldWind.NAVIGATOR_MOVED:
                //Log.d(TAG, "Navigation Moved.");
                eEvent = MapViewEventEnum.VIEW_IN_MOTION;

                // TODO If we decide to go into LookAt mode and stay there until setCamera or applyCamera then following is
                //   required to stop event triggering going into infinite motion as we recalculate and rest LookAt on the Map
//                if(bLookAtUpdated) return;
                break;
            case WorldWind.NAVIGATOR_STOPPED:
                //Log.d(TAG, "Navigation Stopped.");
                eEvent = MapViewEventEnum.VIEW_MOTION_STOPPED;
                this.mapInstance.reRenderMPTacticalGraphics();
                // TODO If we decide to go into LookAt mode and stay there until setCamera or applyCamera then following is
                //   required to stop event triggering going into infinite motion as we recalculate and rest LookAt on the Map
//                if(bLookAtUpdated) {
//                    bLookAtUpdated = false;
//                    return;
//                }
                break;
            default:
                return;
        }

        this.oEventProcessingThread.queueEvent(eEvent, oWWCamera, oWWLookAt);
        if (bLookAtSet && navigatorEvent.getAction() == WorldWind.NAVIGATOR_STOPPED) {
            // TODO Uncommenting following line had no effect on the Map Behavior, this needs further discussion
            // Emp3NavigationListener.this.ww.getNavigator().setAsCamera(Emp3NavigationListener.this.ww.getGlobe(), oWWCamera);
            bLookAtSet = false;
        }
    }
}
