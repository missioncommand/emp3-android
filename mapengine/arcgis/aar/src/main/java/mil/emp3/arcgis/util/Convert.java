package mil.emp3.arcgis.util;

import com.esri.core.geometry.Point;
import com.esri.core.geometry.GeometryEngine;
import com.esri.android.map.MapView;

/**
 *
 */
public class Convert {
    public static Point latlonToPoint(double dLat, double dLon, MapView mapView) {
        Point mapPoint;
        try {
            mapPoint = GeometryEngine.project(dLon, dLat, mapView.getSpatialReference());
        } catch (Exception Ex) {
            mapPoint = new com.esri.core.geometry.Point(0,0);
        }
        return mapPoint;    
    }
}
