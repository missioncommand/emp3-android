package mil.emp3.worldwind.feature;

import mil.emp3.api.Text;
import mil.emp3.worldwind.MapInstance;

/**
 * This class implements the mapping between an EMP Text feature and the WW renderables.
 */

public class TextFeature extends FeatureRenderableMapping<Text> {
    public TextFeature(Text feature, MapInstance instance) {
        super(feature, instance);
    }
}
