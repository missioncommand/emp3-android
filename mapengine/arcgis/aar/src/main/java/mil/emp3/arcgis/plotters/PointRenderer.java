package mil.emp3.arcgis.plotters;

import android.graphics.Color;
import android.util.Log;

import com.esri.android.map.MapView;
import com.esri.core.geometry.CoordinateConversion;
import com.esri.core.map.Graphic;
import com.esri.core.symbol.PictureMarkerSymbol;
import com.esri.core.symbol.SimpleMarkerSymbol;

import java.io.IOException;


import java.text.DecimalFormat;

import mil.emp3.api.Point;
import mil.emp3.arcgis.StateManager;
import mil.emp3.arcgis.util.*;

/**
 * Created by deepakkarmarkar on 5/4/2016.
 */
public class PointRenderer implements IRenderer<Point> {

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
    public com.esri.core.map.Graphic buildGraphic(final Point emp3Point) {
        com.esri.core.map.Graphic graphic = null;
        final StateManager sm = StateManager.getInstance();
        final MapView mapView = sm.getMapView();

        Log.d(TAG, "Lat = " + emp3Point.getPosition().getLatitude() + " Lon = " + emp3Point.getPosition().getLongitude());

        //com.esri.core.geometry.Point geometryPoint = CoordinateConversion.decimalDegreesToPoint(latFormatted + " " + lonFormatted, mapView.getSpatialReference());
        com.esri.core.geometry.Point geometryPoint = Convert.latlonToPoint(emp3Point.getPosition().getLatitude(), emp3Point.getPosition().getLongitude(), mapView);
        Log.d(TAG, "geometryPoint " + geometryPoint.getX() + " " + geometryPoint.getY());

        try {
            // Use SimpleMarkerSymbol for now until we figure out a way to set a default URL for Icon.
            // If URL is not locatable then map is crashing, preventing progress.
            // PictureMarkerSymbol icon = new PictureMarkerSymbol();
            // icon.setUrl(emp3Point.getIconURI());
            // graphic = new Graphic(geometryPoint, icon);

            java.util.Map<String, Object> attributes = new java.util.HashMap<>();
            attributes.put("Desc", emp3Point.getDescription());
            graphic = new Graphic(geometryPoint, new SimpleMarkerSymbol(Color.RED, 25, SimpleMarkerSymbol.STYLE.CIRCLE), attributes);
        }
        catch (Exception Ex) {
        }
        
        return graphic;
    }
}
