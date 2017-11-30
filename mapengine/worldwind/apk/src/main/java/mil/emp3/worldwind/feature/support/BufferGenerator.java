package mil.emp3.worldwind.feature.support;

import android.util.Log;

import org.cmapi.primitives.IGeoPosition;

import java.util.ArrayList;
import java.util.List;

import mil.emp3.api.Circle;
import mil.emp3.api.Ellipse;
import mil.emp3.api.Path;
import mil.emp3.api.Point;
import mil.emp3.api.Polygon;
import mil.emp3.api.Rectangle;
import mil.emp3.api.Square;
import mil.emp3.api.interfaces.IFeature;
import mil.emp3.api.utils.GeographicLib;
import mil.emp3.worldwind.MapInstance;

/**
 * Utility methods to generate a buffer around a specified Feature.
 */
public class BufferGenerator {
    private static String TAG = BufferGenerator.class.getSimpleName();
    /**
     * Generates a buffer around the Polygon Feature as follows:
     *     Calculates the center of the polygon.
     *     For each vertex of the polygon:
     *         Find the bearing from center to the vertex.
     *         Compute the distance to the vertex from the center
     *         Compute new distance by adding the distance specified in the buffer
     *         Find the new vertex at that new distance but with same bearing.
     *     Create a Polygon and add the newly created positions, set the Fill Style, and create WW polygon
     * @param buffer The buffer distance in meters.
     * @return
     */
    public static Polygon generateBufferPolygon(IFeature targetFeature, MapInstance mapInstance, double buffer) {
        try {
            if(targetFeature instanceof Rectangle) {
                return generateBufferPolygon((Rectangle) targetFeature, mapInstance, buffer);
            } else if(targetFeature instanceof Square) {
                return generateBufferPolygon((Square) targetFeature, mapInstance, buffer);
            } else if(targetFeature instanceof Ellipse) {
                return generateBufferPolygon((Ellipse) targetFeature, mapInstance, buffer);
            } else if(targetFeature instanceof Circle) {
                return generateBufferPolygon((Circle) targetFeature, mapInstance, buffer);
            } else if(targetFeature instanceof Polygon) {
                return generateBufferPolygon((Polygon) targetFeature, mapInstance, buffer);
            } else if(targetFeature instanceof Point) {
                return generateBufferPolygon((Point) targetFeature, mapInstance, buffer);
            } else if(targetFeature instanceof Path) {
                return generateBufferPolygon((Path) targetFeature, mapInstance, buffer);
            } else {
                Log.e(TAG,targetFeature.getClass().getSimpleName() + " NOT supported");
                return null;
            }
        } catch(Exception e) {
            Log.e(TAG, "generateBuffer buffer " + buffer, e);
        }
        return null;
    }

    /**
     * Generates a buffer around the Rectangle Feature as follows:
     *    Creates a new Rectangle object, copies all attributes adding the buffer value to height to width
     *    Sets the Fill Style
     *    Invokes the SEC renderer to create a Polygon
     * @param targetFeature
     * @param mapInstance
     * @param buffer The buffer distance in meters.
     * @return
     */
    public static Polygon generateBufferPolygon(Rectangle targetFeature, MapInstance mapInstance, double buffer) {
        try {
            Rectangle bufferRectangle = new Rectangle();
            bufferRectangle.setWidth(targetFeature.getWidth() + (2 * buffer));
            bufferRectangle.setHeight(targetFeature.getHeight() + (2 * buffer));
            bufferRectangle.setAzimuth(targetFeature.getAzimuth());
            bufferRectangle.setAltitudeMode(targetFeature.getAltitudeMode());
            bufferRectangle.getPositions().addAll(targetFeature.getPositions());
            bufferRectangle.setFillStyle( mapInstance.getEmpResources().getBufferFillStyle(mapInstance));
            java.util.List<IFeature> featureList = mapInstance.getMilStdRenderer().getFeatureRenderableShapes(mapInstance, bufferRectangle, false);

            if((null == featureList) || (1 != featureList.size()) || (!(featureList.get(0) instanceof Polygon))) {
                Log.e(TAG,"SEC Renderer didn't return a Polygon");
                return null;
            }

            return (Polygon) featureList.get(0);
        } catch(Exception e) {
            Log.e(TAG, "generateBufferPolygon Rectangle buffer " + buffer, e);
        }
        return null;
    }

    /**
     * Generates a buffer around the Square Feature as follows:
     *    Creates a new Square object, copies all attributes adding the buffer value to width
     *    Sets the Fill Style
     *    Invokes the SEC renderer to create a Polygon
     * @param targetFeature
     * @param mapInstance
     * @param buffer The buffer distance in meters.
     * @return
     */

    public static Polygon generateBufferPolygon(Square targetFeature, MapInstance mapInstance, double buffer) {
        try {
            Square bufferSquare = new Square();
            bufferSquare.setWidth(targetFeature.getWidth() + (2 *buffer));
            bufferSquare.setAzimuth(targetFeature.getAzimuth());
            bufferSquare.setAltitudeMode(targetFeature.getAltitudeMode());
            bufferSquare.getPositions().addAll(targetFeature.getPositions());
            bufferSquare.setFillStyle( mapInstance.getEmpResources().getBufferFillStyle(mapInstance));
            java.util.List<IFeature> featureList = mapInstance.getMilStdRenderer().getFeatureRenderableShapes(mapInstance, bufferSquare, false);

            if((null == featureList) || (1 != featureList.size()) || (!(featureList.get(0) instanceof Polygon))) {
                Log.e(TAG,"SEC Renderer didn't return a Polygon");
                return null;
            }

            return (Polygon) featureList.get(0);
        } catch(Exception e) {
            Log.e(TAG, "generateBufferPolygon Square buffer " + buffer, e);
        }
        return null;
    }

    /**
     * Generates a buffer around the Circle Feature as follows:
     *    Creates a new Circle object, copies all attributes adding the buffer value to radius
     *    Sets the Fill Style
     *    Invokes the SEC renderer to create a Polygon
     * @param targetFeature
     * @param mapInstance
     * @param buffer The buffer distance in meters.
     * @return
     */

    public static Polygon generateBufferPolygon(Circle targetFeature, MapInstance mapInstance, double buffer) {
        try {
            Circle bufferCircle = new Circle();
            bufferCircle.setRadius(targetFeature.getRadius() + buffer);
            bufferCircle.setAltitudeMode(targetFeature.getAltitudeMode());
            bufferCircle.getPositions().addAll(targetFeature.getPositions());
            bufferCircle.setFillStyle( mapInstance.getEmpResources().getBufferFillStyle(mapInstance));
            java.util.List<IFeature> featureList = mapInstance.getMilStdRenderer().getFeatureRenderableShapes(mapInstance, bufferCircle, false);

            if((null == featureList) || (1 != featureList.size()) || (!(featureList.get(0) instanceof Polygon))) {
                Log.e(TAG,"SEC Renderer didn't return a Polygon");
                return null;
            }

            return (Polygon) featureList.get(0);
        } catch(Exception e) {
            Log.e(TAG, "generateBufferPolygon Circle buffer " + buffer, e);
        }
        return null;
    }

    /**
     * Generates a buffer around the Ellipse Feature as follows:
     *    Creates a new Ellipse object, copies all attributes adding the buffer value to major/minor axis
     *    Sets the Fill Style
     *    Invokes the SEC renderer to create a Polygon
     * @param targetFeature
     * @param mapInstance
     * @param buffer The buffer distance in meters.
     * @return
     */

    public static Polygon generateBufferPolygon(Ellipse targetFeature, MapInstance mapInstance, double buffer) {
        try {
            Ellipse bufferEllipse = new Ellipse();
            bufferEllipse.setSemiMinor(targetFeature.getSemiMinor() + buffer);
            bufferEllipse.setSemiMajor(targetFeature.getSemiMajor() + buffer);
            bufferEllipse.setAzimuth(targetFeature.getAzimuth());
            bufferEllipse.setAltitudeMode(targetFeature.getAltitudeMode());
            bufferEllipse.getPositions().addAll(targetFeature.getPositions());
            bufferEllipse.setFillStyle( mapInstance.getEmpResources().getBufferFillStyle(mapInstance));
            java.util.List<IFeature> featureList = mapInstance.getMilStdRenderer().getFeatureRenderableShapes(mapInstance, bufferEllipse, false);

            if((null == featureList) || (1 != featureList.size()) || (!(featureList.get(0) instanceof Polygon))) {
                Log.e(TAG,"SEC Renderer didn't return a Polygon");
                return null;
            }

            return (Polygon) featureList.get(0);
        } catch(Exception e) {
            Log.e(TAG, "generateBufferPolygon Ellipse buffer " + buffer, e);
        }
        return null;
    }

    /**
     * Generates a buffer around the Polygon Feature as follows:
     *     Calculates the center of the polygon.
     *     For each vertex of the polygon:
     *         Find the bearing from center to the vertex.
     *         Compute the distance to the vertex from the center
     *         Compute new distance by adding the distance specified in the buffer
     *         Find the new vertex at that new distance but with same bearing.
     *     Create a Polygon and add the newly created positions, set the Fill Style
     *
     * @param targetFeature
     * @param mapInstance
     * @param buffer The buffer distance in meters.
     * @return
     */
    public static Polygon generateBufferPolygon(Polygon targetFeature, MapInstance mapInstance, double buffer) {
        try {
            IGeoPosition center = GeographicLib.getCenter(targetFeature.getPositions());
            Polygon bufferPolygon = new Polygon();
            bufferPolygon.setFillStyle( mapInstance.getEmpResources().getBufferFillStyle(mapInstance));
            for(IGeoPosition vertex: targetFeature.getPositions()) {
                double bearing = GeographicLib.computeBearing(center, vertex);
                double distance = GeographicLib.computeDistanceBetween(center, vertex);
                distance += buffer;
                IGeoPosition bufferVetrex = GeographicLib.computePositionAt(bearing, distance, center);
                bufferPolygon.getPositions().add(bufferVetrex);
            }
            return(bufferPolygon);
        } catch(Exception e) {
            Log.e(TAG, "generateBufferPolygon Polygon buffer " + buffer, e);
        }
        return null;
    }

    /**
     * Generates a buffer around the Point Feature as follows:
     *    Creates a new Circle object, copies all attributes adding the buffer value to radius
     *    Sets the Fill Style
     *    Invokes the SEC renderer to create a Polygon
     * @param targetFeature
     * @param mapInstance
     * @param buffer The buffer distance in meters.
     * @return
     */

    public static Polygon generateBufferPolygon(Point targetFeature, MapInstance mapInstance, double buffer) {
        try {
            Circle bufferCircle = new Circle();
            bufferCircle.setRadius(buffer);
            bufferCircle.setAltitudeMode(targetFeature.getAltitudeMode());
            bufferCircle.getPositions().addAll(targetFeature.getPositions());
            bufferCircle.setFillStyle( mapInstance.getEmpResources().getBufferFillStyle(mapInstance));
            java.util.List<IFeature> featureList = mapInstance.getMilStdRenderer().getFeatureRenderableShapes(mapInstance, bufferCircle, false);

            if((null == featureList) || (1 != featureList.size()) || (!(featureList.get(0) instanceof Polygon))) {
                Log.e(TAG,"SEC Renderer didn't return a Polygon");
                return null;
            }

            return (Polygon) featureList.get(0);
        } catch(Exception e) {
            Log.e(TAG, "generateBufferPolygon Point buffer " + buffer, e);
        }
        return null;
    }

    /**
     * Generates a buffer around the Path Feature as follows:
     *     Calculates the center of the open polygon.
     *     For each vertex of the polygon:
     *         Find the bearing from center to the vertex.
     *         Compute the distance to the vertex from the center
     *         Compute new distance by adding the distance specified in the buffer
     *         Find the new vertex at that new distance but with same bearing.
     *         Add vertex to polygon
     *         Compute new distance by subtracting the distance specified in the buffer
     *         Add vertex to backward list
     *     Add vertices from backward list to the polygon.
     * @param targetFeature
     * @param mapInstance
     * @param buffer The buffer distance in meters.
     * @return
     */

    public static Polygon generateBufferPolygon(Path targetFeature, MapInstance mapInstance, double buffer) {
        try {
            IGeoPosition center = GeographicLib.getCenter(targetFeature.getPositions());
            Polygon bufferPolygon = new Polygon();
            bufferPolygon.setFillStyle( mapInstance.getEmpResources().getBufferFillStyle(mapInstance));
            List<IGeoPosition> backward = new ArrayList<>();
            for(IGeoPosition vertex: targetFeature.getPositions()) {
                double bearing = GeographicLib.computeBearing(center, vertex);
                double distance = GeographicLib.computeDistanceBetween(center, vertex);

                bufferPolygon.getPositions().add(GeographicLib.computePositionAt(bearing, distance+buffer, center));
                backward.add(GeographicLib.computePositionAt(bearing, distance-buffer, center));
            }

            for(int jj = backward.size(); jj > 0; jj--) {
                bufferPolygon.getPositions().add(backward.get(jj-1));
            }
            return(bufferPolygon);
        } catch(Exception e) {
            Log.e(TAG, "generateBufferPolygon Path buffer " + buffer, e);
        }
        return null;
    }
}
