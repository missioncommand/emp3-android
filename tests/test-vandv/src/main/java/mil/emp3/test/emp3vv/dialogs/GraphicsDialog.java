package mil.emp3.test.emp3vv.dialogs;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import mil.emp3.api.enums.FontSizeModifierEnum;
import mil.emp3.api.enums.IconSizeEnum;
import mil.emp3.api.enums.MilStdLabelSettingEnum;
import mil.emp3.api.exceptions.EMP_Exception;
import mil.emp3.api.interfaces.IMap;
import mil.emp3.test.emp3vv.R;
import mil.emp3.test.emp3vv.common.Emp3TesterDialogBase;
import mil.emp3.test.emp3vv.dialogs.utils.EnumerationSelection;

public class GraphicsDialog extends Emp3TesterDialogBase {
    private static String TAG = GraphicsDialog.class.getSimpleName();

    public interface IGraphicsDialogListener extends IEmp3TesterDialogBaseListener {
        void graphicsSet(GraphicsDialog cameraDialog);
    }
    
    EnumerationSelection<IconSizeEnum> iconSize;
    EnumerationSelection<MilStdLabelSettingEnum> milStdLabelSetting;
    EnumerationSelection<FontSizeModifierEnum> fontSizeModifier;

    public GraphicsDialog() {
        // Empty constructor is required for DialogFragment
        // Make sure not to add arguments to the constructor
        // Use `newInstance` instead as shown below
    }

    public static GraphicsDialog newInstance(String title, IGraphicsDialogListener listener, IMap map) {

        GraphicsDialog frag = new GraphicsDialog();
        Bundle args = new Bundle();
        args.putString("title", title);
        frag.setArguments(args);
        frag.init(map, listener);
        return frag;
    }

    public static GraphicsDialog newInstanceForOptItem(String title, IGraphicsDialogListener listener, IMap map) {

        GraphicsDialog frag = new GraphicsDialog();
        Bundle args = new Bundle();
        args.putString("title", title);
        frag.setArguments(args);
        frag.initForOptItem(map, listener);
        return frag;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.graphics_dialog, container);
        setDialogPosition();
        return v;
    }

    @Override
    public void onViewCreated(final View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        IconSizeEnum cIconSize = map.getIconSize() == null ? IconSizeEnum.MEDIUM : map.getIconSize();
        iconSize = new EnumerationSelection<>(IconSizeEnum.class, cIconSize, new IconSizeListener());
        iconSize.onViewCreated(getActivity(), view.findViewById(R.id.graphic_icon_size), savedInstanceState);

        MilStdLabelSettingEnum cMylStdLabelSetting = map.getMilStdLabels() == null ? MilStdLabelSettingEnum.ALL_LABELS : map.getMilStdLabels();
        milStdLabelSetting = new EnumerationSelection<>(MilStdLabelSettingEnum.class, cMylStdLabelSetting, new MilStdLabelSettingListener());
        milStdLabelSetting.onViewCreated(getActivity(), view.findViewById(R.id.graphic_mil_std_labels), savedInstanceState);

        FontSizeModifierEnum cFontSizeModifier = map.getFontSizeModifier() == null ? FontSizeModifierEnum.NORMAL : map.getFontSizeModifier();
        fontSizeModifier = new EnumerationSelection<>(FontSizeModifierEnum.class, cFontSizeModifier, new FontSizeModifierListener());
        fontSizeModifier.onViewCreated(getActivity(), view.findViewById(R.id.graphic_font_size_modifier), savedInstanceState);

        Button doneButton = (Button) view.findViewById(R.id.done);
        doneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GraphicsDialog.this.dismiss();
            }
        });
    }

    class IconSizeListener implements EnumerationSelection.IEnumerationSelectionListener<IconSizeEnum> {
        @Override
        public void onValueChanged(IconSizeEnum newValue) {
            try {
                getMap().setIconSize(newValue);
                ((GraphicsDialog.IGraphicsDialogListener)GraphicsDialog.this.listener).graphicsSet(GraphicsDialog.this);
            } catch (EMP_Exception e) {
                e.printStackTrace();
            }
        }
    }

    class MilStdLabelSettingListener implements EnumerationSelection.IEnumerationSelectionListener<MilStdLabelSettingEnum> {
        @Override
        public void onValueChanged(MilStdLabelSettingEnum newValue) {
            try {
                getMap().setMilStdLabels(newValue);
                ((GraphicsDialog.IGraphicsDialogListener)GraphicsDialog.this.listener).graphicsSet(GraphicsDialog.this);
            } catch (EMP_Exception e) {
                e.printStackTrace();
            }
        }
    }

    class FontSizeModifierListener implements EnumerationSelection.IEnumerationSelectionListener<FontSizeModifierEnum> {
        @Override
        public void onValueChanged(FontSizeModifierEnum newValue) {
            try {
                getMap().setFontSizeModifier(newValue);
                ((GraphicsDialog.IGraphicsDialogListener)GraphicsDialog.this.listener).graphicsSet(GraphicsDialog.this);
            } catch (EMP_Exception e) {
                e.printStackTrace();
            }
        }
    }
}
