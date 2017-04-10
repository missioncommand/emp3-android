package mil.emp3.api.enums;

import mil.emp3.api.interfaces.IEventEnum;

/**
 * KML add service action executes in the background. This event notifies the client application when background procedure
 * is complete.
 */

public enum KMLSEventEnum implements IEventEnum {
    /**
     * KML or KMZ file was retrieved
     */
    KML_SERVICE_FILE_RETRIEVED,

    /**
     * If file was KMZ file then it was exploded
     */
    KML_SERVICE_FILE_EXPLODED,

    /**
     * KMZ/KML file was parsed
     */
    KML_SERVICE_FILE_PARSED,
    /**
     * Features were drawn on the map
     */
    KML_SERVICE_FEATURES_DRAWN,
    /**
     * KML Service failed to load
     */
    KML_SERVICE_INSTALL_FAILED,
}
