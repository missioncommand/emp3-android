package mil.emp3.openstreet;


import android.util.Log;

import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.ItemizedIconOverlay;
import org.osmdroid.views.overlay.OverlayItem;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Created by deepakkarmarkar on 3/30/2016.
 */
public class StateManager {

    private String TAG = StateManager.class.getCanonicalName();

    private static StateManager instance;
    MapView mapView;
    Map<UUID, OverlayItem> itemsOnOverlay = new HashMap<>();
    ItemizedIconOverlay overlay;

    public static StateManager getInstance() {
        if(null == instance) {
            synchronized(StateManager.class) {
                if(null == instance) {
                    instance = new StateManager();
                }
            }
        }
        return instance;
    }

    private StateManager() { }

    public MapView getMapView() {
        return mapView;
    }

    public void setMapView(MapView mapView) {
        this.mapView = mapView;
    }

    public Map<UUID, OverlayItem> getItemsOnOverlay() {
        return itemsOnOverlay;
    }

    public ItemizedIconOverlay getOverlay() {
        return overlay;
    }

    public void setOverlay(ItemizedIconOverlay overlay) {
        this.overlay = overlay;
    }

    public void updateGraphicsLayer() {
        Log.d(TAG, "updateGraphicsLayer " + itemsOnOverlay.values().size());
        overlay.removeAllItems();

        for(OverlayItem overlayItem: itemsOnOverlay.values()) {
            overlay.addItem(overlayItem);
        }
        mapView.getOverlays().clear();
        mapView.getOverlays().add(overlay);
        mapView.invalidate();
    }

    public void remove(UUID uuid) {
        StateManager sm = StateManager.getInstance();
        OverlayItem item = sm.getItemsOnOverlay().get(uuid);
        if(null != item) {
            sm.getItemsOnOverlay().remove(uuid);
            sm.updateGraphicsLayer();
        }
    }
}
