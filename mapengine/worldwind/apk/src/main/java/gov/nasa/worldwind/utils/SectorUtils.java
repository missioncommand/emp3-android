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
    public static Sector intersection(Sector This, Sector that) {
        if (that == null)
            return This;

        double minLat, maxLat;
        minLat = Math.max(This.minLatitude(), that.minLatitude());
        maxLat = Math.min(This.maxLatitude(), that.maxLatitude());
        if (minLat > maxLat)
            return null;

        double minLon, maxLon;
        minLon = Math.max(This.minLongitude(), that.minLongitude());
        maxLon = Math.min(This.maxLongitude(), that.maxLongitude());
        if (minLon > maxLon)
            return null;

        Sector sector = new Sector();
        sector.union(minLat, minLon);
        sector.union(maxLat, maxLon);
        return sector;
    }

    public static Sector getMapSector(IMapInstance mapInstance) {
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
