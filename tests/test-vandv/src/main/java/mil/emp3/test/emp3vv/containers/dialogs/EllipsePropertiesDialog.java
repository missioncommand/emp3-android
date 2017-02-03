package mil.emp3.test.emp3vv.containers.dialogs;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;

import java.util.List;

import mil.emp3.api.Ellipse;
import mil.emp3.api.interfaces.IMap;
import mil.emp3.test.emp3vv.R;

public class EllipsePropertiesDialog extends FeaturePropertiesDialog<EllipsePropertiesDialog> {
    private Ellipse ellipse;

    public static EllipsePropertiesDialog newInstance(String title, IMap map, List<String> parentList,
            String featureName, boolean visible,
            FeaturePropertiesDialog.FeaturePropertiesDialogListener<EllipsePropertiesDialog> listener) {

        EllipsePropertiesDialog frag = new EllipsePropertiesDialog();
        frag.init(title, map, parentList, featureName, visible, listener);
        return frag;
    }

    public static EllipsePropertiesDialog newInstanceForOpt(String title, IMap map, List<String> parentList,
                                                      String featureName, boolean visible,
                                                      FeaturePropertiesDialog.FeaturePropertiesDialogListener<EllipsePropertiesDialog> listener, Ellipse ellipse) {

        EllipsePropertiesDialog frag = new EllipsePropertiesDialog();
        frag.initForOptItem(title, map, parentList, featureName, visible, listener, ellipse);
        frag.ellipse = ellipse;
        return frag;
    }

    @Override
    public void onViewCreated(View view, final Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        this.activateAzimuth();

        LinearLayout semiMajorLayout = (LinearLayout) oDialogView.findViewById(R.id.semiMajor);
        semiMajorLayout.setVisibility(View.VISIBLE);

        LinearLayout semiMinorLayout = (LinearLayout) oDialogView.findViewById(R.id.semiMinor);
        semiMinorLayout.setVisibility(View.VISIBLE);

        if(null != ellipse) { /* Updating an existing feature properties */
            EditText editCtrlMaj = (EditText) oDialogView.findViewById(R.id.semiMajorValue);
            editCtrlMaj.setText(String.valueOf(ellipse.getSemiMajor()));
            EditText editCtrlMin = (EditText) oDialogView.findViewById(R.id.semiMinorValue);
            editCtrlMin.setText(String.valueOf(ellipse.getSemiMinor()));
            EditText azimuthCtrl = (EditText) oDialogView.findViewById(R.id.azimuthValue);
            azimuthCtrl.setText(String.valueOf(ellipse.getAzimuth()));
        }
    }

    public double getSemiMajorValue() {
        EditText editCtrl = (EditText) oDialogView.findViewById(R.id.semiMajorValue);
        return Double.parseDouble(editCtrl.getText().toString());
    }

    public double getSemiMinorValue() {
        EditText editCtrl = (EditText) oDialogView.findViewById(R.id.semiMinorValue);
        return Double.parseDouble(editCtrl.getText().toString());
    }

    @Override
    protected boolean isBufferApplicable() { return true; }
}
