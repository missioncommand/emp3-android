package mil.emp3.mapengine.interfaces;

import android.graphics.Point;

import org.cmapi.primitives.IGeoBounds;
import org.cmapi.primitives.IGeoPosition;

import java.util.List;

import mil.emp3.api.enums.MapMotionLockEnum;
import mil.emp3.api.enums.MapStateEnum;
import mil.emp3.api.enums.UserInteractionEventEnum;
import mil.emp3.api.interfaces.ICamera;
import mil.emp3.api.interfaces.ICapture;
import mil.emp3.api.interfaces.IContainer;
import mil.emp3.api.interfaces.IFeature;
import mil.emp3.api.interfaces.ILookAt;
import mil.emp3.api.interfaces.IMapService;
import mil.emp3.api.interfaces.IUUIDSet;
import mil.emp3.mapengine.api.FeatureVisibilityList;
import mil.emp3.mapengine.events.MapInstanceFeatureUserInteractionEvent;
import mil.emp3.mapengine.events.MapInstanceStateChangeEvent;
import mil.emp3.mapengine.listeners.MapInstanceFeatureAddedEventListener;
import mil.emp3.mapengine.listeners.MapInstanceFeatureRemovedEventListener;
import mil.emp3.mapengine.listeners.MapInstanceFeatureUserInteractionEventListener;
import mil.emp3.mapengine.listeners.MapInstanceStateChangeEventListener;
import mil.emp3.mapengine.listeners.MapInstanceUserInteractionEventListener;
import mil.emp3.mapengine.listeners.MapInstanceViewChangeEventListener;

/**
 * This is the interface that must be implemented by all map engines. It represent the interface to one instance
 * of a map implemented by the engine APK The APK must be able to create multiple independent instances at any one time.
 * Each map instance must expose this interface.
 */
public interface IMapInstance {

    android.view.View getMapInstanceAndroidView();

    void onResume();
    void onPause();
    void onDestroy();
    android.view.View onCreateView();

    /**
     * This method is used by the EMP core to retrieve the map engines properties.
     * @return See {@link IMapEngineProperties}
     */
    IMapEngineProperties getMapEngineProperties();

    /**
     * This method is called by the EMP core to identify the map instances capabilities. It is used
     * to ensure that the core does NOT attempt an operation on the map engine that it does not support.
     * @return See {@link IMapEngineCapabilities}
     */
    IMapEngineCapabilities getCapabilities();
    
    /**
     * This method retrieves the map engine's requirement set. It is queried by the core
     * to properly access the map engines capabilities.
     * @return {@link IMapEngineRequirements}
     */
    IMapEngineRequirements getRequirements();

    /**
     * This method is called to retrieve an image of the current viewing area of the map.
     * @return An {@link ICapture} object or null if not supported.
     */
    ICapture getCapture();

    /**
     * This method is called by the core to set the visibility of the features listed to the setting indicated.
     * @param visibilityList A list of unique Ids of the affected features and their visibility setting.
     */
    void setVisibility(ISetVisibilityList visibilityList);

    /**
     * This method is called by the EMP core to add features to the map.
     * @param features The list of one or more feature to add to the map. See {@link FeatureVisibilityList}
     */
    void addFeatures(FeatureVisibilityList features);

    /**
     * This method is called by the EMP core to remove features from the map.
     * @param features The list of one or more unique Id of features to remove from the map.
     */
    void removeFeatures(IUUIDSet features);

    /**
     * This method is called by the EMP core to add a WMS service to the map.
     * @param mapService The list of one or more map services to add to the map.
     */
    void addMapService(IMapService mapService);

    /**
     * This method is called by the EMP core to remove map services from the map.
     * @param mapService The list of one or more map services to remove from the map.
     */
    void removeMapService(IMapService mapService);

    /**
     * This method is used to retrieve the map current camera.
     * @return See {@link ICamera}
     */
    ICamera getCamera();

    /**
     * This method is used to set the camera for the map. Once the camera values are set in the map,
     * the map registers for camera change events for the camera and must apply the camera
     * changes to the map view on each event.
     * @param oCamera The new setting. See {@link ICamera}
     * @param animate If set to true then animate to new position
     */
    void setCamera(ICamera oCamera, boolean animate);

    /**
     * This method is used to set the camera for the map. Once the camera values are set in the map,
     * the map registers for camera change events for the camera and must apply the camera
     * changes to the map view on each event.
     * @param oLookAt The new setting. See {@link ILookAt}
     * @param animate If set to true then animate to new position
     */
    void setLookAt(ILookAt oLookAt, boolean animate);

    /**
     * This method is used to retrieve the map current camera.
     * @return See {@link ILookAt}
     */
    ILookAt getLookAt();

    /**
     * This method is called by the core to register a Feature user interaction event listener. The core will call this once
     * and only once upon starting the map instance. The listener registration MUST be destroyed when the onDestroy call is made.
     * The Map instance MUST generate a {@link MapInstanceFeatureUserInteractionEvent} each time the user performs any of the
     * actions defined in {@link UserInteractionEventEnum} on a feature currently visible on the map.
     * @param listener The object that implements the appropriate listener see {@link MapInstanceFeatureUserInteractionEventListener}
     */
    void addMapInstanceFeatureUserInteractionEventListener(MapInstanceFeatureUserInteractionEventListener listener);

    /**
     * This method is called by the core to register a map state change event listener. The core will call this once
     * and only once upon starting the map instance. The listener registration MUST be destroyed when the onDestroy call is made.
     * The Map instance MUST generate a {@link MapInstanceStateChangeEvent} each time the map changes from one state (See {@link MapStateEnum}) to another.
     * @param listener The object that implements the appropriate listener see {@link MapInstanceStateChangeEventListener}
     */
    void addMapInstanceStateChangeEventListener(MapInstanceStateChangeEventListener listener);

    /**
     * This method is called by the core to register a map user interaction event listener. The core will call this once
     * and only once upon starting the map instance. The listener registration MUST be destroyed when the onDestroy call is made.
     * The Map instance MUST generate a MapInstanceUserInteractionEvent each time the user performs any of the
     * actions defined in {@link UserInteractionEventEnum} on the map.
     * @param listener The object that implements the appropriate listener see {@link MapInstanceUserInteractionEventListener}
     */
    void addMapInstanceUserInteractionEventListener(MapInstanceUserInteractionEventListener listener);

    /**
     * This method is called by the core to register a map view change event listener. The core will call this once
     * and only once upon starting the map instance. The listener registration MUST be destroyed when the onDestroy call is made.
     * The Map instance MUST generate a MapInstanceViewChangeEvent each time the map's viewing area is changed. The
     * map engine implementation must ensure that an excessive number of consecutive map view change events are not generated. I.E.
     * continuous view change events due to animation should not be generated.
     * @param listener The object that implements the appropriate listener see {@link MapInstanceViewChangeEventListener}
     */
    void addMapInstanceViewChangeEventListener(MapInstanceViewChangeEventListener listener);

    void addMapInstanceFeatureAddedEventListener(MapInstanceFeatureAddedEventListener listener);
    void addMapInstanceFeatureRemovedEventListener(MapInstanceFeatureRemovedEventListener listener);

    /**
     * This method sets the maps viewing area to the coordinates contained in the bounding box provided.
     * If this was designed for zoomIt support then it should be removed as all calculations are done in the core. Methods
     * getFieldOfView(), getViewHeight() and getViewWidth() were added to this interface for zoomIt support.
     * @param bounds
     */
    void setBounds(IGeoBounds bounds);

    /**
     * This method set the map's motion lock.
     * @param mode see {@link MapMotionLockEnum}
     */
    void setMotionLockMode(MapMotionLockEnum mode);

    /**
     * This method is called by the core to register a MilStd renderer with the map instance.
     * The instance must call this renderer to obtain the basic shapes of a MilStd feature.
     * @param oRenderer see {@link IMilStdRenderer}
     */
    void registerMilStdRenderer(IMilStdRenderer oRenderer);

    /**
     * Returns field of view. Currently used by the core for zoomIt calculations.
     * @return field of view in degrees
     */
    double getFieldOfView();

    /**
     * Returns height of the view. Currently used by the core for zoomIt calculations to adjust camera altitude.
     * @return
     */
    int getViewHeight();

    /**
     * Returns height of the view. Currently used by the core for zoomIt calculations to adjust camera altitude.
     * @return
     */
    int getViewWidth();

    /**
     * This method sets the threshold of the distances beyond which the map displays MilStd single point
     * icons as dots.
     * @param dValue Distance in meters.
     */
    void setFarDistanceThreshold(double dValue);

    /**
     * This method sets the threshold of the distances beyond which the map displays MilStd single point
     * icons with no modifiers. Icons at distances less than this threshold get displayed as fully qualified icons
     * as per the label setting.
     * @param dValue Distance in meters.
     */
    void setMidDistanceThreshold(double dValue);

    /**
     * Returns version information for the map engine
     * @Param builder
     * @Param buildConfigFields
     * @return
     */
    void getVersionInformation(StringBuilder builder, List<String> buildConfigFields);

    /**
     * This method returns the current bounds of the maps viewing area.
     * @return IGeoBounds
     */
    IGeoBounds getMapBounds();

    /**
     * geoToScreen - convert from GeoPosition to screen coordinates
     * @param pos
     * @return a screen X,Y coordinate for the provided GeoPosition.*/

    android.graphics.Point geoToScreen(IGeoPosition pos);

    /**
     * screenToGeo - converts from screen coordinates to GeoPosition
     * @param point
     * @return a GeoPosition coordinate for the provided screen X,Y coordinate.
     */

    IGeoPosition screenToGeo(android.graphics.Point point);

    /**
     * geoToContainer - convert from GeoPosition to container coordinates
     * @param pos
     * @return a container X,Y coordinate for the provided GeoPosition.
     */

    Point geoToContainer(IGeoPosition pos);

    /**
     * containerToGeo - converts from container coordinates to GeoPosition
     * @param point
     * @return a GeoPosition coordinate for the provided container X,Y coordinate.
     */

    IGeoPosition containerToGeo(Point point);

    /**
     * This method instructs the map instance to render the features in the list with the selected style
     * obtained from the core.
     * @param featureList
     */
    void selectFeatures(java.util.List<IFeature> featureList);

    /**
     * This method instructs the map instance to render the features in the list with their normal style.
     * obtained from the core.
     * @param featureList
     */
    void deselectFeatures(java.util.List<IFeature> featureList);
}
