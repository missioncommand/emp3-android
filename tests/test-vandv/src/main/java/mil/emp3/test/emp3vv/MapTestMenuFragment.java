package mil.emp3.test.emp3vv;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.PopupMenu;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.List;

import mil.emp3.test.emp3vv.common.ITestMenuManager;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * MapTestMenuFragment.OnFragmentInteractionListener interface
 * to handle interaction events.
 * Use the {@link MapTestMenuFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MapTestMenuFragment extends Fragment implements ITestMenuManager {
    private static String TAG = MapTestMenuFragment.class.getSimpleName();

    private OnNavItemSelectedListener mListener;
    private OnOptItemSelectedListener optItemSelectedListener;
    private List<MenuItem> items = new ArrayList<>();

    private LinearLayout userActions;
    private MoreHandler moreHandler;

    private int maxSupportedActions = 0;

    public MapTestMenuFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment MapTestMenuFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static MapTestMenuFragment newInstance(String param1, String param2) {
        MapTestMenuFragment fragment = new MapTestMenuFragment();
        return fragment;
    }

    @Override
    public int getMaxSupportedActions() {
        return maxSupportedActions;
    }

    @Override
    public void recreateTestMenu(String[] supportedUserActions, String[] moreUserActions) {
        disableUserActions();
        updateSupportedUserActions(supportedUserActions, moreUserActions);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    void toggleButtonSensitivity(boolean action) {
        if(null != items) {
            for(MenuItem item : items) {
                item.setEnabled(action);
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_map_test_menu, container, false);

        userActions = (LinearLayout) view.findViewById(R.id.UserActions);
        maxSupportedActions = userActions.getChildCount()-1;
        for(int ii = 0; ii < userActions.getChildCount(); ii++) {
            final Button button = (Button) userActions.getChildAt(ii);

            if(button.getId() == R.id.More) {
                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(null != moreHandler) {
                            moreHandler.showMenu(MapTestMenuFragment.this.getActivity(), button);
                        }
                    }
                });
            } else {
                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Log.d(TAG, "UserAction " + ((Button) v).getText());
                        if (null != mListener) {
                            mListener.onNavItemUserAction(((Button) v).getText().toString());
                            toggleButtonSensitivity(false);
                        }
                    }
                });
            }
        }
        return view;
    }

    public void updateSupportedUserActions(String []supportedUserActions, String[] moreUserActions) {

        if(null != supportedUserActions) {
            for (int ii = 0; ii < supportedUserActions.length; ii++) {
                if (ii < (userActions.getChildCount() - 1)) {
                    Button button = (Button) userActions.getChildAt(ii);
                    button.setText(supportedUserActions[ii]);
                    button.setVisibility(View.VISIBLE);
                } else {
                    Log.e(TAG, "No more buttons left " + supportedUserActions[ii]);
                    throw new IllegalStateException();
                }
            }
        }

        if(null != moreUserActions) {
            moreHandler = new MoreHandler(moreUserActions);
            Button button = (Button) userActions.getChildAt(userActions.getChildCount()-1);
            button.setVisibility(View.VISIBLE);
        }
    }

    public void disableUserActions() {
        for(int ii = 0; ii < userActions.getChildCount(); ii++) {
            Button button = (Button) userActions.getChildAt(ii);
            button.setVisibility(View.INVISIBLE);
        }
        moreHandler = null;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        Log.d(TAG, "onAttach");
        if (context instanceof OnNavItemSelectedListener) {
            mListener = (OnNavItemSelectedListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnNavItemSelectedListener");
        }

        if (context instanceof OnOptItemSelectedListener) {
            optItemSelectedListener = (OnOptItemSelectedListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnOptItemSelectedListener");
        }
    }

    @Override
    public void onAttach(Activity context) {
        super.onAttach(context);
        Log.d(TAG, "onAttach deprecated");
        if (context instanceof OnNavItemSelectedListener) {
            mListener = (OnNavItemSelectedListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnNavItemSelectedListener");
        }

        if (context instanceof OnOptItemSelectedListener) {
            optItemSelectedListener = (OnOptItemSelectedListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnOptItemSelectedListener");
        }
    }
    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
        optItemSelectedListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnNavItemSelectedListener {
        // TODO: Update argument type and name
        void onNavItemSelected(String selectedTest);
        void onNavItemUserAction(String userAction);
    }

    public interface OnOptItemSelectedListener {
        void onOptItemSelected(String selectedOptItem);
    }

    public void testComplete(String testCompleted) {
        Log.d(TAG, "testCompleted " + testCompleted);
        if(this.getActivity() != null) {
            this.getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    toggleButtonSensitivity(true);
                    disableUserActions();
                }
            });
        }
    }

    public boolean optionsItemSelected(MenuItem item) {

        Log.d(TAG, "optionsItemSelected item.getTitle() " + item.getTitle());

        if(null != optItemSelectedListener) {
            Log.d(TAG, "Execute " + item.getTitle());
            optItemSelectedListener.onOptItemSelected(item.getTitle().toString());
            return true;
        }

        return false;
    }

    public boolean onNavigationItemSelected(MenuItem item) {

        Log.d(TAG, "onOptionsItemSelected item.getTitle() " + item.getTitle());

        if(null != mListener) {
            Log.d(TAG, "Execute " + item.getTitle());
            mListener.onNavItemSelected(item.getTitle().toString());
            toggleButtonSensitivity(false);
            return true;
        }

        return false;
    }

    class MoreHandler implements PopupMenu.OnMenuItemClickListener {
        private String[] menuItems;
        MoreHandler(String[] actions) {
            this.menuItems = actions;
        }

        void showMenu(Context context, View v) {
            PopupMenu popup = new PopupMenu(context, v);

            // This activity implements OnMenuItemClickListener
            popup.setOnMenuItemClickListener(this);

            for(int ii = 0; ii < menuItems.length; ii++) {
                popup.getMenu().add(menuItems[ii]);
            }
            popup.show();
        }
        @Override
        public boolean onMenuItemClick(MenuItem item) {

            if (null != mListener) {
                mListener.onNavItemUserAction(item.getTitle().toString());
                toggleButtonSensitivity(false);
            }
            return true;
        }
    }

    public void setNavigationView(NavigationView navigationView) {
        Log.d(TAG, "setNavigationView menu size " + navigationView.getMenu().size());
        for(int index = 0; index < navigationView.getMenu().size(); index++) {
            MenuItem item = navigationView.getMenu().getItem(index);
            if(null != item.getSubMenu()) {
                SubMenu subMenu = item.getSubMenu();
                for(int sindex = 0; sindex < subMenu.size(); sindex++) {
                    items.add(subMenu.getItem(sindex));
                }
            } else {
                items.add(navigationView.getMenu().getItem(index));
            }
        }
    }

    public void preLaunchMap() {
        for(MenuItem item : items) {
            if(!item.getTitle().equals("Launch Map")) {
                item.setEnabled(false);
            }
        }
    }
}
