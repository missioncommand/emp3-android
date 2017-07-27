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

import mil.emp3.api.enums.WMSVersionEnum;
import mil.emp3.api.interfaces.IMap;
import mil.emp3.test.emp3vv.R;
import mil.emp3.test.emp3vv.common.Emp3TesterDialogBase;

/**
 * Dialog for adding a WMS service.
 */

public class WmsSettingsDialog extends Emp3TesterDialogBase {

    private EditText name;
    private EditText url;
    private Spinner version;
    private Spinner tileFormat;
    private Spinner transparent;
    private EditText layer1;
    private EditText layer2;
    private EditText layer3;
    private EditText resolution;

    public WmsSettingsDialog() {

    }

    public static WmsSettingsDialog newInstance(String title, IWmsSettingsDialogListener listener,
                                                IMap map) {
        WmsSettingsDialog wmsSettingsDialog = new WmsSettingsDialog();
        Bundle args = new Bundle();
        args.putString("title", title);
        wmsSettingsDialog.setArguments(args);
        wmsSettingsDialog.init(map, listener);
        return wmsSettingsDialog;
    }

    public static WmsSettingsDialog newInstanceForOptItem(String title, IWmsSettingsDialogListener listener,
                                                IMap map) {
        WmsSettingsDialog wmsSettingsDialog = new WmsSettingsDialog();
        Bundle args = new Bundle();
        args.putString("title", title);
        wmsSettingsDialog.setArguments(args);
        wmsSettingsDialog.initForOptItem(map, listener);
        return wmsSettingsDialog;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.wms_settings_dialog, container);
        setDialogPosition();
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        name = (EditText) view.findViewById(R.id.NameText);
        url = (EditText) view.findViewById(R.id.UrlText);
        ArrayAdapter<CharSequence> versionAdapter = ArrayAdapter.createFromResource(getActivity(),
                R.array.wms_versions, android.R.layout.simple_spinner_item);
        version = (Spinner)view.findViewById(R.id.VersionText);
        version.setAdapter(versionAdapter);
        ArrayAdapter<CharSequence> tileAdapter = ArrayAdapter.createFromResource(getActivity(),
                R.array.image_formats, android.R.layout.simple_spinner_item);
        tileFormat = (Spinner) view.findViewById(R.id.TileFormatText);
        tileFormat.setAdapter(tileAdapter);
        ArrayAdapter<CharSequence> booleanAdapter = ArrayAdapter.createFromResource(getActivity(),
                R.array.boolean_values, android.R.layout.simple_spinner_item);
        transparent = (Spinner) view.findViewById(R.id.TransparentText);
        transparent.setAdapter(booleanAdapter);
        layer1 = (EditText) view.findViewById(R.id.Layer1Text);
        layer2 = (EditText) view.findViewById(R.id.Layer2Text);
        layer3 = (EditText) view.findViewById(R.id.Layer3Text);
        resolution = (EditText) view.findViewById(R.id.ResolutionText);

        Button doneButton = (Button) view.findViewById(R.id.done);
        doneButton.setOnClickListener(v -> WmsSettingsDialog.this.dismiss());

        Button applyButton = (Button) view.findViewById(R.id.apply);
        applyButton.setOnClickListener(v -> {
            if (null != WmsSettingsDialog.this.listener) {
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
                WMSVersionEnum wmsVersion = WMSVersionEnum.valueOf(version.getSelectedItem().toString());

                ((IWmsSettingsDialogListener) WmsSettingsDialog.this.listener)
                        .addWmsService(map,
                                name.getText().toString(),
                                url.getText().toString(),
                                wmsVersion,
                                tileFormat.getSelectedItem().toString(),
                                transparent.getSelectedItem().toString().equalsIgnoreCase("true"),
                                layers,
                                Double.valueOf(resolution.getText().toString()));
            }
        });
    }

    public interface IWmsSettingsDialogListener extends IEmp3TesterDialogBaseListener {
        void addWmsService(IMap map, String name, String url, WMSVersionEnum wmsVersion, String tileFormat,
                           boolean transparent, List<String> layers, double resolution);
    }
}
