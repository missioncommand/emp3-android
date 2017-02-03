package mil.emp3.mapengine.interfaces;

import android.util.SparseArray;

import org.cmapi.primitives.IGeoBounds;

import mil.emp3.api.MilStdSymbol;
import mil.emp3.api.interfaces.IFeature;
import mil.emp3.api.interfaces.core.IStorageManager;

/**
 *
 * This interface defines access to the core MilStd rendering capability required by the map instances.
 */
public interface IMilStdRenderer {
    void setStorageManager(IStorageManager storageManager);

    /**
     * This method sets the Cache Directory used by renderer and also sets the available heap memory for the application
     * @param sCacheDir
     * @param getMemoryClass
     */
    void setRendererCacheDir(String sCacheDir, int getMemoryClass);

    /**
     * This method is called by the map instances to get a list of renderable that compose the
     * MilStd tactical graphic symbol.
     * @param mapInstance
     * @param symbol
     * @param selected
     * @return A list of basic IFeature objects. See {@link IFeature).
     */
    java.util.List<IFeature> getTGRenderableShapes(IMapInstance mapInstance, MilStdSymbol symbol, boolean selected);
    
    /**
     * This method is called by the map instances the bitmap that compose the
     * MilStd icon symbol.
     * @param sSymbolCode The symbol code that identifies the 2525 symbol.
     * @param oModifiers An array of modifiers to apply to the icon.
     * @param oAttr An array of attributes to use for the icon.
     * @return {@link IEmpImageInfo}.
     */
    IEmpImageInfo getMilStdIcon(String sSymbolCode, SparseArray oModifiers, SparseArray oAttr);

    /**
     * This method returns a SparseArray of the 2525 modifiers define in the symbol.
     * @param mapInstance The map instance making the call.
     * @param symbol the feature.
     * @return SparseArray as per MilStd 2525 renderer
     */
    SparseArray<String> getUnitModifiers(IMapInstance mapInstance, MilStdSymbol symbol);

    /**
     * This method returns a SparseArray of the 2525 modifiers define in the tactical graphic symbol.
     * @param mapInstance The map instance making the call.
     * @param symbol the feature.
     * @return SparseArray as per MilStd 2525 renderer
     */
    SparseArray<String> getTGModifiers(IMapInstance mapInstance, MilStdSymbol symbol);

    /**
     * This method returns a SparseArray of the attributes define in the symbol.
     * @param mapInstance The map instance making the call.
     * @param symbol the feature.
     * @return SparseArray as per MilStd 2525 renderer
     */
    SparseArray<String> getAttributes(IMapInstance mapInstance, MilStdSymbol symbol, boolean selected);

    /**
     * This method retrieves the far distance threshold for the map. All single point MilStd icons at or
     * beyond the far distances threshold (from the camera) are displayed as dots.
     * @param mapInstance The map instance for request.
     * @return The far distance threshold in meters.
     */
    double getFarDistanceThreshold(IMapInstance mapInstance);

    /**
     * This method retrieves the mid distance threshold for the map. All single point MiloStd icons at
     * a distance less than the mid distance threshold are displayed as fully qualified MilStd icons. The
     * labels displayed will depend on the maps MilStd label setting.
     * the distances
     * @param mapInstance The map instance for request.
     * @return The mid distance threshold in meters.
     */
    double getMidDistanceThreshold(IMapInstance mapInstance);

    /**
     * Returns the name of the cache being used. In the current implementation if we want to use the RenderCache implemented
     * by NASA WW then MilStdRenderer must use "NoBitmapCache".
     * If we want to use the EMP cache then "BitmapCache" or "AdaptiveBitmapCache" should be used.
     * "BitmapCache" and "AdaptiveBitmapCache" does not work well with NAS WW but is kept in place if we move to or add a new map engine.
     * @return
     */
    String getBitmapCacheName();

    /**
     * this methos return the selected icon scale factor for the map instance.
     * @param mapInstance
     * @return
     */
    double getSelectedIconScale(IMapInstance mapInstance);
}
