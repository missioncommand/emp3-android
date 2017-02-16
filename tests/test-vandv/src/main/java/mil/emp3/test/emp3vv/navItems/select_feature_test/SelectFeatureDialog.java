package mil.emp3.test.emp3vv.navItems.select_feature_test;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

import mil.emp3.api.events.FeatureEvent;
import mil.emp3.api.exceptions.EMP_Exception;
import mil.emp3.api.interfaces.IFeature;
import mil.emp3.api.interfaces.IMap;
import mil.emp3.api.listeners.EventListenerHandle;
import mil.emp3.api.listeners.IFeatureEventListener;
import mil.emp3.test.emp3vv.R;
import mil.emp3.test.emp3vv.common.Emp3TesterDialogBase;
import mil.emp3.test.emp3vv.dialogs.utils.ErrorDialog;
import mil.emp3.test.emp3vv.utils.MapNamesUtility;

public class SelectFeatureDialog extends Emp3TesterDialogBase {

    ListView allFeaturesList;
    ArrayAdapter<String> allFeaturesListAdapter;
    List<String> allFeaturesListData;

    ListView selectedFeaturesList;
    ArrayAdapter<String> selectedFeaturesListAdapter;
    List<String> selectedFeaturesListData;

    ListView listenerInvokedList;
    ArrayAdapter<String> listenerInvokedListAdapter;
    List<String> listenerInvokedListData;

    EventListenerHandle featureEventListenerHandle;

    public interface ISelectFeatureDialogListener extends IEmp3TesterDialogBaseListener {
        void isSelectedFeature(SelectFeatureDialog dialog);
        void selectFeature(SelectFeatureDialog dialog);
        void deselectFeature(SelectFeatureDialog dialog);
        void clearSelected(SelectFeatureDialog dialog);
    }

    public SelectFeatureDialog() {
    }

    public static SelectFeatureDialog newInstance(String title, IMap map, ISelectFeatureDialogListener listener) {
        SelectFeatureDialog frag = new SelectFeatureDialog();
        Bundle args = new Bundle();
        args.putString("title", title);
        frag.setArguments(args);
        frag.init(map, listener);
        return frag;
    }

    public List<String> getUserSelectedFeatures() {
        return getSelectedFromMultiChoiceList(allFeaturesList);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.select_feature_dialog, container);
        getDialog().getWindow().setGravity(Gravity.LEFT | Gravity.BOTTOM);
        return v;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        allFeaturesList = (ListView) view.findViewById(R.id.all_features_list);
        allFeaturesListData = MapNamesUtility.getNames(map, false, false, true);
        allFeaturesListAdapter = setupMultiChoiceList("All Features", allFeaturesList, allFeaturesListData);

        Button selectFeature = (Button) view.findViewById(R.id.select_feature);
        selectFeature.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(allFeaturesList.getCheckedItemCount() < 1) {
                    ErrorDialog.showError(getContext(), "Please select at least one feature");
                } else {
                    ((ISelectFeatureDialogListener)SelectFeatureDialog.this.listener).selectFeature(SelectFeatureDialog.this);
                    updateSelectedList();
                }
            }
        });

        Button deselectFeature = (Button) view.findViewById(R.id.deselect_feature);
        deselectFeature.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(allFeaturesList.getCheckedItemCount() < 1) {
                    ErrorDialog.showError(getContext(), "Please select at least one feature");
                } else {
                    ((ISelectFeatureDialogListener)SelectFeatureDialog.this.listener).deselectFeature(SelectFeatureDialog.this);
                    updateSelectedList();
                }
            }
        });

        Button isSelectedFeature = (Button) view.findViewById(R.id.is_selected_feature);
        isSelectedFeature.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(1 != allFeaturesList.getCheckedItemCount()) {
                    ErrorDialog.showError(getContext(), "Please select one and only one feature");
                } else {
                    ((ISelectFeatureDialogListener)SelectFeatureDialog.this.listener).isSelectedFeature(SelectFeatureDialog.this);
                }
            }
        });

        selectedFeaturesList = (ListView) view.findViewById(R.id.selected_features_list);
        selectedFeaturesList.setEnabled(false);
        List<IFeature> selectedFeatures = map.getSelected();
        selectedFeaturesListData = new ArrayList<>();
        for(IFeature feature: selectedFeatures) {
            selectedFeaturesListData.add(feature.getName());
        }
        selectedFeaturesListAdapter = setupMultiChoiceList("Selected", selectedFeaturesList, selectedFeaturesListData);

        listenerInvokedList = (ListView) view.findViewById(R.id.listener_invoked_list);
        listenerInvokedList.setEnabled(false);
        listenerInvokedListData = new ArrayList<>();
        listenerInvokedListAdapter = setupMultiChoiceList("Listener", listenerInvokedList, listenerInvokedListData);

        Button cleanListenerInvokedList = (Button) view.findViewById(R.id.clear_listener_invoked_list);
        cleanListenerInvokedList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listenerInvokedListData.clear();
                listenerInvokedListAdapter.notifyDataSetChanged();
            }
        });

        Button clearSelected = (Button) view.findViewById(R.id.clear_selected);
        clearSelected.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((ISelectFeatureDialogListener)SelectFeatureDialog.this.listener).clearSelected(SelectFeatureDialog.this);
                updateSelectedList();
            }
        });

        Button doneButton = (Button) view.findViewById(R.id.done);
        doneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                map.removeEventListener(featureEventListenerHandle);
                SelectFeatureDialog.this.dismiss();
            }
        });

        try {
            featureEventListenerHandle = map.addFeatureEventListener(new FeatureEventListener());
        } catch (EMP_Exception e) {
            e.printStackTrace();
        }
    }

    private void updateSelectedList() {
        List<IFeature> selectedFeatures = map.getSelected();
        selectedFeaturesListData.clear();
        for(IFeature feature: selectedFeatures) {
            selectedFeaturesListData.add(feature.getName());
        }
        selectedFeaturesListAdapter.notifyDataSetChanged();
    }

    private void updateListenerInvokedList(String featureName) {
        if(!listenerInvokedListData.contains(featureName)) {
            listenerInvokedListData.add(featureName);
            listenerInvokedListAdapter.notifyDataSetChanged();
        }
    }

    class FeatureEventListener implements IFeatureEventListener {

        FeatureEventListener() { }
        @Override
        public void onEvent(FeatureEvent event) {
            if(event.getTarget() != null) {
                updateListenerInvokedList(event.getTarget().getName());
            }
        }
    }
}
