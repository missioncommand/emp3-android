package mil.emp3.api.interfaces;

import org.cmapi.primitives.IGeoCamera;

import mil.emp3.api.exceptions.EMP_Exception;
import mil.emp3.api.listeners.EventListenerHandle;
import mil.emp3.api.listeners.ICameraEventListener;

/**
 * This class defines the interface to a camera.
 * @author ish.rivera
 */
public interface ICamera extends IGeoCamera {
    /**
     * This method copies all settings from the camera object provided. The settings copied are
     * the coordinates, altitude, altitude mode, tilt, roll, range and azimuth.
     * @param from An object that implements the ICamera interface.
     */
    void copySettingsFrom(ICamera from);
    
    /**
     * This method adds an event listener to the camera.
     * @param listener A reference to a class object that implements the {@link  ICameraEventListener} interface.
     * @return The handle to the event registration. See {@link EventListenerHandle}
     */
    EventListenerHandle addCameraEventListener(ICameraEventListener listener) throws EMP_Exception;
    
    /**
     * This method must be used to remove the registration from the system.
     * @param hListener  The handle returned by the call to addCameraEventListener. See {@link EventListenerHandle}
     */
    void removeEventListener(EventListenerHandle hListener);

    /**
     * This method causes any changes done to the camera settings to be applied to any map or object that it may
     * be associated with.
     * @param userContext user defined object
     * @param animate If set to true then camera movement will be animated.
     */
    void apply(boolean animate, Object userContext);

    /**
     * This method causes any changes done to the camera settings to be applied to any map or object that it may
     * be associated with.
     * @param animate If set to true then camera movement will be animated.
     */
    void apply(boolean animate);

}
