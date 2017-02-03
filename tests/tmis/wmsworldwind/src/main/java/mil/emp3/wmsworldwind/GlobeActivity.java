package mil.emp3.wmsworldwind;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.support.annotation.LayoutRes;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import java.util.ArrayList;

import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.layer.BackgroundLayer;
import gov.nasa.worldwind.layer.BlueMarbleLandsatLayer;
import mil.emp3.api.WMS;
import mil.emp3.api.enums.WMSVersionEnum;
import mil.emp3.api.events.MapStateChangeEvent;
import mil.emp3.api.events.MapUserInteractionEvent;
import mil.emp3.api.interfaces.IMap;
import mil.emp3.api.listeners.IMapInteractionEventListener;
import mil.emp3.api.listeners.IMapStateChangeEventListener;

public class GlobeActivity extends AppCompatActivity
    implements NavigationView.OnNavigationItemSelectedListener {

    private final static String TAG = GlobeActivity.class.getSimpleName();
    private IMap map = null;
    WMS wmsService = null;
    WMS oldWMSService = null;
        static private int selectedItemId = R.id.TMIS_WMS;

        private ActionBarDrawerToggle drawerToggle;

        private NavigationView navigationView;

        protected String aboutBoxTitle = "Title goes here";
        protected String aboutBoxText = "Description goes here;";

        @Override
        public void setContentView(@LayoutRes int layoutResID) {
            super.setContentView(layoutResID);
            onCreateDrawer();
        }

    protected void onCreateDrawer() {

        // Add support for a Toolbar and set to act as the ActionBar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        // setSupportActionBar(toolbar);

        // Add support for the navigation drawer full of examples
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        this.drawerToggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(this.drawerToggle);
        this.drawerToggle.syncState();

        this.navigationView = (NavigationView) findViewById(R.id.nav_view);
        this.navigationView.setNavigationItemSelectedListener(this);
        this.navigationView.setCheckedItem(selectedItemId);
    }

    @Override
    protected void onResume() {
        super.onResume();
        this.navigationView.setCheckedItem(selectedItemId);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        return super.onOptionsItemSelected(item);
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
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        this.drawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        this.drawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Persist the selected item between Activities
        selectedItemId = item.getItemId();

        // Handle navigation view item clicks here.
        switch (selectedItemId) {

            case R.id.TMIS_WMS:
                addWMSLayer(R.array.TMIS);
                break;
            case R.id.NASANeoWMS_WMS:
                addWMSLayer(R.array.NASANeoWMS);
                break;
            case R.id.LandSat_WMS:
                addWMSLayer(R.array.LandSat);
                break;
            case R.id.OpenStreetMap_WMS:
                addWMSLayer(R.array.OpenStreetMap);
                break;
            case R.id.TIGRwms_WMS:
                addWMSLayer(R.array.TIGRWms);
                break;
            case R.id.TIGRelevation_WMS:
                addWMSLayer(R.array.TIGRelevation);
                break;
        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }


    private void addWMSLayer(int wmsServer) {
        try {
            if (map == null) {
                map = (IMap) findViewById(R.id.map);
                map.addMapStateChangeEventListener(new IMapStateChangeEventListener() {
                    @Override
                    public void onEvent(MapStateChangeEvent mapStateChangeEvent) {
                        Log.d(TAG, "mapStateChangeEvent " + mapStateChangeEvent.getNewState());
                    }
                });
                map.addMapInteractionEventListener(new IMapInteractionEventListener() {
                    @Override
                    public void onEvent(MapUserInteractionEvent mapUserInteractionEvent) {
                        Log.d(TAG, "mapUserInteractionEvent " + mapUserInteractionEvent.getPoint().x);
                    }
                });
            }
            String[] params = getResources().getStringArray(wmsServer);
            ArrayList<String> layers = new ArrayList<>();
            layers.add(params[4]);
            wmsService = new WMS(params[0],
                    params[1].equals("1.1.1") ? WMSVersionEnum.VERSION_1_1 : WMSVersionEnum.VERSION_1_3,
                    params[2].equals("null") ? null : params[2],  // tile format
                    params[3].equals("false"), // transparent
                    layers);
            if (wmsService != null) {
                if (wmsService != oldWMSService) {
                    if (oldWMSService != null)
                        map.removeMapService(oldWMSService);
                    else
                        Log.i(TAG, "No previous WMS service");
                    map.addMapService(wmsService);
                    oldWMSService = wmsService;
                } else {
                    Log.i(TAG, "Layer unchanged");
                }
            } else {
                Log.i(TAG, "Got null WMS service");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_globe);
    }

}
