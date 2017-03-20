package mil.emp3.core.mapgridlines.utils;

import org.cmapi.primitives.GeoPosition;
import org.cmapi.primitives.IGeoPosition;

/**
 * This class provides static utility functions used in the map grid classes.
 */

public final class GridLineUtils {
    public static IGeoPosition newPosition(double latitude, double longitude, double altitude) {
        IGeoPosition pos = new GeoPosition();

        pos.setLatitude(latitude);
        pos.setLongitude(longitude);
        pos.setAltitude(altitude);
        return pos;
    }
}
