package mil.emp3.test.emp3vv.dialogs;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;

import org.cmapi.primitives.GeoStrokeStyle;
import org.cmapi.primitives.IGeoColor;
import org.cmapi.primitives.IGeoStrokeStyle;

import mil.emp3.api.interfaces.IMap;
import mil.emp3.test.emp3vv.R;
import mil.emp3.test.emp3vv.common.Emp3TesterDialogBase;
import mil.emp3.test.emp3vv.dialogs.utils.ColorComponents;
import mil.emp3.test.emp3vv.dialogs.utils.IncrementDecrement;

public class StrokeStyleDialog extends Emp3TesterDialogBase {
    private static String TAG = StrokeStyleDialog.class.getSimpleName();

    ColorComponents colorComponents = new ColorComponents();
    short stipplingPattern = 0B0101010101010101;
    IncrementDecrement<Integer> stipplingFactorIncDec;
    int stipplingFactor = 1;
    IncrementDecrement<Double> strokeWidthIncDec;
    double strokeWidth = 5.0;

    public StrokeStyleDialog() {
        // Empty constructor is required for DialogFragment
        // Make sure not to add arguments to the constructor
        // Use `newInstance` instead as shown below
    }

    public static StrokeStyleDialog newInstance(String title, IStrokeStyleDialogListener listener, IMap map, IGeoStrokeStyle style) {
        StrokeStyleDialog frag = new StrokeStyleDialog();
        Bundle args = new Bundle();
        args.putString("title", title);
        frag.setArguments(args);
        frag.init(map, listener);
        frag.initStyle(style);
        return frag;
    }

    public static StrokeStyleDialog newInstanceForOptItem(String title, IStrokeStyleDialogListener listener, IMap map, IGeoStrokeStyle style) {
        StrokeStyleDialog frag = new StrokeStyleDialog();
        Bundle args = new Bundle();
        args.putString("title", title);
        frag.setArguments(args);
        frag.initForOptItem(map, listener);
        frag.initStyle(style);
        return frag;
    }

    private void initStyle(IGeoStrokeStyle style) {
        if(null != style) {
            if(null != style.getStrokeColor()) {
                colorComponents.initColors(style.getStrokeColor());
            }

            stipplingPattern = style.getStipplingPattern();
            stipplingFactor = style.getStipplingFactor();
            stipplingFactorIncDec = new IncrementDecrement<>(stipplingFactor, 1,  Integer.MAX_VALUE, 1, 1);
            strokeWidth = style.getStrokeWidth();
            strokeWidthIncDec = new IncrementDecrement<>(strokeWidth, 1.0, 10.0, 1.0, 1.0);
        }
    }
    public IGeoStrokeStyle getStrokeStyle() {
        IGeoStrokeStyle strokeStyle = new GeoStrokeStyle();
        IGeoColor geoColor = colorComponents.getColor();
        strokeStyle.setStrokeColor(geoColor);

        if(null != strokeWidthIncDec) {
            strokeStyle.setStrokeWidth(strokeWidthIncDec.getValue());
        } else {
            strokeStyle.setStrokeWidth(strokeWidth);
        }

        if(null != stipplingFactorIncDec) {
            strokeStyle.setStipplingFactor((int)stipplingFactorIncDec.getValue());
        } else {
            strokeStyle.setStipplingFactor(stipplingFactor);
        }
        updateStipplePattern(getView());
        strokeStyle.setStipplingPattern(stipplingPattern);
        return strokeStyle;
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.stroke_style_dialog, container);
        setDialogPosition();
        return v;
    }

    @Override
    public void onViewCreated(final View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        colorComponents.onViewCreated(view, savedInstanceState);

        setupStipplePattern(view);

        if(null == stipplingFactorIncDec) {
            stipplingFactorIncDec = new IncrementDecrement<>(stipplingFactor, 1,  Integer.MAX_VALUE, 1, 1);
        }
        stipplingFactorIncDec.onViewCreated(view.findViewById(R.id.stroke_style_stippling_factor), savedInstanceState, false);

        // Setup width
        if(null == strokeWidthIncDec) {
            strokeWidthIncDec = new IncrementDecrement<>(strokeWidth, 1.0, 10.0, 1.0, 1.0);
        }
        strokeWidthIncDec.onViewCreated(view.findViewById(R.id.stroke_style_width), savedInstanceState, false);

        Button applyButton = (Button) view.findViewById(R.id.apply);
        applyButton.setOnClickListener(v -> {
            if (null != StrokeStyleDialog.this.listener) {
                ((IStrokeStyleDialogListener)StrokeStyleDialog.this.listener).setStrokeStyle(StrokeStyleDialog.this);
            }
        });
        Button doneButton = (Button) view.findViewById(R.id.done);
        doneButton.setOnClickListener(v -> StrokeStyleDialog.this.dismiss());
    }

    /**
     * Setup one of the check boxes representing one bit of the stroke pattern
     * @param view

     */
    private void setupStipplePattern(View view) {
        for(int bitNumber = 0; bitNumber <=15; bitNumber++) {
            String tag = "sp" + String.valueOf(bitNumber);
            CheckBox cb = (CheckBox) view.findViewWithTag(tag);
            if (null != cb) {
                int mask = 1;
                mask = mask << bitNumber;
                mask &= stipplingPattern;
                if (0 == mask) {
                    cb.setChecked(false);
                } else {
                    cb.setChecked(true);
                }
            }
        }
    }

    private void updateStipplePattern(View view) {
        stipplingPattern = 0;
        for(int bitNumber = 0; bitNumber <=15; bitNumber++) {
            String tag = "sp" + String.valueOf(bitNumber);
            CheckBox cb = (CheckBox) view.findViewWithTag(tag);
            if (null != cb) {
                if(cb.isChecked()) {
                    int mask = 1;
                    mask = mask << bitNumber;
                    stipplingPattern |= mask;
                }
            }
        }
    }

    public interface IStrokeStyleDialogListener extends IEmp3TesterDialogBaseListener {
        void setStrokeStyle(StrokeStyleDialog strokeStyleDialog);
    }
}
