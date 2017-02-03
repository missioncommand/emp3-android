package mil.emp3.openstreet.plotters;

import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.overlay.OverlayItem;

import mil.emp3.api.MilStdSymbol;
import mil.emp3.openstreet.StateManager;

/**
 * Created by deepakkarmarkar on 6/17/2016.
 */
public class MilStdSymbolRenderer extends Renderer<MilStdSymbol> {
    private static String TAG = MilStdSymbolRenderer.class.getSimpleName();
    private static MilStdSymbolRenderer instance;
    public static MilStdSymbolRenderer getInstance() {
        if(null == instance) {
            synchronized(PointRenderer.class) {
                if(null == instance) {
                    instance = new MilStdSymbolRenderer();
                }
            }
        }
        return instance;
    }
    @Override
    public void render(MilStdSymbol emp3MilStdSymbol) {
        StateManager sm = StateManager.getInstance();

        OverlayItem item = sm.getItemsOnOverlay().get(emp3MilStdSymbol.getGeoId());
        if(null != item) {
            sm.getItemsOnOverlay().remove(emp3MilStdSymbol.getGeoId());
        }
        item = new OverlayItem("title", "snippet",
                new GeoPoint(emp3MilStdSymbol.getPositions().get(0).getLatitude(), emp3MilStdSymbol.getPositions().get(0).getLongitude()));
        sm.getItemsOnOverlay().put(emp3MilStdSymbol.getGeoId(), item);

        sm.updateGraphicsLayer();
    }
}
