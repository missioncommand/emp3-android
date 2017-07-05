package mil.emp3.test.emp3vv.dialogs;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import mil.emp3.api.interfaces.IMap;
import mil.emp3.test.emp3vv.R;
import mil.emp3.test.emp3vv.common.Emp3TesterDialogBase;
import mil.emp3.test.emp3vv.dialogs.utils.IncrementDecrement;

/**
 * Dialog to setup mid distance and far distance thresholds.
 */
public class DistanceThresholdsDialog extends Emp3TesterDialogBase {
    private static String TAG = DistanceThresholdsDialog.class.getSimpleName();
    IncrementDecrement<Double> fdt;
    IncrementDecrement<Double> mdt;

    final double minThreshold = 0.0;
    final double maxThreshold = Double.MAX_VALUE;

    class IncDecListener implements IncrementDecrement.IIncDecListener {
        String what;

        IncDecListener(String what) {
            this.what = what;
        }
        @Override
        public double onValueChanged(double newValue) {
            if(what.equals("MDT")) {
                getMap().setMidDistanceThreshold(newValue);
                return getMap().getMidDistanceThreshold();
            } else if(what.equals("FDT")) {
                getMap().setFarDistanceThreshold(newValue);
                return getMap().getFarDistanceThreshold();
            } else {
                Log.e(TAG, "Unknown callback " + what);
                throw new IllegalStateException("must be FDT or MDT");
            }
        }
    }

    public DistanceThresholdsDialog() {
        // Empty constructor is required for DialogFragment
        // Make sure not to add arguments to the constructor
        // Use `newInstance` instead as shown below
    }

    public static DistanceThresholdsDialog newInstance(String title, IDistanceThresholdsDialogListener listener, IMap map) {
        DistanceThresholdsDialog frag = new DistanceThresholdsDialog();
        Bundle args = new Bundle();
        args.putString("title", title);
        frag.setArguments(args);
        frag.init(map, listener);
        return frag;
    }

    public static DistanceThresholdsDialog newInstanceForOptItem(String title, IDistanceThresholdsDialogListener listener, IMap map) {
        DistanceThresholdsDialog frag = new DistanceThresholdsDialog();
        Bundle args = new Bundle();
        args.putString("title", title);
        frag.setArguments(args);
        frag.initForOptItem(map, listener);
        return frag;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.distance_thresholds_dialog, container);
        setDialogPosition();
        return v;
    }

    /**
     * This code was borrowed from test basic project.
     * @param view
     * @param savedInstanceState
     */
    @Override
    public void onViewCreated(final View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Increment by 1000 or 5000
        fdt = new IncrementDecrement<>(getMap().getFarDistanceThreshold(), minThreshold, maxThreshold, 1000.0, 5.0);
        fdt.onViewCreated(view.findViewById(R.id.far_distance_threshold), savedInstanceState, true, new IncDecListener("FDT"));

        mdt = new IncrementDecrement<>(getMap().getMidDistanceThreshold(), minThreshold, maxThreshold, 1000.0, 5.0);
        mdt.onViewCreated(view.findViewById(R.id.mid_distance_threshold), savedInstanceState, true, new IncDecListener("MDT"));

        Button doneButton = (Button) view.findViewById(R.id.done);
        doneButton.setOnClickListener(v -> DistanceThresholdsDialog.this.dismiss());
    }

    public interface IDistanceThresholdsDialogListener extends IEmp3TesterDialogBaseListener {
        void setFarDistanceThreshold(DistanceThresholdsDialog distanceThresholdDialog);
        void setMidDistanceThreshold(DistanceThresholdsDialog distanceThresholdDialog);
    }
}
