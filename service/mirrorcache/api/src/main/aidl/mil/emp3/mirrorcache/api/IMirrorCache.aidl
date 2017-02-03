package mil.emp3.mirrorcache.api;

import mil.emp3.mirrorcache.api.MirrorCacheParcelable;
import mil.emp3.mirrorcache.api.IMirrorCacheListener;

interface IMirrorCache {

    void register(in IBinder clientDeathListener, String packageName, int pid, IMirrorCacheListener listener);
    void unregister(int pid);

    void mirror(int pid, IMirrorCacheListener listener);
    long genKey();

    void nuke();

    void update(int pid, in MirrorCacheParcelable o, long parentKey, in String guid);
    void delete(int pid, long key);
    void noop(int pid);
    void map(int pid, long parentKey, long childKey);
    void unmap(int pid, long parentKey, long childKey);
}
