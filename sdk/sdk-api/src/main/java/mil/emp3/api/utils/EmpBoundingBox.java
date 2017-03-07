package mil.emp3.api.utils;

import org.cmapi.primitives.GeoBounds;
import org.cmapi.primitives.GeoPosition;
import org.cmapi.primitives.IGeoBounds;
import org.cmapi.primitives.IGeoPosition;

import java.security.InvalidParameterException;

/**
 * This class defines a bounded box.
 */

public class EmpBoundingBox extends GeoBounds {
    /**
     * This is the default constructor.
     */
    public EmpBoundingBox() {

    }

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
            this.setNorth(northValue);
            this.setSouth(southValue);
        } else {
            this.setSouth(northValue);
            this.setNorth(southValue);
        }
        this.setEast(eastValue);
        this.setWest(westValue);
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
            this.setNorth(northEast.getLatitude());
            this.setSouth(southWest.getLatitude());
        } else {
            this.setSouth(northEast.getLatitude());
            this.setNorth(southWest.getLatitude());
        }

        this.setEast(northEast.getLongitude());
        this.setWest(southWest.getLongitude());
    }

    /**
     * This method returns the northern latitude.
     * @return The north latitude in degrees.
     */
    public double north() {
        return this.getNorth();
    }

    /**
     * This method returns the southern latitude.
     * @return The southern latitude in degrees.
     */
    public double south() {
        return this.getSouth();
    }

    /**
     * This method returns the eastern longitude.
     * @return The eastern longitude in degrees.
     */
    public double east() {
        return this.getEast();
    }

    /**
     * This method returns the western longitude.
     * @return The western logintude in degrees.
     */
    public double west() {
        return this.getWest();
    }

    @Override
    public void setNorth(double value) {
        super.setNorth(((value + 90.0) % 180.0) - 90.0);
    }

    @Override
    public void setSouth(double value) {
        super.setSouth(((value + 90.0) % 180.0) - 90.0);
    }

    @Override
    public void setWest(double value) {
        super.setWest(((value + 180.0) % 360.0) - 180.0);
        if (getWest() == 180.0) {
            super.setWest(-180.0);
        }
    }

    @Override
    public void setEast(double value) {
        super.setEast(((value + 180.0) % 360.0) - 180.0);
        if (getEast() == -180.0) {
            super.setEast(180.0);
        }
    }

    /**
     * This method returns the delta of the north and south latitudes.
     * @return Degrees
     */
    public double deltaLatitude() {
        return Math.abs(this.north() - this.south());
    }

    /**
     * This method returns the delta between the west and east longitudes.
     * @return degrees.
     */
    public double deltaLongitude() {

        if (containsIDL()) {
            return (180.0 - this.west()) + (this.east() + 180.0);
        }
        return this.east() - this.west();
    }

    /**
     * This method check if the international date line is within the bounding box.
     * @return True if the IDL is contained in the bounding box, false if not.
     */
    public boolean containsIDL() {
        return (this.west() > this.east());
    }

    public void copyFrom(IGeoBounds geoBounds) {
        this.setEast(geoBounds.getEast());
        this.setSouth(geoBounds.getSouth());
        this.setWest(geoBounds.getWest());
        this.setNorth(geoBounds.getNorth());
    }

    public void copyFrom(EmpBoundingBox empBounds) {
        this.setEast(empBounds.east());
        this.setSouth(empBounds.south());
        this.setWest(empBounds.west());
        this.setNorth(empBounds.north());
    }

    public boolean intersects(double northDeg, double southDeg, double eastDeg, double westDeg) {
        if ((northDeg == Double.NaN) || (southDeg == Double.NaN) || (eastDeg == Double.NaN) || (westDeg == Double.NaN)) {
            throw new InvalidParameterException("invalid coordinate.");
        }

        boolean bRet = false;

        // If their north and south don't intersect they don't intersect.
        if ((this.south() <= northDeg) && (this.north() >= southDeg)) {
            if (this.containsIDL()) {
                // This bounding box contains the IDL.
                if (westDeg > eastDeg) {
                    // The coordinates provided also contains the IDL;
                    bRet = true;
                } else {
                    // The coordinates provided do not contains the IDL;
                    bRet = ((this.west() <= eastDeg) && (180 >= westDeg)) || ((-180 <= eastDeg) && (this.east() >= westDeg));
                }
            } else {
                // This bounding box does not contains the IDL.
                if (westDeg > eastDeg) {
                    // The coordinates provided contains the IDL;
                    bRet = ((this.getWest() <= eastDeg) && (this.getEast() <= westDeg)) || ((this.getEast() >= westDeg) && (this.getWest() >= eastDeg));
                } else {
                    // The coordinates provided do not contains the IDL;
                    bRet = ((this.getWest() <= eastDeg) && (this.getEast() >= westDeg));
                }
            }
        }
        return bRet;
    }

    public boolean intersects(IGeoBounds bounds) {
        if (bounds == null) {
            throw new InvalidParameterException("null bounds.");
        }
        return this.intersects(bounds.getNorth(), bounds.getSouth(), bounds.getEast(), bounds.getWest());
    }

    public boolean intersects(EmpBoundingBox bounds) {
        if (bounds == null) {
            throw new InvalidParameterException("null bounds.");
        }
        return this.intersects(bounds.north(), bounds.south(), bounds.east(), bounds.west());
    }

    /**
     * This method returns the coordinate of the middle of the west edge of the boundary.
     * @return
     */
    public IGeoPosition centerWest() {
        IGeoPosition pos = new GeoPosition();

        pos.setAltitude(0);
        pos.setLatitude(centerLatitude());
        pos.setLongitude(west());
        return pos;
    }

    /**
     * This method returns the coordinate of the middle of the east edge of the boundary.
     * @return
     */
    public IGeoPosition centerEast() {
        IGeoPosition pos = new GeoPosition();

        pos.setAltitude(0);
        pos.setLatitude(centerLatitude());
        pos.setLongitude(east());
        return pos;
    }

    /**
     * This method returns the coordinate of the middle of the north edge of the boundary.
     * @return
     */
    public IGeoPosition centerNorth() {
        IGeoPosition pos = new GeoPosition();

        pos.setAltitude(0);
        pos.setLatitude(getNorth());
        pos.setLongitude(centerLongitude());
        return pos;
    }

    /**
     * This method returns the coordinate of the middle of the south edge of the boundary.
     * @return
     */
    public IGeoPosition centerSouth() {
        IGeoPosition pos = new GeoPosition();

        pos.setAltitude(0);
        pos.setLatitude(getSouth());
        pos.setLongitude(centerLongitude());
        return pos;
    }

    /**
     * This method checks if the the given lat/long is contained in the bounding box.
     * @param latitude
     * @param longitude
     * @return If if it is contained in the bounding box, false otherwise.
     */
    public boolean contains(double latitude, double longitude) {
        if (latitude > this.north()) {
            return false;
        }
        if (latitude < this.south()) {
            return false;
        }

        if (this.containsIDL()) {
            if ((longitude < this.west()) && (longitude > this.east())) {
                return false;
            }
        } else if (longitude > this.east()) {
            return false;
        } else if (longitude < this.west()) {
            return false;
        }

        return true;
    }

    /**
     * This method returns the width in meters of the bounding box across the center.
     * @return The Width in meters.
     */
    public double widthAcrossCenter() {
        IGeoPosition pos1 = this.centerWest();
        IGeoPosition pos2 = this.centerEast();

        return GeoLibrary.computeRhumbDistance(pos1, pos2);
    }

    /**
     * This method returns the height in meters of the bounding box across the center.
     * @return The Height in meters.
     */
    public double heightAcrossCenter() {
        IGeoPosition pos1 = centerNorth();
        IGeoPosition pos2 = centerSouth();

        return GeoLibrary.computeDistanceBetween(pos1, pos2);
    }

    /**
     * This method returns the latitude of the center of the bounding area.
     * @return Center latitude in degrees.
     */
    public double centerLatitude() {
        return (this.north() + this.south()) / 2.0;
    }

    /**
     * This method returns the logitude of the center of the bounding area.
     * @return Center longitude in degrees.
     */
    public double centerLongitude() {
        if (this.containsIDL()) {
            return (((this.west() + (this.deltaLongitude() / 2.0)) + 180.0) % 360.0) - 180.0;
        }

        return (this.east() + this.west()) / 2.0;
    }

    /**
     * This method returns the bounding box that is the intersection of this box and the box defined
     * by the with parameter. The resulting box is returned in the result parameter.
     * @param with The bounding box to intersect with.
     * @param result A bounding box to place the result.
     * @return The result parameter with the intersecting bounds or null if the two bounding boxes do not intersect.
     */
    public EmpBoundingBox intersection(EmpBoundingBox with, EmpBoundingBox result) {
        if (null == with) {
            throw new InvalidParameterException("The with parameters can not be null.");
        }
        if (null == result) {
            throw new InvalidParameterException("The result parameters can not be null.");
        }

        if (!this.intersects(with)) {
            return null;
        }

        result.setNorth(Math.min(this.getNorth(), with.getNorth()));
        result.setSouth(Math.max(this.getSouth(), with.getSouth()));

        if (this.containsIDL()) {
            // This box contains the IDL
            if (with.containsIDL()) {
                // They both contain the IDL.
                result.setWest(Math.max(this.getWest(), with.getWest()));
                result.setEast(Math.min(this.getEast(), with.getEast()));
            } else if (with.getWest() > 0) {
                // With does not contain the IDL and it is on the left side of the IDL.
                result.setWest(Math.max(this.getWest(), with.getWest()));
                result.setEast(with.getEast());
            } else {
                // With does not contain the IDL and it is on the right side of the IDL.
                result.setWest(with.getWest());
                result.setEast(Math.min(this.getEast(), with.getEast()));
            }
        } else if (with.containsIDL()) {
            // This does not contain the IDL but With does.
            if (this.getWest() > 0) {
                // This is on the left side of the IDL
                result.setWest(Math.max(this.getWest(), with.getWest()));
                result.setEast(this.getEast());
            } else {
                // This is on the right side of the IDL
                result.setWest(this.getWest());
                result.setEast(Math.min(this.getEast(), with.getEast()));
            }
        } else {
            // This nor with contain the IDL.
            result.setWest(Math.max(this.getWest(), with.getWest()));
            result.setEast(Math.min(this.getEast(), with.getEast()));
        }

        return result;
    }

    /**
     * This method returns the bounding box that is the intersection of this box and the box defined
     * by the with parameter.
     * @param with The bounding box to intersect with.
     * @return The resulting bounding box. Or null if the two bounding boxes do not intersect.
     */
    public EmpBoundingBox intersection(EmpBoundingBox with) {
        return intersection(with, new EmpBoundingBox());
    }
}
