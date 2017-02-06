package mil.emp3.test.emp3vv.containers.dialogs;

import java.util.List;

import mil.emp3.api.interfaces.IMap;

public class PolygonPropertiesDialog extends FeaturePropertiesDialog<PolygonPropertiesDialog> {
    public static PolygonPropertiesDialog newInstance(String title, IMap map, List<String> parentList,
                                                     String featureName, boolean visible,
                                                     FeaturePropertiesDialog.FeaturePropertiesDialogListener<PolygonPropertiesDialog> listener) {
        PolygonPropertiesDialog frag = new PolygonPropertiesDialog();
        frag.init(title, map, parentList, featureName, visible, listener);
        return frag;
    }

    protected boolean isFeatureMultiPoint() {
        return true;
    }
    protected boolean isBufferApplicable() { return true; }
}
