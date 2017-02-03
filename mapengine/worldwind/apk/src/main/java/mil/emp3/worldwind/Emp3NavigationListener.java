package mil.emp3.worldwind;

import android.util.Log;
import gov.nasa.worldwind.Navigator;
import gov.nasa.worldwind.NavigatorEvent;
import gov.nasa.worldwind.NavigatorListener;
import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.geom.Location;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import mil.emp3.api.enums.MapViewEventEnum;
import mil.emp3.api.events.CameraEvent;
import mil.emp3.api.events.LookAtEvent;
import mil.emp3.api.exceptions.EMP_Exception;
import mil.emp3.api.interfaces.ICamera;
import mil.emp3.api.interfaces.ILookAt;
import mil.emp3.api.listeners.EventListenerHandle;
import mil.emp3.api.listeners.ICameraEventListener;
import mil.emp3.api.listeners.ILookAtEventListener;
import mil.emp3.mapengine.events.MapInstanceViewChangeEvent;

import org.cmapi.primitives.GeoBounds;
import org.cmapi.primitives.IGeoAltitudeMode;
import org.cmapi.primitives.IGeoBounds;

import mil.emp3.worldwind.utils.SystemUtils;

public class Emp3NavigationListener implements NavigatorListener {
    final static private String TAG = Emp3NavigationListener.class.getSimpleName();

    // The current Camera.
    private ICamera currentCamera;
    private ILookAt currentLookAt;
    private EventListenerHandle hCameraListener = null;
    private EventListenerHandle hLookAtListener = null;
    // The bMapChangedCamera is true when the map is the one that changes the camera.
    // This allows the map to ignore the camera event.
    private boolean bMapChangedLookAt = false;
    private boolean bMapChangedCamera = false;
    private boolean bLookAtSet = false;

    protected final WorldWindow ww;
    protected final MapInstance mapInstance;
    private final Object oSyncObject = new Object();

    private class EventQueueItem {
        private final MapViewEventEnum eEvent;
        private final gov.nasa.worldwind.geom.Camera oWWCamera;
        private final gov.nasa.worldwind.geom.LookAt oWWLookAt;

        public EventQueueItem(MapViewEventEnum eventEnum, gov.nasa.worldwind.geom.Camera camera,
                              gov.nasa.worldwind.geom.LookAt lookAt) {
            this.eEvent = eventEnum;
            this.oWWCamera = camera;
            this.oWWLookAt = lookAt;
        }

        public MapViewEventEnum getEvent() {
            return this.eEvent;
        }

        public gov.nasa.worldwind.geom.Camera getCamera() {
            return this.oWWCamera;
        }
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
            // alternatating series of IN_MOTION, MOTION_STOPPED events. However I assume that the 
            // probability of it happening is very low. If the assumption is found to be incorrect
            // changes must be made.
            boolean bTimedWait = false;
            long nextEventTime = System.currentTimeMillis() + this.iEventWaitDelay;
            long lWaitTime;
            EventQueueItem oQueueItem = null;
            while (!Thread.interrupted()) {
                try {
                    if (bTimedWait) {
                        lWaitTime = nextEventTime - System.currentTimeMillis();
                        oQueueItem = this.qEventEnumQueue.poll(lWaitTime, TimeUnit.MILLISECONDS);
                        if ((oQueueItem == null) && (this.oWaitingQueueItem != null)) {
                            // We timed out
                            // Lets process the waiting event.
                            //Log.d(TAG, "  Timed out " + this.eWaitingEvent.name());
                            this.oCameraHandler.generateViewChangeEvent(this.oWaitingQueueItem.getEvent(), this.oWaitingQueueItem.getCamera());
                            this.ePreviousEventSent = this.oWaitingQueueItem.getEvent();
                            nextEventTime = System.currentTimeMillis() + this.iEventWaitDelay;
                            this.oWaitingQueueItem = null;
                        }
                        bTimedWait = false;
                        continue;
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
                        if (this.ePreviousEventSent == MapViewEventEnum.VIEW_IN_MOTION) {
                            // Its the first in motion stopped event. Dont wait just send it.
                            this.oCameraHandler.generateViewChangeEvent(oQueueItem.getEvent(), oQueueItem.getCamera());
                            this.ePreviousEventSent = oQueueItem.getEvent();
                            nextEventTime = System.currentTimeMillis() + this.iEventWaitDelay;
                            bTimedWait = false;
                        } else {
                            // Its another MOTION STOPPED event we wait before we send it.
                            // If another arrives before the wait time expires, we ignore this one.
                            this.oWaitingQueueItem = oQueueItem;
                            bTimedWait = true;
                        }
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
        ICamera oCamera = new mil.emp3.api.Camera();
        ILookAt oLookAt = new mil.emp3.api.LookAt();
        gov.nasa.worldwind.geom.Camera camera = new gov.nasa.worldwind.geom.Camera();
        gov.nasa.worldwind.geom.LookAt lookAt = new gov.nasa.worldwind.geom.LookAt();

        this.oEventProcessingThread = new EventProcessingThread(this);
        this.oEventProcessingThread.start();
        this.ww.getNavigator().getAsCamera(this.ww.getGlobe(), camera);
        this.updateCamera(oCamera, camera);
        this.setCamera(oCamera, false);
        this.ww.getNavigator().getAsLookAt(this.ww.getGlobe(), lookAt);
        this.updateLookAt(oLookAt, lookAt);
        this.setLookAt(oLookAt, false);
    }
    
    public void Destroy() {
        this.oEventProcessingThread.exitThread();
    }

    private void updateCamera(ICamera oCamera, gov.nasa.worldwind.geom.Camera oWWCamera) {
        oCamera.setAltitude(oWWCamera.altitude);
        // For now lets set the mode to absolute.
        oCamera.setAltitudeMode(IGeoAltitudeMode.AltitudeMode.ABSOLUTE);
        oCamera.setHeading(oWWCamera.heading);
        oCamera.setLatitude(oWWCamera.latitude);
        oCamera.setLongitude(oWWCamera.longitude);
        oCamera.setRoll(oWWCamera.roll);
        oCamera.setTilt(oWWCamera.tilt);
    }

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

    public ICamera getCamera() {
        return this.currentCamera;
    }

    public void setCamera(ICamera oCamera, final boolean animate) {
        if (oCamera != this.currentCamera) {
            // Set the event listener.
            if (this.hCameraListener != null) {
                oCamera.removeEventListener(this.hCameraListener);
            }
            try {
                this.hCameraListener = oCamera.addCameraEventListener(new ICameraEventListener() {
                    @Override
                    public void onEvent(CameraEvent event) {
                        if (Emp3NavigationListener.this.currentCamera.getGeoId().compareTo(event.getCamera().getGeoId()) == 0) {
                            if (!bMapChangedCamera) {
                                Emp3NavigationListener.this.moveCamera(event.getCamera(), event.isAnimate());
                            } else {
                                Emp3NavigationListener.this.bMapChangedCamera = false;
                            }
                        }
                    }
                });
            } catch (EMP_Exception e) {
                Log.e(TAG, "addCameraEventListener: " + e.toString());
            }

            this.currentCamera = oCamera;
        }
        this.moveCamera(oCamera, animate);
    }

    public ILookAt getLookAt() {
        return this.currentLookAt;
    }

    public void setLookAt(ILookAt oLookAt, final boolean animate) {
        if (oLookAt != this.currentLookAt) {
            // Set the event listener.
            if (this.hLookAtListener != null) {
                oLookAt.removeEventListener(this.hLookAtListener);
            }
            try {
                this.hLookAtListener = oLookAt.addLookAtEventListener(new ILookAtEventListener() {
                    @Override
                    public void onEvent(LookAtEvent event) {
                        if (Emp3NavigationListener.this.currentLookAt.getGeoId().compareTo(event.getLookAt().getGeoId()) == 0) {
                            if (!bMapChangedLookAt) {
                                Emp3NavigationListener.this.moveLookAt(event.getLookAt(), event.isAnimate());
                            } else {
                                Emp3NavigationListener.this.bMapChangedLookAt = false;
                            }
                        }
                    }
                });
            } catch (EMP_Exception e) {
                Log.e(TAG, "addLookAtEventListener: " + e.toString());
            }

            this.currentLookAt = oLookAt;
        }
        this.moveLookAt(oLookAt, animate);
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
        double dFOV = this.ww.getFieldOfView();
        double dAlt = oNav.getAltitude();
        double dHeading = oNav.getHeading();
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
        this.bMapChangedCamera = true;
        this.mapInstance.generateViewChangeEvent(oEvent);
    }

    @Override
    public void onNavigatorEvent(WorldWindow worldWindow, NavigatorEvent navigatorEvent) {
        MapViewEventEnum eEvent;
        gov.nasa.worldwind.geom.Camera oWWCamera = new gov.nasa.worldwind.geom.Camera();
        gov.nasa.worldwind.geom.LookAt oWWLookAt = new gov.nasa.worldwind.geom.LookAt();

        this.ww.getNavigator().getAsCamera(this.ww.getGlobe(), oWWCamera);
        this.ww.getNavigator().getAsLookAt(this.ww.getGlobe(), oWWLookAt);

        switch (navigatorEvent.getAction()) {
            case WorldWind.NAVIGATOR_MOVED:
                //Log.d(TAG, "Navigation Moved.");
                eEvent = MapViewEventEnum.VIEW_IN_MOTION;
                break;
            case WorldWind.NAVIGATOR_STOPPED:
                //Log.d(TAG, "Navigation Stopped.");
                eEvent = MapViewEventEnum.VIEW_MOTION_STOPPED;
                this.mapInstance.reRenderMPTacticalGraphics();
                break;
            default:
                return;
        }

        this.oEventProcessingThread.queueEvent(eEvent, oWWCamera, oWWLookAt);
        if (bLookAtSet && navigatorEvent.getAction() == WorldWind.NAVIGATOR_STOPPED) {
            Emp3NavigationListener.this.ww.getNavigator().setAsCamera(Emp3NavigationListener.this.ww.getGlobe(), oWWCamera);
            bLookAtSet = false;
        }
    }
}
