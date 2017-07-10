package mil.emp3.test.emp3vv.dialogs;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import org.cmapi.primitives.GeoLabelStyle;
import org.cmapi.primitives.IGeoLabelStyle;

import mil.emp3.api.interfaces.IMap;
import mil.emp3.test.emp3vv.R;
import mil.emp3.test.emp3vv.common.Emp3TesterDialogBase;
import mil.emp3.test.emp3vv.dialogs.utils.ColorComponents;
import mil.emp3.test.emp3vv.dialogs.utils.EnumerationSelection;
import mil.emp3.test.emp3vv.dialogs.utils.IncrementDecrement;

public class LabelStyleDialog extends Emp3TesterDialogBase {
    private static String TAG = LabelStyleDialog.class.getSimpleName();

    ColorComponents colorComponents = new ColorComponents();
    ColorComponents outlineColorComponents = new ColorComponents();
    IncrementDecrement<Double> size;

    EnumerationSelection<IGeoLabelStyle.Justification> justification;
    EnumerationSelection<IGeoLabelStyle.Typeface> typeface;

    enum FontFamily {
        MONOSPACE, SERIF, SANS_SERIF
    };
    EnumerationSelection<FontFamily> fontFamily;

    public LabelStyleDialog() {
        // Empty constructor is required for DialogFragment
        // Make sure not to add arguments to the constructor
        // Use `newInstance` instead as shown below
    }

    public static LabelStyleDialog newInstance(String title, ILabelStyleDialogListener listener, IMap map, IGeoLabelStyle style) {
        LabelStyleDialog frag = new LabelStyleDialog();
        Bundle args = new Bundle();
        args.putString("title", title);
        frag.setArguments(args);
        frag.init(map, listener);
        frag.initStyle(style);
        return frag;
    }

    public static LabelStyleDialog newInstanceForOptItem(String title, ILabelStyleDialogListener listener, IMap map, IGeoLabelStyle style) {
        LabelStyleDialog frag = new LabelStyleDialog();
        Bundle args = new Bundle();
        args.putString("title", title);
        frag.setArguments(args);
        frag.initForOptItem(map, listener);
        frag.initStyle(style);
        return frag;
    }

    private void initStyle(IGeoLabelStyle style) {
        if(null != style) {
            if(null != style.getColor()) {
                colorComponents.initColors(style.getColor());
            }
            if(null != style.getOutlineColor()) {
                outlineColorComponents.initColors(style.getOutlineColor());
            }
            size = new IncrementDecrement<>(style.getSize(), 1.0, 32.0, 1.0, 1.0);
            if(null != style.getJustification()) {
                justification = new EnumerationSelection<>(IGeoLabelStyle.Justification.class, style.getJustification());
            }
            if(null != style.getTypeface()) {
                typeface = new EnumerationSelection<>(IGeoLabelStyle.Typeface.class, style.getTypeface());
            }
            if(null != style.getFontFamily()) {
                fontFamily = new EnumerationSelection<>(FontFamily.class, FontFamily.valueOf(style.getFontFamily().toUpperCase()));
            }
        }
    }
    public IGeoLabelStyle getLabelStyle() {
        IGeoLabelStyle labelStyle = new GeoLabelStyle();
        labelStyle.setColor(colorComponents.getColor());
        labelStyle.setOutlineColor(outlineColorComponents.getColor());
        if(null != size) {
            labelStyle.setSize(size.getValue());
        }
        labelStyle.setJustification(justification.getValue());
        labelStyle.setTypeface(typeface.getValue());
        String ff = fontFamily.getValue().toString().toLowerCase();
        if(ff.equalsIgnoreCase("sans_serif")) {
            ff = "sans-serif";
        }
        labelStyle.setFontFamily(ff);
        return labelStyle;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.label_style_dialog, container);
        setDialogPosition();
        return v;
    }

    @Override
    public void onViewCreated(final View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        View labelStyleColor = view.findViewById(R.id.label_style_color);
        colorComponents.onViewCreated(labelStyleColor, savedInstanceState);
        View labelStyleOutlineColor = view.findViewById(R.id.label_style_outline_color);
        outlineColorComponents.onViewCreated(labelStyleOutlineColor, savedInstanceState);

        IGeoLabelStyle defaultStyle = new GeoLabelStyle();
        if(null == size) {
            size = new IncrementDecrement<>(defaultStyle.getSize(), 1.0, 32.0, 1.0, 1.0);
        }
        size.onViewCreated(view.findViewById(R.id.label_style_size), savedInstanceState, false);

        if(null == justification) {
            justification = new EnumerationSelection<>(IGeoLabelStyle.Justification.class, IGeoLabelStyle.Justification.CENTER);
        }
        justification.onViewCreated(getActivity(), view.findViewById(R.id.label_style_justification), savedInstanceState);

        if(null == typeface) {
            typeface = new EnumerationSelection<>(IGeoLabelStyle.Typeface.class, IGeoLabelStyle.Typeface.REGULAR);
        }
        typeface.onViewCreated(getActivity(), view.findViewById(R.id.label_style_typeface), savedInstanceState);

        if(null == fontFamily) {
            fontFamily = new EnumerationSelection<>(FontFamily.class, FontFamily.MONOSPACE);
        }
        fontFamily.onViewCreated(getActivity(), view.findViewById(R.id.label_style_font_family), savedInstanceState);

        Button applyButton = (Button) view.findViewById(R.id.label_style_apply_done).findViewById(R.id.apply);
        applyButton.setOnClickListener(v -> {
            if (null != LabelStyleDialog.this.listener) {
                ((ILabelStyleDialogListener)LabelStyleDialog.this.listener).setLabelStyle(LabelStyleDialog.this);
            }
        });
        Button doneButton = (Button) view.findViewById(R.id.label_style_apply_done).findViewById(R.id.done);
        doneButton.setOnClickListener(v -> LabelStyleDialog.this.dismiss());
    }

    public interface ILabelStyleDialogListener extends IEmp3TesterDialogBaseListener {
        void setLabelStyle(LabelStyleDialog labelStyleDialog);
    }
}
