package mil.emp3.api;

import mil.emp3.api.abstracts.Feature;

import org.cmapi.primitives.GeoCircle;
import org.cmapi.primitives.GeoPosition;
import org.cmapi.primitives.IGeoCircle;
import org.cmapi.primitives.IGeoPosition;
import org.cmapi.primitives.IGeoStrokeStyle;
import org.xmlpull.v1.XmlSerializer;

import java.io.IOException;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;

import mil.emp3.api.enums.FeatureTypeEnum;
import mil.emp3.api.utils.GeoLibrary;
import mil.emp3.api.utils.kml.EmpKMLExporter;

/**
 * This class implements a Circle feature. It requires a radius and a single coordinate that indicates the
 * geographic position of the center of the circle.
 */
public class Circle extends Feature<IGeoCircle> implements IGeoCircle {

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

        if (radius < 0) {
            radius = Math.abs(radius);
        }
        if (radius >= 1.0) {
            this.getRenderable().setRadius(radius);
        }
        this.setFillStyle(null);
    }

    /**
     * This constructor create a circle feature based on the GeoCircle object provided.
     * @param renderable see IGeoCircle. If the GeoCircle does not have a radius it is set to the default radius.
     */
    public Circle(IGeoCircle renderable) {
        super(renderable, FeatureTypeEnum.GEO_CIRCLE);

        if (this.getRadius() < 0.0) {
            // If the radius in the geo circle is negative we need to set the absolute value.
            this.setRadius(Math.abs(this.getRadius()));
        }
        if (this.getRadius() < 1.0) {
            // If the radius is between 0 and 1.0. We set it to the default radius.
            this.setRadius(100.0D);
        }
    }

    /**
     * This method overrides the default radius of a circular feature.
     * @param radius The new radius in meters. If the radius is < 0 the absolute value is used. If the value is < 1, an InvalidParameterException is raised..
     */
    @Override
    public void setRadius(double radius) {
        if (radius < 0) {
            radius = Math.abs(radius);
        }
        if (radius < 1) {
            throw new InvalidParameterException("Invalid radius.");
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

    public List<IGeoPosition> getPolygonPositionList() {
        double distanceFromCenter = this.getRadius();
        double deltaBearing = Math.toDegrees(Math.atan2(0.005, 1.0));
        List<IGeoPosition> posList = new ArrayList<>();
        IGeoPosition center = this.getPosition();
        IGeoPosition zerozero = new GeoPosition();
        IGeoPosition pos1, pos, tempPos;
        int pointsPerQuadrant;

        zerozero.setLatitude(0.0);
        zerozero.setLongitude(0.0);

        // Generate the coordinates for the perimeter.
        pos1 = GeoLibrary.computePositionAt(0, distanceFromCenter, zerozero);
        posList.add(pos1);
        // Create position for the top right quadrant.
        for (double bearing = deltaBearing; bearing < 90.0; bearing += deltaBearing) {
            pos = GeoLibrary.computePositionAt(bearing, distanceFromCenter, zerozero);
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
}
