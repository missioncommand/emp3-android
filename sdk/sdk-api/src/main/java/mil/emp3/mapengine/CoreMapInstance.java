package mil.emp3.mapengine;

import android.graphics.Point;
import android.util.Log;
import android.view.View;

import org.cmapi.primitives.IGeoPosition;

import java.lang.reflect.Field;
import java.util.EnumSet;
import java.util.List;

import mil.emp3.api.enums.FeatureTypeEnum;
import mil.emp3.api.enums.MapFeatureEventEnum;
import mil.emp3.api.enums.MapStateEnum;
import mil.emp3.api.enums.UserInteractionEventEnum;
import mil.emp3.api.enums.UserInteractionKeyEnum;
import mil.emp3.api.enums.UserInteractionMouseButtonEnum;
import mil.emp3.api.enums.WMSVersionEnum;

import mil.emp3.api.exceptions.EMP_Exception;
import mil.emp3.api.interfaces.IFeature;

import mil.emp3.mapengine.api.Capabilities;

import mil.emp3.mapengine.events.MapInstanceFeatureAddedEvent;
import mil.emp3.mapengine.events.MapInstanceFeatureRemovedEvent;
import mil.emp3.mapengine.events.MapInstanceFeatureUserInteractionEvent;
import mil.emp3.mapengine.events.MapInstanceStateChangeEvent;
import mil.emp3.mapengine.events.MapInstanceUserInteractionEvent;
import mil.emp3.mapengine.events.MapInstanceViewChangeEvent;
import mil.emp3.mapengine.interfaces.IEmpImageInfo;

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
 * A COMPILATION FAILURE FIRST. AT THAT MOMENT YOU MAY DECIDE TO ADD A STUB METHOD IF YOU SEE FIT. IF YOU DO ADD A STUB METHOD MAKE SURE
 * YOU CREATE A Log.wft clause, SO THAT YOU WOULD KNOW THAT UNIMPLEMENTED METHOD IS BEING CALLED.
 *
 * ADDITIONAL COMMON FUNCTIONALITY CAN BE PROMOTED BY USE OF GENERICS BUT WE WILL DO THAT IN A FOLLOW UP TICKET
 *
 * AS A RESULT OF THIS REFACTORING, class mil.emp3.worldwind.MapInstance, had to be modified to ot extend gov.nasa.worldwind.WorldWindow. This caused
 * changes to Wall Paper Service.
 */
public abstract class CoreMapInstance implements IMapInstance {
    protected IMilStdRenderer oMilStdRenderer = null;

    private MapInstanceFeatureUserInteractionEventListener featureUserInteractionEventListener;
    private MapInstanceStateChangeEventListener stateChangeEventListener;
    private MapInstanceUserInteractionEventListener userInteractionEventListener;
    private MapInstanceViewChangeEventListener viewChangeEventListener;
    private MapInstanceFeatureAddedEventListener featureAddedEventListener;
    private MapInstanceFeatureRemovedEventListener featureRemovedEventListener;

    protected final IEmpResources empResources;
    protected final String TAG;

    protected Capabilities oEngineCapabilities;

    protected CoreMapInstance(String TAG, IEmpResources resources) {
        this.empResources = resources;
        this.TAG = TAG;
    }

    public void generateStateChangeEvent(MapStateEnum eNewState) {
        if(null != this.stateChangeEventListener) {
            MapInstanceStateChangeEvent event = new MapInstanceStateChangeEvent(this, eNewState);
            stateChangeEventListener.onEvent(event);
            Log.d(TAG, "Worldwind state change: " + eNewState);
        }
    }

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

    protected void generateFeatureRemovedEvent(IFeature feature) {
        if(null != featureRemovedEventListener) {
            featureRemovedEventListener.onEvent(new MapInstanceFeatureRemovedEvent(this, MapFeatureEventEnum.MAP_FEATURE_REMOVED, feature));
        }
    }
    protected void generateFeatureAddedEvent(IFeature feature) {
        if(null != featureAddedEventListener) {
            featureAddedEventListener.onEvent(new MapInstanceFeatureAddedEvent(this, MapFeatureEventEnum.MAP_FEATURE_ADDED, feature));
        }
    }
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
        int [] loc = new int[2];
        this.getMapInstanceAndroidView().getLocationOnScreen(loc);
        point.x += loc[0];
        point.y += loc[1];
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
}
