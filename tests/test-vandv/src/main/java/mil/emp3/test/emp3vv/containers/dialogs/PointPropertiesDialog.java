package mil.emp3.test.emp3vv.containers.dialogs;

import java.util.List;

import mil.emp3.api.interfaces.IMap;

public class PointPropertiesDialog extends FeaturePropertiesDialog<PointPropertiesDialog> {

    public static PointPropertiesDialog newInstance(String title, IMap map, List<String> parentList,
                                                   String featureName, boolean visible,
                                                   FeaturePropertiesDialog.FeaturePropertiesDialogListener<PointPropertiesDialog> listener) {
        PointPropertiesDialog frag = new PointPropertiesDialog();
        frag.init(title, map, parentList, featureName, visible, listener);
        return frag;
    }

    @Override
    protected boolean isBufferApplicable() { return true; }
}
