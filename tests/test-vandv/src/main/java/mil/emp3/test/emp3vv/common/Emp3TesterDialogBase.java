package mil.emp3.test.emp3vv.common;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.util.Pair;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import mil.emp3.api.interfaces.IMap;

/**
 * Use this as a base class of your dialog if:
 *
 *     You don't want to make your dialog MODAL (because you want to allow user to interact with the map while the dialog is up)
 *     but you don't want to allow the user to access any other functionality of your test while this dialog is up, e.g. don't allow
 *     user to select Remove Feature while Add Feature is in progress.
 *
 *     NOTE the implication that you cannot use cascaded dialogs, You should dismiss the dialog before invoking the parent/listener of the
 *     dialog. Parent/listener of the dialog can then launch a new dialog if required.
 *
 *     NOTE when dialog is created by an optItem we don't register the dialog. Look at initForOptItem
 *
 *     This dialog base class should be used as base class for owners that are in the navItems package.
 *
 *     Additionally this class supports some generic ListView related methods that you can reuse. You can add any other capabilities
 *     to that class that have a potential for re-use.
 *
 *     When we come up with a better way of doing this we will retire this class.
 */

public abstract class Emp3TesterDialogBase extends DialogFragment {
    private static String TAG = Emp3TesterDialogBase.class.getSimpleName();
    protected IMap map;
    protected IEmp3TesterDialogBaseListener listener;
    protected boolean showOnly = false; // If this is set to true then all update and select functionality is disabled.
    private boolean registerActiveDialog = true; // If invoked from NavItem dialog will by default register as active Dialog
                                          // If invoked from OptItem dialog will not register as active dialog.

    private static Pair<String, Emp3TesterDialogBase> activeDialog;

    public interface IEmp3TesterDialogBaseListener {

    }

    public static boolean isEmp3TesterDialogBaseActive() {
       return (null != activeDialog ? true : false);
    }
    public IMap getMap() {
        return map;
    }

    public Emp3TesterDialogBase() {

    }

    protected void init(IMap map, IEmp3TesterDialogBaseListener listener) {
        if((null == map) || (null == listener)) {
            throw new IllegalArgumentException(TAG + "map and listener must be non-null");
        }
        this.map = map;
        this.listener = listener;
    }

    protected void init(IMap map, IEmp3TesterDialogBaseListener listener, boolean showOnly) {
        if((null == map) || (null == listener)) {
            throw new IllegalArgumentException(TAG + "map and listener must be non-null");
        }
        this.map = map;
        this.listener = listener;
        this.showOnly = showOnly;
    }

    protected void initForOptItem(IMap map, IEmp3TesterDialogBaseListener listener) {
        if((null == map) || (null == listener)) {
            throw new IllegalArgumentException(TAG + "map and listener must be non-null");
        }
        this.map = map;
        this.listener = listener;
        this.registerActiveDialog = false;
    }

    protected void init(IMap map) {
        if (null == map) {
            throw new IllegalArgumentException(TAG + "map must be non-null");
        }
        this.map = map;
    }

    protected void init(IMap map, boolean showOnly) {
        if (null == map) {
            throw new IllegalArgumentException(TAG + "map must be non-null");
        }
        this.map = map;
        this.showOnly = showOnly;
    }

    protected void initForOptItem(IMap map) {
        if (null == map) {
            throw new IllegalArgumentException(TAG + "map must be non-null");
        }
        this.map = map;
        this.registerActiveDialog = false;
    }

    @Override
    public void show(FragmentManager fm, String fragmentName) {
        if(!registerActiveDialog) {
            // This was launched from optItem and hence we will not register it. We want to show those dialogs any time
            // and optItem dialogs don't interfere with navItem dialogs.
            super.show(fm, fragmentName);
            return;
        }
        if(null == activeDialog) {
            activeDialog = new Pair<>(fragmentName, this);
            super.show(fm, fragmentName);
        } else {
            Log.e(TAG, "ERROR: Please read the comments in class Emp3TesterDialog");
        }
    }

    public void dismiss() {
        if(registerActiveDialog) {
            // This dialog was registered as active dialog so unregister it. Most probably a navItem dialog.
            activeDialog = null;
        }
        super.dismiss();
    }

    /**
     * Dialogs that want to register are navItem dialogs and are kept at left, bottom
     * Dialogs that don't want to register are optItem dialogs and are kept at right, bottom
     */
    protected void setDialogPosition() {
        if(registerActiveDialog) {
            getDialog().getWindow().setGravity(Gravity.LEFT | Gravity.BOTTOM);
        } else {
            getDialog().getWindow().setGravity(Gravity.RIGHT | Gravity.BOTTOM);
        }
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Dialog should remain active and showing even if user touches the screen outside the dialog area. We don't want this
        // dialog to be modal. We will depend on the application to prevent other dialogs from showing up if application
        // wishes to do so. Do not DIM the background widgets.
        Window dialogWindow = getDialog().getWindow();

        // Make the dialog possible to be outside touch
        dialogWindow.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL);
        dialogWindow.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
    }

    protected ArrayAdapter<String> setupMultiChoiceList(String header, ListView listView, List<String> elements) {
        TextView headerView = new TextView(getActivity());
        headerView.setText(header);
        listView.addHeaderView(headerView);
        if(!showOnly) {
            ArrayAdapter<String> listAdapter = new ArrayAdapter(getActivity(), android.R.layout.simple_list_item_multiple_choice, elements);
            listView.setAdapter(listAdapter);
            listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
            return listAdapter;
        } else {
            ArrayAdapter<String> listAdapter = new ArrayAdapter(getActivity(), android.R.layout.simple_list_item_1, elements);
            listView.setAdapter(listAdapter);
            listView.setChoiceMode(ListView.CHOICE_MODE_NONE);
            return listAdapter;
        }
    }

    protected List<String> getSelectedFromMultiChoiceList(ListView multiChoiceList) {
        SparseBooleanArray selectedPositions = multiChoiceList.getCheckedItemPositions();
        List<String> selectedFromMultiChoiceList = new ArrayList();
        for(int ii = 0; ii < selectedPositions.size(); ii++) {
            int key = selectedPositions.keyAt(ii);
            if(selectedPositions.get(key)) {
                // Make sure you skip the header ???
                if((null != (multiChoiceList.getAdapter().getItem(key)) && (multiChoiceList.getAdapter().getItem(key) instanceof String)))
                {
                    selectedFromMultiChoiceList.add(multiChoiceList.getAdapter().getItem(key).toString());
                }
            }
        }
        return selectedFromMultiChoiceList;
    }

    protected ArrayAdapter<String> setupSingleChoiceList(String header, ListView listView, List<String> elements) {
        TextView headerView = new TextView(getActivity());
        headerView.setText(header);
        listView.addHeaderView(headerView);
        if(!showOnly) {
            ArrayAdapter<String> listAdapter = new ArrayAdapter(getActivity(), android.R.layout.simple_list_item_single_choice, elements);
            listView.setAdapter(listAdapter);
            listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
            return listAdapter;
        } else {
            ArrayAdapter<String> listAdapter = new ArrayAdapter(getActivity(), android.R.layout.simple_list_item_1, elements);
            listView.setAdapter(listAdapter);
            listView.setChoiceMode(ListView.CHOICE_MODE_NONE);
            return listAdapter;
        }
    }

    protected String getSelectedFromSingleChoiceList(ListView singleChoiceList) {
        Log.d(TAG, "getSelectedFromSingleChoiceList getCheckedItemPosition " + singleChoiceList.getCheckedItemPosition());

        if(singleChoiceList.getCheckedItemPosition() < 0) {
            return null;
        } else {
            Log.d(TAG, "getSelectedFromSingleChoiceList selected " +
                    singleChoiceList.getAdapter().getItem(singleChoiceList.getCheckedItemPosition()).toString());
            return singleChoiceList.getAdapter().getItem(singleChoiceList.getCheckedItemPosition()).toString();
        }
    }
}
