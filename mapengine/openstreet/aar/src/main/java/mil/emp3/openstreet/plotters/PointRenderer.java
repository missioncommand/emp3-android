package mil.emp3.openstreet.plotters;

import android.graphics.Color;
import android.util.Log;

import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.OverlayItem;

import mil.emp3.api.Point;
import mil.emp3.openstreet.StateManager;

/**
 * Created by deepakkarmarkar on 5/4/2016.
 */
public class PointRenderer extends Renderer<Point> {

    private static String TAG = PointRenderer.class.getSimpleName();
    private static PointRenderer instance;
    public static PointRenderer getInstance() {
        if(null == instance) {
            synchronized(PointRenderer.class) {
                if(null == instance) {
                    instance = new PointRenderer();
                }
            }
        }
        return instance;
    }

    @Override
    public void render(Point emp3Point) {
        StateManager sm = StateManager.getInstance();

        OverlayItem item = sm.getItemsOnOverlay().get(emp3Point.getGeoId());
        if(null != item) {
            sm.getItemsOnOverlay().remove(emp3Point.getGeoId());
        }
        item = new OverlayItem("title", "snippet",
                new GeoPoint(emp3Point.getPosition().getLatitude(), emp3Point.getPosition().getLongitude()));
        sm.getItemsOnOverlay().put(emp3Point.getGeoId(), item);

        sm.updateGraphicsLayer();
    }
}
