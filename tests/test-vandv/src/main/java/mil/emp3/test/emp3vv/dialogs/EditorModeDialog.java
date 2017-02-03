package mil.emp3.test.emp3vv.dialogs;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import mil.emp3.api.enums.MapMotionLockEnum;
import mil.emp3.api.exceptions.EMP_Exception;
import mil.emp3.api.interfaces.IMap;
import mil.emp3.test.emp3vv.R;
import mil.emp3.test.emp3vv.common.Emp3TesterDialogBase;

public class EditorModeDialog extends Emp3TesterDialogBase {
    private static String TAG = EditorModeDialog.class.getSimpleName();

    ListView mapMotionList;
    ArrayAdapter<String> mapMotionListAdapter;
    List<String> mapMotionListData;

    TextView mapMotionLockMode;
    MapMotionLockEnum selectedMotionLockMode;

    public EditorModeDialog() {
        // Empty constructor is required for DialogFragment
        // Make sure not to add arguments to the constructor
        // Use `newInstance` instead as shown below
    }

    public static EditorModeDialog newInstance(String title, IEditorModeDialogListener listener, IMap map) {
        EditorModeDialog frag = new EditorModeDialog();
        Bundle args = new Bundle();
        args.putString("title", title);
        frag.setArguments(args);
        frag.init(map, listener);
        return frag;
    }

    public static EditorModeDialog newInstanceForOptItem(String title, IEditorModeDialogListener listener, IMap map) {
        EditorModeDialog frag = new EditorModeDialog();
        Bundle args = new Bundle();
        args.putString("title", title);
        frag.setArguments(args);
        frag.initForOptItem(map, listener);
        return frag;
    }

    public MapMotionLockEnum getSelectedMapMotionLockMode() {
        return selectedMotionLockMode;
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.editor_mode_dialog, container);
        setDialogPosition();
        return v;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TextView editorMode = (TextView) view.findViewById(R.id.editor_mode_value);
        try {
            editorMode.setText(map.getEditorMode().toString());
        } catch (EMP_Exception e) {
            Log.e(TAG, "getEditorMode FAILED ", e);
        }

        mapMotionLockMode = (TextView) view.findViewById(R.id.map_motion_lock_mode_value);
        try {
            mapMotionLockMode.setText(map.getMotionLockMode().toString());
            selectedMotionLockMode = map.getMotionLockMode();
        } catch (EMP_Exception e) {
            Log.e(TAG, "getEditorMode FAILED ", e);
        }

        mapMotionList = (ListView) view.findViewById(R.id.map_motion_list);
        mapMotionListData = new ArrayList<>();

        for(mil.emp3.api.enums.MapMotionLockEnum em: mil.emp3.api.enums.MapMotionLockEnum.values()) {
            mapMotionListData.add(em.toString());
        }
        mapMotionListAdapter = setupSingleChoiceList("Map Motion Lock Modes", mapMotionList, mapMotionListData);

        // Single choice getSelected doesn't work, so use this listener.
        mapMotionList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                selectedMotionLockMode = MapMotionLockEnum.valueOf(mapMotionListAdapter.getItem(position-1));
            }
        });

        Button doneButton = (Button) view.findViewById(R.id.done);
        doneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditorModeDialog.this.dismiss();
            }
        });

        Button applyButton = (Button) view.findViewById(R.id.apply);
        applyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != EditorModeDialog.this.listener) {
                    ((IEditorModeDialogListener)EditorModeDialog.this.listener).setMapMotionLockMode(EditorModeDialog.this);
                    try {
                        mapMotionLockMode.setText(map.getMotionLockMode().toString());
                    } catch(EMP_Exception e) {
                        Log.e(TAG, "motionLockMode ", e);
                    }
                }
            }
        });
    }

    public interface IEditorModeDialogListener extends IEmp3TesterDialogBaseListener {
        void setMapMotionLockMode(EditorModeDialog editorModeDialog);
    }
}
