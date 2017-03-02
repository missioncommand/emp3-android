package mil.emp3.core.storage;

import android.content.res.Resources;
import android.util.DisplayMetrics;
import android.util.Log;

import org.cmapi.primitives.GeoFillStyle;
import org.cmapi.primitives.GeoLabelStyle;
import org.cmapi.primitives.GeoPosition;
import org.cmapi.primitives.GeoStrokeStyle;
import org.cmapi.primitives.IGeoBounds;
import org.cmapi.primitives.IGeoColor;
import org.cmapi.primitives.IGeoFillStyle;
import org.cmapi.primitives.IGeoLabelStyle;
import org.cmapi.primitives.IGeoPosition;
import org.cmapi.primitives.IGeoStrokeStyle;

import java.util.ArrayList;
import java.util.List;

import mil.emp3.api.Camera;
import mil.emp3.api.MilStdSymbol;
import mil.emp3.api.enums.EditorMode;
import mil.emp3.api.enums.FontSizeModifierEnum;
import mil.emp3.api.enums.IconSizeEnum;
import mil.emp3.api.enums.MapMotionLockEnum;
import mil.emp3.api.enums.MapStateEnum;
import mil.emp3.api.enums.MilStdLabelSettingEnum;
import mil.emp3.api.exceptions.EMP_Exception;
import mil.emp3.api.interfaces.ICamera;
import mil.emp3.api.interfaces.IFeature;
import mil.emp3.api.interfaces.ILookAt;
import mil.emp3.api.interfaces.IMap;
import mil.emp3.api.interfaces.IMapService;
import mil.emp3.api.interfaces.core.storage.IClientMapToMapInstance;
import mil.emp3.api.interfaces.core.storage.IMapStatus;
import mil.emp3.api.listeners.IDrawEventListener;
import mil.emp3.api.listeners.IEditEventListener;
import mil.emp3.api.listeners.IFreehandEventListener;
import mil.emp3.api.utils.EmpGeoColor;
import mil.emp3.core.editors.AbstractEditor;
import mil.emp3.core.editors.CircleEditor;
import mil.emp3.core.editors.EllipseEditor;
import mil.emp3.core.editors.FreehandDrawEditor;
import mil.emp3.core.editors.PathEditor;
import mil.emp3.core.editors.PointEditor;
import mil.emp3.core.editors.PolygonEditor;
import mil.emp3.core.editors.RectangleEditor;
import mil.emp3.core.editors.SquareEditor;
import mil.emp3.core.editors.TextEditor;
import mil.emp3.core.editors.milstd.MilStdSinglePointEditor;
import mil.emp3.core.utils.CoreMilStdUtilities;
import mil.emp3.mapengine.interfaces.IMapEngineCapabilities;
import mil.emp3.mapengine.interfaces.IMapInstance;

/**
 * This class contains the status and properties of the map instance.
 */
public abstract class MapStatus implements IMapStatus {

    private static String TAG = MapStatus.class.getSimpleName();

    // The value is the current lock mode set on th map.
    private MapMotionLockEnum eLockMode = MapMotionLockEnum.UNLOCKED;
    // This value is the map's current state.
    private MapStateEnum stateEnum = MapStateEnum.MAP_NEW;
    // This value is the map's current camera.
    private ICamera currentCamera = new Camera();
    // This value is the map's lookAt. This must be initialized from MapInstance
    private ILookAt currentLookAt;
    // This value is the base size of icons on the map.
    private int iIconPixelSize;
    // This value is the icon size setting.
    private IconSizeEnum eIconSize;
    //This value is the current MilStd label setting.
    private MilStdLabelSettingEnum eLabelSetting = MilStdLabelSettingEnum.COMMON_LABELS;
    // This hash map contains all the map services the map is currently handling.
    private final java.util.HashMap<java.util.UUID, IMapService> mapServiceHash;
    // This value is the map's current width in pixels.
    private int iMapViewWidth = 0;
    // this value is the map's current height in pixels.
    private int iMapViewHeight = 0;
    // This value contains the map's capabilities.
    protected IMapEngineCapabilities oMapCapabilities;
    // If the map is in Edit/Draw mode, this value contains a reference to the editor.
    private AbstractEditor oEditor = null;
    private IGeoBounds oBounds = null;

    private static double MAX_FAR_DISTANCE_THRESHOLD = 50000.0;
    private static double MAX_MID_DISTANCE_THRESHOLD = 30000.0;

    // This value is the map's far distance threshold.
    private double dFarDistanceThreshold = 30000.0;
    // This value is the map's mid distance threshold.
    private double dMidDistanceThreshold = 10000.0;

    // This is a temp object used to avoid periodic memory allocations.
    private IGeoPosition oTempPosition = new GeoPosition();

    // This is the select style.
    private final IGeoStrokeStyle selectedStrokeStyle = new GeoStrokeStyle();
    private final IGeoLabelStyle selectedLabelStyle = new GeoLabelStyle();
    private double selectedIconScale = 1.2;

    private final IGeoFillStyle bufferFillStyle = new GeoFillStyle();

    private FontSizeModifierEnum fontSizeModifier = FontSizeModifierEnum.NORMAL;

    protected MapStatus() {
        this.mapServiceHash = new java.util.HashMap<>();
        this.setIconSize(IconSizeEnum.SMALL);
        IGeoColor color = new EmpGeoColor(1.0, 255, 255, 0);
        this.selectedStrokeStyle.setStrokeColor(color);
        this.selectedStrokeStyle.setStrokeWidth(3.0);
        this.selectedLabelStyle.setColor(color);

        this.bufferFillStyle.setFillColor(new EmpGeoColor(0.5, 255, 255, 0));
    }

    protected AbstractEditor getEditor() {
        return this.oEditor;
    }

    @Override
    public boolean isEditing() {
        return (this.oEditor != null);
    }

    public abstract IMap getClientMap();

    public abstract IMapInstance getMapInstance();

    public void copy(IClientMapToMapInstance from) {
        this.eIconSize = from.getIconSize();
        this.eLabelSetting = from.getMilStdLabels();
        this.selectedStrokeStyle.setStipplingPattern(from.getSelectStrokeStyle().getStipplingPattern());
        this.selectedStrokeStyle.setStipplingFactor(from.getSelectStrokeStyle().getStipplingFactor());
        this.selectedStrokeStyle.setStrokeWidth(from.getSelectStrokeStyle().getStrokeWidth());
        this.selectedStrokeStyle.setStrokeColor(from.getSelectStrokeStyle().getStrokeColor());
        this.selectedIconScale = from.getSelectIconScale();
        bufferFillStyle.setFillColor(from.getBufferFillStyle().getFillColor());
    }

    public final MapMotionLockEnum getLockMode() {
        return this.eLockMode;
    }

    @Override
    public final EditorMode getEditorMode() {
        if(null == this.oEditor) {
            return EditorMode.INACTIVE;
        }
        return this.oEditor.getEditorMode();
    }

    /**
     * This is for internals use. It sets the mode without any regards to editor mode. Currently it is invoked by FreehandDrawEditor
     * @param eMode
     */
    public void setLockMode_(MapMotionLockEnum eMode) {
        if((MapMotionLockEnum.UNLOCKED != this.eLockMode) && (MapMotionLockEnum.UNLOCKED != eMode)) {
            throw new IllegalStateException("map is already locked current mode " + eLockMode + " requested mode " + eMode);
        }

        Log.i(TAG, "setLockMode to " + eMode);
        this.eLockMode = eMode;
        if (this.getMapInstance() != null) {
            this.getMapInstance().setMotionLockMode(eMode);
        }
    }

    @Override
    public void setLockMode(MapMotionLockEnum eMode) throws EMP_Exception {
        if(null != this.oEditor) {
            throw new EMP_Exception(EMP_Exception.ErrorDetail.OTHER, "setLockMode is not allowed when in Draw/Edit mode");
        }
        if((MapMotionLockEnum.UNLOCKED != this.eLockMode) && (MapMotionLockEnum.UNLOCKED != eMode)) {
            throw new EMP_Exception(EMP_Exception.ErrorDetail.OTHER, "map is already locked current mode " + eLockMode + " requested mode " + eMode);
        }

        setLockMode_(eMode);
    }

    protected void setMapState(MapStateEnum eState) {
        this.stateEnum = eState;
        if (eState == MapStateEnum.MAP_READY) {
            this.oMapCapabilities = this.getMapInstance().getCapabilities();
        }
    }

    @Override
    public MapStateEnum getMapState() {
        return this.stateEnum;
    }

    @Override
    public void setCamera(ICamera camera) {
        this.currentCamera = camera;
    }

    @Override
    public ICamera getCamera() {
        return this.currentCamera;
    }

    @Override
    public void setLookAt(ILookAt lookAt) {
        this.currentLookAt = lookAt;
    }

    @Override
    public ILookAt getLookAt() {
        return this.currentLookAt;
    }

    // Make sure that to start with MapInstance and Mapping in the core have same reference if LookAt object.
    // This object has all 0 values to start with.

    protected void initializeLookAt(ILookAt lookAt) {
        this.currentLookAt = lookAt;
    }
    public final java.util.Collection<IMapService> getWMSList() {
        return this.mapServiceHash.values();
    }

    public void addMapService(IMapService mapService) {
        this.mapServiceHash.put(mapService.getGeoId(), mapService);
    }

    public boolean removeMapService(IMapService mapService) {
        if (this.mapServiceHash.containsKey(mapService.getGeoId())) {
            this.mapServiceHash.remove(mapService.getGeoId());
            return true;
        }

        return false;
    }

    public boolean hasWMS(java.util.UUID id) {
        return this.mapServiceHash.containsKey(id);
    }

    public List<IMapService> getMapServices() {
        List<IMapService> oList = new ArrayList<>();

        for (java.util.UUID id : this.mapServiceHash.keySet()) {
            oList.add(this.mapServiceHash.get(id));
        }

        return oList;
    }

    @Override
    public void setIconSize(IconSizeEnum eSize) {
        this.eIconSize = eSize;

        // Now we set the icon pixel size based on the density.
        // The maps then apply the scale of the size enum.
        DisplayMetrics metrics = Resources.getSystem().getDisplayMetrics();
        this.iIconPixelSize = (int) Math.round((double) metrics.densityDpi * 0.3); // 0.3 of an in
    }

    @Override
    public IconSizeEnum getIconSize() {
        return this.eIconSize;
    }

    @Override
    public int getIconPixelSize() {
        return this.iIconPixelSize;
    }

    @Override
    public void setMilStdLabels(MilStdLabelSettingEnum labelSetting) {
        this.eLabelSetting = labelSetting;
    }

    @Override
    public MilStdLabelSettingEnum getMilStdLabels() {
        return this.eLabelSetting;
    }

    public int getMapViewWidth() {
        return this.iMapViewWidth;
    }

    public void setMapViewWidth(int iWidth) {
        this.iMapViewWidth = iWidth;
    }

    public int getMapViewHeight() {
        return this.iMapViewHeight;
    }

    public void setMapViewHeight(int iHeight) {
        this.iMapViewHeight = iHeight;
    }

    @Override
    public boolean canEdit(IFeature oFeature) {
        // if the map can't plot the feature we can't edit it.
        return this.oMapCapabilities.canPlot(oFeature.getFeatureType());
    }

    @Override
    public boolean canPlot(IFeature oFeature) {
        return this.oMapCapabilities.canPlot(oFeature.getFeatureType());
    }

    @Override
    public void editFeature(IFeature oFeature, IEditEventListener listener) throws EMP_Exception {
        if ((this.oEditor == null) && (MapMotionLockEnum.UNLOCKED == eLockMode)) {
            switch (oFeature.getFeatureType()) {
                case GEO_POINT:
                    this.oEditor = new PointEditor(this.getMapInstance(), (mil.emp3.api.Point) oFeature, listener);
                    break;
                case GEO_MIL_SYMBOL:
                    MilStdSymbol oSymbol = (MilStdSymbol) oFeature;
                    if (oSymbol.isTacticalGraphic()) {
                        this.oEditor = CoreMilStdUtilities.getMultiPointEditor(this.getMapInstance(), (mil.emp3.api.MilStdSymbol) oFeature, listener);
                    } else {
                        this.oEditor = new MilStdSinglePointEditor(this.getMapInstance(), (mil.emp3.api.MilStdSymbol) oFeature, listener);
                    }
                    break;
                case GEO_PATH:
                    this.oEditor = new PathEditor(this.getMapInstance(), (mil.emp3.api.Path) oFeature, listener);
                    break;
                case GEO_POLYGON:
                    this.oEditor = new PolygonEditor(this.getMapInstance(), (mil.emp3.api.Polygon) oFeature, listener);
                    break;
                case GEO_TEXT:
                    this.oEditor = new TextEditor(this.getMapInstance(), (mil.emp3.api.Text) oFeature, listener);
                    break;
                case GEO_RECTANGLE:
                    this.oEditor = new RectangleEditor(this.getMapInstance(), (mil.emp3.api.Rectangle) oFeature, listener);
                    break;
                case GEO_CIRCLE:
                    this.oEditor = new CircleEditor(this.getMapInstance(), (mil.emp3.api.Circle) oFeature, listener);
                    break;
                case GEO_ELLIPSE:
                    this.oEditor = new EllipseEditor(this.getMapInstance(), (mil.emp3.api.Ellipse) oFeature, listener);
                    break;
                case GEO_SQUARE:
                    this.oEditor = new SquareEditor(this.getMapInstance(), (mil.emp3.api.Square) oFeature, listener);
                    break;
                case GEO_ACM:
                    throw new EMP_Exception(EMP_Exception.ErrorDetail.NOT_SUPPORTED, "Not Supported. The current map can not display a feature of this type.");
            }
        } else {
            throw new EMP_Exception(EMP_Exception.ErrorDetail.OTHER, "Map already in edit/draw mode or locked " + eLockMode);
        }
    }

    @Override
    public void editCancel() throws EMP_Exception {
        if (!this.isEditing() || (this.getEditor().getEditorMode() != EditorMode.EDIT_MODE)) {
            throw new EMP_Exception(EMP_Exception.ErrorDetail.INVALID_PARAMETER, "Map not in edit mode.");
        }

        this.getEditor().Cancel();
        this.oEditor = null;
    }

    @Override
    public void editComplete() throws EMP_Exception {
        if (!this.isEditing() || (this.getEditor().getEditorMode() != EditorMode.EDIT_MODE)) {
            throw new EMP_Exception(EMP_Exception.ErrorDetail.INVALID_PARAMETER, "Map not in edit mode.");
        }

        this.getEditor().Complete();
        this.oEditor = null;
    }

    @Override
    public void drawCancel() throws EMP_Exception {
        if (!this.isEditing() || (this.getEditor().getEditorMode() != EditorMode.DRAW_MODE)) {
            throw new EMP_Exception(EMP_Exception.ErrorDetail.INVALID_PARAMETER, "Map not in draw mode.");
        }

        this.getEditor().Cancel();
        this.oEditor = null;
    }

    @Override
    public void drawComplete() throws EMP_Exception {
        if (!this.isEditing() || (this.getEditor().getEditorMode() != EditorMode.DRAW_MODE)) {
            throw new EMP_Exception(EMP_Exception.ErrorDetail.INVALID_PARAMETER, "Map not in draw mode.");
        }

        this.getEditor().Complete();
        this.oEditor = null;
    }

    @Override
    public void freehandComplete() throws EMP_Exception {
        if (!this.isEditing() || (this.getEditor().getEditorMode() != EditorMode.FREEHAND_MODE)) {
            throw new EMP_Exception(EMP_Exception.ErrorDetail.INVALID_PARAMETER, "Map not in freehand draw mode.");
        }

        this.getEditor().Complete();
        this.oEditor = null;
    }

    @Override
    public void drawFeature(IFeature oFeature, IDrawEventListener listener) throws EMP_Exception {
        if ((this.oEditor == null) && (MapMotionLockEnum.UNLOCKED == eLockMode)) {
            switch (oFeature.getFeatureType()) {
                case GEO_POINT:
                    this.oEditor = new PointEditor(this.getMapInstance(), (mil.emp3.api.Point) oFeature, listener);
                    break;
                case GEO_MIL_SYMBOL:
                    MilStdSymbol oSymbol = (MilStdSymbol) oFeature;
                    if (oSymbol.isTacticalGraphic()) {
                        this.oEditor = CoreMilStdUtilities.getMultiPointEditor(this.getMapInstance(), (mil.emp3.api.MilStdSymbol) oFeature, listener);
                    } else {
                        this.oEditor = new MilStdSinglePointEditor(this.getMapInstance(), (mil.emp3.api.MilStdSymbol) oFeature, listener);
                    }
                    break;
                case GEO_PATH:
                    this.oEditor = new PathEditor(this.getMapInstance(), (mil.emp3.api.Path) oFeature, listener);
                    break;
                case GEO_POLYGON:
                    this.oEditor = new PolygonEditor(this.getMapInstance(), (mil.emp3.api.Polygon) oFeature, listener);
                    break;
                case GEO_TEXT:
                    this.oEditor = new TextEditor(this.getMapInstance(), (mil.emp3.api.Text) oFeature, listener);
                    break;
                case GEO_RECTANGLE:
                    this.oEditor = new RectangleEditor(this.getMapInstance(), (mil.emp3.api.Rectangle) oFeature, listener);
                    break;
                case GEO_CIRCLE:
                    this.oEditor = new CircleEditor(this.getMapInstance(), (mil.emp3.api.Circle) oFeature, listener);
                    break;
                case GEO_ELLIPSE:
                    this.oEditor = new EllipseEditor(this.getMapInstance(), (mil.emp3.api.Ellipse) oFeature, listener);
                    break;
                case GEO_SQUARE:
                    this.oEditor = new SquareEditor(this.getMapInstance(), (mil.emp3.api.Square) oFeature, listener);
                    break;
                case GEO_ACM:
                    throw new EMP_Exception(EMP_Exception.ErrorDetail.NOT_SUPPORTED, "Not Supported. The current map can not display a feature of this type.");
            }
        } else {
            throw new EMP_Exception(EMP_Exception.ErrorDetail.OTHER, "Map already in edit/draw mode or locked " + eLockMode);
        }
    }

    @Override
    public IGeoPosition getCameraPosition() {
        return this.getCameraPosition(null);
    }

    public IGeoPosition getCameraPosition(IGeoPosition oTempPos) {
        IGeoPosition oRetPos = (oTempPos == null) ? this.oTempPosition : oTempPos;

        oRetPos.setLatitude(this.currentCamera.getLatitude());
        oRetPos.setLongitude(this.currentCamera.getLongitude());
        oRetPos.setAltitude(this.currentCamera.getAltitude());
        return oRetPos;
    }

    @Override
    public void setFarDistanceThreshold(double dValue) {
        if (!Double.isNaN(dValue)) {
            this.dFarDistanceThreshold = dValue;
            this.getMapInstance().setFarDistanceThreshold(this.dFarDistanceThreshold);
        }
    }

    @Override
    public double getFarDistanceThreshold() {
        return this.dFarDistanceThreshold;
    }

    /**
     * Value will be applied to MapInstance only if setOnMapInstance is true. This is controlled by AdaptiveBitmapCache.
     * If Cache is full above threshold then we will not set this on MapInstance and stop it from taking effect immediately
     *
     * @param dValue
     * @param setOnMapInstance
     */
    @Override
    public void setMidDistanceThreshold(double dValue, boolean setOnMapInstance) {
        if (!Double.isNaN(dValue)) {
            this.dMidDistanceThreshold = dValue;
            if(setOnMapInstance) {
                this.getMapInstance().setMidDistanceThreshold(this.dMidDistanceThreshold);
            }
        }
    }

    @Override
    public double getMidDistanceThreshold() {
        return this.dMidDistanceThreshold;
    }

    @Override
    public void setBounds(IGeoBounds bounds) {
        oBounds = bounds;
    }

    @Override
    public IGeoBounds getBounds() {
        return oBounds;
    }

    @Override
    public void freehandDraw(IGeoStrokeStyle initialStyle, IFreehandEventListener listener) throws EMP_Exception {
        if ((this.oEditor == null) && (MapMotionLockEnum.UNLOCKED == eLockMode)) {
            this.oEditor = new FreehandDrawEditor(this, initialStyle, listener);
        } else {
            throw new EMP_Exception(EMP_Exception.ErrorDetail.OTHER, "Map already in edit/draw mode or locked " + eLockMode);
        }
    }

    @Override
    public void setFreehandDrawStyle(IGeoStrokeStyle style) throws EMP_Exception {
        if ((this.oEditor == null) || !(this.oEditor instanceof FreehandDrawEditor)) {
            throw new EMP_Exception(EMP_Exception.ErrorDetail.OTHER, "Map is not in freehand draw mode.");
        }

        ((FreehandDrawEditor) this.oEditor).setFreehandDrawStyle(style);
    }

    @Override
    public void stopEditing() {
        if (isEditing()) {
            this.getEditor().Cancel();
            this.oEditor = null;
        }
    }

    @Override
    public IGeoStrokeStyle getSelectStrokeStyle() {
        return this.selectedStrokeStyle;
    }

    @Override
    public IGeoLabelStyle getSelectLabelStyle() {
        return this.selectedLabelStyle;
    }

    @Override
    public double getSelectIconScale() {
        return this.selectedIconScale;
    }

    @Override
    public FontSizeModifierEnum getFontSizeModifier() {
        return this.fontSizeModifier;
    }

    @Override
    public void setFontSizeModifier(FontSizeModifierEnum value) {
        this.fontSizeModifier = value;
        if (null != this.getMapInstance()) {
            this.getMapInstance().setFontSizeModifier(value);
        }
    }

    @Override
    public IGeoFillStyle getBufferFillStyle() {
        return bufferFillStyle;
    }
}
