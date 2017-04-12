package mil.emp3.api;

import android.util.Log;

import org.cmapi.primitives.IGeoBase;
import org.cmapi.primitives.IGeoMilSymbol;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import mil.emp3.api.enums.ContainerEventEnum;
import mil.emp3.api.events.ContainerEvent;
import mil.emp3.api.exceptions.EMP_Exception;
import mil.emp3.api.interfaces.IFeature;
import mil.emp3.api.listeners.EventListenerHandle;
import mil.emp3.api.listeners.IContainerEventListener;
import mil.emp3.mirrorcache.MirrorCacheClient;
import mil.emp3.mirrorcache.MirrorCacheException;
import mil.emp3.mirrorcache.Transport.TransportType;
import mil.emp3.mirrorcache.channel.Channel;
import mil.emp3.mirrorcache.event.ChannelDeletedEvent;
import mil.emp3.mirrorcache.event.ChannelEventHandler;
import mil.emp3.mirrorcache.event.ChannelPublishedEvent;
import mil.emp3.mirrorcache.event.ChannelUpdatedEvent;
import mil.emp3.mirrorcache.event.EventRegistration;
import mil.emp3.mirrorcache.spi.MirrorCacheClientProvider;
import mil.emp3.mirrorcache.spi.MirrorCacheClientProviderFactory;

/**
 * This class provides a simplified means of interacting with a MirrorCache service by
 * only allowing a user to subscribe to {@link Overlay} (product) instances being
 * offered by remote users, and to offer {@link Overlay} instances to a MirrorCache
 * service for other remote users to subscribe to.
 */
public class MirrorCache {
    static final private String TAG            = MirrorCache.class.getSimpleName();
    static final private String PRODUCT_PREFIX = "product:";

    private boolean isConnected;

    final private MirrorCacheClient client;
    final private Map<String, Product> localProductMap;

    /**
     * Initializes this instance with a MirrorCache service endpoint.
     *
     * @param endpointUri the MirrorCache service endpoint to communicate with
     *
     * @throws IllegalStateException If a MirrorCache error occurs
     * @throws NullPointerException If {@code endpointUri} is null
     */
    public MirrorCache(final URI endpointUri) {
        Objects.requireNonNull(endpointUri, "endpointUri == null");

        final MirrorCacheClient client;
        try {
            client = MirrorCacheClientProviderFactory.getClient(new MirrorCacheClientProvider.ClientArguments() {
                @Override
                public TransportType transportType() {
                    return TransportType.WEBSOCKET_PROGRAMMATIC;
                }
                @Override
                public URI endpoint() {
                    return endpointUri;
                }
            });
        } catch (MirrorCacheException e) {
            throw new IllegalStateException(e);
        }

        this.client          = client;
        this.localProductMap = new HashMap<>();
    }

    /**
     * Initializes this instance with a MirrorCache service endpoint.
     *
     * @param client an existing MirrorCacheClient implementation pre-configured
     *               to communicate with a MirrorCache service
     *
     * @throws NullPointerException If {@code client} is null
     */
    public MirrorCache(MirrorCacheClient client) {
        Objects.requireNonNull(client, "client == null");

        this.client          = client;
        this.localProductMap = new HashMap<>();
    }

    // -+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+- //
    // -+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+- //

    /**
     * Connects to the MirrorCache service.
     *
     * @throws EMP_Exception If a MirrorCache error occurs
     */
    public void connect() throws EMP_Exception {
        Log.d(TAG, "connect()");

        if (!isConnected()) {
            try {
                client.init();
                client.connect();

            } catch (MirrorCacheException e) {
                throw new EMP_Exception(EMP_Exception.ErrorDetail.OTHER, e.getReason().getMsg() + "\n" + e.getDetails(), e);
            }

            isConnected = true;
        }
    }

    /**
     * Disconnects from the MirrorCache service.
     *
     * @throws EMP_Exception If a MirrorCache error occurs
     */
    public void disconnect() throws EMP_Exception {
        Log.d(TAG, "disconnect()");

        if (isConnected()) {
            try {
                client.disconnect();
                client.shutdown();

                isConnected = false;

            } catch (MirrorCacheException e) {
                throw new EMP_Exception(EMP_Exception.ErrorDetail.OTHER, e.getReason().getMsg() + "\n" + e.getDetails(), e);
            }
        }
    }

    /**
     * Retrieves a list of productIds.
     *
     * @return the gathered productIds
     *
     * @throws EMP_Exception If a MirrorCache error occurs
     * @throws IllegalStateException If not connected
     */
    public List<String> getProductIds() throws EMP_Exception {
        Log.d(TAG, "getProductIds()");
        validateConnection();

        final List<String> productIds = new ArrayList<>();

        try {
            final List<Channel> channels = client.findChannels("*");
            for (Channel channel : channels) {
                if (channel.getName().startsWith(PRODUCT_PREFIX)) {
                    productIds.add(channel.getName());
                }
            }

            return productIds;

        } catch (MirrorCacheException e) {
            throw new EMP_Exception(EMP_Exception.ErrorDetail.OTHER, e.getReason().getMsg() + "\n" + e.getDetails(), e);
        }
    }

    /**
     * Subscribes to the provided productId.
     *
     * @param productId the productId to subscribe to.
     *
     * @return an Overlay whose contents are managed by the MirrorCache
     *
     * @throws EMP_Exception If a MirrorCache error occurs
     * @throws IllegalStateException If not connected
     * @throws NullPointerException If {@code productId} is null
     * @throws IllegalArgumentException If {@code productId} is incorrectly formatted
     */
    public Overlay subscribe(String productId) throws EMP_Exception {
        Log.d(TAG, "subscribe()");
        Objects.requireNonNull(productId, "productId == null");
        validateConnection();

        validateProductId(productId);
        Log.d(TAG, "productId: " + productId);

        if (!localProductMap.containsKey(productId)) { // new productId
            /*
             * Try to retrieve the productId from server.
             * Assemble a Product if successful.
             */
            try {
                final List<Channel> channels = client.findChannels("*");
                for (Channel channel : channels) {
                    if (channel.getName().equals(productId)) {

                        final Overlay overlay = new Overlay();
                        overlay.setName(channel.getName().substring(PRODUCT_PREFIX.length()));

                        final Product product = new Product(channel.getName(), channel, overlay);
                        product.subscribe();

                        localProductMap.put(product.getId(), product);

                        return product.getOverlay();
                    }
                }
                throw new EMP_Exception(EMP_Exception.ErrorDetail.OTHER, "productId not found on server: " + productId);

            } catch (MirrorCacheException e) {
                throw new EMP_Exception(EMP_Exception.ErrorDetail.OTHER, e.getReason().getMsg() + "\n" + e.getDetails(), e);
            }

        } else {
            final Product product = localProductMap.get(productId);
            product.subscribe();

            return product.getOverlay();
        }
    }

    /**
     * Unsubscribes from the provided productId.
     *
     * @param productId the productId to unsubscribe from
     *
     * @throws EMP_Exception If a MirrorCache error occurs
     * @throws IllegalStateException If not connected or if {@code productId} is not subscribed to
     * @throws NullPointerException If {@code productId} is null
     * @throws IllegalArgumentException If {@code productId} is incorrectly formatted
     */
    public void unsubscribe(String productId) throws EMP_Exception {
        Log.d(TAG, "unsubscribe()");
        Objects.requireNonNull(productId, "productId == null");
        validateConnection();

        validateProductId(productId);
        Log.d(TAG, "productId: " + productId);

        /*
         * Ensure subscription exists.
         */
        if (!localProductMap.containsKey(productId)) {
            throw new IllegalStateException("no subscriptions found for productId: " + productId);
        }

        try {
            final Product product = localProductMap.get(productId);
            product.unsubscribe();

        } finally {
            localProductMap.remove(productId);
        }
    }

    /**
     * Signals intent to have the provided Overlay and its contents
     * available to other users of the MirrorCache.
     *
     * @param overlay the Overlay to be managed by the MirrorCache
     *
     * @throws EMP_Exception If a MirrorCache error occurs
     * @throws IllegalStateException If not connected or if {@code overlay} was already added as a product
     * @throws NullPointerException If {@code overlay} is null
     * @throws IllegalArgumentException If {@code overlay.getName()} is invalid
     */
    public void addProduct(final Overlay overlay) throws EMP_Exception {
        Log.d(TAG, "addProduct()");
        Objects.requireNonNull(overlay, "overlay == null");
        validateConnection();

        final String productId = generateProductId(overlay);
        Log.d(TAG, "productId: " + productId);

        /*
         * Ensure product does not exist.
         */
        if (localProductMap.containsKey(productId)) {
            throw new IllegalStateException("productId already exists: " + productId);
        }

        try {
            Log.i(TAG, "Creating channel for overlay: " + productId);
            final Channel channel = client.createChannel(productId, Channel.Visibility.PUBLIC, Channel.Type.TEMPORARY);

            final Product product = new Product(channel.getName(), channel, overlay);
            product.subscribe();

            localProductMap.put(product.getId(), product);

        } catch (MirrorCacheException e) {
            throw new EMP_Exception(EMP_Exception.ErrorDetail.OTHER, e.getReason().getMsg() + "\n" + e.getDetails(), e);
        }
    }

    /**
     * Removes the Overlay from being available as a product within MirrorCache.
     *
     * @param overlay the Overlay to remove from MirrorCache
     * @param allowSubscribersToKeep if true, the local data previously received by
     *                               subscribers via this product will not be purged
     *
     * @throws EMP_Exception If a MirrorCache error occurs
     * @throws IllegalStateException If not connected or if {@code overlay} was not added as a product
     * @throws NullPointerException If {@code overlay} is null
     * @throws IllegalArgumentException If {@code overlay.getName()} is invalid
     */
    public void removeProduct(Overlay overlay, boolean allowSubscribersToKeep) throws EMP_Exception {
        Log.d(TAG, "removeProduct()");
        Objects.requireNonNull(overlay, "overlay == null");
        validateConnection();

        final String productId = generateProductId(overlay);
        Log.d(TAG, "productId: " + productId);

        /*
         * Ensure product exists.
         */
        if (!localProductMap.containsKey(productId)) {
            throw new IllegalStateException("productId does not exist: " + productId);
        }

        try {
            final Product product = localProductMap.get(productId);
            product.unsubscribe();

            Log.d(TAG, "Removing channel for overlay: " + productId);
            client.deleteChannel(productId);

        } catch (MirrorCacheException e) {
            throw new EMP_Exception(EMP_Exception.ErrorDetail.OTHER, e.getReason().getMsg() + "\n" + e.getDetails(), e);

        } finally {
            localProductMap.remove(productId);
        }
    }

    // -+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+- //
    // -+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+- //

    private boolean isConnected() {
        return isConnected;
    }

    private void validateConnection() {
        if (!isConnected()) {
            throw new IllegalStateException("!isConnected()");
        }
    }

    /**
     * Validates the format of a productId.
     *
     * @param productId the productId to validate
     *
     * @throws IllegalArgumentException If {@code productId} is incorrectly formatted
     */
    static private void validateProductId(String productId) {
        if (!productId.startsWith(PRODUCT_PREFIX) || productId.length() == PRODUCT_PREFIX.length()) {
            throw new IllegalArgumentException("Invalid productId. (!productId.startsWith(PRODUCT_PREFIX) || productId.length() == PRODUCT_PREFIX.length())");
        }
    }

    /**
     * Constructs a productId based on Overlay name.
     *
     * @param overlay the Overlay used to construct the productId
     *
     * @return the constructed productId
     *
     * @throws IllegalArgumentException If {@code overlay.getName()} is invalid
     */
    static private String generateProductId(Overlay overlay) {
        final String overlayName = overlay.getName();

        if (overlayName == null || overlayName.trim().length() == 0) {
            throw new IllegalArgumentException("Invalid overlayName: " + overlayName);
        }

        return PRODUCT_PREFIX + overlayName;
    }

    // -+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+- //
    // -+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+- //

    static private class Product {
        final private Set<String> ignoredPublishedGeoIds = Collections.newSetFromMap(new ConcurrentHashMap<String, Boolean>());
        final private Set<String> ignoredDeletedGeoIds   = Collections.newSetFromMap(new ConcurrentHashMap<String, Boolean>());

        private List<EventRegistration> channelEventRegistrations = new ArrayList<>();
        private EventListenerHandle overlayEventRegistration;

        final private String id;
        final private Channel channel;
        final private Overlay overlay;

        public Product(String id, Channel channel, Overlay overlay) {
            this.id      = id;
            this.channel = channel;
            this.overlay = overlay;
        }

        public String getId() {
            return id;
        }
        public Channel getChannel() {
            return channel;
        }
        public Overlay getOverlay() {
            return overlay;
        }

        // -+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+- //
        // -+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+- //

        public void subscribe() throws EMP_Exception {
            /*
             * Hook into Channel events.
             */
            final ChannelEventHandler handler = new ChannelEventHandler() {
                @Override public void onChannelPublishedEvent(ChannelPublishedEvent event) {
                    Log.d(TAG, "ChannelEventHandler[" + channel.getName() + "].onChannelPublishedEvent()");

                    /*
                     * An item was published to the channel, update the overlay.
                     */
                    final Object data = event.getMessage().getPayload().getData();
                    if (data instanceof IGeoMilSymbol) {
                        try {
                            final MilStdSymbol milStdSymbol = new MilStdSymbol((IGeoMilSymbol) data);

                            ignoredPublishedGeoIds.add(milStdSymbol.getGeoId().toString());
                            overlay.addFeature(milStdSymbol, true);

                        } catch (EMP_Exception e) {
                            Log.e(TAG, e.getMessage(), e);
                        }
                    }
                }
                @Override public void onChannelDeletedEvent(ChannelDeletedEvent event) {
                    Log.d(TAG, "ChannelEventHandler[" + channel.getName() + "].onChannelDeletedEvent()");

                    /*
                     * An item was deleted from the channel, update the overlay.
                     */
                    final String payloadId = event.getPayloadId();

                    //TODO can we remove a feature by geoId instead?
                    for (IFeature feature : overlay.getFeatures()) {
                        if (payloadId.equals(feature.getGeoId().toString())) {
                            try {
                                ignoredDeletedGeoIds.add(feature.getGeoId().toString());
                                overlay.removeFeature(feature);
                            } catch (EMP_Exception e) {
                                Log.e(TAG, e.getMessage(), e);
                            }
                            break;
                        }
                    }

                }
                @Override public void onChannelUpdatedEvent(ChannelUpdatedEvent event) {
                    Log.d(TAG, "ChannelEventHandler[" + channel.getName() + "].onChannelUpdatedEvent()");
                }
            };

            this.channelEventRegistrations.add(channel.on(ChannelPublishedEvent.TYPE, handler));
            this.channelEventRegistrations.add(channel.on(ChannelDeletedEvent.TYPE, handler));

            /*
             * Subscribe to channel.
             */
            try {
                channel.open(Channel.Flow.BOTH, "*");

            } catch (MirrorCacheException e) {
                throw new EMP_Exception(EMP_Exception.ErrorDetail.OTHER, e.getReason().getMsg() + "\n" + e.getDetails(), e);
            }

            /*
             * Hook into Overlay events.
             */
            this.overlayEventRegistration = overlay.addContainerEventListener(new IContainerEventListener() {
                @Override
                public void onEvent(ContainerEvent event) {

                    /*
                     * When a feature is added we publish to the channel.
                     */
                    if (event.getEvent() == ContainerEventEnum.OBJECT_ADDED) {
                        Log.d(TAG, "Overlay[" + overlay.getName() + "].onEvent():OBJECT_ADDED");

                        for (IGeoBase child : event.getAffectedChildren()) {
                            final String geoId = child.getGeoId().toString();

                            if (ignoredPublishedGeoIds.contains(geoId)) {
                                Log.d(TAG, "ignoring event for geoId: " + geoId);
                                ignoredPublishedGeoIds.remove(geoId);
                                break;

                            } else {
                                try {
                                    if (child instanceof IGeoMilSymbol) {
                                        final IGeoMilSymbol geoMilSymbol = (IGeoMilSymbol) child;

                                        getChannel().publish(geoMilSymbol.getGeoId().toString(), IGeoMilSymbol.class, geoMilSymbol);
                                    }

                                } catch (MirrorCacheException e) {
                                    Log.e(TAG, e.getReason().getMsg() + "\n" + e.getDetails(), e);
                                }
                            }
                        }

                    /*
                     * When a feature is removed we delete from the channel.
                     */
                    } else if (event.getEvent() == ContainerEventEnum.OBJECT_REMOVED) {
                        Log.d(TAG, "Overlay[" + overlay.getName() + "].onEvent():OBJECT_REMOVED");

                        for (IGeoBase child : event.getAffectedChildren()) {
                            final String geoId = child.getGeoId().toString();

                            if (ignoredDeletedGeoIds.contains(geoId)) {
                                Log.d(TAG, "ignoring event for geoId: " + geoId);
                                ignoredDeletedGeoIds.remove(geoId);
                                break;

                            } else {
                                try {
                                    getChannel().delete(geoId);

                                } catch (MirrorCacheException e) {
                                    Log.e(TAG, e.getReason().getMsg() + "\n" + e.getDetails(), e);
                                }
                            }
                        }
                    }
                }
            });
        }

        public void unsubscribe() throws EMP_Exception {
            getOverlay().removeEventListener(overlayEventRegistration);

            try {
                getChannel().close();
                for (EventRegistration channelEventRegistration : channelEventRegistrations) {
                    channelEventRegistration.removeHandler();
                }

            } catch (MirrorCacheException e) {
                throw new EMP_Exception(EMP_Exception.ErrorDetail.OTHER, e.getReason().getMsg() + "\n" + e.getDetails(), e);
            }
        }

        // -+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+- //
        // -+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+- //

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Product product = (Product) o;

            return id.equals(product.id);

        }
        @Override
        public int hashCode() {
            return id.hashCode();
        }

        @Override
        public String toString() {
            return "Product{" +
                    "id='" + id + '\'' +
                    '}';
        }
    }
}
