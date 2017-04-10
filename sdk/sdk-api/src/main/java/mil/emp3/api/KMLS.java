package mil.emp3.api;

import android.content.Context;

import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import mil.emp3.api.abstracts.MapService;
import mil.emp3.api.enums.KMLSStatusEnum;
import mil.emp3.api.exceptions.EMP_Exception;
import mil.emp3.api.interfaces.IKML;
import mil.emp3.api.interfaces.IKMLS;
import mil.emp3.api.interfaces.IMap;
import mil.emp3.api.listeners.IKMLSEventListener;

/**
 * Implements the KML Map Service. This service used by the application to fetch a KMZ file, convert it to KMLFeature and
 * draw it on a background Layer.
 */
public class KMLS extends MapService implements IKMLS {
    private Map<UUID, KMLSStatusEnum> KMLSStatusMap = new HashMap<>();
    private final Context context;
    private final boolean isPersistent;
    private final IKMLSEventListener listener;
    private KML feature;

    /**
     * Create a KMLS service
     * @param context Android context, required to find a directory to copy the fetched KMZ file
     * @param serviceURL URL for the KMZ.
     * @param listener client application must install a listener
     * @param isPersistent decides if service should remain installed across application restarts
     * @throws MalformedURLException
     * @throws IllegalArgumentException
     */
    public KMLS(Context context, String serviceURL, boolean isPersistent, IKMLSEventListener listener) throws MalformedURLException {
        super(serviceURL);
        if(null == context) {
            throw new IllegalArgumentException("context must be specified");
        }
        if(null == listener) {
            throw new IllegalArgumentException("Listener must not be null");
        }
        this.context = context;
        this.isPersistent = isPersistent;
        this.listener = listener;
    }

    /**
     * Creates a KMLS service using application specified UUID.
     * @param context Android context, required to find a directory to copy the fetched KMZ file
     * @param serviceURL URL for the KMZ.
     * @param isPersistent decides if service should remain installed across application restarts
     * @param listener client application must install a listener
     * @param geoId
     * @throws MalformedURLException
     * @throws IllegalArgumentException
     */
    public KMLS(Context context, String serviceURL, boolean isPersistent, IKMLSEventListener listener, UUID geoId) throws MalformedURLException {
        this(context, serviceURL, isPersistent, listener);
        if(null == geoId) {
            throw new IllegalArgumentException("geoId must be non-null");
        }
        setGeoId(geoId);
    }

    /**
     * Fetches the current status of the service for the specified client map.
     * @param mapClient
     * @return
     * @throws EMP_Exception
     */
    @Override
    public KMLSStatusEnum getStatus(IMap mapClient) throws EMP_Exception {
        if(null == mapClient) {
            throw new IllegalArgumentException("mapClient must be non-null");
        }
        KMLSStatusEnum status = KMLSStatusMap.get(mapClient.getGeoId());
        if(null == status) {
            throw new EMP_Exception(EMP_Exception.ErrorDetail.INVALID_MAP, "Service wasn't added to the map");
        }
        return status;
    }

    /*
     * Used by EMP Core components to set the current status of the service for a specific mapClient. Client Applications
     * should not use this method.
     */
    public void setStatus(IMap mapClient, KMLSStatusEnum status) {
        KMLSStatusMap.put(mapClient.getGeoId(), status);
    }

    /**
     * Fetch the context with which object was initialized.
     * @return
     */
    @Override
    public Context getContext() {
        return context;
    }

    /**
     * Returns true if service was created as persistent service else returns false
     * @return
     */
    public boolean isPersistent() {
        return isPersistent;
    }

    /**
     * Returns reference to client application installed listener.
     * @return
     */
    public IKMLSEventListener getListener() {
        return listener;
    }

    /**
     * Sets the KML feature. This is used internally for EMP to store the feature that generated from the KMZ.
     * Client applications shouldn't use this method.
     * @param feature
     */

    @Override
    public void setFeature(KML feature) {
        this.feature = feature;
    }

    /**
     * Fetches the feature that was generated from the KMZ.
     * @return
     */
    @Override
    public IKML getFeature() {
        return feature;
    }
}
