package mil.emp3.api;


import android.graphics.Bitmap;

import org.cmapi.primitives.GeoPolygon;
import org.cmapi.primitives.IGeoPolygon;
import org.cmapi.primitives.IGeoPosition;

import java.util.List;
import mil.emp3.api.abstracts.Feature;

import mil.emp3.api.enums.FeatureTypeEnum;

/**
 * This class implements an polygon. It requires 3 or more geographic coordinates.
 */
public class Polygon extends Feature<IGeoPolygon> implements IGeoPolygon {

    private Bitmap patternFill = null;
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
            throw new IllegalArgumentException("The position list parameter can NOT be null");
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

    public void setPatternFillImage(Bitmap bmp){patternFill = bmp;}
    public Bitmap getPatternFillImage(){return patternFill;}
    /**
     *
     * @return String gives polygon parameters
     */

    @Override
    public String toString() {
        String str =  "Polygon ";
        if (getPositions() != null && !getPositions().isEmpty()) {
            str += "with " + getPositions().size() + " points\n";
            for (IGeoPosition position : getPositions()) {
                str += "\tlatitude: " + position.getLatitude() + ", " +
                        "\tlongitude: " + position.getLongitude() + ", " +
                        "\taltitude: " + position.getAltitude() + "\n";
            }
        } else {
            str +="with zero points";
        }
        return str;
    }

}
