package mil.emp3.test.emp3vv.navItems.kml_service_test;

import android.app.Activity;
import android.util.Log;

import mil.emp3.api.events.KMLSEvent;
import mil.emp3.api.exceptions.EMP_Exception;
import mil.emp3.api.interfaces.IMap;
import mil.emp3.api.KMLS;
import mil.emp3.api.listeners.IKMLSEventListener;
import mil.emp3.test.emp3vv.common.Emp3TesterDialogBase;
import mil.emp3.test.emp3vv.common.ExecuteTest;
import mil.emp3.test.emp3vv.common.NavItemBase;
import mil.emp3.test.emp3vv.common.StyleManager;

public class KMLServiceTest extends NavItemBase {

    private static String TAG = KMLServiceTest.class.getSimpleName();

    private final StyleManager styleManager;

    private IKMLSEventListener[] kmlsEventListener = new IKMLSEventListener[ExecuteTest.MAX_MAPS];

    public KMLServiceTest(Activity activity, IMap map1, IMap map2) {
        super(activity, map1, map2, TAG);
        styleManager = new StyleManager(activity, maps);
        kmlsEventListener[0] = new KMLSServiceListener(0);
        kmlsEventListener[1] = new KMLSServiceListener(1);
    }

    @Override
    public String[] getSupportedUserActions() {
        String[] actions = {"Add Service", "Remove Service", "Show Services"};
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
            } else if (userAction.equals("Add Service")) {
                KMLS kmls = new KMLS(activity, "https://github.com/downloads/brazzy/nikki/example.kmz", true, kmlsEventListener[whichMap]);
                maps[whichMap].addMapService(kmls);
            } else if (userAction.equals("Remove Service")) {

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
