package mil.emp3.api.abstracts;

import org.cmapi.primitives.IGeoAltitudeMode;
import org.cmapi.primitives.IGeoFillStyle;
import org.cmapi.primitives.IGeoLabelStyle;
import org.cmapi.primitives.IGeoPosition;
import org.cmapi.primitives.IGeoRenderable;
import org.cmapi.primitives.IGeoStrokeStyle;
import org.cmapi.primitives.IGeoTimeSpan;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import mil.emp3.api.enums.FeatureTypeEnum;
import mil.emp3.api.exceptions.EMP_Exception;
import mil.emp3.api.global;
import mil.emp3.api.interfaces.IFeature;
import mil.emp3.api.interfaces.IOverlay;
import mil.emp3.api.utils.EmpGeoPosition;
import mil.emp3.api.utils.EmpStyles;


/**
 * All single point and multi point shapes and symbols that are displayed on the Map use Feature as their base class. Feature class provides for
 * graphical properties, hierarchy placement and geo spatial positions among other attributes. Feature is a Container and as such it can contain
 * other Features. A Feature can be a child of many other containers and it can have many children. Visibility of a feature is governed by its
 * own visibility and visibility of its parents.
 */
public class Feature<T extends IGeoRenderable> extends Container implements IFeature<T> {

    public static final String TAG = Feature.class.getSimpleName();
    private static SimpleDateFormat zonedDate = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZZZZZ");
    private final FeatureTypeEnum eFeatureType;

    protected Feature(T oRenderable, FeatureTypeEnum eFeatureType) {
        super(oRenderable);
        this.eFeatureType = eFeatureType;
    }

    @Override
    public final T getRenderable() {
        return (T) this.getGeoContainer();
    }

    @Override
    public FeatureTypeEnum getFeatureType() {
        return this.eFeatureType;
    }

    @Override
    public List<IFeature> getChildFeatures() {
        return storageManager.getChildFeatures(this);
    }

    @Override
    public List<IOverlay> getParentOverlays() {
        return storageManager.getParentOverlays(this);
    }

    @Override
    public List<IFeature> getParentFeatures() {
        return storageManager.getParentFeatures(this);
    }

    @Override
    public void validate() {}

    @Override
    public void addFeature(IFeature feature, boolean visible, Object userContext)
            throws EMP_Exception {
        if (feature == null) {
            throw new EMP_Exception(EMP_Exception.ErrorDetail.INVALID_PARAMETER, "Parameter to Feature.addFeature can not be null.");
        }

        ArrayList<IFeature> oList = new ArrayList<>();
        oList.add(feature);
        this.addFeatures(oList, visible, userContext);
    }

    @Override
    public void addFeatures(List<IFeature> features, boolean visible, Object userContext)
            throws EMP_Exception {
        if (features == null) {
            throw new EMP_Exception(EMP_Exception.ErrorDetail.INVALID_PARAMETER, "Parameter to Feature.addFeatures can not be null.");
        } else if (features.size() > 0) {
            for (IFeature feature : features) {
                feature.validate();
            }
            storageManager.addFeatures(this, features, visible, userContext);
        }
    }

    @Override
    public void removeFeature(IFeature feature, Object userContext)
            throws EMP_Exception {
        if(null == feature) return;
        ArrayList<IFeature> oList = new ArrayList<>();
        oList.add(feature);
        this.removeFeatures(oList, userContext);
    }

    @Override
    public void removeFeatures(List<IFeature> features, Object userContext)
            throws EMP_Exception {
        if((null == features) || (0 == features.size())) return;
        storageManager.removeFeatures(this, features, userContext);
    }

    @Override
    public void addFeature(IFeature feature, boolean visible)
            throws EMP_Exception {
        addFeature(feature, visible, null);
    }

    @Override
    public void addFeatures(List<IFeature> features, boolean visible)
            throws EMP_Exception {
        addFeatures(features, visible, null);
    }

    @Override
    public void removeFeature(IFeature feature)
            throws EMP_Exception {
        removeFeature(feature, null);
    }

    @Override
    public void removeFeatures(List<IFeature> features)
            throws EMP_Exception {
        removeFeatures(features, null);
    }

    /**
     * Set one or more positions for the Feature. This method will be removed once underlying CMAPI is updated.
     * @param oPositionList
     */
    @Override
    public void setPositions(List<IGeoPosition> oPositionList) {
        throw new IllegalArgumentException("Application should get Positions list and update it or clear it");
    }

    /**
     * Get a list of positions associated with this Feature.
     * @return
     */
    @Override
    public List<IGeoPosition> getPositions() {
        return this.getRenderable().getPositions();
    }

    /**
     * Set Time Stamp on the Feature.
     * @param date
     */
    @Override
    public void setTimeStamp(java.util.Date date) {
        this.getRenderable().setTimeStamp(date);
    }

    /**
     * Get Previously set Timestamp.
     * @return
     */
    @Override
    public java.util.Date getTimeStamp() {
        return this.getRenderable().getTimeStamp();
    }

    /**
     * Set a new Time Span list. This method will be removed once underlying CMAPI is updated.
     * @param list
     */
    @Override
    public void setTimeSpans(List<IGeoTimeSpan> list) {
        throw new IllegalArgumentException("Application should get Time Spans list and update it or clear it");
    }

    /**
     * Returns list of existing Time Spans.
     * @return
     */
    @Override
    public List<IGeoTimeSpan> getTimeSpans() {
        return this.getRenderable().getTimeSpans();
    }

    /**
     * Sets Altitude Mode.
     * @param eMode {@link IGeoAltitudeMode.AltitudeMode}
     */
    @Override
    public void setAltitudeMode(IGeoAltitudeMode.AltitudeMode eMode) {
        this.getRenderable().setAltitudeMode(eMode);
        if (null == eMode) {
            storageManager.setDefaultAltitudeMode(this);
        }
    }

    /**
     * Returns Altitude Mode.
     * @return {@link IGeoAltitudeMode.AltitudeMode}
     */
    @Override
    public IGeoAltitudeMode.AltitudeMode getAltitudeMode() {
        return this.getRenderable().getAltitudeMode();
    }

    /**
     * Once application has added a feature to an overlay, it can change one or more attributes of the Feature and invoke the apply method to
     * have those changes reflected on the display. This is typically used to change position(s) as battle field objects move around.
     */
    @Override
    public void apply(Object userContext) {
        try {
            storageManager.apply(this, true, userContext);
        } catch(EMP_Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Once application has added a feature to an overlay, it can change one or more attributes of the Feature and invoke the apply method to
     * have those changes reflected on the display. This is typically used to change position(s) as battle field objects move around.
     */
    @Override
    public void apply() {
        try {
            storageManager.apply(this, true, null);
        } catch(EMP_Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Sets line style used to render the feature. The line style applies to all features that
     * render lines, borders or outlines. Unless otherwise specified by the specific feature class,
     * the default line style for a feature is a line width of 3 pixels and black color, with no stippling.
     * @param oStyle The new line style to use to render the feature. {@link IGeoStrokeStyle}
     */
    @Override
    public void setStrokeStyle(IGeoStrokeStyle oStyle) {
        this.getRenderable().setStrokeStyle(oStyle);
    }

    /**
     * Returns the current line style.
     * @return The current line style or null if it is not set.
     */
    @Override
    public IGeoStrokeStyle getStrokeStyle() {
        return this.getRenderable().getStrokeStyle();
    }

    /**
     * Sets fill style used to render the feature. The fill style applies to all features that render as polygons.
     * Unless otherwise specified by the specific feature class, the default fill style for a feature is solid black.
     * @param oStyle The new fill style. {@link IGeoFillStyle}
     */
    @Override
    public void setFillStyle(IGeoFillStyle oStyle) {
        this.getRenderable().setFillStyle(oStyle);
    }

    /**
     * Returns the feature fill style.
     * @return The fill style or null if it has not been set.
     */
    @Override
    public IGeoFillStyle getFillStyle() {
        return this.getRenderable().getFillStyle();
    }

    /**
     * This method sets the style the text components of the feature are rendered in.  The label style
     * is ignored by all features that do not render text components. Unless otherwise specified by
     * the specific feature class, the default label style for a feature is size 12pt, left justified,
     * Regular typeface, Black text with a black outline.
     * @param labelStyle {@link IGeoLabelStyle}
     */
    @Override
    public void setLabelStyle(IGeoLabelStyle labelStyle) {
        this.getRenderable().setLabelStyle(labelStyle);
    }

    /**
     * Returns label style.
     * @return
     */
    @Override
    public IGeoLabelStyle getLabelStyle() {
        return this.getRenderable().getLabelStyle();
    }

    /**
     * Set/Reset the extrude option
     * @param extrude
     */
    @Override
    public void setExtrude(boolean extrude) {
        this.getRenderable().setExtrude(extrude);
    }

    /**
     * Returns current status of the extrude option.
     * @return
     */
    @Override
    public boolean getExtrude() {
        return this.getRenderable().getExtrude();
    }

    /**
     * Set/Reset the tessellate option.
     * @param b
     */
    @Override
    public void setTessellate(boolean b) {
        this.getRenderable().setTessellate(b);
    }

    /**
     * Returns current status of tessellate option.
     * @return
     */
    @Override
    public boolean getTessellate() {
        return this.getRenderable().getTessellate();
    }

    /**
     * A Buffer represents an area around the feature which extends beyond the outer borders of the
     * feature by the distance specified. It is rendered as a shaded area around the feature in the default
     * buffer fill style which has an opacity of 0.5 and a yellow color. in the current version a buffer
     * can be applied to Points, Paths, polygons, rectangles, squares, circles, and ellipses only.
     * @param buffer the distance in meters the buffer extends beyond the outer borders of the feature.
     */
    @Override
    public void setBuffer(double buffer) {
        this.getRenderable().setBuffer(buffer);
    }

    /**
     * Get buffer.
     * @return
     */
    @Override
    public double getBuffer() {
        return this.getRenderable().getBuffer();
    }

    /**
     * This method sets the azimuth value for the feature. The azimuth indicates the clockwise rotation from north at which the feature is to be rendered.
     * @param dValue - The azimuth in degrees. Valid range is -360 to 360. Out of range values raises an IllegalArgumentException.
     */
    @Override
    public void setAzimuth(final double dValue) {
        validateWithinRange(dValue, global.HEADING_MINIMUM, global.HEADING_MAXIMUM);
        this.getRenderable().setAzimuth(dValue);
    }

    /**
     * Returns Azimuth.
     * @return
     */
    @Override
    public double getAzimuth() {
        return this.getRenderable().getAzimuth();
    }

    @Override
    public void setPathType(PathType pathType) {
        if (null == pathType) {
            throw new IllegalArgumentException("Paramater ca not be null.");
        }

        this.getRenderable().setPathType(pathType);
    }

    @Override
    public PathType getPathType() {
        return this.getRenderable().getPathType();
    }

    /**
     * Returns the first position on the position list
     * @return The first position on the list or null.
     */
    public IGeoPosition getPosition() {
        List<IGeoPosition> oList = this.getPositions();

        if ((oList == null) || (oList.size() == 0)) {
            return null;
        }
        return oList.get(0);
    }

    /**
     * Sets the geographic coordinate of a single point feature. It ensures that there
     * is only one position on the list of positions.
     * @param oPosition see {@link org.cmapi.primitives.IGeoPosition}
     */
    public void setPosition(IGeoPosition oPosition) {
        List<IGeoPosition> oList = this.getPositions();

        if (oPosition != null) {
            if (oList == null) {
                oList = new ArrayList<>();
                this.setPositions(oList);
            }
            oList.clear();
            oList.add(oPosition);
        }
    }

    /**
     * Use by sub classes of this class to validate input parameters. If value is not NaN then we return absolute
     * value else thro an exception.
     * @param dValue value to be made positive
     * @param message Message that is returned if dValue is NaN
     * @return positve value of the passed in value
     */
    protected double makePositive(final double dValue, final String message) {
        if(Double.isNaN(dValue)) {
            throw new IllegalArgumentException(message);
        }
        else {
            return Math.abs(dValue);
        }
    }

    /**
     * Use by subclasses to check if provided value is a positive number
     * @param dValue value to be checked
     */
    protected void validatePositive(final Double dValue) {
        if (dValue <= 0 || Double.isNaN(dValue)) {
            throw new IllegalArgumentException("Invalid Input, " + dValue + " is not a positive number");
        }
    }

    /**
     * Validates if a parameter is in a certain range, throws an exception describing the error if not
     * Not inclusive
     * @param dValue value to be checked
     * @param minimum lower bound, NaN if no lower bound
     * @param maximum upper bound, NaN if no upper bound
     */
    protected void validateWithinRange(final double dValue, final double minimum, final double maximum) {
        if(Double.isNaN(dValue)) {
            throw new IllegalArgumentException("invalid parameter, value is not a nubmer");
        }
        if(Double.isNaN(minimum) || Double.isNaN(maximum)) {
               throw new IllegalArgumentException("Invalid parameter, one of the bounds is NaN");
        }
        if(dValue > maximum || dValue < minimum) {
            throw new IllegalArgumentException("Invalid parameter, " + String.valueOf(dValue) + " is not between" + String.valueOf(minimum) + " and " + String.valueOf(maximum));
        }
    }

    /**
     * Convert selected members to String.
     * @return String describing the feature including name, extrude, altitude mode and positions
     */
    @Override
    public String toString() {
        String str = getFeatureType().toString() + " ";
        if(null != getName()) {
            str += getName() + "\n";
        } else {
            str += "\n";
        }
        str += "PathType " + getPathType().toString() + " Extrude " + getExtrude() + " AltitudeMode " + getAltitudeMode() + "\n";
        str += EmpStyles.toString(getFillStyle()) + "\n" + EmpStyles.toString(getStrokeStyle()) + "\n";
        if(null != getPositions() && getPositions().size() > 0) {
            str += "Position Count " + getPositions().size() + " " + EmpGeoPosition.toString(getPositions().get(0)) + "\n";
        }
        // If you need to print all positions use EmpGeoPosition.toString(List<IGeoPosition>);

        return str;
    }
}