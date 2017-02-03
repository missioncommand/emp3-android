package mil.emp3.mirrorcache.api;

import mil.emp3.mirrorcache.api.MirrorCacheParcelable;

interface IMirrorCacheListener {

    void onUpdate(int pid, in MirrorCacheParcelable o, String parentKey);
    void onDelete(int pid, String key);

    void onMap(String parentKey, String childKey);
    void onUnmap(String parentKey, String childKey);

    String getPackageName();
}
