package mil.emp3.test.emp3vv.navItems;

import android.app.Activity;
import android.content.DialogInterface;

import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.widget.ArrayAdapter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import mil.emp3.api.interfaces.IFeature;
import mil.emp3.api.interfaces.IMap;
import mil.emp3.json.geoJson.GeoJsonExporter;
import mil.emp3.test.emp3vv.common.Emp3TesterDialogBase;
import mil.emp3.test.emp3vv.common.ExecuteTest;
import mil.emp3.test.emp3vv.common.NavItemBase;
import mil.emp3.test.emp3vv.common.StyleManager;
import mil.emp3.test.emp3vv.containers.AddContainer;
import mil.emp3.test.emp3vv.containers.UpdateContainer;

import mil.emp3.test.emp3vv.utils.MapNamesUtility;

/**
 * This test is launched when 'Add Remove Get' item on the hamburger menu is selected.
 *
 * How do I add a new Capability Test?
 *     - Read the ValidateAndVerify.java documentation on how to make the item for your test appear in the menu.
 *     - Create a class that will implement the test and keep it in the package 'navItems', you can create a new
 *          package under 'navItems' package if you think your test will need multiple classes. Read the documentation below
 *          to understand what some of the requirements on you class are. You can choose AddRemoveGetTest as template.
 *     - Now update the common/ExecuteTest.java@onTestSelected method to invoke the new class
 *
 * What are the requirements on the new class I create?
 *     - extends NavItemBase
 *     - NavItemBase implements the 'run()' method and invokes the 'test0()' method, you should implement the test0 method.
 *     - You can choose to override the 'run()' method like LaunchMap.java class does.
 *     - Implement getSupportedUserActions - Each test is allowed to put up five Buttons in the test reserved area. Return the names of those
 *         buttons in this method. Framework will invoke the 'actOn()' method on your class with this name as argument.
 *
 *     - Implement getMoreActions - This allows you to buttons beyond the four buttons discussed above. Everything else works the same.
 *     - You should now implement the 'actOn()' method that acts on the button press by the user.
 *     - You should override the clearMapForTest and ExitTest methods. These are invoked when user selects the corresponding buttons on the action menu.
 *     - AddRemoveGetTest is special in that it uses all the common dialogs in the 'containers' package. In most cases there will be lot more
 *         local functionality in your class. If you have common artifacts then feel free to promote them
 *
 */
public class AddRemoveGetTest extends NavItemBase {

    private static String TAG = AddRemoveGetTest.class.getSimpleName();

    private final StyleManager styleManager;
    public AddRemoveGetTest(Activity activity, IMap map1, IMap map2) {
        super(activity, map1, map2, TAG);
        styleManager = new StyleManager(activity, maps);
    }

    @Override
    public String[] getSupportedUserActions() {
        String[] actions = {"Add Overlay", "Add Feature", "Update Overlay", "Update Feature"};
        return actions;
    }

    @Override
    public String[] getMoreActions() {
        String[] actions = {"Show Overlay", "Show Feature", "Export features to GeoJSON"};
        return styleManager.getMoreActions(actions);
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
        final int whichMap = ExecuteTest.getCurrentMap();

        try {
            if(Emp3TesterDialogBase.isEmp3TesterDialogBaseActive()) {
                updateStatus("Dismiss the dialog first");
                return false;
            }

            if (userAction.equals("Exit")) {
                testThread.interrupt();
            } else if(userAction.equals("ClearMap")) {
                clearMaps();
            } else if(userAction.equals("Add Overlay")) {
                AddContainer addContainer = new AddContainer(activity, maps[whichMap], this, styleManager);
                addContainer.showAddOverlayDialog();
            }  else if(userAction.equals("Add Feature")) {
                AddContainer addContainer = new AddContainer(activity, maps[whichMap], this, styleManager);
                addContainer.showAddFeatureDialog();
            } else if(userAction.equals("Update Overlay")) {
                final List<String> overlayNames = MapNamesUtility.getNames(maps[whichMap], false, true, false);
                AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                builder.setTitle("Choose Overlay").setAdapter(new ArrayAdapter(activity, android.R.layout.simple_list_item_1, overlayNames),
                        new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Log.d(TAG, "Selected Overlay " + overlayNames.get(which));
                        UpdateContainer updateContainer = new UpdateContainer(activity, maps[whichMap], AddRemoveGetTest.this, styleManager);
                        updateContainer.showUpdateContainerDialog(maps[whichMap], MapNamesUtility.getContainer(maps[whichMap],overlayNames.get(which)), false);
                    }
                }).show();
            }  else if(userAction.equals("Update Feature")) {
                final List<String> featureNames = MapNamesUtility.getNames(maps[whichMap], false, false, true);
                AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                builder.setTitle("Choose Feature").setAdapter(new ArrayAdapter(activity, android.R.layout.simple_list_item_1, featureNames),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Log.d(TAG, "Selected Feature " + featureNames.get(which));
                                UpdateContainer updateContainer = new UpdateContainer(activity, maps[whichMap], AddRemoveGetTest.this, styleManager);
                                updateContainer.showUpdateContainerDialog(maps[whichMap], MapNamesUtility.getContainer(maps[whichMap],featureNames.get(which)), false);
                            }
                        }).show();
            } else if(userAction.equals("Show Overlay")) {
                final List<String> overlayNames = MapNamesUtility.getNames(maps[whichMap], false, true, false);
                AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                builder.setTitle("Choose Overlay").setAdapter(new ArrayAdapter(activity, android.R.layout.simple_list_item_1, overlayNames),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Log.d(TAG, "Selected Overlay " + overlayNames.get(which));
                                UpdateContainer updateContainer = new UpdateContainer(activity, maps[whichMap], AddRemoveGetTest.this, styleManager);
                                updateContainer.showUpdateContainerDialog(maps[whichMap], MapNamesUtility.getContainer(maps[whichMap],overlayNames.get(which)), true);
                            }
                        }).show();
            } else if(userAction.equals("Show Feature")) {
                final List<String> featureNames = MapNamesUtility.getNames(maps[whichMap], false, false, true);
                AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                builder.setTitle("Choose Feature").setAdapter(new ArrayAdapter(activity, android.R.layout.simple_list_item_1, featureNames),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Log.d(TAG, "Selected Feature " + featureNames.get(which));
                                UpdateContainer updateContainer = new UpdateContainer(activity, maps[whichMap], AddRemoveGetTest.this, styleManager);
                                updateContainer.showUpdateContainerDialog(maps[whichMap], MapNamesUtility.getContainer(maps[whichMap], featureNames.get(which)), true);
                            }
                        }).show();
            } else if (userAction.equals("Export features to GeoJSON")) {
                try {
                List<IFeature> featureList = maps[whichMap].getAllFeatures();
                    for (IFeature feature : featureList) {
                        String geoJSON = GeoJsonExporter.export(feature);
                        File file = new File("/sdcard/Download/" + feature.getName() + "Geo.json");
                        if (!file.exists()) {
                            boolean created = file.createNewFile();
                        }
                        FileOutputStream fos = new FileOutputStream(file);
                        fos.write(geoJSON.getBytes());
                        fos.close();
                    }
                } catch (IOException ioe) {
                    ioe.printStackTrace();
                }

            } else {
                styleManager.actOn(userAction);
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
