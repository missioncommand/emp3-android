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

import java.io.File;
import java.io.FilenameFilter;
import java.util.Arrays;
import java.util.List;

import mil.emp3.api.interfaces.IMap;
import mil.emp3.test.emp3vv.R;
import mil.emp3.test.emp3vv.common.Emp3TesterDialogBase;
import mil.emp3.test.emp3vv.dialogs.utils.ErrorDialog;

public class AddGeoJSONDialog extends Emp3TesterDialogBase {
    private static String TAG = AddGeoJSONDialog.class.getSimpleName();

    private ListView geoJSONParentList;
    private ListView fileList;
    private CheckBox geoJSONVisible;

    private List<String> parentList;

    ArrayAdapter<String> geoJSONParentListAdapter;
    ArrayAdapter<String> geoJSONAdapter;
    private List<String> namesInUse;
    private File downloadFolder = new File("/sdcard/Download/");
    private static final String SUFFIX = ".geojson";


    public interface IAddGeoJSONDialogListener extends IEmp3TesterDialogBaseListener {
        boolean featureSet(AddGeoJSONDialog dialog);
    }

    public AddGeoJSONDialog() {
        // Empty constructor is required for DialogFragment
        // Make sure not to add arguments to the constructor
        // Use `newInstance` instead as shown below
    }

    public static AddGeoJSONDialog newInstance(String title, IMap map, List<String> parentList, IAddGeoJSONDialogListener listener, List<String> namesInUse) {

        if (null == listener) {
            throw new IllegalArgumentException("listener and must be non-null");
        }

        if((null == parentList) || (0 == parentList.size())) {
            throw new IllegalArgumentException("parentList must be non-null and non empty");
        }

        AddGeoJSONDialog frag = new AddGeoJSONDialog();
        Bundle args = new Bundle();
        args.putString("title", title);
        frag.setArguments(args);
        frag.init(map, listener);
        frag.parentList = parentList;
        frag.namesInUse = namesInUse;
        return frag;
    }

    public boolean getGeoJSONVisible() { return geoJSONVisible.isChecked(); }

    public List<String> getSelectedParentList() {
        return getSelectedFromMultiChoiceList(geoJSONParentList);
    }

    public String getSelectedGeoJSON() {
        return getSelectedFromSingleChoiceList(fileList);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.add_geojson_dialog, container);
        getDialog().getWindow().setGravity(Gravity.LEFT | Gravity.BOTTOM);
        return v;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        geoJSONVisible = (CheckBox)view.findViewById(R.id.geojson_visible);
        geoJSONVisible.setChecked(true);
        geoJSONParentList = (ListView) view.findViewById(R.id.geojson_parent_list);
        fileList = (ListView) view.findViewById(R.id.geojson_list);
        geoJSONParentListAdapter = setupMultiChoiceList("Choose Parent", geoJSONParentList, parentList);
        geoJSONAdapter = setupSingleChoiceList("Choose File", fileList, getGeoJSONList());

        Button doneButton = (Button) view.findViewById(R.id.done);
        doneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AddGeoJSONDialog.this.dismiss();
            }
        });

        Button applyButton = (Button) view.findViewById(R.id.apply);
        applyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != AddGeoJSONDialog.this.listener) {
                    if(geoJSONParentList.getCheckedItemCount() == 0) {
                        ErrorDialog.showError(getContext(),"Please pick a parent");
                        return;
                    }

                    if (fileList.getCheckedItemCount() == 0) {
                        ErrorDialog.showError(getContext(),"Please pick a geoJSON file ");
                        return;
                    }

                    String geoJSONName = getSelectedGeoJSON().split("\\.")[0];
                    AddGeoJSONDialog.this.dismiss();
                    if(((IAddGeoJSONDialogListener)AddGeoJSONDialog.this.listener).featureSet(AddGeoJSONDialog.this)) {
                        parentList.add(geoJSONName);
                        geoJSONParentListAdapter.notifyDataSetChanged();
                    }
                }
            }
        });
    }

    private List<String> getGeoJSONList() {
        String[] geoJSONList = null;
        try {
            downloadFolder.mkdirs();
        } catch (SecurityException e) {
            e.printStackTrace();
        }
        if (downloadFolder.exists()) {
            FilenameFilter filter = new FilenameFilter() {

                @Override
                public boolean accept(File dir, String filename) {
                    File sel = new File(dir, filename);
                    return filename.contains(SUFFIX) || sel.isDirectory();
                }

            };
            geoJSONList = downloadFolder.list(filter);
        } else {
            geoJSONList = new String[0];
        }
        return Arrays.asList(geoJSONList);
    }
}
