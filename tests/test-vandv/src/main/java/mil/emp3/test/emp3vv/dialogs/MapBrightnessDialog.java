package mil.emp3.test.emp3vv.dialogs;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import mil.emp3.api.interfaces.IMap;
import mil.emp3.test.emp3vv.R;
import mil.emp3.test.emp3vv.common.Emp3TesterDialogBase;

/**
 * This class implements the map brightness dialog box.
 */

public class MapBrightnessDialog extends Emp3TesterDialogBase {
    private static String TAG = MapBrightnessDialog.class.getSimpleName();

    public interface IBrightnessDialogListener extends IEmp3TesterDialogBaseListener {
        void onDone();
    }

    public MapBrightnessDialog() {
        // Empty constructor is required for DialogFragment
        // Make sure not to add arguments to the constructor
        // Use `newInstance` instead as shown below
    }

    public static MapBrightnessDialog newInstance(String title, IBrightnessDialogListener listener, IMap map) {
        MapBrightnessDialog frag = new MapBrightnessDialog();
        Bundle args = new Bundle();
        args.putString("title", title);
        frag.setArguments(args);
        frag.init(map, listener);

        return frag;
    }

    public static MapBrightnessDialog newInstanceForOptItem(String title, IBrightnessDialogListener listener, IMap map) {
        MapBrightnessDialog frag = new MapBrightnessDialog();
        Bundle args = new Bundle();
        args.putString("title", title);
        frag.setArguments(args);
        frag.initForOptItem(map, listener);

        return frag;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.brightness_dialog, container);
        setDialogPosition();
        return v;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        final TextView brightnessCtrl = (TextView) view.findViewById(R.id.brightnessValue);
        if (null != brightnessCtrl) {
            brightnessCtrl.setText("" + MapBrightnessDialog.this.map.getBackgroundBrightness());

            ImageButton increaseBrightnessBtn = (ImageButton) view.findViewById(R.id.brightnessUp);
            if (null != increaseBrightnessBtn) {
                increaseBrightnessBtn.setOnClickListener(v -> {
                    int value = Integer.parseInt(brightnessCtrl.getText().toString()) + 5;

                    if (value > 100) {
                        value = 100;
                    }
                    brightnessCtrl.setText("" + value);
                    MapBrightnessDialog.this.map.setBackgroundBrightness(value);
                });
            }

            ImageButton decreaseBrightnessBtn = (ImageButton) view.findViewById(R.id.brightnessDown);
            if (null != decreaseBrightnessBtn) {
                decreaseBrightnessBtn.setOnClickListener(v -> {
                    int value = Integer.parseInt(brightnessCtrl.getText().toString()) - 5;

                    if (value < 0) {
                        value = 0;
                    }
                    brightnessCtrl.setText("" + value);
                    MapBrightnessDialog.this.map.setBackgroundBrightness(value);
                });
            }
            Button doneBtn = (Button) view.findViewById(R.id.doneBtn);
            if (null != doneBtn) {
                doneBtn.setOnClickListener(v -> {
                    ((IBrightnessDialogListener) MapBrightnessDialog.this.listener).onDone();
                    MapBrightnessDialog.this.dismiss();
                });
            }
        }
    }
}
