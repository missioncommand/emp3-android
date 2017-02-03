package mil.emp3.api;


import org.cmapi.primitives.GeoSquare;
import org.cmapi.primitives.IGeoPosition;
import org.cmapi.primitives.IGeoSquare;

import java.util.ArrayList;
import java.util.List;

import mil.emp3.api.abstracts.Feature;

import mil.emp3.api.enums.FeatureTypeEnum;

/**
 * This class implements the EMP square feature. It accepts one (1) geographic coordinate that places the
 * center of the square. The square's length of all sides is set with the setWidth method. And the square
 * is centered at the coordinate.
 */
public class Square extends Feature implements IGeoSquare {

    public Square() {
        super(new GeoSquare(), FeatureTypeEnum.GEO_SQUARE);
    }

    public Square(IGeoPosition oCenter) {
        super(new GeoSquare(), FeatureTypeEnum.GEO_SQUARE);
        List<IGeoPosition> oCoordinates = new ArrayList<>();
        oCoordinates.add(oCenter);
        this.setPositions(oCoordinates);
    }

    public Square(IGeoPosition oCenter, double dWidth) {
        super(new GeoSquare(), FeatureTypeEnum.GEO_SQUARE);
        List<IGeoPosition> oCoordinates = new ArrayList<>();
        oCoordinates.add(oCenter);
        this.setPositions(oCoordinates);
        this.setWidth(dWidth);
    }

    public Square(IGeoSquare oRenderable) {
        super(oRenderable, FeatureTypeEnum.GEO_SQUARE);
    }

    /**
     * This method returns a reference to the GeoSquare object encapsulated in the feature.
     * @return See {@link IGeoSquare}
     */
    @Override
    public final IGeoSquare getRenderable() {
        return (IGeoSquare) super.getRenderable();
    }

    /**
     * This method overrides the default width of a square feature.
     * @param fValue The width in meters.
     */
    @Override
    public void setWidth(double fValue) {
        this.getRenderable().setWidth(fValue);
    }

    /**
     * This method retrieves the width of the rectangle.
     * @return The width in meters.
     */
    @Override
    public double getWidth() {
        return this.getRenderable().getWidth();
    }
}
