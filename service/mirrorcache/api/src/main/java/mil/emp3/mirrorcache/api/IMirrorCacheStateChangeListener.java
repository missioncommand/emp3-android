package mil.emp3.mirrorcache.api;

/**
 * Useful for reacting to mirrorcache state changes.
 */
public interface IMirrorCacheStateChangeListener  {
    /**
     * This listener is called every time the underlying MirrorCache connection establishment state machine changes
     * states and is completely up and running in a fully mirrorred mode. The Developer must assume that this is
     * called as a result of an exception and MUST re-plumb all the data references and all associated callbacks.
     */
    void onMirrorred();

    /**
     * Generic update notification.
     * This can be used to generically be notified any element changes, or
     * as a is dirty mechanism for use with maps
     * @param o Newly updated object
     */
    void onUpdate(IMirrorable o);

    /**
     * Invoked when an object is being removed from the mirrorcache.
     * @param o the object being removed
     */
    void onDelete(IMirrorable o);
}
