package mil.emp3.test.emp3vv.dialogs;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import mil.emp3.api.interfaces.IMap;
import mil.emp3.test.emp3vv.common.Emp3TesterDialogBase;
import mil.emp3.test.emp3vv.R;

public class WcsSettingsDialog extends Emp3TesterDialogBase {

    private EditText url;
    private EditText coverage;
    private EditText name;

    public WcsSettingsDialog(){}

    public static WcsSettingsDialog newInstance(String title,
                                                IWcsSettingsDialogListener listener,
                                                IMap map) {
        WcsSettingsDialog wcsSettingsDialog = new WcsSettingsDialog();
        Bundle args = new Bundle();
        args.putString("title", title);
        wcsSettingsDialog.setArguments(args);
        wcsSettingsDialog.init(map, listener);
        return wcsSettingsDialog;
    }

    public static WcsSettingsDialog newInstanceForOptItem(String title,
                                                          IWcsSettingsDialogListener listener,
                                                          IMap map) {
        WcsSettingsDialog wcsSettingsDialog = new WcsSettingsDialog();
        Bundle args = new Bundle();
        args.putString("title", title);
        wcsSettingsDialog.setArguments(args);
        wcsSettingsDialog.initForOptItem(map, listener);
        return wcsSettingsDialog;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.wcs_settings_dialog, container);
        setDialogPosition();
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        url = (EditText) view.findViewById(R.id.WcsUrl);
        coverage = (EditText) view.findViewById(R.id.WcsCoverage);
        name = (EditText) view.findViewById(R.id.WcsName);
        Button doneButton = (Button) view.findViewById(R.id.done);
        doneButton.setOnClickListener(v -> WcsSettingsDialog.this.dismiss());

        Button applyButton = (Button) view.findViewById(R.id.apply);
        applyButton.setOnClickListener(v -> {
            if (null != WcsSettingsDialog.this.listener) {
                String urlS = url.getText().toString();
                String covS = coverage.getText().toString();
                String nameS = name.getText().toString();
                ((IWcsSettingsDialogListener)WcsSettingsDialog.this.listener)
                        .addWcsService(map, nameS, urlS, covS);
            }
        });
    }

    public interface IWcsSettingsDialogListener extends IEmp3TesterDialogBaseListener {
        void addWcsService(IMap map, String name, String url, String coverage);
    }
}
