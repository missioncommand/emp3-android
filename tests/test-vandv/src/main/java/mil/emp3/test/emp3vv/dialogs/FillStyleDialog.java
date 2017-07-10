package mil.emp3.test.emp3vv.dialogs;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import org.cmapi.primitives.GeoFillStyle;
import org.cmapi.primitives.IGeoFillStyle;

import mil.emp3.api.interfaces.IMap;
import mil.emp3.test.emp3vv.R;
import mil.emp3.test.emp3vv.common.Emp3TesterDialogBase;
import mil.emp3.test.emp3vv.dialogs.utils.ColorComponents;
import mil.emp3.test.emp3vv.dialogs.utils.EnumerationSelection;

public class FillStyleDialog extends Emp3TesterDialogBase {
    private static String TAG = FillStyleDialog.class.getSimpleName();
    ColorComponents fillColor = new ColorComponents();
    EnumerationSelection<IGeoFillStyle.FillPattern> fillPattern;

    public interface IFillStyleDialogListener extends IEmp3TesterDialogBaseListener {
        void set(FillStyleDialog fillStyleDialog);
    }

    public FillStyleDialog() {
        // Empty constructor is required for DialogFragment
        // Make sure not to add arguments to the constructor
        // Use `newInstance` instead as shown below
    }

    public static FillStyleDialog newInstance(String title, IFillStyleDialogListener listener, IMap map, IGeoFillStyle style) {
        FillStyleDialog frag = new FillStyleDialog();
        Bundle args = new Bundle();
        args.putString("title", title);
        frag.setArguments(args);
        frag.init(map, listener);
        frag.initStyle(style);
        return frag;
    }

    public static FillStyleDialog newInstanceForOptItem(String title, IFillStyleDialogListener listener, IMap map, IGeoFillStyle style) {
        FillStyleDialog frag = new FillStyleDialog();
        Bundle args = new Bundle();
        args.putString("title", title);
        frag.setArguments(args);
        frag.initForOptItem(map, listener);
        frag.initStyle(style);
        return frag;
    }

    private void initStyle(IGeoFillStyle style) {
        if(null != style) {
            if(null != style.getFillColor()) {
                fillColor.initColors(style.getFillColor());
            }
            if(null != style.getFillPattern()) {
                fillPattern = new EnumerationSelection<>(IGeoFillStyle.FillPattern.class, style.getFillPattern());
            }
        }
    }
    public IGeoFillStyle getFillStyle() {
        IGeoFillStyle fs = new GeoFillStyle();
        fs.setFillColor(fillColor.getColor());
        fs.setFillPattern(fillPattern.getValue());
        return fs;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fill_style_dialog, container);
        setDialogPosition();
        return v;
    }

    @Override
    public void onViewCreated(final View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        View fillStyleColor = view.findViewById(R.id.fill_style_color);
        fillColor.onViewCreated(fillStyleColor, savedInstanceState);
        View fillStylePattern = view.findViewById(R.id.fill_style_pattern);
        if(null == fillPattern) {
            fillPattern = new EnumerationSelection<>(IGeoFillStyle.FillPattern.class, IGeoFillStyle.FillPattern.crossHatched);
        }
        fillPattern.onViewCreated(getActivity(), fillStylePattern, savedInstanceState);

        Button applyButton = (Button) view.findViewById(R.id.fill_style_apply_done).findViewById(R.id.apply);
        applyButton.setOnClickListener(v -> {
            if (null != FillStyleDialog.this.listener) {
                ((IFillStyleDialogListener)FillStyleDialog.this.listener).set(FillStyleDialog.this);
            }
        });
        Button doneButton = (Button) view.findViewById(R.id.fill_style_apply_done).findViewById(R.id.done);
        doneButton.setOnClickListener(v -> FillStyleDialog.this.dismiss());
    }
}
