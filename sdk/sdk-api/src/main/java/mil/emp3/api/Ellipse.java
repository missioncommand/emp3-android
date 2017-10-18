package mil.emp3.api;


import mil.emp3.api.abstracts.Feature;

import org.cmapi.primitives.GeoEllipse;
import org.cmapi.primitives.GeoPosition;
import org.cmapi.primitives.IGeoEllipse;
import org.cmapi.primitives.IGeoPosition;

import java.util.List;

import mil.emp3.api.enums.FeatureTypeEnum;
import mil.emp3.api.interfaces.IEmpBoundingBox;
import mil.emp3.api.utils.EmpBoundingBox;
import mil.emp3.api.utils.GeoLibrary;

/**
 * This class implements an ellipse feature. It requires a single coordinate that indicates the
 * geographic position of the center of the ellipse.
 */
public class Ellipse extends Feature<IGeoEllipse> implements IGeoEllipse {

    public final static double MINIMUM_SEMI_MAJOR = global.MINIMUM_DISTANCE;
    public final static double MINIMUM_SEMI_MINOR = global.MINIMUM_DISTANCE;

    /**
     * This constructor creates an ellipse with default dimensions.
     */
    public Ellipse() {
        super(new GeoEllipse(), FeatureTypeEnum.GEO_ELLIPSE);
        this.setFillStyle(null);
    }

    /**
     * This constructor creates an ellipse with the given major and minor radius. Negative values are made positive. The absolute value of the values must be >= 1.0.
     * @param dMajorRadius see {@link #setSemiMajor(double)}.
     * @param dMinorRadius  see {@link #setSemiMinor(double)}.
     */
    public Ellipse(final double dMajorRadius, final double dMinorRadius) {
        super(new GeoEllipse(), FeatureTypeEnum.GEO_ELLIPSE);
        this.setSemiMajor(dMajorRadius);
        this.setSemiMinor(dMinorRadius);
        this.setFillStyle(null);
    }

    /**
     * This constructor creates an ellipse with the given major, minor radius and oriented at the azimuth angle from north.
     * @param dMajorRadius see {@link #setSemiMajor(double)}.
     * @param dMinorRadius  see {@link #setSemiMinor(double)}.
     * @param dAzimuth see {@link Feature#setAzimuth(double)}.
     */
    public Ellipse(final double dMajorRadius, final double dMinorRadius, final double dAzimuth) {
        super(new GeoEllipse(), FeatureTypeEnum.GEO_ELLIPSE);
        this.setAzimuth(dAzimuth);
        this.setSemiMajor(dMajorRadius);
        this.setSemiMinor(dMinorRadius);
        this.setFillStyle(null);
    }

    /**
     * This constructor creates an ellipse that encapsulates the given IGeoEllipse.
     * @param oRenderable See {@link IGeoEllipse}
     */
    public Ellipse(IGeoEllipse oRenderable) {
        super(oRenderable, FeatureTypeEnum.GEO_ELLIPSE);

        if(null == oRenderable) {
            throw new IllegalArgumentException("Encapsulated GeoEllipse must be non-null");
        }

        this.setSemiMajor(this.getSemiMajor());
        this.setSemiMinor(this.getSemiMinor());
        this.setAzimuth(this.getAzimuth());  // For validation
    }

    /**
     * This method sets the major radius of an elliptical feature.
     * @param value The major radius in meters. The absolute value must be >= 1.0. Otherwise an InvalidParameter is raised.
     */
    @Override
    public void setSemiMajor(final double value) {
        if(isValidSemiMajor(value)) {
            this.getRenderable().setSemiMajor(value);
        } else {
            throw new IllegalArgumentException("Invalid semimajor, " + String.valueOf(value) + " Minimum supported " + MINIMUM_SEMI_MAJOR);
        }
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
     * @param value The minor radius in meters. An InvalidPaameterException is raised if the absolute value is less than 1.0.
     */
    @Override
    public void setSemiMinor(final double value) {
        if(isValidSemiMinor(value)) {
            this.getRenderable().setSemiMinor(value);
        } else {
            throw new IllegalArgumentException("Invalid semiminor, " + String.valueOf(value) + " Minimum supported " + MINIMUM_SEMI_MINOR);
        }
    }

    /**
     * This method retrieves the minor radius of an elliptical feature
     * @return The minor radius.
     */
    @Override
    public double getSemiMinor() {
        return this.getRenderable().getSemiMinor();
    }

    private boolean isValidSemiMajor(final double dMajor) {
        if(isPositive(dMajor)) {
            if(dMajor < MINIMUM_SEMI_MAJOR) {
                return false;
            }
        }
        return true;
    }

    private boolean isValidSemiMinor(final double dMinor) {
        if(isPositive(dMinor)) {
            if(dMinor < MINIMUM_SEMI_MINOR) {
                return false;
            }
        }
        return true;
    }

    public IEmpBoundingBox getFeatureBoundingBox() {
        IEmpBoundingBox bBox = null;
        List<IGeoPosition> posList = getPositions();

        if ((null != posList) && !posList.isEmpty()) {
            double majorAxis = this.getSemiMajor();
            double minorAxis = this.getSemiMinor();

            if (!Double.isNaN(majorAxis) && !Double.isNaN(minorAxis)) {
                double angle = this.getAzimuth();
                bBox = new EmpBoundingBox();
                IGeoPosition pos = new GeoPosition();

                // Compute north.
                GeoLibrary.computePositionAt(angle, minorAxis, posList.get(0), pos);
                bBox.includePosition(pos.getLatitude(), pos.getLongitude());

                // Compute east.
                angle = this.getAzimuth() + 90.0;
                angle = (((angle + 360.0) % 360.0) + 360.0) % 360.0;
                GeoLibrary.computePositionAt(angle, majorAxis, posList.get(0), pos);
                bBox.includePosition(pos.getLatitude(), pos.getLongitude());

                // Compute south.
                angle = this.getAzimuth() + 180.0;
                angle = (((angle + 360.0) % 360.0) + 360.0) % 360.0;
                GeoLibrary.computePositionAt(angle, minorAxis, posList.get(0), pos);
                bBox.includePosition(pos.getLatitude(), pos.getLongitude());

                // Compute west.
                angle = this.getAzimuth() + 270.0;
                angle = (((angle + 360.0) % 360.0) + 360.0) % 360.0;
                GeoLibrary.computePositionAt(angle, majorAxis, posList.get(0), pos);
                bBox.includePosition(pos.getLatitude(), pos.getLongitude());

                // Now we need to extend the box by ~ 10%.
                double deltaLat = bBox.deltaLatitude();
                double deltaLong = bBox.deltaLongitude();

                if (deltaLat == 0.0) {
                    deltaLat = 0.05;
                }
                if (deltaLong == 0.0) {
                    deltaLong = 0.05;
                }

                deltaLat *= 0.05;
                deltaLong *= 0.05;

                bBox.includePosition(bBox.getNorth() + deltaLat, bBox.getWest());
                bBox.includePosition(bBox.getSouth() - deltaLat, bBox.getWest());
                bBox.includePosition(bBox.getNorth(), bBox.getWest() - deltaLong);
                bBox.includePosition(bBox.getNorth(), bBox.getEast() + deltaLong);
            }
        }
        return bBox;
    }

    /**
     *
     * @return String gives ellipse parameters
     */

    @Override
    public String toString() {
        if (getPosition() != null) {
            return "Ellipse at\n" +
                    "\tlatitude: " + getPosition().getLatitude() + "\n" +
                    "\tlongitude: " + getPosition().getLongitude() + "\n" +
                    "\taltitude: " + getPosition().getAltitude() + "\n" +
                    "\tsemi-major: " + getSemiMajor() + "\n" +
                    "\tsemi-minor: " + getSemiMinor() + "\n" +
                    "\tazimuth: " + getAzimuth() + "\n";
        } else {
            return "Ellipse \n" +
                    "\tsemi-major: " + getSemiMajor() + "\n" +
                    "\tsemi-minor: " + getSemiMinor() + "\n" +
                    "\tazimuth: " + getAzimuth() + "\n";
        }
    }
}
