package mil.emp3.api;


import org.cmapi.primitives.GeoPolygon;
import org.cmapi.primitives.IGeoPolygon;
import org.cmapi.primitives.IGeoPosition;
import org.cmapi.primitives.IGeoStrokeStyle;
import org.xmlpull.v1.XmlSerializer;

import java.io.IOException;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;
import mil.emp3.api.abstracts.Feature;

import mil.emp3.api.enums.FeatureTypeEnum;
import mil.emp3.api.utils.kml.EmpKMLExporter;

/**
 * This class implements an polygon. It requires 3 or more geographic coordinates.
 */
public class Polygon extends Feature<IGeoPolygon> implements IGeoPolygon {

    /**
     * This constructor creates a default geo polygon.
     */
    public Polygon() {
        super(new GeoPolygon(), FeatureTypeEnum.GEO_POLYGON);
    }

    /**
     * This constructor creates a default geo polygon with the coordinate list provided.
     * @param oPositionList
     */
    public Polygon(List<IGeoPosition> oPositionList) {
        super(new GeoPolygon(), FeatureTypeEnum.GEO_POLYGON);
        if (null == oPositionList) {
            throw new InvalidParameterException("The position list parameter can NOT be null");
        }
        this.getRenderable().getPositions().addAll(oPositionList);
    }

    /**
     * This constructor creates a polygon feature from the geo polygon provided.
     * @param oRenderable
     */
    public Polygon(IGeoPolygon oRenderable) {
        super(oRenderable, FeatureTypeEnum.GEO_POLYGON);
    }
}
