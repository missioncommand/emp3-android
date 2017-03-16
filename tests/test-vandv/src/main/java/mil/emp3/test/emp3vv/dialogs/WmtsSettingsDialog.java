package mil.emp3.test.emp3vv.dialogs;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import java.util.ArrayList;
import java.util.List;

import mil.emp3.api.enums.WMTSVersionEnum;
import mil.emp3.api.interfaces.IMap;
import mil.emp3.test.emp3vv.R;
import mil.emp3.test.emp3vv.common.Emp3TesterDialogBase;

/**
 * Dialog for adding a WMTS service.
 */

public class WmtsSettingsDialog extends Emp3TesterDialogBase {

    private EditText name;
    private EditText url;
    private Spinner version;
    private Spinner tileFormat;
    private EditText layer1;
    private EditText layer2;
    private EditText layer3;

    public WmtsSettingsDialog() {

    }

    public static WmtsSettingsDialog newInstance(String title, IWmtsSettingsDialogListener listener,
                                                IMap map) {
        WmtsSettingsDialog wmtsSettingsDialog = new WmtsSettingsDialog();
        Bundle args = new Bundle();
        args.putString("title", title);
        wmtsSettingsDialog.setArguments(args);
        wmtsSettingsDialog.init(map, listener);
        return wmtsSettingsDialog;
    }

    public static WmtsSettingsDialog newInstanceForOptItem(String title, IWmtsSettingsDialogListener listener,
                                                          IMap map) {
        WmtsSettingsDialog wmtsSettingsDialog = new WmtsSettingsDialog();
        Bundle args = new Bundle();
        args.putString("title", title);
        wmtsSettingsDialog.setArguments(args);
        wmtsSettingsDialog.initForOptItem(map, listener);
        return wmtsSettingsDialog;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.wmts_settings_dialog, container);
        setDialogPosition();
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        name = (EditText) view.findViewById(R.id.WmtsNameText);
        url = (EditText) view.findViewById(R.id.WmtsUrlText);
        ArrayAdapter<CharSequence> versionAdapter = ArrayAdapter.createFromResource(getActivity(),
                R.array.wmts_versions, android.R.layout.simple_spinner_item);
        version = (Spinner)view.findViewById(R.id.WmtsVersionText);
        version.setAdapter(versionAdapter);
        ArrayAdapter<CharSequence> tileAdapter = ArrayAdapter.createFromResource(getActivity(),
                R.array.image_formats, android.R.layout.simple_spinner_item);
        tileFormat = (Spinner) view.findViewById(R.id.WmtsTileFormatText);
        tileFormat.setAdapter(tileAdapter);
        ArrayAdapter<CharSequence> booleanAdapter = ArrayAdapter.createFromResource(getActivity(),
                R.array.boolean_values, android.R.layout.simple_spinner_item);
        layer1 = (EditText) view.findViewById(R.id.WmtsLayer1Text);
        layer2 = (EditText) view.findViewById(R.id.WmtsLayer2Text);
        layer3 = (EditText) view.findViewById(R.id.WmtsLayer3Text);

        Button doneButton = (Button) view.findViewById(R.id.done);
        doneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                WmtsSettingsDialog.this.dismiss();
            }
        });

        Button applyButton = (Button) view.findViewById(R.id.apply);
        applyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != WmtsSettingsDialog.this.listener) {
                    List<String> layers = new ArrayList<String>();
                    if (!layer1.getText().toString().isEmpty()) {
                        layers.add(layer1.getText().toString());
                    }
                    if (!layer2.getText().toString().isEmpty()) {
                        layers.add(layer2.getText().toString());
                    }
                    if (!layer3.getText().toString().isEmpty()) {
                        layers.add(layer3.getText().toString());
                    }
                    WMTSVersionEnum wmtsVersion = WMTSVersionEnum.valueOf(version.getSelectedItem().toString());

                    ((IWmtsSettingsDialogListener) WmtsSettingsDialog.this.listener)
                            .addWmtsService(map,
                                    name.getText().toString(),
                                    url.getText().toString(),
                                    wmtsVersion,
                                    tileFormat.getSelectedItem().toString(),
                                    layers);
                }
            }
        });
    }

    public interface IWmtsSettingsDialogListener extends IEmp3TesterDialogBaseListener {
        void addWmtsService(IMap map, String name, String url, WMTSVersionEnum wmtsVersion, String tileFormat,
                           List<String> layers);
    }
}
