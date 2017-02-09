/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind.geom.coords;


import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.globe.Globe;
import gov.nasa.worldwind.util.Logger;

/**
 * This immutable class holds a set of UPS coordinates along with it's corresponding latitude and longitude.
 *
 * @author Patrick Murris
 * @version $Id: UPSCoord.java 1171 2013-02-11 21:45:02Z dcollins $
 */

public class UPSCoord
{
    final static private String TAG = UPSCoord.class.getSimpleName();

    private final double latitude;
    private final double longitude;
    private final String hemisphere;
    private final double easting;
    private final double northing;

    /**
     * Create a set of UPS coordinates from a pair of latitude and longitude for a WGS84 globe.
     *
     * @param latitude  the latitude <code>Angle</code>.
     * @param longitude the longitude <code>Angle</code>.
     *
     * @return the corresponding <code>UPSCoord</code>.
     *
     * @throws IllegalArgumentException if <code>latitude</code> or <code>longitude</code> is null, or the conversion to
     *                                  UPS coordinates fails.
     */
    public static UPSCoord fromLatLon(double latitude, double longitude)
    {
        return fromLatLon(latitude, longitude, null);
    }

    /**
     * Create a set of UPS coordinates from a pair of latitude and longitude for the given <code>Globe</code>.
     *
     * @param latitude  the latitude <code>Angle</code>.
     * @param longitude the longitude <code>Angle</code>.
     * @param globe     the <code>Globe</code> - can be null (will use WGS84).
     *
     * @return the corresponding <code>UPSCoord</code>.
     *
     * @throws IllegalArgumentException if <code>latitude</code> or <code>longitude</code> is null, or the conversion to
     *                                  UPS coordinates fails.
     */
    public static UPSCoord fromLatLon(double latitude, double longitude, Globe globe)
    {
        if (latitude == Double.NaN || longitude == Double.NaN)
        {
            String message = Logger.makeMessage(TAG, "fromLatLon", "nullValue.LatitudeOrLongitudeIsNull");
            Logger.log(Logger.ERROR, message);
            throw new IllegalArgumentException(message);
        }

        final UPSCoordConverter converter = new UPSCoordConverter(globe);
        long err = converter.convertGeodeticToUPS(Math.toRadians(latitude), Math.toRadians(longitude));

        if (err != UPSCoordConverter.UPS_NO_ERROR)
        {
            String message = Logger.makeMessage(TAG, "fromLatLon", "Coord.UPSConversionError");
            Logger.log(Logger.ERROR, message);
            throw new IllegalArgumentException(message);
        }

        return new UPSCoord(latitude, longitude, converter.getHemisphere(),
            converter.getEasting(), converter.getNorthing());
    }

    /**
     * Create a set of UPS coordinates for a WGS84 globe.
     *
     * @param hemisphere the hemisphere, either {@link gov.nasa.worldwind .avlist.AVKey#NORTH} or {@link
     *                   gov.nasa.worldwind .avlist.AVKey#SOUTH}.
     * @param easting    the easting distance in meters
     * @param northing   the northing distance in meters.
     *
     * @return the corresponding <code>UPSCoord</code>.
     *
     * @throws IllegalArgumentException if the conversion to UPS coordinates fails.
     */
    public static UPSCoord fromUTM(String hemisphere, double easting, double northing)
    {
        return fromUPS(hemisphere, easting, northing, null);
    }

    /**
     * Create a set of UPS coordinates for the given <code>Globe</code>.
     *
     * @param hemisphere the hemisphere, either {@link gov.nasa.worldwind .avlist.AVKey#NORTH} or {@link
     *                   gov.nasa.worldwind .avlist.AVKey#SOUTH}.
     * @param easting    the easting distance in meters
     * @param northing   the northing distance in meters.
     * @param globe      the <code>Globe</code> - can be null (will use WGS84).
     *
     * @return the corresponding <code>UPSCoord</code>.
     *
     * @throws IllegalArgumentException if the conversion to UPS coordinates fails.
     */
    public static UPSCoord fromUPS(String hemisphere, double easting, double northing, Globe globe)
    {
        final UPSCoordConverter converter = new UPSCoordConverter(globe);
        long err = converter.convertUPSToGeodetic(hemisphere, easting, northing);

        if (err != UTMCoordConverter.UTM_NO_ERROR)
        {
            String message = Logger.makeMessage(TAG, "fromUPS", "Coord.UTMConversionError");
            Logger.log(Logger.ERROR, message);
            throw new IllegalArgumentException(message);
        }

        return new UPSCoord(Math.toDegrees(converter.getLatitude()),
            Math.toDegrees(converter.getLongitude()),
            hemisphere, easting, northing);
    }

    /**
     * Create an arbitrary set of UPS coordinates with the given values.
     *
     * @param latitude   the latitude <code>Angle</code>.
     * @param longitude  the longitude <code>Angle</code>.
     * @param hemisphere the hemisphere, either {@link gov.nasa.worldwind .avlist.AVKey#NORTH} or {@link
     *                   gov.nasa.worldwind .avlist.AVKey#SOUTH}.
     * @param easting    the easting distance in meters
     * @param northing   the northing distance in meters.
     *
     * @throws IllegalArgumentException if <code>latitude</code>, <code>longitude</code>, or <code>hemisphere</code> is
     *                                  null.
     */
    public UPSCoord(double latitude, double longitude, String hemisphere, double easting, double northing)
    {
        if (latitude == Double.NaN || longitude == Double.NaN)
        {
            String message = Logger.makeMessage(TAG, "UPSCoord", "nullValue.LatitudeOrLongitudeIsNull");
            Logger.log(Logger.ERROR, message);
            throw new IllegalArgumentException(message);
        }

        this.latitude = latitude;
        this.longitude = longitude;
        this.hemisphere = hemisphere;
        this.easting = easting;
        this.northing = northing;
    }

    public double getLatitude()
    {
        return this.latitude;
    }

    public double getLongitude()
    {
        return this.longitude;
    }

    public String getHemisphere()
    {
        return this.hemisphere;
    }

    public double getEasting()
    {
        return this.easting;
    }

    public double getNorthing()
    {
        return this.northing;
    }

    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(AVKey.NORTH.equals(hemisphere) ? "N" : "S");
        sb.append(" ").append(easting).append("E");
        sb.append(" ").append(northing).append("N");
        return sb.toString();
    }
}
