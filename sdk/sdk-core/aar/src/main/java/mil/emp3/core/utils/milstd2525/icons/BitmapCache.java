package mil.emp3.core.utils.milstd2525.icons;

import android.util.Log;
import android.util.SparseArray;


import java.lang.ref.WeakReference;

import mil.emp3.api.interfaces.IMap;
import mil.emp3.core.utils.milstd2525.EmpImageInfo;
import mil.emp3.mapengine.interfaces.IEmpImageInfo;

/**
 * This class implements the Bitmap cache for the MilStd single point icons. It allows features to share
 * bitmaps therefore saving memory. It stores the bitmaps with a weakReference so when there are no
 * "hard references" referring to a bitmap it can be garbage collected by the system.
 */
public class BitmapCache extends CoreBitmapCache {
    private static final String TAG = BitmapCache.class.getSimpleName();
    private static BitmapCache instance = null;
    private java.util.concurrent.ConcurrentHashMap<String, WeakReference<EmpImageInfo>> oBitmapCache = new java.util.concurrent.ConcurrentHashMap<>();

    private BitmapCache(String sCacheDir) {
        super(TAG, sCacheDir);
    }

    static synchronized IBitmapCache getInstance(String sCacheDir) {

        if (BitmapCache.instance == null) {
            BitmapCache.instance = new BitmapCache(sCacheDir);
        }

        return BitmapCache.instance;
    }

    @Override
    public void clearCache() {
        this.oBitmapCache.clear();
    }

    private void cleanupBitmapCache() {
        int cacheSize = this.oBitmapCache.size();

        if (cacheSize > 100) {
            String[] keyArray = new String[cacheSize];
            this.oBitmapCache.keySet().toArray(keyArray);

            for (String symbolCode : keyArray) {
                WeakReference<EmpImageInfo> oReference = this.oBitmapCache.get(symbolCode);
                IEmpImageInfo oImageInfo = (oReference == null) ? null : oReference.get();

                if ((oImageInfo != null) && (oImageInfo.getImage() == null)) {
                    // The entry is there but the bitmap has been removed.
                    this.oBitmapCache.remove(symbolCode);
                } else if (oReference == null) {
                    // The reference has been removed.
                    this.oBitmapCache.remove(symbolCode);
                }
            }
        }
    }

    public IEmpImageInfo getImageInfo(String sSymbolCode, SparseArray oModifiers, SparseArray oAttr) {
        String sKey = this.makeKey(sSymbolCode, oModifiers, oAttr);

        //Log.d(TAG, "Key: " + sKey);
        WeakReference<EmpImageInfo> oReference = this.oBitmapCache.get(sKey);
        IEmpImageInfo oImageInfo = (oReference == null)? null: oReference.get();

        if (oImageInfo == null) {
            oImageInfo = this.createImageInfo(sSymbolCode, oModifiers, oAttr);
            Log.e(TAG, "cache create getImageInfo key/size " + oBitmapCache.size() + " " + oImageInfo.getImageKey() + " " + oImageInfo.getImage().getAllocationByteCount());
        } else if (oImageInfo.getImage() == null) {
            this.oBitmapCache.remove(oImageInfo);
            oImageInfo = this.createImageInfo(sSymbolCode, oModifiers, oAttr);
            Log.e(TAG, "cache weak create getImageInfo key/size " + oBitmapCache.size() + " " + oImageInfo.getImageKey() + " " + oImageInfo.getImage().getAllocationByteCount());
        }

        return oImageInfo;
    }

    protected void put(String sKey, EmpImageInfo oEmpImageInfo) {
        this.oBitmapCache.put(sKey, new WeakReference<EmpImageInfo>(oEmpImageInfo));
    }

    protected int cacheSize() {
        return this.oBitmapCache.size();
    }

    @Override
    public boolean setMidDistanceThreshold(IMap clientMap, double midDistanceThreshold) {
        Log.w(TAG, "setMidDistanceThreshold is not supported by this cache");
        return true; // This will allow applying to MapInstance.
    }

    @Override
    public boolean getAlgorithmStatus() {
        Log.w(TAG, "getAlgorithmStatus is not supported by this cache");
        return false;
    }

    @Override
    public void setAlgorithmStatus(boolean enable) {
        Log.w(TAG, "setAlgorithmStatus is not supported by this cache");
    }

    @Override
    public void setTotalAvailableMemory(int availableMB) {
        Log.w(TAG, "setTotalAvailableMemory is not supported by this cache");
    }
}
