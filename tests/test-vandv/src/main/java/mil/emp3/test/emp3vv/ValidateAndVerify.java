package mil.emp3.test.emp3vv;

import android.os.Bundle;

import android.util.Log;

import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import mil.emp3.api.enums.MapStateEnum;
import mil.emp3.api.events.MapStateChangeEvent;
import mil.emp3.api.exceptions.EMP_Exception;
import mil.emp3.api.interfaces.IMap;
import mil.emp3.api.listeners.IMapStateChangeEventListener;
import mil.emp3.test.emp3vv.common.ExecuteTest;
import mil.emp3.test.emp3vv.utils.CameraUtility;

/**
 * Anyone with intention of adding anything to the tester should start reading here.
 *
 * What is on the screen?
 *   - Hamburger menu on top left corner
 *        This contains tests that are setup to capture high level capabilities of the EMP3 API, such as Add/Update/Delete Overlays/Features,
 *        Zoom on a set of features/overlays, Freehand drawing etc. To add tests this menu, update file res/menu/activity_validate_and_verify_drawer.xml
 *        Additional documentation on how to proceed from there is provided in navItems/AddRemoveGet test
 *  - Actions menu on the right hand side of the title bar
 *        This consists of some buttons on the title bar(often used) and then the drop down from the vertical ellipses.
 *        These are NOT tests, loosely speaking there are either get operations or setting at map level, such as Camera, MDT/FDT etc
 *        If you want to add action items then update res/menu/validate_and_verify.xml
 *        Additional documentation on how to proceed from here is provided in optItems/Camera.java
 *  - Map itself
 *        Much of the screen is occupied by the map.
 *        Map is launched by selecting Launch Map on the hamburger menu.
 *        In future Launch Map test may have buttons added to select map engine and other options.
 *  - Between the Map and the title bar is the area reserved for individual Capability Tests that are executed from the hamburger menu.
 *        Additional, explanation is provided in navItems/AddRemoveTest.java
 *
 *  - Special note on dialogs: Dialogs launched from the settings option menu, e.g. Camera are MODAL dialogs, see example CameraDialog.java
 *        Dialogs launched by your test can be made non-MODAL if you extend Emp3TesterDialogBase, please see documentation in that class. Also
 *        note the special one line check that you will need to add to your actOn method.
 *
 * Philosophy behind the Capability Test is to allow the tester/developer to focus on the capability under test and not worry about how to set up
 * the required hierarchy of overlays/features. Each test should be self contained and easy for the tester to follow. We should know the
 * objective of the test.
 *
 * NOTE: Each test is NOT an independent activity. All tests are hosted by one Main Activity and they all operate on the same map that was
 * launched initially using 'Launch Map'
 */
public class ValidateAndVerify extends MapFragmentAndViewActivity
        implements NavigationView.OnNavigationItemSelectedListener, MapTestMenuFragment.OnNavItemSelectedListener {
    private static String TAG = ValidateAndVerify.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_validate_and_verify);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close) {
            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
                super.onDrawerSlide(drawerView, slideOffset);
                    drawerView.bringToFront();
                    getWindow().getDecorView().requestLayout();
                    getWindow().getDecorView().invalidate();

            }
        };

        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        testStatus = (TextView) findViewById(R.id.TestStatus);
        testStatus.setText("Please Launch Map before selecting any action");
        testMenuFragment = ( mil.emp3.test.emp3vv.MapTestMenuFragment) getFragmentManager().findFragmentById(R.id.testMenuFragment);

        testMenuFragment.setNavigationView(navigationView);
        testMenuFragment.preLaunchMap();
        map = (IMap) findViewById(R.id.map);
        try {
            map.addMapStateChangeEventListener(new IMapStateChangeEventListener() {
                @Override
                public void onEvent(MapStateChangeEvent mapStateChangeEvent) {
                    Log.d(TAG, "mapStateChangeEvent map: " + mapStateChangeEvent.getNewState());
                    try {
                        if(mapStateChangeEvent.getNewState() == MapStateEnum.MAP_READY) {
                            ExecuteTest.setMapReady(ExecuteTest.MAP1, true);
                            onTestCompleted("Map Ready");
                            testStatus.setText("map: Map Status Changed to " + mapStateChangeEvent.getNewState() + " Select capability from the drawer");
                        } else {
                            testStatus.setText("map: Map Status Changed to " + mapStateChangeEvent.getNewState());
                        }
                        // map.setCamera(CameraUtility.buildCamera(33.9424368, -118.4081222, 2000000.0), false);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });

            /*
                Following listener code was added to verify EMP-2702. It seems like EMP-2702 is no longer an issue.
            map.addMapViewChangeEventListener(new IMapViewChangeEventListener() {
                @Override
                public void onEvent(MapViewChangeEvent event) {
                    Log.d(TAG, "mapViewChangeEventListener.onEvent map: " + event.getEvent());
                    ICamera camera = event.getCamera();
                    Log.d(TAG, "mapViewChangeEventListener.onEvent " + camera.getLatitude() + "/" + camera.getLongitude());
                }
            });

            map.addCameraEventListener(new ICameraEventListener() {
                @Override
                public void onEvent(CameraEvent event) {
                    Log.d(TAG, "CameraEventListener.onEvent map: " + event.getEvent());
                    ICamera camera = event.getCamera();
                    Log.d(TAG, "CameraEventListener.onEvent " + camera.getLatitude() + "/" + camera.getLongitude());
                }
            });
            */
        } catch (EMP_Exception e) {
            e.printStackTrace();
        }
        map.setName("map1");
        Log.d(TAG, "map name " + map.getName());

        map2 = (IMap) findViewById(R.id.map2);
        try {
            map2.addMapStateChangeEventListener(new IMapStateChangeEventListener() {
                @Override
                public void onEvent(MapStateChangeEvent mapStateChangeEvent) {
                    Log.d(TAG, "mapStateChangeEvent map2: " + mapStateChangeEvent.getNewState());
                    try {
                        if(mapStateChangeEvent.getNewState() == MapStateEnum.MAP_READY) {
                            ExecuteTest.setMapReady(ExecuteTest.MAP2, true);
                            onTestCompleted("Map Ready");
                            testStatus.setText("map2: Map Status Changed to " + mapStateChangeEvent.getNewState() + " Select capability from the drawer");
                        } else {
                            testStatus.setText("map2: Map Status Changed to " + mapStateChangeEvent.getNewState());
                        }
                        map2.setCamera(CameraUtility.buildCamera(33.9424368, -118.4081222, 2000000.0), false);
                    } catch (EMP_Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        } catch (EMP_Exception e) {
            e.printStackTrace();
        }
        map2.setName("map2");
        Log.d(TAG, "map2 name " + map.getName());
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.validate_and_verify, menu);
        return true;
    }

    /**
     * Options menu has common items like CLEAR MAP, CAMERA, DISTANCE THRESHOLDS etc. Developers can add other items that are similar
     * to 'settings' category here. They can be executed while user is executing some test selected from Navigation Menu
     * @param item
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        Log.d(TAG, "onOptionsItemSelected " + item.toString());
        testMenuFragment.optionsItemSelected(item);
        return super.onOptionsItemSelected(item);
    }

    /**
     * These are the developer supplied tests for various features supported by EMP3 API such as "Add Remove Get", "ZOOM", "Freehand Draw" etc.
     * These tests can have custom menus that are Buttons displayed in a widget above the map.
     * @param item
     * @return
     */
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        Log.d(TAG, "onNavigationItemSelected " + item.toString());
        testMenuFragment.onNavigationItemSelected(item);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
