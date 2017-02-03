package mil.emp3.openstreet;

import org.osmdroid.views.MapController;
import org.osmdroid.views.MapView;

import mil.emp3.api.enums.EventListenerTypeEnum;
import mil.emp3.api.listeners.EventListenerHandle;
import mil.emp3.api.listeners.IEventListener;
import mil.emp3.mapengine.interfaces.IMapInstance;
import mil.emp3.mapengine.listeners.MapInstanceUserInteractionEventListener;


/**
 * Created by deepakkarmarkar on 3/7/2016.
 */
public class EventManager {
    private final static String TAG = EventManager.class.getSimpleName();

    MapView mapView;
    MapController mapController;
    IMapInstance mapInstance;
    IEventListener userInteractionEventListener;

    EventManager(IMapInstance mapInstance, MapView mapView, MapController mapController) {
        this.mapView = mapView;
        this.mapController = mapController;
        this.mapInstance = mapInstance;
    }

    private class EngineEventListenerHandle extends EventListenerHandle {

        EventListenerTypeEnum listenerType;
        IEventListener listener;

        EngineEventListenerHandle(EventListenerTypeEnum listenerType, IEventListener listener) {
            this.listenerType = listenerType;
            this.listener = listener;
        }
        @Override
        public EventListenerTypeEnum getListenerType() {
            return listenerType;
        }

        @Override
        public IEventListener getListener() {
            return listener;
        }
    }

    public EventListenerHandle addMapInstanceUserInteractionEventListener(MapInstanceUserInteractionEventListener listener) {
       /* userInteractionEventListener = listener;
        EventListenerHandle handle = new EngineEventListenerHandle(EventListenerTypeEnum.MAP_INTERACTION_EVENT_LISTENER, listener);
        registerListeners();  // THIS TEMP
        return handle;*/
        return null;
    }

    // OSM Event registration and handling

    private void registerListeners() {


       /* mapView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int eventAction = event.getAction();
                UserInteractionEventEnum interaction;
                IGeoPosition geoPosition = new GeoPosition();

                int x_co = Math.round(event.getX());
                int y_co = Math.round(event.getX());

                System.err.println("Coordinates " + x_co + "-" + y_co);
                org.osmdroid.api.IGeoPoint geoPoint = mapView.getProjection().fromPixels(x_co, y_co);
                geoPosition.setLatitude((float) geoPoint.getLatitude());
                geoPosition.setLongitude((float) geoPoint.getLongitude());

                switch(eventAction) {
                    case MotionEvent.ACTION_DOWN:
                        interaction = UserInteractionEventEnum.ACTION_DOWN;
                        break;
                    case MotionEvent.ACTION_UP:
                        interaction = UserInteractionEventEnum.ACTION_UP;
                        break;
                    case MotionEvent.ACTION_MOVE:
                        interaction = UserInteractionEventEnum.ACTION_MOVE;
                        break;
                    default :
                        interaction = UserInteractionEventEnum.ACTION_MOVE;
                }
                MapInstanceUserInteractionEvent uiEvent = new MapInstanceUserInteractionEvent(mapInstance, interaction, new Point(x_co, y_co), geoPosition);

                if(null != userInteractionEventListener) {
                    userInteractionEventListener.onEvent(uiEvent);
                    return true;
                } else
                    return false;
            }
        });*/
    }
}
