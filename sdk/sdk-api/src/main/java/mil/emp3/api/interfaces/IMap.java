package mil.emp3.api.interfaces;

import android.graphics.Point;
import android.view.View;

import org.cmapi.primitives.IGeoBounds;
import org.cmapi.primitives.IGeoPosition;
import org.cmapi.primitives.IGeoStrokeStyle;

import java.security.InvalidParameterException;
import java.util.List;

import mil.emp3.api.enums.EditorMode;
import mil.emp3.api.enums.FontSizeModifierEnum;
import mil.emp3.api.enums.IconSizeEnum;
import mil.emp3.api.enums.MapGridTypeEnum;
import mil.emp3.api.enums.MapMotionLockEnum;
import mil.emp3.api.enums.MapStateEnum;
import mil.emp3.api.enums.MilStdLabelSettingEnum;
import mil.emp3.api.enums.MirrorCacheModeEnum;
import mil.emp3.api.enums.VisibilityActionEnum;
import mil.emp3.api.enums.VisibilityStateEnum;
import mil.emp3.api.exceptions.EMP_Exception;
import mil.emp3.api.listeners.EventListenerHandle;
import mil.emp3.api.listeners.ICameraEventListener;
import mil.emp3.api.listeners.IDrawEventListener;
import mil.emp3.api.listeners.IEditEventListener;
import mil.emp3.api.listeners.IFeatureDrawEventListener;
import mil.emp3.api.listeners.IFreehandEventListener;
import mil.emp3.api.listeners.IMapFeatureAddedEventListener;
import mil.emp3.api.listeners.IMapFeatureRemovedEventListener;
import mil.emp3.api.listeners.IMapFreehandEventListener;
import mil.emp3.api.listeners.IMapInteractionEventListener;
import mil.emp3.api.listeners.IMapStateChangeEventListener;
import mil.emp3.api.listeners.IMapViewChangeEventListener;

/**
 * This interface defines the access to a map instance.
 *
 * Map represents the container that encapsulates the map displayed on the users screen. All overlays and Features are
 * contained within the Map. Map is responsible for starting and displaying a map engine of users choice. An application
 * can display multiple maps on the screen. Applications use either {@Link mil.emp3.api.MapView} or {@Link mil.emp3.api.MapFragment} to build and
 * display a map.
 */
public interface IMap extends IContainer {

    /**
     * Sets mirror cache mode to be either EGRESs, INGRESS or BIDIRECTIONAL {@link MirrorCacheModeEnum}
     * @param mode the operational mode for MirrorCache
     */
    void setMirrorCacheMode(MirrorCacheModeEnum mode);

    /**
     * Returns true if map is enabled to publish data to Mirror Cache.
     * @return true if egress is enabled, false otherwise.
     */
    boolean isEgressEnabled();

    /**
     * Returns true if map is enabled to consume data from the Mirror Cache.
     * @return true if ingress is enabled, false otherwise.
     */
    boolean isIngressEnabled();

    /**
     * Describes the state of this IMap implementation instance {@Link mil.emp3.api.enums.MapStateEnum}
     * @return MapStateEnum
     */
    MapStateEnum getState();

    /**
     * This method request a screen capture from the map. Client software must account for the event of
     * a callback function being invoked prior to the function returning.
     * @param callback An implementation of the {@link IScreenCaptureCallback} interface.
     * @throws InvalidParameterException if the callback parameter is null.
     * @throws UnsupportedOperationException if the map engine does not support screen capture operations.
     */
    void getScreenCapture(IScreenCaptureCallback callback);

    /**
     * This method retrieves the map's current camera.
     * @return ICamera
     */
    ICamera getCamera();

    /**
     * This method sets the map's camera therefore changing the viewing area of the map.
     * The map registers for camera change events for the camera and will update
     * the map viewing area on each change event.
     * @param camera This parameter must contain the new camera setting to be applied to map's viewing area.
     * @param animate If set to true then map will animate to the new position
     * @throws EMP_Exception The exception is raised if a processing error is encountered.
     */
    void setCamera(ICamera camera, boolean animate)
            throws EMP_Exception;

    /**
     * This method retrieves the map's current lookAt.
     * @return ILookAt
     */
    ILookAt getLookAt();

    /**
     * This method sets the map's lookAt therefore changing the viewing area of the map.
     * The map registers for lookAt change events for the lookAt and will update
     * the map viewing area on each change event.
     * @param lookAt This parameter must contain the new lookAt setting to be applied to map's viewing area.
     * @param animate If set tto true then map will animate to the new position.
     * @throws EMP_Exception The exception is raised if a processing error is encountered.
     */
    void setLookAt(ILookAt lookAt, boolean animate)
            throws EMP_Exception;

    /**
     * This method retrieves a list of all features currently plotted in the map.
     * @return This method returns a java.util.List of IFeature objects.
     */
    List<IFeature> getAllFeatures();

    /**
     * This method retrieves a list of all overlays currently plotted in the map.
     * @return This method returns a java.util.List of IOverlay objects.
     */
    List<IOverlay> getAllOverlays();

    /**
     * This method adds an overlay container to the root of the map.
     * @param overlay This parameter defines the overlay container.
     * @param visible True if the overlay is to be made visible, false otherwise.
     * @throws EMP_Exception The exception is raised if a processing error is encountered.
     */
    void addOverlay(IOverlay overlay, boolean visible)
            throws EMP_Exception;

    /**
     * This method adds the overlay containers on the provided list to the root of the map.
     * @param overlays This parameters is a java.util.ArrayList of one or more overlay containers.
     * @param visible True if the overlays are to be made visible, false otherwise.
     * @throws EMP_Exception The exception is raised if a processing error is encountered.
     */
    void addOverlays(List<IOverlay> overlays, boolean visible)
            throws EMP_Exception;

    /**
     * This method removes an overlay containers from the root of the map.
     * @param overlay This parameters defines the overlay that is to be removed from the root of the map.
     * @throws EMP_Exception The exception is raised if a processing error is encountered.
     */
    void removeOverlay(IOverlay overlay)
            throws EMP_Exception;

    /**
     * This method removes the overlay containers on the provided list from the root of the map.
     * @param overlays This parameters is a java.util.ArrayList of one or more overlay containers.
     * @throws EMP_Exception The exception is raised if a processing error is encountered.
     */
    void removeOverlays(List<IOverlay> overlays)
            throws EMP_Exception;

    /**
     * This method directs the map to utilize the map data provided by the service defined in the parameter.
     * @param mapService This parameter provides the parameters necessary to access the service.
     * @throws EMP_Exception The exception is raised if a processing error is encountered.
     */
    void addMapService(IMapService mapService)
            throws EMP_Exception;

    /**
     * This method removes the service from the map.
     * @param mapService This parameter identifies the service.
     * @throws EMP_Exception The exception is raised if a processing error is encountered.
     */
    void removeMapService(IMapService mapService)
            throws EMP_Exception;

    /**
     * This method retrieves a list of map services that are being used by this map.
     * @return a java.util.List of IMapService.
     */
    List<IMapService> getMapServices();
    
    /**
     * This method performs the visibility action on the target object under all 
     * of its parents on this map.
     * @param target The object to perform the action on. (See {@link IOverlay}, {@link IFeature})
     * @param actionEnum The action to perform. See {@link VisibilityActionEnum}.
     * @throws EMP_Exception If the target is not on the map or the target is not an overlay or feature.
     */
    void setVisibility(IContainer target, VisibilityActionEnum actionEnum)
            throws EMP_Exception;

    /**
     * This method performs the visibility action on all the target objects listed under all 
     * of there parents on this map.
     * @param targetList A list of one or more objects to perform the action on. (See {@link IOverlay}, {@link IFeature})
     * @param actionEnum The action to perform. See {@link VisibilityActionEnum}.
     * @throws EMP_Exception If any target is not on the map or not an overlay or feature.
     */
    void setVisibility(IContainerSet targetList, VisibilityActionEnum actionEnum)
            throws EMP_Exception;

    /**
     * This method performs the visibility action on the target object identified by
     * the targetId under all of its parents on this map.
     * @param targetId The unique identifier of the object to perform the action on.
     * @param actionEnum The action to perform. See {@link VisibilityActionEnum}.
     * @throws EMP_Exception If the target identified by the Id is not on the map or is not an overlay or feature.
     */
    void setVisibility(java.util.UUID targetId, VisibilityActionEnum actionEnum)
            throws EMP_Exception;

    /**
     * This method performs the visibility action on the targets identified on the 
     * list under all of their parents on this map.
     * @param targetIdList A list of unique identifiers of the objects to perform the action on.
     * @param actionEnum The action to perform. See {@link VisibilityActionEnum}.
     * @throws EMP_Exception If the target identified by the Id is not on the map or is not an overlay or feature.
     */
    void setVisibility(IUUIDSet targetIdList, VisibilityActionEnum actionEnum)
            throws EMP_Exception;

    /**
     * This method performs the visibility action on the target object under the 
     * specified parent on this map.
     * @param target The object to perform the action on. (See {@link IOverlay}, {@link IFeature})
     * @param parent The parent object of the target that the action will be applied to,
     * @param actionEnum The action to perform. See {@link VisibilityActionEnum}.
     * @throws EMP_Exception If the target (or parent) are not on the map, or the target (or parent) are not an overlay or feature.
     */
    void setVisibility(IContainer target, IContainer parent, VisibilityActionEnum actionEnum)
            throws EMP_Exception;

    /**
     * This method performs the visibility action on the target object under the 
     * specified parent on this map.
     * @param targetId The Id of the object to perform the action on. (See {@link IOverlay}, {@link IFeature})
     * @param parentId The Id of the parent object of the target that the action will be applied to,
     * @param actionEnum The action to perform. See {@link VisibilityActionEnum}.
     * @throws EMP_Exception If the target (or parent) are not on the map, or the target (or parent) are not an overlay or feature.
     */
    void setVisibility(java.util.UUID targetId, java.util.UUID parentId, VisibilityActionEnum actionEnum)
            throws EMP_Exception;

    /**
     * This method retrieves the visibility of the target object on the map.
     * @param target The object of which the visibility state is requested. (See {@link IOverlay}, {@link IFeature})
     * @return {@link VisibilityStateEnum#VISIBLE} - indicates that the object is 
     * visible on the map and its child objects visibility depends on their individual visibility states.
     * {@link VisibilityStateEnum#HIDDEN} - indicates that the object is not visible
     * on the map and its child objects visibility state depend on their visibility 
     * state with other parent objects
     * null - If the target object is not on the map or not an overlay or feature.
     */
    VisibilityStateEnum getVisibility(IContainer target);
    
    /**
     * This method retrieves the visibility of the target object on the map.
     * @param target The object of which the visibility state is requested. (See {@link IOverlay}, {@link IFeature})
     * @param parent A parent of the target object of which the visibility state is requested. (See {@link IOverlay}, {@link IFeature})
     * @return {@link VisibilityStateEnum#VISIBLE} - indicates that the object is 
     * visible on the map and its child objects visibility depends on their individual visibility states.
     * {@link VisibilityStateEnum#HIDDEN} - indicates that the target object under the
     * specified parent is marked to be not visible on the map. The target objects 
     * actual visibility depends on its visibility setting under another parent if it has others.
     * {@link VisibilityStateEnum#VISIBLE_ANCESTOR_HIDDEN} - indicates that the target 
     * object under the specified parent is marked to be visible on the map, however
     * no ancestral path from the parent to this root map is visible.
     * null - If the target (or parent) object are not on the map or not an overlay or feature.
     */
    VisibilityStateEnum getVisibility(IContainer target, IContainer parent);

    /**
     * This method registers a listener for map state change events.
     * @param listener The object that implements the proper handler for the map state change event.
     * @return EventListenerHandle A handle to the registration which is needed to unregister the listener.
     * @throws EMP_Exception The exception is raised if the listener is null.
     */
    EventListenerHandle addMapStateChangeEventListener(IMapStateChangeEventListener listener) throws EMP_Exception;

    /**
     * This method registers a listener for map view change events.
     * @param listener The object that implements the proper handler for the map view change event.
     * @return EventListenerHandle A handle to the registration which is needed to unregister the listener.
     * @throws EMP_Exception The exception is raised if the listener is null.
     */
    EventListenerHandle addMapViewChangeEventListener(IMapViewChangeEventListener listener) throws EMP_Exception;

    /**
     * This method registers a listener for all user interaction events.
     * The listener is called when any user interaction event occurs on this map.
     * @param listener The object that implements the proper handler for the user interaction event.
     * @return EventListenerHandle A handle to the registration which is needed to unregister the listener.
     * @throws EMP_Exception The exception is raised if the listener is null.
     */
    EventListenerHandle addMapInteractionEventListener(IMapInteractionEventListener listener) throws EMP_Exception;

    /**
     * This method registers a listener for all camera events.
     * The listener is called when any camera event occurs on this map.
     * @param listener The object that implements the proper handler for the camera event.
     * @return EventListenerHandle A handle to the registration which is needed to unregister the listener.
     * @throws EMP_Exception The exception is raised if the listener is null.
     */
    EventListenerHandle addCameraEventListener(ICameraEventListener listener) throws EMP_Exception;

    /**
     * This method registers a listener for free hand draw events on this map.
     * @param listener The object that implements the IMapFreeHandEventListener interface.
     * @return a registration handle which is needed to unregister the listener.
     * @throws EMP_Exception The exception is raised if the listener is null.
     */
    EventListenerHandle addMapFreehandEventListener(IMapFreehandEventListener listener) throws EMP_Exception;

    /**
     * This method registers a listener for all feature draw events on the map.
     * @param listener The object that implements the proper listener for the feature draw events.
     * @return A handle to the registration which is needed to unregister the listener. See {@link EventListenerHandle}
     * @throws EMP_Exception The exception is raised if the listener is null.
     */
    EventListenerHandle addFeatureDrawEventListener(IFeatureDrawEventListener listener) throws EMP_Exception;

    /**
     * This method registers a listener for all feature added events on the map.
     * @param listener The object that implements the proper listener for the feature draw events.
     * @return A handle to the registration which is needed to unregister the listener. See {@link EventListenerHandle}
     * @throws EMP_Exception The exception is raised if the listener is null.
     */
    EventListenerHandle addMapFeatureAddedEventListener(IMapFeatureAddedEventListener listener) throws EMP_Exception;

    /**
     * This method registers a listener for all feature removed events on the map.
     * @param listener The object that implements the proper listener for the feature draw events.
     * @return A handle to the registration which is needed to unregister the listener. See {@link EventListenerHandle}
     * @throws EMP_Exception The exception is raised if the listener is null.
     */
    EventListenerHandle addMapFeatureRemovedEventListener(IMapFeatureRemovedEventListener listener) throws EMP_Exception;

    /**
     * This method set the size that all icons are displayed. This includes MilStd Point icons.
     * @param eSize {@link IconSizeEnum}
     * @throws EMP_Exception The exception is raised if a processing error is encountered.
     */
    void setIconSize(IconSizeEnum eSize)
            throws EMP_Exception;

    /**
     * This method returns the current icon size setting.
     * @return {@link IconSizeEnum}
     */
    IconSizeEnum getIconSize();

    /**
     * This method indicates to the map engine which MilStd labels to display when renderering MilStd symbols.
     * @param labelSetting {@link MilStdLabelSettingEnum}
     * @throws EMP_Exception The exception is raised if a processing error is encountered.
     */
    void setMilStdLabels(MilStdLabelSettingEnum labelSetting)
            throws EMP_Exception;

    /**
     * This method retrieves the current MilStd label setting.
     * @return {@link MilStdLabelSettingEnum}
     */
    MilStdLabelSettingEnum getMilStdLabels();

    /**
     * This method will swap the current map instance with a new one.
     * @param properties The property key value pair needed to support map swapping. The required properties
     *                   are {@link mil.emp3.api.enums.Property#ENGINE_CLASSNAME} and {@link mil.emp3.api.enums.Property#ENGINE_APKNAME} if
     *                   the map is implemented in an APK.
     * @throws EMP_Exception The exception is raised if a processing error is encountered.
     * @return The new android.view.View object for the new map instance.
     */
    IMap swapMapEngine(IEmpPropertyList properties)
        throws EMP_Exception;

    /**
     * This method places the specified feature into edit mode and EditorMode changes to EDIT_MODE.
     * @param oFeature The specified feature
     * @throws EMP_Exception The exception is raised if a processing error is encountered. I.E.
     * If the feature is currently not on this map, or the map can't edit a feature of that type,
     * or there is a draw or edit in progress or MapMotionLockMode is LOCK/SMART_LOCK.
     */
    void editFeature(IFeature oFeature)
            throws EMP_Exception;

    /**
     * This method places the specified feature into edit mode and EditorMode changes to EDIT_MODE.
     * @param oFeature The specified feature
     * @param listener A listener to handler the edit session events. This listener is
     * unregistered when the edit session is completed or canceled.
     * @throws EMP_Exception The exception is raised if a processing error is encountered. I.E.
     * If the feature is currently not on this map, or the map can't edit a feature of that type,
     * or there is a draw or edit in progress or MapMotionLockMode is LOCK/SMART_LOCK.
     */
    void editFeature(IFeature oFeature, IEditEventListener listener)
            throws EMP_Exception;

    /**
     * This method cancels the edit session on the feature and EditorMode changes to INACTIVE.
     * @throws EMP_Exception The exception is raised if a processing error is encountered. I.E.
     * If the feature is currently not on this map, or the feature is not in edit mode.
     */
    void cancelEdit()
            throws EMP_Exception;

    /**
     * This method instructs the map to complete the edit session on the feature. All the changes are updated in the core and
     * and EditorMode changes to INACTIVE.
     * @throws EMP_Exception The exception is raised if a processing error is encountered. I.E.
     * If the feature is currently not on this map, or the feature is not in edit mode.
     */
    void completeEdit()
            throws EMP_Exception;


    /**
     * This method instructs the map to draw the feature specified and EditorMode changes to DRAW_MODE
     * @param oFeature The specified feature
     * @param listener A draw event listener that will received the draw event. This listener will
     * be unregistered when the draw if complete or canceled.
     * @throws EMP_Exception The exception is raised if a processing error is encountered. I.E.
     * If the map is already drawing or editing, or the map can't draw a feature of that type, or
     * MapMotionLockMode is LOCK or SMART_LOCK.
     */
    void drawFeature(IFeature oFeature, IDrawEventListener listener)
            throws EMP_Exception;

    /**
     * This method instructs the map to draw the feature specified and EditorMode changes to DRAW_MODE
     * @param oFeature The specified feature
     * @throws EMP_Exception The exception is raised if a processing error is encountered. I.E.
     * If the map is already drawing or editing, or the map can't draw a feature of that type.
     */
    void drawFeature(IFeature oFeature)
            throws EMP_Exception;

    /**
     * This method cancels the draw session and EditorMode changes to INACTIVE.
     * @throws EMP_Exception The exception is raised if a processing error is encountered.
     * If the map is NOT in draw mode the call has no effect.
     */
    void cancelDraw()
            throws EMP_Exception;

    /**
     * This method instructs the map to complete the draw session and EditorMode changes to INACTIVE. All the changes are updated in the core.
     * @throws EMP_Exception The exception is raised if a processing error is encountered.
     * If the map is not in draw mode this call has no effect.
     */
    void completeDraw()
            throws EMP_Exception;

    /**
     * This methods places the map into freehand draw mode if no exception is raised and EditorMode changes to FREEHAND_MODE.
     * @param initialStyle This parameters contains the initial style to use for the draw. A value of null causes an EMP_Exception.
     * @throws EMP_Exception if the map does not support free hand drawing, or there is a draw or edit in progress, or MapMotionLockMode
     *     is LOCKED or SMART_LOCK.
     */
    void drawFreehand(IGeoStrokeStyle initialStyle)
            throws EMP_Exception;

    /**
     * This methods places the map into freehand draw mode if no exception is raised. The user can use
     * drag gestures to draw lines on the map.<br/>
     *<br/>
     * When entering freehand mode:<br/>
     * 1) - The map is locked in smart lock mode.<br/>
     * 2) - A MAP_ENTERED_FREEHAND_DRAW_MODE event is generated.<br/>
     * 3) - At the first drag gesture a MAP_FREEHAND_LINE_DRAW_START is generated when the first N
     * points (N > 1) have been drawn. As the drawing takes place a line with the specified style is
     * displayed on the map.<br/>
     * 4) - A MAP_FREEHAND_LINE_DRAW_UPDATE event is generated as the user continues the drag gesture.<br/>
     * 5) - When the user ends the drag gesture (lifts the finger or stylist), a MAP_FREEHAND_LINE_DRAW_END
     * event is generated. The event shall contain a list of geospatial coordinates of the line drawn.
     * Once the event return the line is removed from the map, and the list of coordinates is deleted.<br/>
     * 6) - The user may choose to initiate a new drag gesture to generate a new line therefore returning
     * to step # 3.<br/>
     * 7) - When drawComplete is called a MAP_EXIT_FREEHAND_DRAW_MODE is generated, the map is unlocked,
     * 8) - EditorMode changes to INACTIVE.
     * and the map exits freehand draw mode.<br/>
     * @param initialStyle This parameters contains the initial style to use for the draw. A value of null causes an EMP_Exception.
     * @param listener This parameters is an event listener to handle the freehand draw events.
     * @throws EMP_Exception if the map does not support freehand drawing, or there is a draw or edit in progress,
     *     map is MapMotionLockMode is LOCKED or SMART_LOCK.
     */
    void drawFreehand(IGeoStrokeStyle initialStyle, IFreehandEventListener listener)
            throws EMP_Exception;

    /**
     * This method changes the style used by the freehand operation.
     * @param style A value of null has no effect.
     * @throws EMP_Exception if the map is not in freehand drawing.
     */
    void setFreehandStyle(IGeoStrokeStyle style)
            throws EMP_Exception;

    /**
     * This method causes the map to exit freehand draw mode and EditorMode changes to INACTIVE. If the map is NOT
     * in draw mode the call has no effect.
     * @throws EMP_Exception if the map is not in freehand drawing.
     */
    void drawFreehandExit()
            throws EMP_Exception;

    /**
     * Returns the draw/edit state of the map
     * @return
     * @throws EMP_Exception if draw edit state cannot be retrieved
     */
    EditorMode getEditorMode() throws EMP_Exception;

    /**
     * This methods centers the map with the features location at the center.
     * This call has no effect if feature is null or has no coordinates.
     * @param feature The feature to center on.
     * @param animate If set tto true then map will animate to the new position
     */
    void zoomTo(IFeature feature, boolean animate);

    /**
     * This method calculates the bounding box that contains all the features listed
     * and sets the maps view area to contain the bounding box. (NOTE: a 3D map
     * may not be able to show all the features listed.)
     * @param featureList
     * @param animate If set tto true then map will animate to the new position
     */
    void zoomTo(List<IFeature> featureList, boolean animate);

    /**
     * This method calculates the bounding box of the content of the overlay and sets
     * the maps viewing area to contain the bounding box. The viewing area is not changed if the
     * overlay is empty or not on the map.
     * @param overlay
     * @param animate If set tto true then map will animate to the new position
     */
    void zoomTo(IOverlay  overlay, boolean animate);

    /**
     * This method sets the maps viewing area to the coordinates contained in the bounding box provided.
     * @param bounds
     * @param animate If set tto true then map will animate to the new position
     */
    void setBounds(IGeoBounds bounds, boolean animate);

    /**
     * This method gets the map's viewing area.
     */

    IGeoBounds getBounds();
    /**
     * This method set the map's motion lock. It will throw an exception if any one of the following conditions is true
     * Map is already in one of the supported lock modes (LOCK or SMART_LOCK)
     * EditorMode is not INACTIVE.
     * @param mode see {@link MapMotionLockEnum}
     */
    void setMotionLockMode(MapMotionLockEnum mode) throws EMP_Exception;

    /**
     * Returns current map motion lock mode.
     * @return current map motion mode. see {@link MapMotionLockEnum}
     */
    MapMotionLockEnum getMotionLockMode() throws EMP_Exception;

    /**
     * This method sets the threshold of the distances beyond which the map displays MilStd single point
     * icons as dots.
     * Current default is 30000.0 meters.
     * @param dValue Distance in meters.
     */
    void setFarDistanceThreshold(double dValue);

    /**
     * This method retrieves the far distance threshold set on the map.
     * @return The distance in meters.
     */
    double getFarDistanceThreshold();

    /**
     * This method sets the threshold of the distances beyond which the map displays MilStd single point
     * icons with no modifiers. Icons at distances less than this threshold get displayed as fully qualified icons
     * as per the label setting on the map.
     * Note that this is simply a hint for the system. If EMP core components detect a high level of memory usage
     * this value will be adjusted in the hope of avoiding OutofMemoryError. When memory usage returns to normal an
     * attempt is made to restore the threshold to user set value
     * Current default is 10000.0 meters.
     * @param dValue Distance in meters.
     */
    void setMidDistanceThreshold(double dValue);

    /**
     * This method retrieves the mid distance threshold set by setMidDistanceThreshold. It is not necessarily the value that is
     * actually in effect. Please see notes on setMidDistanceThreshold.
     * @return The distance in meters.
     */
    double getMidDistanceThreshold();

    /**
     * geoToScreen - convert from GeoPosition to screen coordinates
     * @param pos
     * @return a screen X,Y coordinate for the provided GeoPosition.*/

    android.graphics.Point geoToScreen(IGeoPosition pos) throws EMP_Exception;

    /**
     * screenToGeo - converts from screen coordinates to GeoPosition
     * @param point
     * @return a GeoPosition coordinate for the provided screen X,Y coordinate.
     */

    IGeoPosition screenToGeo(android.graphics.Point point) throws EMP_Exception;

    /**
     * geoToContainer - convert from GeoPosition to container coordinates
     * @param pos
     * @return a container X,Y coordinate for the provided GeoPosition.
     */

    Point geoToContainer(IGeoPosition pos) throws EMP_Exception;

    /**
     * containerToGeo - converts from container coordinates to GeoPosition
     * @param point
     * @return a GeoPosition coordinate for the provided container X,Y coordinate.
     */

    IGeoPosition containerToGeo(Point point) throws EMP_Exception;

    /**
     * This method selects the feature on the map. If the feature is already selected, or not on the map no action is taken.
     * @param feature The feature to select. NULL feature is ignored.
     */
    void selectFeature(IFeature feature);

    /**
     * This method selects the features listed. If a feature is already selected or not on the map no action is taken.
     * @param features The list of features to select. NULL features are ignored.
     */
    void selectFeatures(List<IFeature> features);

    /**
     * This method deselects the feature on the map. If the feature is not selected or not on the map no action is taken.
     * @param feature The feature to deselected. NULL feature is ignored.
     */
    void deselectFeature(IFeature feature);

    /**
     * This method deselects the features on the map. If any feature is not selected or not on the map no action is taken.
     * @param features The features to deselected. NULL features are ignored.
     */
    void deselectFeatures(List<IFeature> features);

    /**
     * This method retrieves the list of feature that are marked selected on the map.
     * @return A list of IFeatures. If there are no features selected the list is empty.
     */
    List<IFeature> getSelected();

    /**
     * This method clears the map selected list.
     */
    void clearSelected();

    /**
     * This method check if the feature is selected on the map.
     * @param feature
     * @return True if it is selected false otherwise.
     */
    boolean isSelected(IFeature feature);

    /**
     * This method retrieves the font size modifier setting for the map.
     * @return {@link FontSizeModifierEnum}
     */
    FontSizeModifierEnum getFontSizeModifier();

    /**
     * This method is used to apply a scale factor to the font size of all Text and Tactical Graphics features when rendered on the map.
     * The default setting is {@link FontSizeModifierEnum#NORMAL}. All features
     * affected get re-rendered with the font size increased or decreased according to this setting.
     * @param value {@link FontSizeModifierEnum}
     */
    void setFontSizeModifier(FontSizeModifierEnum value) throws EMP_Exception;

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

    /**
     * This method creates a mini map associated with the map. The android View containing the map is
     * returned to the client. It is the responsibility of the client to add the view to a parent.
     * All touch events are forwarded to the parent of the View. If the mini map has already been
     * created the same view is returned.
     * @return android.view.View
     */
    View showMiniMap();

    /**
     * This method destroys the mini map. The call will remove the view from its parent if it still
     * has a parent. If the mini map has already been destroyed no action is taken.
     */
    void hideMiniMap();

    /**
     * This method set the grid type displayed on the map.
     * @param grid {@link MapGridTypeEnum}
     */
    void setGridType(MapGridTypeEnum grid);
}
