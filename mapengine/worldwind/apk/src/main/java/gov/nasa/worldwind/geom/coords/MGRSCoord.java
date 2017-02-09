/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind.geom.coords;


import gov.nasa.worldwind.globe.Globe;
import gov.nasa.worldwind.util.Logger;

/**
 * This class holds an immutable MGRS coordinate string along with
 * the corresponding latitude and longitude.
 *
 * @author Patrick Murris
 * @version $Id: MGRSCoord.java 1171 2013-02-11 21:45:02Z dcollins $
 */

public class MGRSCoord
{
    final static private String TAG = MGRSCoord.class.getSimpleName();

    private final String MGRSString;
    private final double latitude;
    private final double longitude;

    /**
     * Create a WGS84 MGRS coordinate from a pair of latitude and longitude <code>Angle</code>
     * with the maximum precision of five digits (one meter).
     *
     * @param latitude the latitude <code>Angle</code>.
     * @param longitude the longitude <code>Angle</code>.
     * @return the corresponding <code>MGRSCoord</code>.
     * @throws IllegalArgumentException if <code>latitude</code> or <code>longitude</code> is null,
     * or the conversion to MGRS coordinates fails.
     */
    public static MGRSCoord fromLatLon(double latitude, double longitude)
    {
        return fromLatLon(latitude, longitude, null, 5);
    }

    /**
     * Create a WGS84 MGRS coordinate from a pair of latitude and longitude <code>Angle</code>
     * with the given precision or number of digits.
     *
     * @param latitude the latitude <code>Angle</code>.
     * @param longitude the longitude <code>Angle</code>.
     * @param precision the number of digits used for easting and northing (1 to 5).
     * @return the corresponding <code>MGRSCoord</code>.
     * @throws IllegalArgumentException if <code>latitude</code> or <code>longitude</code> is null,
     * or the conversion to MGRS coordinates fails.
     */
    public static MGRSCoord fromLatLon(double latitude, double longitude, int precision)
    {
        return fromLatLon(latitude, longitude, null, precision);
    }

    /**
     * Create a MGRS coordinate from a pair of latitude and longitude <code>Angle</code>
     * with the maximum precision of five digits (one meter).
     *
     * @param latitude the latitude <code>Angle</code>.
     * @param longitude the longitude <code>Angle</code>.
     * @param globe the <code>Globe</code>.
     * @return the corresponding <code>MGRSCoord</code>.
     * @throws IllegalArgumentException if <code>latitude</code> or <code>longitude</code> is null,
     * the <code>globe</code> is null, or the conversion to MGRS coordinates fails.
     */
    public static MGRSCoord fromLatLon(double latitude, double longitude, Globe globe)
    {
        return fromLatLon(latitude, longitude, globe, 5);
    }

    /**
     * Create a MGRS coordinate from a pair of latitude and longitude <code>Angle</code>
     * with the given precision or number of digits (1 to 5).
     *
     * @param latitude the latitude <code>Angle</code>.
     * @param longitude the longitude <code>Angle</code>.
     * @param globe the <code>Globe</code> - can be null (will use WGS84).
     * @param precision the number of digits used for easting and northing (1 to 5).
     * @return the corresponding <code>MGRSCoord</code>.
     * @throws IllegalArgumentException if <code>latitude</code> or <code>longitude</code> is null,
     * or the conversion to MGRS coordinates fails.
     */
    public static MGRSCoord fromLatLon(double latitude, double longitude, Globe globe, int precision)
    {
        if (latitude == Double.NaN || longitude == Double.NaN)
        {
            String message = Logger.makeMessage(TAG, "fromLatLon", "nullValue.LatitudeOrLongitudeIsNull");
            Logger.log(Logger.ERROR, message);
            throw new IllegalArgumentException(message);
        }

        final MGRSCoordConverter converter = new MGRSCoordConverter(globe);
        long err = converter.convertGeodeticToMGRS(Math.toRadians(latitude), Math.toRadians(longitude), precision);

        if (err != MGRSCoordConverter.MGRS_NO_ERROR)
        {
            String message = Logger.makeMessage(TAG, "fromLatLon", "Coord.MGRSConversionError");
            Logger.log(Logger.ERROR, message);
            throw new IllegalArgumentException(message);
        }

        return new MGRSCoord(latitude, longitude, converter.getMGRSString());
    }

    /**
     * Create a MGRS coordinate from a standard MGRS coordinate text string.
     * <p>
     * The string will be converted to uppercase and stripped of all spaces before being evaluated.
     * </p>
     * <p>Valid examples:<br />
     * 32TLP5626635418<br />
     * 32 T LP 56266 35418<br />
     * 11S KU 528 111<br />
     * </p>
     * @param MGRSString the MGRS coordinate text string.
     * @param globe the <code>Globe</code> - can be null (will use WGS84).
     * @return the corresponding <code>MGRSCoord</code>.
     * @throws IllegalArgumentException if the <code>MGRSString</code> is null or empty,
     * the <code>globe</code> is null, or the conversion to geodetic coordinates fails (invalid coordinate string).
     */
    public static MGRSCoord fromString(String MGRSString, Globe globe)
    {
        if (MGRSString == null || MGRSString.length() == 0)
        {
            String message = Logger.makeMessage(TAG, "fromString", "nullValue.StringIsNull");
            Logger.log(Logger.ERROR, message);
            throw new IllegalArgumentException(message);
        }

        MGRSString = MGRSString.toUpperCase().replaceAll(" ", "");

        final MGRSCoordConverter converter = new MGRSCoordConverter(globe);
        long err = converter.convertMGRSToGeodetic(MGRSString);

        if (err != MGRSCoordConverter.MGRS_NO_ERROR)
        {
            String message = Logger.makeMessage(TAG, "fromString", "Coord.MGRSConversionError");
            Logger.log(Logger.ERROR, message);
            throw new IllegalArgumentException(message);
        }

        return new MGRSCoord(Math.toDegrees(converter.getLatitude()), Math.toDegrees(converter.getLongitude()), MGRSString);
    }

    /**
     * Create an arbitrary MGRS coordinate from a pair of latitude-longitude <code>Angle</code>
     * and the corresponding MGRS coordinate string.
     *
     * @param latitude the latitude <code>Angle</code>.
     * @param longitude the longitude <code>Angle</code>.
     * @param MGRSString the corresponding MGRS coordinate string.
     * @throws IllegalArgumentException if <code>latitude</code> or <code>longitude</code> is null,
     * or the MGRSString is null or empty.
     */
    public MGRSCoord(double latitude, double longitude, String MGRSString)
    {
        if (latitude == Double.NaN || longitude == Double.NaN)
        {
            String message = Logger.makeMessage(TAG, "MGRSCoord", "nullValue.LatitudeOrLongitudeIsNull");
            Logger.log(Logger.ERROR, message);
            throw new IllegalArgumentException(message);
        }
        if (MGRSString == null)
        {
            String message = Logger.makeMessage(TAG, "MGRSCoord", "nullValue.StringIsNull");
            Logger.log(Logger.ERROR, message);
            throw new IllegalArgumentException(message);
        }
        if (MGRSString.length() == 0)
        {
            String message = Logger.makeMessage(TAG, "MGRSCoord", "generic.StringIsEmpty");
            Logger.log(Logger.ERROR, message);
            throw new IllegalArgumentException(message);
        }
        this.latitude = latitude;
        this.longitude = longitude;
        this.MGRSString = MGRSString;
    }

    public double getLatitude()
    {
        return this.latitude;
    }

    public double getLongitude()
    {
        return this.longitude;
    }

    public String toString()
    {
        return this.MGRSString;
    }

}
