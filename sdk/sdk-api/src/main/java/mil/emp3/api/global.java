package mil.emp3.api;

import mil.emp3.api.interfaces.IContainer;
import mil.emp3.api.interfaces.IEmpPropertyList;
import mil.emp3.api.interfaces.IFeature;
import mil.emp3.api.interfaces.IOverlay;
import mil.emp3.api.interfaces.core.IStorageManager;
import mil.emp3.api.utils.ManagerFactory;


/**
 *
 * This static class provides global method to retrieve data or configuration setting.
 *
 * A Note on Input Validation of double values:
 *
 * According to The Java Language Specification (JLS), §4.2.3, "Floating-Point Types, Formats, and Values" [JLS 2015]:
 * NaN (not-a-number) is unordered, so the numerical comparison operators <, <=, >, and >= return false if either or both
 * operands are NaN. The equality operator == returns false if either operand is NaN, and the inequality operator != returns true
 * if either operand is NaN.
 */
//  This class name is all lower case by design.
public class global {
    static final private IStorageManager storageManager = ManagerFactory.getInstance().getStorageManager();

    public static final double LATITUDE_MINIMUM = -90.0;
    public static final double LATITUDE_MAXIMUM = 90.0;

    public static final double LONGITUDE_MINIMUM = -180.0;
    public static final double LONGITUDE_MAXIMUM = 180.0;

    public static final double HEADING_MINIMUM = -360.0;  // Changed from -180.0 to -360.0 mission command render-er
    public static final double HEADING_MAXIMUM = 360.0;


    public static final double CAMERA_TILT_MINIMUM = 0.0;
    public static final double CAMERA_TILT_MAXIMUM = 180.0;
    public static final double CAMERA_TILT_TO_GROUND = CAMERA_TILT_MINIMUM;
    public static final double CAMERA_TILT_TO_HORIZON = 90.0;
    public static final double CAMERA_TILT_TO_SKY = CAMERA_TILT_MAXIMUM;

    public static final double CAMERA_ROLL_MINIMUM = -180.0;
    public static final double CAMERA_ROLL_MAXIMUM = 180.0;
    public static final double CAMERA_ROLL_LEVEL = 0.0;

    public static final double MINIMUM_DISTANCE = 1.0;   // Used for checking minimum radius, height etc.

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

    public static double modulus(double value1, double value2) {
        return ((value1 % value2) + value2) % value2;
    }
}
