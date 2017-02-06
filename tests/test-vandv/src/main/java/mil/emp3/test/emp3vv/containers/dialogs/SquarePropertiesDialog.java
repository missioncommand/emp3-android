package mil.emp3.test.emp3vv.containers.dialogs;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;

import java.util.List;

import mil.emp3.api.Square;
import mil.emp3.api.interfaces.IMap;
import mil.emp3.test.emp3vv.R;

public class SquarePropertiesDialog extends FeaturePropertiesDialog<SquarePropertiesDialog> {
    private Square square;
    public static SquarePropertiesDialog newInstance(String title, IMap map, List<String> parentList,
                                                        String featureName, boolean visible,
                                                        FeaturePropertiesDialog.FeaturePropertiesDialogListener<SquarePropertiesDialog> listener) {
        SquarePropertiesDialog frag = new SquarePropertiesDialog();
        frag.init(title, map, parentList, featureName, visible, listener);
        return frag;
    }

    public static SquarePropertiesDialog newInstanceForOpt(String title, IMap map, List<String> parentList,
                                                     String featureName, boolean visible,
                                                     FeaturePropertiesDialog.FeaturePropertiesDialogListener<SquarePropertiesDialog> listener,
                                                           Square square) {
        SquarePropertiesDialog frag = new SquarePropertiesDialog();
        frag.initForOptItem(title, map, parentList, featureName, visible, listener, square);
        frag.square = square;
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

        if(null != square) { /* Updating an existing feature properties */
            EditText widthCtrl = (EditText) oDialogView.findViewById(R.id.widthValue);
            widthCtrl.setText(String.valueOf(square.getWidth()));
            EditText azimuthCtrl = (EditText) oDialogView.findViewById(R.id.azimuthValue);
            azimuthCtrl.setText(String.valueOf(square.getAzimuth()));
        }
    }

    public double getWidthValue() {
        EditText widthCtrl = (EditText) oDialogView.findViewById(R.id.widthValue);
        return Double.parseDouble(widthCtrl.getText().toString());
    }
}