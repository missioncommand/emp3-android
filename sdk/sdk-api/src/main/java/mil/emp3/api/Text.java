package mil.emp3.api;

import mil.emp3.api.abstracts.Feature;
import mil.emp3.api.enums.FeatureTypeEnum;

import org.cmapi.primitives.GeoText;
import org.cmapi.primitives.IGeoText;

/**
 * This class positions text on the map at a specific coordinate.
 */
public class Text extends Feature implements IGeoText {
    // This type must be removed when it is supported by the IGeoLabelStyle
    public enum TypeFaceStyle {
        NORMAL,
        BOLD,
        ITALIC,
        BOLD_ITALIC
    }

    // This type must be removed when it is supported by the IGeoLabelStyle
    private String fontFamily = "Arial";
    // This type must be removed when it is supported by the IGeoLabelStyle
    private int fontSize = 20;
    // This type must be removed when it is supported by the IGeoLabelStyle
    private TypeFaceStyle typeFaceStyle = TypeFaceStyle.NORMAL;

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

    /**
     * This method return the font family the text is rendered with.
     * @return String
     */
    public String getFontFamily() {
        return this.fontFamily;
    }

    /**
     * This method set the font family the text is to be rendered with. The map will use the closest font available.
     * @param fFamily String
     */
    public void setFontFamily(String fFamily) {
        this.fontFamily = fFamily;
    }

    /**
     * This method retrieves the font size used to render the text.
     * @return Integer
     */
    public int getFontSize() {
        return this.fontSize;
    }

    /**
     * This method set the size of the font the map is to render the text in.
     * @param size
     */
    public void setFontSize(int size) {
        this.fontSize = size;
    }

    /**
     * This methos returns the type face style of the lable.
     * @return Text.TypeFaceStyle
     */
    public Text.TypeFaceStyle getTypeFaceStyle() {
        return this.typeFaceStyle;
    }

    /**
     * This methos set the type face style the text is to be rendered in.
     * @param tfs
     */
    public void setTypeFaceStyle(Text.TypeFaceStyle tfs) {
        this.typeFaceStyle = tfs;
    }
}

