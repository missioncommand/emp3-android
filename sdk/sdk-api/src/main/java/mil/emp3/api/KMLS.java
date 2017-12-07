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
import mil.emp3.api.interfaces.IMapService;
import mil.emp3.api.listeners.IKMLSEventListener;

/**
 * KMLS Implements the KML Map Service. This service used by the application to fetch a KMZ/KML file, convert it to KMLFeature and
 * draw it on a background Layer.
 * <p>
 * The URL specified when constructing the KML Service must be a valid "file" or "network" URL. If applications developer expects that the host machine
 * may loose network connectivity then it is recommended that client application fetch the KMZ/KML file and store it locally. EMP doesn't store the
 * fetched file, it removes the previously fetched files on startup. This approach was chosen to avoid having to manage the storage space usage.
 * Additionally, if the KML file references any network resources and host machine looses network connectivity then rendering of those artifacts
 * shall fail. To avoid this scenario it is recommended that you build the KMZ archives that are self sufficient.
 * </p>
 * <p>
 * The KMZ file is copied to the applications private storage, root KMLS directory is created under the folder returned by executing 'getDir()' on the
 * the context object supplied by the client application. Each KMZ/KML is stored in its own folder.
 * </p>
 * <p>
 * There are four steps involved in completing the addMapService request:
 * <ul>
 * <li>Fetch the KMZ/KML file using the application supplied URL</li>
 * <li>Explode the KMZ file archive, if it is a KMZ file</li>
 * <li>Parse the KML file and build a list of features to be rendered</li>
 * <li>Request the map engine to render the features on a background layer</li>
 * </ul>
 * As each of these steps are completed, user supplied listener is invoked with appropriate event {@link mil.emp3.api.events.KMLSEvent}. Client applications
 * may also use the {@link #getStatus(IMap)} to check the current status of the service.
 * </p>
 * <p>
 * It is client applications responsibility to use {@link IMap#removeMapService(IMapService)} to remove failed map service from the map.
 * </p>
 * <p>
 * In the current implementation storage is cleaned up only on application restart. This limitation is in effect to support the case where there are
 * multiple map engines and a service was was added to multiple map engines. Once a better
 * </p>
 * <p>
 * The feature member of this class is for use by the EMP core component. Client application should never set it or get it.
 * </p>
 *
 * <pre>

     class KMLSServiceListener implements IKMLSEventListener {
         final IMap map;
         KMLSServiceListener(IMap map) {
             this.map = map;
         }

         public void onEvent(KMLSEvent event) {
             try {
                 Log.d(TAG, "KMLSServiceListener-onEvent " + event.getEvent().toString() + " status " + event.getTarget().getStatus(map));
             } catch(EMP_Exception e) {
                 Log.e(TAG, e.getMessage(), e);
             }
         }
     }

     // Somewhere in your activity
     IMap map;
     .....
     ....
     try {
         KMLS kmls = new KMLS(activity, "https://github.com/downloads/brazzy/nikki/example.kmz", new KMLSServiceListener(map));
         kmls.setName("example");
         map.addMapService(kmls);
     } catch (Exception e) {
         Log.e(TAG, e.getMessage(), e);
     }
 * </pre>
 */
public class KMLS extends MapService implements IKMLS {
    final private Map<UUID, KMLSStatusEnum> KMLSStatusMap = new HashMap<>();
    private final Context context;
    private final IKMLSEventListener listener;
    private IKML feature; // We need to store the feature in this class because MapEngine API needs it to inert in the Layer.
                         // Client application should never set or get this feature.
    /**
     * Create a KMLS service
     * @param context Android context, required to find a directory to copy the fetched KMZ file
     * @param serviceURL URL for the KMZ.
     * @param listener client application must install a listener
     * @throws MalformedURLException
     * @throws IllegalArgumentException
     */
    public KMLS(final Context context, final String serviceURL, final IKMLSEventListener listener) throws MalformedURLException {
        super(serviceURL);
        if(null == context) {
            throw new IllegalArgumentException("context must be specified");
        }
        if(null == listener) {
            throw new IllegalArgumentException("Listener must not be null");
        }
        this.context = context;
        this.listener = listener;
    }

    /**
     * Creates a KMLS service using application specified UUID.
     * @param context Android context, required to find a directory to copy the fetched KMZ file
     * @param serviceURL URL for the KMZ.
     * @param listener client application must install a listener
     * @param geoId
     * @throws MalformedURLException
     * @throws IllegalArgumentException
     */
    public KMLS(final Context context, final String serviceURL, final IKMLSEventListener listener, final UUID geoId) throws MalformedURLException {
        this(context, serviceURL, listener);
        if(null == geoId) {
            throw new IllegalArgumentException("geoId must be non-null");
        }
    }

    /**
     * Fetches the current status of the service for the specified client map.
     * @param mapClient
     * @return
     * @throws EMP_Exception
     */
    @Override
    public KMLSStatusEnum getStatus(final IMap mapClient) throws EMP_Exception {
        if(null == mapClient) {
            throw new IllegalArgumentException("mapClient must be non-null");
        }
        final KMLSStatusEnum status = KMLSStatusMap.get(mapClient.getGeoId());
        if(null == status) {
            throw new EMP_Exception(EMP_Exception.ErrorDetail.INVALID_MAP, "Service wasn't added to the map");
        }
        return status;
    }

    /*
     * Used by EMP Core components to set the current status of the service for a specific mapClient. Client Applications
     * should not use this method.
     */
    public void setStatus(final IMap mapClient, final KMLSStatusEnum status) {
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
     * Returns reference to client application installed listener.
     * @return
     */
    public IKMLSEventListener getListener() {
        return listener;
    }

    @Override
    public String toString() {
        String str = "KMLS: " + super.toString();
        if(null != getName()) {
            str += "name: " + getName() + " ";
        }
        return str;
    }

    public void setGeoId(final java.util.UUID geoId){
        throw new IllegalStateException("GeoId can't be changed after construction");
    }

    @Override
    public void setFeature(final IKML feature) {
        this.feature = feature;
    }

    @Override
    public IKML getFeature() {
        return feature;
    }
}
