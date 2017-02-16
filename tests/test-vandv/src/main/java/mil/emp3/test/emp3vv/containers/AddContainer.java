package mil.emp3.test.emp3vv.containers;

import android.app.Activity;
import android.os.Handler;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import org.cmapi.primitives.IGeoFillStyle;
import org.cmapi.primitives.IGeoLabelStyle;
import org.cmapi.primitives.IGeoStrokeStyle;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import mil.emp3.api.Overlay;
import mil.emp3.api.enums.FeatureTypeEnum;
import mil.emp3.api.exceptions.EMP_Exception;
import mil.emp3.api.interfaces.IMap;
import mil.emp3.api.interfaces.IOverlay;
import mil.emp3.test.emp3vv.common.StyleManager;
import mil.emp3.test.emp3vv.containers.dialogs.AddFeatureDialog;
import mil.emp3.test.emp3vv.containers.dialogs.AddOverlayDialog;
import mil.emp3.test.emp3vv.utils.MapNamesUtility;

/**
 * To enhance the capability tester to add a new type of feature:
 *     create AddNewFeatureFeature class and NewFeaturePropertiesDialog
 *     You can model these after {@link AddEllipseFeature} and {@link mil.emp3.test.emp3vv.containers.dialogs.EllipsePropertiesDialog} classes
 *     update the entityClass table below with feature type and AddNewFeatureFeature class.
 */
public class AddContainer extends AddEntityBase implements AddOverlayDialog.IAddOverlayDialogListener, AddFeatureDialog.IAddFeatureDialogListener {

    private static Map<FeatureTypeEnum, Class<? extends AddEntityBase>> entityClasses;
    static {
        entityClasses = new HashMap<>();
        entityClasses.put(FeatureTypeEnum.GEO_MIL_SYMBOL, AddMilStdSymbol.class);
        entityClasses.put(FeatureTypeEnum.GEO_RECTANGLE, AddRectangleFeature.class);
        entityClasses.put(FeatureTypeEnum.GEO_CIRCLE, AddCircleFeature.class);
        entityClasses.put(FeatureTypeEnum.GEO_ELLIPSE, AddEllipseFeature.class);
        entityClasses.put(FeatureTypeEnum.GEO_TEXT, AddTextFeature.class);
        entityClasses.put(FeatureTypeEnum.GEO_POLYGON, AddPolygonFeature.class);
        entityClasses.put(FeatureTypeEnum.GEO_PATH, AddPathFeature.class);
        entityClasses.put(FeatureTypeEnum.GEO_POINT, AddPointFeature.class);
        entityClasses.put(FeatureTypeEnum.GEO_SQUARE, AddSquareFeature.class);
    }

    private static String TAG = AddContainer.class.getSimpleName();
    
    public static final String addOverlayFragment = "fragment_add_overlay_dialog";
    public static final String addFeatureFragment = "fragment_add_feature_dialog";

    public AddContainer(Activity activity, IMap map, IStatusListener statusListener, StyleManager styleManager) {
        super(activity, map, statusListener, styleManager);
    }
    
    public void showAddOverlayDialog() {
        Handler mainHandler = new Handler(activity.getMainLooper());

        final List<String> parentList = MapNamesUtility.getNames(map, true, true, false);
        final List<String> namesInUse = MapNamesUtility.getNames(map, true, true, true);
        Runnable myRunnable = new Runnable() {
            @Override
            public void run() {
                FragmentManager fm = ((AppCompatActivity)activity).getSupportFragmentManager();
                AddOverlayDialog addOverlayDialogFragment = AddOverlayDialog.newInstance("Add Overlay", map, parentList, AddContainer.this, namesInUse);
                addOverlayDialogFragment.show(fm, addOverlayFragment);
            }
        };
        mainHandler.post(myRunnable);
        return;
        
    }
    @Override
    public boolean overlaySet(AddOverlayDialog dialog) {

        String overlayName = dialog.getOverlayName();
        List<String> parentNameList = dialog.getSelectedParentList();
        boolean visible = dialog.getOverlayVisible();
        IMap map = dialog.getMap();

        boolean overlayAdded = false;
        if((null == overlayName) || (null == parentNameList)) {
            statusListener.updateStatus(TAG, "Overlay Name or Parent Name is NULL");
            return overlayAdded;
        }
        Log.d(TAG, "overlaySet " + overlayName + " " + parentNameList.get(0) + " " + visible + " parentNameList.size " + parentNameList.size());

        IOverlay newOverlay = new Overlay();
        newOverlay.setName(overlayName);

        if(parentNameList.contains(map.getName())) {
            try {
                map.addOverlay(newOverlay, visible);
                overlayAdded = true;
            } catch (EMP_Exception e) {
                Log.e(TAG, "addOverlay failed", e);
                statusListener.updateStatus(TAG, e.getMessage());
            }
        }

        List<IOverlay> overlays = map.getAllOverlays();
        for(IOverlay overlay: overlays) {
            if(parentNameList.contains(overlay.getName())) {
                try {
                    overlay.addOverlay(newOverlay, visible);
                    overlayAdded = true;
                } catch (EMP_Exception e) {
                    Log.e(TAG, "addOverlay failed", e);
                    statusListener.updateStatus(TAG, e.getMessage());
                }
            }
        }


        Log.d(TAG, "newOverlay parent count " + newOverlay.getName() + " " + newOverlay.getParents().size());
        return overlayAdded;
    }

    public void showAddFeatureDialog() {
        Handler mainHandler = new Handler(activity.getMainLooper());

        final List<String> parentList = MapNamesUtility.getNames(map, false, true, true);
        final List<String> namesInUse = MapNamesUtility.getNames(map, true, true, true);

        if(parentList.size() == 0) {
            statusListener.updateStatus(TAG, "You need at least one overlay to add a feature");
            return;
        }

        Runnable myRunnable = new Runnable() {
            @Override
            public void run() {
                FragmentManager fm = ((AppCompatActivity)activity).getSupportFragmentManager();
                AddFeatureDialog addFeatureDialogFragment = AddFeatureDialog.newInstance("Add Feature", map, parentList, AddContainer.this, namesInUse, entityClasses);
                addFeatureDialogFragment.show(fm, addFeatureFragment);
            }
        };
        mainHandler.post(myRunnable);

        return;
    }

    @Override
    public boolean featureSet(AddFeatureDialog dialog) {

        IMap map = dialog.getMap();
        List<String> parentNameList = dialog.getSelectedParentList();
        boolean visible = dialog.getFeatureVisible();
        String featureName = dialog.getFeatureName();
        FeatureTypeEnum featureTypeEnum = dialog.getSelectedFeatureType();
        Log.d(TAG, "featureSet " + featureName + " " + parentNameList.get(0) + " " + visible + " " + featureTypeEnum);

        if(featureTypeEnum == FeatureTypeEnum.GEO_MIL_SYMBOL) {
            AddMilStdSymbol addMilStdSymbol = new AddMilStdSymbol(activity, map, statusListener, styleManager);
            if(!dialog.isTacticalGraphic()) {
                addMilStdSymbol.showSymbolPropertiesDialog(parentNameList, featureName, visible);
            } else {
                addMilStdSymbol.showTacticalGraphicPropertiesDialog(parentNameList, featureName, visible);
            }
        } else {
            Class<? extends AddEntityBase> entityClass = entityClasses.get(featureTypeEnum);
            if(null != entityClass) {
                try {
                    Constructor<? extends AddEntityBase> c = entityClass.getConstructor(Activity.class, IMap.class, IStatusListener.class,
                            StyleManager.class);
                    AddEntityBase entityClassInstance = c.newInstance(activity, map, statusListener, styleManager);
                    entityClassInstance.showPropertiesDialog(parentNameList, featureName, visible);
                } catch (NoSuchMethodException | IllegalAccessException | InstantiationException | InvocationTargetException e) {
                    Log.e(TAG, "featureType " + featureTypeEnum, e);
                    statusListener.updateStatus(TAG, featureTypeEnum.toString() + " FAILED to start");
                }
            } else {
                statusListener.updateStatus(TAG, featureTypeEnum.toString() + " is NOT yet supported");
            }
        }
        return false;
    }
}
