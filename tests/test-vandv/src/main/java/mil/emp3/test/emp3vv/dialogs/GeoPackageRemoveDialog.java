package mil.emp3.test.emp3vv.dialogs;

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

public class GeoPackageRemoveDialog extends Emp3TesterDialogBase {

    private List<String> geoPackageNames;

    public GeoPackageRemoveDialog() {

    }

    public static GeoPackageRemoveDialog newInstance(String title, GeoPackageRemoveDialog.IGeoPackageRemoveDialogListener listener,
                                              IMap map, List<String> geoPackageNames) {
        GeoPackageRemoveDialog geoPackageRemoveDialog = new GeoPackageRemoveDialog();
        Bundle args = new Bundle();
        args.putString("title", title);
        geoPackageRemoveDialog.setArguments(args);
        geoPackageRemoveDialog.setGeoPackageNames(geoPackageNames);
        geoPackageRemoveDialog.init(map, listener);
        return geoPackageRemoveDialog;
    }

    public static GeoPackageRemoveDialog newInstanceForOptItem(String title, GeoPackageRemoveDialog.IGeoPackageRemoveDialogListener listener,
                                                     IMap map, List<String> geoPackageNames) {
        GeoPackageRemoveDialog geoPackageRemoveDialog = new GeoPackageRemoveDialog();
        Bundle args = new Bundle();
        args.putString("title", title);
        geoPackageRemoveDialog.setArguments(args);
        geoPackageRemoveDialog.setGeoPackageNames(geoPackageNames);
        geoPackageRemoveDialog.initForOptItem(map, listener);
        return geoPackageRemoveDialog;
    }

    public void setGeoPackageNames(List<String> geoPackageNames) {
        this.geoPackageNames = geoPackageNames;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.geopackage_remove_dialog, container);
        setDialogPosition();
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        final ListView listView = (ListView) view.findViewById(R.id.listview);
        final ArrayAdapter adapter = new ArrayAdapter(getActivity(),
                android.R.layout.simple_list_item_multiple_choice, geoPackageNames);
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
                GeoPackageRemoveDialog.this.dismiss();
            }
        });

        Button applyButton = (Button) view.findViewById(R.id.apply);
        applyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != GeoPackageRemoveDialog.this.listener) {
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
                        ((IGeoPackageRemoveDialogListener) GeoPackageRemoveDialog.this.listener).
                                removeGeoPackageService(map,
                                        item);
                    }
                }
            }
        });
    }

    public interface IGeoPackageRemoveDialogListener extends IEmp3TesterDialogBaseListener {
        void removeGeoPackageService(IMap map, String name);
    }

}

