package mil.emp3.mirrorcache.api;

import mil.emp3.mirrorcache.api.MirrorCacheParcelable;
import mil.emp3.mirrorcache.api.IMirrorCacheListener;

interface IMirrorCache {

    void register(in IBinder clientDeathListener, String packageName, int pid, IMirrorCacheListener listener);
    void unregister(int pid);

    void mirror(int pid, IMirrorCacheListener listener);

    void nuke();

    void update(int pid, in MirrorCacheParcelable o, String parentKey);
    void delete(int pid, String key);

    void map(int pid, String parentKey, String childKey);
    void unmap(int pid, String parentKey, String childKey);
}
