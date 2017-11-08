package mil.emp3.test.emp3vv.navItems.performance_test;

import android.app.Activity;
import android.os.Handler;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import org.cmapi.primitives.GeoPosition;
import org.cmapi.primitives.IGeoAltitudeMode;
import org.cmapi.primitives.IGeoMilSymbol;
import org.cmapi.primitives.IGeoPosition;

import java.util.Locale;

import mil.emp3.api.MilStdSymbol;
import mil.emp3.api.exceptions.EMP_Exception;
import mil.emp3.api.interfaces.ICamera;
import mil.emp3.api.interfaces.IFeature;
import mil.emp3.api.interfaces.IMap;
import mil.emp3.api.interfaces.IOverlay;
import mil.emp3.api.utils.GeographicLib;
import mil.emp3.test.emp3vv.common.Emp3TesterDialogBase;
import mil.emp3.test.emp3vv.common.ExecuteTest;
import mil.emp3.test.emp3vv.common.NavItemBase;

/**
 * Adds up to 10000 mil std symbols based on configuration to the Map. Moves the symbols around at a given rate. It is upto the user to decide if
 * what is displayed on the map is acceptable or not. Map should be able to support uto 10000 symbols. If there are multiple maps then combined count should
 * not exceed 10000.
 */
public class PerformanceTest extends NavItemBase implements PerformanceTestConfigureDialog.IPerformanceTestConfigureDialogListener {

    private static String TAG = PerformanceTest.class.getSimpleName();
    PerformanceTestThread pt[] = new PerformanceTestThread[ExecuteTest.MAX_MAPS];
    int trackCount = 10000;
    PerformanceTestConfig config[] = new PerformanceTestConfig[ExecuteTest.MAX_MAPS];

    public PerformanceTest(Activity activity, IMap map1, IMap map2) {
        super(activity, map1, map2, TAG);

        config[ExecuteTest.MAP1] = new PerformanceTestConfig();
        config[ExecuteTest.MAP2] = new PerformanceTestConfig();
        config[ExecuteTest.MAP1].setTrackCount(10000);
        config[ExecuteTest.MAP1].setChangeAffiliation(false);
        config[ExecuteTest.MAP1].setBatchUpdates(false);
        config[ExecuteTest.MAP2].setTrackCount(10000);
        config[ExecuteTest.MAP2].setChangeAffiliation(false);
        config[ExecuteTest.MAP2].setBatchUpdates(false);
    }

    @Override
    public String[] getSupportedUserActions() {
        String[] actions = {"Start", "Stop", "Configure"};
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
            } else if(userAction.equals("Start")) {
                if(null == pt[whichMap]) {
                    createOverlay(maps[whichMap]);
                    pt[whichMap] = new PerformanceTestThread(maps[whichMap], config[whichMap].getTrackCount(),
                            config[whichMap].isChangeAffiliation(), config[whichMap].isBatchUpdates());
                    pt[whichMap].start();
                }
            } else if(userAction.equals("Stop")) {
                if(null != pt[whichMap]) {
                    pt[whichMap].stopTest();
                    pt[whichMap] = null;
                }
            } else if(userAction.equals("Configure")) {
                Handler mainHandler = new Handler(activity.getMainLooper());
                Runnable myRunnable = new Runnable() {
                    @Override
                    public void run() {
                        FragmentManager fm = ((AppCompatActivity)activity).getSupportFragmentManager();
                        PerformanceTestConfigureDialog configureDialog = PerformanceTestConfigureDialog.newInstance("Configure", maps[whichMap], PerformanceTest.this, config[whichMap]);
                        configureDialog.show(fm, "fragment_performance_test_configure_dialog");
                    }
                };
                mainHandler.post(myRunnable);
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

    @Override
    public void configure(PerformanceTestConfigureDialog dialog) {
        config[ExecuteTest.getCurrentMap()] = dialog.getConfig();
    }

    class PerformanceTestThread extends Thread {
        private long COUNT_PER_INTERVAL = 200;
        private long UPDATE_INTERVAL = 200;
        private String[] aTrackSymbol = {"SFAPMFF--------", "SFAPMFA--------", "SFAPMFJ--------", "SFAPMFL--------", "SFAPMHQ--------", "SFAPMHO--------", "SHAPMFF--------", "SHAPMFA--------", "SHAPMFJ--------", "SHAPMFL--------", "SHAPMHQ--------", "SHAPMHO--------", "SNAPCF---------", "SFAPCF---------", "SFAPCH---------"};
        private int iTrackSymbolCount = this.aTrackSymbol.length;
        private MilStdSymbol.Affiliation[] aAffiliations = {MilStdSymbol.Affiliation.FRIEND, MilStdSymbol.Affiliation.NEUTRAL, MilStdSymbol.Affiliation.HOSTILE};
        private final int iAffCount = aAffiliations.length;
        private int iCount = 2000;
        private boolean bAiffChange = false;
        private java.util.List<IFeature> oFeatureList = new java.util.ArrayList<>();
        private boolean bContinue = true;
        private final int TIME_LIST_SIZE = 100;
        private int iTimelistIndex = 0;
        private long[] alTimeList = new long[TIME_LIST_SIZE];
        private double dTimeSum = 0.0;
        private int iTimeSamples = 0;
        private String sMessage;
        private boolean bBatchUpdate = false;
        private IOverlay oOverlay;
        private ICamera oCamera;
        private IMap oMap;
        public PerformanceTestThread(IMap map,int count, boolean bAffChg, boolean bBatch) {
            this.oMap = map;
            this.iCount = count;
            this.bAiffChange = bAffChg;
            this.bBatchUpdate = bBatch;
            this.oOverlay = map.getAllOverlays().get(0);
            oCamera = map.getCamera();
            map.setFarDistanceThreshold(120000);
            map.setMidDistanceThreshold(100000);

            Log.d(TAG, "count " + count + " Affiliation Change " + bAffChg + " Batch Update " + bBatch);
        }

        protected IGeoPosition getRandomCoordinate() {
            IGeoPosition oPos = new GeoPosition();
            double dTemp;

            dTemp = oCamera.getLatitude() + (3 * Math.random()) - 1.5;
            oPos.setLatitude(dTemp);
            dTemp = oCamera.getLongitude() + (3 * Math.random()) - 1.5;
            oPos.setLongitude(dTemp);
            //oPos.setLongitude((Math.random() * 360.0) - 180.0);
            //oPos.setLatitude((Math.random() * 180.0) - 90);
            //oPos.setAltitude(Math.random() * 16000.0);
            oPos.setAltitude(0);

            return oPos;
        }

        private void createTracks() {
            java.util.List<IGeoPosition> oPosList;
            int iNextTrackSymbol = 0;
            int iNextAffiliation = 0;
            String sSymbolCode;
            int iDirectionOfMovement = 0;
            int iSpeed = 600;

            try {
                oOverlay.removeFeatures(oOverlay.getFeatures());
            } catch (EMP_Exception e) {
                e.printStackTrace();
            }
            for (int iIndex = 0; iIndex < this.iCount; iIndex++) {
                try {
                    oPosList = new java.util.ArrayList<>();
                    oPosList.add(getRandomCoordinate());

                    sSymbolCode = aTrackSymbol[iNextTrackSymbol];
                    iNextTrackSymbol++;
                    iNextTrackSymbol %= this.iTrackSymbolCount;

                    // Allocate the new MilStd Symbol with a MilStd version and the symbol code.
                    mil.emp3.api.MilStdSymbol oSPSymbol = new mil.emp3.api.MilStdSymbol(IGeoMilSymbol.SymbolStandard.MIL_STD_2525C, sSymbolCode);

                    // Set the symbols affiliation.
                    oSPSymbol.setAffiliation(this.aAffiliations[iNextAffiliation]);
                    iNextAffiliation++;
                    iNextAffiliation %= this.iAffCount;

                    // Set the echelon.
                    oSPSymbol.setEchelonSymbolModifier(MilStdSymbol.EchelonSymbolModifier.UNIT, MilStdSymbol.Echelon.UNIT);
                    // Set the symbols altitude mode.
                    oSPSymbol.setAltitudeMode(IGeoAltitudeMode.AltitudeMode.RELATIVE_TO_GROUND);
                    //oSPSymbol.setEchelonSymbolModifier(MilStdSymbol.EchelonSymbolModifier.HQ_BRIGADE);

                    // Set the position list with 1 position.
                    oSPSymbol.getPositions().clear();
                    oSPSymbol.getPositions().addAll(oPosList);

                    // Give the feature a name.
                    oSPSymbol.setName("Unit " + iIndex);

                    // Set Direction of movement.
                    oSPSymbol.setModifier(IGeoMilSymbol.Modifier.DIRECTION_OF_MOVEMENT, iDirectionOfMovement + "");
                    iDirectionOfMovement += 10;
                    iDirectionOfMovement %= 360;

                    // Set Speed.
                    oSPSymbol.setModifier(IGeoMilSymbol.Modifier.SPEED, iSpeed + "");
                    oOverlay.addFeature(oSPSymbol, true);
                } catch (EMP_Exception e) {
                    e.printStackTrace();
                }
            }


            oFeatureList = oOverlay.getFeatures();

            for (int iIndex = 0; iIndex < this.TIME_LIST_SIZE; iIndex++) {
                this.alTimeList[iIndex] = 0;
            }
        }

        private void removeTracks() {
            try {
                oOverlay.removeFeatures(oFeatureList);
            } catch (EMP_Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void run() {
            mil.emp3.api.MilStdSymbol oSPSymbol;
            int iNextFeature = 0;
            long lStartTimestamp;
            long lDeltaTime = 0;
            IGeoPosition oPos, oNewPos;
            int iDOM;
            double dSpeed;
            double dDistance;
            double dRandom;
            long lLastMoved = System.currentTimeMillis();
            long dWaitTime  = this.UPDATE_INTERVAL; // msec
            java.util.List<IFeature> oBatchList = new java.util.ArrayList<>();

            this.createTracks();

            while (bContinue) {
                try {
                    oBatchList.clear();
                    sleep(dWaitTime);
                    lStartTimestamp = System.currentTimeMillis();
                    for (int iIndex = 0; iIndex < this.COUNT_PER_INTERVAL; iIndex++) {
                        oSPSymbol = (mil.emp3.api.MilStdSymbol) this.oFeatureList.get(iNextFeature);
                        oPos = oSPSymbol.getPosition();
                        iDOM = Integer.parseInt(oSPSymbol.getStringModifier(IGeoMilSymbol.Modifier.DIRECTION_OF_MOVEMENT));
                        dSpeed = (double) Integer.parseInt(oSPSymbol.getStringModifier(IGeoMilSymbol.Modifier.SPEED));
                        dDistance = dSpeed * 1609.0 * (double)(lStartTimestamp - lLastMoved) / 3.6e6;

                        oNewPos = GeographicLib.computePositionAt((double) iDOM, dDistance, oPos);
                        oPos.setLatitude(oNewPos.getLatitude());
                        oPos.setLongitude(oNewPos.getLongitude());
                        dRandom = Math.random();
                        if (dRandom <= 0.2) {
                            // Make the heading 45Deg CCW
                            iDOM += 350;
                            iDOM %= 360;
                            oSPSymbol.setModifier(IGeoMilSymbol.Modifier.DIRECTION_OF_MOVEMENT, iDOM + "");
                        } else if (dRandom >= 0.8) {
                            // Make the heading 45Deg CW
                            iDOM += 10;
                            iDOM %= 360;
                            oSPSymbol.setModifier(IGeoMilSymbol.Modifier.DIRECTION_OF_MOVEMENT, iDOM + "");
                        }

                        if (this.bAiffChange) {
                            dRandom = Math.random();
                            if (dRandom <= 0.25) {
                                // Change the Affilication.
                                int iAffIndex = (int) Math.floor(Math.random() * this.iAffCount) % this.iAffCount;
                                oSPSymbol.setAffiliation(this.aAffiliations[iAffIndex]);
                            }
                        }

                        if (this.bBatchUpdate) {
                            oBatchList.add(oSPSymbol);
                        } else {
                            oSPSymbol.apply();
                        }

                        iNextFeature++;
                        iNextFeature %= this.iCount;
                        if (iNextFeature == 0) {
                            lLastMoved = lStartTimestamp;
                            break;
                        }
                    }

                    if (this.bBatchUpdate) {
                        try {
                            for (IFeature oSymbol: oBatchList) {
                                oSymbol.apply();
                            }
                            //this.oMainActivity.oRootOverlay.addFeatures(oBatchList, true);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    // Cal the time it took to update the COUNT_PER_INTERVAL units.
                    lDeltaTime = System.currentTimeMillis() - lStartTimestamp;
                    dWaitTime = (lDeltaTime < this.UPDATE_INTERVAL)? this.UPDATE_INTERVAL - lDeltaTime: 50;
                    if (this.alTimeList[this.iTimelistIndex] != 0) {
                        iTimeSamples--;
                        this.dTimeSum -= (double) this.alTimeList[this.iTimelistIndex];
                    }
                    iTimeSamples++;
                    this.alTimeList[this.iTimelistIndex] = lDeltaTime;
                    this.dTimeSum += (double) lDeltaTime;
                    this.iTimelistIndex++;
                    this.iTimelistIndex %= this.TIME_LIST_SIZE;
                    sMessage = String.format("Update %1$,4d features in %2$,4d msec. Avg of %3$,4d = %4$8.3f msec", this.COUNT_PER_INTERVAL, lDeltaTime, iTimeSamples,
                            this.dTimeSum / (double) iTimeSamples);

                    oCamera = oMap.getCamera();

                    String labels = oMap.getMilStdLabels().toString();

                    sMessage = String.format(Locale.US, "%1s L:N:A %2$6.3f %3$6.3f %4$6.0f F:M %5$6.1f %6$6.1f %7$d ", oMap.getName(),
                            oCamera.getLatitude(), oCamera.getLongitude(), oCamera.getAltitude(),
                            oMap.getFarDistanceThreshold(), oMap.getMidDistanceThreshold(),
                            iCount);
                    sMessage += oMap.getMilStdLabels().toString();
                    String uMessage = String.format(" U:%1$d in %2$d ms", this.COUNT_PER_INTERVAL, lDeltaTime);
                    sMessage += uMessage;
                    updateStatus(sMessage);

                } catch (InterruptedException Ex) {

                }
            }
            this.removeTracks();
        }

        public void stopTest() {
            this.bContinue = false;
            this.interrupt();
        }
    }
}
