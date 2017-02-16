package mil.emp3.test.emp3vv.containers;

import android.app.Activity;
import android.os.Handler;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;

import org.cmapi.primitives.IGeoAltitudeMode;
import org.cmapi.primitives.IGeoFillStyle;
import org.cmapi.primitives.IGeoLabelStyle;
import org.cmapi.primitives.IGeoStrokeStyle;

import java.util.List;

import mil.emp3.api.Text;
import mil.emp3.api.exceptions.EMP_Exception;
import mil.emp3.api.interfaces.IFeature;
import mil.emp3.api.interfaces.IMap;
import mil.emp3.api.interfaces.IOverlay;
import mil.emp3.test.emp3vv.common.StyleManager;
import mil.emp3.test.emp3vv.containers.dialogs.FeaturePropertiesDialog;

public abstract class AddEntityBase {

    private static String TAG = AddEntityBase.class.getSimpleName();
    final protected IStatusListener statusListener;
    final protected IMap map;
    final protected Activity activity;

    final protected StyleManager styleManager;

    protected AddEntityBase(Activity activity, IMap map, IStatusListener statusListener, StyleManager styleManager) {
        if((null == activity) || (null == map) || (null == statusListener)) {
            throw new IllegalArgumentException("activity, map and statusListener must be non-null");
        }
        this.map = map;
        this.statusListener = statusListener;
        this.activity = activity;

        this.styleManager = styleManager;
    }

    protected String showPropertiesDialog(final FeaturePropertiesDialog featurePropertiesDialog, final String fragmentName) {
        Handler mainHandler = new Handler(activity.getMainLooper());

        Runnable myRunnable = new Runnable() {
            @Override
            public void run() {
                FragmentManager fm = ((AppCompatActivity)activity).getSupportFragmentManager();
                featurePropertiesDialog.show(fm, fragmentName);
            }
        };
        mainHandler.post(myRunnable);
        return fragmentName;
    }

    public String showPropertiesDialog(final List<String> parentList, final String featureName, final boolean visible) {
        throw new IllegalStateException("showPropertiesDialog(final List<String> parentList, final String featureName, final boolean visible) must be implemented");
    }

    protected void addFeature2Map(IMap map, List<String> parentList, boolean isFeatureVisible, IFeature newFeature) throws EMP_Exception {
        for (IOverlay overlay : map.getAllOverlays()) {
            if (parentList.contains(overlay.getName())) {
                overlay.addFeature(newFeature, isFeatureVisible);
            }
        }

        for (IFeature feature : map.getAllFeatures()) {
            if (parentList.contains(feature.getName())) {
                feature.addFeature(newFeature, isFeatureVisible);
            }
        }
    }

    protected void applyStyle(IFeature newFeature) {
        if(null != styleManager) {
            styleManager.setStyles(newFeature);
        }
    }
    protected void addFeature(IFeature newFeature, FeaturePropertiesDialog dialog) throws EMP_Exception {
        dialog.setPosition(newFeature);
        newFeature.setAltitudeMode(IGeoAltitudeMode.AltitudeMode.CLAMP_TO_GROUND);
        if(!(newFeature instanceof Text)) { // Don't overwrite the Text use entered
            newFeature.setName(dialog.getFeatureName());
        }
        applyStyle(newFeature);
        addFeature2Map(map, dialog.getParentList(), dialog.isFeatureVisible(), newFeature);
    }
}
