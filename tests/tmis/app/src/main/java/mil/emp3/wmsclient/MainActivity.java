// This client is based the TMIS test client
// The original client was written in C# and Xamarin and Visual Studio
// It was converted from C# to Java and built with Gradle
// by Raju Supnekar, March 2016
// It was tested with the TMIS Service on MFoCS
// It was also tested with another WMS provider GeoServer running
// on Tomcat.  That test was done using the Android Emulator

// To pick the server for testing, change the wmsServerUrl below.


package mil.emp3.wmsclient;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.content.Context;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import java.io.UnsupportedEncodingException;

import java.net.URLEncoder;


public class MainActivity extends Activity {

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private static String INTENT_TMIS_SERVICE = "wms.TMISService";
    private static String ACTION_TMIS_STARTED = "wms.TMISService.STARTED";
    private static String ACTION_TMIS_STOPPING = "wms.TMISService.STOPPED";

    /// <summary>
    /// Used to track the current running state of the TMIS Service
    /// </summary>
    private boolean serviceRunning = false;

    /// <summary>
    /// Receiver for intents broadcasted by the TMIS service
    /// </summary>
    private BroadcastReceiver tmisBrodcastReceiver;

    /// <summary>
    /// The TMIS server url.
    /// </summary>
    private String wmsServerUrl = "http://127.0.0.1:8080/WmsServer.ashx";//"http://10.0.2.2:8080/geoserver/wms";
    private static String TMIS_SERVER_URL = "http://127.0.0.1:8080/WmsServer.ashx";
    private boolean isTMIS = false;
    private Resources resources;
    private String[] paramNames = null;
    private TypedArray paramViewIds = null;

    /// <summary>
    /// Called when this Activity is created to setup the layout and UI elements
    /// </summary>
    /// <param name="bundle">unused</param>
    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);

        // Set our view from the "main" layout resource
        setContentView(R.layout.activity_main);

//        WebView.EnablePlatformNotifications(); This call is obsolete, Raju 3/23/2016
        final WebView responseViewer = (WebView) findViewById(R.id.ResponseViewer);
        WebSettings webSettings = responseViewer.getSettings();
        if (webSettings != null) {
            webSettings.setJavaScriptEnabled(true);
        }
        responseViewer.setWebViewClient(new ProxyWebViewClient(this));
        responseViewer.setWebChromeClient(new WebChromeClient());

        resources = getResources();
        paramNames = resources.getStringArray(R.array.ParamNames);
        paramViewIds = resources.obtainTypedArray(R.array.ParamViewIds);

        // Hook up the "start/stop service" button
        isTMIS = wmsServerUrl.equals(TMIS_SERVER_URL);
        serviceRunning = !isTMIS;
        if (isTMIS) {
            Button toggleServiceBtn = (Button) findViewById(R.id.ToggleServiceBtn);
            if (toggleServiceBtn != null) {
                toggleServiceBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        toggleTMISService();
                    }
                });
            }
        }

        // Hook up temporary testing "Google" button
        Button googleBtn = (Button) findViewById(R.id.Google);
        if (googleBtn != null) {
            googleBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    responseViewer.loadUrl("http://www.google.com");
                }
            });
        }

        // Hook up the "map launcher" button
        Button mapLauncherBtn = (Button) findViewById(R.id.MapLauncherBtn);
        if (mapLauncherBtn != null) {
            mapLauncherBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent mapIntent = new Intent(MainActivity.this, MapViewActivity.class);
                    startActivity(mapIntent);
                }
            });
        }

        // Hook up the "get capabilities" button
        Button getCapabilitiesBtn = (Button) findViewById(R.id.GetCapabilitiesBtn);
        if (getCapabilitiesBtn != null) {
            getCapabilitiesBtn.setEnabled(serviceRunning);
            getCapabilitiesBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.out.println("Get Capabilities Button");
                /// <summary>
                /// Makes a "GetCapabilities" request in the WebView for the TMIS Service to handle.
                /// </summary>
                String version = ((EditText) findViewById(R.id.VersionText)).getText().toString();
                String url = wmsServerUrl + "?service=WMS&request=GetCapabilities&version=" + version;
                System.out.println("URL requested " + url);
                responseViewer.loadUrl(url);
            }
        });
        }

        // Hook up the "toggle request params" button
        Button toggleRequestParamsBtn = (Button)findViewById(R.id.ToggleRequestParamsBtn);
        if (toggleRequestParamsBtn != null) {
            toggleRequestParamsBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    LinearLayout layout = (LinearLayout) findViewById(R.id.WmsParametersLayout);
                    layout.setVisibility(layout.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
                }
            });
        }

        // Hook up the "make request" button
        Button makeRequestBtn = (Button)findViewById(R.id.MakeRequestBtn);
        if (makeRequestBtn != null) {
            makeRequestBtn.setEnabled(serviceRunning);
            makeRequestBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    System.out.println("Make Request Button");
                    String url = makeRequest();
                    responseViewer.loadUrl(url);
                }
            });
        }

        // Hook up the preset requests spinner
        Spinner presetRequests = (Spinner)findViewById(R.id.PresetRequests);
        if (presetRequests != null) {
//            presetRequests.setEnabled(false);
            presetRequests.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                /// <summary>
                /// Sets the presets parameter values and potentially enables custom editing of the parameters.
                /// </summary>
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    String selection = parent.getSelectedItem().toString();
                    boolean custom = selection.equals("Custom");

                    TypedArray presetIds = resources.obtainTypedArray(R.array.PresetRequestIds);
                    int selectedPreset = presetIds.getResourceId(position, R.array.GeoPkg_Caribean_Imagery);
                    String[] paramValues = resources.getStringArray(selectedPreset);
                    for (int i = 0; i < paramValues.length; ++i) {
                        int textId = paramViewIds.getResourceId(i, 0);
                        if (textId == 0) // invalid resource id
                            continue;
                        EditText editText = (EditText) findViewById(textId);
                        if (!custom)
                            editText.setText(paramValues[i]);
                        editText.setEnabled(custom && serviceRunning);
                    }
                    findViewById(R.id.SRSRadioBtn).setEnabled(custom && serviceRunning);
                    findViewById(R.id.CRSRadioBtn).setEnabled(custom && serviceRunning);
                    presetIds.recycle();
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });
            ArrayAdapter adapter = ArrayAdapter.createFromResource(
                    this, R.array.PresetRequests, android.R.layout.simple_spinner_item);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            presetRequests.setAdapter(adapter);
        }

        // Hook up the SRS and CRS radio buttons
        Button srsRadioBtn = (Button)findViewById(R.id.SRSRadioBtn);
        final String srsRadioHint = srsRadioBtn.getText().toString();
        srsRadioBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText editText = (EditText) findViewById(R.id.ReferenceSystemText);
                editText.setHint(srsRadioHint);
            }
        });
        Button crsRadioBtn = (Button)findViewById(R.id.CRSRadioBtn);
        final String crsRadioHint = crsRadioBtn.getText().toString();
        crsRadioBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText editText = (EditText) findViewById(R.id.ReferenceSystemText);
                editText.setHint(crsRadioHint);
            }
        });

        // Register to receive broadcast intents from the TMIS Service indicating it starting and stopping
        if (isTMIS) {
            IntentFilter tmisIntentFilter = new IntentFilter();
            tmisIntentFilter.addAction(ACTION_TMIS_STARTED);
            tmisIntentFilter.addAction(ACTION_TMIS_STOPPING);
            tmisBrodcastReceiver = new TMISServiceIntentReceiver();
            registerReceiver(tmisBrodcastReceiver, tmisIntentFilter);

            // Make sure the TMIS service is running
//            if (!serviceRunning)
//                startService(new Intent(INTENT_TMIS_SERVICE));
        } else {
            serviceChangedState(serviceRunning);
        }
    }

    /// <summary>
    /// Called when this Activity is being torn down.
    /// </summary>
    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        if (tmisBrodcastReceiver != null)
        {
            unregisterReceiver(tmisBrodcastReceiver);
        }
    }

    /// <summary>
    /// Toggles the TMIS Service on and off
    /// </summary>
    private void toggleTMISService()
    {
        if (serviceRunning)
        {
            (Toast.makeText(this, R.string.StopServiceToast, Toast.LENGTH_SHORT)).show();
            stopService(new Intent(INTENT_TMIS_SERVICE));
        }
        else
        {
            (Toast.makeText(this, R.string.StartServiceToast, Toast.LENGTH_SHORT)).show();
            startService(new Intent(INTENT_TMIS_SERVICE));
        }
    }

    /// <summary>
    /// Called whenever the TMIS Service state changes to update the UI accordingly
    /// </summary>
    /// <param name="running">Whether or not the TMIS Service is running</param>
    private void serviceChangedState(boolean running)
    {
        System.out.println("Service state " + (running ? "running" : "stopped"));
        serviceRunning = running;

        // Toggle the service button's stop/start message
        Button toggleServiceBtn = (Button)findViewById(R.id.ToggleServiceBtn);
        int toggleTextId = (running ? R.string.StopServiceBtn : R.string.StartServiceBtn);
        toggleServiceBtn.setText(resources.getString(toggleTextId));

        // Enable/Disable client side WMS capabilities
        findViewById(R.id.MapLauncherBtn).setEnabled(running);
        findViewById(R.id.GetCapabilitiesBtn).setEnabled(running);
        findViewById(R.id.MakeRequestBtn).setEnabled(running);

        // Enable/Disable WMS param fields based on selection and whether or not the service is running
        Spinner presets = (Spinner)findViewById(R.id.PresetRequests);
        String presetSelection = (String)presets.getSelectedItem();
        boolean customSelected = presetSelection.equals("Custom");

        findViewById(R.id.SRSRadioBtn).setEnabled(customSelected && running);
        findViewById(R.id.CRSRadioBtn).setEnabled(customSelected && running);
        ViewGroup wmsParametersLayout = (ViewGroup)findViewById(R.id.WmsParametersLayout);
        for (int i = 0; i < wmsParametersLayout.getChildCount(); ++i)
        {
            wmsParametersLayout.getChildAt(i).setEnabled(customSelected && running);
        }
        presets.setEnabled(running);

        // if the service is running make the initial request
        if (running)
        {
            String url = makeRequest();
            final WebView responseViewer = (WebView) findViewById(R.id.ResponseViewer);
            responseViewer.loadUrl(url);

        }
    }

    /// <summary>
    /// Makes a request to the TMIS Service as specified by the user input fields
    /// </summary>
    private String makeRequest()
    {
        System.out.println("Making request");
        EditText editText;
        String url = wmsServerUrl;

        // build up the url query from the parameters specified by the user
        for (int i = 0; i < paramNames.length; ++i)
        {
            int textId = paramViewIds.getResourceId(i, 0);
            if (textId == 0) {
                System.out.println("Invalid resource id");
                continue;
            }

            editText = (EditText)findViewById(textId);

            // don't incude parameters that the user didn't enter
            if (editText.getText() == null || editText.getText().toString().isEmpty())
                continue;

            String editString = editText.getText().toString();

            // if this is the reference system parameter set the parameter
            // name to the reference system acronym selected by the user
            // (note that the EditText view's hint is set to the selected
            // reference system acronym)
            if (textId == R.id.ReferenceSystemText)
            {
                paramNames[i] = editText.getHint().toString();
            }

            try {
                url += (i == 0 ? "?" : "&") + paramNames[i] + "=" + URLEncoder.encode(editString, "UTF-8");
            } catch (UnsupportedEncodingException u) {
                u.printStackTrace();
            }
        }

        System.out.println("URL requested " + url);
        WebView responseViewer = (WebView)findViewById(R.id.ResponseViewer);
        return url;
    }

    /// <summary>
    /// Receives broacast Intents from the TMIS Service such that this activity can react to
    /// any changes in it's state.
    /// </summary>
    class TMISServiceIntentReceiver extends BroadcastReceiver
    {

        public TMISServiceIntentReceiver()
        {
        }

        @Override
        public void onReceive(Context context, Intent intent)
        {
            if (ACTION_TMIS_STARTED.equals(intent.getAction())) {
                serviceChangedState(true);
            } else if (ACTION_TMIS_STOPPING.equals(intent.getAction())) {
                serviceChangedState(false);
            }
        }
    }

}
