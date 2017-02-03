package mil.emp3.api;

import mil.emp3.api.abstracts.Feature;

import org.cmapi.primitives.GeoCircle;
import org.cmapi.primitives.IGeoCircle;

import mil.emp3.api.enums.FeatureTypeEnum;

/**
 * This class implements a Circle feature. It requires a single coordinate that indicates the
 * geographic position of the center of the circle.
 */
public class Circle extends Feature implements IGeoCircle {

    /**
     * This constructor creates a Circle feature with a default radius.
     */
    public Circle() {
        super(new GeoCircle(), FeatureTypeEnum.GEO_CIRCLE);
    }

    /**
     * This constructor creates a circle with the indicated radius.
     * @param radius The required radius in meters. If the radius is < 0 the absolute value is used. If the value is == 0, the default radius is used.
     */
    public Circle(double radius) {
        super(new GeoCircle(), FeatureTypeEnum.GEO_CIRCLE);
        if (radius < 0) {
            radius = Math.abs(radius);
        } else if (radius == 0) {
            return;
        }
        this.getRenderable().setRadius(radius);
    }

    /**
     * This constructor create a circle feature based on the GeoCircle object provided.
     * @param renderable see IGeoCircle. If the GeoCircle does not have a radius it is set to the default radius.
     */
    public Circle(IGeoCircle renderable) {
        super(renderable, FeatureTypeEnum.GEO_CIRCLE);
    }

    /**
     * This method returns a reference to the GeoCircle object encapsulated in the feature.
     * @return See {@link IGeoCircle}
     */
    @Override
    public final IGeoCircle getRenderable() {
        return (IGeoCircle) super.getRenderable();
    }

    /**
     * This method overrides the default radius of a circular feature.
     * @param radius The new radius in meters. If the radius is < 0 the absolute value is used. If the value is == 0, the radius is NOT set.
     */
    @Override
    public void setRadius(double radius) {
        if (radius < 0) {
            radius = Math.abs(radius);
        } else if (radius == 0) {
            return;
        }
        this.getRenderable().setRadius(radius);
    }

    /**
     * This method retrieves the current radius setting.
     * @return The current radius.
     */
    @Override
    public double getRadius() {
        return this.getRenderable().getRadius();
    }
}
