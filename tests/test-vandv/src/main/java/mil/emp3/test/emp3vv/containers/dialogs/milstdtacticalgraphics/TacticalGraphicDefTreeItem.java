package mil.emp3.test.emp3vv.containers.dialogs.milstdtacticalgraphics;

import armyc2.c2sd.renderer.utilities.SymbolDef;

/**
 *
 */
public class TacticalGraphicDefTreeItem {
    private TacticalGraphicDefTreeItem oParentItem = null;
    private final SymbolDef oSymbolDef;
    private final java.util.TreeMap<String, TacticalGraphicDefTreeItem> oChildren = new java.util.TreeMap<>(new StringVersionComparator());

    public TacticalGraphicDefTreeItem(SymbolDef symDef) {
        this.oSymbolDef = symDef;
    }

    public void setParent(TacticalGraphicDefTreeItem oParent) {
        this.oParentItem = oParent;
    }

    public TacticalGraphicDefTreeItem getParent() {
        return this.oParentItem;
    }

    public SymbolDef getSymbolDef() {
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
        String sHierachy = oChild.getSymbolDef().getHierarchy();
        String sParentHierarchy = StringUtils.getParentVersion(sHierachy);

        if (this.oSymbolDef.getHierarchy().equals(sParentHierarchy)) {
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
