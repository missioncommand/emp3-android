package mil.emp3.validator.fragment;

import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import java.util.ArrayList;

import mil.emp3.validator.R;
import mil.emp3.validator.ValidatorStateManager;
import mil.emp3.validator.model.ManagedWms;

public class WmsValidatorFragment extends Fragment {
    static final private String TAG = WmsValidatorFragment.class.getSimpleName();

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView");

        final View view = inflater.inflate(R.layout.fragment_validator_create_wms, container, false);

        final ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity(), R.array.wms_version_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        final Spinner spinner = (Spinner) view.findViewById(R.id.spinner_wms_version);
        spinner.setAdapter(adapter);

        final Button createButton = (Button) view.findViewById(R.id.button_create);
        createButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick");

                final String name       = ((EditText) getView().findViewById(R.id.textView_name)).getText().toString();
                final String url        = ((EditText) getView().findViewById(R.id.textView_url)).getText().toString();
                final String layers     = ((EditText) getView().findViewById(R.id.textView_layers)).getText().toString();
                final String wmsVersion = ((Spinner) getView().findViewById(R.id.spinner_wms_version)).getSelectedItem().toString();
                final String tileFormat = ((EditText) getView().findViewById(R.id.textView_tile_format)).getText().toString();

                Log.d(TAG, "name: "       + name);
                Log.d(TAG, "url: "        + url);
                Log.d(TAG, "layers: "     + layers);
                Log.d(TAG, "wmsVersion: " + wmsVersion);
                Log.d(TAG, "tileFormat: " + tileFormat);

                final ManagedWms managedWms = ValidatorStateManager.getInstance().createWms(name, url, new ArrayList<String>(){{ add(layers); }}, wmsVersion, tileFormat);

                try {
                    ValidatorStateManager.getInstance().getMap().get().addMapService(managedWms.get());
                } catch (Exception e) {
                    throw new IllegalStateException(e);
                }

                getFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                getFragmentManager().beginTransaction().replace(R.id.sideNav_fragment, new SideNavFragment()).commit();
            }
        });

        return view;
    }
}
