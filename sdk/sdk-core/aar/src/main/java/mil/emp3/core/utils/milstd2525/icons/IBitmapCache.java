package mil.emp3.core.utils.milstd2525.icons;

import android.util.SparseArray;

import mil.emp3.api.interfaces.IMap;
import mil.emp3.mapengine.interfaces.IEmpImageInfo;

/**
 * Created by deepakkarmarkar on 7/26/2016.
 */
public interface IBitmapCache {

    /**
     * Clears bitmap cache.
     */
    void clearCache();

    /**
     * Fetched the Bitmap that represents the icon that will be drawn on the map. Searched the cache before building
     * a brand new Bitmap.
     * @param sSymbolCode
     * @param oModifiers
     * @param oAttr
     * @return
     */

    IEmpImageInfo getImageInfo(String sSymbolCode, SparseArray oModifiers, SparseArray oAttr);

    /**
     * Stores the supplied midDistanceThreshold.
     * Returns true if new setting can be applied to MapInstance without affecting the BitmapCache adversely
     * @param clientMap
     * @param midDistanceThreshold
     * @return
     */
    boolean setMidDistanceThreshold(IMap clientMap, double midDistanceThreshold);

    /**
     * Returns true if algorithm that adjusts Mid Distance Threshold is enabled else returns false
     * @return
     */
    boolean getAlgorithmStatus();

    /**
     * Turns on/off algorithm to adjust Mid Distance Threshold.
     * @param enable
     * @return
     */
    void setAlgorithmStatus(boolean enable);

    /**
     * This is the value returned from getClassMemory or getLargeClassMemory
     * @param availableMB
     */
    void setTotalAvailableMemory(int availableMB);
}
