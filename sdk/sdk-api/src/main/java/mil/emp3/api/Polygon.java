package mil.emp3.api;


import org.cmapi.primitives.GeoPolygon;
import org.cmapi.primitives.IGeoPolygon;
import org.cmapi.primitives.IGeoPosition;

import java.util.List;
import mil.emp3.api.abstracts.Feature;

import mil.emp3.api.enums.FeatureTypeEnum;

/**
 * This class implements an polygon. It requires 3 or more geographic coordinates.
 */
public class Polygon extends Feature implements IGeoPolygon {

    public Polygon() {
        super(new GeoPolygon(), FeatureTypeEnum.GEO_POLYGON);
    }

    public Polygon(List<IGeoPosition> oPositionList) {
        super(new GeoPolygon(), FeatureTypeEnum.GEO_POLYGON);
        this.setPositions(oPositionList);
    }

    public Polygon(IGeoPolygon oRenderable) {
        super(oRenderable, FeatureTypeEnum.GEO_POLYGON);
    }

    /**
     * This method returns a reference to the GeoPolygon object encapsulated in the feature.
     * @return See {@link IGeoPolygon}
     */
    @Override
    public final IGeoPolygon getRenderable() {
        return (IGeoPolygon) super.getRenderable();
    }
}
