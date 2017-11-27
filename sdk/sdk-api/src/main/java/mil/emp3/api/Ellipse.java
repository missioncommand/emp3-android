package mil.emp3.api;


import org.cmapi.primitives.GeoEllipse;
import org.cmapi.primitives.GeoPosition;
import org.cmapi.primitives.IGeoEllipse;
import org.cmapi.primitives.IGeoPosition;

import java.util.List;

import mil.emp3.api.abstracts.Feature;
import mil.emp3.api.enums.FeatureTypeEnum;
import mil.emp3.api.interfaces.IEmpBoundingBox;
import mil.emp3.api.utils.EmpBoundingBox;
import mil.emp3.api.utils.GeographicLib;

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
    public Ellipse(double dMajorRadius, double dMinorRadius) {
        super(new GeoEllipse(), FeatureTypeEnum.GEO_ELLIPSE);
        dMajorRadius = makePositive(dMajorRadius, "Invalid major radius. NaN");
        dMinorRadius = makePositive(dMinorRadius, "Invalid minor radius. NaN");

        if((dMajorRadius < MINIMUM_SEMI_MAJOR) || (dMinorRadius < MINIMUM_SEMI_MINOR)) {
            throw new IllegalArgumentException("Invalid Major or Minor. " + dMajorRadius + " " + dMinorRadius +
                    " Minimum supported " + MINIMUM_SEMI_MAJOR + " " + MINIMUM_SEMI_MINOR);
        }
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
    public Ellipse(double dMajorRadius, double dMinorRadius, double dAzimuth) {
        super(new GeoEllipse(), FeatureTypeEnum.GEO_ELLIPSE);
        dMajorRadius = makePositive(dMajorRadius, "Invalid major radius. NaN");
        dMinorRadius = makePositive(dMinorRadius, "Invalid minor radius. NaN");

        if((dMajorRadius < MINIMUM_SEMI_MAJOR) || (dMinorRadius < MINIMUM_SEMI_MINOR)) {
            throw new IllegalArgumentException("Invalid Major or Minor. " + dMajorRadius + " " + dMinorRadius +
                    " Minimum supported " + MINIMUM_SEMI_MAJOR + " " + MINIMUM_SEMI_MINOR);
        }

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
        this.setSemiMajor(makePositive(this.getSemiMajor(), "Invalid Semi Major NaN"));
        this.setSemiMinor(makePositive(this.getSemiMinor(), "Invalid Semi Minor NaN"));

        if((this.getSemiMajor() < MINIMUM_SEMI_MAJOR) || (this.getSemiMinor() < MINIMUM_SEMI_MINOR)) {
            throw new IllegalArgumentException("Invalid Major or Minor. " + this.getSemiMajor() + " " + this.getSemiMinor() +
                    " Minimum supported " + MINIMUM_SEMI_MAJOR + " " + MINIMUM_SEMI_MINOR);
        }

        this.setAzimuth(this.getAzimuth());  // For validation
    }

    /**
     * This method sets the major radius of an elliptical feature.
     * @param value The major radius in meters. The absolute value must be >= 1.0. Otherwise an InvalidParameter is raised.
     */
    @Override
    public void setSemiMajor(double value) {
        value = makePositive(value, "Semi Major is NaN");

        if (value < MINIMUM_SEMI_MAJOR) {
            throw new IllegalArgumentException("Semi Major must be >= 1.0");
        }
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
     * @param value The minor radius in meters. An IllegalArgumentException is raised if the absolute value is less than 1.0.
     */
    @Override
    public void setSemiMinor(double value) {
        value = makePositive(value, "Semi Minor is NaN");

        if (value < MINIMUM_SEMI_MINOR) {
            throw new IllegalArgumentException("The Semi Minor must be >= 1.0.");
        }
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
                GeographicLib.computePositionAt(angle, minorAxis, posList.get(0), pos);
                bBox.includePosition(pos.getLatitude(), pos.getLongitude());

                // Compute east.
                angle = this.getAzimuth() + 90.0;
                angle = (((angle + 360.0) % 360.0) + 360.0) % 360.0;
                GeographicLib.computePositionAt(angle, majorAxis, posList.get(0), pos);
                bBox.includePosition(pos.getLatitude(), pos.getLongitude());

                // Compute south.
                angle = this.getAzimuth() + 180.0;
                angle = (((angle + 360.0) % 360.0) + 360.0) % 360.0;
                GeographicLib.computePositionAt(angle, minorAxis, posList.get(0), pos);
                bBox.includePosition(pos.getLatitude(), pos.getLongitude());

                // Compute west.
                angle = this.getAzimuth() + 270.0;
                angle = (((angle + 360.0) % 360.0) + 360.0) % 360.0;
                GeographicLib.computePositionAt(angle, majorAxis, posList.get(0), pos);
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
