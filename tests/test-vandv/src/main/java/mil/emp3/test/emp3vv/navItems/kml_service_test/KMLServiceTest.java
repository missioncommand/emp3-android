package mil.emp3.test.emp3vv.navItems.kml_service_test;

import android.app.Activity;
import android.content.DialogInterface;
import android.os.Environment;
import android.support.v4.os.EnvironmentCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.widget.ArrayAdapter;

import java.io.File;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

import mil.emp3.api.events.KMLSEvent;
import mil.emp3.api.exceptions.EMP_Exception;
import mil.emp3.api.interfaces.IMap;
import mil.emp3.api.KMLS;
import mil.emp3.api.listeners.IKMLSEventListener;
import mil.emp3.test.emp3vv.common.Emp3TesterDialogBase;
import mil.emp3.test.emp3vv.common.ExecuteTest;
import mil.emp3.test.emp3vv.common.NavItemBase;
import mil.emp3.test.emp3vv.common.StyleManager;
import mil.emp3.test.emp3vv.containers.UpdateContainer;
import mil.emp3.test.emp3vv.dialogs.utils.ErrorDialog;
import mil.emp3.test.emp3vv.navItems.AddRemoveGetTest;
import mil.emp3.test.emp3vv.utils.MapNamesUtility;

public class KMLServiceTest extends NavItemBase {

    private static String TAG = KMLServiceTest.class.getSimpleName();

    private final StyleManager styleManager;

    private IKMLSEventListener[] kmlsEventListener = new IKMLSEventListener[ExecuteTest.MAX_MAPS];

    String[] sampleURLs = {
            "https://github.com/downloads/brazzy/nikki/example.kmz",
            "https://developers.google.com/kml/documentation/KML_Samples.kml"
    };

    public KMLServiceTest(Activity activity, IMap map1, IMap map2) {
        super(activity, map1, map2, TAG);
        styleManager = new StyleManager(activity, maps);
        kmlsEventListener[0] = new KMLSServiceListener(0);
        kmlsEventListener[1] = new KMLSServiceListener(1);
    }

    @Override
    public String[] getSupportedUserActions() {
        String[] actions = {"Network URL", "File URL", "Remove Service", "Show Services"};
        return actions;
    }

    @Override
    public String[] getMoreActions() {
        return styleManager.getMoreActions(null);
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
            if (Emp3TesterDialogBase.isEmp3TesterDialogBaseActive()) {
                updateStatus("Dismiss the dialog first");
                return false;
            }

            if (userAction.equals("Exit")) {
                testThread.interrupt();
            } else if (userAction.equals("ClearMap")) {
                clearMaps();
            } else if (userAction.equals("Network URL")) {
                // KMLS kmls = new KMLS(activity, "https://github.com/downloads/brazzy/nikki/example.kmz", true, kmlsEventListener[whichMap]);
                // https://developers.google.com/kml/documentation/KML_Samples.kml
                // KMLS kmls = new KMLS(activity, "https://developers.google.com/kml/documentation/KML_Samples.kml", true, kmlsEventListener[whichMap]);
                // maps[whichMap].addMapService(kmls);
                AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                builder.setTitle("Choose URL").setAdapter(new ArrayAdapter(activity, android.R.layout.simple_list_item_1, sampleURLs),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                try {
                                    Log.d(TAG, "Selected ORL " + sampleURLs[which]);
                                    KMLS kmls = new KMLS(activity, sampleURLs[which], true, kmlsEventListener[whichMap]);
                                    maps[whichMap].addMapService(kmls);
                                } catch (MalformedURLException mue) {
                                    ErrorDialog.showError(activity, "malformed URL " + sampleURLs[which]);
                                    Log.e(TAG, mue.getMessage(), mue);
                                } catch (EMP_Exception e) {
                                    ErrorDialog.showError(activity, "EMP Exception " + sampleURLs[which]);
                                    Log.e(TAG, e.getMessage(), e);
                                }
                            }
                        }).show();
            } else if(userAction.equals("File URL")) {
                String root_sd = Environment.getExternalStorageDirectory().getPath();
                File directory = new File(root_sd + "/kml");
                final List<String> sampleURLs = new ArrayList<>();

                File[] files = directory.listFiles();
                for (File file : files) {
                    if (!file.isDirectory() && (file.getName().endsWith(".kmz") || file.getName().endsWith(".kml"))) {
                        File aFile = new File(directory.getPath() + File.separator + file.getName());
                        sampleURLs.add(aFile.toURI().toURL().toString());
                    }
                }


                if(sampleURLs.size() > 0) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                    builder.setTitle("Choose URL").setAdapter(new ArrayAdapter(activity, android.R.layout.simple_list_item_1, sampleURLs),
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    try {
                                        Log.d(TAG, "Selected ORL " + sampleURLs.get(which));
                                        KMLS kmls = new KMLS(activity, sampleURLs.get(which), true, kmlsEventListener[whichMap]);
                                        maps[whichMap].addMapService(kmls);
                                    } catch (MalformedURLException mue) {
                                        ErrorDialog.showError(activity, "malformed URL " + sampleURLs.get(which));
                                        Log.e(TAG, mue.getMessage(), mue);
                                    } catch (EMP_Exception e) {
                                        ErrorDialog.showError(activity, "EMP Exception " + sampleURLs.get(which));
                                        Log.e(TAG, e.getMessage(), e);
                                    }
                                }
                            }).show();
                } else {
                    ErrorDialog.showError(activity, "No KML or KMZ files found in /sdcard/kml");
                }
            }else if (userAction.equals("Remove Service")) {

            } else if (userAction.equals("Show Services")) {

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

    class KMLSServiceListener implements IKMLSEventListener {
        private final int whichMap;
        KMLSServiceListener(int whichMap) {
            this.whichMap = whichMap;
        }
        @Override
        public void onEvent(KMLSEvent event) {
            try {
                Log.d(TAG, "KMLSServiceListener-onEvent " + event.getEvent().toString() + " status " + event.getTarget().getStatus(maps[whichMap]));
            } catch(EMP_Exception e) {
                Log.e(TAG, e.getMessage(), e);
            }
        }
    }
}
