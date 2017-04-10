package mil.emp3.api.interfaces;

import android.content.Context;

import mil.emp3.api.KML;
import mil.emp3.api.enums.KMLSStatusEnum;
import mil.emp3.api.exceptions.EMP_Exception;
import mil.emp3.api.listeners.IKMLSEventListener;

/**
 * 3.4.1.4 Display KML Service Data
 * Requirement: The Map Engine (Smart Client) shall display data loaded obtained from external KML services.
 * Rationale: At times, it may be necessary to display geo-spatial information from other systems.
 * The KML format is XML notation for geographic annotation and visualization.
 *
 * https://developers.google.com/kml/documentation/kmzarchives for structure of KMZ file, Important thing to note is:
 *
 * Some design decisions (also listed in the issue)
 *
 * 1. Features created by KMZ are treated as a special layer in Map Instance. They are not added to any overlay within the core.
 * 2. Features created via KMZ are not returned when getAllMapFeatures is executed.
 * 3. No event is generated when KMZ processing is complete or fails.
 * 4. KMZ referring to another KMZ is not supported as our parser skips over network links.
 * 5. Decision regarding replication via mirror-cache is pending.
 */
public interface IKMLS extends IMapService {
    /**
     * Gets the current status of KML Service request
     * @param mapClient
     * @return
     * @throws EMP_Exception
     */
    KMLSStatusEnum getStatus(IMap mapClient) throws EMP_Exception;

    /**
     * Get the Context provided by the client application when KMLS object was created
     * @return
     */
    Context getContext();

    /**
     * Returns application installed listener.
     * @return
     */
    IKMLSEventListener getListener();
    /**
     * KML Feature generated from the downloaded KMZ file is set by this method. Client applications should not use this method.
     * @param feature
     */
    void setFeature(KML feature);
    /**
     * Get the KML Features created from the KMZ file that was specified when KMLS object was created. Client applications
     * should not make any changes to this object.
     * @return
     */
    IKML getFeature();
}
