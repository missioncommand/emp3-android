package mil.emp3.api.interfaces;

import org.cmapi.primitives.IGeoBounds;
import org.cmapi.primitives.IGeoPosition;

import mil.emp3.api.utils.EmpBoundingBox;

/**
 * This class defines the interface to an EMP bounding box.
 */

public interface IEmpBoundingBox extends IGeoBounds {
    /**
     * This method returns the northern latitude.
     * @return The southern latitude in degrees.
     */
    double north();

    /**
     * This method returns the southern latitude.
     * @return The southern latitude in degrees.
     */
    double south();

    /**
     * This method returns the eastern longitude.
     * @return The eastern longitude in degrees.
     */
    double east();

    /**
     * This method returns the western longitude.
     * @return The western logintude in degrees.
     */
    double west();

    /**
     * This method returns the delta of the north and south latitudes.
     * @return Degrees
     */
    double deltaLatitude();

    /**
     * This method returns the delta between the west and east longitudes.
     * @return degrees.
     */
    double deltaLongitude();

    /**
     * This method check if the international date line is within the bounding box.
     * @return True if the IDL is contained in the bounding box, false if not.
     */
    boolean containsIDL();

    void copyFrom(IGeoBounds geoBounds);

    void copyFrom(IEmpBoundingBox empBounds);

    boolean intersects(double northDeg, double southDeg, double eastDeg, double westDeg);

    boolean intersects(IGeoBounds bounds);

    boolean intersects(IEmpBoundingBox bounds);

    /**
     * This method returns the coordinate of the middle of the west edge of the boundary.
     * @return
     */
    IGeoPosition centerWest();

    /**
     * This method returns the coordinate of the middle of the east edge of the boundary.
     * @return
     */
    IGeoPosition centerEast();

    /**
     * This method returns the coordinate of the middle of the north edge of the boundary.
     * @return
     */
    IGeoPosition centerNorth();

    /**
     * This method returns the coordinate of the middle of the south edge of the boundary.
     * @return
     */
    IGeoPosition centerSouth();

    /**
     * This method checks if the the given lat/long is contained in the bounding box.
     * @param latitude
     * @param longitude
     * @return If if it is contained in the bounding box, false otherwise.
     */
    boolean contains(double latitude, double longitude);

    /**
     * This method returns the width in meters of the bounding box across the center.
     * @return The Width in meters.
     */
    double widthAcrossCenter();

    /**
     * This method returns the height in meters of the bounding box across the center.
     * @return The Height in meters.
     */
    double heightAcrossCenter();

    /**
     * This method returns the latitude of the center of the bounding area.
     * @return Center latitude in degrees.
     */
    double centerLatitude();

    /**
     * This method returns the longitude of the center of the bounding area.
     * @return Center longitude in degrees.
     */
    double centerLongitude();

    /**
     * This method returns the bounding box that is the intersection of this box and the box defined
     * by the with parameter. The resulting box is returned in the result parameter.
     * @param with The bounding box to intersect with.
     * @param result A bounding box to place the result.
     * @return The result parameter with the intersecting bounds or null if the two bounding boxes do not intersect.
     */
    IEmpBoundingBox intersection(IEmpBoundingBox with, IEmpBoundingBox result);

    /**
     * This method returns the bounding box that is the intersection of this box and the box defined
     * by the with parameter.
     * @param with The bounding box to intersect with.
     * @return The resulting bounding box. Or null if the two bounding boxes do not intersect.
     */
    IEmpBoundingBox intersection(IEmpBoundingBox with);

    /**
     * This method will ensure that the bounding box includes the position provided.
     * @param latitude     The latitude in degrees.
     * @param longitude    The longitude in degrees.
     */
    void includePosition(double latitude, double longitude);
}
