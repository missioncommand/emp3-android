package mil.emp3.api;


import org.cmapi.primitives.GeoPoint;
import org.cmapi.primitives.IGeoIconStyle;
import org.cmapi.primitives.IGeoPoint;
import org.cmapi.primitives.IGeoPosition;

import mil.emp3.api.utils.kml.EmpKMLExporter;
import mil.emp3.api.enums.FeatureTypeEnum;
import mil.emp3.api.abstracts.Feature;
import mil.emp3.api.interfaces.IKMLExportable;

import org.cmapi.primitives.GeoPosition;
import org.xmlpull.v1.XmlSerializer;

import java.io.IOException;
import java.util.HashMap;

/**
 * This class implements the Point feature that encapsulates a GeoPoint object.
 */
public class Point extends Feature<IGeoPoint> implements IGeoPoint, IKMLExportable {
    private double dIconScale = 1.0;
    private int resourceId = 0;

    /**
     * this is the default constructor.
     */
    public Point() {
        super(new GeoPoint(), FeatureTypeEnum.GEO_POINT);
    }

    /**
     * This constructor creates a Point feature wherer the icon is accessed via the URL provided. The caller must
     * provide the GeoIconStyle to correctly position the icon.
     * @param sURL A valid URL
     */
    public Point(String sURL) {
        super(new GeoPoint(), FeatureTypeEnum.GEO_POINT);
        this.setIconURI(sURL);
    }

    /**
     * This constructor creates a point feature positioned at the coordinates provided.
     * @param dLat
     * @param dLong
     */
    public Point(double dLat, double dLong) {
        super(new GeoPoint(), FeatureTypeEnum.GEO_POINT);
        
        IGeoPosition oPos = new GeoPosition();
        oPos.setLatitude(dLat);
        oPos.setLongitude(dLong);
        this.setPosition(oPos);
    }

    /**
     * This constructor creates a point feature from a geo point object.
     * @param oRenderable
     */
    public Point(IGeoPoint oRenderable) {
        super(oRenderable, FeatureTypeEnum.GEO_POINT);
    }

    /**
     * This method set the image style.
     * @param oStyle See {@link IGeoIconStyle}
     */
    @Override
    public void setIconStyle(IGeoIconStyle oStyle) {
        ((IGeoPoint) this.getRenderable()).setIconStyle(oStyle);
    }

    /**
     * This method retrieves the icon style of the point if one has been set.
     * @return An {@link IGeoIconStyle} or null
     */
    @Override
    public IGeoIconStyle getIconStyle() {
        return this.getRenderable().getIconStyle();
    }

    /**
     * This method set the image URL of a point feature. It will override the default icon.
     * @param sURL The URL of the image.
     */
    @Override
    public void setIconURI(String sURL) {
        ((IGeoPoint) this.getRenderable()).setIconURI(sURL);
    }

    /**
     * This method retrieves the URL of the image
     * @return URL string.
     */
    @Override
    public String getIconURI() {
        return ((IGeoPoint) this.getRenderable()).getIconURI();
    }

    /**
     * This method sets the icon scale. The size of the icon will be modified by this factor when rendered.
     * @param dScale A value smaller than 1.0 decreases the size. A value larger than 1.0 increases the size.
     */
    public void setIconScale(double dScale) {
        this.dIconScale = Math.abs(dScale);
    }

    /**
     * This method returns the scale value.
     * @return
     */
    public double getIconScale() {
        return this.dIconScale;
    }

    /**
     * This method is an ANDROID ONLY method. It allows the developer to us an android drawable resource
     * as an icon for a Point feature. The resource must be accessible to the EMP Core. If a
     * resource Id is set the icon style offset is interpreted as a fraction.
     * @param resId The resource ID of the resource.
     */
    public void setResourceId(int resId) {
        this.resourceId = resId;
    }

    /**
     * This method is an ANDROID ONLY method. It returns the resource Id.
     * @return The resojurce Id or 0 if one has not been set.
     */
    public int getResourceId() {
        return this.resourceId;
    }

    @Override
    public String exportToKML() throws IOException {
        return callKMLExporter();
    }

    private boolean needStyle() {
        return (((null != this.getIconURI()) && !this.getIconURI().isEmpty()) || (this.getIconScale() != 1.0));
    }

    @Override
    public void exportStylesToKML(XmlSerializer xmlSerializer) throws IOException {
        if  (needStyle()){
            xmlSerializer.startTag(null, "Style");
            xmlSerializer.attribute(null, "id", EmpKMLExporter.getStyleId(this));
            xmlSerializer.startTag(null, "IconStyle");

            if (this.getIconScale() != 1.0) {
                xmlSerializer.startTag(null, "scale");
                xmlSerializer.text("" + this.getIconScale());
                xmlSerializer.endTag(null, "scale");
            }
            if ((null != this.getIconURI()) && !this.getIconURI().isEmpty()) {
                xmlSerializer.startTag(null, "Icon");
                EmpKMLExporter.serializeHRef(this.getIconURI(), xmlSerializer);
                xmlSerializer.endTag(null, "Icon");

                EmpKMLExporter.serializeIconHotSpot(this.getIconStyle(), xmlSerializer);
            }

            xmlSerializer.endTag(null, "IconStyle");
            xmlSerializer.endTag(null, "Style");
        }

        super.exportStylesToKML(xmlSerializer);
    }

    @Override
    public void exportEmpObjectToKML(XmlSerializer xmlSerializer) throws IOException {
        EmpKMLExporter.serializePlacemark(this, xmlSerializer, new EmpKMLExporter.ISerializePlacemarkGeometry() {
            @Override
            public void serializeGeometry(XmlSerializer xmlSerializer) throws IOException {
                if  (needStyle()){
                    xmlSerializer.startTag(null, "styleUrl");
                    xmlSerializer.text("#" + EmpKMLExporter.getStyleId(Point.this));
                    xmlSerializer.endTag(null, "styleUrl");
                }
                xmlSerializer.startTag(null, "Point");
                EmpKMLExporter.serializeExtrude(Point.this, xmlSerializer);
                EmpKMLExporter.serializeAltitudeMode(Point.this, xmlSerializer);
                EmpKMLExporter.serializeCoordinates(Point.this.getPositions(), xmlSerializer);
                xmlSerializer.endTag(null, "Point");
            }
        });

        super.exportEmpObjectToKML(xmlSerializer);
    }

    @Override
    protected void appendGeoJSONProperties(StringBuffer buffer) {
        buffer.append("\"properties\": ");
        buffer.append("{\"style\": ");
        buffer.append("{\"iconStyle\": ");
        buffer.append("{\"url\": ");
        buffer.append("\""+ this.getIconURI() + "\"");
        buffer.append("}"); // url
        buffer.append("}"); // iconStyle
        buffer.append("}"); // style
        buffer.append(",\"name\":");
        buffer.append("\"" + this.getName() + "\",");
        buffer.append(",\"id\":");
        buffer.append("\"" + this.getGeoId() + "\",");
        buffer.append(",\"description\":");
        buffer.append("\"" + this.getDescription() + "\"");
        buffer.append("}");
    }

    @Override
    protected void appendGeoJSONGeometry(StringBuffer buffer) {
        buffer.append("\"geometry\":  {\"type\": \"Point\",");
        buffer.append("\"coordinates\":  [");
        buffer.append(this.getPosition().getLatitude());
        buffer.append(", ");
        buffer.append(this.getPosition().getLatitude());
        buffer.append("]");// end of coordinates
        buffer.append("}");// end of geometry
    }

}
