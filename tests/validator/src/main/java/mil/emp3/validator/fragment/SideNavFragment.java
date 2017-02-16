package mil.emp3.validator.fragment;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
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
import java.util.UUID;


import mil.emp3.api.exceptions.EMP_Exception;
import mil.emp3.validator.PlotModeEnum;
import mil.emp3.validator.R;
import mil.emp3.validator.ValidatorStateManager;
import mil.emp3.validator.dialogs.milstdtacticalgraphics.TacticalGraphicPropertiesDialog;
import mil.emp3.validator.dialogs.milstdunits.SymbolPropertiesDialog;
import mil.emp3.validator.features.FeatureDrawListener;
import mil.emp3.validator.features.LinePath;
import mil.emp3.validator.features.Polygon;
import mil.emp3.validator.model.ManagedMapFragment;

public class SideNavFragment extends Fragment {
    static final private String TAG = SideNavFragment.class.getSimpleName();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
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
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    private ManagedMapFragment getCurrentMap() {
        ManagedMapFragment currentMap = null;
        try {
            currentMap = ValidatorStateManager.getInstance().getMap();
        } catch (Exception e) {
            Log.d(TAG, "No map found");
        }
        if (currentMap != null) {
            currentMap.setPlotMode(PlotModeEnum.NEW);
        }
        return  currentMap;
    }

    private boolean checkMap() {
        if (getCurrentMap() != null) {
            return true;
        }
        if (this.isAdded()) {
            new AlertDialog.Builder(this.getActivity())
                    .setTitle("ERROR:")
                    .setMessage(" You must create a map first !")
                    .setNeutralButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface arg0, int arg1) {
                        }
                    }).create().show();
        }
        return false;
    }

    final String NAME = "NAME";
    //TODO clean up
    final String[] group = {"General", "Map", "Overlay", "Features"};
    final String[][] child = {
            {"Create a Map", "populate Map Data", "Create Overlays", "Create Cameras", "Create Air Control Measure", "Create Units", "Create Graphics", "Create Points", "Create Paths", "Create Polygons", /*"Create Rectangles", "Create Squares",*/ "create Text", "Create WMS", "delete Features"},
            {"addEventListener", "addMapService", "drawFeature", "editFeature", "getAllOverlays", "getCamera", "getMapServices", "removeMapServices", "removeOverlays", "setCamera", "setExtent", "setVisibility", "zoomTo", "addOverlay"},
            {"Apply", "Add Features", "getFeatures", "Remove Features", "Add Overlays", "getOverlays", "removeOverlays", "clearContainer"},
            {"addFeature", "clearContainer"},
    };

    @Override
    public void onActivityCreated(final Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);


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
                new String[]{NAME},
                new int[]{android.R.id.text1},
                childData,
                android.R.layout.simple_expandable_list_item_2,
                new String[]{NAME},
                new int[]{android.R.id.text1}
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
                    case "Create a Map":
                        getFragmentManager().beginTransaction().replace(R.id.sideNav_fragment, new MapValidatorFragment())
                                .addToBackStack(null)
                                .commit();
                        break;

                    case "Create Overlays":
                        if (checkMap()) {
                            getFragmentManager().beginTransaction().replace(R.id.sideNav_fragment, new OverlayValidatorFragment())
                                    .addToBackStack(null)
                                    .commit();
                        }
                        break;

                    case "Create Cameras":
                        if (checkMap()) {
                            getFragmentManager().beginTransaction().replace(R.id.sideNav_fragment, new CameraValidatorFragment())
                                    .addToBackStack(null)
                                    .commit();
                        }
                        break;

                    case "Create WMS":
                        getFragmentManager().beginTransaction().replace(R.id.sideNav_fragment, new WmsValidatorFragment())
                                .addToBackStack(null)
                                .commit();

                        break;


                    case "Create Graphics":
                        if (checkMap()) {
                            ManagedMapFragment currentMap = ValidatorStateManager.getInstance().getMap();
                            currentMap.setPlotMode(PlotModeEnum.DRAW_FEATURE);
                            TacticalGraphicPropertiesDialog oDialog = new TacticalGraphicPropertiesDialog();
                            oDialog.show(getFragmentManager(), "Tactical Graphic Properties");
                        }
                        break;


                    case "Create Units":
                        if (checkMap()) {
                            ManagedMapFragment currentMap = ValidatorStateManager.getInstance().getMap();
                            currentMap.setPlotMode(PlotModeEnum.NEW);
                            SymbolPropertiesDialog oDialog = new SymbolPropertiesDialog();
                            oDialog.show(getFragmentManager(), "Tactical Graphic Properties");
                        }
                        break;


                    case "Create Points":
                        if (checkMap()) {
                            mil.emp3.api.Point point = new mil.emp3.api.Point();
                            try {
                                ManagedMapFragment currentMap = getCurrentMap();
                                FeatureDrawListener featureDrawListener = new FeatureDrawListener(currentMap,
                                        getFragmentManager());
                                currentMap.get().drawFeature(point, featureDrawListener);
                            } catch (EMP_Exception Ex) {
                                Log.e(TAG, "Draw polygon failed.");
                            }
                        }
                        break;


                    case "Create Paths":
                        if (checkMap()) {
                            LinePath path = new LinePath(getFragmentManager());
                            path.createPath(getCurrentMap());
                        }
                        break;


                    case "Create Polygons":
                        if (checkMap()) {
                            Polygon polygon = new Polygon(getFragmentManager());
                            polygon.createPolygon(getCurrentMap());
                        }
                        break;

                    case "Add Features" :
                        if (checkMap()) {
                            FeatureListFragment fragment = new FeatureListFragment();
                            Bundle arguments = new Bundle();
                            arguments.putString("FeatureListOperation", "Add");
                            fragment.setArguments(arguments);
                            getFragmentManager().beginTransaction().replace(R.id.sideNav_fragment, fragment)
                                    .addToBackStack(null)
                                    .commit();
                        }
                        break;

                    case "Remove Features" :
                        if (checkMap()) {
                            FeatureListFragment fragment = new FeatureListFragment();
                            Bundle arguments = new Bundle();
                            arguments.putString("FeatureListOperation", "Remove");
                            fragment.setArguments(arguments);
                            getFragmentManager().beginTransaction().replace(R.id.sideNav_fragment, fragment)
                                    .addToBackStack(null)
                                    .commit();
                        }
                        break;

                    case "Apply" :
                        if (checkMap()) {
                            OverlayListFragment fragment = new OverlayListFragment();
                            Bundle arguments = new Bundle();
                            arguments.putString("OverlayListOperation", "Apply");
                            fragment.setArguments(arguments);
                            getFragmentManager().beginTransaction().replace(R.id.sideNav_fragment, fragment)
                                    .addToBackStack(null)
                                    .commit();
                        }
                        break;

                    case "Add Overlays" :
                        if (checkMap()) {
                            OverlayListFragment fragment = new OverlayListFragment();
                            Bundle arguments = new Bundle();
                            arguments.putString("OverlayListOperation", "Add");
                            fragment.setArguments(arguments);
                            getFragmentManager().beginTransaction().replace(R.id.sideNav_fragment, fragment)
                                    .addToBackStack(null)
                                    .commit();
                        }
                        break;

                    case "Remove Overlays" :
                        if (checkMap()) {
                            OverlayListFragment fragment = new OverlayListFragment();
                            Bundle arguments = new Bundle();
                            arguments.putString("OverlayListOperation", "Remove");
                            fragment.setArguments(arguments);
                            getFragmentManager().beginTransaction().replace(R.id.sideNav_fragment, fragment)
                                    .addToBackStack(null)
                                    .commit();
                        }
                        break;

                    default: {
                        new AlertDialog.Builder(SideNavFragment.this.getActivity())
                                .setTitle("ERROR:")
                                .setMessage(" Not implemented yet")
                                .setNeutralButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface arg0, int arg1) {
                                    }
                                }).create().show();
                    }
                }

                return true;
            }
        });
    }

}
