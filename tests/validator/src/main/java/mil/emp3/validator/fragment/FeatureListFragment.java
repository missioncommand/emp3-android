package mil.emp3.validator.fragment;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import mil.emp3.api.Camera;
import mil.emp3.api.Overlay;
import mil.emp3.api.events.MapStateChangeEvent;
import mil.emp3.api.exceptions.EMP_Exception;
import mil.emp3.api.interfaces.ICamera;
import mil.emp3.api.interfaces.IFeature;
import mil.emp3.api.interfaces.IMap;
import mil.emp3.api.interfaces.IOverlay;
import mil.emp3.api.listeners.EventListenerHandle;
import mil.emp3.api.listeners.IMapStateChangeEventListener;
import mil.emp3.validator.R;
import mil.emp3.validator.ValidatorStateManager;
import mil.emp3.validator.model.ManagedMapFragment;

/**
 * Created by raju on 9/22/2016.
 */

public class FeatureListFragment extends Fragment {
    static final private String TAG = FeatureListFragment.class.getSimpleName();

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        Log.d(TAG, "onCreateView");

        final View view = inflater.inflate(R.layout.fragment_list_view, container, false);
        final ListView listview = (ListView) view.findViewById(R.id.listview);
        final String operation = getArguments().getString("FeatureListOperation");

        final ManagedMapFragment mapFragment = ValidatorStateManager.getInstance().getMap();
        String currentOverlayName = ValidatorStateManager.getInstance().getCurrentOverlay();
        // For add, get features not currently added to overlay
        // For remove, get features on the overlay
        final IOverlay overlay = ValidatorStateManager.getInstance().getOverlay(currentOverlayName).get();
        final HashMap<String, UUID> nameHash = new HashMap<>();
        final ArrayList<String> list = new ArrayList<>();
        final HashMap<UUID, IFeature> featureHash = mapFragment.getFeatureHash();
        switch (operation) {
            case "Add":
            for (UUID uuid : featureHash.keySet()) {
                IFeature feature = featureHash.get(uuid);
                list.add(feature.getName());
                nameHash.put(feature.getName(), uuid);
            }
            break;
            case "Remove":
                List<IFeature> features = overlay.getFeatures();
                for (IFeature feature : features) {
                    list.add(feature.getName());
                    nameHash.put(feature.getName(), feature.getGeoId());
                }
            break;
            default:
        }

        final ArrayAdapter adapter = new ArrayAdapter(this.getActivity(),
                android.R.layout.simple_list_item_multiple_choice, list);
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

                for (String item : selectedItems) {
                    UUID uuid = nameHash.get(item);
                    switch (operation) {
                        case "Add" :
                            try {
                                overlay.addFeature(featureHash.get(uuid), false);
                            } catch (EMP_Exception empe) {
                                empe.printStackTrace();
                            }
                        break;
                        case "Remove" :
                            try {
                                overlay.removeFeature(featureHash.get(uuid));
                            } catch  (EMP_Exception empe) {
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
