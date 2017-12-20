package mil.emp3.core.utils.milstd2525.icons;

import android.graphics.Typeface;

import org.cmapi.primitives.IGeoMilSymbol;

import armyc2.c2sd.renderer.utilities.RendererSettings;
import mil.emp3.api.utils.MilStdUtilities;

/**
 * This will create the BitmapCache that you want to deploy.
 *
 * Currently three types of Cache implementations are supported
 *
 * BitmapCache - Caches WeakReferences to Bitmaps and releases and reclaims nodes as Bitmaps are garbage collected. This mechanism did not work well
 *     in the tests we performed, hence we started investigating further and developed AdaptiveBitmapCache
 *
 * AdaptiveBitmapCache - This reduces Mid Distance Threshold as we reach the preset memory usage limit and increases the MDT as memory usage returns to
 *     normal. This return to normal logic isn't working well because of the way World Wind is handling the Bitmaps. This lead to the NoBitmapCache
 *
 * NoBitmapCache doesn't cache anything, it depends on underlying map engine to manage the render cache. Cache was created so that we can continue
 *     to use the methods that build an ImageInfo when it is required.
 *
 * Here is a brief explanation of why Bitmap caching by EMP will NOT work, it also answers the question about multiple map instances:
 *
 * Java objects are shareable between map instances, assuming those instances share a common Java VM. This includes Placemark, PlacemarkAttributes,
 * and ImageSource. Note that when an app uses ImageSource.BitmapFactory the bitmap instances are quickly converted to OpenGL textures and discarded,
 * so sharing bitmap instances becomes irrelevant.
 * OpenGL objects are currently not shareable between map instances. Each map instance has its own OpenGL context and its own render resource cache
 * on the native heap. Each map instance therefore has its own OpenGL texture for a placemark image, regardless of whether an app uses
 * ImageSource.fromBitmap or ImageSource.fromBitmapFactory. Note that it's possible to configure an OpenGL context to share texture objects,
 * but we have yet to implement this as it's not supported out of the box on Android.
 *
 */
public class BitmapCacheFactory {
    private static String TAG = BitmapCacheFactory.class.getSimpleName();
    private static BitmapCacheFactory factoryInstance;

    private static boolean useAdaptiveBitMapCache = true;
    private static boolean useNoBitmapCache = false;
    private IBitmapCache bitmapCache = null;

    public static BitmapCacheFactory instance() {
        if(null == BitmapCacheFactory.factoryInstance) {
            synchronized (BitmapCacheFactory.class) {
                if(null == factoryInstance) {
                    BitmapCacheFactory.factoryInstance = new BitmapCacheFactory();
                }
            }
        }
        return BitmapCacheFactory.factoryInstance;
    }

    private BitmapCacheFactory() {
        RendererSettings rs = RendererSettings.getInstance();
        rs.setSymbologyStandard(MilStdUtilities.geoMilStdVersionToRendererVersion(IGeoMilSymbol.SymbolStandard.MIL_STD_2525B));

        //rs.setDefaultPixelSize(75);

        // Depending on screen size and DPI you may want to change the font size.
        rs.setModifierFont("Arial", Typeface.BOLD, 20);
        rs.setMPModifierFont("Arial", Typeface.BOLD, 20);

        // Configure modifier text output
        rs.setTextBackgroundMethod(RendererSettings.TextBackgroundMethod_OUTLINE);
        rs.setTextOutlineWidth(4);  // 4 is the default
    }

    /**
     * This is used by the MilStdRenderer to initialize the cache.
     * @param sCacheDir
     * @return
     */
    public IBitmapCache getBitMapCache(String sCacheDir, int getMemoryClass) {
        if(null != bitmapCache) {
            return bitmapCache;
        }

        synchronized(BitmapCacheFactory.factoryInstance) {
            if(null == bitmapCache) {
                if (useAdaptiveBitMapCache) {
                    bitmapCache = AdaptiveBitmapCache.getInstance(sCacheDir, getMemoryClass);
                } else if(useNoBitmapCache) {
                    bitmapCache = NoBitmapCache.getInstance(sCacheDir);
                } else {
                    bitmapCache = BitmapCache.getInstance(sCacheDir);
                }
            }
        }
        return bitmapCache;

    }

    /**
     * This is used by objects that need an already initialized Cache but don't have knowledge of sCacheDir.
     * @return
     */
    public IBitmapCache getBitMapCache() {
        if(null == bitmapCache) {
            throw new IllegalStateException("bitmapCache wasn't initialized");
        }
        return bitmapCache;
    }
}
