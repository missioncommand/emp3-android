package mil.emp3.test.emp3vv.containers.dialogs;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;

import java.util.List;

import mil.emp3.api.enums.VisibilityStateEnum;
import mil.emp3.api.interfaces.IContainer;
import mil.emp3.api.interfaces.IMap;
import mil.emp3.test.emp3vv.R;
import mil.emp3.test.emp3vv.common.Emp3TesterDialogBase;
import mil.emp3.test.emp3vv.dialogs.utils.ErrorDialog;
import mil.emp3.test.emp3vv.utils.MapNamesUtility;
import mil.emp3.test.emp3vv.utils.PositionUtility;

abstract public class UpdateContainerDialog extends Emp3TesterDialogBase {
    private static String TAG = UpdateContainerDialog.class.getSimpleName();

    protected IContainer me;    // This is the container under edit.

    private EditText meName;
    private CheckBox meVisible;
    private ListView myParentList;
    private ArrayAdapter<String> myParentListAdapter;
    private List<String> myParentListData;

    private ListView myChildrenList;
    private ArrayAdapter myChildrenListAdapter;
    private List<String> myChildrenListData;

    protected ListView addParentsList;
    protected ArrayAdapter addParentsListAdapter;
    protected List<String> addParentListData;

    protected ListView addChildrenList;
    protected ArrayAdapter<String> addChildrenListAdapter;
    protected List<String> addChildrenListData;

    protected PositionUtility positionUtility;

    public interface IUpdateContainerDialogListener extends IEmp3TesterDialogBaseListener {
        void removeMe(UpdateContainerDialog dialog);
        void updateName(UpdateContainerDialog dialog);
        boolean updateVisibility(UpdateContainerDialog dialog);
        void removeFromParents(UpdateContainerDialog dialog);
        void removeChildren(UpdateContainerDialog dialog);
        void addParents(UpdateContainerDialog dialog);
        void addChildren(UpdateContainerDialog dialog);
    }

    public UpdateContainerDialog() {

    }

    protected void init(IMap map, IContainer me, UpdateContainerDialog.IUpdateContainerDialogListener listener, boolean showOnly) {
        super.init(map, listener, showOnly);
        if (null == me) {
            throw new IllegalArgumentException("me-container must be non-null");
        }

        Bundle args = new Bundle();
        this.setArguments(args);

        this.me = me;
    }

    public String getMeName() {
        return meName.getText().toString().trim();
    }

    public boolean getMeVisible() {
        return meVisible.isChecked();
    }

    public List<IContainer> getRemoveFromParents() {
        return null;
    }

    public IContainer getMe() {
        return me;
    }

    public IMap getMap() { return map; }

    public List<String> getSelectedFromMyParentList() {
        return(getSelectedFromMultiChoiceList(myParentList));
    }

    public List<String> getSelectedFromMyChildrenList() {
        return(getSelectedFromMultiChoiceList(myChildrenList));
    }

    public List<String> getSelectedFromAddParentsList() {
        return(getSelectedFromMultiChoiceList(addParentsList));
    }

    public List<String> getSelectedFromAddChildrenList() {
        return(getSelectedFromMultiChoiceList(addChildrenList));
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.update_container_dialog, container);
        getDialog().getWindow().setGravity(Gravity.LEFT | Gravity.BOTTOM);
        return v;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        meName = (EditText) view.findViewById(R.id.me_name);
        meName.setText(me.getName());
        final Button updateMeName = (Button) view.findViewById(R.id.update_me_name);
        if(!showOnly) {
            updateMeName.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(meName.getText().toString().trim().equals(me.getName())) {
                        return;
                    }

                    boolean nameInUse = MapNamesUtility.isNameInUse(map, meName.getText().toString().trim());
                    if(nameInUse) {
                        ErrorDialog.showError(getContext(),"Name is already in use, please select another");
                    } else {
                        ((IUpdateContainerDialogListener)listener).updateName(UpdateContainerDialog.this);
                        resetListsOnDataChange();
                        updateName(meName.getText().toString().trim());
                        // need to repopulate all the lists as name has changed
                    }
                }
            });
        } else {
            meName.setEnabled(false);
            updateMeName.setVisibility(View.GONE);
        }

        meVisible = (CheckBox) view.findViewById(R.id.me_visible);
        meVisible.setChecked( map.getVisibility(me) == VisibilityStateEnum.HIDDEN ? false : true);
        Button updateMeVisible = (Button) view.findViewById(R.id.update_me_visible);
        if(!showOnly) {
            updateMeVisible.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d(TAG, "getCheckedCount " + myParentList.getCheckedItemCount());
                    if(!((IUpdateContainerDialogListener)listener).updateVisibility(UpdateContainerDialog.this)) {
                        meVisible.setChecked(map.getVisibility(me) == VisibilityStateEnum.HIDDEN ? false : true);
                    }
                }
            });
        } else {
            meVisible.setEnabled(false);
            updateMeVisible.setVisibility(View.GONE);
        }

        setupMyParentsList(view);
        setupMyChildrenList(view);

        if(!showOnly) {
            setupAddParentList(view);
            setupAddChildrenList(view);
        } else {
            setupDescendantsList(view);
        }

        Button doneButton = (Button) view.findViewById(R.id.done);
        doneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(null != positionUtility) {
                    positionUtility.stop();
                }
                UpdateContainerDialog.this.dismiss();
            }
        });

        Button removeMeButton = (Button) view.findViewById(R.id.remove_me);
        if(!showOnly) {
            removeMeButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ((IUpdateContainerDialogListener)listener).removeMe(UpdateContainerDialog.this);
                    UpdateContainerDialog.this.dismiss();
                }
            });
        } else {
            removeMeButton.setVisibility(View.GONE);
        }

        setupUpdatePropertiesButton(view, myParentListData);
        setupOptional(view);
        positionUtility = setupPositionUtility();
    }

    protected void resetListsOnDataChange() {
        resetMyParentsList();
        resetMyChildrenList();

        if(!showOnly) {
            resetAddParentList();
            resetAddChildrenList();
        } else {
            // resetDescendantsList(view); not required as it is shown only in read only mode and will not have to be reset.
        }
    }
    private void setupMyParentsList(View view) {
        myParentList = (ListView) view.findViewById(R.id.my_parent_list);
        myParentListData = MapNamesUtility.getParentNames(me);
        myParentListAdapter = setupMultiChoiceList("Parents", myParentList, myParentListData);
        Button removeFromButton = (Button) view.findViewById(R.id.remove_from_parent);
        if(!showOnly) {
            removeFromButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(myParentList.getCheckedItemCount() == 0) {
                        ErrorDialog.showError(getContext(),"You must select at least one parent");
                        return;
                    } else {
                        Log.d(TAG, "getCheckedCount " + myParentList.getCheckedItemCount());
                        ((IUpdateContainerDialogListener)listener).removeFromParents(UpdateContainerDialog.this);
                        resetListsOnDataChange();
                    }
                }
            });
        } else {
            removeFromButton.setVisibility(View.GONE);
        }
    }

    private void resetMyParentsList() {
        myParentListData.clear();
        myParentListData.addAll(MapNamesUtility.getParentNames(me));
        myParentListAdapter.notifyDataSetChanged();
    }

    private void setupMyChildrenList(View view) {
        myChildrenList = (ListView) view.findViewById(R.id.my_children_list);
        myChildrenListData = MapNamesUtility.getChildrenNames(me);
        myChildrenListAdapter = setupMultiChoiceList("Children", myChildrenList, myChildrenListData);
        Button removeChildrenButton = (Button) view.findViewById(R.id.remove_children);
        if(!showOnly) {
            removeChildrenButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(myChildrenList.getCheckedItemCount() == 0) {
                        ErrorDialog.showError(getContext(),"You must select at least one child");
                        return;
                    } else {
                        Log.d(TAG, "getCheckedCount " + myChildrenList.getCheckedItemCount());
                        ((IUpdateContainerDialogListener)listener).removeChildren(UpdateContainerDialog.this);
                        resetListsOnDataChange();
                    }
                }
            });
        } else {
            removeChildrenButton.setVisibility(View.GONE);
        }
    }

    private void resetMyChildrenList() {
        myChildrenListData.clear();
        myChildrenListData.addAll(MapNamesUtility.getChildrenNames(me));
        myChildrenListAdapter.notifyDataSetChanged();
    }

    protected void setupDescendantsList(View view ) {
        // Hijack addParents list as it won't be shown
        addParentsList = (ListView) view.findViewById(R.id.add_parents_list);
        addParentsListAdapter = setupMultiChoiceList("Descendants", addParentsList, MapNamesUtility.getDescendantsNames(me));

        Button addParentsButton = (Button) view.findViewById(R.id.add_parents);
        addParentsButton.setVisibility(View.GONE);

        // Removing the add children list is causing issues in UI so just remove the button for now. We will see an
        // empty column.
        Button addChildrenButton = (Button) view.findViewById(R.id.add_children);
        addChildrenButton.setVisibility(View.GONE);
    }
    /**
     * These can be added as my parents. This should be overridden by sub class as features can have feature/overlays as
     * parent where as overlays can have overlays/map as parent.
     *
     * @param view
     */
    abstract protected void setupAddParentList(View view);
    abstract protected void resetAddParentList();
    /**
     * These can be added as my children. Type of children depend on what is being updated Overlay or Feature
     * @param view
     */
    abstract protected void setupAddChildrenList(View view);
    abstract protected void resetAddChildrenList();

    protected void setupOptional(View view) {
        view.findViewById(R.id.position_layout).setVisibility(View.GONE);
        view.findViewById(R.id.symbol_layout).setVisibility(View.GONE);
    }

    /**
     * UpdateFeatureDialog creates this so that feature position(s) can be updated.
     * @return
     */
    protected PositionUtility setupPositionUtility() { return null; }
    /**
     * This is overridden by UpdateFeatureDialog to allow user to update properties for basic shapes, Circle, Ellipse, Rectangle and Square
     * @param view
     * @param parentList
     */
    protected void setupUpdatePropertiesButton(View view, final List<String> parentList) {
        final Button updateProperties = (Button) view.findViewById(R.id.update_other_properties);
        updateProperties.setVisibility(View.GONE);
    }

    /**
     * This is overridden by UpdateFeatureDialog to allow user to update properties for basic shapes, Circle, Ellipse, Rectangle and Square
     * @param parentList
     * @param featureName
     * @param visible
     */
    abstract protected void updateOtherProperties(List<String> parentList, String featureName, boolean visible);

    /**
     * Sub class should override this if required. UpdateFeatureDialog uses this to setText on Text feature.
     * @param newName
     */
    protected void updateName(String newName) {

    }
}
