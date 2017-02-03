package mil.emp3.arcgis.plotters;

import org.cmapi.primitives.IGeoRenderable;

/**
 */
public interface IRenderer<T extends IGeoRenderable> {
    public com.esri.core.map.Graphic buildGraphic(T geoRenderable);
}
