package mil.emp3.dev_test_sdk.dialogs.milstdunits;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;

import org.cmapi.primitives.GeoMilSymbol;
import org.cmapi.primitives.IGeoMilSymbol;

import java.util.List;

import armyc2.c2sd.renderer.utilities.UnitDef;
import mil.emp3.api.MilStdSymbol;
import mil.emp3.api.exceptions.EMP_Exception;
import mil.emp3.api.utils.ManagerFactory;
import mil.emp3.dev_test_sdk.R;
import mil.emp3.dev_test_sdk.dialogs.utils.SpinnerWithIconAdapter;
import mil.emp3.dev_test_sdk.utils.StringUtils;
import mil.emp3.dev_test_sdk.utils.SymbolCodeUtils;
import mil.emp3.mapengine.interfaces.IEmpImageInfo;
import mil.emp3.mapengine.interfaces.IMilStdRenderer;

/**
 * This class presents a dialog box to enter the feature properties.
 */
public class SymbolPropertiesDialog extends DialogFragment {
    private final static String TAG = SymbolPropertiesDialog.class.getSimpleName();

    final private IMilStdRenderer milStdRenderer = ManagerFactory.getInstance().getMilStdRenderer();

    public static final String sDropDownFirstOption = " ";
    public static final String sDropDownBackOption = "back";
    private String sFeatureName = "";
    private org.cmapi.primitives.GeoMilSymbol.SymbolStandard eMilStdVersion = GeoMilSymbol.SymbolStandard.MIL_STD_2525C;
    private MilStdSymbol.Affiliation eAffiliation = MilStdSymbol.Affiliation.FRIEND;
    private MilStdSymbol.EchelonSymbolModifier eMilStdEchelon = MilStdSymbol.EchelonSymbolModifier.UNIT;
    private int iEchelonIndex = 0;
    private String sSymbolCode = null;

    private static final java.util.HashMap<org.cmapi.primitives.GeoMilSymbol.SymbolStandard, UnitSymbolTable> oUnitSymbolTableList =
            new java.util.HashMap<>();
    
    private UnitSymbolTable oUnitSymbolTable;

    private final java.util.HashMap<String, MilStdSymbol.Affiliation> oAffiliationHash = new java.util.HashMap<>();
    private final java.util.List<String> oAffiliationList = new java.util.ArrayList<>();

    private final java.util.List<EchelonSymbolModifierSpinnerItem> oEchelonMainList = new java.util.ArrayList<>();
    private final java.util.List<EchelonSymbolModifierSpinnerItem> oEchelonUnitList = new java.util.ArrayList<>();
    private final java.util.List<EchelonSymbolModifierSpinnerItem> oEchelonInstallationList = new java.util.ArrayList<>();
    private final java.util.List<EchelonSymbolModifierSpinnerItem> oEchelonMobilityList = new java.util.ArrayList<>();
    private final java.util.List<EchelonSymbolModifierSpinnerItem> oEchelonTowedArrayList = new java.util.ArrayList<>();
    private java.util.List<EchelonSymbolModifierSpinnerItem> oEchelonCurrentList = null;
    
    private UnitSymbolDefTreeItem oCurrentUnitDefItem;
    private final java.util.List<UnitDefSpinnerItem> oSymbolSpinnerList = new java.util.ArrayList<>();
    private final java.util.HashMap<Integer, UnitSymbolDefTreeItem> oSymbolUnitItemMap = new java.util.HashMap<>();
    private int iCurrentSymbolItem;

    private boolean bProcessSymbolChange = false;

    private View oDialogView;
    
    /* The activity that creates an instance of this dialog fragment must
     * implement this interface in order to receive event callbacks.
     * Each method passes the DialogFragment in case the host needs to query it. */
    public interface SymbolPropertiesDialogListener {
        public void onSymbolPropertiesSaveClick(SymbolPropertiesDialog dialog);
        public void onSymbolPropertiesCancelClick(SymbolPropertiesDialog dialog);
    }
    
    // Use this instance of the interface to deliver action events
    private SymbolPropertiesDialogListener mListener;

    public SymbolPropertiesDialog() {
        super();
        String tempString;
        EchelonSymbolModifierSpinnerItem oEchelonSinnerItem;
        EchelonSymbolModifierSpinnerItem oEchelonSinnerItem2;
        this.oUnitSymbolTable = SymbolPropertiesDialog.oUnitSymbolTableList.get(GeoMilSymbol.SymbolStandard.MIL_STD_2525B);

        for (MilStdSymbol.Affiliation affiliation: MilStdSymbol.Affiliation.values()) {
            tempString = StringUtils.capitalizeWords(affiliation.name().replace('_', ' '));
            oAffiliationHash.put(tempString, affiliation);
            oAffiliationList.add(tempString);
        }
        this.loadEchelonLists();
    }

    public static void loadSymbolTables() {
        SymbolPropertiesDialog.oUnitSymbolTableList.put(GeoMilSymbol.SymbolStandard.MIL_STD_2525B, new UnitSymbolTable(GeoMilSymbol.SymbolStandard.MIL_STD_2525B));
        SymbolPropertiesDialog.oUnitSymbolTableList.put(GeoMilSymbol.SymbolStandard.MIL_STD_2525C, new UnitSymbolTable(GeoMilSymbol.SymbolStandard.MIL_STD_2525C));
    }

    private void loadEchelonLists() {
        EchelonSymbolModifierSpinnerItem oEchelonSinnerItem;

        for (MilStdSymbol.EchelonSymbolModifier eValue: MilStdSymbol.EchelonSymbolModifier.values()) {
            oEchelonSinnerItem = new EchelonSymbolModifierSpinnerItem(eValue, -1);
            this.oEchelonMainList.add(oEchelonSinnerItem);
        }

        for (MilStdSymbol.Echelon eValue: MilStdSymbol.Echelon.values()) {
            oEchelonSinnerItem = new EchelonSymbolModifierSpinnerItem(eValue, -1);
            this.oEchelonUnitList.add(oEchelonSinnerItem);
        }

        for (MilStdSymbol.InstalationEchelon eValue: MilStdSymbol.InstalationEchelon.values()) {
            oEchelonSinnerItem = new EchelonSymbolModifierSpinnerItem(eValue, -1);
            this.oEchelonInstallationList.add(oEchelonSinnerItem);
        }

        for (MilStdSymbol.MobilityEchelonModifier eValue: MilStdSymbol.MobilityEchelonModifier.values()) {
            oEchelonSinnerItem = new EchelonSymbolModifierSpinnerItem(eValue, -1);
            this.oEchelonMobilityList.add(oEchelonSinnerItem);
        }

        for (MilStdSymbol.TowedArrayEchelonModifier eValue: MilStdSymbol.TowedArrayEchelonModifier.values()) {
            oEchelonSinnerItem = new EchelonSymbolModifierSpinnerItem(eValue, -1);
            this.oEchelonTowedArrayList.add(oEchelonSinnerItem);
        }
    }

    public void setSymbolCode(String sValue) {
        if (sValue != null) {
            this.sSymbolCode = sValue;
            String sTemp = String.valueOf(armyc2.c2sd.renderer.utilities.SymbolUtilities.getAffiliation(sValue));
            this.eAffiliation = MilStdSymbol.Affiliation.fromString(sTemp);

            sTemp = sValue.substring(10,11);
            this.eMilStdEchelon = MilStdSymbol.EchelonSymbolModifier.fromString(sTemp);

            this.setEchelonList();
            sTemp = sValue.substring(11,12);

            switch (this.eMilStdEchelon) {
                case UNIT:
                case HEADQUARTERS:
                case TASK_FORCE_HQ:
                case FEINT_DUMMY_HQ:
                case FEINT_DUMMY_TASK_FORCE_HQ:
                case TASK_FORCE:
                case FEINT_DUMMY:
                case FEINT_DUMMY_TASK_FORCE:
                    this.iEchelonIndex = MilStdSymbol.Echelon.fromString(sTemp).ordinal();
                    break;
                case INSTALLATION:
                    this.iEchelonIndex = MilStdSymbol.InstalationEchelon.fromString(sTemp).ordinal();
                    break;
                case MOBILITY:
                    this.iEchelonIndex = MilStdSymbol.MobilityEchelonModifier.fromString(sTemp).ordinal();
                    break;
                case TOWED_ARRAY:
                    this.iEchelonIndex = MilStdSymbol.TowedArrayEchelonModifier.fromString(sTemp).ordinal();
                    break;
            }

            this.setSymbolImage();
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
    
    public void setMilStdVersion(org.cmapi.primitives.GeoMilSymbol.SymbolStandard eVersion) {
        if (eVersion != null) {
            this.eMilStdVersion = eVersion;
        }
    }

    public org.cmapi.primitives.GeoMilSymbol.SymbolStandard getMilStdVersion() {
        return this.eMilStdVersion;
    }
/*
    public void setAffiliation(MilStdSymbol.Affiliation eAff) {
        if (eAff != null) {
            this.eAffiliation = eAff;
        }
    }
    
    public MilStdSymbol.Affiliation getAffiliation() {
        return this.eAffiliation;
    }

    public void setEchelon(MilStdSymbol.EchelonSymbolModifier eEchelon) {
        if (eEchelon != null) {
            this.eMilStdEchelon = eEchelon;
        }
    }
    
    public MilStdSymbol.EchelonSymbolModifier getEchelon() {
        return this.eMilStdEchelon;
    }
*/
    public armyc2.c2sd.renderer.utilities.UnitDef getCurrentUnitDef() {
        return this.oCurrentUnitDefItem.getUnitDef();
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
                    + " must implement FeaturePropertiesDialogListener");
        }
    }

    private void setEchelonList() {
        switch (this.eMilStdEchelon) {
            case UNIT:
            case HEADQUARTERS:
            case TASK_FORCE_HQ:
            case FEINT_DUMMY_HQ:
            case FEINT_DUMMY_TASK_FORCE_HQ:
            case TASK_FORCE:
            case FEINT_DUMMY:
            case FEINT_DUMMY_TASK_FORCE:
                this.oEchelonCurrentList = this.oEchelonUnitList;
                break;
            case INSTALLATION:
                this.oEchelonCurrentList = this.oEchelonInstallationList;
                break;
            case MOBILITY:
                this.oEchelonCurrentList = this.oEchelonMobilityList;
                break;
            case TOWED_ARRAY:
                this.oEchelonCurrentList = this.oEchelonTowedArrayList;
                break;
        }
    }
    
    private void setSymbolCodeList() {
        UnitSymbolDefTreeItem oUnitDefItem;
        
        //this.oUnitSymbolTable.loadUnitDef(this.eMilStdVersion);
        if (this.sSymbolCode == null) {
            this.oCurrentUnitDefItem = this.oUnitSymbolTable.getRootSymbolDefItem();
        } else {
            this.oCurrentUnitDefItem = this.oUnitSymbolTable.getUnitSymbolDefItem(this.sSymbolCode);
        }
    }
    
    private void loadSymbolCodeList() {
        int iIndex = 0;
        armyc2.c2sd.renderer.utilities.UnitDef oUnitDef;
        UnitSymbolDefTreeItem oUnitTreeItem;
        UnitDefSpinnerItem oUnitDefSpinnerItem;

        if (this.oCurrentUnitDefItem ==  null) {
            Log.e(TAG, "oCurrentUnitDefItem is null");
            return;
        }

        this.oSymbolSpinnerList.clear();
        this.oSymbolUnitItemMap.clear();

        oUnitDef = new armyc2.c2sd.renderer.utilities.UnitDef("***************",
                SymbolPropertiesDialog.sDropDownFirstOption,
                UnitDef.DRAW_CATEGORY_DONOTDRAW, "", "");
        oUnitTreeItem = new UnitSymbolDefTreeItem(oUnitDef);
        oUnitDefSpinnerItem = new UnitDefSpinnerItem(oUnitTreeItem, -1);
        this.oSymbolSpinnerList.add(oUnitDefSpinnerItem);

        this.oSymbolUnitItemMap.put(iIndex, null);
        iIndex++;

        if (this.oCurrentUnitDefItem.getParent() != null) {
            oUnitDef = new armyc2.c2sd.renderer.utilities.UnitDef("***************",
                    SymbolPropertiesDialog.sDropDownBackOption,
                    UnitDef.DRAW_CATEGORY_DONOTDRAW, "", "");
            oUnitTreeItem = new UnitSymbolDefTreeItem(oUnitDef);
            oUnitDefSpinnerItem = new UnitDefSpinnerItem(oUnitTreeItem, android.R.drawable.arrow_up_float);
            this.oSymbolSpinnerList.add(oUnitDefSpinnerItem);
            this.oSymbolUnitItemMap.put(iIndex, this.oCurrentUnitDefItem.getParent());
            iIndex++;
        }
    
        if (this.oCurrentUnitDefItem.getUnitDef().getDrawCategory() == armyc2.c2sd.renderer.utilities.UnitDef.DRAW_CATEGORY_POINT) {
            oUnitDefSpinnerItem = new UnitDefSpinnerItem(this.oCurrentUnitDefItem, -1);
            this.oSymbolSpinnerList.add(oUnitDefSpinnerItem);
            this.oSymbolUnitItemMap.put(iIndex, this.oCurrentUnitDefItem);
            if (this.sSymbolCode != null) {
                this.iCurrentSymbolItem = iIndex;
            }
            iIndex++;
        }
        
        for (UnitSymbolDefTreeItem oUnitDefItem: this.oCurrentUnitDefItem.getChilrenMap().values()) {
            oUnitDefSpinnerItem = new UnitDefSpinnerItem(oUnitDefItem,
                    (oUnitDefItem.hasChildren()? android.R.drawable.arrow_down_float: -1));
            this.oSymbolSpinnerList.add(oUnitDefSpinnerItem);
            this.oSymbolUnitItemMap.put(iIndex, oUnitDefItem);
            iIndex++;
        }
    }
    
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Get the layout inflater
        final LayoutInflater inflater = getActivity().getLayoutInflater();
        oDialogView = inflater.inflate(R.layout.feature_properties_dialog, null);

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
                        SymbolPropertiesDialog.this.eMilStdVersion = GeoMilSymbol.SymbolStandard.MIL_STD_2525B;
                        break;
                    case R.id.milstd2525C:
                        SymbolPropertiesDialog.this.eMilStdVersion = GeoMilSymbol.SymbolStandard.MIL_STD_2525C;
                        break;
                    default:
                        SymbolPropertiesDialog.this.eMilStdVersion = GeoMilSymbol.SymbolStandard.MIL_STD_2525B;
                        break;
                }
                SymbolPropertiesDialog.this.oUnitSymbolTable = SymbolPropertiesDialog.oUnitSymbolTableList.get(SymbolPropertiesDialog.this.eMilStdVersion);
                SymbolPropertiesDialog.this.sSymbolCode = null;
                oSymbolCode.setText("");
                SymbolPropertiesDialog.this.setSymbolCodeList();
                SymbolPropertiesDialog.this.loadSymbolCodeList();
                SymbolPropertiesDialog.this.setSymbolImage();
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
        
        oAffiliation.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String sValue = (String) parent.getItemAtPosition(position);
                
                SymbolPropertiesDialog.this.eAffiliation = SymbolPropertiesDialog.this.oAffiliationHash.get(sValue);
                SymbolPropertiesDialog.this.setSymbolImage();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        this.setEchelonList();
        final Spinner oEchelon = (Spinner) oDialogView.findViewById(R.id.feature_echelon);
        ArrayAdapter oEchelonStringAdapter = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            oEchelonStringAdapter = new SpinnerWithIconAdapter(inflater, this.getContext(), android.R.layout.simple_spinner_dropdown_item, this.oEchelonCurrentList);
        } else {
            oEchelonStringAdapter = new SpinnerWithIconAdapter(inflater, this.getActivity(), android.R.layout.simple_spinner_dropdown_item, this.oEchelonCurrentList);
        }
        oEchelon.setAdapter(oEchelonStringAdapter);

        oEchelon.setSelection(this.iEchelonIndex);

        oEchelon.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                SymbolPropertiesDialog.this.iEchelonIndex = position;

                SymbolPropertiesDialog.this.updateSymbolCode();
                SymbolPropertiesDialog.this.setSymbolImage();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        final Spinner oEchelonModifier = (Spinner) oDialogView.findViewById(R.id.feature_echelon_modifier);
        ArrayAdapter oEchelonModifierStringAdapter = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            oEchelonModifierStringAdapter = new SpinnerWithIconAdapter(inflater, this.getContext(), android.R.layout.simple_spinner_dropdown_item, this.oEchelonMainList);
        } else {
            oEchelonModifierStringAdapter = new SpinnerWithIconAdapter(inflater, this.getActivity(), android.R.layout.simple_spinner_dropdown_item, this.oEchelonMainList);
        }
        oEchelonModifier.setAdapter(oEchelonModifierStringAdapter);

        if (this.eMilStdEchelon != null) {
            oEchelonModifier.setSelection(this.eMilStdEchelon.ordinal());
        }

        oEchelonModifier.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                ArrayAdapter oNewEchelonStringAdapter;
                List<EchelonSymbolModifierSpinnerItem> oNewList = null;
                EchelonSymbolModifierSpinnerItem oValue = (EchelonSymbolModifierSpinnerItem) parent.getItemAtPosition(position);

                switch (oValue.getEcheclonSymbolModifier()) {
                    case UNIT:
                    case HEADQUARTERS:
                    case TASK_FORCE_HQ:
                    case FEINT_DUMMY_HQ:
                    case FEINT_DUMMY_TASK_FORCE_HQ:
                    case TASK_FORCE:
                    case FEINT_DUMMY:
                    case FEINT_DUMMY_TASK_FORCE:
                        oNewList = SymbolPropertiesDialog.this.oEchelonUnitList;
                        break;
                    case INSTALLATION:
                        oNewList = SymbolPropertiesDialog.this.oEchelonInstallationList;
                        break;
                    case MOBILITY:
                        oNewList = SymbolPropertiesDialog.this.oEchelonMobilityList;
                        break;
                    case TOWED_ARRAY:
                        oNewList = SymbolPropertiesDialog.this.oEchelonTowedArrayList;
                        break;
                }
                SymbolPropertiesDialog.this.eMilStdEchelon = oValue.getEcheclonSymbolModifier();

                if ((oNewList != null) && (SymbolPropertiesDialog.this.oEchelonCurrentList != oNewList)) {
                    SymbolPropertiesDialog.this.oEchelonCurrentList = oNewList;
                    SymbolPropertiesDialog.this.iEchelonIndex = 0;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        oNewEchelonStringAdapter = new SpinnerWithIconAdapter(inflater, SymbolPropertiesDialog.this.getContext(), android.R.layout.simple_spinner_dropdown_item, oNewList);
                    } else {
                        oNewEchelonStringAdapter = new SpinnerWithIconAdapter(inflater, SymbolPropertiesDialog.this.getActivity(), android.R.layout.simple_spinner_dropdown_item, oNewList);
                    }
                    oEchelon.setAdapter(oNewEchelonStringAdapter);
                    oEchelon.performClick();
                } else {
                    SymbolPropertiesDialog.this.updateSymbolCode();
                    SymbolPropertiesDialog.this.setSymbolImage();
                }
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
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            oSymbolCodeStringAdapter = new SpinnerWithIconAdapter(inflater, this.getContext(), android.R.layout.simple_spinner_dropdown_item, this.oSymbolSpinnerList);
        } else {
            oSymbolCodeStringAdapter = new SpinnerWithIconAdapter(inflater, this.getActivity(), android.R.layout.simple_spinner_dropdown_item, this.oSymbolSpinnerList);
        }
        oSymbolSpinner.setAdapter(oSymbolCodeStringAdapter);
        
        if (this.sSymbolCode != null) {
            oSymbolSpinner.setSelection(this.iCurrentSymbolItem);
        }
        
        oSymbolSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                SpinnerWithIconAdapter oNewStringAdapter;

                if (!SymbolPropertiesDialog.this.bProcessSymbolChange) {
                    SymbolPropertiesDialog.this.bProcessSymbolChange = true;
                    return;
                }
                
                UnitSymbolDefTreeItem oUnitDefItem = SymbolPropertiesDialog.this.oSymbolUnitItemMap.get(position);
                
                if (oUnitDefItem != null) {
                    SymbolPropertiesDialog.this.sSymbolCode = oUnitDefItem.getUnitDef().getBasicSymbolId();
                    oSymbolCode.setText(SymbolPropertiesDialog.this.sSymbolCode);
                    SymbolPropertiesDialog.this.setSymbolImage();
                    if (oUnitDefItem != SymbolPropertiesDialog.this.oCurrentUnitDefItem) {
                        if (oUnitDefItem.hasChildren()) {
                            SymbolPropertiesDialog.this.oCurrentUnitDefItem = oUnitDefItem;
                            SymbolPropertiesDialog.this.loadSymbolCodeList();
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                oNewStringAdapter = new SpinnerWithIconAdapter(inflater, SymbolPropertiesDialog.this.getContext(), android.R.layout.simple_spinner_dropdown_item, SymbolPropertiesDialog.this.oSymbolSpinnerList);
                            } else {
                                oNewStringAdapter = new SpinnerWithIconAdapter(inflater, SymbolPropertiesDialog.this.getActivity(), android.R.layout.simple_spinner_dropdown_item, SymbolPropertiesDialog.this.oSymbolSpinnerList);
                            }
                            oSymbolSpinner.setAdapter(oNewStringAdapter);
                            oSymbolSpinner.performClick();
                            SymbolPropertiesDialog.this.bProcessSymbolChange = false;
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
                EditText mEdit = (EditText) SymbolPropertiesDialog.this.oDialogView.findViewById(R.id.featurename);
                SymbolPropertiesDialog.this.sFeatureName = mEdit.getText().toString();
                
                mListener.onSymbolPropertiesSaveClick(SymbolPropertiesDialog.this);
            }
        });
        
        builder.setNegativeButton(R.string.cancelBtn, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                mListener.onSymbolPropertiesCancelClick(SymbolPropertiesDialog.this);
                //FeaturePropertiesDialog.this.getDialog().cancel();
            }
        });
        
        builder.setTitle(R.string.FPD_title);
        
        return builder.create();
    }

    private void setSymbolImage() {
        ImageView oItemIcon = null;

        if (this.oDialogView != null) {
            oItemIcon = (ImageView) this.oDialogView.findViewById(R.id.symbolimage);
        }

        if ((null == this.oDialogView) || (null == this.sSymbolCode) || (this.sSymbolCode.length() != 15)) {
            if (oItemIcon != null) {
                oItemIcon.setImageBitmap(null);
            }
            return;
        }
        try {
            SparseArray<String> emptyArray = new SparseArray<>();

            if ((this.oCurrentUnitDefItem != null) &&
                    (this.oCurrentUnitDefItem.getUnitDef() != null) &&
                    (this.oCurrentUnitDefItem.getUnitDef().getDrawCategory() == UnitDef.DRAW_CATEGORY_POINT)) {
                this.updateSymbolCode();
                MilStdSymbol oSymbol = new MilStdSymbol(this.eMilStdVersion, this.sSymbolCode);
                IMilStdRenderer oRenderer = milStdRenderer;
                IEmpImageInfo oImageInfo;

                oImageInfo = oRenderer.getMilStdIcon(oSymbol.getSymbolCode(), emptyArray, emptyArray);
                if (oImageInfo != null) {
                    oItemIcon.setImageBitmap(oImageInfo.getImage());
                }
            } else {
                oItemIcon.setImageBitmap(null);
            }
        } catch (EMP_Exception e) {
            e.printStackTrace();
        }
    }

    private void updateSymbolCode() {
        if (this.sSymbolCode == null) {
            return;
        }
        String sTemp = SymbolCodeUtils.setEchelonModifier(this.sSymbolCode, this.eMilStdEchelon.toString());
        String sStr;

        switch (this.eMilStdEchelon) {
            default:
            case UNIT:
            case HEADQUARTERS:
            case TASK_FORCE_HQ:
            case FEINT_DUMMY_HQ:
            case FEINT_DUMMY_TASK_FORCE_HQ:
            case TASK_FORCE:
            case FEINT_DUMMY:
            case FEINT_DUMMY_TASK_FORCE:
                sStr = MilStdSymbol.Echelon.values()[this.iEchelonIndex].toString();
                break;
            case INSTALLATION:
                sStr = MilStdSymbol.InstalationEchelon.values()[this.iEchelonIndex].toString();
                break;
            case MOBILITY:
                sStr = MilStdSymbol.MobilityEchelonModifier.values()[this.iEchelonIndex].toString();
                break;
            case TOWED_ARRAY:
                sStr = MilStdSymbol.TowedArrayEchelonModifier.values()[this.iEchelonIndex].toString();
                break;
        }

        this.sSymbolCode = SymbolCodeUtils.setEchelon(sTemp, sStr);
        this.sSymbolCode = armyc2.c2sd.renderer.utilities.SymbolUtilities.setAffiliation(this.sSymbolCode, this.eAffiliation.toString());
    }
}
