package mil.emp3.api;


import org.cmapi.primitives.GeoRectangle;
import org.cmapi.primitives.IGeoPosition;
import org.cmapi.primitives.IGeoRectangle;

import java.util.ArrayList;
import java.util.List;

import mil.emp3.api.abstracts.Feature;

import mil.emp3.api.enums.FeatureTypeEnum;

/**
 * This class implements the EMP rectangle feature. It accepts one (1) geographic coordinate that places the
 * center of the rectangle. The rectangle's width and height are centered at the coordinate.
 */
public class Rectangle extends Feature implements IGeoRectangle {

    public Rectangle() {
        super(new GeoRectangle(), FeatureTypeEnum.GEO_RECTANGLE);
    }

    public Rectangle(IGeoPosition oCenter) {
        super(new GeoRectangle(), FeatureTypeEnum.GEO_RECTANGLE);
        List<IGeoPosition> oCoordinates = new ArrayList<>();
        oCoordinates.add(oCenter);
        this.setPositions(oCoordinates);
    }

    public Rectangle(IGeoPosition oCenter, double dWidth, double dHeight) {
        super(new GeoRectangle(), FeatureTypeEnum.GEO_RECTANGLE);
        List<IGeoPosition> oCoordinates = new ArrayList<>();
        oCoordinates.add(oCenter);
        this.setPositions(oCoordinates);
        this.setWidth(dWidth);
        this.setHeight(dHeight);
    }

    public Rectangle(IGeoRectangle oRenderable) {
        super(oRenderable, FeatureTypeEnum.GEO_RECTANGLE);
    }

    /**
     * This method returns a reference to the GeoRectangle object encapsulated in the feature.
     * @return See {@link IGeoRectangle}
     */
    @Override
    public final IGeoRectangle getRenderable() {
        return (IGeoRectangle) super.getRenderable();
    }

    /**
     * This method overrides the default width of a rectangular feature.
     * @param fValue The width in meters.
     */
    @Override
    public void setWidth(double fValue) {
        ((IGeoRectangle) this.getRenderable()).setWidth(fValue);
    }

    /**
     * This method retrieves the width of the rectangle.
     * @return The width in meters.
     */
    @Override
    public double getWidth() {
        return ((IGeoRectangle) this.getRenderable()).getWidth();
    }

    /**
     * This method overrides the default height of a rectangular feature.
     * @param fValue The width in meters.
     */
    @Override
    public void setHeight(double fValue) {
        ((IGeoRectangle) this.getRenderable()).setHeight(fValue);
    }

    /**
     * This method retrieves the height of the rectangle.
     * @return The height in meters or 0 if its not applicable.
     */
    @Override
    public double getHeight() {
        return ((IGeoRectangle) this.getRenderable()).getHeight();
    }
}
