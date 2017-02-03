package mil.emp3.validator.dialogs.milstdunits;

import mil.emp3.validator.utils.StringUtils;
import mil.emp3.validator.utils.StringVersionComparator;

/**
 *
 */
public class UnitSymbolDefTreeItem {
    private UnitSymbolDefTreeItem oParentItem = null;
    private final armyc2.c2sd.renderer.utilities.UnitDef oUnitDef;
    private final java.util.TreeMap<String, UnitSymbolDefTreeItem> oChildren = new java.util.TreeMap<>(new StringVersionComparator());

    public UnitSymbolDefTreeItem(armyc2.c2sd.renderer.utilities.UnitDef unitDef) {
        this.oUnitDef = unitDef;
    }

    public void setParent(UnitSymbolDefTreeItem oParent) {
        this.oParentItem = oParent;
    }
    
    public UnitSymbolDefTreeItem getParent() {
        return this.oParentItem;
    }
    
    public armyc2.c2sd.renderer.utilities.UnitDef getUnitDef() {
        return this.oUnitDef;
    }
    
    public boolean hasChildren() {
        return !this.oChildren.isEmpty();
    }
    
    public java.util.TreeMap<String, UnitSymbolDefTreeItem> getChilrenMap() {
        return this.oChildren;
    }

    public void addChild(UnitSymbolDefTreeItem oChild) {
        boolean bAdded = false;
        String sHierachy = oChild.getUnitDef().getHierarchy();
        String sParentHierarchy = StringUtils.getParentVersion(sHierachy);

        if (this.oUnitDef.getHierarchy().equals(sParentHierarchy)) {
            oChild.setParent(this);
            this.oChildren.put(sHierachy, oChild);
        } else {
            while (sParentHierarchy.length() > 0) {
                if (this.oChildren.containsKey(sParentHierarchy)) {
                    this.oChildren.get(sParentHierarchy).addChild(oChild);
                    bAdded = true;
                    break;
                } else {
                    sParentHierarchy = StringUtils.getParentVersion(sParentHierarchy);
                }
            }

            if (!bAdded) {
                oChild.setParent(this);
                this.oChildren.put(sHierachy, oChild);
            }
        }
    }
}
