package mil.emp3.api;

import org.cmapi.primitives.GeoBase;

import java.net.URL;

import mil.emp3.api.interfaces.ILineOfSight;
import mil.emp3.api.utils.EmpGeoColor;
import mil.emp3.api.utils.EmpGeoPosition;

public class LineOfSight extends GeoBase implements ILineOfSight {

    private EmpGeoPosition position;
    private double range;
    private EmpGeoColor visibleAttributes;
    private EmpGeoColor occludeAttributes;


    public LineOfSight(EmpGeoPosition position,
                       double range,
                       EmpGeoColor visibleAttributes,
                       EmpGeoColor occludeAttributes) {
        super();  // to create a UUID for this
        this.position = position;
        this.range = range;
        this.visibleAttributes = visibleAttributes;
        this.occludeAttributes = occludeAttributes;
    }

    @Override
    public EmpGeoPosition getPosition() {
        return position;
    }

    @Override
    public double getRange() {
        return range;
    }

    @Override
    public EmpGeoColor getVisibleAttributes() {
        return visibleAttributes;
    }

    @Override
    public EmpGeoColor getOccludeAttributes() {
        return occludeAttributes;
    }

    @Override
    public URL getURL() {
        return null;
    }
}
