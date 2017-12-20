package mil.emp3.core.utils.milstd2525.icons;

import android.util.Log;
import android.util.SparseArray;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

import mil.emp3.api.interfaces.IMap;
import mil.emp3.api.interfaces.core.IStorageManager;
import mil.emp3.api.utils.ManagerFactory;
import mil.emp3.core.utils.milstd2525.EmpImageInfo;
import mil.emp3.mapengine.interfaces.IEmpImageInfo;

/**
 * Objective of this class is to control (best possible strategy) how much memory is used by the Bitmaps that represent the features placed
 * on the map. Considering how Android OS controls heap allocation and how applications using EMP manage their memory usage this strategy
 * may or may not work. This Cache was created for JIRA ticket EMP-2350 where guidance was to stabilize (not crash) the EMP V3 - Dev Test
 * application under following conditions:
 *     - Label Settings are set to ALL
 *     - WMS is added
 *     - Run the Performance test with 10,000 features FDT = 120000 and MDT = 100000
 *     - Zoom in and out to cras these thresholds and much more.
 *
 * Please review the code in MilStd2525LevelOfDetailSelector in mapengine-worldwind-aar project. It will be clear that this strategy works only
 * if underlying map engine supports the FDT/MDT capability or we are able to implement it outside the map engine.
 *
 * We have created the Bitmap Cache with the intent of sharing Bitmaps across multiple icons when possible. We maintain a Weak Reference to these Bitmaps
 * and depend on the underlying map engine to release the Bitmaps when they are no longer required. We of course are at the mercy of GC.
 *
 * General algorithm goes as follows:
 *
 *    - We try to control the amount of memory used for Bitmaps
 *    - When memory usage exceeds the threshold we reduce the MDT with hope that less icons will now need huge Bitmaps
 *    - MDT is either adjusted by a background thread or in-line when a new entry is about to be added to the Cache.
 *    - Note that actual usage will go above the limit we want to place as we don't know ahead of time how many icons with
 *        what amount of detail will get rendered.
 *
 * CAUTION: Application can add huge numbers of features and still run into a OutOfMemoryError. There needs to be some high level
 * guidance on what are the requirements on smart client platform and what is technically feasible. Since we control the OS we can
 * control the amount of heap will be allocated to application on Android.
 *
 * Don't forget that we are adding some computational overhead for this defensive strategy. We will have to tune that if it has
 * a significant impact on the performance.
 */
public class AdaptiveBitmapCache extends CoreBitmapCache {
    private static final String TAG = AdaptiveBitmapCache.class.getSimpleName();
    private static AdaptiveBitmapCache instance = null;

    // This is the class that manages the MDT based on current state of the EMP like cache usage, current MDT and events leading
    //   to the current state.
    private DistanceManager distanceManager = new DistanceManager();

    // This is ideally the ceiling on the Cache Size. We should be using the return value from getMemoryClass and then setting the ceiling
    // as a percentage of that value. Following code in MapViewFragmentBase will have to inject this valuie.
    //     ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
    //     Log.e(TAG, "Get Memory Class " + am.getMemoryClass());
    // TODO set threshold based on getMemoryClass value
    private double MaxBitmapCacheSize = 10 * 1024 * 1024;
    private final double minimumMaxBitmapCacheSize = 10 * 1024 * 1024; // We will not set MaxBitmapCacheSize to a lesser value

    // This cache contains Bitmaps that contain either medium or high amount of details. I have seen Bitmap sizes of 60K for high
    // level of detail and it will probably be more in future. It is this cache that is used to manage MDT.
    private java.util.concurrent.ConcurrentHashMap<String, IEmpImageInfo> oBitmapCache = new java.util.concurrent.ConcurrentHashMap<>();

    // These are the icons that are beyond the FDT and each have 400 bytes of Bitmap, Algorithm doesn't look at this cache.
    private java.util.concurrent.ConcurrentHashMap<String, IEmpImageInfo> oBitmapCacheFarImage = new java.util.concurrent.ConcurrentHashMap<>();

    // Control access to the Caches even though they use Concurrent Hash Maps as there are multiple places where we are doing insert and
    // remove operations and calculating cache size. If this is deemed unnecessary in future we can remove it.
    private ReentrantLock lock = new ReentrantLock();

    // Size of the Cache is calculated using the size of the BitMap held in the EmpImageInfo class. We are ignoring the other metadat in that
    // class as it is insignificant compared to the Bitmap size and we don't need an exact size in this operation.
    private int sizeBitmapCache = 0;
    private int sizeBitmapCacheFarImage = 0;

    // TODO test with multiple map instances.
    Map<IMap, Double> appDesiredMidDistanceThreshold = null; // This is the value set by application
    Map<IMap, Double> actualMidDistanceThreshold = null;     // This is the value set by this class

    IStorageManager storageManager = ManagerFactory.getInstance().getStorageManager();

    // This gives the ability to turn on/off the algorithm to adjust Mid Distance Algorithm
    private boolean enabled = true;
    /**
     * This class takes care of managing the MDT based on the current state of the oBitmapCache. It currently supports one MapInstance
     * and it will shortly be updated to support multiple map instances.
     */
    class DistanceManager implements Runnable {
        final double memoryThreshold = .75;                  // This kicks of adjustments to the MID Distance Threshold
        private double lastPercentWhenDecreasedMid = 0.0;    // percentage of used cache when last time MDT was reduced. can be more than 1.0
        private double lastPercentWhenIncreasedMid = 1.0;    // percentage of used cache when last time MDT was adjusted. can be more than 1.0
        private boolean initialized = false;
        private int consecutiveSameSizeBitmapCache = 0;      // consecutive cycles of the thread where size of the cache was same
        private int lastSizeBitmapCache = 0;                 // size of oBitmapCache last iteration.
        private double defaultMidDistanceTh = 10000.0;

        private void initialize() {

            // application set and this class set values are same to begin with.
            appDesiredMidDistanceThreshold = storageManager.getMidDistanceThreshold();
            actualMidDistanceThreshold = storageManager.getMidDistanceThreshold();
            initialized = true;
        }

        /**
         * Looks at the current state of the cache and updates the MDT if necessary. This is invoked any time an entry is put in
         * oBitmapCache. Our major goal will be to avoid constant MDT adjustments that just keep shuffling adding and removing
         * details from the screen. That will be annoying to the user.
         * @param size
         */
        void processCapacity(double size) {
            if(!initialized) {
                initialize();
            }

            if(!enabled) {
                return; // We will not try to restore any updated Mid Distance Threshold as this enable/disable should really
                        // be done by the app at first opportunity, on getting IMAP Ready.
            }

            double percentFull = size / MaxBitmapCacheSize;

            // We will take action i.e. reduce MDT only if percentage has changed my more than 10%
            if((percentFull - lastPercentWhenDecreasedMid) > .1) {
                lastPercentWhenDecreasedMid = percentFull;
                lastPercentWhenIncreasedMid = 1.0;
                for(Map.Entry<IMap, Double> entry : actualMidDistanceThreshold.entrySet()) {
                    if (Double.NaN != entry.getValue()) {
                        // Set the MDT to a 90% of current value
                        storageManager.setMidDistanceThresholdActual(entry.getKey(), entry.getValue() * .9);
                        entry.setValue(entry.getValue() * .9);
                        Log.w(TAG, "processCapacity setMid " + entry.getValue());
                    } else {
                        // Set the MDT to some default number
                        Log.e(TAG, "ERROR currentMidDistanceThreshold is NaN");
                        storageManager.setMidDistanceThresholdActual(entry.getKey(), defaultMidDistanceTh);
                        entry.setValue(10000.0);
                        Log.e(TAG, "processCapacity setMid default " + 10000);
                    }
                }
            } else if(percentFull < memoryThreshold) {
                lastPercentWhenDecreasedMid = 0.0; // We should never reach here as processCapacity wouldn't be invoked
            }
        }

        /**
         * This thread currently wakes up every five seconds, checks the state of oBitmapCache and increases MDT if possible
         * allowing user to see higher level of detail (maybe).
         */
        @Override
        public void run() {
            while(!Thread.interrupted()) {
                try {
                    Thread.sleep(5000);
                    if(!initialized) {
                        continue;
                    }

                    if(!enabled) {
                        continue; // We will not try to restore any updated Mid Distance Threshold as this enable/disable should really
                        // be done by the app at first opportunity, on getting IMAP Ready.
                    }

                    // Check if any maps needs adjusting
                    // if current MDT is greater than original MDT then we are not going to touch it as user is able to see additional details.

                    boolean aMapMayNeedUpdate = false;
                    for(Map.Entry<IMap, Double> entry: actualMidDistanceThreshold.entrySet()) {
                        if(entry.getValue() > appDesiredMidDistanceThreshold.get(entry.getKey())) {
                            continue;
                        } else {
                            aMapMayNeedUpdate = true;
                            break;
                        }
                    }

                    if(!aMapMayNeedUpdate) {
                        lastPercentWhenIncreasedMid = 1.0;
                        continue;
                    }  else {
                        // Since we may want to increase the MDT, recalculate the size of the oBitmapCache, eliminating any Weak References
                        // that have been garbage collected.
//                        Log.v(TAG, "sizeBitmapCache " + sizeBitmapCache);
                        if(sizeBitmapCache > (MaxBitmapCacheSize * distanceManager.memoryThreshold)) {
                            cleanBitmapCache();
                        }
                    }

                    // Now we have the most current and clean cache size, if size is less than the threshold then try to increase MDT.
                    if(sizeBitmapCache < (MaxBitmapCacheSize * distanceManager.memoryThreshold)) {
                        lastPercentWhenDecreasedMid = 0.0;            // avoids double processing in the next if block.
                        double percentFull = sizeBitmapCache / MaxBitmapCacheSize;

                        // If the difference in current percentage and last time when size was increased is more than 10% then increase MDT.
                        if((lastPercentWhenIncreasedMid - percentFull) > .1) {
                            consecutiveSameSizeBitmapCache = 0;
                            lastPercentWhenIncreasedMid = percentFull;
                            lastPercentWhenDecreasedMid = 0.0;
                            for(Map.Entry<IMap, Double> entry: actualMidDistanceThreshold.entrySet()) {
                                if(entry.getValue() < appDesiredMidDistanceThreshold.get(entry.getKey())) {
                                    storageManager.setMidDistanceThresholdActual(entry.getKey(), entry.getValue() * 1.10);
                                    entry.setValue(entry.getValue() * 1.10);
                                    Log.w(TAG, "run setMid " + entry.getValue());
                                }
                            }
                        }
                    }
                    if(lastSizeBitmapCache == sizeBitmapCache) {
                        consecutiveSameSizeBitmapCache++;

                        // If for 10 iterations BitmapCache size hasn't changed and it is less than threshold then may be  we can increase MDT
                        //   and possibly allow more detail.
                        if((consecutiveSameSizeBitmapCache > 9) && (sizeBitmapCache < (MaxBitmapCacheSize * distanceManager.memoryThreshold))) {
                            consecutiveSameSizeBitmapCache = 0;
                            lastPercentWhenIncreasedMid = sizeBitmapCache / MaxBitmapCacheSize;
                            lastPercentWhenDecreasedMid = 0.0;
                            for(Map.Entry<IMap, Double> entry: actualMidDistanceThreshold.entrySet()) {
                                if(entry.getValue() < appDesiredMidDistanceThreshold.get(entry.getKey())) {
                                    storageManager.setMidDistanceThresholdActual(entry.getKey(), entry.getValue() * 1.10);
                                    entry.setValue(entry.getValue() * 1.10);
                                    Log.w(TAG, "run setMid consecutiveSameSizeBitmapCache " + entry.getValue());
                                }
                            }
                        } else if(consecutiveSameSizeBitmapCache > 9) {
                            consecutiveSameSizeBitmapCache = 0;
                            lastPercentWhenIncreasedMid = sizeBitmapCache / MaxBitmapCacheSize;
                            lastPercentWhenDecreasedMid = 0.0;
                            for(Map.Entry<IMap, Double> entry: actualMidDistanceThreshold.entrySet()) {
                                if(entry.getValue() < appDesiredMidDistanceThreshold.get(entry.getKey())) {
                                    storageManager.setMidDistanceThresholdActual(entry.getKey(), entry.getValue() * 1.01);
                                    entry.setValue(entry.getValue() * 1.01);
                                    Log.w(TAG, "run setMid consecutiveSameSizeBitmapCache " + entry.getValue());
                                }
                            }
                        }
                    } else {
                        lastSizeBitmapCache = sizeBitmapCache;
                        consecutiveSameSizeBitmapCache = 0;
                    }
                } catch (InterruptedException e) {
                    Log.e(TAG, "Existing thread DistanceManager ", e);
                    break;
                }
            }
        }
    }

    private AdaptiveBitmapCache(String sCacheDir) {
        super(TAG, sCacheDir);
        Thread distanceManagerThread = new Thread(distanceManager);
        distanceManagerThread.start();
    }

    static synchronized IBitmapCache getInstance(String sCacheDir, int getMemoryClass) {

        if (AdaptiveBitmapCache.instance == null) {
            AdaptiveBitmapCache.instance = new AdaptiveBitmapCache(sCacheDir);
            instance.setTotalAvailableMemory(getMemoryClass);
        }

        return instance;
    }

    @Override
    public void clearCache() {
        oBitmapCache.clear();
        oBitmapCacheFarImage.clear();
    }

    /**
     * Either the EmpImageInfo from cache or create one.
     * @param sSymbolCode
     * @param oModifiers
     * @param oAttr
     * @return
     */
    public IEmpImageInfo getImageInfo(String sSymbolCode, SparseArray oModifiers, SparseArray oAttr) {
        String sKey = makeKey(sSymbolCode, oModifiers, oAttr);

        // Remember that Bitmap is a WeakReference in EmpImgaeInfo and could be null as was garbage collected
        // If Weak Reference is detected then it should be removed from the cache.
        IEmpImageInfo empImageInfo = oBitmapCache.get(sKey);
        if ((null != empImageInfo) && (null == empImageInfo.getImage())) {
            removeFromBitmapCache(sKey);
            empImageInfo = null;
//            Log.i(TAG, "WeakReference in oBitmapCache");
        }

        // Remember that Bitmap is a WeakReference in EmpImgaeInfo and could be null as was garbage collected
        // If Weak Reference is detected then it should be removed from the cache.
        if (null == empImageInfo) {
            empImageInfo = oBitmapCacheFarImage.get(sKey);
            if ((null != empImageInfo) && (null == empImageInfo.getImage())) {
                removeFromBitmapCacheFarImage(sKey);
                empImageInfo = null;
//                Log.i(TAG, "WeakReference in oBitmapCacheFarImage");
            }
        }

        if (empImageInfo == null) {
            empImageInfo = createImageInfo(sSymbolCode, oModifiers, oAttr);
            if (!empImageInfo.isFarImageIcon()) {
//                Log.d(TAG, "cache create getImageInfo oBitmapCache key/size " + sizeBitmapCache + " " + empImageInfo.getImageKey() + " " +
//                        empImageInfo.getImageSize());
            }
            else {
//                Log.d(TAG, "cache create getImageInfo oBitmapCacheFarImage key/size " + sizeBitmapCacheFarImage + " " + empImageInfo.getImageKey() + " " +
//                        empImageInfo.getImageSize());
            }
        }

        return empImageInfo;
    }

    /**
     * oEmpImage has a flag that indicates if this is a Far Image. Use it to put the object in correct cache. If inserting in BitmapCache
     * run the processCapacity algorithm based on current size of cache. Note that we call cleanBitmapCache to remove any Weak References,
     * we could do that in processCapacity itself.
     * @param sKey
     * @param oEmpImageInfo
     */
    protected void put(String sKey, EmpImageInfo oEmpImageInfo) {
        if(!oEmpImageInfo.isFarImageIcon()) {
            if(sizeBitmapCache >= (MaxBitmapCacheSize * distanceManager.memoryThreshold)) {
                cleanBitmapCache();
                if(sizeBitmapCache >= (MaxBitmapCacheSize * distanceManager.memoryThreshold)) {
                    distanceManager.processCapacity(sizeBitmapCache);
                }
            }
            try {
                lock.lock();
                oBitmapCache.put(sKey, oEmpImageInfo);
                sizeBitmapCache += oEmpImageInfo.getImageSize();
            } finally {
                lock.unlock();
            }
        } else {
            try {
                lock.lock();
                oBitmapCacheFarImage.put(sKey, oEmpImageInfo);
                sizeBitmapCacheFarImage += oEmpImageInfo.getImageSize();
            } finally {
                lock.unlock();
            }
        }
    }

    @Override
    public boolean setMidDistanceThreshold(IMap clientMap, double midDistanceThreshold) {
        if(null == appDesiredMidDistanceThreshold) {
            appDesiredMidDistanceThreshold = new HashMap<>();
        }
        appDesiredMidDistanceThreshold.put(clientMap, midDistanceThreshold);

        if(null == actualMidDistanceThreshold) {
            actualMidDistanceThreshold = new HashMap<>();
            actualMidDistanceThreshold.put(clientMap, midDistanceThreshold);
        } else {
            if((actualMidDistanceThreshold.containsKey(clientMap)) && (midDistanceThreshold > actualMidDistanceThreshold.get(clientMap))) {
                cleanBitmapCache();
                if(sizeBitmapCache < (MaxBitmapCacheSize * distanceManager.memoryThreshold)) {
                    actualMidDistanceThreshold.put(clientMap, midDistanceThreshold);
                } else {
//                    Log.i(TAG, "Not using user specified Mid Threshold Distance " + midDistanceThreshold +
//                        " Actual " + actualMidDistanceThreshold.get(clientMap));
                    return false;
                }
            } else {
                actualMidDistanceThreshold.put(clientMap, midDistanceThreshold);
            }
        }
        return true;
    }

    @Override
    public boolean getAlgorithmStatus() {
        return enabled;
    }

    @Override
    public void setAlgorithmStatus(boolean enable) {
        this.enabled = enable;
        Log.i(TAG, "algorithm enable status is " + enable);
    }

    @Override
    public void setTotalAvailableMemory(int availableMB) {
        if((availableMB * 1024 * 1024 * .1) > minimumMaxBitmapCacheSize) {
            MaxBitmapCacheSize = availableMB * 1024 * 1024 * .1;
            Log.i(TAG, "Setting MaxBitmapCacheSize = " + MaxBitmapCacheSize);
        } else {
            Log.i(TAG, "Ignoring availableMB " + availableMB);
        }
    }

    /**
     * gets current total cache size, note this is just the Bitmap sizes.
     * @return
     */
    protected int cacheSize() {
        return (sizeBitmapCache + sizeBitmapCacheFarImage);
    }

    /**
     * Remove and update size.
     * @param sKey
     */
    private void removeFromBitmapCache(String sKey) {
        try {
            lock.lock();
            IEmpImageInfo eii = oBitmapCache.remove(sKey);
            if (null != eii) {
                sizeBitmapCache -= eii.getImageSize();
            }
        } finally {
            lock.unlock();
        }
    }

    /**
     * Remove and update size
     * @param sKey
     */
    private void removeFromBitmapCacheFarImage(String sKey) {
        try {
            lock.lock();
            IEmpImageInfo eii = oBitmapCacheFarImage.remove(sKey);
            if (null != eii) {
                sizeBitmapCacheFarImage -= eii.getImageSize();
            }
        } finally {
            lock.unlock();
        }
    }

    /**
     * Remove all entries with Weak References and get the correct size of the cache.
     */
    private void cleanBitmapCache() {
        try {
            lock.lock();
            sizeBitmapCache = 0;
            Iterator<Map.Entry<String, IEmpImageInfo>> iter = oBitmapCache.entrySet().iterator();
            while (iter.hasNext()) {
                Map.Entry<String, IEmpImageInfo> entry = iter.next();
                if (entry.getValue().getImage() == null) {
//                    Log.i(TAG, "Weak Reference " + entry.getKey() + " " + entry.getValue().getImageSize());
                    iter.remove();
                } else {
                    sizeBitmapCache += entry.getValue().getImageSize();
                }
            }
        } finally {
            lock.unlock();
        }
    }
}
