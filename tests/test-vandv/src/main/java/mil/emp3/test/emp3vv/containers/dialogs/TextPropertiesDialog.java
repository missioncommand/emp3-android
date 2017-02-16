package mil.emp3.test.emp3vv.containers.dialogs;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import java.util.List;

import mil.emp3.api.interfaces.IMap;
import mil.emp3.test.emp3vv.R;

public class TextPropertiesDialog extends FeaturePropertiesDialog<TextPropertiesDialog> {
    public static TextPropertiesDialog newInstance(String title, IMap map, List<String> parentList,
                                                        String featureName, boolean visible,
                                                        FeaturePropertiesDialog.FeaturePropertiesDialogListener<TextPropertiesDialog> listener) {

        TextPropertiesDialog frag = new TextPropertiesDialog();
        frag.init(title, map, parentList, featureName, visible, listener);
        return frag;
    }

    @Override
    public void onViewCreated(View view, final Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        this.activateAzimuth();

        EditText text = (EditText) view.findViewById(R.id.textFeature);
        text.setVisibility(View.VISIBLE);
    }

    public String getTextForFeature() {
        EditText text = (EditText) getView().findViewById(R.id.textFeature);
        return text.getText().toString();
    }
}
