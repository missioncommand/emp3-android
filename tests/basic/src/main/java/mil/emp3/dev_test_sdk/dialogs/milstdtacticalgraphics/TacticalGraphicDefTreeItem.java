package mil.emp3.dev_test_sdk.dialogs.milstdtacticalgraphics;

import armyc2.c2sd.renderer.utilities.SymbolDef;
import mil.emp3.dev_test_sdk.utils.StringUtils;
import mil.emp3.dev_test_sdk.utils.StringVersionComparator;

/**
 *
 */
public class TacticalGraphicDefTreeItem {
    private TacticalGraphicDefTreeItem oParentItem = null;
    private final armyc2.c2sd.renderer.utilities.SymbolDef oSymbolDef;
    private final java.util.TreeMap<String, TacticalGraphicDefTreeItem> oChildren = new java.util.TreeMap<>(new StringVersionComparator());

    public TacticalGraphicDefTreeItem(armyc2.c2sd.renderer.utilities.SymbolDef symDef) {
        this.oSymbolDef = symDef;
    }

    public void setParent(TacticalGraphicDefTreeItem oParent) {
        this.oParentItem = oParent;
    }

    public TacticalGraphicDefTreeItem getParent() {
        return this.oParentItem;
    }

    public armyc2.c2sd.renderer.utilities.SymbolDef getSymbolDef() {
        return this.oSymbolDef;
    }

    public boolean hasChildren() {
        return !this.oChildren.isEmpty();
    }

    public java.util.TreeMap<String, TacticalGraphicDefTreeItem> getChildrenMap() {
        return this.oChildren;
    }

    public void addChild(TacticalGraphicDefTreeItem oChild) {
        boolean bAdded = false;
        String sHierarchy = oChild.getSymbolDef().getHierarchy();
        String sParentHierarchy = StringUtils.getParentVersion(sHierarchy);

        if (this.oSymbolDef.getHierarchy().equals(sParentHierarchy)) {
            oChild.setParent(this);
            this.oChildren.put(sHierarchy, oChild);
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
                this.oChildren.put(sHierarchy, oChild);
            }
        }
    }
}
