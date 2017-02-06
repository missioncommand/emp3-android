package mil.emp3.test.emp3vv.navItems.select_feature_test;

import android.app.Activity;
import android.os.Handler;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;


import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import mil.emp3.api.Overlay;
import mil.emp3.api.exceptions.EMP_Exception;
import mil.emp3.api.interfaces.ICamera;
import mil.emp3.api.interfaces.IContainer;
import mil.emp3.api.interfaces.IFeature;
import mil.emp3.api.interfaces.IMap;
import mil.emp3.api.interfaces.IOverlay;
import mil.emp3.test.emp3vv.common.Emp3TesterDialogBase;
import mil.emp3.test.emp3vv.common.ExecuteTest;
import mil.emp3.test.emp3vv.common.NavItemBase;
import mil.emp3.test.emp3vv.containers.AddContainer;
import mil.emp3.test.emp3vv.containers.dialogs.milstdunits.SymbolPropertiesDialog;
import mil.emp3.test.emp3vv.dialogs.utils.ErrorDialog;
import mil.emp3.test.emp3vv.utils.MapNamesUtility;

public class SelectFeatureTest extends NavItemBase implements SelectFeatureDialog.ISelectFeatureDialogListener {
    private static String TAG = SelectFeatureTest.class.getSimpleName();

    Thread selectorThread = null;
    Thread deselectorThread = null;
    Thread adderThread = null;
    Thread removerThread = null;

    boolean multiThreadTestInProgress = false;
    private IOverlay[] overlays = new IOverlay[2];

    public SelectFeatureTest(Activity activity, IMap map1, IMap map2) {
        super(activity, map1, map2, TAG);
    }

    @Override
    public String[] getSupportedUserActions() {
        String[] actions = {"Add Overlay", "Add Feature", "Select Feature Test"};
        return actions;
    }

    @Override
    public String[] getMoreActions() {
        String[] actions = { "MultiThread-Start", "MultiThread-Stop" };
        return actions;
    }
    protected void test0() {

        try {
            SymbolPropertiesDialog.loadSymbolTables();
            testThread = Thread.currentThread();
            while (!Thread.interrupted()) {
                try {
                    Thread.sleep(large_waitInterval * 10);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        } finally {
            endTest();
        }
    }

    @Override
    public boolean actOn(String userAction) {
        final int whichMap = ExecuteTest.getCurrentMap();

        try {
            if(multiThreadTestInProgress) {
                if(!userAction.equals("MultiThread-Stop")) {
                    updateStatus("Please stop MultiThread Test first");
                    return false;
                } else {
                    stopMultiThreadTest(whichMap);
                    return true;
                }
            }

            if(Emp3TesterDialogBase.isEmp3TesterDialogBaseActive()) {
                updateStatus("Dismiss the dialog first");
                return false;
            }

            if (userAction.equals("Exit")) {
                testThread.interrupt();
            } else if(userAction.equals("ClearMap")) {
                clearMaps();
            } else if(userAction.contains("Add Overlay")) {
                AddContainer addContainer = new AddContainer(activity, maps[whichMap], this, null);
                addContainer.showAddOverlayDialog();
            }  else if(userAction.contains("Add Feature")) {
                AddContainer addContainer = new AddContainer(activity, maps[whichMap], this, null);
                addContainer.showAddFeatureDialog();
            } else if(userAction.contains("Select Feature Test")) {
                Handler mainHandler = new Handler(activity.getMainLooper());
                Runnable myRunnable = new Runnable() {
                    @Override
                    public void run() {
                        FragmentManager fm = ((AppCompatActivity)activity).getSupportFragmentManager();
                        SelectFeatureDialog selectFeatureDialog = SelectFeatureDialog.newInstance("Select Feature Test", maps[whichMap], SelectFeatureTest.this);
                        selectFeatureDialog.show(fm, "fragment_select_feature_dialog");
                    }
                };
                mainHandler.post(myRunnable);
            }  else if (userAction.contains("MultiThread-Start")) {
                startMultiThreadTest(whichMap);
            }
        } catch (Exception e) {
            updateStatus(TAG, e.getMessage());
            e.printStackTrace();
        }

        return true;
    }

    @Override
    protected void clearMapForTest() {
        String userAction = "ClearMap";
        actOn(userAction);
    }

    @Override
    protected boolean exitTest() {
        String userAction = "Exit";
        return(actOn(userAction));
    }

    @Override
    public void isSelectedFeature(SelectFeatureDialog dialog) {
        List<String> userSelectedFeatures = dialog.getUserSelectedFeatures();
        for(String userSelectedFeature: userSelectedFeatures) {
            IContainer c = MapNamesUtility.getContainer(dialog.getMap(), userSelectedFeature);
            if(c instanceof IFeature) {
                IFeature feature = (IFeature) c;
                ErrorDialog.showError(activity, "Feature " + feature.getName() + " " + (dialog.getMap().isSelected(feature) ? "is selected" : "is NOT selected"));
            } else {
                ErrorDialog.showError(activity, "isSelectedFeature: container is NOT a feature?");
            }
        }
    }

    @Override
    public void selectFeature(SelectFeatureDialog dialog) {
        List<String> userSelectedFeatures = dialog.getUserSelectedFeatures();
        List<IFeature> selectFeatures = new ArrayList<>();

        for(String userSelectedFeature: userSelectedFeatures) {
            IContainer c = MapNamesUtility.getContainer(dialog.getMap(), userSelectedFeature);
            if(c instanceof IFeature) {
                IFeature feature = (IFeature) c;
                selectFeatures.add(feature);
            } else {
                ErrorDialog.showError(activity, "isSelectedFeature: container is NOT a feature?");
            }
        }

        // Need to exercise both interfaces
        if(selectFeatures.size() == 1) {
            dialog.getMap().selectFeature(selectFeatures.get(0));
        } else {
            dialog.getMap().selectFeatures(selectFeatures);
        }
    }

    @Override
    public void deselectFeature(SelectFeatureDialog dialog) {
        List<String> userSelectedFeatures = dialog.getUserSelectedFeatures();
        List<IFeature> deselectFeatures = new ArrayList<>();

        for(String userSelectedFeature: userSelectedFeatures) {
            IContainer c = MapNamesUtility.getContainer(dialog.getMap(), userSelectedFeature);
            if(c instanceof IFeature) {
                IFeature feature = (IFeature) c;
                deselectFeatures.add(feature);
            } else {
                ErrorDialog.showError(activity, "isSelectedFeature: container is NOT a feature?");
            }
        }

        // Need to exercise both interfaces
        if(deselectFeatures.size() == 1) {
            dialog.getMap().deselectFeature(deselectFeatures.get(0));
        } else {
            dialog.getMap().deselectFeatures(deselectFeatures);
        }
    }

    @Override
    public void clearSelected(SelectFeatureDialog dialog) {
        dialog.getMap().clearSelected();
    }

    private void stopMultiThreadTest(int whichMap) {
        if(multiThreadTestInProgress) {
            multiThreadTestInProgress = false;
            selectorThread.interrupt();
            deselectorThread.interrupt();
            removerThread.interrupt();
            adderThread.interrupt();
        }
    }
    private void startMultiThreadTest(int whichMap) {

        ICamera camera = maps[whichMap].getCamera();
        List<IFeature>  list = generateMilStdSymbolList(10, camera.getLatitude(), camera.getLongitude());

        try {
            List<IOverlay> allOverlays = maps[whichMap].getAllOverlays();
            if((null == allOverlays) || (allOverlays.size() == 0)) {
                IOverlay o = new Overlay();
                o.setName("my Overlay");
                maps[whichMap].addOverlay(o, true);
                overlays[whichMap] = o;
            } else {
                overlays[whichMap] = maps[whichMap].getAllOverlays().get(0);
            }

            overlays[whichMap].addFeatures(list, true);
            adderThread = new Thread(new SelectFeatureTest.Adder(whichMap, list));
            selectorThread = new Thread(new SelectFeatureTest.Selector(whichMap, list));
            deselectorThread = new Thread(new SelectFeatureTest.Deselector(whichMap, list));
            removerThread = new Thread(new SelectFeatureTest.Remover(whichMap, list));

            selectorThread.start();
            deselectorThread.start();
            removerThread.start();
            adderThread.start();
            multiThreadTestInProgress = true;

        } catch (EMP_Exception e) {
            e.printStackTrace();
        }
    }

    abstract class BaseMultiThreadTest implements Runnable {
        protected int whichMap;
        protected List<IFeature> list;
        Random random = new Random();
        BaseMultiThreadTest(int whichMap, List<IFeature> list) {
            this.whichMap = whichMap;
            this.list = list;
        }

        abstract protected void action(int index);

        public void run() {
            while(!Thread.interrupted()) {
                try {
                    Thread.sleep(random.nextInt(10));
                    action(random.nextInt(list.size()));
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }
    class Selector extends BaseMultiThreadTest {
        Selector(int whichMap, List<IFeature> list) {
            super(whichMap, list);
        }
        @Override
        protected void action(int index) {
            maps[whichMap].selectFeature(list.get(index));
        }
    }

    class Deselector extends BaseMultiThreadTest {

        Deselector(int whichMap, List<IFeature> list) {
            super(whichMap, list);
        }
        @Override
        protected void action(int index) {
            maps[whichMap].deselectFeature(list.get(index));
        }
    }

    class Adder extends BaseMultiThreadTest {

        Adder(int whichMap, List<IFeature> list) {
            super(whichMap, list);
        }
        @Override
        protected void action(int index) {
            try {
                overlays[whichMap].addFeature(list.get(index), true);
            } catch (EMP_Exception e) {
                e.printStackTrace();
            }
        }
    }

    class Remover extends BaseMultiThreadTest {

        Remover(int whichMap, List<IFeature> list) {
            super(whichMap, list);
        }
        @Override
        protected void action(int index) {
            try {
                overlays[whichMap].removeFeature(list.get(index));
            } catch (EMP_Exception e) {
                e.printStackTrace();
            }
        }
    }
}
