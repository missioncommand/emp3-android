package mil.emp3.worldwind.feature;

import android.util.Log;

import gov.nasa.worldwind.render.Renderable;
import mil.emp3.api.Point;
import mil.emp3.api.Polygon;
import mil.emp3.worldwind.MapInstance;
import mil.emp3.worldwind.feature.support.BufferGenerator;

/**
 * This class implements the mapping between an EMP Point feature and the WW placemark.
 */

public class PointFeature extends FeatureRenderableMapping<Point> {
    private static final String TAG = PointFeature.class.getSimpleName();

    public PointFeature(Point feature, MapInstance instance) {
        super(feature, instance);
    }

    /**
     * Create a Circle using buffer value as Radius
     * @param buffer The buffer distance in meters.
     * @return
     */
    protected Renderable generateBuffer(double buffer) {
        try {
            Polygon bufferPolygon = BufferGenerator.generateBufferPolygon(getFeature(), getMapInstance(), buffer);
            if(null != bufferPolygon) {
                return (this.createWWPolygon(bufferPolygon, false));
            }
        } catch(Exception e) {
            Log.e(TAG, "generateBuffer buffer " + buffer, e);
        }
        return null;
    }
}
