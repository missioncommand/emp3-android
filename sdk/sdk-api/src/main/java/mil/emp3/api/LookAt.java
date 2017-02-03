package mil.emp3.api;

import org.cmapi.primitives.GeoLookAt;
import org.cmapi.primitives.IGeoAltitudeMode;
import org.cmapi.primitives.IGeoLookAt;

import java.util.HashMap;
import java.util.UUID;

import mil.emp3.api.enums.EventListenerTypeEnum;
import mil.emp3.api.exceptions.EMP_Exception;
import mil.emp3.api.interfaces.ILookAt;
import mil.emp3.api.interfaces.core.ICoreManager;
import mil.emp3.api.interfaces.core.IEventManager;
import mil.emp3.api.listeners.EventListenerHandle;
import mil.emp3.api.listeners.ILookAtEventListener;
import mil.emp3.api.utils.ManagerFactory;

/**
 * This class implemts a LookAt object which can be set on the map.
 */
public class LookAt implements ILookAt{

    final private IEventManager eventManager = ManagerFactory.getInstance().getEventManager();
    final private ICoreManager coreManager   = ManagerFactory.getInstance().getCoreManager();

    /**
     * This is the backing/describing implementation instance as passed in through the
     * copy constructor or created new by the default constructor. This is always active
     * so we do not need to perform null checks when passing through getters and setters.
     */
    private final IGeoLookAt geoLookAt;

    /**
     * This default constructor creates a GeoLookAt.
     */
    public LookAt() {
        this.geoLookAt = new GeoLookAt();
        if (this.geoLookAt.getAltitudeMode() == null) {
            this.geoLookAt.setAltitudeMode(AltitudeMode.RELATIVE_TO_GROUND);
        }
    }

    /**
     * This constructor creates the LookAt with the IGeoLookAt encapsulated within.
     * @param lookAt An object that implements the IGeoLookAt interface. See {@link IGeoLookAt}
     */
    public LookAt(IGeoLookAt lookAt) {
        this.geoLookAt = lookAt;
        if (this.geoLookAt.getAltitudeMode() == null) {
            this.geoLookAt.setAltitudeMode(AltitudeMode.RELATIVE_TO_GROUND);
        }
    }

    @Override
    public void copySettingsFrom (ILookAt from) {
        this.geoLookAt.setAltitude(from.getAltitude());
        this.geoLookAt.setAltitudeMode(from.getAltitudeMode());
        this.geoLookAt.setHeading(from.getHeading());
        this.geoLookAt.setLatitude(from.getLatitude());
        this.geoLookAt.setLongitude(from.getLongitude());
        this.geoLookAt.setTilt(from.getTilt());
        this.geoLookAt.setName(from.getName());
        this.geoLookAt.setRange(from.getRange());
        this.geoLookAt.setDescription(from.getDescription());
        this.geoLookAt.setGeoId(from.getGeoId());
    }

    @Override
    public EventListenerHandle addLookAtEventListener(ILookAtEventListener listener) throws EMP_Exception {
        return eventManager.addEventHandler(EventListenerTypeEnum.LOOKAT_EVENT_LISTENER, this, listener);
    }

    @Override
    public void removeEventListener(EventListenerHandle listenerHandle) {
        eventManager.removeEventHandler(listenerHandle);
    }

    /**
     * This method sets the tilt on the lookAt. If this lookAt is the current lookAt on
     * any map, it will change the view on the map.
     * @param value The tilt angle in degrees.
     */
    @Override
    public void setTilt(double value) {
        this.geoLookAt.setTilt(value);
    }

    /**
     * This method retrieves the current tilt setting of the lookAt.
     * @return The current tilt in degrees.
     */
    @Override
    public double getTilt() {
        return this.geoLookAt.getTilt();
    }

    /**
     * This method sets the lookAts heading. Setting this value on a lookAt that is
     * associated with a map will cause the map to change its viewing area.
     * @param heading The new heading in degrees from north.
     */
    @Override
    public void setHeading(double heading) {
        this.geoLookAt.setHeading(heading);
    }

    /**
     * This method retrieves the current azimuth from the lookAt.
     * @return The azimuth in degrees from north.
     */
    @Override
    public double getHeading() {
        return this.geoLookAt.getHeading();
    }

    @Override
    public void setRange(double v) {
        this.geoLookAt.setRange(v);
    }

    @Override
    public double getRange() {
        return this.geoLookAt.getRange();
    }

    /**
     * This method set the altitude mode for the elevation setting. Setting this value on a lookAt that is
     * associated with a map will cause the map to change its viewing area.
     * @param value The new altitude mode. See {@link IGeoAltitudeMode.AltitudeMode}
     */
    @Override
    public void setAltitudeMode(IGeoAltitudeMode.AltitudeMode value) {
        this.geoLookAt.setAltitudeMode(value);
    }

    /**
     * This method retrieves the current altitude mode setting.
     * @return See {@link IGeoAltitudeMode.AltitudeMode}
     */
    @Override
    public IGeoAltitudeMode.AltitudeMode getAltitudeMode() {
        return this.geoLookAt.getAltitudeMode();
    }

    /**
     * This method sets the latitude of the lookAt position. Setting this value on a lookAt that is
     * associated with a map will cause the map to change its viewing area.
     * @param value The latitude in degrees. The value must be -90.0 and 90 degrees. Values outside of this range are ignored.
     */
    @Override
    public void setLatitude(double value) {
        if ((value >= -90.0) && (value <= 90.0)) {
            this.geoLookAt.setLatitude(value);
        }
    }

    /**
     * This method retrieves the current latitude from the lookAt.
     * @return The current latitude in degrees.
     */
    @Override
    public double getLatitude() {
        return this.geoLookAt.getLatitude();
    }

    /**
     * This method sets the longitude of the lookAt position. Setting this value on a lookAt that is
     * associated with a map will cause the map to change its viewing area.
     * @param value The longitude in degrees. The value must be -180.0 and 180.0 degrees. Values outside of this range are ignored.
     */
    @Override
    public void setLongitude(double value) {
        if ((value >= -180.0) && (value <= 180.0)) {
            this.geoLookAt.setLongitude(value);
        }
    }

    /**
     * This method retrieves the current longitude setting from the lookAt.
     * @return The current longitude in degrees.
     */
    @Override
    public double getLongitude() {
        return this.geoLookAt.getLongitude();
    }

    /**
     * This method sets the altitude of the lookAt. Setting this value on a lookAt that is
     * associated with a map will cause the map to change its viewing area.
     * @param value The new altitude in meters.
     */
    @Override
    public void setAltitude(double value) {
        this.geoLookAt.setAltitude(value);
    }

    /**
     * This method retrieves the current altitude setting of the lookAt.
     * @return The altitude in meters.
     */
    @Override
    public double getAltitude() {
        return this.geoLookAt.getAltitude();
    }

    /**
     * This method allows the user to give the lookAt a name.
     * @param s The new name for the lookAt.
     */
    @Override
    public void setName(String s) {
        this.geoLookAt.setName(s);
    }

    /**
     * This method retrieves the current name.
     * @return A string or null.
     */
    @Override
    public String getName() {
        return this.geoLookAt.getName();
    }

    @Override
    public void setGeoId(java.util.UUID uuid) {
        this.geoLookAt.setGeoId(uuid);
    }

    /**
     * This method retrieves the lookAts unique identifier.
     * @return a UUID. See {@link java.util.UUID}
     */
    @Override
    public UUID getGeoId() {
        return this.geoLookAt.getGeoId();
    }

    @Override
    public void setDataProviderId(String s) {
        this.geoLookAt.setDataProviderId(s);
    }

    @Override
    public String getDataProviderId() {
        return this.geoLookAt.getDataProviderId();
    }

    /**
     * This method allows the user to set a description.
     * @param s String
     */
    @Override
    public void setDescription(String s) {
        this.geoLookAt.setDescription(s);
    }

    /**
     * This method retrieves the lookAts description.
     * @return String or null.
     */
    @Override
    public String getDescription() {
        return this.geoLookAt.getDescription();
    }

    @Override
    public HashMap<String, Object> getProperties() {
        return null;
    }

    @Override
    public void apply(boolean animate) {
        coreManager.processLookAtSettingChange(this, animate);
    }
}
