package mil.emp3.test.emp3vv.navItems.kml_service_test;

import android.app.Activity;
import android.content.DialogInterface;
import android.os.Environment;
import android.os.Handler;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ArrayAdapter;

import java.io.File;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import mil.emp3.api.events.KMLSEvent;
import mil.emp3.api.exceptions.EMP_Exception;
import mil.emp3.api.interfaces.IKMLS;
import mil.emp3.api.interfaces.IMap;
import mil.emp3.api.KMLS;
import mil.emp3.api.interfaces.IMapService;
import mil.emp3.api.listeners.IKMLSEventListener;
import mil.emp3.test.emp3vv.common.Emp3TesterDialogBase;
import mil.emp3.test.emp3vv.common.ExecuteTest;
import mil.emp3.test.emp3vv.common.NavItemBase;
import mil.emp3.test.emp3vv.common.StyleManager;
import mil.emp3.test.emp3vv.dialogs.utils.ErrorDialog;

public class KMLServiceTest extends NavItemBase implements KmlsSettingsDialog.IKmlsSettingsDialogListener {

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
        String[] actions = {"Custom URL"};
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
                                    Log.d(TAG, "Selected URL " + sampleURLs[which]);
                                    KMLS kmls = new KMLS(activity, sampleURLs[which], kmlsEventListener[whichMap]);
                                    // maps[whichMap].addMapService(kmls);
                                    getServiceNameAndAdd(kmls, whichMap);
                                } catch (MalformedURLException mue) {
                                    ErrorDialog.showError(activity, "malformed URL " + sampleURLs[which]);
                                    Log.e(TAG, mue.getMessage(), mue);
                                }

                            }
                        }).show();
            } else if(userAction.equals("File URL")) {
                String root_sd = Environment.getExternalStorageDirectory().getPath();
                File directory = new File(root_sd + "/testFiles");
                final List<String> sampleFileURLs = new ArrayList<>();

                File[] files = directory.listFiles();
                for (File file : files) {
                    if (!file.isDirectory() && (file.getName().endsWith(".kmz") || file.getName().endsWith(".kml"))) {
                        File aFile = new File(directory.getPath() + File.separator + file.getName());
                        sampleFileURLs.add(aFile.toURI().toURL().toString());
                    }
                }

                if(sampleFileURLs.size() > 0) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                    builder.setTitle("Choose URL").setAdapter(new ArrayAdapter(activity, android.R.layout.simple_list_item_1, sampleFileURLs),
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    try {
                                        Log.d(TAG, "Selected URL " + sampleFileURLs.get(which));
                                        KMLS kmls = new KMLS(activity, sampleFileURLs.get(which), kmlsEventListener[whichMap]);
                                        // maps[whichMap].addMapService(kmls);
                                        getServiceNameAndAdd(kmls, whichMap);
                                    } catch (MalformedURLException mue) {
                                        ErrorDialog.showError(activity, "malformed URL " + sampleFileURLs.get(which));
                                        Log.e(TAG, mue.getMessage(), mue);
                                    }
                                }
                            }).show();
                } else {
                    ErrorDialog.showError(activity, "No KML or KMZ files found in /sdcard/kml, fine some in tests/data in repository");
                }
            } else if (userAction.equals("Remove Service")) {
                final Map<String, IKMLS> addedServices = getKMLServices(whichMap);
                if (addedServices.size() > 0) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                    builder.setTitle("Choose URL").setAdapter(new ArrayAdapter(activity, android.R.layout.simple_list_item_1, addedServices.keySet().toArray()),
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    String selectedService = (String) addedServices.keySet().toArray()[which];
                                    Log.d(TAG, "Selected Service " + selectedService);
                                    IKMLS kmls = addedServices.get(selectedService);
                                    if (null != kmls) {
                                        try {
                                            maps[whichMap].removeMapService(kmls);
                                        } catch (EMP_Exception e) {
                                            Log.e(TAG, "Remove Service exception", e);
                                            ErrorDialog.showError(activity, "Remove Service: " + e.getMessage());
                                        }
                                    }

                                }
                            }).show();
                } else {
                    ErrorDialog.showError(activity, "There are no KML services to remove");
                }
            } else if (userAction.equals("Show Services")) {
                final Map<String, IKMLS> addedServices = getKMLServices(whichMap);
                if (addedServices.size() > 0) {
                    List<String> kmlServices = new ArrayList<>();
                    for(IKMLS kmls: addedServices.values()) {
                        kmlServices.add(kmls.toString());
                    }
                    AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                    builder.setTitle("KML Services").setAdapter(new ArrayAdapter(activity, android.R.layout.simple_list_item_1, kmlServices),
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    String selectedService = (String) addedServices.keySet().toArray()[which];
                                    Log.d(TAG, "Selected Service " + selectedService);
                                }
                            }).show();
                } else {
                    ErrorDialog.showError(activity, "There are no KML services to show");
                }
            } else if (userAction.equals("Custom URL")) {
                Handler mainHandler = new Handler(activity.getMainLooper());
                Runnable myRunnable = new Runnable() {
                    @Override
                    public void run() {
                        FragmentManager fm = ((AppCompatActivity)activity).getSupportFragmentManager();
                        KmlsSettingsDialog kmlsSettingsDialog = KmlsSettingsDialog.newInstance("KMLS Settings", KMLServiceTest.this, maps[whichMap], whichMap, null);
                        kmlsSettingsDialog.show(fm, "fragment_kmls_settings_dialog");
                    }
                };
                mainHandler.post(myRunnable);
            }else {
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
        final IMap map;
        KMLSServiceListener(int whichMap) {
            this.map = maps[whichMap];
        }

        @Override
        public void onEvent(KMLSEvent event) {
            try {
                Log.d(TAG, "KMLSServiceListener-onEvent " + event.getEvent().toString() + " status " + event.getTarget().getStatus(map));
            } catch(EMP_Exception e) {
                Log.e(TAG, e.getMessage(), e);
            }
        }
    }

    private Map<String, IKMLS> getKMLServices(int whichMap) {
        Map<String, IKMLS> addedServices = new HashMap<>();
        for(IMapService ms: maps[whichMap].getMapServices()) {
            if(ms instanceof IKMLS) {
                if(null != ms.getName()) {
                    addedServices.put(ms.getName(), (IKMLS) ms);
                } else {
                    addedServices.put(ms.getGeoId().toString(), (IKMLS) ms);
                }
            }
        }
        return addedServices;
    }

    private void getServiceNameAndAdd(final IKMLS kmls, final int whichMap) {
        Handler mainHandler = new Handler(activity.getMainLooper());
        Runnable myRunnable = new Runnable() {
            @Override
            public void run() {
                FragmentManager fm = ((AppCompatActivity)activity).getSupportFragmentManager();
                KmlsSettingsDialog kmlsSettingsDialog = KmlsSettingsDialog.newInstance("KMLS Settings", KMLServiceTest.this, maps[whichMap], whichMap, kmls.getURL().toString());
                kmlsSettingsDialog.show(fm, "fragment_kmls_settings_dialog");
            }
        };
        mainHandler.post(myRunnable);
    }

    @Override
    public void kmlsSet(KmlsSettingsDialog kmlsSettingsDialog) {
        Log.d(TAG, "kmlsSet " + kmlsSettingsDialog.getName() + " " + kmlsSettingsDialog.getUrl());

        try {
            KMLS kmls = new KMLS(activity, kmlsSettingsDialog.getUrl(), kmlsEventListener[kmlsSettingsDialog.getWhichMap()]);
            kmls.setName(kmlsSettingsDialog.getName());
            kmlsSettingsDialog.getMap().addMapService(kmls);
        } catch (Exception e) {
            ErrorDialog.showError(activity, e.getMessage());
        }
    }
}
