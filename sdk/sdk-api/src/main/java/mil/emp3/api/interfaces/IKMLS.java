package mil.emp3.api.interfaces;

import android.content.Context;

import mil.emp3.api.KML;
import mil.emp3.api.enums.KMLSStatusEnum;
import mil.emp3.api.exceptions.EMP_Exception;
import mil.emp3.api.listeners.IKMLSEventListener;

/**
 * Requirement 3.4.1.4: Display KML Service Data, the Map Engine (Smart Client) shall display data loaded obtained from external KML services.
 * <p>
 * Rationale: At times, it may be necessary to display geo-spatial information from other systems.
 * </p><p>
 * The KML format is XML notation for geographic annotation and visualization.
 * </p><p>
 * Please see <a href="https://developers.google.com/kml/documentation/kmzarchives"> for structure of KMZ file</a>
 * </p>
 * Some design decisions:
 * <ul>
 * <li>Features created by KMZ are treated as a special layer in Map Instance. They are not added to any overlay within the core.</li>
 * <li>Features created via KMZ are not returned when getAllMapFeatures is executed.</li>
 * <li>KMZ referring to another KMZ is not supported as our parser skips over network links.</li>
 * <li>Decision regarding replication via mirror-cache is pending.</li>
 * </ul>
 *
 * Application client creates an instance of {@link mil.emp3.api.KMLS} object and uses the {@link IMap#addMapService(IMapService)} to add the KML service
 * to an existing Map.
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
    void setFeature(IKML feature);

    /**
     * Get the KML Features created from the KMZ file that was specified when KMLS object was created. Client applications
     * should not make any changes to this object.
     * @return
     */
    IKML getFeature();
}
