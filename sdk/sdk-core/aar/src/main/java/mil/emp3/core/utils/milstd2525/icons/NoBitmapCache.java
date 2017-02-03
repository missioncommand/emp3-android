package mil.emp3.core.utils.milstd2525.icons;

import android.util.Log;
import android.util.SparseArray;

import mil.emp3.api.interfaces.IMap;
import mil.emp3.core.utils.milstd2525.EmpImageInfo;
import mil.emp3.mapengine.interfaces.IEmpImageInfo;

/**
 * NoBitmapCache class is used when we wan to use the NASA Render Cache (which will be used by the engine anyway) and avoid
 * caching any Bitmaps in EMP. code in the map engine (worldwind-aar) will invoke the getImageInfo method when it needs to
 * create a new Bitmap as it doesn't exist in the Render Cache.
 *
 * All other methods here are basically NO-OP.
 */
public class NoBitmapCache extends CoreBitmapCache {

    private static final String TAG = NoBitmapCache.class.getSimpleName();
    private static NoBitmapCache instance = null;
    private NoBitmapCache(String sCacheDir) {
        super(TAG, sCacheDir);
    }

    static synchronized IBitmapCache getInstance(String sCacheDir) {

        if (NoBitmapCache.instance == null) {
            NoBitmapCache.instance = new NoBitmapCache(sCacheDir);
        }

        return NoBitmapCache.instance;
    }

    @Override
    public void clearCache() {

    }

    @Override
    public IEmpImageInfo getImageInfo(String sSymbolCode, SparseArray oModifiers, SparseArray oAttr) {
        IEmpImageInfo oImageInfo = this.createImageInfo(sSymbolCode, oModifiers, oAttr);
        return oImageInfo;
    }

    protected void put(String sKey, EmpImageInfo oEmpImageInfo) {
        // There is no cache so nothing to put anywhere
    }

    protected int cacheSize() {
        return 0;
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
