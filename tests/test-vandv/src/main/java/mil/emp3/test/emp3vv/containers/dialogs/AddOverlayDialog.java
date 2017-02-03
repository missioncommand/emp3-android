package mil.emp3.test.emp3vv.containers.dialogs;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;

import java.util.List;

import mil.emp3.api.interfaces.IMap;
import mil.emp3.test.emp3vv.R;
import mil.emp3.test.emp3vv.common.Emp3TesterDialogBase;
import mil.emp3.test.emp3vv.dialogs.utils.ErrorDialog;

public class AddOverlayDialog extends Emp3TesterDialogBase {
    private static String TAG = AddOverlayDialog.class.getSimpleName();
    private EditText overlayName;
    private CheckBox overlayVisible;
    private ListView overlayParentList;

    private List<String> parentList;
    ArrayAdapter<String> overlayParentListAdapter;
    private List<String> namesInUse;

    public interface IAddOverlayDialogListener extends IEmp3TesterDialogBaseListener {
        // boolean overlaySet(String overlayName, List<String> parentNameList, boolean visible);
        boolean overlaySet(AddOverlayDialog dialog);
    }

    public AddOverlayDialog() {
        // Empty constructor is required for DialogFragment
        // Make sure not to add arguments to the constructor
        // Use `newInstance` instead as shown below
    }

    public static AddOverlayDialog newInstance(String title, IMap map, List<String> parentList, IAddOverlayDialogListener listener, List<String> namesInUse) {

        if((null == parentList) || (0 == parentList.size())) {
            throw new IllegalArgumentException(TAG + "parentList must be non-null and non empty");
        }

        AddOverlayDialog frag = new AddOverlayDialog();
        Bundle args = new Bundle();
        args.putString("title", title);
        frag.setArguments(args);
        frag.init(map, listener);
        frag.parentList = parentList;
        frag.namesInUse = namesInUse;

        return frag;
    }

    public String getOverlayName() {
        return overlayName.getText().toString().trim();
    }

    public boolean getOverlayVisible() {
        return overlayVisible.isChecked();
    }

    public List<String> getSelectedParentList() {
        return(getSelectedFromMultiChoiceList(overlayParentList));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.add_overlay_dialog, container);
        getDialog().getWindow().setGravity(Gravity.LEFT | Gravity.BOTTOM);
        return v;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        overlayName = (EditText) view.findViewById(R.id.overlay_name);
        overlayVisible = (CheckBox) view.findViewById(R.id.overlay_visible);
        overlayVisible.setChecked(true);
        overlayParentList = (ListView) view.findViewById(R.id.overlay_parent_list);

//        TextView headerView = new TextView(getActivity());
//        headerView.setText("Choose Parent Map/Overlay");
//        overlayParentList.addHeaderView(headerView);
//        itemsAdapter = new ArrayAdapter(getActivity(), android.R.layout.simple_list_item_multiple_choice, parentList);
//        overlayParentList.setAdapter(itemsAdapter);
//        overlayParentList.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);

        overlayParentListAdapter = setupMultiChoiceList("Choose Parent Map/Overlay", overlayParentList, parentList);
        Button doneButton = (Button) view.findViewById(R.id.done);
        doneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AddOverlayDialog.this.dismiss();
            }
        });

        Button applyButton = (Button) view.findViewById(R.id.apply);
        applyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != AddOverlayDialog.this.listener) {

                    if((overlayName.getText().toString() == null) || (overlayName.getText().toString().trim().length() == 0)) {
                        ErrorDialog.showError(getContext(),"Please enter a non-null overlay name");
                        return;
                    }

                    if((parentList.contains(overlayName.getText().toString())) || (namesInUse.contains(overlayName.getText().toString()))) {
                        ErrorDialog.showError(getContext(), "Overlay name " + overlayName.getText().toString() + " is already in use");
                        return;
                    }

                    if(overlayParentList.getCheckedItemCount() == 0) {
                        ErrorDialog.showError(getContext(),"You must select at least one parent");
                        return;
                    } else {
                        Log.d(TAG, "getCheckedCount " + overlayParentList.getCheckedItemCount());
                    }

                    if(((IAddOverlayDialogListener) AddOverlayDialog.this.listener).overlaySet(AddOverlayDialog.this)) {
                        parentList.add(overlayName.getText().toString());
                        overlayParentListAdapter.notifyDataSetChanged();
                    }
                }
            }
        });
    }
}
