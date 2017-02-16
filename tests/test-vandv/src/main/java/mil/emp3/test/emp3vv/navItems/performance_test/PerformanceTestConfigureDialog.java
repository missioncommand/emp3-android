package mil.emp3.test.emp3vv.navItems.performance_test;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;

import mil.emp3.api.interfaces.IMap;
import mil.emp3.test.emp3vv.R;
import mil.emp3.test.emp3vv.common.Emp3TesterDialogBase;

import mil.emp3.test.emp3vv.dialogs.utils.IncrementDecrement;

/**
 * Performance Test configuration dialog,
 *     track count
 *     change affiliation or not
 *     batch update or not
 */
public class PerformanceTestConfigureDialog extends Emp3TesterDialogBase {
    private static String TAG = PerformanceTestConfigureDialog.class.getSimpleName();

    int defaultTrackCount = 10000;
    int minTrackCount = 100;
    int maxTrackCount = 10000;
    int trackCountResolution = 100;
    int trackCountFastMultiplier = 10;
    IncrementDecrement<Integer> trackCount;
    CheckBox changeAffiliation;
    CheckBox batchUpdates;
    PerformanceTestConfig initConfig;

    public interface IPerformanceTestConfigureDialogListener extends IEmp3TesterDialogBaseListener {
        void configure(PerformanceTestConfigureDialog dialog);
    }

    public PerformanceTestConfigureDialog() {
    }

    public static PerformanceTestConfigureDialog newInstance(String title, IMap map, IPerformanceTestConfigureDialogListener listener, PerformanceTestConfig config) {
        PerformanceTestConfigureDialog frag = new PerformanceTestConfigureDialog();
        Bundle args = new Bundle();
        args.putString("title", title);
        frag.setArguments(args);
        frag.init(map, listener);
        frag.initConfig = config;
        return frag;
    }

    public static PerformanceTestConfigureDialog newInstanceForOptItem(String title, IMap map, IPerformanceTestConfigureDialogListener listener, PerformanceTestConfig config) {
        PerformanceTestConfigureDialog frag = new PerformanceTestConfigureDialog();
        Bundle args = new Bundle();
        args.putString("title", title);
        frag.setArguments(args);
        frag.initForOptItem(map, listener);
        frag.initConfig = config;
        return frag;
    }

    private int getNumberofTracks() {
        if(null != trackCount) {
            return (int) trackCount.getValue();
        }
        return defaultTrackCount;
    }

    private boolean getChangeAffiliation() {
        if(null != changeAffiliation) {
            return changeAffiliation.isChecked();
        }
        return false;
    }

    private boolean getBatchUpdates() {
        if(null != batchUpdates) {
            return batchUpdates.isChecked();
        }
        return false;
    }

    public PerformanceTestConfig getConfig() {
        PerformanceTestConfig config = new PerformanceTestConfig();
        config.setTrackCount(getNumberofTracks());
        config.setChangeAffiliation(getChangeAffiliation());
        config.setBatchUpdates(getBatchUpdates());
        return config;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.performance_test_configure_dialog, container);
        setDialogPosition();
        return v;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        int initialTrackCount = defaultTrackCount;
        if(null != initConfig) {
            initialTrackCount = initConfig.getTrackCount();
        }
        trackCount = new IncrementDecrement<>(initialTrackCount, minTrackCount, maxTrackCount, trackCountResolution, trackCountFastMultiplier);
        trackCount.onViewCreated(view.findViewById(R.id.number_of_tracks), savedInstanceState, true);

        changeAffiliation = (CheckBox) view.findViewById(R.id.change_affiliation);
        batchUpdates = (CheckBox) view.findViewById(R.id.batch_updates);
        if(null != initConfig) {
            changeAffiliation.setChecked(initConfig.isChangeAffiliation());
            batchUpdates.setChecked(initConfig.isBatchUpdates());
        }

        Button applyButton = (Button) view.findViewById(R.id.configure_apply_done).findViewById(R.id.apply);
        applyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != PerformanceTestConfigureDialog.this.listener) {
                    ((IPerformanceTestConfigureDialogListener)PerformanceTestConfigureDialog.this.listener).configure(PerformanceTestConfigureDialog.this);
                }
            }
        });
        Button doneButton = (Button) view.findViewById(R.id.configure_apply_done).findViewById(R.id.done);
        doneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PerformanceTestConfigureDialog.this.dismiss();
            }
        });
    }
}
