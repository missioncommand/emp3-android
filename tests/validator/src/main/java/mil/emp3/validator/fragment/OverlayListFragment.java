package mil.emp3.validator.fragment;

import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import java.util.UUID;

import mil.emp3.api.Overlay;
import mil.emp3.api.exceptions.EMP_Exception;
import mil.emp3.api.interfaces.IFeature;
import mil.emp3.api.interfaces.IMap;
import mil.emp3.validator.R;
import mil.emp3.validator.ValidatorStateManager;
import mil.emp3.validator.model.ManagedMapFragment;
import mil.emp3.validator.model.ManagedOverlay;

/**
 * Created by raju on 9/30/2016.
 */

public class OverlayListFragment extends Fragment {
    static final private String TAG = FeatureListFragment.class.getSimpleName();
    static final ValidatorStateManager stateManager = ValidatorStateManager.getInstance();

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView");

        final View view = inflater.inflate(R.layout.fragment_list_view, container, false);
        final ListView listview = (ListView) view.findViewById(R.id.listview);
        final String operation = getArguments().getString("OverlayListOperation");

        ArrayList<String> overlayNames = stateManager.getOverlayNames();

        final ArrayAdapter adapter = new ArrayAdapter(this.getActivity(),
                android.R.layout.simple_list_item_multiple_choice, overlayNames);
        listview.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        listview.setAdapter(adapter);
        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, final View view,
                                    int position, long id) {
                final String item = (String) parent.getItemAtPosition(position);
            }

        });
        final Button okButton = (Button) view.findViewById(R.id.button_ok);
        okButton.setText(operation);
        final Button cancelButton = (Button) view.findViewById(R.id.button_cancel);
        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick");
                SparseBooleanArray checked = listview.getCheckedItemPositions();
                ArrayList<String> selectedItems = new ArrayList<String>();
                for (int i = 0; i < checked.size(); i++) {
                    // Item position in adapter
                    int position = checked.keyAt(i);
                    // Add sport if it is checked i.e.) == TRUE!
                    if (checked.valueAt(i)) {
                        String item = (String) adapter.getItem(position);
                        selectedItems.add(item);
                    }
                }

                IMap map = stateManager.getMap().get();
                for (String item : selectedItems) {
                    switch (operation) {
                        case "Apply" :
                            ManagedOverlay managedOverlay = stateManager.getOverlay(item);
                            managedOverlay.get().apply();
                            break;
                        case "Add" :
                            try {
                                map.addOverlay(stateManager.getOverlay(item).get(), false);
                            } catch (EMP_Exception empe) {
                                empe.printStackTrace();
                            }
                            break;
                        case "Remove" :
                            try {
                                map.removeOverlay(stateManager.getOverlay(item).get());
                            } catch (EMP_Exception empe) {
                                empe.printStackTrace();
                            }
                            break;
                        default:
                    }
                }

                getFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                getFragmentManager().beginTransaction().replace(R.id.sideNav_fragment, new SideNavFragment()).commit();
            }
        });

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick");
                getFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                getFragmentManager().beginTransaction().replace(R.id.sideNav_fragment, new SideNavFragment()).commit();
            }
        });

        return view;
    }
}
