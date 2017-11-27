package mil.emp3.test.emp3vv.containers.dialogs.milstdtacticalgraphics;

import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.cmapi.primitives.GeoMilSymbol;
import org.cmapi.primitives.IGeoMilSymbol;
import org.cmapi.primitives.IGeoPosition;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import armyc2.c2sd.renderer.utilities.SymbolDef;
import armyc2.c2sd.renderer.utilities.SymbolUtilities;
import mil.emp3.api.MilStdSymbol;
import mil.emp3.api.exceptions.EMP_Exception;
import mil.emp3.api.interfaces.IMap;
import mil.emp3.test.emp3vv.R;
import mil.emp3.test.emp3vv.common.Emp3TesterDialogBase;
import mil.emp3.test.emp3vv.utils.PositionListUtility;
import mil.emp3.test.emp3vv.utils.PositionUtility;

/**
 * This class implements the TG properties dialog box.
 */
public class TacticalGraphicPropertiesDialog extends Emp3TesterDialogBase implements PositionUtility.IPositionChangedListener {
    private final static String TAG = TacticalGraphicPropertiesDialog.class.getSimpleName();

    // Modifier components.
    private Spinner modifierSpinner;
    private EditText modifierValue;
    private List<CharSequence> modifierList;
    private ArrayAdapter<CharSequence> modifierTypeAdapter;
    private HashMap<IGeoMilSymbol.Modifier, String> modifierToValue = new HashMap<>();


    public static final String sDropDownFirstOption = " ";
    public static final String sDropDownBackOption = "back";
    private String sFeatureName = "";
    private GeoMilSymbol.SymbolStandard eMilStdVersion = GeoMilSymbol.SymbolStandard.MIL_STD_2525C;
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
    public interface SymbolPropertiesDialogListener extends IEmp3TesterDialogBaseListener {
        boolean onSaveClick(TacticalGraphicPropertiesDialog dialog);
        void onCancelClick(TacticalGraphicPropertiesDialog dialog);
    }

    // Use this instance of the interface to deliver action events
    private SymbolPropertiesDialogListener mListener;
    private List<String> parentList;
    private boolean featureVisible;
    private PositionUtility positionUtility;
    private PositionListUtility positionListUtility;

    static {
        loadSymbolTables();
    }

    public TacticalGraphicPropertiesDialog() {
        super();
        String tempString;
        this.oSymbolTable = TacticalGraphicPropertiesDialog.oSymbolTableList.get(this.eMilStdVersion);

        for (MilStdSymbol.Affiliation affiliation: MilStdSymbol.Affiliation.values()) {
            tempString = StringUtils.capitalizeWords(affiliation.name().replace('_', ' '));
            oAffiliationHash.put(tempString, affiliation);
            oAffiliationList.add(tempString);
        }
    }

    public static TacticalGraphicPropertiesDialog newInstance(String title, IMap map, List<String> parentList,
                                                     String featureName, boolean visible, SymbolPropertiesDialogListener listener) {
        if (null == listener) {
            throw new IllegalArgumentException("listener and must be non-null");
        }

        if((null == parentList) || (0 == parentList.size())) {
            throw new IllegalArgumentException("parentList must be non-null and non empty");
        }

        TacticalGraphicPropertiesDialog frag = new TacticalGraphicPropertiesDialog();
        Bundle args = new Bundle();
        args.putString("title", title);
        frag.setArguments(args);
        frag.init(map, listener);
        frag.mListener = listener;
        frag.setParentList(parentList);
        frag.setFeatureName(featureName);
        frag.setFeatureVisible(visible);
        return frag;
    }

    public void setParentList(List<String> parentList) {
        this.parentList = parentList;
    }

    public List<String> getParentList() {
        return parentList;
    }
    public void setFeatureVisible(boolean visible) {
        Log.d(TAG, this.sFeatureName + " visible " + visible);
        this.featureVisible = visible;
    }

    public boolean isFeatureVisible() {
        return featureVisible;
    }

    public static void loadSymbolTables() {
        TacticalGraphicPropertiesDialog.oSymbolTableList.put(GeoMilSymbol.SymbolStandard.MIL_STD_2525B, new TacticalGraphicSymbolTable(GeoMilSymbol.SymbolStandard.MIL_STD_2525B));
        TacticalGraphicPropertiesDialog.oSymbolTableList.put(GeoMilSymbol.SymbolStandard.MIL_STD_2525C, new TacticalGraphicSymbolTable(GeoMilSymbol.SymbolStandard.MIL_STD_2525C));
    }

    public void setSymbolCode(String sValue) {
        Log.d(TAG, this.sFeatureName + " symbol code " + sValue);
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
        Log.d(TAG, "Feature name is " + sValue);
        if (sValue != null) {
            this.sFeatureName = sValue;
        }
    }

    public HashMap<IGeoMilSymbol.Modifier, String> getModifiers() {
        return this.modifierToValue;
    }

    public String getFeatureName() {
        return this.sFeatureName;
    }

    public void setMilStdVersion(GeoMilSymbol.SymbolStandard eVersion) {
        Log.d(TAG, this.sFeatureName + " standard " + eVersion);
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
        oDefSpinnerItem = new TacticalGraphicSpinnerItem(oTreeItem);
        this.oSymbolSpinnerList.add(oDefSpinnerItem);

        this.oSymbolItemMap.put(iIndex, null);
        iIndex++;

        if (oParentTreeItem != null) {
            oDef = new SymbolDef("***************",
                    oParentTreeItem.getSymbolDef().getDescription(),
                    //TacticalGraphicPropertiesDialog.sDropDownBackOption,
                    SymbolDef.DRAW_CATEGORY_UNKNOWN, "", 0, 0, "", "");
            oTreeItem = new TacticalGraphicDefTreeItem(oDef);
            oDefSpinnerItem = new TacticalGraphicSpinnerItem(oTreeItem);
            this.oSymbolSpinnerList.add(oDefSpinnerItem);
            this.oSymbolItemMap.put(iIndex, oParentTreeItem);
            iIndex++;
        }

        if ((this.oCurrentDefItem.getSymbolDef().getDrawCategory() != SymbolDef.DRAW_CATEGORY_UNKNOWN) &&
                (this.oCurrentDefItem.getSymbolDef().getDrawCategory() != SymbolDef.DRAW_CATEGORY_3D_AIRSPACE) &&
                (this.oCurrentDefItem.getSymbolDef().getDrawCategory() != SymbolDef.DRAW_CATEGORY_DONOTDRAW)){
            oTreeItem = new TacticalGraphicDefTreeItem(this.oCurrentDefItem.getSymbolDef());
            oDefSpinnerItem = new TacticalGraphicSpinnerItem(oTreeItem);
            this.oSymbolSpinnerList.add(oDefSpinnerItem);
            this.oSymbolItemMap.put(iIndex, oTreeItem); //this.oCurrentDefTreeItem);
            if (this.sSymbolCode != null) {
                this.iCurrentSymbolItem = iIndex;
            }
            iIndex++;
        }

        for (TacticalGraphicDefTreeItem oDefItem: this.oCurrentDefItem.getChildrenMap().values()) {
            oDefSpinnerItem = new TacticalGraphicSpinnerItem(oDefItem);
            this.oSymbolSpinnerList.add(oDefSpinnerItem);
            this.oSymbolItemMap.put(iIndex, oDefItem);
            iIndex++;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        oDialogView = inflater.inflate(R.layout.tactical_graphic_properties_dialog, container);
        setDialogPosition();
        getDialog().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        return oDialogView;
    }

    @Override
    public void onViewCreated(View view, final Bundle savedInstanceState) {

        super.onViewCreated(view, savedInstanceState);

        // Modifier stuff
        modifierToValue.clear();
        modifierSpinner = (Spinner) view.findViewById(R.id.modifier_type);
        modifierValue = (EditText) view.findViewById(R.id.modifier_value);

        modifierList = new ArrayList<>();
        for(IGeoMilSymbol.Modifier modifier : IGeoMilSymbol.Modifier.values()) {
            modifierList.add(modifier.toString());
        }
        modifierTypeAdapter = new ArrayAdapter<CharSequence>(this.getContext(), android.R.layout.simple_spinner_item, modifierList);
        modifierSpinner.setAdapter(modifierTypeAdapter);
        // Modifier Button
        Button addModifierButton = (Button) view.findViewById(R.id.add_modifier_button);
        addModifierButton.setOnClickListener(v -> {
            modifierToValue.put(IGeoMilSymbol.Modifier.valueOf(((String)modifierSpinner.getSelectedItem())), modifierValue.getText().toString());
            Toast.makeText(this.getContext(), "Added modifier", Toast.LENGTH_SHORT).show();
        });

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
        ArrayAdapter oSymbolCodeStringAdapter;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            oSymbolCodeStringAdapter = new ArrayAdapter(this.getContext(), android.R.layout.simple_spinner_dropdown_item, this.oSymbolSpinnerList);
        } else {
            oSymbolCodeStringAdapter = new ArrayAdapter(this.getActivity(), android.R.layout.simple_spinner_dropdown_item, this.oSymbolSpinnerList);
        }
        oSymbolSpinner.setAdapter(oSymbolCodeStringAdapter);

        if (this.sSymbolCode != null) {
            oSymbolSpinner.setSelection(this.iCurrentSymbolItem);
        }

        oSymbolSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                ArrayAdapter oNewStringAdapter;

                if (!TacticalGraphicPropertiesDialog.this.bProcessSymbolChange) {
                    TacticalGraphicPropertiesDialog.this.bProcessSymbolChange = true;
                    return;
                }

                TacticalGraphicDefTreeItem oDefItem = TacticalGraphicPropertiesDialog.this.oSymbolItemMap.get(position);

                if (oDefItem != null) {
                    TacticalGraphicPropertiesDialog.this.sSymbolCode = oDefItem.getSymbolDef().getBasicSymbolId();
                    Log.d(TAG, getFeatureName() + " is " + oDefItem.getSymbolDef().getDescription());
                    Log.d(TAG, getFeatureName() + " symbol code set to " + getSymbolCode());
                    oSymbolCode.setText(TacticalGraphicPropertiesDialog.this.sSymbolCode);

                    if (oDefItem != TacticalGraphicPropertiesDialog.this.oCurrentDefItem) {
                        if (oDefItem.hasChildren()) {
                            TacticalGraphicPropertiesDialog.this.oCurrentDefItem = oDefItem;
                            TacticalGraphicPropertiesDialog.this.loadSymbolCodeList();
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                oNewStringAdapter = new ArrayAdapter(TacticalGraphicPropertiesDialog.this.getContext(), android.R.layout.simple_spinner_dropdown_item, TacticalGraphicPropertiesDialog.this.oSymbolSpinnerList);
                            } else {
                                oNewStringAdapter = new ArrayAdapter(TacticalGraphicPropertiesDialog.this.getActivity(), android.R.layout.simple_spinner_dropdown_item, TacticalGraphicPropertiesDialog.this.oSymbolSpinnerList);
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

        Button doneButton = (Button) view.findViewById(R.id.cancel);
        doneButton.setOnClickListener(v -> {
            TacticalGraphicPropertiesDialog.this.dismiss();
            mListener.onCancelClick(TacticalGraphicPropertiesDialog.this);
            positionUtility.stop();
            positionListUtility.stop();
        });

        Button applyButton = (Button) view.findViewById(R.id.apply);
        applyButton.setOnClickListener(v -> {
            if (null != TacticalGraphicPropertiesDialog.this.mListener) {

                EditText mEdit = (EditText) TacticalGraphicPropertiesDialog.this.oDialogView.findViewById(R.id.featurename);
                TacticalGraphicPropertiesDialog.this.sFeatureName = mEdit.getText().toString();

                if(mListener.onSaveClick(TacticalGraphicPropertiesDialog.this)) {
                    TacticalGraphicPropertiesDialog.this.dismiss();
                    positionUtility.stop();
                    positionListUtility.stop();
                }
            } else {
                positionUtility.stop();
                positionListUtility.stop();
                TacticalGraphicPropertiesDialog.this.dismiss();
            }
        });

        try {
            positionUtility = new PositionUtility(map, this, true);
        } catch (EMP_Exception e) {
            Log.e(TAG, "positionUtility ", e);
        }

        // Allows user to type in positions. Once this button is selected, position utility is turned off, i.e.
        // Tapping on the map will have no consequences to position list.
        positionListUtility = new PositionListUtility(view, getMap());
        positionListUtility.onCreateView(getFragmentManager(), positionUtility);
    }
    public PositionUtility getPositionUtility() {
        return positionUtility;
    }

    @Override
    public void newPosition(IGeoPosition geoPosition, String stringPosition) {
        TextView view = (TextView) getView().findViewById(R.id.position);
        view.setText(stringPosition);
    }

    private void updateSymbolCode() {
        if (this.sSymbolCode == null) {
            return;
        }

        this.sSymbolCode = SymbolUtilities.setAffiliation(this.sSymbolCode, this.eAffiliation.toString());
        Log.d(TAG, this.sFeatureName + " symbol code updated " + this.sSymbolCode);
    }
}
