package mil.emp3.test.emp3vv.containers.dialogs;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ListView;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Arrays;
import java.util.List;

import mil.emp3.api.GeoJSON;
import mil.emp3.api.interfaces.IMap;
import mil.emp3.test.emp3vv.R;

public class GeoJSONPropertiesDialog extends FeaturePropertiesDialog<GeoJSONPropertiesDialog> {
    private static String TAG = GeoJSONPropertiesDialog.class.getSimpleName();

    private GeoJSON geoJSON;
    private ListView fileList;
    private File downloadFolder = new File("/sdcard/Download/");
    private static final String SUFFIX = ".geojson";

    public static GeoJSONPropertiesDialog newInstance(String title, IMap map, List<String> parentList,
                                                        String featureName, boolean visible,
                                                        FeaturePropertiesDialog.FeaturePropertiesDialogListener<GeoJSONPropertiesDialog> listener) {

        GeoJSONPropertiesDialog frag = new GeoJSONPropertiesDialog();
        frag.init(title, map, parentList, featureName, visible, listener);
        return frag;
    }

    public static GeoJSONPropertiesDialog newInstanceForOpt(String title, IMap map, List<String> parentList,
                                                              String featureName, boolean visible,
                                                              FeaturePropertiesDialog.FeaturePropertiesDialogListener<GeoJSONPropertiesDialog> listener,
                                                            GeoJSON geoJSON) {

        GeoJSONPropertiesDialog frag = new GeoJSONPropertiesDialog();
        frag.initForOptItem(title, map, parentList, featureName, visible, listener, geoJSON);
        frag.geoJSON = geoJSON;
        return frag;
    }

    @Override
    protected boolean isBufferApplicable() { return false; }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        oDialogView = inflater.inflate(R.layout.geojson_properties_dialog, container);
        isFeaturePositionSettable = false;
        setDialogPosition();
        return oDialogView;
    }

    @Override
    public void onViewCreated(View view, final Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        CheckBox geoJSONVisible = (CheckBox)view.findViewById(R.id.geojson_visible);
        geoJSONVisible.setChecked(featureVisible);
        fileList = (ListView) view.findViewById(R.id.geojson_list);
        setupSingleChoiceList("Choose File", fileList, getGeoJSONList());
    }

    public String getSelectedGeoJSON() {
        return getSelectedFromSingleChoiceList(fileList);
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
            Log.i(TAG, "No geojson files found in folder");
        }
        return Arrays.asList(geoJSONList);
    }

}

