package mil.emp3.core.mapgridlines;

import android.util.Log;

import org.cmapi.primitives.GeoLabelStyle;
import org.cmapi.primitives.GeoStrokeStyle;
import org.cmapi.primitives.IGeoLabelStyle;
import org.cmapi.primitives.IGeoPosition;
import org.cmapi.primitives.IGeoRenderable;
import org.cmapi.primitives.IGeoStrokeStyle;

import java.util.ArrayList;
import java.util.List;

import mil.emp3.api.Path;
import mil.emp3.api.Text;
import mil.emp3.api.interfaces.ICamera;
import mil.emp3.api.interfaces.IEmpBoundingBox;
import mil.emp3.api.interfaces.IFeature;
import mil.emp3.api.utils.EmpGeoColor;
import mil.emp3.api.utils.EmpGeoPosition;
import mil.emp3.api.utils.GeographicLib;
import mil.emp3.mapengine.interfaces.IMapInstance;

/**
 * This class generates Decimal Degree map grid lines.
 */

public class DDMapGridLines extends AbstractMapGridLine {
    private static final String TAG = DDMapGridLines.class.getSimpleName();

    private static final String DD_GRID_LINE_MAJOR = "DD.gridline.major";
    private static final String DD_GRID_LINE_MINOR = "DD.gridline.minor";
    private static final String DD_GRID_LAT_MAJOR_VALUE = "DD.grid.lat.major";
    private static final String DD_GRID_LONG_MAJOR_VALUE = "DD.grid.long.major";
    private static final String DD_GRID_LAT_MINOR_VALUE = "DD.grid.lat.minor";
    private static final String DD_GRID_LONG_MINOR_VALUE = "DD.grid.long.minor";

    private static final int DD_10_MICRO_DEGREE_INTERVAL = 0;
    private static final int DD_50_MICRO_DEGREE_INTERVAL = DD_10_MICRO_DEGREE_INTERVAL + 1;
    private static final int DD_100_MICRO_DEGREE_INTERVAL = DD_50_MICRO_DEGREE_INTERVAL + 1;
    private static final int DD_500_MICRO_DEGREE_INTERVAL = DD_100_MICRO_DEGREE_INTERVAL + 1;
    private static final int DD_1_MILLI_DEGREE_INTERVAL = DD_500_MICRO_DEGREE_INTERVAL + 1;
    private static final int DD_5_MILLI_DEGREE_INTERVAL = DD_1_MILLI_DEGREE_INTERVAL + 1;
    private static final int DD_10_MILLI_DEGREE_INTERVAL = DD_5_MILLI_DEGREE_INTERVAL + 1;
    private static final int DD_50_MILLI_DEGREE_INTERVAL = DD_10_MILLI_DEGREE_INTERVAL + 1;
    private static final int DD_100_MILLI_DEGREE_INTERVAL = DD_50_MILLI_DEGREE_INTERVAL + 1;
    private static final int DD_500_MILLI_DEGREE_INTERVAL = DD_100_MILLI_DEGREE_INTERVAL + 1;
    private static final int DD_1_DEGREE_INTERVAL = DD_500_MILLI_DEGREE_INTERVAL + 1;
    private static final int DD_5_DEGREE_INTERVAL = DD_1_DEGREE_INTERVAL + 1;
    private static final int DD_10_DEGREE_INTERVAL = DD_5_DEGREE_INTERVAL + 1;
    private static final int DD_LAST_INTERVAL = DD_10_DEGREE_INTERVAL + 1;

    private final double[] intervalThreshold = new double[DD_LAST_INTERVAL];
    private final String[] formatString = new String[DD_LAST_INTERVAL];
    private final double[] intervalIncrement = new double[DD_LAST_INTERVAL];

    public DDMapGridLines(IMapInstance mapInstance) {
        super(mapInstance);
        setStyles();
        initialize();
    }

    // This method loads the styles.
    private void setStyles() {
        EmpGeoColor color;
        IGeoStrokeStyle strokeStyle;
        IGeoLabelStyle labelStyle;

        color = new EmpGeoColor(0.5, 75, 75, 255);
        strokeStyle = new GeoStrokeStyle();
        strokeStyle.setStrokeColor(color);
        strokeStyle.setStrokeWidth(3.0);
        addStrokeStyle(DD_GRID_LINE_MAJOR, strokeStyle);

        color = new EmpGeoColor(0.5, 150, 150, 255);
        strokeStyle = new GeoStrokeStyle();
        strokeStyle.setStrokeColor(color);
        strokeStyle.setStrokeWidth(1.0);
        addStrokeStyle(DD_GRID_LINE_MINOR, strokeStyle);

        labelStyle = new GeoLabelStyle();
        color = new EmpGeoColor(1.0, 150, 150, 150);
        labelStyle.setColor(color);
        labelStyle.setSize(8.0);
        labelStyle.setJustification(IGeoLabelStyle.Justification.LEFT);
        labelStyle.setFontFamily("Arial");
        labelStyle.setTypeface(IGeoLabelStyle.Typeface.REGULAR);
        addLabelStyle(DD_GRID_LAT_MAJOR_VALUE, labelStyle);
        addLabelStyle(DD_GRID_LONG_MAJOR_VALUE, labelStyle);

        labelStyle = new GeoLabelStyle();
        color = new EmpGeoColor(1.0, 150, 150, 150);
        labelStyle.setColor(color);
        labelStyle.setSize(6.0);
        labelStyle.setJustification(IGeoLabelStyle.Justification.LEFT);
        labelStyle.setFontFamily("Arial");
        labelStyle.setTypeface(IGeoLabelStyle.Typeface.REGULAR);
        addLabelStyle(DD_GRID_LAT_MINOR_VALUE, labelStyle);
        addLabelStyle(DD_GRID_LONG_MINOR_VALUE, labelStyle);
    }

    @Override
    protected void setPathAttributes(Path path, String gridObjectType) {
        if (gridObjectType.startsWith("DD.")) {
            path.setPathType(IGeoRenderable.PathType.RHUMB_LINE);
        } else {
            super.setPathAttributes(path, gridObjectType);
        }
    }

    @Override
    protected void setLabelAttributes(Text label, String gridObjectType) {
        if (gridObjectType.startsWith("DD.")) {
            switch (gridObjectType) {
                case DD_GRID_LONG_MAJOR_VALUE:
                case DD_GRID_LONG_MINOR_VALUE:
                    label.setAzimuth(-90.0);
                    break;
            }
        } else {
            super.setLabelAttributes(label, gridObjectType);
        }
    }

    private void initialize() {
        intervalThreshold[DD_10_MICRO_DEGREE_INTERVAL]  = 0.00001;
        intervalThreshold[DD_50_MICRO_DEGREE_INTERVAL]  = 0.00005;
        intervalThreshold[DD_100_MICRO_DEGREE_INTERVAL] = 0.0001;
        intervalThreshold[DD_500_MICRO_DEGREE_INTERVAL] = 0.0005;
        intervalThreshold[DD_1_MILLI_DEGREE_INTERVAL]   = 0.001;
        intervalThreshold[DD_5_MILLI_DEGREE_INTERVAL]   = 0.005;
        intervalThreshold[DD_10_MILLI_DEGREE_INTERVAL]  = 0.01;
        intervalThreshold[DD_50_MILLI_DEGREE_INTERVAL]  = 0.05;
        intervalThreshold[DD_100_MILLI_DEGREE_INTERVAL] = 0.1;
        intervalThreshold[DD_500_MILLI_DEGREE_INTERVAL] = 0.5;
        intervalThreshold[DD_1_DEGREE_INTERVAL]         = 1.37;
        intervalThreshold[DD_5_DEGREE_INTERVAL]         = 5.0;
        intervalThreshold[DD_10_DEGREE_INTERVAL]        = 6.5;

        formatString[DD_10_MICRO_DEGREE_INTERVAL] = "%9.5f°";
        formatString[DD_50_MICRO_DEGREE_INTERVAL] = "%9.5f°";
        formatString[DD_100_MICRO_DEGREE_INTERVAL] = "%8.4f°";
        formatString[DD_500_MICRO_DEGREE_INTERVAL] = "%8.4f°";
        formatString[DD_1_MILLI_DEGREE_INTERVAL] = "%7.3f°";
        formatString[DD_5_MILLI_DEGREE_INTERVAL] = "%7.3f°";
        formatString[DD_10_MILLI_DEGREE_INTERVAL] = "%6.2f°";
        formatString[DD_50_MILLI_DEGREE_INTERVAL] = "%6.2f°";
        formatString[DD_100_MILLI_DEGREE_INTERVAL] = "%5.1f°";
        formatString[DD_500_MILLI_DEGREE_INTERVAL] = "%5.1f°";
        formatString[DD_1_DEGREE_INTERVAL] = "%4.0f°";
        formatString[DD_5_DEGREE_INTERVAL] = "%4.0f°";
        formatString[DD_10_DEGREE_INTERVAL] = "%4.0f°";

        intervalIncrement[DD_10_MICRO_DEGREE_INTERVAL]  = 0.00001;
        intervalIncrement[DD_50_MICRO_DEGREE_INTERVAL]  = 0.00005;
        intervalIncrement[DD_100_MICRO_DEGREE_INTERVAL] = 0.0001;
        intervalIncrement[DD_500_MICRO_DEGREE_INTERVAL] = 0.0005;
        intervalIncrement[DD_1_MILLI_DEGREE_INTERVAL]   = 0.001;
        intervalIncrement[DD_5_MILLI_DEGREE_INTERVAL]   = 0.005;
        intervalIncrement[DD_10_MILLI_DEGREE_INTERVAL]  = 0.01;
        intervalIncrement[DD_50_MILLI_DEGREE_INTERVAL]  = 0.05;
        intervalIncrement[DD_100_MILLI_DEGREE_INTERVAL] = 0.1;
        intervalIncrement[DD_500_MILLI_DEGREE_INTERVAL] = 0.5;
        intervalIncrement[DD_1_DEGREE_INTERVAL]         = 1.0;
        intervalIncrement[DD_5_DEGREE_INTERVAL]         = 5.0;
        intervalIncrement[DD_10_DEGREE_INTERVAL]       = 10.0;
    }

    @Override
    protected void processViewChange(IEmpBoundingBox mapBounds, ICamera camera, double metersPerPixel) {
        if ((this.getBoundingBoxPixelHeight() == 0) || (this.getBoundingBoxPixelWidth() == 0)) {
            return;
        }
        // Pixels per fraction of an inch.
        double pixelsPerFractionInch = PIXELS_PER_INCH / 4.0; // 1/4 inch

        // Calculate the meters per 1/8 of an inch on the display.
        double metersInOneEighthOfAnInch = metersPerPixel * PIXELS_PER_INCH / 8.0;

        // Index 0 for latitude and 1 from longitude.
        double[] degreesPerFractionInch = new double[] {mapBounds.deltaLatitude() * pixelsPerFractionInch / (double) this.getBoundingBoxPixelHeight(),
                mapBounds.deltaLongitude() * pixelsPerFractionInch / (double) this.getBoundingBoxPixelWidth()};
        int[] gridSetting = new int[] { -1, -1};

        clearFeatureList();

        double degreesPerUnit;

        try {
            for (int iIndex = 0; iIndex < 2; iIndex++) {
                degreesPerUnit = degreesPerFractionInch[iIndex];
                for (int setting = 0; setting < DD_LAST_INTERVAL; setting++) {
                    if (degreesPerUnit < intervalThreshold[setting]) {
                        gridSetting[iIndex] = setting;
                        Log.i(TAG, ((iIndex == 0) ? "Lat" : "Long") + " " + intervalIncrement[setting] + " Grid :" + degreesPerUnit);
                        break;
                    }
                }
            }

            if ((gridSetting[0] > -1) && (gridSetting[1] > -1)) {
                createDDGridLines(gridSetting[0], gridSetting[1], mapBounds, metersPerPixel, metersInOneEighthOfAnInch);
                displayGridLabel("DD Grid", mapBounds, metersPerPixel);
            } else {
                displayGridLabel("DD Grid Off", mapBounds, metersPerPixel);
                Log.i(TAG, "DD Grid Off :");
            }
            // Else the grid turns off.
        } catch (Exception Ex) {
            Log.e(TAG, "DMS grid generation failed.", Ex);
        }
    }

    private double initDegrees(int gridSetting, double degrees) {
        double newValue = Math.floor(degrees / this.intervalIncrement[gridSetting]) * this.intervalIncrement[gridSetting];

        if (degrees > newValue) {
            newValue += this.intervalIncrement[gridSetting];
        }

        return newValue;
    }

    private void initLatitude(int gridSetting, EmpGeoPosition position) {
        position.setLatitude(initDegrees(gridSetting, position.getLatitude()));
    }

    private void initLongitude(int gridSetting, EmpGeoPosition position) {
        position.setLongitude(initDegrees(gridSetting, position.getLongitude()));
    }

    private double incrementDegrees(int gridSetting, double degrees) {
        //return initDegrees(gridSetting, degrees + this.intervalIncrement[gridSetting]);
        return degrees + this.intervalIncrement[gridSetting];
    }

    private void incrementLatitude(int gridSetting, EmpGeoPosition position) {
        double latitude = incrementDegrees(gridSetting, position.getLatitude());
        position.setLatitude(EmpGeoPosition.normalizeLatitude(latitude));
    }

    private void incrementLongitude(int gridSetting, EmpGeoPosition position) {
        double longitude = incrementDegrees(gridSetting, position.getLongitude());
        position.setLongitude(EmpGeoPosition.normalizeLongitude(longitude));
    }

    private String getLabelString(int gridSetting, double value) {
        return String.format(formatString[gridSetting], value);
    }

    private void createDDGridLines(int latitudeGridSetting, int longitudeGridSetting, IEmpBoundingBox mapBounds, double metersPerPixel, double metersInOneEighthOfAnInch) {
        EmpGeoPosition meridianNorth = new EmpGeoPosition(mapBounds.getNorth(), mapBounds.getWest());
        EmpGeoPosition meridianSouth = new EmpGeoPosition(mapBounds.getSouth(), mapBounds.getWest());
        EmpGeoPosition parallelWest = new EmpGeoPosition(mapBounds.getSouth(), mapBounds.getWest());
        EmpGeoPosition parallelEast = new EmpGeoPosition(mapBounds.getSouth(), mapBounds.getEast());

        initLongitude(longitudeGridSetting, meridianNorth);
        initLongitude(longitudeGridSetting, meridianSouth);

        initLatitude(latitudeGridSetting, parallelWest);
        initLatitude(latitudeGridSetting, parallelEast);

        createMeridians(longitudeGridSetting, mapBounds, meridianNorth, meridianSouth, metersInOneEighthOfAnInch);
        createParallels(latitudeGridSetting, mapBounds, parallelWest, parallelEast, metersInOneEighthOfAnInch);
    }

    private void createMeridians(int longitudeGridSetting, IEmpBoundingBox mapBounds, EmpGeoPosition northCoordinate, EmpGeoPosition southCoordinate, double metersInOneEighthOfAnInch) {
        IFeature gridObject;
        IGeoPosition pos;
        double northLat;
        double southLat;
        String label;
        List<IGeoPosition> posList = new ArrayList<>();
        double longitude = northCoordinate.getLongitude();
        double centerLat = mapBounds.centerLatitude();
        double labelOffset =  metersInOneEighthOfAnInch / 2.0;

        while (mapBounds.contains(centerLat, longitude)) {
            posList.clear();
            northLat = northCoordinate.getLatitude();
            pos = new EmpGeoPosition(northLat, longitude);
            posList.add(pos);
            southLat = southCoordinate.getLatitude();
            pos = new EmpGeoPosition(southLat, longitude);
            posList.add(pos);

            gridObject = createPathFeature(posList, DD_GRID_LINE_MINOR);
            addFeature(gridObject);
          
            pos = GeographicLib.computeRhumbPositionAt(0.0, labelOffset, pos);
            label = getLabelString(longitudeGridSetting, longitude);
            gridObject = createLabelFeature(pos, label, DD_GRID_LONG_MINOR_VALUE);
            addFeature(gridObject);

            incrementLongitude(longitudeGridSetting, northCoordinate);
            incrementLongitude(longitudeGridSetting, southCoordinate);
            longitude = northCoordinate.getLongitude();
        }
    }

    private void createParallels(int latitudeGridSetting, IEmpBoundingBox mapBounds, EmpGeoPosition westCoordinate, EmpGeoPosition eastCoordinate, double metersInOneEighthOfAnInch) {
        IFeature gridObject;
        IGeoPosition pos;
        String label;
        double westLong;
        double eastLong;
        double latitude = westCoordinate.getLatitude();
        List<IGeoPosition> posList = new ArrayList<>();
        double centerLongitude = mapBounds.centerLongitude();
        double labelOffset =  metersInOneEighthOfAnInch;

        while (mapBounds.contains(latitude, centerLongitude)) {
            posList.clear();
            eastLong = eastCoordinate.getLongitude();
            pos = new EmpGeoPosition(latitude, eastLong);
            posList.add(pos);

            westLong = westCoordinate.getLongitude();
            pos = new EmpGeoPosition(latitude, westLong);
            posList.add(pos);

            gridObject = createPathFeature(posList, DD_GRID_LINE_MINOR);
            addFeature(gridObject);

            pos = GeographicLib.computeRhumbPositionAt(90.0, labelOffset, pos);
            label = getLabelString(latitudeGridSetting, latitude);
            Log.i(TAG, "    Lat:" + label);
            gridObject = createLabelFeature(pos, label, DD_GRID_LAT_MINOR_VALUE);
            addFeature(gridObject);

            incrementLatitude(latitudeGridSetting, westCoordinate);
            incrementLatitude(latitudeGridSetting, eastCoordinate);
            latitude = westCoordinate.getLatitude();
        }
    }
}
