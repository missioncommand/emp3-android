package mil.emp3.api;


import org.cmapi.primitives.GeoPoint;
import org.cmapi.primitives.GeoPosition;
import org.cmapi.primitives.IGeoIconStyle;
import org.cmapi.primitives.IGeoPoint;
import org.cmapi.primitives.IGeoPosition;

import java.io.File;

import mil.emp3.api.abstracts.Feature;
import mil.emp3.api.enums.FeatureTypeEnum;

/**
 * This class implements the Point feature that encapsulates a GeoPoint object.
 */
public class Point extends Feature<IGeoPoint> implements IGeoPoint {
    private double dIconScale = 1.0;
    private int resourceId = 0;
    private static final double latLowerBound = -90.0;
    private static final double latUpperBound = 90.0;
    private static final double longLowerBound = -180.0;
    private static final double longUpperBound = 180.0;

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
    public Point(final String sURL) {
        super(new GeoPoint(), FeatureTypeEnum.GEO_POINT);
        if(android.webkit.URLUtil.isValidUrl(sURL)) {
            this.setIconURI(sURL);
        } else {
            throw new IllegalArgumentException("Invalid url input, " + sURL + " is not a valid url");
        }
    }

    /**
     * This constructor creates a point feature positioned at the coordinates provided.
     * @param dLat latitude value for constructor
     * @param dLong longitude value for constructor
     */
    public Point(final double dLat, final double dLong) {
        super(new GeoPoint(), FeatureTypeEnum.GEO_POINT);
        final IGeoPosition oPos = new GeoPosition();
        validateWithinRange(dLat, latLowerBound, latUpperBound);
        oPos.setLatitude(dLat);

        validateWithinRange(dLong, longLowerBound, longUpperBound);
        oPos.setLongitude(dLong);
        this.setPosition(oPos);
    }

    /**
     * This constructor creates a point feature from a geo point object.
     * @param oRenderable geopoint to make new point off of
     */
    public Point(final IGeoPoint oRenderable) {
        super(oRenderable, FeatureTypeEnum.GEO_POINT);
    }

    /**
     * This method set the image style.
     * @param oStyle See {@link IGeoIconStyle}
     */
    @Override
    public void setIconStyle(final IGeoIconStyle oStyle) {
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
    public void setIconURI(final String sURL) {
        if(!android.webkit.URLUtil.isValidUrl(sURL)) {
            if(sURL.matches("^file:.*$")) {
                final File testLocation = new File(sURL.substring(5));
                if (!testLocation.exists()) {
                    throw new IllegalArgumentException("Invalid Input, " + sURL + " is not a valid URL");
                }
            }
        }
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
    public void setIconScale(final double dScale) {
        validatePositive(dScale);
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
    public void setResourceId(final int resId) {
        this.resourceId = resId;
    }

    /**
     * This method is an ANDROID ONLY method. It returns the resource Id.
     * @return The resojurce Id or 0 if one has not been set.
     */
    public int getResourceId() {
        return this.resourceId;
    }

    /**
     *
     * @return String gives point parameters
     */

    @Override
    public String toString() {
        return "Point at\n" +
                "\tlatitude: " + getPosition().getLatitude() + "\n" +
                "\tlongitude: " + getPosition().getLongitude() + "\n" +
                "\taltitude: " + getPosition().getAltitude();
    }
}