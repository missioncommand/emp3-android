package mil.emp3.test.emp3vv.containers.dialogs.milstdunits;

public class UnitDefSpinnerItem {
    private final UnitSymbolDefTreeItem oUnitDefItem;

    public UnitDefSpinnerItem(UnitSymbolDefTreeItem oDefItem) {
        this.oUnitDefItem = oDefItem;
    }

    @Override
    public String toString() {
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
