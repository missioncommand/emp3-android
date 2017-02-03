package mil.emp3.api;


import mil.emp3.api.abstracts.Feature;

import org.cmapi.primitives.GeoEllipse;
import org.cmapi.primitives.IGeoEllipse;

import mil.emp3.api.enums.FeatureTypeEnum;

/**
 * This class implements an ellipse feature. It requires a single coordinate that indicates the
 * geographic position of the center of the ellipse.
 */
public class Ellipse extends Feature implements IGeoEllipse {

    /**
     * This constructor creates an ellipse with default dimensions.
     */
    public Ellipse() {
        super(new GeoEllipse(), FeatureTypeEnum.GEO_ELLIPSE);
    }

    /**
     * This constructor creates an ellipse with the given major and minor radius.
     * @param dMajorRadius The major radius in meters.
     * @param dMinorRadius  The minor radius in meters.
     */
    public Ellipse(double dMajorRadius, double dMinorRadius) {
        super(new GeoEllipse(), FeatureTypeEnum.GEO_ELLIPSE);
        this.setSemiMajor(dMajorRadius);
        this.setSemiMinor(dMinorRadius);
    }

    /**
     * This constructor creates an ellipse with the given major, minor radius and oriented at the azimuth angle from north.
     * @param dMajorRadius The major radius in meters.
     * @param dMinorRadius  The minor radius in meters.
     * @param dAzimuth The azimuth in degrees.
     */
    public Ellipse(double dMajorRadius, double dMinorRadius, double dAzimuth) {
        super(new GeoEllipse(), FeatureTypeEnum.GEO_ELLIPSE);
        this.setAzimuth(dAzimuth);
        this.setSemiMajor(dMajorRadius);
        this.setSemiMinor(dMinorRadius);
    }

    /**
     * This constructor creates an ellipse that encapsulates the given IGeoEllipse.
     * @param oRenderable See {@link IGeoEllipse}
     */
    public Ellipse(IGeoEllipse oRenderable) {
        super(oRenderable, FeatureTypeEnum.GEO_ELLIPSE);
    }

    /**
     * This method returns a reference to the GeoEllipse object encapsulated in the feature.
     * @return See {@link IGeoEllipse}
     */
    @Override
    public final IGeoEllipse getRenderable() {
        return (IGeoEllipse) super.getRenderable();
    }

    /**
     * This method sets the major radius of an elliptical feature.
     * @param value The major radius in meters.
     */
    @Override
    public void setSemiMajor(double value) {
        this.getRenderable().setSemiMajor(value);
    }

    /**
     * This method retrieves the major radius value.
     * @return The major radius value.
     */
    @Override
    public double getSemiMajor() {
        return this.getRenderable().getSemiMajor();
    }

    /**
     * This method sets minor radius of an elliptical feature.
     * @param value The minor radius in meters.
     */
    @Override
    public void setSemiMinor(double value) {
        this.getRenderable().setSemiMinor(value);
    }

    /**
     * This method retrieves the minor radius of an elliptical feature
     * @return The minor radius.
     */
    @Override
    public double getSemiMinor() {
        return this.getRenderable().getSemiMinor();
    }
}
