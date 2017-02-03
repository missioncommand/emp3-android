package mil.emp3.arcgis.plotters;

import android.util.Log;

import com.esri.android.map.MapView;

import mil.emp3.api.Circle;
import mil.emp3.arcgis.StateManager;

/**
 * Created by deepakkarmarkar on 3/31/2016.
 */
/*
public class CircleRenderer implements IRenderer<Circle> {

    private static String TAG = CircleRenderer.class.getSimpleName();
    private static CircleRenderer instance;
    public static CircleRenderer getInstance() {
        if(null == instance) {
            synchronized(CircleRenderer.class) {
                if(null == instance) {
                    instance = new CircleRenderer();
                }
            }
        }
        return instance;
    }

    private CircleRenderer() { }
    @Override
    public void render(Circle circle) {
        StateManager sm = StateManager.getInstance();
        MapView mapView = sm.getMapView();

        Log.d(TAG, "render " + circle.getRadius() + "-" + circle.getPositions());

    }
}
*/
