package mil.emp3.api.interfaces;

import android.content.Context;

import mil.emp3.api.enums.KMLSStatusEnum;
import mil.emp3.api.exceptions.EMP_Exception;

/**
 * 3.4.1.4 Display KML Service Data
 * Requirement: The Map Engine (Smart Client) shall display data loaded obtained from external KML services.
 * Rationale: At times, it may be necessary to display geospatial information from other systems.
 * The KML format is XML notation for geographic annotation and visualization.
 */
public interface IKMLS extends IMapService {
    KMLSStatusEnum getStatus(IMap mapClient) throws EMP_Exception;
    Context getContext();
}
