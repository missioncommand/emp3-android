package mil.emp3.api.enums;

import mil.emp3.api.interfaces.IEventEnum;

/**
 * {@link mil.emp3.api.KMLS} service executes in the background. {@link mil.emp3.api.events.KMLSEvent} notifies the client application when
 * background procedure is complete. KMLSEventEnum enumerates the various events.
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
    /**
     * KML Service file retrieval failed
     */
    KML_SERVICE_FILE_RETRIEVAL_FAILED,
    /**
     * File specified by URL is neither a kml file nor z properly packed kmz file
     */
    KML_SERVICE_FILE_INVALID,
    /**
     * Failed to parse extracted kml file
     */
    KML_SERVICE_PARSE_FAILED
}
