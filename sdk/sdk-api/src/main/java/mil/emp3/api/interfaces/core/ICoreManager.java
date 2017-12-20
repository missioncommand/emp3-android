package mil.emp3.api.interfaces.core;

import android.graphics.Point;
import android.graphics.PointF;

import org.cmapi.primitives.IGeoBounds;
import org.cmapi.primitives.IGeoMilSymbol;
import org.cmapi.primitives.IGeoPosition;
import org.cmapi.primitives.IGeoStrokeStyle;

import java.util.List;

import mil.emp3.api.enums.EditorMode;
import mil.emp3.api.enums.MapGridTypeEnum;
import mil.emp3.api.enums.MapMotionLockEnum;
import mil.emp3.api.enums.MapStateEnum;
import mil.emp3.api.enums.MilStdLabelSettingEnum;
import mil.emp3.api.exceptions.EMP_Exception;
import mil.emp3.api.interfaces.ICamera;
import mil.emp3.api.interfaces.IFeature;
import mil.emp3.api.interfaces.ILookAt;
import mil.emp3.api.interfaces.IMap;
import mil.emp3.api.interfaces.IOverlay;
import mil.emp3.api.listeners.IDrawEventListener;
import mil.emp3.api.listeners.IEditEventListener;
import mil.emp3.api.listeners.IFreehandEventListener;

/*
 * This is an internal interface class.  The app developer must not implement this interface.
 */

public interface ICoreManager {
    void setStorageManager(IStorageManager storageManager);
    void setEventManager(IEventManager eventManager);

    MapStateEnum getState(IMap clientMap);

    void setCamera(IMap clientMap, ICamera camera, boolean animate, Object userContext) throws EMP_Exception;
    ICamera getCamera(IMap clientMap);
    void processCameraSettingChange(ICamera camera, boolean animate, Object userContext);

    void setLookAt(IMap clientMap, ILookAt lookAt, boolean animate, Object userContext) throws EMP_Exception;
    ILookAt getLookAt(IMap clientMap);
    void processLookAtSettingChange(ILookAt lookAt, boolean animate, Object userContext);

    void setMotionLockMode(IMap clientMap, MapMotionLockEnum mode) throws EMP_Exception;
    MapMotionLockEnum getMotionLockMode(IMap clientMap) throws EMP_Exception;

    EditorMode getEditorMode(IMap clientMap) throws EMP_Exception;
    void editFeature(IMap clientMap, IFeature oFeature, IEditEventListener listener) throws EMP_Exception;
    void editCancel(IMap clientMap) throws EMP_Exception;
    void editComplete(IMap clientMap) throws EMP_Exception;

    void drawFeature(IMap clientMap, IFeature oFeature, IDrawEventListener listener) throws EMP_Exception;
    void drawCancel(IMap clientMap) throws EMP_Exception;
    void drawComplete(IMap clientMap) throws EMP_Exception;

    void zoomTo(IMap clientMap, List<IFeature> featureList, boolean animate);
    void zoomTo(IMap clientMap, IOverlay overlay, boolean animate);

    void setBounds(IMap clientMap, IGeoBounds bounds, boolean animate);
    IGeoBounds getBounds(IMap clientMap);

    void drawFreehand(IMap clientMap, IGeoStrokeStyle initialStyle, IFreehandEventListener listener) throws EMP_Exception;
    void setFreehandStyle(IMap clientMap, IGeoStrokeStyle style) throws EMP_Exception;
    void drawFreehandExit(IMap clientMap) throws EMP_Exception;

    /**
     * geoToScreen - convert from GeoPosition to screen coordinates
     * @param clientMap
     * @param pos
     * @return a screen X,Y coordinate for the provided GeoPosition.*/

    Point geoToScreen(IMap clientMap, IGeoPosition pos) throws EMP_Exception;

    /**
     * screenToGeo - converts from screen coordinates to GeoPosition
     * @param clientMap
     * @param point
     * @return a GeoPosition coordinate for the provided screen X,Y coordinate.
     */

    IGeoPosition screenToGeo(IMap clientMap, android.graphics.Point point) throws EMP_Exception;

    /**
     * geoToContainer - convert from GeoPosition to container coordinates
     * @param clientMap
     * @param pos
     * @return a container X,Y coordinate for the provided GeoPosition.
     */

    Point geoToContainer(IMap clientMap, IGeoPosition pos) throws EMP_Exception;

    /**
     * containerToGeo - converts from container coordinates to GeoPosition
     *
     * Deprecated: Use containerToGeo using a PointF instead for more precise
     * calculations
     * @param clientMap
     * @param point
     * @return a GeoPosition coordinate for the provided container X,Y coordinate.
     */
    @Deprecated
    IGeoPosition containerToGeo(IMap clientMap, Point point) throws EMP_Exception;

    /**
     * containerToGeo - converts from container coordinates to GeoPosition
     * @param clientMap
     * @param point
     * @return a GeoPosition coordinate for the provided container X,Y coordinate.
     */
    IGeoPosition containerToGeo(IMap clientMap, PointF point) throws EMP_Exception;

    /**
     * This method set the specified map grid on the map specified.
     * @param clientMap
     * @param grid
     */
    void setMapGridType(IMap clientMap, MapGridTypeEnum grid);

    /**
     * This method returns the list of labels to be processed given the label setting.
     * @param eLabelSetting {@link MilStdLabelSettingEnum}
     * @return java.util.Set<IGeoMilSymbol.Modifier>
     */
    java.util.Set<IGeoMilSymbol.Modifier> getMilStdModifierLabelList(MilStdLabelSettingEnum eLabelSetting);
}
