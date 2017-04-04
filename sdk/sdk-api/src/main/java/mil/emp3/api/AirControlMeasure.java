package mil.emp3.api;

import java.util.HashMap;
import mil.emp3.api.abstracts.Feature;
import mil.emp3.api.enums.FeatureTypeEnum;
import mil.emp3.api.exceptions.EMP_Exception;
import mil.emp3.api.interfaces.core.ICoreManager;
import mil.emp3.api.interfaces.core.IEventManager;
import mil.emp3.api.utils.ManagerFactory;

import org.cmapi.primitives.GeoAirControlMeasure;
import org.cmapi.primitives.IGeoAirControlMeasure;

/**
 *
 * This class Implements an air control measure or airspace. This class encapsulates a GeoAirControlMeasure object.
 */
public class AirControlMeasure extends Feature<IGeoAirControlMeasure> implements IGeoAirControlMeasure {

    final private IEventManager eventManager = ManagerFactory.getInstance().getEventManager();
    final private ICoreManager coreManager   = ManagerFactory.getInstance().getCoreManager();

    /**
     * This constructor creates an ACM of the given type. If eACMType is null it generates an
     * EMP_Exception.
     * @param eAMCType A value enumerated value from AcmType
     * @throws EMP_Exception If eACMType is null.
     */
    public AirControlMeasure(AcmType eAMCType) throws EMP_Exception {
        super(new GeoAirControlMeasure(), FeatureTypeEnum.GEO_ACM);
        
        if (eAMCType == null) {
            throw new EMP_Exception(EMP_Exception.ErrorDetail.INVALID_PARAMETER, "Invalid ACM Type.");
        }
        ((IGeoAirControlMeasure) super.getRenderable()).setAcmType(eAMCType);
    }

    /**
     * This constructor creates an ACM from the given object.
     * @param oGeoACM An object that implements the IGeoAirControlMeasure interface.
     * @throws EMP_Exception If oGeoACM is null, or the object's AcmType is null.
     */
    public AirControlMeasure(IGeoAirControlMeasure oGeoACM) throws EMP_Exception {
        super(oGeoACM, FeatureTypeEnum.GEO_ACM);
        
        if ((oGeoACM == null) || (oGeoACM.getAcmType() == null)) {
            throw new EMP_Exception(EMP_Exception.ErrorDetail.INVALID_PARAMETER, "Parameter can not be null.");
        }
    }

    @Override
    public void setAcmType(AcmType at) {
        this.getRenderable().setAcmType(at);
    }

    @Override
    public AcmType getAcmType() {
        return this.getRenderable().getAcmType();
    }

    @Override
    public void setAttributes(HashMap<Attribute, String> hm) {
        this.getRenderable().setAttributes(hm);
    }

    @Override
    public HashMap<Attribute, String> getAttributes() {
        return this.getRenderable().getAttributes();
    }

    /**
     * This method removes the attribute from the attribute list.
     * @param attributeName The attribute to remove.
     */
    public void removeAttribute(IGeoAirControlMeasure.Attribute attributeName) {
        if (this.getAttributes().containsKey(attributeName)) {
            this.getAttributes().remove(attributeName);
        }
    }

    /**
     * This method retrieves a double value from the attribute list.
     * @param attributeName The attribute to retrieve
     * @return The double value assigned to the attribute or Double.NaN if the attribute does not exists or is not a double value.
     */
    private double getDoubleAttribute(IGeoAirControlMeasure.Attribute attributeName) {
        String strValue;

        if (!this.getAttributes().containsKey(attributeName)) {
            return Double.NaN;
        }

        strValue = this.getAttributes().get(attributeName);

        try {
            return Double.parseDouble(strValue);
        } catch (NumberFormatException ex) {
        }

        return Double.NaN;
    }

    /**
     * This private method set a double attribute.
     * @param attributeName The attribute to set.
     * @param dValue The value to set.
     */
    private void setDoubleAttribute(IGeoAirControlMeasure.Attribute attributeName, double dValue) {
        if (Double.isNaN(dValue)) {
            throw new IllegalArgumentException("The value can not be NaN.");
        } else {
            this.getAttributes().put(attributeName, dValue + "");
        }
    }

    /**
     * This method retrieves the Radius attribute value.
     * @return The radius value or NaN if it does not exists or is not a valid double value.
     */
    public double getRadiusAttribute() {
        return this.getDoubleAttribute(Attribute.RADIUS);
    }

    /**
     * This method set the Radius attribute
     * @param dValue The radius value to set. An IllegalArgumentException is raised if the value is Double.Nan.
     */
    public void setRadiusAttribute(double dValue) {
        this.setDoubleAttribute(Attribute.RADIUS, dValue);
    }

    /**
     * This method retrieves the inner radius attribute value.
     * @return The inner radius value or NaN if it does not exists or is not a valid double value.
     */
    public double getInnerRadiusAttribute() {
        return this.getDoubleAttribute(Attribute.INNER_RADIUS);
    }


    /**
     * This method set the Inner Radius attribute
     * @param dValue The inner radius value to set. An IllegalArgumentException is raised if the value is Double.Nan.
     */
    public void setInnerRadiusAttribute(double dValue) {
        this.setDoubleAttribute(Attribute.INNER_RADIUS, dValue);
    }

    /**
     * This method retrieves the turn attribute value.
     * @return The turn value or NaN if it does not exists or is not a valid double value.
     */
    //public ACMTurnEnum getTurnAttribute() {

    //}

    /**
     * This method set the turn attribute
     * @param eValue The turn value to set. An IllegalArgumentException is raised if the value is null.
     */
    //public void setTurnAttribute(ACMTurnEnum eValue) {

    //}

    /**
     * This method retrieves the Minimum Altitude attribute value.
     * @return The minimum altitude value or NaN if it does not exists or is not a valid double value.
     */
    public double getMinAltAttribute() {
        return this.getDoubleAttribute(Attribute.MIN_ALT);
    }

    /**
     * This method set the minimum altitude attribute
     * @param dValue The minimum altitude value to set. An IllegalArgumentException is raised if the value is Double.Nan.
     */
    public void setMinAltAttribute(double dValue) {
        this.setDoubleAttribute(Attribute.MIN_ALT, dValue);
    }

    /**
     * This method retrieves the maximum altitude attribute value.
     * @return The maximum altitude value or NaN if it does not exists or is not a valid double value.
     */
    public double getMaxAltAttribute() {
        return this.getDoubleAttribute(Attribute.MAX_ALT);
    }

    /**
     * This method set the Maximum Altitude attribute
     * @param dValue The maximum altitude value to set. An IllegalArgumentException is raised if the value is Double.Nan.
     */
    public void setMaxAltAttribute(double dValue) {
        this.setDoubleAttribute(Attribute.MAX_ALT, dValue);
    }

    /**
     * This method retrieves the left azimuth attribute value.
     * @return The left azimuth value or NaN if it does not exists or is not a valid double value.
     */
    public double getLeftAzimuthAttribute() {
        return this.getDoubleAttribute(Attribute.LEFT_AZIMUTH);
    }

    /**
     * This method set the left azimuth attribute
     * @param dValue The left azimuth value to set. An IllegalArgumentException is raised if the value is Double.Nan.
     */
    public void setLeftAzimuthAttribute(double dValue) {
        this.setDoubleAttribute(Attribute.LEFT_AZIMUTH, dValue);
    }

    /**
     * This method retrieves the right azimuth attribute value.
     * @return The right azimuth value or NaN if it does not exists or is not a valid double value.
     */
    public double getRightAzimuthAttribute() {
        return this.getDoubleAttribute(Attribute.RIGHT_AZIMUTH);
    }

    /**
     * This method set the right azimuth attribute
     * @param dValue The right azimuth value to set. An IllegalArgumentException is raised if the value is Double.Nan.
     */
    public void setRightAzimuthAttribute(double dValue) {
        this.setDoubleAttribute(Attribute.RIGHT_AZIMUTH, dValue);
    }

    /**
     * This method retrieves the width attribute value.
     * @return The width value or NaN if it does not exists or is not a valid double value.
     */
    public double getWidthAttribute() {
        return this.getDoubleAttribute(Attribute.WIDTH);
    }

    /**
     * This method set the width attribute
     * @param dValue The width value to set. An IllegalArgumentException is raised if the value is Double.Nan.
     */
    public void setWidthAttribute(double dValue) {
        this.setDoubleAttribute(Attribute.WIDTH, dValue);
    }

    /**
     * This method retrieves the left width attribute value.
     * @return The left width value or NaN if it does not exists or is not a valid double value.
     */
    public double getLeftWidthAttribute() {
        return this.getDoubleAttribute(Attribute.LEFT_WIDTH);
    }

    /**
     * This method set the left width attribute
     * @param dValue The left width value to set. An IllegalArgumentException is raised if the value is Double.Nan.
     */
    public void setLeftWidthAttribute(double dValue) {
        this.setDoubleAttribute(Attribute.LEFT_WIDTH, dValue);
    }

    /**
     * This method retrieves the right width attribute value.
     * @return The radius value or NaN if it does not exists or is not a valid double value.
     */
    public double getRightWidthAttribute() {
        return this.getDoubleAttribute(Attribute.RIGHT_WIDTH);
    }

    /**
     * This method set the right width attribute
     * @param dValue The right width value to set. An IllegalArgumentException is raised if the value is Double.Nan.
     */
    public void setRightWidthAttribute(double dValue) {
        this.setDoubleAttribute(Attribute.RIGHT_WIDTH, dValue);
    }
}
