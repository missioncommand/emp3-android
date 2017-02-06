package mil.emp3.validator.dialogs.milstdtacticalgraphics;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;

import org.cmapi.primitives.GeoMilSymbol;

import armyc2.c2sd.renderer.utilities.SymbolDef;
import armyc2.c2sd.renderer.utilities.SymbolUtilities;
import mil.emp3.api.MilStdSymbol;
import mil.emp3.validator.R;
import mil.emp3.validator.dialogs.utils.SpinnerWithIconAdapter;
import mil.emp3.validator.utils.StringUtils;

/**
 * This class implements the TG properties dialog box.
 */
public class TacticalGraphicPropertiesDialog extends DialogFragment {
    private final static String TAG = TacticalGraphicPropertiesDialog.class.getSimpleName();

    public static final String sDropDownFirstOption = " ";
    public static final String sDropDownBackOption = "back";
    private String sFeatureName = "";
    private GeoMilSymbol.SymbolStandard eMilStdVersion = GeoMilSymbol.SymbolStandard.MIL_STD_2525B;
    private MilStdSymbol.Affiliation eAffiliation = MilStdSymbol.Affiliation.FRIEND;
    private String sSymbolCode = null;

    private static final java.util.HashMap<GeoMilSymbol.SymbolStandard, TacticalGraphicSymbolTable> oSymbolTableList =
            new java.util.HashMap<>();

    private TacticalGraphicSymbolTable oSymbolTable;

    private final java.util.HashMap<String, MilStdSymbol.Affiliation> oAffiliationHash = new java.util.HashMap<>();
    private final java.util.List<String> oAffiliationList = new java.util.ArrayList<>();


    private TacticalGraphicDefTreeItem oCurrentDefItem;
    private final java.util.List<TacticalGraphicSpinnerItem> oSymbolSpinnerList = new java.util.ArrayList<>();
    private final java.util.HashMap<Integer, TacticalGraphicDefTreeItem> oSymbolItemMap = new java.util.HashMap<>();
    private int iCurrentSymbolItem;

    private boolean bProcessSymbolChange = false;

    private View oDialogView;

    /* The activity that creates an instance of this dialog fragment must
     * implement this interface in order to receive event callbacks.
     * Each method passes the DialogFragment in case the host needs to query it. */
    public interface SymbolPropertiesDialogListener {
        void onSaveClick(TacticalGraphicPropertiesDialog dialog);
        void onCancelClick(TacticalGraphicPropertiesDialog dialog);
    }

    // Use this instance of the interface to deliver action events
    private SymbolPropertiesDialogListener mListener;

    public TacticalGraphicPropertiesDialog() {
        super();
        String tempString;
        this.oSymbolTable = TacticalGraphicPropertiesDialog.oSymbolTableList.get(GeoMilSymbol.SymbolStandard.MIL_STD_2525B);

        for (MilStdSymbol.Affiliation affiliation: MilStdSymbol.Affiliation.values()) {
            tempString = StringUtils.capitalizeWords(affiliation.name().replace('_', ' '));
            oAffiliationHash.put(tempString, affiliation);
            oAffiliationList.add(tempString);
        }
    }

    public static void loadSymbolTables() {
        TacticalGraphicPropertiesDialog.oSymbolTableList.put(GeoMilSymbol.SymbolStandard.MIL_STD_2525B, new TacticalGraphicSymbolTable(GeoMilSymbol.SymbolStandard.MIL_STD_2525B));
        TacticalGraphicPropertiesDialog.oSymbolTableList.put(GeoMilSymbol.SymbolStandard.MIL_STD_2525C, new TacticalGraphicSymbolTable(GeoMilSymbol.SymbolStandard.MIL_STD_2525C));
    }

    public void setSymbolCode(String sValue) {
        if (sValue != null) {
            this.sSymbolCode = sValue;
            String sTemp = String.valueOf(SymbolUtilities.getAffiliation(sValue));
            this.eAffiliation = MilStdSymbol.Affiliation.fromString(sTemp);
        }
    }

    public String getSymbolCode() {
        // sSymCode = armyc2.c2sd.renderer.utilities.SymbolUtilities.setAffiliation(this.sSymbolCode, this.eAffiliation.toString());

        //sSymCode = armyc2.c2sd.renderer.utilities.SymbolUtilities.setEchelon(sSymCode, this.eMilStdEchelon.toString());
        return this.sSymbolCode;
    }

    public void setFeatureName(String sValue) {
        if (sValue != null) {
            this.sFeatureName = sValue;
        }
    }

    public String getFeatureName() {
        return this.sFeatureName;
    }

    public void setMilStdVersion(GeoMilSymbol.SymbolStandard eVersion) {
        if (eVersion != null) {
            this.eMilStdVersion = eVersion;
        }
    }

    public GeoMilSymbol.SymbolStandard getMilStdVersion() {
        return this.eMilStdVersion;
    }

    public SymbolDef getCurrentDef() {
        return this.oCurrentDefItem.getSymbolDef();
    }

    // Override the Fragment.onAttach() method to instantiate the NoticeDialogListener
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the SymbolPropertiesDialogListener so we can send events to the host
            mListener = (SymbolPropertiesDialogListener) activity;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(activity.toString()
                    + " must implement SymbolPropertiesDialogListener");
        }
    }

    private void setSymbolCodeList() {
        TacticalGraphicDefTreeItem oDefItem;

        //this.oUnitSymbolTable.loadUnitDef(this.eMilStdVersion);
        if (this.sSymbolCode == null) {
            this.oCurrentDefItem = this.oSymbolTable.getRootSymbolDefItem();
        } else {
            this.oCurrentDefItem = this.oSymbolTable.getSymbolDefItem(this.sSymbolCode);
        }
    }

    private void loadSymbolCodeList() {
        int iIndex = 0;
        SymbolDef oDef;
        TacticalGraphicDefTreeItem oTreeItem;
        TacticalGraphicSpinnerItem oDefSpinnerItem;
        TacticalGraphicDefTreeItem oParentTreeItem = this.oCurrentDefItem.getParent();

        if (this.oCurrentDefItem ==  null) {
            Log.e(TAG, "oCurrentDefItem is null");
            return;
        }

        this.oSymbolSpinnerList.clear();
        this.oSymbolItemMap.clear();

        oDef = new SymbolDef("***************",
                TacticalGraphicPropertiesDialog.sDropDownFirstOption,
                SymbolDef.DRAW_CATEGORY_UNKNOWN, "", 0, 0, "", "");
        oTreeItem = new TacticalGraphicDefTreeItem(oDef);
        oDefSpinnerItem = new TacticalGraphicSpinnerItem(oTreeItem, -1);
        this.oSymbolSpinnerList.add(oDefSpinnerItem);

        this.oSymbolItemMap.put(iIndex, null);
        iIndex++;

        if (oParentTreeItem != null) {
            oDef = new SymbolDef("***************",
                    oParentTreeItem.getSymbolDef().getDescription(),
                    //TacticalGraphicPropertiesDialog.sDropDownBackOption,
                    SymbolDef.DRAW_CATEGORY_UNKNOWN, "", 0, 0, "", "");
            oTreeItem = new TacticalGraphicDefTreeItem(oDef);
            oDefSpinnerItem = new TacticalGraphicSpinnerItem(oTreeItem, android.R.drawable.arrow_up_float);
            this.oSymbolSpinnerList.add(oDefSpinnerItem);
            this.oSymbolItemMap.put(iIndex, oParentTreeItem);
            iIndex++;
        }

        if ((this.oCurrentDefItem.getSymbolDef().getDrawCategory() != SymbolDef.DRAW_CATEGORY_UNKNOWN) &&
                (this.oCurrentDefItem.getSymbolDef().getDrawCategory() != SymbolDef.DRAW_CATEGORY_3D_AIRSPACE) &&
                (this.oCurrentDefItem.getSymbolDef().getDrawCategory() != SymbolDef.DRAW_CATEGORY_DONOTDRAW)){
            oTreeItem = new TacticalGraphicDefTreeItem(this.oCurrentDefItem.getSymbolDef());
            oDefSpinnerItem = new TacticalGraphicSpinnerItem(oTreeItem, -1);
            this.oSymbolSpinnerList.add(oDefSpinnerItem);
            this.oSymbolItemMap.put(iIndex, oTreeItem); //this.oCurrentDefTreeItem);
            if (this.sSymbolCode != null) {
                this.iCurrentSymbolItem = iIndex;
            }
            iIndex++;
        }

        for (TacticalGraphicDefTreeItem oDefItem: this.oCurrentDefItem.getChildrenMap().values()) {
            oDefSpinnerItem = new TacticalGraphicSpinnerItem(oDefItem,
                    (oDefItem.hasChildren()? android.R.drawable.arrow_down_float: -1));
            this.oSymbolSpinnerList.add(oDefSpinnerItem);
            this.oSymbolItemMap.put(iIndex, oDefItem);
            iIndex++;
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Get the layout inflater
        final LayoutInflater inflater = getActivity().getLayoutInflater();
        oDialogView = inflater.inflate(R.layout.tactical_graphic_properties_dialog, null);

        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        builder.setView(oDialogView);

        final TextView oSymbolCode = (TextView) oDialogView.findViewById(R.id.symbolcodetextField);
        oSymbolCode.setText(this.sSymbolCode);

        EditText oFeatureName = (EditText) oDialogView.findViewById(R.id.featurename);
        oFeatureName.setText(this.sFeatureName);

        // Set the default MilStd version
        RadioGroup oRBGroup = (RadioGroup) oDialogView.findViewById(R.id.milstdversionRB);
        switch (eMilStdVersion) {
            case MIL_STD_2525B:
                oRBGroup.check(R.id.milstd2525B);
                break;
            case MIL_STD_2525C:
                oRBGroup.check(R.id.milstd2525C);
                break;
        }

        oRBGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener(){
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                // Check which radio button was clicked
                switch(checkedId) {
                    case R.id.milstd2525B:
                        TacticalGraphicPropertiesDialog.this.eMilStdVersion = GeoMilSymbol.SymbolStandard.MIL_STD_2525B;
                        break;
                    case R.id.milstd2525C:
                        TacticalGraphicPropertiesDialog.this.eMilStdVersion = GeoMilSymbol.SymbolStandard.MIL_STD_2525C;
                        break;
                    default:
                        TacticalGraphicPropertiesDialog.this.eMilStdVersion = GeoMilSymbol.SymbolStandard.MIL_STD_2525B;
                        break;
                }
                TacticalGraphicPropertiesDialog.this.oSymbolTable = TacticalGraphicPropertiesDialog.oSymbolTableList.get(TacticalGraphicPropertiesDialog.this.eMilStdVersion);
                TacticalGraphicPropertiesDialog.this.sSymbolCode = null;
                oSymbolCode.setText("");
                TacticalGraphicPropertiesDialog.this.setSymbolCodeList();
                TacticalGraphicPropertiesDialog.this.loadSymbolCodeList();
            }
        });

        Spinner oAffiliation = (Spinner) oDialogView.findViewById(R.id.feature_affiliation);
        ArrayAdapter oAffiliationStringAdapter = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            oAffiliationStringAdapter = new ArrayAdapter<>(this.getContext(), android.R.layout.simple_spinner_dropdown_item, oAffiliationList);
        } else {
            oAffiliationStringAdapter = new ArrayAdapter<>(this.getActivity(), android.R.layout.simple_spinner_dropdown_item, oAffiliationList);
        }
        oAffiliation.setAdapter(oAffiliationStringAdapter);

        if (this.eAffiliation != null) {
            int iIndex = this.oAffiliationList.indexOf(StringUtils.capitalizeWords(this.eAffiliation.name().replace('_', ' ')));

            if (iIndex != -1) {
                oAffiliation.setSelection(iIndex);
            }
        }

        oAffiliation.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String sValue = (String) parent.getItemAtPosition(position);

                TacticalGraphicPropertiesDialog.this.eAffiliation = TacticalGraphicPropertiesDialog.this.oAffiliationHash.get(sValue);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        this.setSymbolCodeList();
        this.loadSymbolCodeList();
        this.bProcessSymbolChange = false;
        final Spinner oSymbolSpinner = (Spinner) oDialogView.findViewById(R.id.feature_symbolcode);
        SpinnerWithIconAdapter oSymbolCodeStringAdapter;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            oSymbolCodeStringAdapter = new SpinnerWithIconAdapter(inflater, this.getContext(), android.R.layout.simple_spinner_dropdown_item, this.oSymbolSpinnerList);
        } else {
            oSymbolCodeStringAdapter = new SpinnerWithIconAdapter(inflater, this.getActivity(), android.R.layout.simple_spinner_dropdown_item, this.oSymbolSpinnerList);
        }
        oSymbolSpinner.setAdapter(oSymbolCodeStringAdapter);

        if (this.sSymbolCode != null) {
            oSymbolSpinner.setSelection(this.iCurrentSymbolItem);
        }

        oSymbolSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                SpinnerWithIconAdapter oNewStringAdapter;

                if (!TacticalGraphicPropertiesDialog.this.bProcessSymbolChange) {
                    TacticalGraphicPropertiesDialog.this.bProcessSymbolChange = true;
                    return;
                }

                TacticalGraphicDefTreeItem oDefItem = TacticalGraphicPropertiesDialog.this.oSymbolItemMap.get(position);

                if (oDefItem != null) {
                    TacticalGraphicPropertiesDialog.this.sSymbolCode = oDefItem.getSymbolDef().getBasicSymbolId();
                    oSymbolCode.setText(TacticalGraphicPropertiesDialog.this.sSymbolCode);

                    if (oDefItem != TacticalGraphicPropertiesDialog.this.oCurrentDefItem) {
                        if (oDefItem.hasChildren()) {
                            TacticalGraphicPropertiesDialog.this.oCurrentDefItem = oDefItem;
                            TacticalGraphicPropertiesDialog.this.loadSymbolCodeList();
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                oNewStringAdapter = new SpinnerWithIconAdapter(inflater, TacticalGraphicPropertiesDialog.this.getContext(), android.R.layout.simple_spinner_dropdown_item, TacticalGraphicPropertiesDialog.this.oSymbolSpinnerList);
                            } else {
                                oNewStringAdapter = new SpinnerWithIconAdapter(inflater, TacticalGraphicPropertiesDialog.this.getActivity(), android.R.layout.simple_spinner_dropdown_item, TacticalGraphicPropertiesDialog.this.oSymbolSpinnerList);
                            }
                            oSymbolSpinner.setAdapter(oNewStringAdapter);
                            oSymbolSpinner.performClick();
                            TacticalGraphicPropertiesDialog.this.bProcessSymbolChange = false;
                        }
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        // Add action buttons
        builder.setPositiveButton(R.string.saveBtn, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                EditText mEdit = (EditText) TacticalGraphicPropertiesDialog.this.oDialogView.findViewById(R.id.featurename);
                TacticalGraphicPropertiesDialog.this.sFeatureName = mEdit.getText().toString();
                TacticalGraphicPropertiesDialog.this.sSymbolCode = SymbolUtilities.setAffiliation(TacticalGraphicPropertiesDialog.this.sSymbolCode,
                        TacticalGraphicPropertiesDialog.this.eAffiliation.toString());

                mListener.onSaveClick(TacticalGraphicPropertiesDialog.this);
            }
        });

        builder.setNegativeButton(R.string.cancelBtn, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                mListener.onCancelClick(TacticalGraphicPropertiesDialog.this);
                //FeaturePropertiesDialog.this.getDialog().cancel();
            }
        });

        builder.setTitle(R.string.FPD_title);

        return builder.create();
    }

    private void updateSymbolCode() {
        if (this.sSymbolCode == null) {
            return;
        }

        this.sSymbolCode = SymbolUtilities.setAffiliation(this.sSymbolCode, this.eAffiliation.toString());
    }
}
