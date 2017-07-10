package mil.emp3.test.emp3vv.navItems.zoom_and_bounds;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import java.util.List;

import mil.emp3.api.interfaces.IMap;
import mil.emp3.api.interfaces.IOverlay;
import mil.emp3.test.emp3vv.R;
import mil.emp3.test.emp3vv.common.Emp3TesterDialogBase;
import mil.emp3.test.emp3vv.dialogs.utils.ErrorDialog;
import mil.emp3.test.emp3vv.utils.MapNamesUtility;

public class ZoomToDialog extends Emp3TesterDialogBase {
    private static String TAG = ZoomToDialog.class.getSimpleName();
    ListView allFeaturesList;
    ArrayAdapter<String> allFeaturesListAdapter;
    List<String> allFeaturesListData;

    ListView allOverlaysList;
    ArrayAdapter<String> allOverlaysListAdapter;
    List<String>  allOverlaysListData;

    public interface IZoomToDialogListener extends IEmp3TesterDialogBaseListener {
        void zoomToOverlay(ZoomToDialog dialog);
        void zoomToFeatures(ZoomToDialog dialog);
    }

    public ZoomToDialog() {
    }

    public static ZoomToDialog newInstance(String title, IMap map, IZoomToDialogListener listener) {
        ZoomToDialog frag = new ZoomToDialog();
        Bundle args = new Bundle();
        args.putString("title", title);
        frag.setArguments(args);
        frag.init(map, listener);
        return frag;
    }

    public static ZoomToDialog newInstanceForOptItem(String title, IMap map, IZoomToDialogListener listener) {
        ZoomToDialog frag = new ZoomToDialog();
        Bundle args = new Bundle();
        args.putString("title", title);
        frag.setArguments(args);
        frag.initForOptItem(map, listener);
        return frag;
    }
    public String getSelectedOverlay() {
        return getSelectedFromMultiChoiceList(allOverlaysList).get(0);
    }

    public List<String> getSelectedFeatures() {
        return getSelectedFromMultiChoiceList(allFeaturesList);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.zoom_to_dialog, container);
        setDialogPosition();
        return v;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        allFeaturesList = (ListView) view.findViewById(R.id.all_features_list);
        allFeaturesListData = MapNamesUtility.getNames(map, false, false, true);
        allFeaturesListAdapter = setupMultiChoiceList("All Features", allFeaturesList, allFeaturesListData);

        Button zoomToFeatures = (Button) view.findViewById(R.id.zoom_to_features);
        zoomToFeatures.setOnClickListener(v -> {
            if(allFeaturesList.getCheckedItemCount() < 1) {
                ErrorDialog.showError(getContext(), "Please select at least one feature");
            } else {
                ((IZoomToDialogListener)ZoomToDialog.this.listener).zoomToFeatures(ZoomToDialog.this);
            }
        });

        allOverlaysList = (ListView) view.findViewById(R.id.all_overlays_list);
        allOverlaysListData = MapNamesUtility.getNames(map, false, true, false);
        allOverlaysListAdapter = setupMultiChoiceList("All Overlays", allOverlaysList, allOverlaysListData);

        Button zoomToOverlay = (Button) view.findViewById(R.id.zoom_to_overlay);
        zoomToOverlay.setOnClickListener(v -> {
            if(allOverlaysList.getCheckedItemCount() != 1) {
                ErrorDialog.showError(getContext(), "Please select one and only one overlay");
            } else {
                ((IZoomToDialogListener)ZoomToDialog.this.listener).zoomToOverlay(ZoomToDialog.this);
            }
        });

        Button doneButton = (Button) view.findViewById(R.id.done);
        doneButton.setOnClickListener(v -> ZoomToDialog.this.dismiss());
    }
}
