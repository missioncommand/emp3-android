package mil.emp3.mapengine.interfaces;

import org.cmapi.primitives.IGeoBounds;

import mil.emp3.api.interfaces.ICamera;
import mil.emp3.mapengine.events.MapInstanceViewChangeEvent;

/**
 * This class defines the interface to a map grid line generator.
 */

public interface ICoreMapGridLineGenerator {
    void shutdownGenerator();
    void mapViewChange(IGeoBounds mapBounds, ICamera camera, int viewWidth, int viewHeight);
}
