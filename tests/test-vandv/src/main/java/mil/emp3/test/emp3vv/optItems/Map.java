package mil.emp3.test.emp3vv.optItems;

import android.app.Activity;
import android.content.DialogInterface;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.widget.ArrayAdapter;

import java.util.ArrayList;
import java.util.List;

import mil.emp3.api.interfaces.IMap;
import mil.emp3.test.emp3vv.common.ExecuteTest;
import mil.emp3.test.emp3vv.common.OptItemBase;

public class Map extends OptItemBase {

    public Map(Activity activity, IMap map1, IMap map2) {
        super(activity, map1, map2, TAG, true);
    }

    private static String TAG = Map.class.getCanonicalName();
    @Override
    public void run() {
        showSelectMapDialog();
    }

    private void showSelectMapDialog() {
        final List<String> availableMaps = new ArrayList<>();

        // Currently we support two maps only
        if(ExecuteTest.getMapReady(ExecuteTest.MAP1)) {
            availableMaps.add("MAP1");
        }
        if(ExecuteTest.getMapReady(ExecuteTest.MAP2)) {
            availableMaps.add("MAP2");
        }
        final int selection = ExecuteTest.getCurrentMap();


        activity.runOnUiThread(new Runnable() {
            public void run() {
                ArrayAdapter<String> availableMapsAdapter = new ArrayAdapter(activity, android.R.layout.simple_list_item_checked, availableMaps);

                AlertDialog.Builder builder = new AlertDialog.Builder(activity)
                        .setTitle("Choose Map")
                        .setSingleChoiceItems(availableMapsAdapter, selection, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                Log.d(TAG, "Selected Map " + which);
                                ExecuteTest.setCurrentMap(which);
                                dialog.cancel();
                            }
                        });

                final AlertDialog dialog = builder.create();
                dialog.show();
            }
        });

    }
}
