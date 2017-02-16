package mil.emp3.test.emp3vv.optItems;

import android.app.Activity;
import android.os.Handler;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import mil.emp3.api.interfaces.IContainer;
import mil.emp3.api.interfaces.IFeature;
import mil.emp3.api.interfaces.IMap;
import mil.emp3.api.interfaces.IOverlay;
import mil.emp3.test.emp3vv.common.ExecuteTest;
import mil.emp3.test.emp3vv.common.OptItemBase;
import mil.emp3.test.emp3vv.navItems.zoom_and_bounds.ZoomToDialog;
import mil.emp3.test.emp3vv.utils.MapNamesUtility;

/**
 * Puts up a dialog with a list of overlays and features. User can either select one overlay or multiple features and zoom in.
 */
public class Locate extends OptItemBase implements ZoomToDialog.IZoomToDialogListener {
    private static String TAG = Locate.class.getSimpleName();

    public Locate(Activity activity, IMap map1, IMap map2) {
        super(activity, map1, map2, TAG, true);
    }

    @Override
    public void run() {
        showLocateDialog(ExecuteTest.getCurrentMap());
    }

    private void showLocateDialog(final int whichMap) {
        Handler mainHandler = new Handler(activity.getMainLooper());

        Runnable myRunnable = new Runnable() {
            @Override
            public void run() {
                FragmentManager fm = ((AppCompatActivity)activity).getSupportFragmentManager();
                ZoomToDialog locateDialogFragment = ZoomToDialog.newInstanceForOptItem("Locate", maps[whichMap], Locate.this);
                locateDialogFragment.show(fm, "fragment_locate_dialog");
            }
        };
        mainHandler.post(myRunnable);
    }


    @Override
    public void zoomToOverlay(ZoomToDialog dialog) {
        IContainer c =  MapNamesUtility.getContainer(dialog.getMap(), dialog.getSelectedOverlay());
        if((null != c) && (c instanceof IOverlay)) {
            dialog.getMap().zoomTo((IOverlay)c, true);
        }
    }

    @Override
    public void zoomToFeatures(ZoomToDialog dialog) {
        List<String> featureNames = dialog.getSelectedFeatures();
        List<IFeature> features = new ArrayList<>();
        if(null != featureNames) {
            for(String featureName: featureNames) {
                IContainer c = MapNamesUtility.getContainer(dialog.getMap(), featureName);
                if(c instanceof IFeature) {
                    features.add((IFeature) c);
                }
            }
        }

        if(features.size() == 0) {
            return;
        } else if(features.size() == 1) {
            dialog.getMap().zoomTo(features.get(0), true);
        } else {
            dialog.getMap().zoomTo(features, true);
        }
    }
}
