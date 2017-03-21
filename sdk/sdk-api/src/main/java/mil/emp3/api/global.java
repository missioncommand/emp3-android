package mil.emp3.api;

import mil.emp3.api.interfaces.IContainer;
import mil.emp3.api.interfaces.IEmpPropertyList;
import mil.emp3.api.interfaces.IFeature;
import mil.emp3.api.interfaces.IOverlay;
import mil.emp3.api.interfaces.core.ICoreManager;
import mil.emp3.api.interfaces.core.IStorageManager;
import mil.emp3.api.utils.ManagerFactory;


/**
 *
 * This static class provides global method to retrieve data or configuration setting.
 */
//  This class name is all lower case by design.
public class global {
    static final private IStorageManager storageManager = ManagerFactory.getInstance().getStorageManager();

    public static final double LATITUDE_MINIMUM = -90.0;
    public static final double LATITUDE_MAXIMUM = 90.0;

    public static final double LONGITUDE_MINIMUM = -180.0;
    public static final double LONGITUDE_MAXIMUM = 180.0;

    public static final double HEADING_MINIMUM = -180.0;
    public static final double HEADING_MAXIMUM = 360.0;


    public static final double CAMERA_TILT_MINIMUM = 0.0;
    public static final double CAMERA_TILT_MAXIMUM = 180.0;
    public static final double CAMERA_TILT_TO_GROUND = CAMERA_TILT_MINIMUM;
    public static final double CAMERA_TILT_TO_HORIZON = 90.0;
    public static final double CAMERA_TILT_TO_SKY = CAMERA_TILT_MAXIMUM;

    public static final double CAMERA_ROLL_MINIMUM = -180.0;
    public static final double CAMERA_ROLL_MAXIMUM = 180.0;
    public static final double CAMERA_ROLL_LEVEL = 0.0;



    /**
     * This method finds a container with the specified ID.
     * @param uuid The unique identifier of the container.
     * @return IContainer or null if its not found.
     */
    public static IContainer findContainer(java.util.UUID uuid) {
        return storageManager.findContainer(uuid);
    }

    /**
     * This method finds an overlay with the specified ID.
     * @param uuid The unique identifier of the overlay.
     * @return IOveraly or null if its not found.
     */
    public static IOverlay findOverlay(java.util.UUID uuid) {
        IContainer oContainer = global.findContainer(uuid);
        
        if (oContainer instanceof IOverlay) {
            return (IOverlay) oContainer;
        }
        
        return null;
    }

    /**
     * This method finds a feature with the specified ID.
     * @param uuid The unique identifier of the feature.
     * @return A feature object or null if its not found.
     */
    public static IFeature findFeature(java.util.UUID uuid) {
        IContainer oContainer = global.findContainer(uuid);
        
        if (oContainer instanceof IFeature) {
            return (IFeature) oContainer;
        }
        
        return null;
    }

    public static void setConfiguration(IEmpPropertyList properties) {

    }
}
