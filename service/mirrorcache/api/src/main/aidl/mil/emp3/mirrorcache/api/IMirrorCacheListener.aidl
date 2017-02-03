package mil.emp3.mirrorcache.api;

import mil.emp3.mirrorcache.api.MirrorCacheParcelable;

interface IMirrorCacheListener {

    void onUpdate(int pid, in MirrorCacheParcelable o, long parentKey, in String guid);
    void onDelete(int pid, long key);
    void onNoop();
    void onMap(long parentKey, long childKey);
    void onUnmap(long parentKey, long childKey);

    String getPackageName();
}
