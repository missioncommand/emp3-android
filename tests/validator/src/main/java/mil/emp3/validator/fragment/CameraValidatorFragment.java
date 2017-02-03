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

import mil.emp3.validator.R;
import mil.emp3.validator.ValidatorStateManager;
import mil.emp3.validator.model.ManagedCamera;

public class CameraValidatorFragment extends Fragment {
    static final private String TAG = CameraValidatorFragment.class.getSimpleName();

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView");

        final View view = inflater.inflate(R.layout.fragment_validator_create_camera, container, false);

        final ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity(), R.array.altitude_mode_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        final Spinner spinner = (Spinner) view.findViewById(R.id.spinner_altitude_mode);
        spinner.setAdapter(adapter);


        final Button createButton = (Button) view.findViewById(R.id.button_create);
        createButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick");

                final String name         = ((EditText) getView().findViewById(R.id.textView_name)).getText().toString();
                final String latitude     = ((EditText) getView().findViewById(R.id.textView_latitude)).getText().toString();
                final String longitude    = ((EditText) getView().findViewById(R.id.textView_longitude)).getText().toString();
                final String altitude     = ((EditText) getView().findViewById(R.id.textView_altitude)).getText().toString();
                final String roll         = ((EditText) getView().findViewById(R.id.textView_roll)).getText().toString();
                final String tilt         = ((EditText) getView().findViewById(R.id.textView_tilt)).getText().toString();
                final String heading      = ((EditText) getView().findViewById(R.id.textView_heading)).getText().toString();
                final String altitudeMode = ((Spinner) getView().findViewById(R.id.spinner_altitude_mode)).getSelectedItem().toString();

                Log.d(TAG, "name: "         + name);
                Log.d(TAG, "latitude: "     + latitude);
                Log.d(TAG, "longitude: "    + longitude);
                Log.d(TAG, "altitude: "     + altitude);
                Log.d(TAG, "roll: "         + roll);
                Log.d(TAG, "tilt: "         + tilt);
                Log.d(TAG, "heading: "      + heading);
                Log.d(TAG, "altitudeMode: " + altitudeMode);

                final ManagedCamera managedCamera = ValidatorStateManager.getInstance().createCamera(
                        name,
                        Double.parseDouble(latitude), Double.parseDouble(longitude), Double.parseDouble(altitude),
                        Double.parseDouble(roll), Double.parseDouble(tilt), Double.parseDouble(heading),
                        altitudeMode
                );

                try { // apply camera to map
                    ValidatorStateManager.getInstance().getMap().get().setCamera(managedCamera.get(), false);
                } catch (Exception e) {
                    throw new IllegalStateException(e);
                }

                //TODO layout the lat/lon/alt and roll/tilt/head better

                getFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                getFragmentManager().beginTransaction().replace(R.id.sideNav_fragment, new SideNavFragment()).commit();
            }
        });

        return view;
    }
}
