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
 * Dialog for removing WMTS service(s).
 */

public class WmtsRemoveDialog extends Emp3TesterDialogBase {

    private List<String> wmtsNames;

    public WmtsRemoveDialog() {

    }

    public static WmtsRemoveDialog newInstance(String title, WmtsRemoveDialog.IWmtsRemoveDialogListener listener,
                                              IMap map, List<String> wmtsNames) {
        WmtsRemoveDialog wmtsRemoveDialog = new WmtsRemoveDialog();
        Bundle args = new Bundle();
        args.putString("title", title);
        wmtsRemoveDialog.setArguments(args);
        wmtsRemoveDialog.init(map, listener);
        wmtsRemoveDialog.setWmtsNames(wmtsNames);
        return wmtsRemoveDialog;
    }

    public static WmtsRemoveDialog newInstanceForOptItem(String title, WmtsRemoveDialog.IWmtsRemoveDialogListener listener,
                                                        IMap map, List<String> wmtsNames) {
        WmtsRemoveDialog wmtsRemoveDialog = new WmtsRemoveDialog();
        Bundle args = new Bundle();
        args.putString("title", title);
        wmtsRemoveDialog.setArguments(args);
        wmtsRemoveDialog.initForOptItem(map, listener);
        wmtsRemoveDialog.setWmtsNames(wmtsNames);
        return wmtsRemoveDialog;
    }

    public void setWmtsNames(List<String> wmtsNames) {
        this.wmtsNames = wmtsNames;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.wmts_remove_dialog, container);
        setDialogPosition();
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        final ListView listView = (ListView) view.findViewById(R.id.listview);
        final ArrayAdapter adapter = new ArrayAdapter(getActivity(),
                android.R.layout.simple_list_item_multiple_choice, wmtsNames);
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
                WmtsRemoveDialog.this.dismiss();
            }
        });

        Button applyButton = (Button) view.findViewById(R.id.apply);
        applyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != WmtsRemoveDialog.this.listener) {
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
                        ((IWmtsRemoveDialogListener) WmtsRemoveDialog.this.listener).
                                removeWmtsService(map,
                                        item);
                    }
                }
            }
        });
    }

    public interface IWmtsRemoveDialogListener extends IEmp3TesterDialogBaseListener {
        void removeWmtsService(IMap map, String name);
    }

}
