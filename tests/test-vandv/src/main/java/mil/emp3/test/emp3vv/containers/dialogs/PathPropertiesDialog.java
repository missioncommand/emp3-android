package mil.emp3.test.emp3vv.containers.dialogs;

import java.util.List;

import mil.emp3.api.interfaces.IMap;

public class PathPropertiesDialog extends FeaturePropertiesDialog<PathPropertiesDialog> {
    public static PathPropertiesDialog newInstance(String title, IMap map, List<String> parentList,
                                                   String featureName, boolean visible,
                                                   FeaturePropertiesDialog.FeaturePropertiesDialogListener<PathPropertiesDialog> listener) {
        PathPropertiesDialog frag = new PathPropertiesDialog();
        frag.init(title, map, parentList, featureName, visible, listener);
        return frag;
    }

    protected boolean isFeatureMultiPoint() {
        return true;
    }
    @Override
    protected boolean isBufferApplicable() { return true; }
}
