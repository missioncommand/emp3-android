package mil.emp3.test.emp3vv.containers.dialogs;


import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;

import java.util.List;

import mil.emp3.api.interfaces.IMap;
import mil.emp3.api.interfaces.IOverlay;
import mil.emp3.test.emp3vv.R;
import mil.emp3.test.emp3vv.dialogs.utils.ErrorDialog;
import mil.emp3.test.emp3vv.utils.MapNamesUtility;

public class UpdateOverlayDialog extends UpdateContainerDialog {
    private static String TAG = UpdateOverlayDialog.class.getSimpleName();
    public UpdateOverlayDialog() {

    }

    public static UpdateOverlayDialog newInstance(IMap map, IOverlay overlay, IUpdateContainerDialogListener listener, boolean showOnly) {

        if ((null == listener) || (null == map) || (null == overlay)) {
            throw new IllegalArgumentException("listener/map/overlay must be non-null");
        }

        UpdateOverlayDialog frag = new UpdateOverlayDialog();
        frag.init(map, overlay, listener, showOnly);

        return frag;
    }

    @Override
    protected void setupAddParentList(View view) {
        addParentsList = (ListView) view.findViewById(R.id.add_parents_list);
        addParentListData = MapNamesUtility.getNames(map, true, true, false);
        addParentsListAdapter = setupMultiChoiceList("New Parents", addParentsList, addParentListData);

        Button addParentsButton = (Button) view.findViewById(R.id.add_parents);
        addParentsButton.setOnClickListener(v -> {
            if(addParentsList.getCheckedItemCount() == 0) {
                ErrorDialog.showError(getContext(),"You must select at least one parent");
                return;
            } else {
                Log.d(TAG, "getCheckedCount " + addParentsList.getCheckedItemCount());
                ((IUpdateContainerDialogListener)listener).addParents(UpdateOverlayDialog.this);
                resetListsOnDataChange();
            }
        });
    }

    @Override
    protected void setupAddChildrenList(View view) {
        addChildrenList = (ListView) view.findViewById(R.id.add_children_list);
        addChildrenListData = MapNamesUtility.getNames(map, false, true, true);
        addChildrenListAdapter = setupMultiChoiceList("New Children", addChildrenList, addChildrenListData);

        Button addChildrenButton = (Button) view.findViewById(R.id.add_children);
        addChildrenButton.setOnClickListener(v -> {
            if(addChildrenList.getCheckedItemCount() == 0) {
                ErrorDialog.showError(getContext(),"You must select at least one child");
                return;
            } else {
                Log.d(TAG, "addChildrenList getCheckedCount " + addChildrenList.getCheckedItemCount());
                ((IUpdateContainerDialogListener)listener).addChildren(UpdateOverlayDialog.this);
                resetListsOnDataChange();
            }
        });
    }

    @Override
    protected void resetAddParentList() {
        addParentListData.clear();
        addParentListData.addAll(MapNamesUtility.getNames(map, true, true, false));
        addParentsListAdapter.notifyDataSetChanged();
    }

    @Override
    protected void resetAddChildrenList() {
        addChildrenListData.clear();
        addChildrenListData.addAll(MapNamesUtility.getNames(map, false, true, true));
        addChildrenListAdapter.notifyDataSetChanged();
    }

    protected void updateOtherProperties(List<String> parentList, String featureName, boolean visible) {

    }
}
