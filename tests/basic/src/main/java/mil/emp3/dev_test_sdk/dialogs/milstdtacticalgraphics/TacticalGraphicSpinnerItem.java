package mil.emp3.dev_test_sdk.dialogs.milstdtacticalgraphics;

import mil.emp3.dev_test_sdk.dialogs.utils.SpinnerWithIconItem;

/**
 *
 */
public class TacticalGraphicSpinnerItem extends SpinnerWithIconItem {
    private final TacticalGraphicDefTreeItem oDefItem;

    public TacticalGraphicSpinnerItem(TacticalGraphicDefTreeItem oDefItem, int iResId) {
        super(iResId);
        this.oDefItem = oDefItem;
    }

    @Override
    public String getText() {
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
