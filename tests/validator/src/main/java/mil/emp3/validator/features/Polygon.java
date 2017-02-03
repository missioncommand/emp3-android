package mil.emp3.validator.features;

import android.app.FragmentManager;
import android.support.design.widget.FloatingActionButton;
import android.util.Log;

import org.cmapi.primitives.GeoFillStyle;
import org.cmapi.primitives.GeoStrokeStyle;
import org.cmapi.primitives.IGeoAltitudeMode;
import org.cmapi.primitives.IGeoColor;
import org.cmapi.primitives.IGeoFillStyle;
import org.cmapi.primitives.IGeoStrokeStyle;

import mil.emp3.api.exceptions.EMP_Exception;
import mil.emp3.api.utils.EmpGeoColor;
import mil.emp3.validator.model.ManagedMapFragment;

/**
 * Created by raju on 9/21/2016.
 */

public class Polygon {
    static final private String TAG = Polygon.class.getSimpleName();

    private FragmentManager oFragmentManager;

    public Polygon(FragmentManager fragmentManager) {
        oFragmentManager = fragmentManager;
    }

    public void createPolygon(ManagedMapFragment currentMap) {
        IGeoStrokeStyle strokeStyle = new GeoStrokeStyle();
        IGeoFillStyle fillStyle = new GeoFillStyle();
        IGeoColor geoColor = new EmpGeoColor(1.0, 0, 255, 255);
        IGeoColor geoFillColor = new EmpGeoColor(0.7, 0, 0, 255);
        mil.emp3.api.Polygon polygon = new mil.emp3.api.Polygon();

        strokeStyle.setStrokeColor(geoColor);
        strokeStyle.setStrokeWidth(5);
        strokeStyle.setStipplingPattern((short) 0xCCCC);
        strokeStyle.setStipplingFactor(4);
        polygon.setStrokeStyle(strokeStyle);

        fillStyle.setFillColor(geoFillColor);
        fillStyle.setFillPattern(IGeoFillStyle.FillPattern.hatched);
        polygon.setFillStyle(fillStyle);

        polygon.setAltitudeMode(IGeoAltitudeMode.AltitudeMode.CLAMP_TO_GROUND);
        try {
            FeatureDrawListener featureDrawListener = new FeatureDrawListener(currentMap,
                    oFragmentManager);
            currentMap.get().drawFeature(polygon, featureDrawListener);
        } catch(EMP_Exception Ex) {
            Log.e(TAG, "Draw polygon failed.");
        }
    }
}
