package gov.nasa.worldwind.utils;

import org.cmapi.primitives.IGeoBounds;

import gov.nasa.worldwind.geom.Line;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.geom.Vec3;
import gov.nasa.worldwind.render.RenderContext;
import mil.emp3.api.interfaces.ICamera;
import mil.emp3.mapengine.interfaces.IMapInstance;

/**
 *
 */

public class SectorUtils {
    public static Sector intersection(Sector This, Sector that)
    {
        if (that == null)
            return This;

        double minLat, maxLat;
        minLat = (This.minLatitude() > that.minLatitude()) ? This.minLatitude() : that.minLatitude();
        maxLat = (This.maxLatitude() < that.maxLatitude()) ? This.maxLatitude() : that.maxLatitude();
        if (minLat > maxLat)
            return null;

        double minLon, maxLon;
        minLon = (This.minLongitude() > that.minLongitude()) ? This.minLongitude() : that.minLongitude();
        maxLon = (This.maxLongitude() < that.maxLongitude()) ? This.maxLongitude() : that.maxLongitude();
        if (minLon > maxLon)
            return null;

        return new Sector(minLat, minLon, maxLat - minLat, maxLon - minLon);
    }

    public static Sector getSector(IMapInstance mapInstance) {
        Sector sector = null;
        //ICamera camera = mapInstance.getCamera();
        IGeoBounds bounds = mapInstance.getMapBounds();

        if ((null != bounds) && !Double.isNaN(bounds.getNorth()) && !Double.isNaN(bounds.getSouth()) &&
                !Double.isNaN(bounds.getWest()) && !Double.isNaN(bounds.getEast())) {
            if (bounds.getEast() > bounds.getWest()) {
                sector = new Sector(bounds.getSouth(), bounds.getWest(), bounds.getNorth() - bounds.getSouth(), bounds.getEast() - bounds.getWest());
            } else {
                sector = new Sector(bounds.getSouth(), bounds.getWest(), bounds.getNorth() - bounds.getSouth(), (180 - bounds.getWest()) + (bounds.getEast() + 180));
            }
        }

        return sector;
    }
}
