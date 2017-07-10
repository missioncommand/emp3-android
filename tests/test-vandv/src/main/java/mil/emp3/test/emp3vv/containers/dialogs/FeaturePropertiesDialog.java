package mil.emp3.test.emp3vv.containers.dialogs;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.cmapi.primitives.IGeoPosition;

import java.util.List;

import mil.emp3.api.exceptions.EMP_Exception;
import mil.emp3.api.interfaces.IFeature;
import mil.emp3.api.interfaces.IMap;
import mil.emp3.test.emp3vv.R;
import mil.emp3.test.emp3vv.common.Emp3TesterDialogBase;
import mil.emp3.test.emp3vv.utils.PositionListUtility;
import mil.emp3.test.emp3vv.utils.PositionUtility;

/**
 * This class implements the base for feature property dialog boxes.
 */

public class FeaturePropertiesDialog<T extends FeaturePropertiesDialog> extends Emp3TesterDialogBase
        implements PositionUtility.IPositionChangedListener {
    private final static String TAG = FeaturePropertiesDialog.class.getSimpleName();

    private IFeature feature;
    public interface FeaturePropertiesDialogListener<T extends FeaturePropertiesDialog> {
        boolean onFeaturePropertiesSaveClick(T dialog);
        void onFeaturePropertiesCancelClick(T dialog);
    }

    protected String sFeatureName;
    protected FeaturePropertiesDialogListener<T> listener;
    protected List<String> parentList;
    protected boolean featureVisible;

    protected View oDialogView;
    protected boolean isFeaturePositionSettable = true;
    private PositionUtility positionUtility;
    EditText bufferValue;

    private PositionListUtility positionListUtility;

    public FeaturePropertiesDialog() {}

    private void initialize(String title, IMap map, List<String> parentList,
                       String featureName, boolean visible,
                       FeaturePropertiesDialogListener<T> listener, IFeature feature) {
        if (null == listener) {
            throw new IllegalArgumentException("listener and must be non-null");
        }

        if((null == parentList) || (0 == parentList.size())) {
            throw new IllegalArgumentException("parentList must be non-null and non empty");
        }

        Bundle args = new Bundle();

        args.putString("title", title);
        this.setArguments(args);
        this.parentList = parentList;
        this.featureVisible = visible;
        this.sFeatureName = featureName;
        this.listener = listener;
        this.feature = feature;
    }
    protected void init(String title, IMap map, List<String> parentList,
            String featureName, boolean visible,
            FeaturePropertiesDialogListener<T> listener) {

        initialize(title, map, parentList, featureName, visible, listener, null);
        super.init(map);
    }

    /**
     * This method is used when application wants to show this dialog from another dialog or application wants the ability to show
     * another dialog when this dialog is still active. It has one more parameter "feature' than the init method.
     * @param title
     * @param map
     * @param parentList
     * @param featureName
     * @param visible
     * @param listener
     * @param feature Current feature whose properties can be updated by the user.
     */
    protected void initForOptItem(String title, IMap map, List<String> parentList,
                        String featureName, boolean visible,
                        FeaturePropertiesDialogListener<T> listener, IFeature feature) {

        initialize(title, map, parentList, featureName, visible, listener, feature);
        super.initForOptItem(map);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        oDialogView = inflater.inflate(R.layout.feature_properties_dialog, container);
        setDialogPosition();
        return oDialogView;
    }

    @Override
    public void onViewCreated(View view, final Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TextView oFeatureName = (TextView) oDialogView.findViewById(R.id.featurename);
        oFeatureName.setText(this.sFeatureName);

        if(isBufferApplicable()) {
            view.findViewById(R.id.buffer_layout).setVisibility(View.VISIBLE);
            bufferValue = (EditText) view.findViewById(R.id.bufferValue);
            if((null != feature) && (feature.getBuffer() > 0.0)){
                bufferValue.setText(String.valueOf(feature.getBuffer()));
            } else {
                bufferValue.setText(String.valueOf(0.0));
            }

            Button bufferButton = (Button) view.findViewById(R.id.update_buffer);
            bufferButton.setVisibility(View.GONE);
        } else {
            view.findViewById(R.id.buffer_layout).setVisibility(View.GONE);
        }

        Button doneButton = (Button) view.findViewById(R.id.cancel);
        doneButton.setOnClickListener(v -> {
            FeaturePropertiesDialog.this.dismiss();
            FeaturePropertiesDialog.this.listener.onFeaturePropertiesCancelClick((T)FeaturePropertiesDialog.this);
            positionUtility.stop();
            positionListUtility.stop();
        });

        Button applyButton = (Button) view.findViewById(R.id.apply);
        applyButton.setOnClickListener(v -> {
            if (null != FeaturePropertiesDialog.this.listener) {
                if(FeaturePropertiesDialog.this.listener.onFeaturePropertiesSaveClick((T) FeaturePropertiesDialog.this)) {
                    FeaturePropertiesDialog.this.dismiss();
                    positionUtility.stop();
                    positionListUtility.stop();
                }
            } else {
                FeaturePropertiesDialog.this.dismiss();
                positionUtility.stop();
                positionListUtility.stop();
            }
        });

        try {
            positionUtility = new PositionUtility(map, this, isFeatureMultiPoint());
        } catch (EMP_Exception e) {
            Log.e(TAG, "positionUtility ", e);
        }

        // Allows user to type in positions. Once this button is selected, position utility is turned off, i.e.
        // Tapping on the map will have no consequences to position list.
        positionListUtility = new PositionListUtility(view, getMap());
        positionListUtility.onCreateView(getFragmentManager(), positionUtility);
    }

    protected boolean isFeatureMultiPoint() {
        return false;
    }
    public boolean isFeatureVisible() {
        return this.featureVisible;
    }
    protected boolean isBufferApplicable() { return false; }

    public List<String> getParentList() {
        return this.parentList;
    }

    public String getFeatureName() {
        TextView mEdit = (TextView) this.oDialogView.findViewById(R.id.featurename);
        return mEdit.getText().toString();
    }

    protected void activateAzimuth() {
        LinearLayout azimuth = (LinearLayout) oDialogView.findViewById(R.id.azimuth);
        azimuth.setVisibility(View.VISIBLE);
    }

    public double getAzimuthValue() {
        try {
            EditText azimuthCtrl = (EditText) oDialogView.findViewById(R.id.azimuthValue);
            return Double.parseDouble(azimuthCtrl.getText().toString());
        } catch (NumberFormatException ex) {
            return 0;
        }
    }

    public double getBufferValue() {
        if(null != bufferValue) {
            return Double.parseDouble(bufferValue.getText().toString());
        } else {
            return 0.0;
        }
    }

    public PositionUtility getPositionUtility() {
        return positionUtility;
    }

    @Override
    public void newPosition(IGeoPosition geoPosition, String stringPosition) {
        if (isFeaturePositionSettable) {
            TextView view = (TextView) getView().findViewById(R.id.position);
            view.setText(stringPosition);
        }
    }

    public void setPosition(IFeature feature) {
        if (isFeaturePositionSettable) {
            feature.getPositions().clear();
            feature.getPositions().addAll(positionUtility.getPositionList());
        }
    }
}
