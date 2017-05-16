package mil.emp3.api;

import mil.emp3.api.abstracts.Feature;

import org.cmapi.primitives.GeoCircle;
import org.cmapi.primitives.GeoPosition;
import org.cmapi.primitives.IGeoCircle;
import org.cmapi.primitives.IGeoPosition;

import java.util.List;

import mil.emp3.api.enums.FeatureTypeEnum;
import mil.emp3.api.interfaces.IEmpBoundingBox;
import mil.emp3.api.utils.EmpBoundingBox;
import mil.emp3.api.utils.GeoLibrary;

/**
 * This class implements a Circle feature. It requires a radius and a single coordinate that indicates the
 * geographic position of the center of the circle.
 */
public class Circle extends Feature<IGeoCircle> implements IGeoCircle {
    public final static double MINIMUM_RADIUS = global.MINIMUM_DISTANCE;

    /**
     * This constructor creates a Circle feature with a default radius.
     */
    public Circle() {
        super(new GeoCircle(), FeatureTypeEnum.GEO_CIRCLE);
        this.setFillStyle(null);
    }

    /**
     * This constructor creates a circle with the indicated radius.
     * @param radius The required radius in meters. If the radius is < 0 the absolute value is used. If the value is < 1.0, the default radius is used.
     */
    public Circle(double radius) {
        super(new GeoCircle(), FeatureTypeEnum.GEO_CIRCLE);
        radius = makePositive(radius, "Invalid radius. NaN");

        if (radius >= MINIMUM_RADIUS) {
            this.getRenderable().setRadius(radius);
        } else {
            throw new IllegalArgumentException("Invalid radius. " + radius + " Minimum supported " + MINIMUM_RADIUS);
        }
        this.setFillStyle(null);
    }

    /**
     * This constructor create a circle feature based on the GeoCircle object provided.
     * @param renderable see IGeoCircle. If the GeoCircle does not have a radius it is set to the default radius.
     */
    public Circle(IGeoCircle renderable) {
        super(renderable, FeatureTypeEnum.GEO_CIRCLE);

        if(null == renderable) {
            throw new IllegalArgumentException("Encapsulated GeoCircle must be non-null");
        }
        this.setRadius(makePositive(this.getRadius(), "Invalid radius. NaN"));

        if (this.getRadius() < MINIMUM_RADIUS) {
            throw new IllegalArgumentException("Invalid radius. " + this.getRadius() + " Minimum supported " + MINIMUM_RADIUS);
        }
    }

    /**
     * This method overrides the default radius of a circular feature.
     * @param radius The new radius in meters. If the radius is < 0 the absolute value is used. If the value is < 1, an IllegalArgumentException is raised..
     */
    @Override
    public void setRadius(double radius) {
        radius = makePositive(radius, "Invalid radius. NaN");

        if (radius < MINIMUM_RADIUS) {
            throw new IllegalArgumentException("Invalid radius. " + radius + " Minimum supported " + MINIMUM_RADIUS);
        }
        this.getRenderable().setRadius(radius);
    }

    /**
     * This method retrieves the current radius setting.
     * @return The current radius in meters.
     */
    @Override
    public double getRadius() {
        return this.getRenderable().getRadius();
    }

    public IEmpBoundingBox getFeatureBoundingBox() {
        IEmpBoundingBox bBox = null;
        List<IGeoPosition> posList = getPositions();

        if ((null != posList) && !posList.isEmpty()) {
            double dist = this.getRadius();

            if (!Double.isNaN(dist)) {
                bBox = new EmpBoundingBox();
                IGeoPosition pos = new GeoPosition();

                // Compute north.
                GeoLibrary.computePositionAt(0.0, dist, posList.get(0), pos);
                bBox.includePosition(pos.getLatitude(), pos.getLongitude());
                // Compute east.
                GeoLibrary.computePositionAt(90.0, dist, posList.get(0), pos);
                bBox.includePosition(pos.getLatitude(), pos.getLongitude());
                // Compute south.
                GeoLibrary.computePositionAt(180.0, dist, posList.get(0), pos);
                bBox.includePosition(pos.getLatitude(), pos.getLongitude());
                // Compute west.
                GeoLibrary.computePositionAt(270.0, dist, posList.get(0), pos);
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
     * @return String gives circle parameters
     */

    @Override
    public String toString() {
        if (getPosition() != null) {
            return "Circle at\n" +
                    "\tlatitude: " + getPosition().getLatitude() + "\n" +
                    "\tlongitude: " + getPosition().getLongitude() + "\n" +
                    "\taltitude: " + getPosition().getAltitude() + "\n" +
                    "\tradius: " + getRadius() + "\n" +
                    "\tazimuth: " + getAzimuth() + "\n";
        } else {
            return "Circle\n" +
                    "\tradius: " + getRadius() + "\n" +
                    "\tazimuth: " + getAzimuth() + "\n";
        }
    }
}
