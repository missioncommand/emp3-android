package mil.emp3.validator.dialogs.milstdunits;

import mil.emp3.validator.dialogs.utils.SpinnerWithIconItem;

/**
 *
 */
public class UnitDefSpinnerItem extends SpinnerWithIconItem {
    private final UnitSymbolDefTreeItem oUnitDefItem;

    public UnitDefSpinnerItem(UnitSymbolDefTreeItem oDefItem, int iResId) {
        super(iResId);
        this.oUnitDefItem = oDefItem;
    }

    @Override
    public String getText() {
        return this.oUnitDefItem.getUnitDef().getDescription();
    }

    public String getSymbolCode() {
        return this.oUnitDefItem.getUnitDef().getBasicSymbolId();
    }

    public int getDrawCategory() {
        return this.oUnitDefItem.getUnitDef().getDrawCategory();
    }

    public String getHierarchy() {
        return this.oUnitDefItem.getUnitDef().getHierarchy();
    }
}
