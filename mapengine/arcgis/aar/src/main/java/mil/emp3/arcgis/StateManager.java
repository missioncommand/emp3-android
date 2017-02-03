package mil.emp3.arcgis;

import com.esri.android.map.GraphicsLayer;
import com.esri.android.map.MapView;
import com.esri.core.map.Graphic;

import java.util.HashMap;

import java.util.Map;
import java.util.UUID;

/**
 * Created by deepakkarmarkar on 3/30/2016.
 */
public class StateManager {

    private static StateManager instance;
    MapView mapView;
    GraphicsLayer graphicsLayer = null;

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

    public GraphicsLayer getGraphicsLayer() {
        return graphicsLayer;
    }

    public void setGraphicsLayer(GraphicsLayer graphicsLayer) {
        this.graphicsLayer = graphicsLayer;
    }
}
