package mil.emp3.dev_test_sdk.dialogs.milstdunits;

import armyc2.c2sd.renderer.utilities.UnitDef;
import armyc2.c2sd.renderer.utilities.UnitDefTable;
import mil.emp3.dev_test_sdk.utils.StringVersionComparator;

/**
 *
 */
public class UnitSymbolTable {
    private org.cmapi.primitives.GeoMilSymbol.SymbolStandard eMilStdVersion = null;
    private UnitSymbolDefTreeItem oRootUnitItem;
    private final java.util.HashMap<String, String> oBasicSymbolHierarchyMap = new java.util.HashMap<>();
    private final java.util.HashMap<String, UnitSymbolDefTreeItem> oAllHierarchyItemMap = new java.util.HashMap<>();

    public UnitSymbolTable(org.cmapi.primitives.GeoMilSymbol.SymbolStandard eStdVersion) {
        this.loadUnitDef(eStdVersion);
    }

    private void loadUnitDef(org.cmapi.primitives.GeoMilSymbol.SymbolStandard eStdVersion) {
        UnitSymbolDefTreeItem oNewTreeItem;

        if (this.eMilStdVersion == eStdVersion) {
            return;
        }
        this.eMilStdVersion = eStdVersion;
        java.util.TreeMap<String, armyc2.c2sd.renderer.utilities.UnitDef> oUnitTreeMap = new java.util.TreeMap<>(new StringVersionComparator());
        
        int iMilStdVersion = (eStdVersion == org.cmapi.primitives.GeoMilSymbol.SymbolStandard.MIL_STD_2525B) ?
                armyc2.c2sd.renderer.utilities.MilStdSymbol.Symbology_2525Bch2_USAS_13_14 : armyc2.c2sd.renderer.utilities.MilStdSymbol.Symbology_2525C;
        UnitDefTable oDefTable = UnitDefTable.getInstance();
        java.util.Map<java.lang.String, armyc2.c2sd.renderer.utilities.UnitDef> oUnitDefList = oDefTable.getAllUnitDefs(iMilStdVersion);
        
        this.oBasicSymbolHierarchyMap.clear();
        oUnitTreeMap.clear();
        for (armyc2.c2sd.renderer.utilities.UnitDef oUnitDef: oUnitDefList.values()) {
            oUnitTreeMap.put(oUnitDef.getHierarchy(), oUnitDef);
            this.oBasicSymbolHierarchyMap.put(oUnitDef.getBasicSymbolId(), oUnitDef.getHierarchy());
        }

        // We need to create a dummy root because the render SDK do not have
        // the entire hierarchy.
        armyc2.c2sd.renderer.utilities.UnitDef oRootUnitDef = new armyc2.c2sd.renderer.utilities.UnitDef("***************", eStdVersion.name().replace('_', ' '), UnitDef.DRAW_CATEGORY_DONOTDRAW, "", "");
        this.oBasicSymbolHierarchyMap.put(oRootUnitDef.getBasicSymbolId(), oRootUnitDef.getHierarchy());
        this.oRootUnitItem = new UnitSymbolDefTreeItem(oRootUnitDef);
        oAllHierarchyItemMap.put(oRootUnitDef.getHierarchy(), oRootUnitItem);
        for (armyc2.c2sd.renderer.utilities.UnitDef oUnitDef: oUnitTreeMap.values()) {
            oNewTreeItem = new UnitSymbolDefTreeItem(oUnitDef);
            this.oRootUnitItem.addChild(oNewTreeItem);
            oAllHierarchyItemMap.put(oUnitDef.getHierarchy(), oNewTreeItem);
        }
    }
    
    public UnitSymbolDefTreeItem getRootSymbolDefItem() {
        return this.oRootUnitItem;
    }
    
    public UnitSymbolDefTreeItem getUnitSymbolDefItem(String sSymbolCode) {
        String sBasicSymbolCode = armyc2.c2sd.renderer.utilities.SymbolUtilities.getBasicSymbolID(sSymbolCode);
        String sHierarchy = this.oBasicSymbolHierarchyMap.get(sBasicSymbolCode);
        
        return this.oAllHierarchyItemMap.get(sHierarchy);
    }
}
