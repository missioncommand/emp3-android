package mil.emp3.worldwind.layer;

import android.graphics.Typeface;
import android.util.Log;

import org.cmapi.primitives.IGeoLabelStyle;
import org.cmapi.primitives.IGeoPosition;

import java.util.List;

import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.render.Renderable;
import mil.emp3.api.Text;
import mil.emp3.api.utils.FontUtilities;
import mil.emp3.worldwind.feature.FeatureRenderableMapping;
import mil.emp3.worldwind.MapInstance;
import mil.emp3.worldwind.feature.TextFeature;
import mil.emp3.worldwind.utils.Conversion;

public class TextLayer extends EmpLayer<Text> {
    static final private String TAG = TextLayer.class.getSimpleName();

    public TextLayer(MapInstance mapInstance) {
        super(TAG, mapInstance);
    }

    @Override
    protected FeatureRenderableMapping createFeatureMapping(Text feature) {
        return new TextFeature(feature, getMapInstance());
    }
}
