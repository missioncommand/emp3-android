package mil.emp3.mapengine.abstracts;

import android.graphics.Point;
import android.util.Log;

import org.cmapi.primitives.IGeoPosition;

import java.lang.reflect.Field;
import java.util.EnumSet;
import java.util.List;

import mil.emp3.api.enums.FontSizeModifierEnum;
import mil.emp3.api.enums.MapFeatureEventEnum;
import mil.emp3.api.enums.MapStateEnum;
import mil.emp3.api.enums.UserInteractionEventEnum;
import mil.emp3.api.enums.UserInteractionKeyEnum;
import mil.emp3.api.enums.UserInteractionMouseButtonEnum;
import mil.emp3.api.interfaces.IFeature;
import mil.emp3.api.interfaces.IScreenCaptureCallback;
import mil.emp3.mapengine.api.Capabilities;
import mil.emp3.mapengine.events.MapInstanceFeatureAddedEvent;
import mil.emp3.mapengine.events.MapInstanceFeatureRemovedEvent;
import mil.emp3.mapengine.events.MapInstanceFeatureUserInteractionEvent;
import mil.emp3.mapengine.events.MapInstanceStateChangeEvent;
import mil.emp3.mapengine.events.MapInstanceUserInteractionEvent;
import mil.emp3.mapengine.events.MapInstanceViewChangeEvent;
import mil.emp3.mapengine.interfaces.IEmpResources;
import mil.emp3.mapengine.interfaces.IMapEngineCapabilities;
import mil.emp3.mapengine.interfaces.IMapInstance;
import mil.emp3.mapengine.interfaces.IMilStdRenderer;
import mil.emp3.mapengine.listeners.MapInstanceFeatureAddedEventListener;
import mil.emp3.mapengine.listeners.MapInstanceFeatureRemovedEventListener;
import mil.emp3.mapengine.listeners.MapInstanceFeatureUserInteractionEventListener;
import mil.emp3.mapengine.listeners.MapInstanceStateChangeEventListener;
import mil.emp3.mapengine.listeners.MapInstanceUserInteractionEventListener;
import mil.emp3.mapengine.listeners.MapInstanceViewChangeEventListener;

/**
 * This class implements functionality that is common to all MapInstance classes that implement IMapInstance, currently
 * known MapInstance classes are
 *
 * mil.emp3.worldwind.MapInstance
 * mil.emp3.arcgis.MapInstance
 * mil.emp3.openstreet
 * mil.emp3.api.mock.MockMapInstance
 * mil.emp3.api.RemoteMap.NoMap
 *
 * THIS CLASS SHOULD NOT BE USED TO CREATE STUB IMPLEMENTATIONS OF ALL THE METHODS IN IMAPINSTANCE. WHEN IMAPINSTANCE IS UPDATED WE WANT TO DETECT
 * A COMPILATION FAILURE FIRST. AT THAT MOMENT YOU MAY DECIDE TO ADD A STUB METHOD IF YOU SEE IT FIT. IF YOU DO ADD A STUB METHOD MAKE SURE
 * YOU CREATE A Log.wft clause, SO THAT YOU WOULD KNOW THAT UNIMPLEMENTED METHOD IS BEING CALLED.
 *
 * ADDITIONAL COMMON FUNCTIONALITY CAN BE PROMOTED BY USE OF GENERICS BUT WE WILL DO THAT IN A FOLLOW UP TICKET
 *
 * AS A RESULT OF THIS REFACTORING, class mil.emp3.worldwind.MapInstance, had to be modified to extend gov.nasa.worldwind.WorldWindow. This caused
 * changes to Wall Paper Service.
 */
public abstract class CoreMapInstance implements IMapInstance {
    protected IMilStdRenderer oMilStdRenderer = null;

    //
    // EMP3 Core will install these listeners MapInstance is created.
    //
    private MapInstanceFeatureUserInteractionEventListener featureUserInteractionEventListener;
    private MapInstanceStateChangeEventListener stateChangeEventListener;
    private MapInstanceUserInteractionEventListener userInteractionEventListener;
    private MapInstanceViewChangeEventListener viewChangeEventListener;
    private MapInstanceFeatureAddedEventListener featureAddedEventListener;
    private MapInstanceFeatureRemovedEventListener featureRemovedEventListener;

    private final String TAG;

    //
    // EMP Core registers an handle to the resources that will be required by the MapInstance.
    //
    private final IEmpResources empResources;

    /**
     * Capabilities supported by the MapEngine such as which shapes can be rendered.
     */
    protected Capabilities oEngineCapabilities;

    /**
     * The fontSizeModifier indicates the amount the font size of all text should be modified by.
     */
    protected FontSizeModifierEnum fontSizeModifier = FontSizeModifierEnum.NORMAL;

    /**
     *
     * @param TAG
     * @param empResources
     */
    protected CoreMapInstance(String TAG, IEmpResources empResources) {
        this.TAG = TAG;
        this.empResources = empResources;
    }

    /**
     * Returns previously registered instance of EmpResources
     * @return
     */
    public IEmpResources getEmpResources() {
        return empResources;
    }

    @Override
    public FontSizeModifierEnum getFontSizeModifier() {
        return fontSizeModifier;
    }

    /**
     * Builds a Map State Change event and posts it to the listener.
     * @param eNewState
     */
    public void generateStateChangeEvent(MapStateEnum eNewState) {
        if(null != this.stateChangeEventListener) {
            MapInstanceStateChangeEvent event = new MapInstanceStateChangeEvent(this, eNewState);
            stateChangeEventListener.onEvent(event);
            Log.d(TAG, "Worldwind state change: " + eNewState);
        }
    }

    /**
     * Builds a Feature User Interaction event and posts it to the listener.
     * @param eEvent
     * @param keys
     * @param eButton
     * @param oFeatureList
     * @param oPointCoordinate
     * @param oPosition
     * @param oStartPosition
     * @return
     */
    public boolean generateFeatureUserInteractionEvent(UserInteractionEventEnum eEvent,
                                                    EnumSet<UserInteractionKeyEnum> keys,
                                                    UserInteractionMouseButtonEnum eButton,
                                                    java.util.List oFeatureList,
                                                    android.graphics.Point oPointCoordinate,
                                                    IGeoPosition oPosition,
                                                    IGeoPosition oStartPosition) {
        if (null != this.featureUserInteractionEventListener) {
            MapInstanceFeatureUserInteractionEvent oEvent = new MapInstanceFeatureUserInteractionEvent(this,
                    eEvent, keys, eButton, oFeatureList, oPointCoordinate, oPosition, oStartPosition);
            this.featureUserInteractionEventListener.onEvent(oEvent);
            return oEvent.isEventConsumed();
        }
        return false;
    }

    /**
     * Builds a Feature User Interaction event and posts it to the listener.
     * @param eEvent
     * @param keys
     * @param eButton
     * @param oFeatureList
     * @param oPointCoordinate
     * @param oPosition
     * @param oStartPosition
     * @return
     */
    public boolean generateFeatureUserInteractionEvent(UserInteractionEventEnum eEvent,
                                                       EnumSet<UserInteractionKeyEnum> keys,
                                                       UserInteractionMouseButtonEnum eButton,
                                                       java.util.List oFeatureList,
                                                       android.graphics.PointF oPointCoordinate,
                                                       IGeoPosition oPosition,
                                                       IGeoPosition oStartPosition) {
        if (null != this.featureUserInteractionEventListener) {
            MapInstanceFeatureUserInteractionEvent oEvent = new MapInstanceFeatureUserInteractionEvent(this,
                    eEvent, keys, eButton, oFeatureList, oPointCoordinate, oPosition, oStartPosition);
            this.featureUserInteractionEventListener.onEvent(oEvent);
            return oEvent.isEventConsumed();
        }
        return false;
    }

    /**
     * Builds a Map User Interaction Event and posts it to the listener.
     *
     * Deprecated: Use constructor with PointF coordinate for more precise values
     *
     * @param eEvent
     * @param keys
     * @param eButton
     * @param oPointCoordinate
     * @param oPosition
     * @param oStartPosition
     * @return
     */
    @Deprecated
    public boolean generateMapUserInteractionEvent(UserInteractionEventEnum eEvent,
                                                EnumSet<UserInteractionKeyEnum> keys,
                                                UserInteractionMouseButtonEnum eButton,
                                                android.graphics.Point oPointCoordinate,
                                                IGeoPosition oPosition,
                                                IGeoPosition oStartPosition) {
        if (null != this.userInteractionEventListener) {
            MapInstanceUserInteractionEvent oEvent = new MapInstanceUserInteractionEvent(this,
                    eEvent, keys, eButton, oPointCoordinate, oPosition, oStartPosition);
            this.userInteractionEventListener.onEvent(oEvent);
            return oEvent.isEventConsumed();
        }
        return false;
    }

    /**
     * Builds a Map User Interaction Event and posts it to the listener.
     * @param eEvent
     * @param keys
     * @param eButton
     * @param oPointCoordinate
     * @param oPosition
     * @param oStartPosition
     * @return
     */
    public boolean generateMapUserInteractionEvent(UserInteractionEventEnum eEvent,
                                                   EnumSet<UserInteractionKeyEnum> keys,
                                                   UserInteractionMouseButtonEnum eButton,
                                                   android.graphics.PointF oPointCoordinate,
                                                   IGeoPosition oPosition,
                                                   IGeoPosition oStartPosition) {
        if (null != this.userInteractionEventListener) {
            MapInstanceUserInteractionEvent oEvent = new MapInstanceUserInteractionEvent(this,
                    eEvent, keys, eButton, oPointCoordinate, oPosition, oStartPosition);
            this.userInteractionEventListener.onEvent(oEvent);
            return oEvent.isEventConsumed();
        }
        return false;
    }

    /**
     * Builds a feature removed event and posts it to the listener.
     * @param feature
     */
    protected void generateFeatureRemovedEvent(IFeature feature, Object userContext) {
        if(null != featureRemovedEventListener) {
            MapInstanceFeatureRemovedEvent event = new MapInstanceFeatureRemovedEvent(this, MapFeatureEventEnum.MAP_FEATURE_REMOVED, feature);
            event.setUserObject(userContext);
            featureRemovedEventListener.onEvent(event);
        }
    }

    /**
     * Builds a feature added event and posts it to the listener.
     * @param feature
     */
    protected void generateFeatureAddedEvent(IFeature feature, Object userContext) {
        if(null != featureAddedEventListener) {
            MapInstanceFeatureAddedEvent event = new MapInstanceFeatureAddedEvent(this, MapFeatureEventEnum.MAP_FEATURE_ADDED, feature);
            event.setUserObject(userContext);
            featureAddedEventListener.onEvent(event);
        }
    }

    /**
     * Builds a View Change event and posts it to the listener.
     * @param oEvent
     */
    public void generateViewChangeEvent(MapInstanceViewChangeEvent oEvent) {
        if(null != viewChangeEventListener) {
            viewChangeEventListener.onEvent(oEvent);
        }
    }


    @Override
    public IMapEngineCapabilities getCapabilities() {
        return this.oEngineCapabilities;
    }

    @Override
    public void addMapInstanceFeatureUserInteractionEventListener(MapInstanceFeatureUserInteractionEventListener mapInstanceFeatureUserInteractionEventListener) {
        this.featureUserInteractionEventListener = mapInstanceFeatureUserInteractionEventListener;
    }

    @Override
    public void addMapInstanceStateChangeEventListener(MapInstanceStateChangeEventListener mapInstanceStateChangeEventListener) {
        this.stateChangeEventListener = mapInstanceStateChangeEventListener;
    }

    @Override
    public void addMapInstanceUserInteractionEventListener(MapInstanceUserInteractionEventListener mapInstanceUserInteractionEventListener) {
        this.userInteractionEventListener = mapInstanceUserInteractionEventListener;
    }

    @Override
    public void addMapInstanceViewChangeEventListener(MapInstanceViewChangeEventListener mapInstanceViewChangeEventListener) {
        this.viewChangeEventListener = mapInstanceViewChangeEventListener;
    }

    @Override
    public void addMapInstanceFeatureAddedEventListener(MapInstanceFeatureAddedEventListener featureAddedEventListener) {
        this.featureAddedEventListener = featureAddedEventListener;
    }

    @Override
    public void addMapInstanceFeatureRemovedEventListener(MapInstanceFeatureRemovedEventListener featureRemovedEventListener) {
        this.featureRemovedEventListener = featureRemovedEventListener;
    }

    @Override
    public void registerMilStdRenderer(IMilStdRenderer oRenderer) {
        this.oMilStdRenderer = oRenderer;
    }

    /**
     * Gets a reference to the military standard symbol renderer
     * @return renderer
     */
    public IMilStdRenderer getMilStdRenderer() {
        return oMilStdRenderer;
    }

    /**
     * Fetch Version information from BuildConfig
     * @param builder
     * @param buildConfigFields
     */

    public void getVersionInformation(StringBuilder builder, List<String> buildConfigFields, String buildConfigClazzName) {
        try {
            Class<?> clazz = this.getClass().getClassLoader().loadClass(buildConfigClazzName);
            Field[] declaredFields = clazz.getDeclaredFields();
            for (Field field : declaredFields) {
                if (java.lang.reflect.Modifier.isStatic(field.getModifiers())) {
                    if(buildConfigFields.contains(field.getName())) {
                        try {
                            builder.append("\n\t\t" + (String)field.get(null));
                        } catch (IllegalAccessException e) {
                            Log.e(TAG, "getVersionInformation " + field.getName(), e);
                        }
                    }
                }
            }
        } catch (ClassNotFoundException e) {
            Log.e(TAG, "getVersionInformation " + buildConfigClazzName + " not available");
        }
    }

    @Override
    public Point geoToScreen(IGeoPosition pos){
        Point point = this.geoToContainer(pos);
        if (null != point) {
            int[] loc = new int[2];
            this.getMapInstanceAndroidView().getLocationOnScreen(loc);
            point.x += loc[0];
            point.y += loc[1];
        }
        return point;
    }

    @Override
    public IGeoPosition screenToGeo(Point point){
        Point relPoint = new Point();
        int [] loc = new int[2];
        this.getMapInstanceAndroidView().getLocationOnScreen(loc);
        relPoint.x = point.x - loc[0];
        relPoint.y = point.y - loc[1];
        return this.containerToGeo(relPoint);
    }

    @Override
    public void getCapture(IScreenCaptureCallback callback) {
        throw new UnsupportedOperationException("This map engine does not support this operation.");
    }
}
