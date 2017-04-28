package mil.emp3.api.interfaces.core.storage;

import java.io.File;
import java.util.UUID;

import mil.emp3.api.interfaces.IKML;
import mil.emp3.api.interfaces.IKMLS;
import mil.emp3.api.interfaces.IMap;

/*
 * This is an EMP core internal interface. It should not be used by client applications. It is part of a collection of
 * classes that implement KML Service. This interface is required so that we can store a KML Request in ClientMapRestoreData.
 */

public interface IKMLSRequest {

    /*
     * Get the KML File path associated with this KML Service Request
     */
    String getKmlFilePath();

    /*
     * Get the client Map associated with this KMLS Request
     */
    IMap getMap();

    /*
     * Get the KML Service associated with this KML Request
     */
    IKMLS getService();

    /*
     * Get the directory where KMZ/KML file associated with this request is stored.
     */
    File getKmzDirectory();

    /*
     * Get the full path of the KMZ file associated with this request
     */
    String getKmzFilePath();

    /*
     * Get/Set the KML Feature associated with tis request
     */
    IKML getFeature();
    void setFeature(IKML feature);

    /*
     * Get the Id associated with this feature.
     */
    UUID getId();
}
