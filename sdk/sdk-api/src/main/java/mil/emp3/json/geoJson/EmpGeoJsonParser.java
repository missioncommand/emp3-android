package mil.emp3.json.geoJson;

import org.cmapi.primitives.IGeoAltitudeMode;
import org.cmapi.primitives.IGeoColor;
import org.cmapi.primitives.IGeoFillStyle;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import mil.emp3.api.exceptions.EMP_Exception;
import mil.emp3.api.Path;
import mil.emp3.api.Point;
import mil.emp3.api.Polygon;
import mil.emp3.api.interfaces.IFeature;

public class EmpGeoJsonParser {

    private final List<IFeature> featureList = new ArrayList<>();

    public EmpGeoJsonParser(InputStream is) throws EMP_Exception {
        List<GeoJsonFeature> geoJsonFeatureList = GeoJsonParser.parse(is);
        for (GeoJsonFeature geoJsonFeature : geoJsonFeatureList) {
            IFeature feature = translate(geoJsonFeature);
            featureList.add(feature);
        }
    }

    public EmpGeoJsonParser(String string) throws EMP_Exception {
        List<GeoJsonFeature> geoJsonFeatureList = GeoJsonParser.parse(string);
        for (GeoJsonFeature geoJsonFeature : geoJsonFeatureList) {
            IFeature feature = translate(geoJsonFeature);
            featureList.add(feature);
        }
    }

    private IFeature translate(GeoJsonFeature geoJsonFeature) {
        IFeature newFeature = null;
        GeoJsonProperties properties = geoJsonFeature.getProperties();
        String name = null;
        String description = null;
        if (properties != null) {
            name = properties.getName();
            description = properties.getDescription();
        }

        switch (geoJsonFeature.getGeometryType()) {
            case POINT: {
                Point newPoint = new Point();

                if (null == name) {
                    name = "GeoJSON Point";
                }

                if (null != properties.getIconStyle()) {
                    newPoint.setIconURI(properties.getIconStyle());
                }

                newPoint.setPosition(geoJsonFeature.getCoordinates().get(0));
                newFeature = newPoint;
                break;
            }
            case LINE: {
                Path line = new Path();

                if (null == name) {
                    name = "GeoJSON LineString";
                }

                if (null != properties.getLineStyle()) {
                    line.getStrokeStyle().setStrokeColor(properties.getLineStyle());
                }

                line.getPositions().addAll(geoJsonFeature.getCoordinates());
                newFeature = line;
                break;
            }
            case POLYGON: {
                Polygon newPolygon = new Polygon();

                if (null == name) {
                    name = "GeoJSON Polygon";
                }

                if (null != properties.getLineStyle()) {
                    newPolygon.getStrokeStyle().setStrokeColor(properties.getLineStyle());
                }
                if (null != properties.getPolyStyle()) {
                    newPolygon.getFillStyle().setFillColor(properties.getPolyStyle());
                }

                newPolygon.getPositions().addAll(geoJsonFeature.getCoordinates());
                newFeature = newPolygon;
                break;
            }
        }

        if (null != newFeature) {
            newFeature.setName(name);

            if (description != null) {
                newFeature.setDescription(description);
            }
            newFeature.setAltitudeMode(IGeoAltitudeMode.AltitudeMode.CLAMP_TO_GROUND);
        }
        return newFeature;
    }

    public List<IFeature> getFeatureList() {
        return featureList;
    }
}
