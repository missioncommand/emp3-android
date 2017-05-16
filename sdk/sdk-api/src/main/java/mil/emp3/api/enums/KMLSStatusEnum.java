package mil.emp3.api.enums;

/**
 * Status of the {@link mil.emp3.api.KMLS} service may be retrieved by the client application by invoking
 * {@link mil.emp3.api.interfaces.IKMLS#getStatus} method. The status returned by that method is enumerated by this class.
 */
public enum KMLSStatusEnum {

    /**
     * Service is queued for processing.
     */
    QUEUED,
    /**
     * Currently retrieving the file using the client application supplied URL
     */
    FETCHING,

    /**
     * Currently exploding the retrieved KMZ file
     */
    EXPLODING,

    /**
     * Parsing the the KML file
     */
    PARSING,

    /**
     * Features drawn on the map
     */
    DRAWN,

    /**
     * This service was removed by the user.
     */
    REMOVED,

    /**
     * Service installation has failed
     */
    FAILED
}
