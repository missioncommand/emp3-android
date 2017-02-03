package mil.emp3.api;

import java.util.HashMap;
import mil.emp3.api.abstracts.Feature;
import mil.emp3.api.enums.FeatureTypeEnum;
import mil.emp3.api.exceptions.EMP_Exception;
import org.cmapi.primitives.GeoAirControlMeasure;
import org.cmapi.primitives.IGeoAirControlMeasure;

/**
 *
 * This class Implements an air control measure or airspace.
 */
public class AirControlMeasure extends Feature implements IGeoAirControlMeasure {

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
    public IGeoAirControlMeasure getRenderable() {
        return (IGeoAirControlMeasure) super.getRenderable();
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
    
}
