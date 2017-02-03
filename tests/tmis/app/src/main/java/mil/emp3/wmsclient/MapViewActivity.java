package mil.emp3.wmsclient;

import android.app.Activity;
import android.os.Bundle;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.EditText;

/**
 * Created by raju on 3/22/16.
 */
public class MapViewActivity extends Activity {
    @Override
    protected void onCreate(Bundle bundle)
    {
        super.onCreate(bundle);

        // Set our view from the "main" layout resource
        setContentView(R.layout.mapviewactivity);

        // Load html page displaying OpenLayers WMS Client
//        WebView.EnablePlatformNotifications();  this call is obsolete, Raju 3/23/2016
        WebView mapViewer = (WebView)findViewById(R.id.MapViewer);
        WebSettings webSettings = mapViewer.getSettings();
        webSettings.setJavaScriptEnabled(true);
        mapViewer.setWebViewClient(new ProxyWebViewClient(this));
        mapViewer.setWebChromeClient(new WebChromeClient());
        mapViewer.loadUrl("file:///android_asset/MapOpenLayers2.13.html");
    }
}
