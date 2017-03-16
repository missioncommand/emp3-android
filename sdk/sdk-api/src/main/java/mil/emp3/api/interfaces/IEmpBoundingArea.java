package mil.emp3.api.interfaces;

import org.cmapi.primitives.IGeoBounds;
import org.cmapi.primitives.IGeoPosition;

import mil.emp3.api.utils.EmpBoundingBox;

/**
 * When map view changes i.e. pan/zoom/tilt/rotate/heading we recalculate the quadrilateral that approximates visible geographical area
 * of the map view. When the entire view is occupied by the map we can get the exact geographical coordinates of the four corners. When the map
 * shows sky/space then we provide the best possible geographical coordinates. SEC Military Symbol render-er is one known customer of this
 * capability.
 *
 * The vertices represent a polygon/quadrilateral and they are stored in clockwise order. You cannot associate any specific vertex to any specific
 * corner of the view. The class guarantees that all four vertices are non-null and have valid latitude/longitude values. Altitude is always set to zero.
 *
 * Class also provides a capability to to convert (approximately) these four vertices to GeoBounds object i.e. North, East, West and South
 * boundaries. Application is NOT allowed to invoke any setter methods of the underlying GeoBounds object, an exception will be thrown.
 *
 * If you are interested in the logic used to generate this object then please refer to BoundsGeneration class in mapengine-worldwind-apk project.
 */

public interface IEmpBoundingArea extends IGeoBounds {

    /**
     * Converts vertices to a String to a format required by Military Symbol render-er.
     * @return
     */
    String toString();

    /**
     * Fetch the four vertices. We are cloning the vertices and returning. Application will not be allowed to change this object.
     * @return
     */
    IGeoPosition[] getBoundingVertices();

    /**
     * Builds a GeoBounds object using the calculated boundaries.
     * @return
     */
    IGeoBounds getGeoBounds();

    /**
     * Builds a EmpBoundingBox using the calculated boundaries.
     * @return
     */
    EmpBoundingBox getEmpBoundingBox();

    /**
     * Gets geographic center of the vertices of the Bounding Area
     */
    IGeoPosition getCenter();

    /**
     * returns true if camera position is visible on the screen.
     * @return
     */
    boolean cameraPositionIsVisible();

    /**
     * Returns geographic position corresponding to geometric center of the vertices.
     * @return
     */
    IGeoPosition getGeometricCenter();
}
