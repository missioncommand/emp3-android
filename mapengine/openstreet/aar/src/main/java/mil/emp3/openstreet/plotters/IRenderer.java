package mil.emp3.openstreet.plotters;

import org.cmapi.primitives.IGeoRenderable;

/**
 * Created by deepakkarmarkar on 3/30/2016.
 */
public interface IRenderer<T extends IGeoRenderable> {
    void render(T geoRenderable);
}
