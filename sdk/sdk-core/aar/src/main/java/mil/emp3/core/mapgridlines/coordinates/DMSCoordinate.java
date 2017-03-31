package mil.emp3.core.mapgridlines.coordinates;

import org.cmapi.primitives.IGeoPosition;

import mil.emp3.api.utils.EmpGeoPosition;
import mil.emp3.core.mapgridlines.utils.Angle;
import mil.emp3.core.mapgridlines.utils.DMSAngle;

/**
 * This class implements a DMS map coordinate.
 */

public class DMSCoordinate {
    private DMSAngle latitude = null;
    private DMSAngle longitude = null;

    public DMSCoordinate() {
        this.latitude = new DMSAngle();
        this.longitude = new DMSAngle();
    }

    private DMSCoordinate(double latitude, double longitude) {
        this.latitude = DMSAngle.fromDD(Angle.normalizeLatitude(latitude));
        this.longitude = DMSAngle.fromDD(Angle.normalizeLongitude(longitude));
    }

    public static DMSCoordinate fromLatLong(double latitude, double longitude) {
        return new DMSCoordinate(latitude, longitude);
    }

    public DMSAngle getLatitude() {
        return this.latitude;
    }

    public DMSAngle getLongitude() {
        return this.longitude;
    }

    public void setLatitude(double degLat) {
        this.latitude.setDecimalDegrees(Angle.normalizeLatitude(degLat));
    }

    public void setLongitude(double degLong) {
        this.longitude.setDecimalDegrees(Angle.normalizeLongitude(degLong));
    }

    public void setCoordinate(double degLat, double degLong) {
        setLatitude(degLat);
        setLongitude(degLong);
    }

    public IGeoPosition toPosition() {
        return new EmpGeoPosition(this.latitude.toDD(), this.longitude.toDD());
    }

    public String latitudeToString(String format) {
        String cardinalPoint = "";

        if (this.latitude.getSign() < 0) {
            cardinalPoint = " S";
        } else if (this.latitude.getSign() > 0) {
            cardinalPoint = " N";
        }
        return this.latitude.toString(format) + cardinalPoint;
    }

    public String longitudeToString(String format) {
        String cardinalPoint = "";

        if (this.longitude.getSign() < 0) {
            cardinalPoint = " W";
        } else if (this.longitude.getSign() > 0) {
            cardinalPoint = " E";
        }
        return this.longitude.toString(format) + cardinalPoint;
    }

    public String toString() {
        return latitudeToString("%Dd\u00b0 %M02d' %S05.3f\"") + " " + longitudeToString("%Dd\u00b0 %M02d' %S05.3f\"");
    }
}
