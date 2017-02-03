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
import mil.emp3.api.utils.GeoLibrary;
import mil.emp3.api.utils.kml.EmpKMLExporter;

/**
 * This class implements an ellipse feature. It requires a single coordinate that indicates the
 * geographic position of the center of the ellipse.
 */
public class Ellipse extends Feature<IGeoEllipse> implements IGeoEllipse {

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
    }

    /**
     * This method sets the major radius of an elliptical feature.
     * @param value The major radius in meters. The absolute value must be >= 1.0. Otherwise an InvalidParameter is raised.
     */
    @Override
    public void setSemiMajor(double value) {
        if (value < 0.0) {
            value = Math.abs(value);
        }
        if (value < 1.0D) {
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
        if (value < 0.0) {
            value = Math.abs(value);
        }
        if (value < 1.0D) {
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

    private boolean needStyle() {
        return (null != this.getStrokeStyle()) || (null != this.getFillStyle());
    }

    @Override
    public void exportStylesToKML(XmlSerializer xmlSerializer) throws IOException {
        if (this.needStyle()) {
            IGeoStrokeStyle strokeStyle = this.getStrokeStyle();

            xmlSerializer.startTag(null, "Style");
            xmlSerializer.attribute(null, "id", EmpKMLExporter.getStyleId(this));

            if (null != this.getStrokeStyle()) {
                EmpKMLExporter.serializeStrokeStyle(this.getStrokeStyle(), xmlSerializer);
            }

            if (null != this.getFillStyle()) {
                EmpKMLExporter.serializeFillStyle(this.getFillStyle(), (null != this.getStrokeStyle()), xmlSerializer);
            }
            xmlSerializer.endTag(null, "Style");
        }

        super.exportStylesToKML(xmlSerializer);
    }

    @Override
    public void exportEmpObjectToKML(XmlSerializer xmlSerializer) throws IOException {
        EmpKMLExporter.serializePlacemark(this, xmlSerializer, new EmpKMLExporter.ISerializePlacemarkGeometry() {
            @Override
            public void serializeGeometry(XmlSerializer xmlSerializer) throws IOException {
                double semiMajor = Ellipse.this.getSemiMajor();
                double semiMinor = Ellipse.this.getSemiMinor();
                double azimuth = Ellipse.this.getAzimuth();
                double deltaBearing = Math.toDegrees(Math.atan2(0.005, 1.0));
                List<IGeoPosition> posList = new ArrayList<>();
                IGeoPosition center = Ellipse.this.getPosition();
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

                    pos = GeoLibrary.computePositionAt(bearing, radius, zerozero);
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

                if  (Ellipse.this.needStyle()){
                    xmlSerializer.startTag(null, "styleUrl");
                    xmlSerializer.text("#" + EmpKMLExporter.getStyleId(Ellipse.this));
                    xmlSerializer.endTag(null, "styleUrl");
                }

                xmlSerializer.startTag(null, "Polygon");
                EmpKMLExporter.serializeExtrude(Ellipse.this, xmlSerializer);
                EmpKMLExporter.serializeAltitudeMode(Ellipse.this, xmlSerializer);
                xmlSerializer.startTag(null, "outerBoundaryIs");
                xmlSerializer.startTag(null, "LinearRing");
                EmpKMLExporter.serializeCoordinates(posList, xmlSerializer);
                xmlSerializer.endTag(null, "LinearRing");
                xmlSerializer.endTag(null, "outerBoundaryIs");
                xmlSerializer.endTag(null, "Polygon");
            }
        });

        super.exportEmpObjectToKML(xmlSerializer);
    }
}
