package mil.emp3.mirrorcache.service;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

public class MirrorCacheServiceActivity extends Activity implements View.OnClickListener {
    static final private String TAG = MirrorCacheServiceActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");

        setContentView(R.layout.activity_mirrorcacheservice);

        //... LISTENERS
        findViewById(R.id.button_startService).setOnClickListener(this);
        findViewById(R.id.button_stopService).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        Log.d(TAG, "onClick");

        if (v.getId() == R.id.button_startService) {
            Log.d(TAG, "Starting service..");
            final Intent intent = new Intent(this, MirrorCacheService.class);
            startService(intent);

        } else if (v.getId() == R.id.button_stopService) {
            Log.d(TAG, "Stopping service..");
            final Intent intent = new Intent(this, MirrorCacheService.class);
            stopService(intent);
        }

    }
}
