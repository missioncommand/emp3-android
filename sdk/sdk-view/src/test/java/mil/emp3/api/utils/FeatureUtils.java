package mil.emp3.api.utils;

import org.cmapi.primitives.GeoFillStyle;
import org.cmapi.primitives.GeoStrokeStyle;
import org.cmapi.primitives.IGeoAltitudeMode;
import org.cmapi.primitives.IGeoColor;
import org.cmapi.primitives.IGeoFillStyle;
import org.cmapi.primitives.IGeoStrokeStyle;

import mil.emp3.api.Polygon;

/**
 * Created by deepakkarmarkar on 9/30/2016.
 */
public class FeatureUtils {
    /**
     * Builds a basic polygon with graphic properties.
     * @return
     */
    public static Polygon setupDrawPolygon() {
        IGeoStrokeStyle strokeStyle = new GeoStrokeStyle();
        IGeoFillStyle fillStyle = new GeoFillStyle();
        IGeoColor geoColor = new EmpGeoColor(1.0, 0, 255, 255);
        IGeoColor geoFillColor = new EmpGeoColor(0.7, 0, 0, 255);
        Polygon polygon = new Polygon();

        strokeStyle.setStrokeColor(geoColor);
        strokeStyle.setStrokeWidth(5);
        strokeStyle.setStrokePattern(IGeoStrokeStyle.StrokePattern.dotted);
        polygon.setStrokeStyle(strokeStyle);

        fillStyle.setFillColor(geoFillColor);
        fillStyle.setFillPattern(IGeoFillStyle.FillPattern.hatched);
        polygon.setFillStyle(fillStyle);

        polygon.setAltitudeMode(IGeoAltitudeMode.AltitudeMode.CLAMP_TO_GROUND);

        return polygon;
    }
}
