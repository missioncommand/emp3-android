package mil.emp3.test.emp3vv.navItems.kml_service_test;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import mil.emp3.api.interfaces.IMap;
import mil.emp3.test.emp3vv.R;
import mil.emp3.test.emp3vv.common.Emp3TesterDialogBase;
import mil.emp3.test.emp3vv.dialogs.utils.ErrorDialog;

public class KmlsSettingsDialog extends Emp3TesterDialogBase {

    private static String TAG = KmlsSettingsDialog.class.getSimpleName();

    EditText kmlsName;
    EditText kmlsURL;

    int whichMap;
    String url;

    public interface IKmlsSettingsDialogListener extends IEmp3TesterDialogBaseListener {
        void kmlsSet(KmlsSettingsDialog kmlsSettingsDialog);
    }
    public KmlsSettingsDialog() {
        // Empty constructor is required for DialogFragment
        // Make sure not to add arguments to the constructor
        // Use `newInstance` instead as shown below
    }

    public static KmlsSettingsDialog newInstance(String title, IKmlsSettingsDialogListener listener, IMap map, int whichMap, String url) {
        KmlsSettingsDialog frag = new KmlsSettingsDialog();
        Bundle args = new Bundle();
        args.putString("title", title);
        frag.setArguments(args);
        frag.init(map, listener);
        frag.whichMap = whichMap;
        frag.url = url;
        return frag;
    }

    public static KmlsSettingsDialog newInstanceForOptItem(String title, IKmlsSettingsDialogListener listener, IMap map) {
        KmlsSettingsDialog frag = new KmlsSettingsDialog();
        Bundle args = new Bundle();
        args.putString("title", title);
        frag.setArguments(args);
        frag.initForOptItem(map, listener);
        return frag;
    }

    public String getName() {
        if(null != kmlsName) {
            return kmlsName.getText().toString();
        } else {
            return null;
        }
    }

    public String getUrl() {

        if(null != url) {
            return url;
        }
        if(null != kmlsURL) {
            return kmlsURL.getText().toString();
        } else {
            return null;
        }
    }

    public int getWhichMap() {
        return whichMap;
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.kmls_settings_dialog, container);
        setDialogPosition();
        return v;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        kmlsName = (EditText) view.findViewById(R.id.KmlsName);
        kmlsURL = (EditText) view.findViewById(R.id.KmlsUrl);

        if(null != url) {
            kmlsURL.setVisibility(View.GONE);
        }
        Button doneButton = (Button) view.findViewById(R.id.done);
        doneButton.setOnClickListener(v -> KmlsSettingsDialog.this.dismiss());

        Button applyButton = (Button) view.findViewById(R.id.apply);
        applyButton.setOnClickListener(v -> {
            String name = kmlsName.getText().toString();
            if (null == name || 0 == name.length()) {
                ErrorDialog.showError(getContext(), "Service name must non-null and unique");
                return;
            }

            if (null == url) {
                String url1 = kmlsURL.getText().toString();
                if (null == url1 || 0 == url1.length()) {
                    ErrorDialog.showError(getContext(), "URL must be non-null");
                    return;
                }
            }

            KmlsSettingsDialog.this.dismiss();
            if (null != listener) {
                ((IKmlsSettingsDialogListener) listener).kmlsSet(KmlsSettingsDialog.this);
            }
        });
    }
}
