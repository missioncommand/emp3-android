package mil.emp3.api.interfaces.core.storage;

import java.util.UUID;

import mil.emp3.api.interfaces.IKML;

/*
 * This is an EMP core internal interface. It should not be used by client applications. It is part of a collection of
 * classes that implement KML Service. This interface is required so that we can store a KML Request in ClientMapRestoreData.
 */

public interface IKMLSRequest {
    IKML getFeature();
    UUID getId();
}
