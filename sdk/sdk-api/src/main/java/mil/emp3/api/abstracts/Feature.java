package mil.emp3.api.abstracts;


import org.cmapi.primitives.IGeoAltitudeMode;
import org.cmapi.primitives.IGeoBase;
import org.cmapi.primitives.IGeoFillStyle;
import org.cmapi.primitives.IGeoLabelStyle;
import org.cmapi.primitives.IGeoPosition;
import org.cmapi.primitives.IGeoRenderable;
import org.cmapi.primitives.IGeoStrokeStyle;
import org.cmapi.primitives.IGeoTimeSpan;

import java.util.List;
import java.util.UUID;

import mil.emp3.api.enums.FeatureTypeEnum;
import mil.emp3.api.exceptions.EMP_Exception;
import mil.emp3.api.interfaces.IFeature;
import mil.emp3.api.interfaces.IOverlay;
import mil.emp3.api.interfaces.core.IStorageManager;
import mil.emp3.api.utils.ManagerFactory;

/**
 * This class implements the IFeature interface
 */
public class Feature extends Container implements IFeature {

    final private IStorageManager storageManager = ManagerFactory.getInstance().getStorageManager();

    private final IGeoRenderable oRenderable;
    private final FeatureTypeEnum eFeatureType;

    protected Feature(IGeoRenderable oRenderable, FeatureTypeEnum eFeatureType) {
        super(oRenderable);
        this.oRenderable = oRenderable;
        this.eFeatureType = eFeatureType;
    }
    
    @Override
    public IGeoRenderable getRenderable() {
        return this.oRenderable;
    }

    @Override
    public FeatureTypeEnum getFeatureType() {
        return this.eFeatureType;
    }

    @Override
    public java.util.List<IFeature> getChildFeatures() {
        return storageManager.getChildFeatures(this);
    }

    @Override
    public java.util.List<IOverlay> getParentOverlays() {
        return storageManager.getParentOverlays(this);
    }

    @Override
    public java.util.List<IFeature> getParentFeatures() {
        return storageManager.getParentFeatures(this);
    }

    @Override
    public void addFeature(IFeature feature, boolean visible)
            throws EMP_Exception {
        if (feature == null) {
            throw new EMP_Exception(EMP_Exception.ErrorDetail.INVALID_PARAMETER, "Parameter to Feature.addFeature can not be null.");
        }
        
        java.util.ArrayList<IFeature> oList = new java.util.ArrayList<>();
        oList.add(feature);
        this.addFeatures(oList, visible);
    }

    @Override
    public void addFeatures(java.util.List<IFeature> features, boolean visible)
            throws EMP_Exception {
        if (features == null) {
            throw new EMP_Exception(EMP_Exception.ErrorDetail.INVALID_PARAMETER, "Parameter to Feature.addFeatures can not be null.");
        } else if (features.size() > 0) {
            storageManager.addFeatures(this, features, visible);
        }
    }

    @Override
    public void removeFeature(IFeature feature)
            throws EMP_Exception {
        if(null == feature) return;
        java.util.ArrayList<IFeature> oList = new java.util.ArrayList<>();
        oList.add(feature);
        this.removeFeatures(oList);
    }

    @Override
    public void removeFeatures(java.util.List<IFeature> features)
            throws EMP_Exception {
        if((null == features) || (0 == features.size())) return;
        storageManager.removeFeatures(this, features);
    }

    @Override
    public void setPositions(java.util.List<IGeoPosition> oPositionList) {
        this.oRenderable.setPositions(oPositionList);
    }

    @Override
    public java.util.List<IGeoPosition> getPositions() {
        return this.oRenderable.getPositions();
    }

    @Override
    public void setTimeStamp(java.util.Date date) {
        this.oRenderable.setTimeStamp(date);
    }

    @Override
    public java.util.Date getTimeStamp() {
        return null;
    }

    @Override
    public void setTimeSpans(java.util.List<IGeoTimeSpan> list) {
        this.oRenderable.setTimeSpans(list);
    }

    @Override
    public java.util.List<IGeoTimeSpan> getTimeSpans() {
        return this.oRenderable.getTimeSpans();
    }

    @Override
    public void setAltitudeMode(IGeoAltitudeMode.AltitudeMode eMode) {
        this.oRenderable.setAltitudeMode(eMode);
        if (null == eMode) {
            storageManager.setDefaultAltitudeMode(this);
        }
    }

    @Override
    public IGeoAltitudeMode.AltitudeMode getAltitudeMode() {
        return this.oRenderable.getAltitudeMode();
    }

    @Override
    public void apply() {
        try {
            storageManager.apply(this);
        } catch(EMP_Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void setName(String s) {
        this.oRenderable.setName(s);
    }

    @Override
    public String getName() {
        return this.oRenderable.getName();
    }

    @Override
    public UUID getGeoId() {
        return this.oRenderable.getGeoId();
    }

    @Override
    public void setDescription(String s) {
        this.oRenderable.setDescription(s);
    }

    @Override
    public String getDescription() {
        return this.oRenderable.getDescription();
    }

    @Override
    public void setReadOnly(boolean b) {
        this.oRenderable.setReadOnly(b);
    }

    @Override
    public boolean getReadOnly() {
        return this.oRenderable.getReadOnly();
    }
    
    /**
     * This method overrides the default line style use to render the feature.
     * @param oStyle The new line style to use to render the feature.
     */
    @Override
    public void setStrokeStyle(IGeoStrokeStyle oStyle) {
        this.getRenderable().setStrokeStyle(oStyle);
    }

    /**
     * This method retrieves the current line style.
     * @return The current line style or null if it is not set.
     */
    @Override
    public IGeoStrokeStyle getStrokeStyle() {
        return this.getRenderable().getStrokeStyle();
    }

    /**
     * This method override the default fill style for polygons.
     * @param oStyle The new fill style.
     */
    @Override
    public void setFillStyle(IGeoFillStyle oStyle) {
        this.getRenderable().setFillStyle(oStyle);
    }

    /**
     * This method retrieves the feature fill style.
     * @return The fille style or null if it has not been set.
     */
    @Override
    public IGeoFillStyle getFillStyle() {
        return this.getRenderable().getFillStyle();
    }

    @Override
    public void setLabelStyle(IGeoLabelStyle labelStyle) {
        this.getRenderable().setLabelStyle(labelStyle);
    }

    @Override
    public IGeoLabelStyle getLabelStyle() {
        return this.getRenderable().getLabelStyle();
    }

    @Override
    public void setExtrude(boolean extrude) {
        this.getRenderable().setExtrude(extrude);
    }

    @Override
    public boolean getExtrude() {
        return this.getRenderable().getExtrude();
    }

    @Override
    public void setTessellate(boolean b) {
        this.oRenderable.setTessellate(b);
    }

    @Override
    public boolean getTessellate() {
        return this.oRenderable.getTessellate();
    }

    @Override
    public void setBuffer(double buffer) {
        this.getRenderable().setBuffer(buffer);
    }

    @Override
    public double getBuffer() {
        return this.getRenderable().getBuffer();
    }

    @Override
    public void setAzimuth(double dValue) {
        this.getRenderable().setAzimuth(dValue);
    }

    @Override
    public double getAzimuth() {
        return this.getRenderable().getAzimuth();
    }

    /**
     * This method return the first position on the position list
     * @return The first position on the list or null.
     */
    public IGeoPosition getPosition() {
        java.util.List<IGeoPosition> oList = this.getPositions();

        if ((oList == null) || (oList.size() == 0)) {
            return null;
        }
        return oList.get(0);
    }

    /**
     * This method set the geographic coordinate of a single point feature. It ensures that there
     * is only one position on the list of positions.
     * @param oPosition see {@link org.cmapi.primitives.IGeoPosition}
     */
    public void setPosition(IGeoPosition oPosition) {
        java.util.List<IGeoPosition> oList = this.getPositions();

        if (oPosition != null) {
            if (oList == null) {
                oList = new java.util.ArrayList<>();
                this.setPositions(oList);
            }
            oList.clear();
            oList.add(oPosition);
        }
    }
}
