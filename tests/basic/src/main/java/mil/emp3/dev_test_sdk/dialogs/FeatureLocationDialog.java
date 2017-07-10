package mil.emp3.dev_test_sdk.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import org.cmapi.primitives.GeoPositionGroup;
import org.cmapi.primitives.IGeoIconStyle;
import org.cmapi.primitives.IGeoMilSymbol;
import org.cmapi.primitives.IGeoPosition;
import org.cmapi.primitives.IGeoPositionGroup;

import mil.emp3.api.MilStdSymbol;
import mil.emp3.api.Point;
import mil.emp3.api.enums.FeatureTypeEnum;
import mil.emp3.api.interfaces.IFeature;
import mil.emp3.dev_test_sdk.R;

/**
 * This is a modeless dialog box that displays a features position.
 */
public class FeatureLocationDialog extends DialogFragment {

    private IFeature oFeature;
    private View oDialogView;
    private TextView oFeatureNameField;
    private TextView oFeaturePositionsField;
    private TextView oFeatureDescriptionField;
    private int iCurrentX;
    private int iCurrentY;

    public FeatureLocationDialog() {
        super();
    }

    public void setFeature(IFeature feature) {
        this.oFeature = feature;
    }

    public void updateDialog() {
        IGeoPosition pos;
        java.util.List<IGeoPosition> posList = this.oFeature.getPositions();
        String sDesc = "";

        for(int index = 0; index < posList.size(); index++) {
            pos = posList.get(index);
            if (sDesc.length() > 0) {
                sDesc += "\n";
            }
            sDesc += "[" + index + "] Lat:" + String.format("%7.5f°", pos.getLatitude()) +
                    " Lon:" + String.format("%7.5f°", pos.getLongitude()) +
                    " Alt:" + String.format("%,.0f m", pos.getAltitude());
        }
        this.oFeaturePositionsField.setText(sDesc);

        switch (this.oFeature.getFeatureType()) {
            case GEO_MIL_SYMBOL:
                MilStdSymbol oSymbol = (MilStdSymbol) this.oFeature;
                java.util.HashMap<IGeoMilSymbol.Modifier, String> oModifierList = oSymbol.getModifiers();

                sDesc = "";
                for (IGeoMilSymbol.Modifier eModifier: oModifierList.keySet()) {
                    sDesc += eModifier.name() + ":";
                    sDesc += oModifierList.get(eModifier);
                    sDesc += "\n";
                }

                this.oFeatureDescriptionField.setText(sDesc);
                break;
            case GEO_POINT:
                Point oPoint = (Point) this.oFeature;
                String sURL = oPoint.getIconURI();
                IGeoIconStyle oStyle = oPoint.getIconStyle();

                sDesc = "";
                if ((sURL != null) && (sURL.length() > 0)) {
                    sDesc += "URL: " + sURL + "\n";
                } else {
                    sDesc += "Default Icon." + "\n";
                }

                if (oStyle != null) {
                    sDesc += "Offset X:" + oStyle.getOffSetX() + "\n";
                    sDesc += "Offset Y:" + oStyle.getOffSetY() + "\n";
                }
                this.oFeatureDescriptionField.setText(sDesc);
                break;
        }
    }
/*
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Get the layout inflater
        final LayoutInflater inflater = getActivity().getLayoutInflater();
        oDialogView = inflater.inflate(R.layout.feature_location_dialog, null);

        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        builder.setView(oDialogView);

        this.updateDialog();

        builder.setTitle(this.oFeature.getName());

        //this.setStyle(DialogFragment.STYLE_NO_INPUT, 0);
        Dialog oDialog =  builder.create();

        Window window = oDialog.getWindow();
        window.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL);
        window.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        //window.setGravity(Gravity.LEFT);

        return oDialog;
    }
*/
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog oDialog = super.onCreateDialog(savedInstanceState);

        //oDialog.setTitle(this.oFeature.getName());
        Window window = oDialog.getWindow();
        //window.setTitle(this.oFeature.getName());
        window.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL);
        window.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        window.setGravity(Gravity.LEFT);

        return oDialog;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        oDialogView = inflater.inflate(R.layout.feature_location_dialog, null);
        this.oFeatureNameField = (TextView) oDialogView.findViewById(R.id.symbolname);
        this.oFeaturePositionsField = (TextView) oDialogView.findViewById(R.id.symbolPosition);
        this.oFeatureDescriptionField = (TextView) oDialogView.findViewById(R.id.symbolDescription);

        switch (this.oFeature.getFeatureType()) {
            case GEO_MIL_SYMBOL:
                this.oFeatureNameField.setText(this.oFeature.getName() + " (MilStd)");
                break;
            case GEO_POINT:
                this.oFeatureNameField.setText(this.oFeature.getName() + " (Point)");
                break;
            case GEO_PATH:
                this.oFeatureNameField.setText(this.oFeature.getName() + " (Path)");
                break;
            case GEO_POLYGON:
                this.oFeatureNameField.setText(this.oFeature.getName() + " (Polygon)");
                break;
            default:
                break;
        }
        this.oFeatureNameField.setTypeface(Typeface.DEFAULT_BOLD, 0);

        oDialogView.setOnTouchListener(new View.OnTouchListener() {
            private int dx = 0;
            private int dy = 0;

            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                switch (motionEvent.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        dx = FeatureLocationDialog.this.iCurrentX - (int) motionEvent.getRawX();
                        dy = FeatureLocationDialog.this.iCurrentY - (int) motionEvent.getRawY();
                        break;
                    case MotionEvent.ACTION_MOVE:
                        FeatureLocationDialog.this.iCurrentX = (int) (motionEvent.getRawX() + dx);
                        FeatureLocationDialog.this.iCurrentY = (int) (motionEvent.getRawY() + dy);
                        Dialog oDialog =  FeatureLocationDialog.this.getDialog();
                        //oDialog.setTitle(this.oFeature.getName());
                        Window window = oDialog.getWindow();
                        WindowManager.LayoutParams params = window.getAttributes();
                        params.x = FeatureLocationDialog.this.iCurrentX;
                        params.y = FeatureLocationDialog.this.iCurrentY;
                        window.setAttributes(params);
                        break;
                }
                return true;
            }
        });

        this.updateDialog();
        return oDialogView;
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog oDialog =  this.getDialog();
        //oDialog.setTitle(this.oFeature.getName());
        Window window = oDialog.getWindow();
        Resources oResource = getActivity().getResources();
        int pixels = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 320, oResource.getDisplayMetrics());
        WindowManager.LayoutParams params = window.getAttributes();
        params.width = pixels;
        //params.setTitle(this.oFeature.getName());

        window.setAttributes(params);
        FeatureLocationDialog.this.iCurrentX = params.x;
        FeatureLocationDialog.this.iCurrentY = params.y;

        this.updateDialog();
    }
}
