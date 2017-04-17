package mil.emp3.test.emp3vv.containers.dialogs;

import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.ListView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import mil.emp3.api.interfaces.IMap;
import mil.emp3.test.emp3vv.R;

public class FeatureFromFilePropertiesDialog extends FeaturePropertiesDialog<FeatureFromFilePropertiesDialog> {
    private static String TAG = FeatureFromFilePropertiesDialog.class.getSimpleName();

    private ListView fileList;
    private String SUFFIX;

    public static FeatureFromFilePropertiesDialog newInstance(String title, IMap map, List<String> parentList,
                                                      String featureName, boolean visible,
                                                      FeaturePropertiesDialog.FeaturePropertiesDialogListener<FeatureFromFilePropertiesDialog> listener,
                                                              String suffix) {

        FeatureFromFilePropertiesDialog frag = new FeatureFromFilePropertiesDialog();
        frag.init(title, map, parentList, featureName, visible, listener);
        frag.SUFFIX = suffix;
        return frag;
    }

    public static FeatureFromFilePropertiesDialog newInstanceForOpt(String title, IMap map, List<String> parentList,
                                                            String featureName, boolean visible,
                                                            FeaturePropertiesDialog.FeaturePropertiesDialogListener<FeatureFromFilePropertiesDialog> listener,
                                                            String suffix) {

        FeatureFromFilePropertiesDialog frag = new FeatureFromFilePropertiesDialog();
        frag.initForOptItem(title, map, parentList, featureName, visible, listener, null );
        frag.SUFFIX = suffix;
        return frag;
    }

    @Override
    public void onViewCreated(View view, final Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        fileList = (ListView) view.findViewById(R.id.file_list);
        fileList.setVisibility(View.VISIBLE);
        List<String> availableFiles = getFileList(SUFFIX);
        if((null != availableFiles) && (availableFiles.size() > 0)) {
            setupSingleChoiceList("Choose File", fileList, availableFiles);
        }
    }

    public String getSelectedFile() {
        return getSelectedFromSingleChoiceList(fileList);
    }

    public List<String> getFileList(String suffix) {

        try {
            String root_sd = Environment.getExternalStorageDirectory().getPath();
            File directory = new File(root_sd + "/testFiles");
            List<String> testFiles = new ArrayList<>();

            File[] files = directory.listFiles();
            for (File file : files) {
                if (!file.isDirectory() && file.getName().endsWith(suffix)) {
                    testFiles.add(directory.getPath() + File.separator + file.getName());
                }
            }
            return testFiles;
        } catch(Exception e) {
            Log.e(TAG, "getFileList ", e);
        }
        return null;
    }

}
