package mil.emp3.test.emp3vv.containers.dialogs.milstdtacticalgraphics;

/**
 *
 */
public class TacticalGraphicSpinnerItem {
    private final TacticalGraphicDefTreeItem oDefItem;

    public TacticalGraphicSpinnerItem(TacticalGraphicDefTreeItem oDefItem) {
        this.oDefItem = oDefItem;
    }

    @Override
    public String toString() {
        return this.oDefItem.getSymbolDef().getDescription();
    }

    public String getSymbolCode() {
        return this.oDefItem.getSymbolDef().getBasicSymbolId();
    }

    public int getDrawCategory() {
        return this.oDefItem.getSymbolDef().getDrawCategory();
    }

    public String getHierarchy() {
        return this.oDefItem.getSymbolDef().getHierarchy();
    }
}
