package mil.emp3.validator.fragment;

import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import mil.emp3.validator.R;
import mil.emp3.validator.ValidatorStateManager;
import mil.emp3.validator.model.ManagedOverlay;

public class OverlayValidatorFragment extends Fragment {
    static final private String TAG = OverlayValidatorFragment.class.getSimpleName();

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView");

        final View view = inflater.inflate(R.layout.fragment_validator_create_overlay, container, false);

        final Button createButton = (Button) view.findViewById(R.id.button_create);
        createButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick");

                final String name  = ((EditText) getView().findViewById(R.id.textView_name)).getText().toString();
                Log.d(TAG, "name: " + name);

                final ManagedOverlay overlay = ValidatorStateManager.getInstance().createOverlay(name);
                ValidatorStateManager.getInstance().setCurrentOverlay(name);

                //TODO 'add overlay to map' button with listView of existing maps.
                try {
                    ValidatorStateManager.getInstance().getMap().get().addOverlay(overlay.get(), true);
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
