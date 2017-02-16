package mil.emp3.test.emp3vv.containers.dialogs;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;

import java.util.List;

import mil.emp3.api.Circle;
import mil.emp3.api.interfaces.IMap;
import mil.emp3.test.emp3vv.R;


public class CirclePropertiesDialog extends FeaturePropertiesDialog<CirclePropertiesDialog> {

    private Circle circle;

    public static CirclePropertiesDialog newInstance(String title, IMap map, List<String> parentList,
            String featureName, boolean visible,
            FeaturePropertiesDialog.FeaturePropertiesDialogListener<CirclePropertiesDialog> listener) {

        CirclePropertiesDialog frag = new CirclePropertiesDialog();
        frag.init(title, map, parentList, featureName, visible, listener);
        return frag;
    }

    public static CirclePropertiesDialog newInstanceForOpt(String title, IMap map, List<String> parentList,
                                                     String featureName, boolean visible,
                                                     FeaturePropertiesDialog.FeaturePropertiesDialogListener<CirclePropertiesDialog> listener,
                                                           Circle circle) {

        CirclePropertiesDialog frag = new CirclePropertiesDialog();
        frag.initForOptItem(title, map, parentList, featureName, visible, listener, circle);
        frag.circle = circle;
        return frag;
    }

    @Override
    public void onViewCreated(View view, final Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //this.activateAzimuth();

        LinearLayout radiusLayout = (LinearLayout) oDialogView.findViewById(R.id.radius);
        radiusLayout.setVisibility(View.VISIBLE);
        if (null != circle) { /* Updating an existing feature properties */
            EditText editCtrl = (EditText) oDialogView.findViewById(R.id.radiusValue);
            editCtrl.setText(String.valueOf(circle.getRadius()));
        }
    }

    public double getRadiusValue() {
        EditText editCtrl = (EditText) oDialogView.findViewById(R.id.radiusValue);
        return Double.parseDouble(editCtrl.getText().toString());
    }

    @Override
    protected boolean isBufferApplicable() { return true; }
}
