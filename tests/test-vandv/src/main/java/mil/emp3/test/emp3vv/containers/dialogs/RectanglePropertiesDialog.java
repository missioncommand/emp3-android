package mil.emp3.test.emp3vv.containers.dialogs;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;

import java.util.List;

import mil.emp3.api.Rectangle;
import mil.emp3.api.interfaces.IMap;
import mil.emp3.test.emp3vv.R;

/**
 * This class implements the dialog for rectangle feature properties.
 */

public class RectanglePropertiesDialog extends FeaturePropertiesDialog<RectanglePropertiesDialog> {
    private Rectangle rectangle;
    public static RectanglePropertiesDialog newInstance(String title, IMap map, List<String> parentList,
            String featureName, boolean visible,
            FeaturePropertiesDialog.FeaturePropertiesDialogListener<RectanglePropertiesDialog> listener) {

        RectanglePropertiesDialog frag = new RectanglePropertiesDialog();
        frag.init(title, map, parentList, featureName, visible, listener);
        return frag;
    }

    public static RectanglePropertiesDialog newInstanceForOpt(String title, IMap map, List<String> parentList,
                                                        String featureName, boolean visible,
                                                        FeaturePropertiesDialog.FeaturePropertiesDialogListener<RectanglePropertiesDialog> listener,
                                                              Rectangle rectangle) {

        RectanglePropertiesDialog frag = new RectanglePropertiesDialog();
        frag.initForOptItem(title, map, parentList, featureName, visible, listener, rectangle);
        frag.rectangle = rectangle;
        return frag;
    }

    @Override
    protected boolean isBufferApplicable() { return true; }

    @Override
    public void onViewCreated(View view, final Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        this.activateAzimuth();

        LinearLayout widthLayout = (LinearLayout) oDialogView.findViewById(R.id.width);
        widthLayout.setVisibility(View.VISIBLE);

        LinearLayout heightLayout = (LinearLayout) oDialogView.findViewById(R.id.height);
        heightLayout.setVisibility(View.VISIBLE);

        if(null != rectangle) { /* Updating an existing feature properties */
            EditText widthCtrl = (EditText) oDialogView.findViewById(R.id.widthValue);
            widthCtrl.setText(String.valueOf(rectangle.getWidth()));
            EditText heightCtrl = (EditText) oDialogView.findViewById(R.id.heightValue);
            heightCtrl.setText(String.valueOf(rectangle.getHeight()));
            EditText azimuthCtrl = (EditText) oDialogView.findViewById(R.id.azimuthValue);
            azimuthCtrl.setText(String.valueOf(rectangle.getAzimuth()));
        }
    }

    public double getWidthValue() {
        EditText widthCtrl = (EditText) oDialogView.findViewById(R.id.widthValue);
        return Double.parseDouble(widthCtrl.getText().toString());
    }

    public double getHeightValue() {
        EditText heightCtrl = (EditText) oDialogView.findViewById(R.id.heightValue);
        return Double.parseDouble(heightCtrl.getText().toString());
    }
}
