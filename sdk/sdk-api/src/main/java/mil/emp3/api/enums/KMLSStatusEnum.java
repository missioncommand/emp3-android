package mil.emp3.api.enums;

/**
 * Status of the addMapService primitive executed on KML Service object
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
