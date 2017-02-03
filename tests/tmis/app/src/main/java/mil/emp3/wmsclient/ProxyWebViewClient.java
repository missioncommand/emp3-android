package mil.emp3.wmsclient;

import android.app.Activity;
import android.content.Context;
import android.webkit.WebView;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.view.View;
import android.view.LayoutInflater;
import android.webkit.HttpAuthHandler;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;

/**
 * Created by raju on 3/22/16.
 */
public class ProxyWebViewClient extends WebViewClient {
    private Activity activity;
    private String username = null;
    private String password = null;

    public ProxyWebViewClient(Activity activity)
    {
        super();
        this.activity = activity;
    }

    @Override
    public void onReceivedHttpAuthRequest(WebView view, final HttpAuthHandler handler, String host, String realm)
    {
        AlertDialog dialog = null;
        AlertDialog.Builder builder = new AlertDialog.Builder(activity, AlertDialog.THEME_DEVICE_DEFAULT_DARK);
        builder.setTitle("Proxy Credentials");
        builder.setMessage("Please enter your username and password for the proxy:");
        builder.setView(LayoutInflater.from(activity).inflate(R.layout.proxyusernamepassworddialog, null));
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog,int id) {
                handler.cancel();
                dialog.cancel();
            }
        });
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog,int id) {
                String username = ((EditText)activity.findViewById(R.id.ProxyUsername)).getText().toString();
                String password = ((EditText)activity.findViewById(R.id.ProxyPassword)).getText().toString();
                handler.proceed(username, password);
                dialog.dismiss();
            }
        });
        dialog = builder.show();
    }

}
