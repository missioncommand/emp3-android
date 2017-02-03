package mil.emp3.validator;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;

import mil.emp3.validator.fragment.SideNavFragment;

public class ContentActivity extends AppCompatActivity {
    static final private String TAG = ContentActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_content);

        if (savedInstanceState != null) {
            Log.d(TAG, "savedInstanceState != null");
            return;
        }

        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getFragmentManager().beginTransaction().replace(R.id.sideNav_fragment, new SideNavFragment()).commit();
    }

    @Override
    public void onBackPressed() {
        Log.d(TAG, "onBackPressed");
        if (getFragmentManager().getBackStackEntryCount() == 0) {
            new AlertDialog.Builder(this)
                .setTitle("Really Exit?")
                .setMessage("Are you sure you want to exit?")
                .setNegativeButton(android.R.string.no, null)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface arg0, int arg1) {
                        ContentActivity.super.onBackPressed();
                    }
                }).create().show();

        } else {
            getFragmentManager().popBackStack();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.content, menu);
        return true;
    }
}
