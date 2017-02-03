package mil.emp3.mirrorcache.api;

public interface IMirrorableListener {
    /**
     * WARNING: do not do anything time consuming in this callback.
     * MirrorCache is synchronous.
     * Any delay will perturb other applications using this cache.
     *
     * @param o object that has just changed in the mirrorcache
     */
    void onUpdate(IMirrorable o);

    /**
     *
     * @param o object that is being deleted from mirrorcache
     */
    void onDelete(IMirrorable o);
}
