package mil.emp3.core.mapgridlines.coordinates;

import android.util.Log;

import org.cmapi.primitives.GeoPosition;
import org.cmapi.primitives.IGeoPosition;

import java.security.InvalidParameterException;

/**
 * This class represent a UTM coordinate.
 */

public class UTMCoordainte {
    final static private String TAG = UTMCoordainte.class.getSimpleName();

    // Latitude bands letters - from south to north
    private static final String latBands = "CDEFGHJKLMNPQRSTUVWX";

    String zoneLetterValue = null;
    int zoneNumberValue;
    double eastingValue;
    double northingValue;

    public UTMCoordainte() {
    }

    public UTMCoordainte(int zoneNumber, String zoneLetter, double easting, double northing) {
        zoneNumberValue = zoneNumber;
        zoneLetterValue = zoneLetter;
        eastingValue = easting;
        northingValue = northing;
    }

    public int getZoneNumber() {
        return zoneNumberValue;
    }

    public String getZoneLetter() {
        return zoneLetterValue;
    }

    public double getNorthing() {
        return northingValue;
    }

    public double getEasting() {
        return eastingValue;
    }

    public void setZoneNumber(int value) {
        zoneNumberValue = value;
    }

    public void setZoneLetter(String value) {
        if ((null == value) || (value.length() > 1) || value.isEmpty() || !latBands.contains(value)) {
            Log.e(TAG, "Invalid Zone letter: " + value);
            throw new InvalidParameterException("Invalid Zone letter " + value);
        }
        zoneLetterValue = value;
    }

    public void setNorthing(double value) {
        northingValue = value;
    }

    public void setEasting(double value) {
        eastingValue = value;
    }

    public static int getZoneNumber(double latitude, double longitude) {
        if ((latitude > 84.0) || (latitude < -80)) {
            return 0;
        }
        int startZoneIndex;

        if (longitude == 180.0) {
            startZoneIndex = 1;
        } else {
            startZoneIndex = (int) Math.floor((longitude + 180) / 6.0) + 1;
        }

        if ((latitude >= 56.0) && (latitude <= 64.0)) {
            if ((longitude >= 3.0) && (longitude < 6.0)) {
                // To the west of Norway.
                startZoneIndex = 32;
            }
        } else if ((latitude >= 72.0) && (latitude < 84.0)) {
            // Looking for the X band north of Norway.
            if ((longitude >= 0.0) && (longitude < 9.0)) {
                startZoneIndex = 31;
            } else if ((longitude >= 9.0) && (longitude < 21.0)) {
                startZoneIndex = 33;
            } else if ((longitude >= 21.0) && (longitude < 33.0)) {
                startZoneIndex = 35;
            } else if ((longitude >= 33.0) && (longitude < 42.0)) {
                startZoneIndex = 35;
            }
        }

        return startZoneIndex;
    }

    public static String getZoneLetter(double latitude, double longitude) {
        if (latitude >= 84.0) {
            return "X";
        } else if (latitude < -80) {
            return "C";
        }

        int iLatIndex = (int) Math.floor((Math.max(latitude, -80.0) + 80.0) / 8.0);

        return ((iLatIndex < latBands.length())? latBands.charAt(iLatIndex) + "": "");
    }

    public static UTMCoordainte fromLatLong(double latitude, double longitude) {
        return fromLatLong(latitude, longitude, new UTMCoordainte());
    }

    public static UTMCoordainte fromLatLong(double latitude, double longitude, UTMCoordainte result) {
        double a = 6378137.0; //ellip.radius;
        double eccSquared = 0.00669438; //ellip.eccsq;
        double k0 = 0.9996;
        double LongOrigin;
        double eccPrimeSquared;
        double N, T, C, A, M;
        double LatRad = Math.toRadians(latitude);
        double LongRad = Math.toRadians(longitude);
        double LongOriginRad;
        int ZoneNumber;

        ZoneNumber = getZoneNumber(latitude, longitude);
        result.setZoneNumber(ZoneNumber);
        result.setZoneLetter(getZoneLetter(latitude, longitude));

        //LongOrigin = (ZoneNumber - 1) * 6 - 180 + 3; //+3 puts origin
        LongOrigin = (ZoneNumber - 1) * 6 - 180 + (getGridZoneWidthInDegrees(ZoneNumber, result.getZoneLetter()) / 2.0);
        // in middle of
        // zone
        LongOriginRad = Math.toRadians(LongOrigin);

        eccPrimeSquared = (eccSquared) / (1 - eccSquared);

        N = a / Math.sqrt(1 - eccSquared * Math.sin(LatRad) * Math.sin(LatRad));
        T = Math.tan(LatRad) * Math.tan(LatRad);
        C = eccPrimeSquared * Math.cos(LatRad) * Math.cos(LatRad);
        A = Math.cos(LatRad) * (LongRad - LongOriginRad);

        M = a * ((1 - eccSquared / 4 - 3 * eccSquared * eccSquared / 64 - 5 * eccSquared * eccSquared * eccSquared / 256) * LatRad - (3 * eccSquared / 8 + 3 * eccSquared * eccSquared / 32 + 45 * eccSquared * eccSquared * eccSquared / 1024) * Math.sin(2 * LatRad) + (15 * eccSquared * eccSquared / 256 + 45 * eccSquared * eccSquared * eccSquared / 1024) * Math.sin(4 * LatRad) - (35 * eccSquared * eccSquared * eccSquared / 3072) * Math.sin(6 * LatRad));

        double UTMEasting = (k0 * N * (A + (1 - T + C) * A * A * A / 6.0 + (5 - 18 * T + T * T + 72 * C - 58 * eccPrimeSquared) * A * A * A * A * A / 120.0) + 500000.0);

        double UTMNorthing = (k0 * (M + N * Math.tan(LatRad) * (A * A / 2 + (5 - T + 9 * C + 4 * C * C) * A * A * A * A / 24.0 + (61 - 58 * T + T * T + 600 * C - 330 * eccPrimeSquared) * A * A * A * A * A * A / 720.0)));
        if (latitude < 0.0) {
            UTMNorthing += 10000000.0; //10000000 meter offset for
            // southern hemisphere
        }

        result.setZoneNumber(ZoneNumber);
        result.setZoneLetter(getZoneLetter(latitude, longitude));
        result.setEasting(Math.floor(UTMEasting));
        result.setNorthing(Math.floor(UTMNorthing));

        return result;
    }

    public IGeoPosition toLatLong() {
        return toLatLong(new GeoPosition());
    }

    public IGeoPosition toLatLong(IGeoPosition result) {
        double k0 = 0.9996;
        double a = 6378137.0; //ellip.radius;
        double eccSquared = 0.00669438; //ellip.eccsq;
        double eccPrimeSquared;
        double e1 = (1 - Math.sqrt(1 - eccSquared)) / (1 + Math.sqrt(1 - eccSquared));
        double N1, T1, C1, R1, D, M;
        double LongOrigin;
        double mu, phi1Rad;

        // remove 500,000 meter offset for longitude
        double x = eastingValue - 500000.0;
        double y = northingValue;

        // We must know somehow if we are in the Northern or Southern
        // hemisphere, this is the only time we use the letter So even
        // if the Zone letter isn't exactly correct it should indicate
        // the hemisphere correctly
        if (zoneLetterValue.charAt(0) < 'N') {
            y -= 10000000.0; // remove 10,000,000 meter offset used
            // for southern hemisphere
        }

        // There are 60 zones with zone 1 being at West -180 to -174
        LongOrigin = (zoneNumberValue - 1) * 6 - 180 + 3; // +3 puts origin
        // in middle of
        // zone

        eccPrimeSquared = (eccSquared) / (1 - eccSquared);

        M = y / k0;
        mu = M / (a * (1 - eccSquared / 4 - 3 * eccSquared * eccSquared / 64 - 5 * eccSquared * eccSquared * eccSquared / 256));

        phi1Rad = mu + (3 * e1 / 2 - 27 * e1 * e1 * e1 / 32) * Math.sin(2 * mu) + (21 * e1 * e1 / 16 - 55 * e1 * e1 * e1 * e1 / 32) * Math.sin(4 * mu) + (151 * e1 * e1 * e1 / 96) * Math.sin(6 * mu);
        // double phi1 = ProjMath.radToDeg(phi1Rad);

        N1 = a / Math.sqrt(1 - eccSquared * Math.sin(phi1Rad) * Math.sin(phi1Rad));
        T1 = Math.tan(phi1Rad) * Math.tan(phi1Rad);
        C1 = eccPrimeSquared * Math.cos(phi1Rad) * Math.cos(phi1Rad);
        R1 = a * (1 - eccSquared) / Math.pow(1 - eccSquared * Math.sin(phi1Rad) * Math.sin(phi1Rad), 1.5);
        D = x / (N1 * k0);

        double lat = phi1Rad - (N1 * Math.tan(phi1Rad) / R1) * (D * D / 2 - (5 + 3 * T1 + 10 * C1 - 4 * C1 * C1 - 9 * eccPrimeSquared) * D * D * D * D / 24 + (61 + 90 * T1 + 298 * C1 + 45 * T1 * T1 - 252 * eccPrimeSquared - 3 * C1 * C1) * D * D * D * D * D * D / 720);
        lat = Math.toDegrees(lat);

        double lon = (D - (1 + 2 * T1 + C1) * D * D * D / 6 + (5 - 2 * C1 + 28 * T1 - 3 * C1 * C1 + 8 * eccPrimeSquared + 24 * T1 * T1) * D * D * D * D * D / 120) / Math.cos(phi1Rad);
        lon = LongOrigin + Math.toDegrees(lon);

        result.setLatitude(lat);
        result.setLongitude(lon);

        return result;
    }

    public String getPreviousLetter() {
        int iIndex = latBands.indexOf(zoneLetterValue);

        if (zoneLetterValue.charAt(0) == 'C') {
            return null;
        }
        iIndex--;

        return latBands.charAt(iIndex) + "";
    }

    public String getNextLetter() {
        int iIndex = latBands.indexOf(zoneLetterValue);

        if (zoneLetterValue.charAt(0) == 'X') {
            return null;
        }
        iIndex++;

        return latBands.charAt(iIndex) + "";
    }

    public double getMinNorthingForZone() {
        return getMinNorthingForZone(this.zoneLetterValue);
    }

    public static double getMinNorthingForZone(String letter) {
        double northing;

        switch (letter) {
            case "C":
                northing = 1100000.0;
                break;
            case "D":
                northing = 2000000.0;
                break;
            case "E":
                northing = 2800000.0;
                break;
            case "F":
                northing = 3700000.0;
                break;
            case "G":
                northing = 4600000.0;
                break;
            case "H":
                northing = 5500000.0;
                break;
            case "J":
                northing = 6400000.0;
                break;
            case "K":
                northing = 7300000.0;
                break;
            case "L":
                northing = 8200000.0;
                break;
            case "M":
                northing = 9100000.0;
                break;
            case "N":
                northing = 0.0;
                break;
            case "P":
                northing = 800000.0;
                break;
            case "Q":
                northing = 1700000.0;
                break;
            case "R":
                northing = 2600000.0;
                break;
            case "S":
                northing = 3500000.0;
                break;
            case "T":
                northing = 4400000.0;
                break;
            case "U":
                northing = 5300000.0;
                break;
            case "V":
                northing = 6200000.0;
                break;
            case "W":
                northing = 7000000.0;
                break;
            case "X":
                northing = 7900000.0;
                break;
            default:
                throw new InvalidParameterException("UTM zone letter is not set.");
        }

        return northing;
    }

    public double getMaxNorthingForZone() {
        return getMaxNorthingForZone(this.zoneLetterValue);
    }

    public static double getMaxNorthingForZone(String letter) {
        double northing;

        switch (letter) {
            case "C":
                northing = 2000000.0 - 1.0;
                break;
            case "D":
                northing = 2800000.0 - 1.0;
                break;
            case "E":
                northing = 3700000.0 - 1.0;
                break;
            case "F":
                northing = 4600000.0 - 1.0;
                break;
            case "G":
                northing = 5500000.0 - 1.0;
                break;
            case "H":
                northing = 6400000.0 - 1.0;
                break;
            case "J":
                northing = 7300000.0 - 1.0;
                break;
            case "K":
                northing = 8200000.0 - 1.0;
                break;
            case "L":
                northing = 9100000.0 - 1.0;
                break;
            case "M":
                northing = 10000000.0 - 1.0;
                break;
            case "N":
                northing = 800000.0 - 1.0;
                break;
            case "P":
                northing = 1700000.0 - 1.0;
                break;
            case "Q":
                northing = 2600000.0 - 1.0;
                break;
            case "R":
                northing = 3500000.0 - 1;
                break;
            case "S":
                northing = 4400000.0 - 1.0;
                break;
            case "T":
                northing = 5300000.0 - 1.0;
                break;
            case "U":
                northing = 6200000.0 - 1.0;
                break;
            case "V":
                northing = 7000000.0 - 1.0;
                break;
            case "W":
                northing = 7900000.0 - 1.0;
                break;
            case "X":
                northing = 9328195.0;
                break;
            default:
                throw new InvalidParameterException("UTM zone letter is not set.");
        }

        return northing;
    }

    public int getGridZoneWidthInDegrees() {
        return getGridZoneWidthInDegrees(getZoneNumber(), getZoneLetter());
    }

    public static int getGridZoneWidthInDegrees(int zoneNumber, String zoneLetter) {
        int zoneWidth = 6;

        if (0 == zoneNumber) {
            switch (zoneLetter) {
                case "A":
                case "B":
                    zoneWidth = 10;
                    break;
                case "Y":
                case "Z":
                    zoneWidth = 6;
                    break;
            }
        } else switch (zoneLetter) {
            case "V":
                switch (zoneNumber) {
                    case 31:
                        // To the west of Norway.
                        zoneWidth = 3;
                        break;
                    case 32:
                        // To the west of Norway.
                        zoneWidth = 9;
                        break;
                }
                break;
            case "X":
                switch (zoneNumber) {
                    case 31:
                    case 37:
                        zoneWidth = 9;
                        break;
                    case 32:
                    case 34:
                        zoneWidth = 0;
                        break;
                    case 33:
                    case 35:
                        zoneWidth = 12;
                        break;
                }
                break;
        }

        return zoneWidth;
    }

    public int getGridZoneHeightInDegrees() {
        return getGridZoneHeightInDegrees(getZoneNumber(), getZoneLetter());
    }

    public static int getGridZoneHeightInDegrees(int zoneNumber, String zoneLetter) {
        int zoneHeight = 8;

        switch (zoneLetter) {
            case "A":
            case "B":
                zoneHeight = 10;
                break;
            case "X":
                zoneHeight = 12;
                break;
            case "Y":
            case "Z":
                zoneHeight = 6;
                break;
        }

        return zoneHeight;
    }

    public int getZoneWestLongitude() {
        return getZoneWestLongitude(getZoneNumber(), getZoneLetter());
    }

    public static int getZoneWestLongitude(int zoneNumber, String zoneLetter) {
        if (0 == zoneNumber) {
            return 0;
        }

        int longitude = ((zoneNumber - 1) * 6) - 180;

        switch (zoneLetter) {
            case "V":
                switch (zoneNumber) {
                    case 32:
                        // To the west of Norway.
                        longitude -= 3;
                        break;
                }
                break;
            case "X":
                switch (zoneNumber) {
                    case 32:
                    case 34:
                        longitude = 0;
                        break;
                    case 33:
                    case 35:
                    case 37:
                        longitude -= 3;
                        break;
                }
                break;
        }

        return longitude;
    }

    public int getZoneSouthLatitude() {
        return getZoneSouthLatitude(this.getZoneNumber(), getZoneLetter());
    }

    public static int getZoneSouthLatitude(int zoneNumber, String zoneLetter) {
        if (0 == zoneNumber) {
            return 0;
        }

        int index = latBands.indexOf(zoneLetter.charAt(0));
        return -80 + (index * 8);
    }

    public boolean isNorthernHemisphere() {
        if (null == getZoneLetter()) {
            throw new InvalidParameterException("Zone letter not set.");
        }
        return (getZoneLetter().charAt(0) >= 'N');
    }
}
