package mil.emp3.dev_test_sdk.dialogs.milstdtacticalgraphics;

import armyc2.c2sd.renderer.utilities.SymbolDef;
import mil.emp3.dev_test_sdk.utils.StringVersionComparator;
import armyc2.c2sd.renderer.utilities.SymbolDefTable;
/**
 *
 */
public class TacticalGraphicSymbolTable {
    private org.cmapi.primitives.GeoMilSymbol.SymbolStandard eMilStdVersion = null;
    private TacticalGraphicDefTreeItem oRootItem;
    private final java.util.HashMap<String, String> oBasicSymbolHierarchyMap = new java.util.HashMap<>();
    private final java.util.HashMap<String, TacticalGraphicDefTreeItem> oAllHierarchyItemMap = new java.util.HashMap<>();

    public TacticalGraphicSymbolTable(org.cmapi.primitives.GeoMilSymbol.SymbolStandard eStdVersion) {
        this.loadSymbolDef(eStdVersion);
    }

    private void loadSymbolDef(org.cmapi.primitives.GeoMilSymbol.SymbolStandard eStdVersion) {
        TacticalGraphicDefTreeItem oNewTreeItem;

        if (this.eMilStdVersion == eStdVersion) {
            return;
        }
        this.eMilStdVersion = eStdVersion;
        java.util.TreeMap<String, armyc2.c2sd.renderer.utilities.SymbolDef> oTreeMap = new java.util.TreeMap<>(new StringVersionComparator());

        int iMilStdVersion = (eStdVersion == org.cmapi.primitives.GeoMilSymbol.SymbolStandard.MIL_STD_2525B) ?
                armyc2.c2sd.renderer.utilities.MilStdSymbol.Symbology_2525Bch2_USAS_13_14 : armyc2.c2sd.renderer.utilities.MilStdSymbol.Symbology_2525C;
        SymbolDefTable oDefTable = SymbolDefTable.getInstance();
        java.util.Map<java.lang.String, armyc2.c2sd.renderer.utilities.SymbolDef> oDefList = oDefTable.GetAllSymbolDefs(iMilStdVersion);

        this.oBasicSymbolHierarchyMap.clear();
        oTreeMap.clear();
        for (armyc2.c2sd.renderer.utilities.SymbolDef oSymbolDef: oDefList.values()) {
            if (oSymbolDef.getHierarchy().charAt(0) == '0') {
                continue;
            }
            oTreeMap.put(oSymbolDef.getHierarchy(), oSymbolDef);
            this.oBasicSymbolHierarchyMap.put(oSymbolDef.getBasicSymbolId(), oSymbolDef.getHierarchy());
        }

        // We need to create a dummy root because the render SDK do not have
        // the entire hierarchy.
        armyc2.c2sd.renderer.utilities.SymbolDef oRootDef = new armyc2.c2sd.renderer.utilities.SymbolDef("***************", eStdVersion.name().replace('_', ' '), armyc2.c2sd.renderer.utilities.SymbolDef.DRAW_CATEGORY_UNKNOWN, "", 0, 0, "", "");
        this.oBasicSymbolHierarchyMap.put(oRootDef.getBasicSymbolId(), oRootDef.getHierarchy());
        this.oRootItem = new TacticalGraphicDefTreeItem(oRootDef);
        oAllHierarchyItemMap.put(oRootDef.getHierarchy(), oRootItem);
        for (armyc2.c2sd.renderer.utilities.SymbolDef oDef: oTreeMap.values()) {
            oNewTreeItem = new TacticalGraphicDefTreeItem(oDef);
            this.oRootItem.addChild(oNewTreeItem);
            oAllHierarchyItemMap.put(oDef.getHierarchy(), oNewTreeItem);
        }
    }

    public TacticalGraphicDefTreeItem getRootSymbolDefItem() {
        return this.oRootItem;
    }

    public TacticalGraphicDefTreeItem getSymbolDefItem(String sSymbolCode) {
        String sBasicSymbolCode = armyc2.c2sd.renderer.utilities.SymbolUtilities.getBasicSymbolID(sSymbolCode);
        String sHierarchy = this.oBasicSymbolHierarchyMap.get(sBasicSymbolCode);

        return this.oAllHierarchyItemMap.get(sHierarchy);
    }
}
