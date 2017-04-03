package mil.emp3.api.interfaces;

import org.cmapi.primitives.IGeoLookAt;
import org.cmapi.primitives.IGeoPosition;

import mil.emp3.api.exceptions.EMP_Exception;
import mil.emp3.api.listeners.EventListenerHandle;
import mil.emp3.api.listeners.ILookAtEventListener;

/**
 * This class defines the interface to a lookAt.
 */
public interface ILookAt extends IGeoLookAt{
    /**
     * This method copies all settings from the LookAt object provided. The settings copied are
     * the coordinates, altitude, altitude mode, tilt, roll, range and azimuth.
     * @param from An object that implements the ILookAt interface.
     */
    void copySettingsFrom(ILookAt from);

    /**
     * This method adds an event listener to the LookAt.
     * @param listener A reference to a class object that implements the {@link  ILookAtEventListener} interface.
     * @return The handle to the event registration. See {@link EventListenerHandle}
     * @throws EMP_Exception if the listener is null.
     */
    EventListenerHandle addLookAtEventListener(ILookAtEventListener listener) throws EMP_Exception;

    /**
     * This method must be used to remove the registration from the system.
     * @param hListener  The handle returned by the call to addLookAtEventListener. See {@link EventListenerHandle}
     */
    void removeEventListener(EventListenerHandle hListener);

    /**
     * This method causes any changes done to the LookAt settings to be applied to any map or object that it may
     * be associated with.
     * @param animate If set to true then camera movement will be animated.
     */
    void apply(boolean animate);

    /**
     * Set the lookkAt position. If position parameters are invalid an exception is thrown.
     * If altitudeMode is null then it will then original altitude mode is maintained.
     * @param latitude
     * @param longitude
     * @param altitude
     * @param altitudeMode
     */
    void setPosition(double latitude, double longitude, double altitude, AltitudeMode altitudeMode);

    /**
     * set the lookAt position. If position is null or position parameters are invalid an exception is thrown.
     * Note that altitude mode is not affected by this method.
     * @param position
     */
    void setPosition(IGeoPosition position);
}
