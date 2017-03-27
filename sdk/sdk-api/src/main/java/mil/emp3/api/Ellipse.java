package mil.emp3.api;


import mil.emp3.api.abstracts.Feature;

import org.cmapi.primitives.GeoEllipse;
import org.cmapi.primitives.GeoPosition;
import org.cmapi.primitives.IGeoEllipse;
import org.cmapi.primitives.IGeoPosition;
import org.cmapi.primitives.IGeoStrokeStyle;
import org.xmlpull.v1.XmlSerializer;

import java.io.IOException;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;

import mil.emp3.api.enums.FeatureTypeEnum;
import mil.emp3.api.interfaces.IEmpBoundingBox;
import mil.emp3.api.utils.EmpBoundingBox;
import mil.emp3.api.utils.GeoLibrary;
import mil.emp3.api.utils.kml.EmpKMLExporter;

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
            throw new InvalidParameterException("Invalid Major or Minor. " + dMajorRadius + " " + dMinorRadius +
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
            throw new InvalidParameterException("Invalid Major or Minor. " + dMajorRadius + " " + dMinorRadius +
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
            throw new InvalidParameterException("Encapsulated GeoEllipse must be non-null");
        }
        this.setSemiMajor(makePositive(this.getSemiMajor(), "Invalid Semi Major NaN"));
        this.setSemiMinor(makePositive(this.getSemiMinor(), "Invalid Semi Minor NaN"));

        if((this.getSemiMajor() < MINIMUM_SEMI_MAJOR) || (this.getSemiMinor() < MINIMUM_SEMI_MINOR)) {
            throw new InvalidParameterException("Invalid Major or Minor. " + this.getSemiMajor() + " " + this.getSemiMinor() +
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
            throw new InvalidParameterException("Semi Major must be >= 1.0");
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
     * @param value The minor radius in meters. An InvalidPaameterException is raised if the absolute value is less than 1.0.
     */
    @Override
    public void setSemiMinor(double value) {
        value = makePositive(value, "Semi Minor is NaN");

        if (value < MINIMUM_SEMI_MINOR) {
            throw new InvalidParameterException("The Semi Minor must be >= 1.0.");
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

    public List<IGeoPosition> getPolygonPositionList() {
        double semiMajor = this.getSemiMajor();
        double semiMinor = this.getSemiMinor();
        double azimuth = this.getAzimuth();
        double deltaBearing = Math.toDegrees(Math.atan2(0.005, 1.0));
        List<IGeoPosition> posList = new ArrayList<>();
        IGeoPosition center = this.getPosition();
        IGeoPosition zerozero = new GeoPosition();
        IGeoPosition pos1, pos, tempPos;
        int pointsPerQuadrant;
        double radius;
        double aE2 = semiMajor * semiMajor;
        double bE2 = semiMinor * semiMinor;
        double aE2bE2 = aE2 * bE2;
        double sin;
        double cos;

        zerozero.setLatitude(0.0);
        zerozero.setLongitude(0.0);

        // Generate the coordinates for the perimeter.
        pos1 = GeoLibrary.computePositionAt(0, semiMinor, zerozero);
        posList.add(pos1);

        // Create position for the top right quadrant.
        for (double bearing = deltaBearing; bearing < 90.0; bearing += deltaBearing) {
            sin = Math.sin(Math.toRadians(bearing));
            cos = Math.cos(Math.toRadians(bearing));
            radius = Math.sqrt(aE2bE2 / ((bE2 * sin * sin) + (aE2 * cos * cos)));

            if (azimuth != 0.0) {
                pos = GeoLibrary.computePositionAt(((((bearing + azimuth + 180.0) % 360.0) + 360.0) % 360.0) - 180.0, radius, zerozero);
            } else {
                pos = GeoLibrary.computePositionAt(bearing, radius, zerozero);
            }
            posList.add(pos);
        }

        pointsPerQuadrant = posList.size();

        // Now shadow the top right quadrant onto the bottom right quadrant and offset it by the center coordinate.
        for (int iIndex = pointsPerQuadrant - 1; iIndex >= 0; iIndex--) {
            tempPos = posList.get(iIndex);
            pos = new GeoPosition();
            pos.setLatitude((tempPos.getLatitude() * -1.0) + center.getLatitude());
            pos.setLongitude(tempPos.getLongitude() + center.getLongitude());
            posList.add(pos);
        }

        // Now shadow the top right quadrant onto the bottom left quadrant and offset it by the center coordinate.
        for (int iIndex = 0; iIndex < pointsPerQuadrant; iIndex++) {
            tempPos = posList.get(iIndex);
            pos = new GeoPosition();
            pos.setLatitude((tempPos.getLatitude() * -1.0) + center.getLatitude());
            pos.setLongitude((tempPos.getLongitude() * -1.0) + center.getLongitude());
            posList.add(pos);
        }

        // Now shadow the top right quadrant onto the top left quadrant and offset it by the center coordinate.
        for (int iIndex = pointsPerQuadrant - 1; iIndex >= 0; iIndex--) {
            tempPos = posList.get(iIndex);
            pos = new GeoPosition();
            pos.setLatitude(tempPos.getLatitude() + center.getLatitude());
            pos.setLongitude((tempPos.getLongitude() * -1.0) + center.getLongitude());
            posList.add(pos);
        }

        // Now offset the top right quadrant by the center coordinate.
        for (int iIndex = 0; iIndex < pointsPerQuadrant; iIndex++) {
            pos = posList.get(iIndex);
            pos.setLatitude(pos.getLatitude() + center.getLatitude());
            pos.setLongitude(pos.getLongitude() + center.getLongitude());
        }

        return posList;
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
}
