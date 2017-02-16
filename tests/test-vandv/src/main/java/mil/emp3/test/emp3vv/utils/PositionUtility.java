package mil.emp3.test.emp3vv.utils;


import android.util.Log;

import org.cmapi.primitives.GeoPosition;
import org.cmapi.primitives.IGeoPosition;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import mil.emp3.api.events.MapUserInteractionEvent;
import mil.emp3.api.exceptions.EMP_Exception;
import mil.emp3.api.interfaces.IMap;
import mil.emp3.api.listeners.EventListenerHandle;
import mil.emp3.api.listeners.IMapInteractionEventListener;

public class PositionUtility implements IMapInteractionEventListener {
    private static String TAG = PositionUtility.class.getSimpleName();
    private EventListenerHandle eventListenerHandle;
    private double currentLatitude;
    private double currentLongitude;
    final private IMap map;
    final private IPositionChangedListener listener;
    final private boolean collectMultiplePositions;
    final private List<IGeoPosition> multiplePositions;
    public interface IPositionChangedListener {
        void newPosition(IGeoPosition geoPosition, String stringPosition);
    }

    public PositionUtility(IMap map, IPositionChangedListener listener, boolean collectMultiplePositions) throws EMP_Exception {
        if((null == map) || (null == listener)) {
            throw new IllegalArgumentException("map and listener must be non-null");
        }
        this.map = map;
        this.listener = listener;
        this.collectMultiplePositions = collectMultiplePositions;
        if(collectMultiplePositions) {
            multiplePositions = new ArrayList<>();
        } else {
            multiplePositions = null;
        }

        currentLatitude = map.getCamera().getLatitude();
        currentLongitude = map.getCamera().getLongitude();
        listener.newPosition(getPosition(), getFormatedPosition());
        eventListenerHandle = map.addMapInteractionEventListener(this);
    }

    public IGeoPosition getPosition() {
        IGeoPosition position = new GeoPosition();
        position.setLatitude(currentLatitude);
        position.setLongitude(currentLongitude);
        return position;
    }

    public List<IGeoPosition> getPositionList() {
        List<IGeoPosition> positionList = new ArrayList<>();
        if(null == multiplePositions) {
            positionList.add(getPosition());
        } else {
            positionList.addAll(multiplePositions);
        }
        return positionList;
    }

    public String getFormatedPosition() {
        return String.format(Locale.US, "Lat: %1$6.6f Lon: %2$6.6f", currentLatitude, currentLongitude);
    }

    public void stop() {
        map.removeEventListener(eventListenerHandle);
    }

    @Override
    public void onEvent(MapUserInteractionEvent event) {
        Log.d(TAG, "onEvent " + event.getEvent().name() + " on " + event.getTarget().getName() + " at " + event.getCoordinate().getLatitude() + "/" +
                event.getCoordinate().getLongitude());
        currentLatitude = event.getCoordinate().getLatitude();
        currentLongitude = event.getCoordinate().getLongitude();
        IGeoPosition lastPosition = getPosition();
        listener.newPosition(lastPosition, getFormatedPosition());
        if(null != multiplePositions) {
            multiplePositions.add(lastPosition);
        }
    }
}
