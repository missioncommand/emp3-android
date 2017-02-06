package mil.emp3.validator.features;

import android.app.FragmentManager;
import android.util.Log;

import org.cmapi.primitives.GeoStrokeStyle;
import org.cmapi.primitives.IGeoAltitudeMode;
import org.cmapi.primitives.IGeoColor;
import org.cmapi.primitives.IGeoStrokeStyle;

import mil.emp3.api.exceptions.EMP_Exception;
import mil.emp3.api.utils.EmpGeoColor;
import mil.emp3.validator.model.ManagedMapFragment;

/**
 * Created by raju on 9/22/2016.
 */

public class LinePath {
    static final private String TAG = LinePath.class.getSimpleName();

    private FragmentManager oFragmentManager;

    public LinePath(FragmentManager fragmentManager) {
        oFragmentManager = fragmentManager;
    }

    public void createPath(ManagedMapFragment currentMap) {
        IGeoStrokeStyle strokeStyle = new GeoStrokeStyle();
        IGeoColor geoColor = new EmpGeoColor(1.0, 0, 255, 255);
        mil.emp3.api.Path linePath = new mil.emp3.api.Path();

        strokeStyle.setStrokeColor(geoColor);
        strokeStyle.setStrokeWidth(5);
        strokeStyle.setStipplingPattern((short) 0xCCCC);
        strokeStyle.setStipplingFactor(4);
        linePath.setStrokeStyle(strokeStyle);
        linePath.setAltitudeMode(IGeoAltitudeMode.AltitudeMode.CLAMP_TO_GROUND);

        try {
            currentMap.get().drawFeature(linePath, new FeatureDrawListener(currentMap,
                    oFragmentManager));
        } catch(EMP_Exception Ex) {
            Log.e(TAG, "Draw path failed.");
            //oItem.setEnabled(true);
        }
    }
}
