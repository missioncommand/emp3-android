package mil.emp3.json.geoJson;

import org.cmapi.primitives.IGeoPosition;

import java.util.ArrayList;
import java.util.List;

public class GeoJsonFeature {

    private GeometryEnum geometryType;
    private List<IGeoPosition> coordinates;
    private GeoJsonProperties properties;

    public GeoJsonFeature() {
        coordinates = new ArrayList<>();
    }

    public GeometryEnum getGeometryType() {
        return geometryType;
    }

    public void setGeometryType(GeometryEnum geometryType) {
        this.geometryType = geometryType;
    }

    public void setCoordinates(List<IGeoPosition> coordinates) {
        this.coordinates = coordinates;
    }

    public List<IGeoPosition> getCoordinates() {
        return coordinates;
    }

    public void add(IGeoPosition pair) {
        coordinates.add(pair);
    }

    public GeoJsonProperties getProperties() {
        return properties;
    }

    public void setProperties(GeoJsonProperties properties) {
        this.properties = properties;
    }
}
