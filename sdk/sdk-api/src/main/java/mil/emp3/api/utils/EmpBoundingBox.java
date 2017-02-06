package mil.emp3.api.utils;

import org.cmapi.primitives.IGeoPosition;

import java.security.InvalidParameterException;

/**
 * This class defines a bounded box.
 */

public class EmpBoundingBox {
    private double north = 0;
    private double south = 0;
    private double east = 0;
    private double west = 0;

    /**
     * This constructor defines an area bounded by the north, south, east, and west parameters provided.
     * @param northValue The most northern latitude of the area. If this value is less than southValue, the values are swapped.
     * @param southValue The most southern latitude of the area. If this value is greater than northValue, the values are swapped.
     * @param eastValue The east longitude of the area. If the east value is greater than the west value the area contains the international date line.
     * @param westValue The west longitude of the area. If the west value is less than the east value the area contains the international date line.
     */
    public EmpBoundingBox(double northValue, double southValue, double eastValue, double westValue) {
        if (Double.isNaN(northValue) || Double.isNaN(southValue) || Double.isNaN(eastValue) || Double.isNaN(westValue)) {
            throw new InvalidParameterException("Invalid value NaN.");
        }

        if ((northValue < -90) || (northValue > 90) || (southValue < -90) || (southValue > 90)) {
            throw new InvalidParameterException("Invalid latitude value.");
        }

        if ((eastValue < -180) || (eastValue > 180) || (westValue < -180) || (westValue > 180)) {
            throw new InvalidParameterException("Invalid longitude value.");
        }

        if (northValue >= southValue) {
            this.north = northValue;
            this.south = southValue;
        } else {
            this.south = northValue;
            this.north = southValue;
        }
        this.east = eastValue;
        this.west = westValue;
    }

    /**
     * This constructor defines an area bounded by the northEast and southWest coordinates.
     * @param southWest The coordinates of the south west point of the bounding area.
     * @param northEast The coordinates of the north east point of the bounding area.
     */
    public EmpBoundingBox(IGeoPosition southWest, IGeoPosition northEast) {
        if ((null == southWest) || (null == northEast)) {
            throw new InvalidParameterException("Invalid null GeoPosition.");
        }
        if (northEast.getLatitude() >= southWest.getLatitude()) {
            this.north = northEast.getLatitude();
            this.south = southWest.getLatitude();
        } else {
            this.south = northEast.getLatitude();
            this.north = southWest.getLatitude();
        }

        this.east = northEast.getLongitude();
        this.west = southWest.getLongitude();
    }

    /**
     * This method returns the northern latitude.
     * @return The north latitude in degrees.
     */
    public double north() {
        return this.north;
    }

    /**
     * This method returns the southern latitude.
     * @return The southern latitude in degrees.
     */
    public double south() {
        return this.south;
    }

    /**
     * This method returns the eastern longitude.
     * @return The eastern longitude in degrees.
     */
    public double east() {
        return this.east;
    }

    /**
     * This method returns the western longitude.
     * @return The western logintude in degrees.
     */
    public double west() {
        return this.west;
    }

    /**
     * This method returns the delta of the north and south latitudes.
     * @return Degrees
     */
    public double deltaLatitude() {
        return Math.abs(this.north - this.south);
    }

    /**
     * This method returns the delta between the west and east logitudes.
     * @return degrees.
     */
    public double deltaLongitude() {
        double delta = this.east - this.west;

        if (delta < 0) {
            delta = (delta + 360) % 360;
        }
        return delta;
    }

    /**
     * This method check if the international date line is within the bounding box.
     * @return True if the IDL is contained in the bounding box, false if not.
     */
    public boolean containsIDL() {
        return (this.west > this.east);
    }
}
