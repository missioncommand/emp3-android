package mil.emp3.mapengine.interfaces;

import android.graphics.Point;

import org.cmapi.primitives.IGeoBounds;
import org.cmapi.primitives.IGeoPosition;

import java.util.List;

import mil.emp3.api.enums.FontSizeModifierEnum;
import mil.emp3.api.enums.MapMotionLockEnum;
import mil.emp3.api.enums.MapStateEnum;
import mil.emp3.api.enums.UserInteractionEventEnum;
import mil.emp3.api.interfaces.ICamera;
import mil.emp3.api.interfaces.ICapture;
import mil.emp3.api.interfaces.IFeature;
import mil.emp3.api.interfaces.ILookAt;
import mil.emp3.api.interfaces.IMapService;
import mil.emp3.api.interfaces.IUUIDSet;
import mil.emp3.api.interfaces.IScreenCaptureCallback;
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
 * EMP is loosely split into three layers, namely API, Core and Map Engine. The API and Core together insulate the application
 * from specific Map Engine implementations. IMapInstance insulates the EMP Core from specific map engine API. In the current implementation
 * EMP supports NASA World Wind map engine. If you want to support a map engine other than NASA World Wind then you will need to
 * implement IMpaInstance interface along with other interfaces in this package. EMP also allows for multiple instances of same or different
 * map engines to be present and active in a single application. Implementors of IMapInstance must allow for multiple instances of one or more
 * map engines to operate side-by-side in a single instance of an application. Instances of map engine can either be compiled into the application
 * or can be loaded from a self contained android application.
 */
public interface IMapInstance {

    /**
     * Returns the Map View.
     * @return
     */
    android.view.View getMapInstanceAndroidView();

    /**
     * Android life cycle method.
     */
    void onResume();

    /**
     * Android life cycle method.
     */
    void onPause();

    /**
     * Android life cycle method.
     */
    void onDestroy();

    /**
     * Android life cycle method.
     * @return
     */
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
     * @param callback An implementation of the {@link IScreenCaptureCallback} interface. The EMP core
     *                 ensures that this parameter is not null. Therefore a map engine implementation
     *                 does not need to check for null. Any exception generated during the process must
     *                 invoke the failure callback with the exception as the parameter. Upon success the
     *                 map implementation must call the success callback with the ICapture object.
     * @throws UnsupportedOperationException if the map engine does not support screen capture operation.
     */
    void getCapture(IScreenCaptureCallback callback);

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
     * This method is called by the EMP core to add a service to the map.
     * @param mapService The list of one or more map services to add to the map.
     */
    void addMapService(IMapService mapService);

    /**
     * This method is called by the EMP core to remove map services from the map.
     * @param mapService The list of one or more map services to remove from the map.
     */
    void removeMapService(IMapService mapService);

    /**
     * This method is used to retrieve the map's current camera.
     * @return See {@link ICamera}
     */
    ICamera getCamera();

    /**
     * This method is used to set the camera for the map. It is the responsibility of IMapInstance to keep the
     * EMP Camera and map engine camera in sync.
     * @param oCamera The new setting. See {@link ICamera}
     * @param animate If set to true then animate to new position
     */
    void setCamera(ICamera oCamera, boolean animate);

    /**
     * This method is used to update camera setting. Camera settings are updated only if oCamera is the currentCamera.
     * This method is invoked as a result of application invoking ICamera.apply();
     * @param oCamera
     * @param animate
     */
    void applyCameraChange(ICamera oCamera, boolean animate);

    /**
     * This method is used to set the lookAt for the map. It is the responsibility of IMapInstance to keep the
     * EMP LookAt and map engine LookAt in sync.
     * @param oLookAt The new setting. See {@link ILookAt}
     * @param animate If set to true then animate to new position
     */
    void setLookAt(ILookAt oLookAt, boolean animate);

    /**
     * This method is used to update lookAt settings. LookAt settings are updated only if oLookAt is the current LookAt.
     * This method is invoked as a result of application invoking ILookAt.apply();
     */
    void applyLookAtChange(ILookAt oLookAt, boolean animate);

    /**
     * This method is used to retrieve the map current LookAt.
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

    /**
     * This method is called by the core to register a feature added event listener. The core will call this once
     * and only once upon starting the map instance. The listener registration MUST be destroyed when the onDestroy call is made.
     * The Map instance MUST generate a FeatureAddEvent each time a feature is added to the map using addFeature method.
     * @param listener The object that implements the appropriate listener see {@link mil.emp3.mapengine.events.MapInstanceFeatureAddedEvent}
     */
    void addMapInstanceFeatureAddedEventListener(MapInstanceFeatureAddedEventListener listener);

    /**
     * This method is called by the core to register a feature removed event listener. The core will call this once
     * and only once upon starting the map instance. The listener registration MUST be destroyed when the onDestroy call is made.
     * The Map instance MUST generate a FeatureRemovedEvent each time a feature is removed from the map using removeFeature method.
     * @param listener The object that implements the appropriate listener see {@link mil.emp3.mapengine.events.MapInstanceFeatureRemovedEvent}
     */
    void addMapInstanceFeatureRemovedEventListener(MapInstanceFeatureRemovedEventListener listener);

    /**
     * This method sets the maps viewing area to the coordinates contained in the bounding box provided.
     * @param bounds
     */
    void setBounds(IGeoBounds bounds);

    /**
     * This method sets the map's motion lock.
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
     * Returns height of the view.
     * @return
     */
    int getViewHeight();

    /**
     * Returns height of the view.
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
     * Instructs the map instance to render the features in the list with the selected style
     * obtained from the core.
     * @param featureList
     */
    void selectFeatures(List<IFeature> featureList);

    /**
     * Instructs the map instance to render the features in the list with their normal style.
     * obtained from the core.
     * @param featureList
     */
    void deselectFeatures(List<IFeature> featureList);

    /**
     * Sets the font size modifier for the map.
     * @param value {@link FontSizeModifierEnum}
     */
    void setFontSizeModifier(FontSizeModifierEnum value);

    /**
     * Returns the font size modifier
     * @return
     */
    FontSizeModifierEnum getFontSizeModifier();

    /**
     * This method creates a mini map associated with the map instance. The android View containing the map is
     * returned to the client. It is the responsibility of the client to add the view to a parent.
     * All touch events must be forwarded to the parent of the View. If the mini map has already been
     * created the same view must be returned.
     * @return android.view.View
     */
    android.view.View showMiniMap();

    /**
     * This method destroys the mini map. The call must remove the view from its parent if it still
     * has a parent. If the mini map has already been destroyed no action should be taken. All resources
     * associated with the mini map must be freed.
     */
    void hideMiniMap();
    
    /**
     * This method set the brightness of the background maps on the map.
     * @param setting Range 0 - 100. 0 Black, 100 white. Values less than 0 are set to 0, and values greater then 100 are set to 100.
     */
    void setBackgroundBrightness(int setting);

    /**
     * This method retrieves the current background brightness setting of the map.
     * @return Return the current value 0 - 100.
     */
    int getBackgroundBrightness();
}
