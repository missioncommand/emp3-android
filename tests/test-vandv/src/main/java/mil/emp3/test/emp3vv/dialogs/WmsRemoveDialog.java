package mil.emp3.test.emp3vv.dialogs;

import android.support.v4.app.DialogFragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

import mil.emp3.api.interfaces.IMap;
import mil.emp3.test.emp3vv.R;
import mil.emp3.test.emp3vv.common.Emp3TesterDialogBase;

/**
 * Dialog for removing WMS service(s).
 */

public class WmsRemoveDialog extends Emp3TesterDialogBase {

    private List<String> wmsNames;

    public WmsRemoveDialog() {

    }

    public static WmsRemoveDialog newInstance(String title, WmsRemoveDialog.IWmsRemoveDialogListener listener,
                                                IMap map, List<String> wmsNames) {
        WmsRemoveDialog wmsRemoveDialog = new WmsRemoveDialog();
        Bundle args = new Bundle();
        args.putString("title", title);
        wmsRemoveDialog.setArguments(args);
        wmsRemoveDialog.init(map, listener);
        wmsRemoveDialog.setWmsNames(wmsNames);
        return wmsRemoveDialog;
    }

    public static WmsRemoveDialog newInstanceForOptItem(String title, WmsRemoveDialog.IWmsRemoveDialogListener listener,
                                              IMap map, List<String> wmsNames) {
        WmsRemoveDialog wmsRemoveDialog = new WmsRemoveDialog();
        Bundle args = new Bundle();
        args.putString("title", title);
        wmsRemoveDialog.setArguments(args);
        wmsRemoveDialog.initForOptItem(map, listener);
        wmsRemoveDialog.setWmsNames(wmsNames);
        return wmsRemoveDialog;
    }

    public void setWmsNames(List<String> wmsNames) {
        this.wmsNames = wmsNames;
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
                android.R.layout.simple_list_item_multiple_choice, wmsNames);
        listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, final View view,
                                    int position, long id) {
                final String item = (String) parent.getItemAtPosition(position);
            }

        });
        Button doneButton = (Button) view.findViewById(R.id.done);
        doneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                WmsRemoveDialog.this.dismiss();
            }
        });

        Button applyButton = (Button) view.findViewById(R.id.apply);
        applyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != WmsRemoveDialog.this.listener) {
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
                        ((IWmsRemoveDialogListener) WmsRemoveDialog.this.listener).
                                removeWmsService(map,
                                        item);
                    }
                }
            }
        });
    }

    public interface IWmsRemoveDialogListener extends IEmp3TesterDialogBaseListener {
        void removeWmsService(IMap map, String name);
    }

}
