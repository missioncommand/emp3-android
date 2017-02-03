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
 */
//  This class name is all lower case by design.
public class global {
    static final private IStorageManager storageManager = ManagerFactory.getInstance().getStorageManager();

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
