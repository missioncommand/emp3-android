package mil.emp3.test.emp3vv.navItems.basic_capability_test;

import android.app.Activity;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.widget.ArrayAdapter;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import mil.emp3.api.enums.FeatureTypeEnum;
import mil.emp3.api.interfaces.IMap;
import mil.emp3.test.emp3vv.common.Emp3TesterDialogBase;
import mil.emp3.test.emp3vv.common.NavItemBase;
import mil.emp3.test.emp3vv.dialogs.utils.ErrorDialog;


public class BasicShapeTest extends NavItemBase {
    private static String TAG = BasicShapeTest.class.getSimpleName();

    private static List<String> shapeNames;
    private static java.util.Map<String, Class<? extends SinglePointBasicShapeCapabilityTest>> featureType2Class;
    static {
        featureType2Class = new HashMap<>();
        featureType2Class.put(FeatureTypeEnum.GEO_CIRCLE.toString(), CircleCapabilityTest.class);
        featureType2Class.put(FeatureTypeEnum.GEO_ELLIPSE.toString(), EllipseCapabilityTest.class);
        featureType2Class.put(FeatureTypeEnum.GEO_RECTANGLE.toString(), RectangleCapabilityTest.class);
        featureType2Class.put(FeatureTypeEnum.GEO_SQUARE.toString(), SquareCapabilityTest.class);

        shapeNames = new ArrayList<>();
        shapeNames.add(FeatureTypeEnum.GEO_CIRCLE.toString());
        shapeNames.add(FeatureTypeEnum.GEO_ELLIPSE.toString());
        shapeNames.add(FeatureTypeEnum.GEO_RECTANGLE.toString());
        shapeNames.add(FeatureTypeEnum.GEO_SQUARE.toString());
    }

    SinglePointBasicShapeCapabilityTest shapeTest;
    Thread shapeTestThread;

    public BasicShapeTest(Activity activity, IMap map1, IMap map2) {
        super(activity, map1, map2, TAG);
    }

    @Override
    public String[] getSupportedUserActions() {
        String[] actions = {"Select Shape"};
        return actions;
    }

    @Override
    public String[] getMoreActions() {
        return null;
    }

    protected void test0() {

        try {
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

        try {
            if(Emp3TesterDialogBase.isEmp3TesterDialogBaseActive()) {
                updateStatus("Dismiss the dialog first");
                return false;
            }

            if ((null != shapeTest) && !shapeTest.isNoTest()) {
                // A test is running let it complete
                ErrorDialog.showError(activity, "Wait for Test to Complete");
                return false;
            }

            if(userAction.equals("Select Shape")) {

                if(null != shapeTestThread) {
                    shapeTest.actOn("ClearMap");
                    shapeTest.actOn("Exit");
                }

                final int selection = 0;
                activity.runOnUiThread(new Runnable() {
                    public void run() {
                        final ArrayAdapter<String> featureTypeAdapterAdapter = new ArrayAdapter(activity, android.R.layout.simple_list_item_checked, shapeNames);
                        AlertDialog.Builder builder = new AlertDialog.Builder(activity)
                                .setTitle("Choose Shape")
                                .setSingleChoiceItems(featureTypeAdapterAdapter, selection, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        Log.d(TAG, "Selected Shape " + which);
                                        Class<? extends SinglePointBasicShapeCapabilityTest> shapeClass = featureType2Class.get(shapeNames.get(which));
                                        if (null == shapeClass) {
                                            ErrorDialog.showError(activity, shapeNames.get(which) + " is not yet supported for Draw");
                                        } else {
                                                try {
                                                    Constructor<? extends SinglePointBasicShapeCapabilityTest> c = shapeClass.getConstructor(Activity.class, IMap.class, IMap.class);
                                                    shapeTest = c.newInstance(activity, m1, m2);
                                                    String[] supportedActions = shapeTest.getSupportedUserActions();
                                                    String[] moreActions = shapeTest.getMoreActions();
                                                    String[] newSupportedActions;
                                                    String[] newMoreActions;

                                                    if(supportedActions.length < maxSupportedActions) {
                                                        newSupportedActions = new String[1 + supportedActions.length];
                                                        newSupportedActions[0] = "Select Shape";
                                                        for(int ii = 1; ii <= supportedActions.length; ii++) {
                                                            newSupportedActions[ii] = supportedActions[ii-1];
                                                        }
                                                        newMoreActions = moreActions;
                                                    } else {
                                                        newSupportedActions = new String[supportedActions.length];
                                                        newSupportedActions[0] = "Select Shape";
                                                        for(int ii = 1; ii < supportedActions.length; ii++) {
                                                            newSupportedActions[ii] = supportedActions[ii-1];
                                                        }

                                                        newMoreActions = new String[1+ moreActions.length];
                                                        newMoreActions[0] = supportedActions[supportedActions.length - 1];
                                                        for(int ii = 1; ii <= moreActions.length; ii++) {
                                                            newMoreActions[ii] = moreActions[ii-1];
                                                        }
                                                    }

                                                    testMenuManager.recreateTestMenu(newSupportedActions, newMoreActions);
                                                    shapeTestThread = new Thread(shapeTest);
                                                    shapeTestThread.start();
                                                } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException | InstantiationException e) {
                                                    Log.e(TAG, "constructor not found for " + shapeNames.get(which));
                                                    updateStatus(TAG, e.getMessage());
                                                    ErrorDialog.showError(activity, e.getMessage());
                                                }

                                        }
                                        dialog.cancel();
                                    }
                                });

                        final AlertDialog dialog = builder.create();
                        dialog.show();
                    }
                });
            } else if(userAction.equals("Exit")) {
                if(null != shapeTestThread) {
                    shapeTest.actOn("ClearMap");
                    shapeTest.actOn("Exit");
                }
                testThread.interrupt();
            } else if(null != shapeTest) {
                shapeTest.actOn(userAction);
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
        return (actOn(userAction));
    }
}
