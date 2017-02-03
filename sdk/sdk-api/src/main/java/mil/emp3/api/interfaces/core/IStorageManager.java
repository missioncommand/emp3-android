package mil.emp3.api.interfaces.core;

import org.cmapi.primitives.IGeoBase;
import org.cmapi.primitives.IGeoBounds;
import org.cmapi.primitives.IGeoFillStyle;
import org.cmapi.primitives.IGeoLabelStyle;
import org.cmapi.primitives.IGeoStrokeStyle;

import java.util.Map;

import mil.emp3.api.MilStdSymbol;
import mil.emp3.api.enums.IconSizeEnum;
import mil.emp3.api.enums.MilStdLabelSettingEnum;
import mil.emp3.api.enums.VisibilityActionEnum;
import mil.emp3.api.enums.VisibilityStateEnum;
import mil.emp3.api.exceptions.EMP_Exception;
import mil.emp3.api.interfaces.*;
import mil.emp3.api.interfaces.core.storage.IClientMapRestoreData;
import mil.emp3.api.interfaces.core.storage.IClientMapToMapInstance;
import mil.emp3.mapengine.interfaces.IMapInstance;

public interface IStorageManager {
    void setEventManager(IEventManager eventManager);

    IContainer findContainer(java.util.UUID targetId);

    IClientMapRestoreData addMapNameToRestoreDataMapping(IMap map) throws EMP_Exception;

    void updateMapNameToRestoreDataMapping(IMap map, String engineClassName, String engineApkName) throws EMP_Exception;

    IClientMapRestoreData getRestoreData(IMap map);

    void onSaveInstanceState(boolean keepState);

    IMapInstance getMapInstance(IMap clientMap);

    IClientMapToMapInstance getMapMapping(IMapInstance mapInstance);

    IClientMapToMapInstance getMapMapping(IMap clientMap);

    void swapMapInstance(IMap clientMap, IMapInstance newMapInstance) throws EMP_Exception;

    IContainerSet getParentsOf(IGeoBase oObject);

    void setVisibilityOnMap(IMap map, IUUIDSet targetIdList, VisibilityActionEnum actionEnum) throws EMP_Exception;

    boolean isOnMap(IMap map, IContainer target);

    void setVisibilityOnMap(IMap map, IContainer target, IContainer parent, VisibilityActionEnum actionEnum) throws EMP_Exception;

    VisibilityStateEnum getVisibilityOnMap(IMap map, IContainer target);

    VisibilityStateEnum getVisibilityOnMap(IMap map, IContainer target, IContainer parent);

    void addOverlays(IMap map, java.util.List<IOverlay> overlays, boolean visible) throws EMP_Exception;

    void apply(IFeature feature) throws EMP_Exception;

    java.util.List<IFeature> getChildFeatures(IContainer parent);

    java.util.List<IOverlay> getChildOverlays(IContainer parent);

    java.util.List<IGeoBase> getImmediateChildren(IContainer container);

    java.util.List<IContainer> getParents(IContainer childContainer);

    java.util.List<IOverlay> getParentOverlays(IFeature childFeature);

    java.util.List<IFeature> getParentFeatures(IFeature childFeature);

    void addOverlays(IOverlay parentOverlay, java.util.List<IOverlay> overlays, boolean visible) throws EMP_Exception;

    void addFeatures(IOverlay parentOverlay, java.util.List<IFeature> featureList, boolean visible) throws EMP_Exception;

    void addFeatures(IFeature parentFeature, java.util.List<IFeature> featureList, boolean visible) throws EMP_Exception;

    void removeFeatures(IFeature parentFeature, java.util.List<IFeature> features) throws EMP_Exception;

    void removeFeatures(IOverlay parentOverlay, java.util.List<IFeature> features) throws EMP_Exception;

    void removeOverlays(IMap clientMap, java.util.List<IOverlay> overlays) throws EMP_Exception;

    void removeOverlays(IOverlay parentOverlay, java.util.List<IOverlay> overlays) throws EMP_Exception;

    void removeChildren(IContainer parentContainer) throws EMP_Exception;

    void redrawAllFeatures(IMap clientMap);

    void addMapService(IMap map, IMapService mapService) throws EMP_Exception;

    void removeMapService(IMap map, IMapService mapService) throws EMP_Exception;

    java.util.List<IMapService> getMapServices(IMap map);

    void MapServiceUpdated(IMapService mapService) throws EMP_Exception;

    void setIconSize(IMap map, IconSizeEnum eSize) throws EMP_Exception;

    IconSizeEnum getIconSize(IMap map);

    void setMilStdLabels(IMap map, MilStdLabelSettingEnum labelSetting) throws EMP_Exception;

    MilStdLabelSettingEnum getMilStdLabels(IMap map);

    IconSizeEnum getIconSize(IMapInstance mapInstance);

    int getIconPixelSize(IMapInstance mapInstance);

    MilStdLabelSettingEnum getMilStdLabels(IMapInstance mapInstance);

    void addDrawFeature(IMap oMap, IFeature oFeature) throws EMP_Exception;

    void removeDrawFeature(IMap oMap, IFeature oFeature) throws EMP_Exception;

    void setFarDistanceThreshold(IMap oMap, double dValue);

    double getFarDistanceThreshold(IMap oMap);

    double getFarDistanceThreshold(IMapInstance mapInstance);

    void setMidDistanceThreshold(IMap oMap, double dValue);

    /**
     * This is used internally by AdaptiveBitmapCache to override application set value. It doesn't update the
     * application set value, it directly sets the value on corresponding mapInstance.
     * @param oMap
     * @param dValue
     */
    void setMidDistanceThresholdActual(IMap oMap, double dValue);

    double getMidDistanceThreshold(IMap oMap);

    double getMidDistanceThreshold(IMapInstance mapInstance);

    /**
     * Returns MidDistance threshold for all maps contains within the application. This was added for use by AdaptiveBiymapCache.
     * @return
     */
    Map<IMap, Double> getMidDistanceThreshold();

    void setBounds(IMap map, IGeoBounds bounds);
    IGeoBounds getBounds (IMap map);

    /**
     * This method returns the default fill style for MilStd 2525 symbols.
     * @param symbol
     * @return
     */
    IGeoFillStyle getDefaultFillStyle(MilStdSymbol symbol);

    /**
     * This method returns the default stroke style for MilStd 2525 symbols.
     * @param symbol
     * @return
     */
    IGeoStrokeStyle getDefaultStrokeStyle(MilStdSymbol symbol);

    /**
     * This method selects the features on the map. If the feature is already selected or not on the map,
     * no action is taken on the feature.
     * @param map The map the selection it to be applied.
     * @param features The list of features to select.
     */
    public void selectFeatures(IMap map, java.util.List<IFeature> features);

    /**
     * This method marks the feature as NOT selected on the map. If the feature is not mark selected
     * no action is taken.
     * @param map The map the deselection it to be applied.
     * @param features The list of feature to deselected.
     */
    public void deselectFeatures(IMap map, java.util.List<IFeature> features);

    /**
     * This method retrieves the list of feature that are marked selected on the map.
     * @param map The map to retrieve the list from.
     * @return A list of IFeatures. If there are no features selected the list is empty.
     */
    public java.util.List<IFeature> getSelected(IMap map);

    /**
     * This method clears the map selected list.
     * @param map The map the selection list to clear.
     */
    public void clearSelected(IMap map);

    /**
     * This method check if the feature is selected on the map.
     * @param map
     * @param feature
     * @return True if it is selected false otherwise.
     */
    public boolean isSelected(IMap map, IFeature feature);

    /**
     * This method returns the select stroke style for the map instance.
     * @param mapInstance
     * @return
     */
    public IGeoStrokeStyle getSelectedStrokeStyle(IMapInstance mapInstance);

    /**
     * This method returns the select label style for the map instance.
     * @param mapInstance
     * @return
     */
    public IGeoLabelStyle getSelectedLabelStyle(IMapInstance mapInstance);

    /**
     * This method returns the select icon scale for the map instance.
     * @param mapInstance
     * @return
     */
    public double getSelectedIconScale(IMapInstance mapInstance);

    /**
     * This method set the default altitude mode if the feature's altitude mode is not set.
     * @param feature
     */
    void setDefaultAltitudeMode(IFeature feature);
}
