package mil.emp3.validator.fragment;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.SimpleExpandableListAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import mil.emp3.validator.R;

public class SideNavFragment extends Fragment {
    static final private String TAG = SideNavFragment.class.getSimpleName();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView");

        final View view = inflater.inflate(R.layout.fragment_side_nav, container, false);

        return view;
    }

    @Override
    public void onResume() {
        Log.d(TAG, "onResume");
        super.onResume();

        final InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(getView().getWindowToken(), 0);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        final String NAME = "NAME";
        //TODO clean up
        final String[] group = { "General" , "Map", "Overlay", "Features" };
        final String[][] child = {
                { "Create a Map", "Populate Map Data", "Create Overlays", "Create Cameras", "Create Air Control Measure", "Create Mil. Std. Symbols", "Create Points", "Create Paths", "Create Polygons", "Create Rectangles", "Create Squares", "Create Text", "Create WMS", "Delete Features" },
                { "addEventListener", "addMapService", "drawFeature", "editFeature", "getAllOverlays", "getCamera", "getMapServices", "removeMapServices", "removeOverlays", "setCamera", "setExtent", "setVisibility", "zoomTo", "addOverlay" },
                { "apply", "addFeatures", "getFeatures", "removeFeatures", "addOverlays", "getOverlays", "removeOverlays", "clearContainer" },
                { "addFeature", "clearContainer" },
        };

        final List<Map<String, String>> groupData = new ArrayList<>();
        final List<List<Map<String, String>>> childData = new ArrayList<>();
        for (int i = 0; i < group.length; i++) {
            Map<String, String> curGroupMap = new HashMap<>();
            groupData.add(curGroupMap);
            curGroupMap.put(NAME, group[i]);

            List<Map<String, String>> children = new ArrayList<>();
            for (int j = 0; j < child[i].length; j++) {
                Map<String, String> curChildMap = new HashMap<>();
                children.add(curChildMap);
                curChildMap.put(NAME, child[i][j]);
            }
            childData.add(children);
        }

        final ExpandableListAdapter adapter = new SimpleExpandableListAdapter(
                getActivity(),
                groupData,
                android.R.layout.simple_expandable_list_item_1,
                new String[] { NAME },
                new int[] { android.R.id.text1 },
                childData,
                android.R.layout.simple_expandable_list_item_2,
                new String[] { NAME },
                new int[] { android.R.id.text1 }
        );

        final ExpandableListView listView = (ExpandableListView) getView().findViewById(R.id.listView);
        listView.setAdapter(adapter);

        listView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
                Log.d(TAG, "child[" + groupPosition + "][" + childPosition + "]: " + child[groupPosition][childPosition]);

                // TODO command objects
                final String selected = child[groupPosition][childPosition];
                switch (selected) {
                    case "Create a Map": {
                        getFragmentManager().beginTransaction().replace(R.id.sideNav_fragment, new MapValidatorFragment())
                            .addToBackStack(null)
                            .commit();
                        break;
                    }
                    case "Create Overlays": {
                        getFragmentManager().beginTransaction().replace(R.id.sideNav_fragment, new OverlayValidatorFragment())
                            .addToBackStack(null)
                            .commit();

                        break;
                    }
                    case "Create Cameras": {
                        getFragmentManager().beginTransaction().replace(R.id.sideNav_fragment, new CameraValidatorFragment())
                            .addToBackStack(null)
                            .commit();

                        break;
                    }
                    case "Create WMS": {
                        getFragmentManager().beginTransaction().replace(R.id.sideNav_fragment, new WmsValidatorFragment())
                            .addToBackStack(null)
                            .commit();

                        break;
                    }
                    default: {
                        Log.w(TAG, "Handle: " + selected);
                    }
                }

                return true;
            }
        });
    }
}
