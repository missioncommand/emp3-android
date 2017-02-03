package mil.emp3.json.geoJson;

import mil.emp3.api.utils.EmpGeoColor;

public class GeoJsonProperties {

    private String name;
    private String id;
    private String description;
    private String timeSpanBegin;
    private String timeSpanEnd;
    private String timeStamp;
    private EmpGeoColor lineStyle;
    private EmpGeoColor polyStyle;
    private String iconStyle;

    public GeoJsonProperties() {}

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getTimeSpanBegin() {
        return timeSpanBegin;
    }

    public void setTimeSpanBegin(String timeSpanBegin) {
        this.timeSpanBegin = timeSpanBegin;
    }

    public String getTimeSpanEnd() {
        return timeSpanEnd;
    }

    public void setTimeSpanEnd(String timeSpanEnd) {
        this.timeSpanEnd = timeSpanEnd;
    }

    public String getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(String timeStamp) {
        this.timeStamp = timeStamp;
    }

    public EmpGeoColor getLineStyle() {
        return lineStyle;
    }

    public void setLineStyle(EmpGeoColor lineStyle) {
        this.lineStyle = lineStyle;
    }

    public EmpGeoColor getPolyStyle() {
        return polyStyle;
    }

    public void setPolyStyle(EmpGeoColor polyStyle) {
        this.polyStyle = polyStyle;
    }

    public String getIconStyle() {
        return iconStyle;
    }

    public void setIconStyle(String iconStyle) {
        this.iconStyle = iconStyle;
    }
}
