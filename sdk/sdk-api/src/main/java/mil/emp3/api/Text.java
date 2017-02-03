package mil.emp3.api;

import mil.emp3.api.abstracts.Feature;
import mil.emp3.api.enums.FeatureTypeEnum;

import org.cmapi.primitives.GeoText;
import org.cmapi.primitives.IGeoText;

/**
 * This class positions text on the map at a specific coordinate.
 */
public class Text extends Feature<IGeoText> implements IGeoText {

    /**
     * This constructor creates a Text feature with all default values.
     */
    public Text() {
        super(new GeoText(), FeatureTypeEnum.GEO_TEXT);
    }

    /**
     * Thsi constructor create a Text feature and sets the Text to the value provided.
     * @param text
     */
    public Text(String text) {
        super(new GeoText(), FeatureTypeEnum.GEO_TEXT);
        this.setName(text);
    }

    /**
     * This constructor createa a Text feature based on the GeoText object provided.
     * @param geoText
     */
    public Text(IGeoText geoText) {
        super(geoText, FeatureTypeEnum.GEO_TEXT);
    }

    /**
     * This method retrieves the features text. It is equivalent to calling the object getName method.
     * @return String
     */
    public String getText()
    {
        return this.getName();
    }

    /**
     * This method sets the features text. It is equivalent to calling the object setName method.
     * @param value the Text to set.
     */
    public void setText(String value) {
        this.setName(value);
    }

    /**
     * This method retrieves the text rotation angle.
     * @return The angle in degrees.
     */
    public double getRotationAngle() {
        return this.getAzimuth();
    }

    /**
     * This method set the text rotation angle. The text will be rotated about the geographic coordinate
     * point by the angle provided. A value of 0 degrees renders the text parallel to the latitudinal lines on the globe.
     * A value of 90 degrees renders the text parallel to the longitudinal lines on the globe. the rotation
     * is performed clockwise.
     * @param value The rotation angle in degrees.
     */
    public void setRotationAngle(double value) {
        this.setAzimuth(value);
    }
}

