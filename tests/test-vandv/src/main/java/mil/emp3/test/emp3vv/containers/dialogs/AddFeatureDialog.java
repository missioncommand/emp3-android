package mil.emp3.test.emp3vv.containers.dialogs;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import org.cmapi.primitives.IGeoMilSymbol;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import mil.emp3.api.enums.FeatureTypeEnum;
import mil.emp3.api.interfaces.IMap;
import mil.emp3.test.emp3vv.R;
import mil.emp3.test.emp3vv.common.Emp3TesterDialogBase;
import mil.emp3.test.emp3vv.containers.AddEntityBase;
import mil.emp3.test.emp3vv.dialogs.utils.ErrorDialog;

public class AddFeatureDialog extends Emp3TesterDialogBase {
    private static String TAG = AddFeatureDialog.class.getSimpleName();

    private EditText featureName;
    private CheckBox featureVisible;
    private ListView featureParentList;

    private List<String> parentList;
    private List<String> typeList;

    private ArrayAdapter<String> featureParentListAdapter;
    private Spinner featureType;
    private ArrayAdapter<CharSequence> featureTypeAdapter;
    private List<String> namesInUse;
    Map<FeatureTypeEnum, Class<? extends AddEntityBase>> supportedFeatures;

    public interface IAddFeatureDialogListener extends IEmp3TesterDialogBaseListener {
        boolean featureSet(AddFeatureDialog dialog);
    }

    public AddFeatureDialog() {
        // Empty constructor is required for DialogFragment
        // Make sure not to add arguments to the constructor
        // Use `newInstance` instead as shown below
    }

    public static AddFeatureDialog newInstance(String title, IMap map, List<String> parentList, IAddFeatureDialogListener listener, List<String> namesInUse,
                                               Map<FeatureTypeEnum, Class<? extends AddEntityBase>> supportedFeatures) {

        if (null == listener) {
            throw new IllegalArgumentException("listener and must be non-null");
        }

        if((null == parentList) || (0 == parentList.size())) {
            throw new IllegalArgumentException("parentList must be non-null and non empty");
        }

        AddFeatureDialog frag = new AddFeatureDialog();
        Bundle args = new Bundle();
        args.putString("title", title);
        frag.setArguments(args);
        frag.init(map, listener);
        frag.parentList = parentList;
        frag.namesInUse = namesInUse;
        frag.supportedFeatures = supportedFeatures;
        return frag;
    }

    public boolean getFeatureVisible() {
        return featureVisible.isChecked();
    }
    public String getFeatureName() {
        return featureName.getText().toString();
    }

    public boolean isTacticalGraphic() {
        CheckBox cb = (CheckBox) getView().findViewById(R.id.feature_is_tactical_graphics);
        return cb.isChecked();
    }
    public List<String> getSelectedParentList() {
        return getSelectedFromMultiChoiceList(featureParentList);
    }

    public FeatureTypeEnum getSelectedFeatureType() {
        String featureTypeSelected = (featureType.getSelectedItem().toString());
        return FeatureTypeEnum.valueOf(featureTypeSelected);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.add_feature_dialog, container);
        getDialog().getWindow().setGravity(Gravity.LEFT | Gravity.BOTTOM);
        return v;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        featureName = (EditText) view.findViewById(R.id.feature_name);
        featureVisible = (CheckBox) view.findViewById(R.id.feature_visible);
        featureVisible.setChecked(true);
        featureParentList = (ListView) view.findViewById(R.id.feature_parent_list);
        featureType = (Spinner) view.findViewById(R.id.feature_type);

        featureParentListAdapter = setupMultiChoiceList("Choose Parent",  featureParentList, parentList);

        typeList = new ArrayList<>();
        for(FeatureTypeEnum typeEnum: FeatureTypeEnum.values()) {
            typeList.add(typeEnum.toString());
        }

        featureTypeAdapter = ArrayAdapter.createFromResource(this.getContext(), R.array.feature_types,
                android.R.layout.simple_spinner_item);
        featureType.setAdapter(featureTypeAdapter);


        Button doneButton = (Button) view.findViewById(R.id.done);
        doneButton.setOnClickListener(v -> AddFeatureDialog.this.dismiss());

        Button applyButton = (Button) view.findViewById(R.id.apply);
        applyButton.setOnClickListener(v -> {
            if (null != AddFeatureDialog.this.listener) {
                if((featureName.getText().toString() == null) || (featureName.getText().toString().trim().length() == 0)) {
                    ErrorDialog.showError(getContext(), "Feature Name must be specified");
                    return;
                }

                if((parentList.contains(featureName.getText().toString())) || (namesInUse.contains(featureName.getText().toString()))) {
                    ErrorDialog.showError(getContext(), "Feature name " + featureName.getText().toString() + " is already in use");
                    return;
                }

                if(featureParentList.getCheckedItemCount() == 0) {
                    ErrorDialog.showError(getContext(),"You must select at least one parent");
                    return;
                }

                if (getSelectedFeatureType() == null) {
                    ErrorDialog.showError(getContext(),"You must select a feature type");
                    return;
                }

                if(!supportedFeatures.containsKey(getSelectedFeatureType())) {
                    ErrorDialog.showError(getContext(),getSelectedFeatureType() + " features are currently not supported.");
                    return;
                }

                AddFeatureDialog.this.dismiss();
                if(((IAddFeatureDialogListener)AddFeatureDialog.this.listener).featureSet(AddFeatureDialog.this)) {
                    parentList.add(featureName.getText().toString());
                    featureParentListAdapter.notifyDataSetChanged();
                }

            }
        });
    }
}
