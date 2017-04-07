package mil.emp3.api.interfaces.core.storage;

import org.cmapi.primitives.IGeoBounds;
import org.cmapi.primitives.IGeoFillStyle;
import org.cmapi.primitives.IGeoLabelStyle;
import org.cmapi.primitives.IGeoPosition;
import org.cmapi.primitives.IGeoStrokeStyle;

import java.util.UUID;

import mil.emp3.api.enums.FontSizeModifierEnum;
import mil.emp3.api.enums.IconSizeEnum;
import mil.emp3.api.enums.EditorMode;
import mil.emp3.api.enums.MapMotionLockEnum;
import mil.emp3.api.enums.MapStateEnum;
import mil.emp3.api.enums.MilStdLabelSettingEnum;
import mil.emp3.api.exceptions.EMP_Exception;
import mil.emp3.api.interfaces.ICamera;
import mil.emp3.api.interfaces.IFeature;
import mil.emp3.api.interfaces.ILookAt;
import mil.emp3.api.listeners.IDrawEventListener;
import mil.emp3.api.listeners.IEditEventListener;
import mil.emp3.api.listeners.IFreehandEventListener;

/*
 * This is an internal interface class.  The app developer must not implement this interface.
 */
public interface IMapStatus {

    MilStdLabelSettingEnum getMilStdLabels();
    void setMilStdLabels(MilStdLabelSettingEnum labelSetting);

    void setIconSize(IconSizeEnum eSize);
    IconSizeEnum getIconSize();
    int getIconPixelSize();

    boolean canEdit(IFeature oFeature);
    boolean canPlot(IFeature oFeature);
    boolean isEditing();

    void editFeature(IFeature oFeature, IEditEventListener listener) throws EMP_Exception;
    void drawFeature(IFeature oFeature, IDrawEventListener listener) throws EMP_Exception;
    void freehandDraw(IGeoStrokeStyle initialStyle, IFreehandEventListener listener) throws EMP_Exception;
    void setFreehandDrawStyle(IGeoStrokeStyle style) throws EMP_Exception;

    void setCamera(ICamera camera);
    ICamera getCamera();
    void setLookAt(ILookAt lookAt);
    ILookAt getLookAt();
    IGeoPosition getCameraPosition();

    void editComplete() throws EMP_Exception;
    void drawComplete() throws EMP_Exception;
    void freehandComplete() throws EMP_Exception;

    void editCancel() throws EMP_Exception;
    void drawCancel() throws EMP_Exception;

    /**
     * Regardless of which EditorMode, if a map is being edited, cancel it.
     */
    void stopEditing();

    void setLockMode(MapMotionLockEnum eMode) throws EMP_Exception;
    MapMotionLockEnum getLockMode();

    EditorMode getEditorMode();

    MapStateEnum getMapState();

    void setFarDistanceThreshold(double dValue);
    void setMidDistanceThreshold(double dValue, boolean setOnMapInstance);
    double getFarDistanceThreshold();
    double getMidDistanceThreshold();

    void setBounds(IGeoBounds bounds);
    IGeoBounds getBounds();

    IGeoStrokeStyle getSelectStrokeStyle();
    IGeoLabelStyle getSelectLabelStyle();
    double getSelectIconScale();
    FontSizeModifierEnum getFontSizeModifier();
    void setFontSizeModifier(FontSizeModifierEnum value);
    IGeoFillStyle getBufferFillStyle();

    /**
     * Returns true if a service with specified uuid has already been installed.
     * @param uuid
     * @return
     */
    boolean serviceExists(UUID uuid);
}
