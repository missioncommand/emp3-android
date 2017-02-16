package mil.emp3.test.emp3vv.common;

import android.app.Activity;
import android.os.Handler;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import org.cmapi.primitives.GeoRenderable;
import org.cmapi.primitives.IGeoFillStyle;
import org.cmapi.primitives.IGeoLabelStyle;
import org.cmapi.primitives.IGeoRenderable;
import org.cmapi.primitives.IGeoStrokeStyle;

import mil.emp3.api.interfaces.IFeature;
import mil.emp3.api.interfaces.IMap;
import mil.emp3.test.emp3vv.dialogs.FillStyleDialog;
import mil.emp3.test.emp3vv.dialogs.LabelStyleDialog;
import mil.emp3.test.emp3vv.dialogs.StrokeStyleDialog;
import mil.emp3.test.emp3vv.dialogs.utils.ErrorDialog;

/**
 * EMP SDK allows for three distinct Style objects, namely StrokeStyle, LabelStyle and FillStyle. Default styles are
 * built into the CMAPI GeoRenderable object. The StyleManager allows the users to change these styles.
 *
 * TODO Alow for setting of defaults withing the style object in future.
 */
public class StyleManager implements LabelStyleDialog.ILabelStyleDialogListener,
        StrokeStyleDialog.IStrokeStyleDialogListener, FillStyleDialog.IFillStyleDialogListener {
    private static String TAG = StyleManager.class.getSimpleName();
    private final Activity activity;
    private final IMap maps[];
    private final IGeoRenderable defaults = new GeoRenderable();

    IGeoFillStyle[] fillStyle = new IGeoFillStyle[ExecuteTest.MAX_MAPS];
    IGeoLabelStyle[] labelStyle = new IGeoLabelStyle[ExecuteTest.MAX_MAPS];
    IGeoStrokeStyle[] strokeStyle = new IGeoStrokeStyle[ExecuteTest.MAX_MAPS];

    public StyleManager(Activity activity, IMap maps[]) {
        this.activity = activity;
        this.maps = maps;
    }

    /**
     * Append Style related actions to application actions passed in the actions array
     * @param actions supported by application
     * @return
     */
    public String[] getMoreActions(String[] actions) {
        String[] styleActions = {
                "Set Label Style", "Set Stroke Style", "Set Fill Style",
                "Default Label Style", "Default Stroke Style", "Default Fill Style", "Apply Styles"};

        String combined[] = new String[actions.length + styleActions.length];
        System.arraycopy(actions, 0, combined, 0, actions.length);
        System.arraycopy(styleActions, 0, combined, actions.length, styleActions.length);
        return combined;
    }

    /**
     * Invoked by FillStyleDialog
     * @param fillStyleDialog
     */
    @Override
    public void set(FillStyleDialog fillStyleDialog) {
        fillStyle[ExecuteTest.getCurrentMap()] = fillStyleDialog.getFillStyle();
        Log.d(TAG, "fill pattern " + fillStyleDialog.getFillStyle().getFillPattern().toString());
    }

    /**
     * Invoked by LabelStyleDialog
     * @param labelStyleDialog
     */
    @Override
    public void setLabelStyle(LabelStyleDialog labelStyleDialog) {
        labelStyle[ExecuteTest.getCurrentMap()] = labelStyleDialog.getLabelStyle();
    }

    /**
     * Invoked by StrokeStyleDialog
     * @param strokeStyleDialog
     */
    @Override
    public void setStrokeStyle(StrokeStyleDialog strokeStyleDialog) {
        strokeStyle[ExecuteTest.getCurrentMap()] = strokeStyleDialog.getStrokeStyle();
    }

    /**
     * Execute the user selected action.
     * @param userAction
     * @return
     */
    public boolean actOn(String userAction) {
        final int whichMap = ExecuteTest.getCurrentMap();

        try {
            if (userAction.equals("Set Label Style")) {
                Handler mainHandler = new Handler(activity.getMainLooper());
                Runnable myRunnable = new Runnable() {
                    @Override
                    public void run() {
                        FragmentManager fm = ((AppCompatActivity) activity).getSupportFragmentManager();
                        LabelStyleDialog labelStyleDialog = LabelStyleDialog.newInstance("Label Style", StyleManager.this, maps[whichMap], labelStyle[ExecuteTest.getCurrentMap()]);
                        labelStyleDialog.show(fm, "fragment_label_style_dialog");
                    }
                };
                mainHandler.post(myRunnable);
            } else if (userAction.equals("Set Stroke Style")) {
                Handler mainHandler = new Handler(activity.getMainLooper());
                Runnable myRunnable = new Runnable() {
                    @Override
                    public void run() {
                        FragmentManager fm = ((AppCompatActivity) activity).getSupportFragmentManager();
                        StrokeStyleDialog strokeStyleDialog = StrokeStyleDialog.newInstance("Label Style", StyleManager.this, maps[whichMap], strokeStyle[ExecuteTest.getCurrentMap()]);
                        strokeStyleDialog.show(fm, "fragment_stroke_style_dialog");
                    }
                };
                mainHandler.post(myRunnable);
            } else if (userAction.equals("Set Fill Style")) {
                Handler mainHandler = new Handler(activity.getMainLooper());
                Runnable myRunnable = new Runnable() {
                    @Override
                    public void run() {
                        FragmentManager fm = ((AppCompatActivity) activity).getSupportFragmentManager();
                        FillStyleDialog fillStyleDialog = FillStyleDialog.newInstance("Label Style", StyleManager.this, maps[whichMap], fillStyle[ExecuteTest.getCurrentMap()]);
                        fillStyleDialog.show(fm, "fragment_fill_style_dialog");
                    }
                };
                mainHandler.post(myRunnable);
            } else if (userAction.equals("Default Label Style")) {
                labelStyle[ExecuteTest.getCurrentMap()] = null;
            } else if (userAction.equals("Default Stroke Style")) {
                strokeStyle[ExecuteTest.getCurrentMap()] = null;
            } else if (userAction.equals("Default Fill Style")) {
                fillStyle[ExecuteTest.getCurrentMap()] = null;
            } else if (userAction.equals("Apply Styles")) {
                for(IFeature f : maps[whichMap].getAllFeatures()) {
                    applyStyles(f);
                    f.apply();
                }
            }
            else {
                return false;
            }
        } catch (Exception e) {
            ErrorDialog.showError(activity, "Style Manager Failed");
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * Override the style only if user has set a style, DO NOT make the style null.
     * @param feature
     */
    public void setStyles(IFeature feature) {
        if(null != fillStyle[ExecuteTest.getCurrentMap()]) {
            feature.setFillStyle(fillStyle[ExecuteTest.getCurrentMap()]);
        }

        if(null != strokeStyle[ExecuteTest.getCurrentMap()]) {
            feature.setStrokeStyle(strokeStyle[ExecuteTest.getCurrentMap()]);
        }

        if(null != labelStyle[ExecuteTest.getCurrentMap()]) {
            feature.setLabelStyle(labelStyle[ExecuteTest.getCurrentMap()]);
        }
    }

    /**
     * Applies all styles to all features on the map. Note that all styles don't necessarily impact
     * all the features.
     * @param feature
     */
    public void applyStyles(IFeature feature) {
        if(null != fillStyle[ExecuteTest.getCurrentMap()]) {
            feature.setFillStyle(fillStyle[ExecuteTest.getCurrentMap()]);
        } else {
            feature.setFillStyle(defaults.getFillStyle());
        }

        if(null != strokeStyle[ExecuteTest.getCurrentMap()]) {
            feature.setStrokeStyle(strokeStyle[ExecuteTest.getCurrentMap()]);
        } else {
            feature.setStrokeStyle(defaults.getStrokeStyle());
        }

        if(null != labelStyle[ExecuteTest.getCurrentMap()]) {
            feature.setLabelStyle(labelStyle[ExecuteTest.getCurrentMap()]);
        } else {
            feature.setLabelStyle(defaults.getLabelStyle());
        }
    }
}
