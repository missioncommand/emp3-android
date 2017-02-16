package mil.emp3.mapengine.interfaces;

import android.util.SparseArray;

import org.cmapi.primitives.IGeoBounds;

import java.util.List;

import mil.emp3.api.MilStdSymbol;
import mil.emp3.api.interfaces.IFeature;
import mil.emp3.api.interfaces.core.IStorageManager;

/**
 * This object provides interface to Mild Std renderer. Ir is constructed in the EMP Core and registered with the Map Instance for its usage.
 */
public interface IMilStdRenderer {
    void setStorageManager(IStorageManager storageManager);

    /**
     * Sets the Cache Directory used by renderer and also sets the available heap memory for the application
     * @param sCacheDir
     * @param getMemoryClass
     */
    void setRendererCacheDir(String sCacheDir, int getMemoryClass);

    /**
     * Gets a list of renderables that compose the MilStd tactical graphic symbol.
     * @param mapInstance
     * @param symbol
     * @param selected
     * @return A list of basic IFeature objects. See {@link IFeature).
     */
    List<IFeature> getTGRenderableShapes(IMapInstance mapInstance, MilStdSymbol symbol, boolean selected);
    
    /**
     * Gets the bitmap that represents the MilStd icon symbol.
     * @param sSymbolCode The symbol code that identifies the 2525 symbol.
     * @param oModifiers An array of modifiers to apply to the icon.
     * @param oAttr An array of attributes to use for the icon.
     * @return {@link IEmpImageInfo}.
     */
    IEmpImageInfo getMilStdIcon(String sSymbolCode, SparseArray oModifiers, SparseArray oAttr);

    /**
     * Gets the 2525 modifiers defined in the symbol.
     * @param mapInstance The map instance making the call.
     * @param symbol the feature.
     * @return SparseArray as per MilStd 2525 renderer
     */
    SparseArray<String> getUnitModifiers(IMapInstance mapInstance, MilStdSymbol symbol);

    /**
     * Gets the 2525 modifiers defined in the tactical graphic symbol.
     * @param mapInstance The map instance making the call.
     * @param symbol the feature.
     * @return SparseArray as per MilStd 2525 renderer
     */
    SparseArray<String> getTGModifiers(IMapInstance mapInstance, MilStdSymbol symbol);

    /**
     * Gets the attributes defines in the feature.
     * @param mapInstance The map instance making the call.
     * @param feature the feature.
     * @return SparseArray as per MilStd 2525 renderer
     */
    SparseArray<String> getAttributes(IMapInstance mapInstance, IFeature feature, boolean selected);

    /**
     * Gets the far distance threshold for the map. All single point MilStd icons at or
     * beyond the far distances threshold (from the camera) are displayed as dots.
     * @param mapInstance The map instance for request.
     * @return The far distance threshold in meters.
     */
    double getFarDistanceThreshold(IMapInstance mapInstance);

    /**
     * Gets the mid distance threshold for the map. All single point MiloStd icons at
     * a distance less than the mid distance threshold are displayed as fully qualified MilStd icons. The
     * labels displayed will depend on the maps MilStd label setting.
     * the distances
     * @param mapInstance The map instance for request.
     * @return The mid distance threshold in meters.
     */
    double getMidDistanceThreshold(IMapInstance mapInstance);

    /**
     * Returns the name of the cache used. In the current implementation if we want to use the RenderCache implemented
     * by NASA WW then MilStdRenderer must use "NoBitmapCache".
     * If we want to use the EMP cache then "BitmapCache" or "AdaptiveBitmapCache" should be used.
     * "BitmapCache" and "AdaptiveBitmapCache" does not work well with NAS WW but is kept in place if we move to or add a new map engine.
     * @return
     */
    String getBitmapCacheName();

    /**
     * Returns the selected icon scale factor for the map instance.
     * @param mapInstance
     * @return
     */
    double getSelectedIconScale(IMapInstance mapInstance);

    /**
     * Gets the list of renderables that represents the feature.
     * @param mapInstance
     * @param feature
     * @param selected
     * @return A list of basic IFeature objects. See {@link IFeature).
     */
    List<IFeature> getFeatureRenderableShapes(IMapInstance mapInstance, IFeature feature, boolean selected);
}
