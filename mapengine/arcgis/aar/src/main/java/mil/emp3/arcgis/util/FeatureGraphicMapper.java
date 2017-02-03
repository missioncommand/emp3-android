package mil.emp3.arcgis.util;

/**
 *
 */
public class FeatureGraphicMapper {
    public mil.emp3.api.interfaces.IFeature oFeature;
    public com.esri.core.map.Graphic oGraphic;
    
    public FeatureGraphicMapper(mil.emp3.api.interfaces.IFeature feature,
        com.esri.core.map.Graphic graphic) {
        this.oFeature = feature;
        this.oGraphic = graphic;
    }
}
