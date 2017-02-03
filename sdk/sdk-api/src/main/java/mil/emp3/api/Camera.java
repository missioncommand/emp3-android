package mil.emp3.api;

import org.cmapi.primitives.GeoBase;
import org.cmapi.primitives.GeoCamera;
import org.cmapi.primitives.IGeoAltitudeMode;
import org.cmapi.primitives.IGeoCamera;

import java.security.InvalidParameterException;
import java.util.HashMap;
import java.util.UUID;

import mil.emp3.api.enums.EventListenerTypeEnum;
import mil.emp3.api.exceptions.EMP_Exception;
import mil.emp3.api.interfaces.ICamera;
import mil.emp3.api.interfaces.core.ICoreManager;
import mil.emp3.api.interfaces.core.IEventManager;
import mil.emp3.api.listeners.EventListenerHandle;
import mil.emp3.api.listeners.ICameraEventListener;
import mil.emp3.api.utils.ManagerFactory;

/**
 * This class provides the Camera functionality. It encapsulates a GeoCamera. Once a camera object is created and set on at least one map,
 * the apply method must be called after the camera values have been change in order to change the maps view.
 */
public class Camera implements ICamera {

    final static private IEventManager eventManager = ManagerFactory.getInstance().getEventManager();
    final static private ICoreManager coreManager   = ManagerFactory.getInstance().getCoreManager();

    /**
     * This is the backing/describing implementation instance as passed in through the
     * copy constructor or created new by the default constructor. This is always active
     * so we do not need to perform null checks when passing through getters and setters.
     */
    private final IGeoCamera geoCamera;

    /**
     * This default constructor creates a GeoCamera.
     */
    public Camera() {
        this.geoCamera = new GeoCamera();
        if (this.geoCamera.getAltitudeMode() == null) {
            this.geoCamera.setAltitudeMode(AltitudeMode.ABSOLUTE);
        }
    }

    /**
     * This constructor creates the Camera with the iGeoCamera encapsulated within.
     * @param camera An object that implements the IGeoCamera interface. See {@link IGeoCamera}
     */
    public Camera(IGeoCamera camera) {
        this.geoCamera = camera;
        if (this.geoCamera.getAltitudeMode() == null) {
            this.geoCamera.setAltitudeMode(AltitudeMode.ABSOLUTE);
        }
    }

    /**
     * Copy constructor
     */

    public Camera(ICamera from) {
        this.geoCamera = new GeoCamera();
        copySettingsFrom(from);
        if (this.geoCamera.getAltitudeMode() == null) {
            this.geoCamera.setAltitudeMode(AltitudeMode.ABSOLUTE);
        }
    }

    /**
     * Everything except geoId is copied as we don't want to change geoId after instantiation.
     * @param from An object that implements the ICamera interface.
     */
    @Override
    public void copySettingsFrom (ICamera from) {
        this.geoCamera.setAltitude(from.getAltitude());
        this.geoCamera.setAltitudeMode(from.getAltitudeMode());
        this.geoCamera.setHeading(from.getHeading());
        this.geoCamera.setLatitude(from.getLatitude());
        this.geoCamera.setLongitude(from.getLongitude());
        this.geoCamera.setRoll(from.getRoll());
        this.geoCamera.setTilt(from.getTilt());
    }
    
    @Override
    public EventListenerHandle addCameraEventListener(ICameraEventListener listener) throws EMP_Exception {
        return eventManager.addEventHandler(EventListenerTypeEnum.CAMERA_EVENT_LISTENER, this, listener);
    }

    @Override
    public void removeEventListener(EventListenerHandle listenerHandle) {
        eventManager.removeEventHandler(listenerHandle);
    }

    /**
     * This method sets the tilt on the camera.
     * @param value The tilt angle in degrees. The valid range is 0 - 180. A 0 deg tilt points the camera downward, a 90 deg tilt points the camera towards the horizon, and a 180 deg tilt points the camera skyward. An InvalidParameterException is raised if the value is out of range or NaN.
     */
    @Override
    public void setTilt(double value) {
        if (Double.isNaN(value) || (value < global.CAMERA_TILT_MINIMUM) || (value > global.CAMERA_TILT_MAXIMUM)) {
            throw new InvalidParameterException("The value is out of range.");
        }

        this.geoCamera.setTilt(value);
    }

    /**
     * This method retrieves the current tilt setting of the camera.
     * @return The current tilt in degrees.
     */
    @Override
    public double getTilt() {
        return this.geoCamera.getTilt();
    }

    /**
     * This method sets the roll on the camera. If this camera is the current camera on
     * any map, it will change the view on the map.
     * @param value The roll angle in degrees. The valid range is -180 - 180. An InvalidParameterException is raised if the value is out of range or NaN.
     */
    @Override
    public void setRoll(double value) {
        if (Double.isNaN(value) || (value < global.CAMERA_ROLL_MINIMUM) || (value > global.CAMERA_ROLL_MAXIMUM)) {
            throw new InvalidParameterException("The value is out of range.");
        }

        this.geoCamera.setRoll(value);
    }

    /**
     * This method retrieves the current roll setting of the camera.
     * @return The current roll in degrees.
     */
    @Override
    public double getRoll() {
        return this.geoCamera.getRoll();
    }

    /**
     * This method sets the cameras heading. Setting this value on a camera that is
     * associated with a map will cause the map to change its viewing area.
     * @param heading The new heading in degrees from north. An InvalidParameterException is raised if the value is out of range (-180 to 360) or NaN.
     */
    @Override
    public void setHeading(double heading) {
        if (Double.isNaN(heading) || (heading < global.HEADING_MINIMUM) || (heading > global.HEADING_MAXIMUM)) {
            throw new InvalidParameterException("The value is out of range.");
        }
        this.geoCamera.setHeading(heading);
    }

    /**
     * This method retrieves the current azimuth from the camera.
     * @return The azimuth in degrees from north.
     */
    @Override
    public double getHeading() {
        return this.geoCamera.getHeading();
    }

    /**
     * This method set the altitude mode for the elevation setting. Setting this value on a camera that is
     * associated with a map will cause the map to change its viewing area.
     * @param value The new altitude mode. See {@link IGeoAltitudeMode.AltitudeMode} An InvalidParameterException is raised if the value is null.
     */
    @Override
    public void setAltitudeMode(IGeoAltitudeMode.AltitudeMode value) {
        if (null == value) {
            throw new InvalidParameterException("The value can not be null.");
        }
        this.geoCamera.setAltitudeMode(value);
    }

    /**
     * This method retrieves the current altitude mode setting.
     * @return See {@link IGeoAltitudeMode.AltitudeMode}
     */
    @Override
    public IGeoAltitudeMode.AltitudeMode getAltitudeMode() {
        return this.geoCamera.getAltitudeMode();
    }

    /**
     * This method sets the latitude of the camera position. Setting this value on a camera that is
     * associated with a map will cause the map to change its viewing area.
     * @param value The latitude in degrees. The value must be -90.0 and 90 degrees. An InvalidParameterException is raised if the value is out of range or NaN.
     */
    @Override
    public void setLatitude(double value) {
        if (Double.isNaN(value) || (value < global.LATITUDE_MINIMUM) || (value > global.LATITUDE_MAXIMUM)) {
            throw new InvalidParameterException("The value is out of range.");
        }
        this.geoCamera.setLatitude(value);
    }

    /**
     * This method retrieves the current latitude from the camera.
     * @return The current latitude in degrees.
     */
    @Override
    public double getLatitude() {
        return this.geoCamera.getLatitude();
    }

    /**
     * This method sets the longitude of the camera position. Setting this value on a camera that is
     * associated with a map will cause the map to change its viewing area.
     * @param value The longitude in degrees. The value must be -180.0 and 180.0 degrees. An InvalidParameterException is raised if the value is out of range or NaN.
     */
    @Override
    public void setLongitude(double value) {
        if (Double.isNaN(value) || (value < global.LONGITUDE_MINIMUM) || (value > global.LONGITUDE_MAXIMUM)) {
            throw new InvalidParameterException("The value is out of range.");
        }
        this.geoCamera.setLongitude(value);
    }

    /**
     * This method retrieves the current longitude setting from the camera.
     * @return The current longitude in degrees.
     */
    @Override
    public double getLongitude() {
        return this.geoCamera.getLongitude();
    }

    /**
     * This method sets the altitude of the camera. Setting this value on a camera that is
     * associated with a map will cause the map to change its viewing area.
     * @param value The new altitude in meters.
     */
    @Override
    public void setAltitude(double value) {
        this.geoCamera.setAltitude(value);
    }

    /**
     * This method retrieves the current altitude setting of the camera.
     * @return The altitude in meters.
     */
    @Override
    public double getAltitude() {
        return this.geoCamera.getAltitude();
    }

    /**
     * This method allows the user to give the camera a name.
     * @param s The new name for the camera.
     */
    @Override
    public void setName(String s) {
        this.geoCamera.setName(s);
    }

    /**
     * This method retrieves the current name.
     * @return A string or null.
     */
    @Override
    public String getName() {
        return this.geoCamera.getName();
    }

    @Override
    public void setGeoId(java.util.UUID uuid) {
        this.geoCamera.setGeoId(uuid);
    }

    /**
     * This method retrieves the cameras unique identifier.
     * @return a UUID. See {@link java.util.UUID}
     */
    @Override
    public UUID getGeoId() {
        return this.geoCamera.getGeoId();
    }

    @Override
    public void setDataProviderId(String s) {
        this.geoCamera.setDataProviderId(s);
    }

    @Override
    public String getDataProviderId() {
        return this.geoCamera.getDataProviderId();
    }

    /**
     * This method allows the user to set a description.
     * @param s String
     */
    @Override
    public void setDescription(String s) {
        this.geoCamera.setDescription(s);
    }

    /**
     * This method retrieves the cameras description.
     * @return String or null.
     */
    @Override
    public String getDescription() {
        return this.geoCamera.getDescription();
    }

    @Override
    public HashMap<String, String> getProperties() {
        return this.geoCamera.getProperties();
    }

    @Override
    public void apply(boolean animate) {
        coreManager.processCameraSettingChange(this, animate);
    }
}
