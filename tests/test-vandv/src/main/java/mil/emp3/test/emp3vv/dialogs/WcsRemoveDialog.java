package mil.emp3.test.emp3vv.dialogs;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

import mil.emp3.api.interfaces.IMap;
import mil.emp3.test.emp3vv.R;
import mil.emp3.test.emp3vv.common.Emp3TesterDialogBase;

/**
 * Dialog for removing WCS service(s).
 */

public class WcsRemoveDialog extends Emp3TesterDialogBase {

    private List<String> wcsNames;

    public WcsRemoveDialog() {

    }

    public static WcsRemoveDialog newInstance(String title, WcsRemoveDialog.IWcsRemoveDialogListener listener,
                                              IMap map, List<String> wmsNames) {
        WcsRemoveDialog wmsRemoveDialog = new WcsRemoveDialog();
        Bundle args = new Bundle();
        args.putString("title", title);
        wmsRemoveDialog.setArguments(args);
        wmsRemoveDialog.init(map, listener);
        wmsRemoveDialog.setWcsNames(wmsNames);
        return wmsRemoveDialog;
    }

    public static WcsRemoveDialog newInstanceForOptItem(String title, WcsRemoveDialog.IWcsRemoveDialogListener listener,
                                                        IMap map, List<String> wmsNames) {
        WcsRemoveDialog wmsRemoveDialog = new WcsRemoveDialog();
        Bundle args = new Bundle();
        args.putString("title", title);
        wmsRemoveDialog.setArguments(args);
        wmsRemoveDialog.initForOptItem(map, listener);
        wmsRemoveDialog.setWcsNames(wmsNames);
        return wmsRemoveDialog;
    }

    public void setWcsNames(List<String> wmsNames) {
        this.wcsNames = wmsNames;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.wms_remove_dialog, container);
        setDialogPosition();
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        final ListView listView = (ListView) view.findViewById(R.id.listview);
        final ArrayAdapter adapter = new ArrayAdapter(getActivity(),
                android.R.layout.simple_list_item_multiple_choice, wcsNames);
        listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener((parent, view1, position, id) -> {
            final String item = (String) parent.getItemAtPosition(position);
        });
        Button doneButton = (Button) view.findViewById(R.id.done);
        doneButton.setOnClickListener(v -> WcsRemoveDialog.this.dismiss());

        Button applyButton = (Button) view.findViewById(R.id.apply);
        applyButton.setOnClickListener(v -> {
            if (null != WcsRemoveDialog.this.listener) {
                SparseBooleanArray checked = listView.getCheckedItemPositions();
                ArrayList<String> selectedItems = new ArrayList<String>();
                for (int i = 0; i < checked.size(); i++) {
                    int position = checked.keyAt(i);
                    if (checked.valueAt(i)) {
                        String item = (String) adapter.getItem(position);
                        selectedItems.add(item);
                    }
                }
                for (String item : selectedItems) {
                    ((IWcsRemoveDialogListener) WcsRemoveDialog.this.listener).
                            removeWcsService(map,
                                    item);
                }
            }
        });
    }

    public interface IWcsRemoveDialogListener extends IEmp3TesterDialogBaseListener {
        void removeWcsService(IMap map, String name);
    }
}

