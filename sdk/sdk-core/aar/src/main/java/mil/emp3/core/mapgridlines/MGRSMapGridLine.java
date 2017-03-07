package mil.emp3.core.mapgridlines;

import android.content.res.Resources;
import android.graphics.Point;
import android.util.Log;

import org.cmapi.primitives.GeoLabelStyle;
import org.cmapi.primitives.GeoPosition;
import org.cmapi.primitives.GeoStrokeStyle;
import org.cmapi.primitives.IGeoLabelStyle;
import org.cmapi.primitives.IGeoPosition;
import org.cmapi.primitives.IGeoStrokeStyle;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import mil.emp3.api.Path;
import mil.emp3.api.Text;
import mil.emp3.api.interfaces.ICamera;
import mil.emp3.api.interfaces.IFeature;
import mil.emp3.api.utils.EmpBoundingBox;
import mil.emp3.api.utils.EmpGeoColor;
import mil.emp3.api.utils.FontUtilities;
import mil.emp3.api.utils.GeoLibrary;
import mil.emp3.core.mapgridlines.coordinates.UTMCoordinate;
import mil.emp3.core.mapgridlines.utils.GridLineUtils;
import mil.emp3.mapengine.interfaces.IMapInstance;

/**
 * This class implements the main MGRS grid line generator class.
 */

public class MGRSMapGridLine extends UTMBaseMapGridLine {
    private static final String TAG = MGRSMapGridLine.class.getSimpleName();

    private static final int PIXELS_PER_INCH = Resources.getSystem().getDisplayMetrics().densityDpi;
    private static final int MGRS_100K_METER_GRID = 100000;
    private static final int MGRS_10K_METER_GRID = 10000;
    private static final int MGRS_1K_METER_GRID = 1000;
    private static final int MGRS_100_METER_GRID = 100;
    private static final int MGRS_10_METER_GRID = 10;
    private static final int MGRS_1_METER_GRID = 1;

    private static final String MGRS_GRID_ZONE_MERIDIAN = "gridzone.meridian";
    private static final String MGRS_GRID_ZONE_PARALLELS = "gridzone.parallels";
    private static final String MGRS_GRID_ZONE_LABEL = "gridzone.label";

    private static final String MGRS_GRID_LABEL = "MGRS.grid.label";
    private static final String MGRS_GRID_BOX_MERIDIAN = "MGRS.gridbox.meridian";
    private static final String MGRS_GRID_BOX_PARALLELS = "MGRS.gridbox.parallels";
    private static final String MGRS_GRID_LINE_MAJOR_MERIDIAN = "MGRS.gridline.major.meridian";
    private static final String MGRS_GRID_LINE_MAJOR_PARALLELS = "MGRS.gridline.major.parallels";
    private static final String MGRS_GRID_LINE_MINOR_MERIDIAN = "MGRS.gridline.minor.meridian";
    private static final String MGRS_GRID_LINE_MINOR_PARALLELS = "MGRS.gridline.minor.parallels";
    private static final String MGRS_GRID_BOX_LABEL_CENTERED = "MGRS.label.centered";
    private static final String MGRS_GRID_BOX_NORTH_VALUE = "MGRS.north.values";
    private static final String MGRS_GRID_BOX_EAST_VALUE = "MGRS.east.values";

    private static final int ONE_CHARACTER_8PT_PIXEL_WIDTH = FontUtilities.fontPointsToPixels(8);
    private static final int TWO_CHARACTER_12PT_PIXEL_WIDTH = FontUtilities.fontPointsToPixels(12) * 2;
    private static final int THREE_CHARACTER_12PT_PIXEL_WIDTH = FontUtilities.fontPointsToPixels(12) * 3;

    private final Map<String, IGeoStrokeStyle> strokeStyleMap;
    private final Map<String, IGeoLabelStyle> labelStyleMap;

    private final Map<Integer, Integer> gridValueDigits;

    /**
     * UTM zones are grouped, and assigned to one of a group of 6
     * sets.
     */
    private static final int NUM_100K_SETS = 6;

    /**
     * The column letters (for easting) of the lower left value, per
     * set.
     */
    private static final String SET_ORIGIN_COLUMN_LETTERS = "AJSAJS";

    /**
     * The row letters (for northing) of the lower left value, per
     * set.
     */
    private static final String SET_ORIGIN_ROW_LETTERS = "AFAFAF";

    private static final int A = 65; // A
    private static final int I = 73; // I
    private static final int O = 79; // O
    private static final int V = 86; // V
    private static final int Z = 90; // Z

    public MGRSMapGridLine(IMapInstance mapInstance) {
        super(mapInstance);
        this.strokeStyleMap = new HashMap<>();
        this.labelStyleMap = new HashMap<>();
        setStyles();
        this.gridValueDigits = new HashMap<>();
        this.gridValueDigits.put(MGRS_10K_METER_GRID, 1);
        this.gridValueDigits.put(MGRS_1K_METER_GRID, 2);
        this.gridValueDigits.put(MGRS_100_METER_GRID, 3);
        this.gridValueDigits.put(MGRS_10_METER_GRID, 4);
        this.gridValueDigits.put(MGRS_1_METER_GRID, 5);
    }

    private void setStyles() {
        EmpGeoColor color;
        IGeoStrokeStyle strokeStyle;
        IGeoLabelStyle labelStyle;

        // Grid zone styles.
        color = new EmpGeoColor(0.6, 255, 255, 0);
        strokeStyle = new GeoStrokeStyle();
        strokeStyle.setStrokeColor(color);
        strokeStyle.setStrokeWidth(3.0);
        strokeStyleMap.put(MGRS_GRID_ZONE_MERIDIAN, strokeStyle);
        strokeStyleMap.put(MGRS_GRID_ZONE_PARALLELS, strokeStyle);

        labelStyle = new GeoLabelStyle();
        color = new EmpGeoColor(1.0, 255, 255, 0);
        labelStyle.setColor(color);
        labelStyle.setSize(12.0);
        labelStyle.setJustification(IGeoLabelStyle.Justification.CENTER);
        labelStyle.setFontFamily("Ariel");
        labelStyle.setTypeface(IGeoLabelStyle.Typeface.REGULAR);
        labelStyleMap.put(MGRS_GRID_ZONE_LABEL, labelStyle);

        // MGRS Grid Label
        labelStyle = new GeoLabelStyle();
        color = new EmpGeoColor(0.6, 200, 200, 200);
        labelStyle.setColor(color);
        labelStyle.setSize(8.0);
        labelStyle.setJustification(IGeoLabelStyle.Justification.LEFT);
        labelStyle.setFontFamily("Ariel");
        labelStyle.setTypeface(IGeoLabelStyle.Typeface.REGULAR);
        labelStyleMap.put(MGRS_GRID_LABEL, labelStyle);

        // MGRS Grid Box
        color = new EmpGeoColor(0.7, 0, 0, 255);
        strokeStyle = new GeoStrokeStyle();
        strokeStyle.setStrokeColor(color);
        strokeStyle.setStrokeWidth(4.0);
        strokeStyleMap.put(MGRS_GRID_BOX_MERIDIAN, strokeStyle);
        strokeStyleMap.put(MGRS_GRID_BOX_PARALLELS, strokeStyle);

        // MGRS Grid Lines Major
        color = new EmpGeoColor(0.5, 75, 75, 255);
        strokeStyle = new GeoStrokeStyle();
        strokeStyle.setStrokeColor(color);
        strokeStyle.setStrokeWidth(3.0);
        strokeStyleMap.put(MGRS_GRID_LINE_MAJOR_MERIDIAN, strokeStyle);
        strokeStyleMap.put(MGRS_GRID_LINE_MAJOR_PARALLELS, strokeStyle);

        // MGRS Grid Lines Minor
        color = new EmpGeoColor(0.5, 150, 150, 255);
        strokeStyle = new GeoStrokeStyle();
        strokeStyle.setStrokeColor(color);
        strokeStyle.setStrokeWidth(1.0);
        strokeStyleMap.put(MGRS_GRID_LINE_MINOR_MERIDIAN, strokeStyle);
        strokeStyleMap.put(MGRS_GRID_LINE_MINOR_PARALLELS, strokeStyle);

        // MGRS ID
        // Centered
        labelStyle = new GeoLabelStyle();
        color = new EmpGeoColor(0.7, 100, 100, 255);
        labelStyle.setColor(color);
        labelStyle.setSize(12.0);
        labelStyle.setJustification(IGeoLabelStyle.Justification.CENTER);
        labelStyle.setFontFamily("Ariel");
        labelStyle.setTypeface(IGeoLabelStyle.Typeface.BOLD);
        labelStyleMap.put(MGRS_GRID_BOX_LABEL_CENTERED, labelStyle);

        // MGRS northing values
        labelStyle = new GeoLabelStyle();
        color = new EmpGeoColor(1.0, 0, 0, 0);
        labelStyle.setColor(color);
        labelStyle.setSize(8.0);
        labelStyle.setJustification(IGeoLabelStyle.Justification.LEFT);
        labelStyle.setFontFamily("Ariel");
        labelStyle.setTypeface(IGeoLabelStyle.Typeface.REGULAR);
        labelStyleMap.put(MGRS_GRID_BOX_NORTH_VALUE, labelStyle);

        // MGRS easting values.
        labelStyle = new GeoLabelStyle();
        labelStyle.setColor(color);
        labelStyle.setSize(8.0);
        labelStyle.setJustification(IGeoLabelStyle.Justification.LEFT);
        labelStyle.setFontFamily("Ariel");
        labelStyle.setTypeface(IGeoLabelStyle.Typeface.REGULAR);
        labelStyleMap.put(MGRS_GRID_BOX_EAST_VALUE, labelStyle);
    }

    @Override
    protected void processViewChange(EmpBoundingBox mapBounds, ICamera camera, int viewWidth, int viewHeight) {
        double viewWidthInMeters = mapBounds.widthAcrossCenter();
        double metersPerPixel = viewWidthInMeters / viewWidth;
        double metersInOneEighthOfAnInch = metersPerPixel * PIXELS_PER_INCH / 8.0;
        double metersInOne4thOfAnInch = metersPerPixel * PIXELS_PER_INCH / 4.0;

        clearFeatureList();

        if (metersInOneEighthOfAnInch <= 2) {
            Log.i(TAG, "1 threshold. " + metersInOneEighthOfAnInch);
            createMGRSGridZones(mapBounds, camera, viewWidth, viewHeight, false);
            createMGRSGrid(mapBounds, camera, metersPerPixel, metersInOneEighthOfAnInch, MGRS_1_METER_GRID);
            displayGridLabel("MGRS 1m Grid", mapBounds, metersPerPixel);
        } else if (metersInOneEighthOfAnInch <= 17) {
            Log.i(TAG, "10 threshold. " + metersInOneEighthOfAnInch);
            createMGRSGridZones(mapBounds, camera, viewWidth, viewHeight, false);
            createMGRSGrid(mapBounds, camera, metersPerPixel, metersInOneEighthOfAnInch, MGRS_10_METER_GRID);
            displayGridLabel("MGRS 10m Grid", mapBounds, metersPerPixel);
        } else if (metersInOneEighthOfAnInch <= 160) {
            Log.i(TAG, "100 threshold. " + metersInOneEighthOfAnInch);
            createMGRSGridZones(mapBounds, camera, viewWidth, viewHeight, false);
            createMGRSGrid(mapBounds, camera, metersPerPixel, metersInOneEighthOfAnInch, MGRS_100_METER_GRID);
            displayGridLabel("MGRS 100m Grid", mapBounds, metersPerPixel);
        } else if (metersInOneEighthOfAnInch <= 1600) {
            Log.i(TAG, "1K threshold. " + metersInOneEighthOfAnInch);
            createMGRSGridZones(mapBounds, camera, viewWidth, viewHeight, false);
            createMGRSGrid(mapBounds, camera, metersPerPixel, metersInOneEighthOfAnInch, MGRS_1K_METER_GRID);
            displayGridLabel("MGRS 1Km Grid", mapBounds, metersPerPixel);
        } else if (metersInOneEighthOfAnInch <= 7500) {
            Log.i(TAG, "10K threshold. " + metersInOneEighthOfAnInch);
            createMGRSGridZones(mapBounds, camera, viewWidth, viewHeight, false);
            createMGRSGrid(mapBounds, camera, metersPerPixel, metersInOneEighthOfAnInch, MGRS_10K_METER_GRID);
            displayGridLabel("MGRS 10Km Grid", mapBounds, metersPerPixel);
        } else if ((metersInOneEighthOfAnInch * 3) <= MGRS_100K_METER_GRID) {
            Log.i(TAG, "100K threshold. " + metersInOneEighthOfAnInch);
            createMGRSGridZones(mapBounds, camera, viewWidth, viewHeight, false);
            createMGRSGrid(mapBounds, camera, metersPerPixel, metersInOneEighthOfAnInch, MGRS_100K_METER_GRID);
            displayGridLabel("MGRS 100Km Grid", mapBounds, metersPerPixel);
        } else if (metersInOneEighthOfAnInch <= MGRS_100K_METER_GRID) {
            Log.i(TAG, "GZD threshold. " + metersInOneEighthOfAnInch);
            createMGRSGridZones(mapBounds, camera, viewWidth, viewHeight, true);
            displayGridLabel("MGRS GZD", mapBounds, metersPerPixel);
        } else if ((metersInOneEighthOfAnInch * 4) <= (MGRS_100K_METER_GRID * 7)) {
            Log.i(TAG, "UTM grid. " + metersInOneEighthOfAnInch);
            super.processViewChange(mapBounds, camera, viewWidth, viewHeight);
        } else {
            Log.i(TAG, "Grid off. " + viewWidthInMeters);
            // The grid turns off.
        }
    }

    private void createMGRSGrid(EmpBoundingBox mapBounds, ICamera camera, double metersPerPixel, double metersInOneEighthOfAnInch, int gridSize) {
        int longitude;
        int latitude;
        int eastZoneNumber;
        EmpBoundingBox gridZoneBounds = new EmpBoundingBox();
        UTMCoordinate gridZoneUTMCoord = new UTMCoordinate();
        // UTM coordinates are used by the methods called by this method.
        // We allocate this list here so they can be allocated once per execution.
        UTMCoordinate[] tempUTMCoordList = {new UTMCoordinate(), new UTMCoordinate(), new UTMCoordinate(), new UTMCoordinate(), new UTMCoordinate()};
        EmpBoundingBox tempBoundingBox = new EmpBoundingBox();

        if (0 == UTMCoordinate.getZoneNumber(mapBounds.centerLatitude(), mapBounds.centerLongitude())) {
            // The bounding box is in one of the poles. UPS
            // TODO add UPS.
            return;
        }

        double minLatitude = Math.floor(mapBounds.south());
        double maxLatitude = Math.ceil((mapBounds.north()));
        double minLongitude = Math.floor(mapBounds.west());
        double maxLongitude = Math.ceil((mapBounds.east()));

        eastZoneNumber = (UTMCoordinate.getZoneNumber(minLatitude, maxLongitude) % 60) + 1;

        UTMCoordinate.fromLatLong(minLatitude, minLongitude, gridZoneUTMCoord);
        latitude = UTMCoordinate.getZoneSouthLatitude(gridZoneUTMCoord.getZoneNumber(), gridZoneUTMCoord.getZoneLetter());
        longitude = UTMCoordinate.getZoneWestLongitude(gridZoneUTMCoord.getZoneNumber(), gridZoneUTMCoord.getZoneLetter());
        // This will set the gridZoneUTMCoord to the south west corner of the UTM grid zone that is at the south west corner of the viewing area.
        UTMCoordinate.fromLatLong(latitude, longitude, gridZoneUTMCoord);

        latitude = (int) minLatitude;

        // Process grid zones
        while (latitude <= maxLatitude) {
            gridZoneBounds.setSouth(gridZoneUTMCoord.getZoneSouthLatitude());
            gridZoneBounds.setNorth(gridZoneBounds.getSouth() + gridZoneUTMCoord.getGridZoneHeightInDegrees());
            while (gridZoneUTMCoord.getZoneNumber() != eastZoneNumber) {
                //Log.i(TAG, "Processing Zone: " + gridZoneUTMCoord.getZoneNumber() + gridZoneUTMCoord.getZoneLetter() + " width:" + gridZoneUTMCoord.getGridZoneWidthInDegrees());
                gridZoneBounds.setWest(gridZoneUTMCoord.getZoneWestLongitude());
                gridZoneBounds.setEast(gridZoneBounds.getWest() + gridZoneUTMCoord.getGridZoneWidthInDegrees());
                createMGRSGridsForGridzone(gridZoneUTMCoord, gridZoneBounds, tempUTMCoordList, mapBounds, mapBounds.intersection(gridZoneBounds, tempBoundingBox), gridSize, metersPerPixel, metersInOneEighthOfAnInch);
                // Set UTMCoordinate to the UTM grid zone to east of the current one.
                UTMCoordinate.fromLatLong(latitude, gridZoneBounds.getEast(), gridZoneUTMCoord);
            }
            latitude = (int) gridZoneBounds.getNorth();
            // This sets gridZoneUTMCoord to the first UTM grid zone in the viewing area of the next row north.
            UTMCoordinate.fromLatLong(latitude, longitude, gridZoneUTMCoord);
        }

        if (gridSize != MGRS_100K_METER_GRID) {
            createMGRSGirdNorthingValues(mapBounds, gridSize, metersPerPixel, metersInOneEighthOfAnInch, tempUTMCoordList);
            createMGRSGirdEastingValues(mapBounds, gridSize, metersPerPixel, metersInOneEighthOfAnInch, tempUTMCoordList);
        }
    }

    private void createMGRSGridParallels(UTMCoordinate gridZoneUTMCoord, UTMCoordinate[] tempUTMCoordList, EmpBoundingBox drawBounds, int gridSize) {
        double latitude;
        IGeoPosition WestPos = null;
        IGeoPosition EastPos = null;
        List<IGeoPosition> positionList;
        IFeature gridObject;
        String letter;
        UTMCoordinate westUTMCoord = tempUTMCoordList[0];
        UTMCoordinate eastUTMCoord = tempUTMCoordList[1];
        boolean northernHemesphere = gridZoneUTMCoord.isNorthernHemisphere();
        int parentGridSize = ((gridSize != MGRS_100K_METER_GRID)? gridSize * 10: MGRS_100K_METER_GRID);
        int tempValue;

        latitude = drawBounds.getSouth();

        // Generate the parallel lines of the box (the horizontal lines).
        UTMCoordinate.fromLatLong(latitude, drawBounds.getWest(), westUTMCoord);
        UTMCoordinate.fromLatLong(latitude, ((drawBounds.getEast() == 180.0)? 179.99999999999: drawBounds.getEast()), eastUTMCoord);

        // Make sure that the northing value is a multiple of gridSize.
        tempValue = (((int) Math.floor(westUTMCoord.getNorthing() / gridSize)) * gridSize);
        if ((int) westUTMCoord.getNorthing() != tempValue) {
            westUTMCoord.setNorthing(tempValue);
        }
        eastUTMCoord.setNorthing(westUTMCoord.getNorthing());
        WestPos = westUTMCoord.toLatLong();

        while (latitude <= drawBounds.getNorth()) {
            positionList = new ArrayList<>();
            positionList.add(WestPos);
            EastPos = eastUTMCoord.toLatLong();
            positionList.add(EastPos);

            if (gridSize == MGRS_100K_METER_GRID) {
                gridObject = createPathFeature(positionList, MGRS_GRID_BOX_PARALLELS);
            } else if ((Math.floor(westUTMCoord.getNorthing() / MGRS_100K_METER_GRID) * MGRS_100K_METER_GRID) == westUTMCoord.getNorthing()) {
                gridObject = createPathFeature(positionList, MGRS_GRID_BOX_PARALLELS);
            } else if ((Math.floor(westUTMCoord.getNorthing() / parentGridSize) * parentGridSize) == westUTMCoord.getNorthing()) {
                gridObject = createPathFeature(positionList, MGRS_GRID_LINE_MAJOR_PARALLELS);
            } else {
                gridObject = createPathFeature(positionList, MGRS_GRID_LINE_MINOR_PARALLELS);
            }

            addFeature(gridObject);

            westUTMCoord.setNorthing(westUTMCoord.getNorthing() + gridSize);
            eastUTMCoord.setNorthing(westUTMCoord.getNorthing());
            WestPos = westUTMCoord.toLatLong();
            latitude = WestPos.getLatitude();
        }
    }

    private void createMGRSGridMeridians(UTMCoordinate gridZoneUTMCoord, EmpBoundingBox GridZoneBounds, UTMCoordinate[] tempUTMCoordList, EmpBoundingBox drawBounds, int gridSize) {
        List<IGeoPosition> positionList;
        IFeature gridObject;
        int tempValue;
        double tempNorthing;
        IGeoPosition TopPos = new GeoPosition();
        IGeoPosition BottomPos = new GeoPosition();
        IGeoPosition tempPos = new GeoPosition();
        UTMCoordinate tempUTMCoord1 = tempUTMCoordList[0];
        UTMCoordinate tempUTMCoord2 = tempUTMCoordList[1];
        EmpBoundingBox tempBounds = new EmpBoundingBox();
        int parentGridSize = ((gridSize != MGRS_100K_METER_GRID)? gridSize * 10: MGRS_100K_METER_GRID);

        UTMCoordinate.fromLatLong(drawBounds.getNorth(), drawBounds.getWest(), tempUTMCoord1);
        UTMCoordinate.fromLatLong(drawBounds.getSouth(), drawBounds.getWest(), tempUTMCoord2);

        // Make sure that the easting value is a multiple of grid size.
        if (gridZoneUTMCoord.getZoneLetter().charAt(0) < 'N') {
            // We are in the southern hemisphere so we adjust the northern coordinate because it is wider.
            tempValue = (int) Math.floor(tempUTMCoord1.getEasting() / gridSize) * gridSize;
            if ((int) tempUTMCoord1.getEasting() != tempValue) {
                tempUTMCoord1.setEasting(tempValue);
            }
            tempUTMCoord1.setEasting(tempUTMCoord1.getEasting() + gridSize);
            tempUTMCoord2.setEasting(tempUTMCoord1.getEasting());
        } else {
            // We are in the northern hemisphere so we adjust the southern coordinate because it is wider.
            tempValue = (int) Math.floor(tempUTMCoord2.getEasting() / gridSize) * gridSize;
            if ((int) tempUTMCoord2.getEasting() != tempValue) {
                tempUTMCoord2.setEasting(tempValue);
            }
            tempUTMCoord2.setEasting(tempUTMCoord2.getEasting() + gridSize);
            tempUTMCoord1.setEasting(tempUTMCoord2.getEasting());
        }

        tempPos.setAltitude(0.0);

        tempUTMCoord1.toLatLong(TopPos);
        tempUTMCoord2.toLatLong(BottomPos);
        tempBounds.setNorth(TopPos.getLatitude());
        tempBounds.setSouth(BottomPos.getLatitude());
        tempBounds.setWest(Math.min(TopPos.getLongitude(), BottomPos.getLongitude()));
        tempBounds.setEast(Math.max(TopPos.getLongitude(), BottomPos.getLongitude()));

        while (drawBounds.intersects(tempBounds)) {
            // The first and last meridian in a grid zone may not extend from the north to the south of the grid zone.
            if (!GridZoneBounds.contains(GridZoneBounds.centerLatitude(), TopPos.getLongitude())) {
                // This one does not reach the northern latitude of the grid zone.
                // Adjust the latitude.
                tempNorthing = tempUTMCoord1.getNorthing();
                tempPos.setLatitude(TopPos.getLatitude());
                tempPos.setLongitude(TopPos.getLongitude());
                while (!GridZoneBounds.contains(tempPos.getLatitude(), tempPos.getLongitude()) && (tempPos.getLatitude() > GridZoneBounds.getSouth())) {
                    tempUTMCoord1.setNorthing((Math.floor(tempUTMCoord1.getNorthing() / gridSize) - 1) * gridSize);
                    tempUTMCoord1.toLatLong(tempPos);
                }
                TopPos.setLatitude(tempPos.getLatitude());
                TopPos.setLongitude(tempPos.getLongitude());
                tempUTMCoord1.setNorthing(tempNorthing);
            }
            if (!GridZoneBounds.contains(GridZoneBounds.centerLatitude(), BottomPos.getLongitude())) {
                // This one does not reach the southern latitude of the grid zone.
                // Adjust the latitude.
                tempNorthing = tempUTMCoord2.getNorthing();
                tempPos.setLatitude(BottomPos.getLatitude());
                tempPos.setLongitude(BottomPos.getLongitude());
                while (!GridZoneBounds.contains(tempPos.getLatitude(), tempPos.getLongitude()) && (tempPos.getLatitude() < GridZoneBounds.getNorth())) {
                    tempUTMCoord2.setNorthing((Math.floor(tempUTMCoord2.getNorthing() / gridSize) + 1) * gridSize);
                    tempUTMCoord2.toLatLong(tempPos);
                }
                BottomPos.setLatitude(tempPos.getLatitude());
                BottomPos.setLongitude(tempPos.getLongitude());
                tempUTMCoord2.setNorthing(tempNorthing);
            }
            positionList = new ArrayList<>();
            positionList.add(GridLineUtils.newPosition(TopPos.getLatitude(), TopPos.getLongitude(), 0.0));
            positionList.add(GridLineUtils.newPosition(BottomPos.getLatitude(), BottomPos.getLongitude(), 0.0));
            if (gridSize == MGRS_100K_METER_GRID) {
                gridObject = createPathFeature(positionList, MGRS_GRID_BOX_MERIDIAN);
            } else if ((Math.floor(tempUTMCoord1.getEasting() / MGRS_100K_METER_GRID) * MGRS_100K_METER_GRID) == tempUTMCoord1.getEasting()) {
                gridObject = createPathFeature(positionList, MGRS_GRID_BOX_MERIDIAN);
            } else if ((Math.floor(tempUTMCoord1.getEasting() / parentGridSize) * parentGridSize) == tempUTMCoord1.getEasting()) {
                gridObject = createPathFeature(positionList, MGRS_GRID_LINE_MAJOR_MERIDIAN);
            } else {
                gridObject = createPathFeature(positionList, MGRS_GRID_LINE_MINOR_MERIDIAN);
            }
            addFeature(gridObject);

            tempUTMCoord1.setEasting(tempUTMCoord1.getEasting() + gridSize);
            tempUTMCoord2.setEasting(tempUTMCoord2.getEasting() + gridSize);

            tempUTMCoord1.toLatLong(TopPos);
            tempUTMCoord2.toLatLong(BottomPos);

            tempBounds.setNorth(TopPos.getLatitude());
            tempBounds.setSouth(BottomPos.getLatitude());
            tempBounds.setWest(Math.min(TopPos.getLongitude(), BottomPos.getLongitude()));
            tempBounds.setEast(Math.max(TopPos.getLongitude(), BottomPos.getLongitude()));
        }
    }

    private void createMGRSGirdNorthingValues(EmpBoundingBox mapBounds, int gridSize,
            double metersPerPixel, double metersInOneEighthOfAnInch, UTMCoordinate[] tempUTMCoordList) {
        int northValue;
        double labelLongitude;
        int tempValue;
        String strValue;
        double metersNorth;
        IFeature gridObject;
        IGeoPosition valuePos = new GeoPosition();
        UTMCoordinate northValueUTMCoord = tempUTMCoordList[0];
        UTMCoordinate gridZoneUTMCoord = tempUTMCoordList[1];
        UTMCoordinate tempUTMCoord = tempUTMCoordList[2];
        int gridIncrement = ((gridSize == MGRS_10K_METER_GRID)? gridSize: ((metersInOneEighthOfAnInch < gridSize)? gridSize: gridSize * 10));
        int displayDigits;
        int mgrsGridBoxNorthingStart;
        EmpBoundingBox gridZoneBounds = new EmpBoundingBox();
        EmpBoundingBox gridZoneInMapViewBounds = new EmpBoundingBox();

        displayDigits = this.gridValueDigits.get(gridSize);

        UTMCoordinate.fromLatLong(Math.max(mapBounds.getSouth(), -80.0), mapBounds.getWest(), gridZoneUTMCoord);

        gridZoneBounds.setSouth(gridZoneUTMCoord.getZoneSouthLatitude());
        gridZoneBounds.setNorth(gridZoneBounds.getSouth() + gridZoneUTMCoord.getGridZoneHeightInDegrees());
        gridZoneBounds.setWest(gridZoneUTMCoord.getZoneWestLongitude());
        gridZoneBounds.setEast(gridZoneBounds.getWest() + gridZoneUTMCoord.getGridZoneWidthInDegrees());

        // Make sure that this grid zone has enough horizontal space to write in.
        if (null != gridZoneBounds.intersection(mapBounds, gridZoneInMapViewBounds)) {
            if (Math.floor(gridZoneInMapViewBounds.widthAcrossCenter() / metersPerPixel) < (4 * getCharacterPixelWidth(MGRS_GRID_BOX_NORTH_VALUE))) {
                gridZoneUTMCoord.setZoneNumber((gridZoneUTMCoord.getZoneNumber() % 60) + 1);

                gridZoneBounds.setSouth(gridZoneUTMCoord.getZoneSouthLatitude());
                gridZoneBounds.setNorth(gridZoneBounds.getSouth() + gridZoneUTMCoord.getGridZoneHeightInDegrees());
                gridZoneBounds.setWest(gridZoneUTMCoord.getZoneWestLongitude());
                gridZoneBounds.setEast(gridZoneBounds.getWest() + gridZoneUTMCoord.getGridZoneWidthInDegrees());

                UTMCoordinate.fromLatLong(mapBounds.getSouth(), gridZoneBounds.getWest(), gridZoneUTMCoord);
            }
        }

        // Make sure that the mgrs grid box start is a multiple of 100K.
        mgrsGridBoxNorthingStart = (int) Math.floor(gridZoneUTMCoord.getNorthing() / MGRS_100K_METER_GRID) * MGRS_100K_METER_GRID;

        northValueUTMCoord.copyFrom(gridZoneUTMCoord);

        // Make sure that the north value northing is at a grid increment value.
        tempValue = (int) Math.floor(northValueUTMCoord.getNorthing() / gridIncrement) * gridIncrement;
        if ((int) northValueUTMCoord.getNorthing() != tempValue) {
            northValueUTMCoord.setNorthing(tempValue);
        }

        northValueUTMCoord.setEasting(northValueUTMCoord.getEasting() + (metersInOneEighthOfAnInch * 2));
        tempUTMCoord.copyFrom(northValueUTMCoord);

        tempUTMCoord.toLatLong(valuePos);
        labelLongitude = valuePos.getLongitude();

        while (valuePos.getLatitude() < mapBounds.getNorth()) {
            metersNorth = (northValueUTMCoord.getNorthing() - mgrsGridBoxNorthingStart);
            if (metersNorth >= 0) {
                northValue = (int) Math.floor(metersNorth / gridIncrement);

                if (northValue >= (MGRS_100K_METER_GRID / gridSize)) {
                    break;
                }

                // we use the gird zone center latitude to just check the position longitude.
                if (gridZoneBounds.contains(gridZoneBounds.centerLatitude(), labelLongitude)) {
                    strValue = String.format("%0" + displayDigits + "d", northValue);
                    gridObject = createLabelFeature(GridLineUtils.newPosition(valuePos.getLatitude(), labelLongitude, 0.0), strValue, MGRS_GRID_BOX_NORTH_VALUE);
                    addFeature(gridObject);
                }
            }

            tempUTMCoord.setNorthing(tempUTMCoord.getNorthing() + gridIncrement);
            northValueUTMCoord.setNorthing(northValueUTMCoord.getNorthing() + gridIncrement);

            if (((int) northValueUTMCoord.getNorthing() % MGRS_100K_METER_GRID) == 0) {
                mgrsGridBoxNorthingStart = (int) northValueUTMCoord.getNorthing();
            }
            tempUTMCoord.toLatLong(valuePos);
        }
    }

    private void createMGRSGirdEastingValues(EmpBoundingBox mapBounds, int gridSize,
            double metersPerPixel, double metersInOneEighthOfAnInch, UTMCoordinate[] tempUTMCoordList) {
        int eastValue;
        int tempValue;
        String strValue;
        double metersEast;
        double labelLatitude;
        double widthOfGridZoneAtLabelLat;
        double runningMeterCount;
        double northingValue;
        IFeature gridObject;
        int zoneNumber;
        String zoneLetter;
        IGeoPosition valuePos = new GeoPosition();
        IGeoPosition tempPos = new GeoPosition();
        UTMCoordinate eastValueUTMCoord = tempUTMCoordList[0];
        UTMCoordinate gridZoneUTMCoord = tempUTMCoordList[1];
        int gridIncrement = ((gridSize == MGRS_10K_METER_GRID)? gridSize: ((metersInOneEighthOfAnInch < gridSize)? gridSize: gridSize * 10));
        int displayDigits;
        int mgrsGridBoxEastingStart;
        EmpBoundingBox gridZoneBounds = new EmpBoundingBox();
        EmpBoundingBox gridZoneInMapViewBounds = new EmpBoundingBox();

        displayDigits = this.gridValueDigits.get(gridSize);

        UTMCoordinate.fromLatLong(Math.max(mapBounds.getSouth(), -80.0), mapBounds.getWest(), gridZoneUTMCoord);
        eastValueUTMCoord.copyFrom(gridZoneUTMCoord);

        gridZoneBounds.setSouth(gridZoneUTMCoord.getZoneSouthLatitude());
        gridZoneBounds.setNorth(gridZoneBounds.getSouth() + gridZoneUTMCoord.getGridZoneHeightInDegrees());
        gridZoneBounds.setWest(gridZoneUTMCoord.getZoneWestLongitude());
        gridZoneBounds.setEast(gridZoneBounds.getWest() + gridZoneUTMCoord.getGridZoneWidthInDegrees());

        // Make sure that this grid zone has enough vertical space to write in.
        if (null != gridZoneBounds.intersection(mapBounds, gridZoneInMapViewBounds)) {
            if (Math.floor(gridZoneInMapViewBounds.heightAcrossCenter() / metersPerPixel) < (4 * getCharacterPixelWidth(MGRS_GRID_BOX_EAST_VALUE))) {
                if (null == gridZoneUTMCoord.getNextLetter()) {
                    // The next letter is the polar region.
                    return;
                }
                gridZoneUTMCoord.setZoneLetter(gridZoneUTMCoord.getNextLetter());

                gridZoneBounds.setSouth(gridZoneUTMCoord.getZoneSouthLatitude());
                gridZoneBounds.setNorth(gridZoneBounds.getSouth() + gridZoneUTMCoord.getGridZoneHeightInDegrees());
                gridZoneBounds.setWest(gridZoneUTMCoord.getZoneWestLongitude());
                gridZoneBounds.setEast(gridZoneBounds.getWest() + gridZoneUTMCoord.getGridZoneWidthInDegrees());

                UTMCoordinate.fromLatLong(gridZoneBounds.getSouth(), mapBounds.getWest(), gridZoneUTMCoord);
                eastValueUTMCoord.copyFrom(gridZoneUTMCoord);
            }
        }

        // Make sure that the east value easting is at a multiple of the increment value.
        tempValue = (int) Math.floor(eastValueUTMCoord.getEasting() / gridIncrement) * gridIncrement;
        if ((int) eastValueUTMCoord.getEasting() != tempValue) {
            eastValueUTMCoord.setEasting(tempValue + gridIncrement);
        }

        // Make sure that the east value northing is at a grid size value.
        tempValue = (int) Math.floor(eastValueUTMCoord.getNorthing() / gridSize) * gridSize;
        if ((int) eastValueUTMCoord.getNorthing() != tempValue) {
            eastValueUTMCoord.setNorthing(tempValue + gridSize);
        }
        eastValueUTMCoord.setNorthing(eastValueUTMCoord.getNorthing() + (metersInOneEighthOfAnInch * 2));

        eastValueUTMCoord.toLatLong(valuePos);

        // Make sure that the initial position is in the map view area.
        if (!mapBounds.contains(valuePos.getLatitude(), valuePos.getLongitude())) {
            if (gridIncrement != gridSize) {
                tempValue = (int) Math.floor(eastValueUTMCoord.getEasting() / gridIncrement) * gridIncrement;
                eastValueUTMCoord.setEasting(tempValue + gridIncrement);
            } else {
                eastValueUTMCoord.setEasting(eastValueUTMCoord.getEasting() + gridSize);
            }
            eastValueUTMCoord.toLatLong(valuePos);
        }
        // Make sure that the mgrs grid box start is a multiple of 100K.
        mgrsGridBoxEastingStart = (int) Math.floor(eastValueUTMCoord.getEasting() / MGRS_100K_METER_GRID) * MGRS_100K_METER_GRID;

        // All easting label will be placed at this latitude.
        labelLatitude = valuePos.getLatitude();
        northingValue = eastValueUTMCoord.getNorthing();

        tempPos.setLatitude(labelLatitude);
        tempPos.setLongitude(gridZoneBounds.getWest() + gridZoneUTMCoord.getGridZoneWidthInDegrees());
        tempPos.setAltitude(0.0);
        widthOfGridZoneAtLabelLat = GeoLibrary.computeDistanceBetween(valuePos, tempPos) + eastValueUTMCoord.getEasting() - mgrsGridBoxEastingStart;
        runningMeterCount = eastValueUTMCoord.getEasting() - mgrsGridBoxEastingStart;

        while (mapBounds.contains(labelLatitude, valuePos.getLongitude())) {
            metersEast = (eastValueUTMCoord.getEasting() - mgrsGridBoxEastingStart);
            if (metersEast >= 0) {
                eastValue = (int) Math.floor(metersEast / gridSize);

                // we use the gird zone center longitude to just check the position longitude.
                if (gridZoneBounds.contains(gridZoneBounds.centerLatitude(), valuePos.getLongitude())) {
                    strValue = String.format("%0" + displayDigits + "d", eastValue);
                    gridObject = createLabelFeature(GridLineUtils.newPosition(labelLatitude, valuePos.getLongitude(), 0.0), strValue, MGRS_GRID_BOX_EAST_VALUE);
                    addFeature(gridObject);
                }
            }
            eastValueUTMCoord.setEasting(eastValueUTMCoord.getEasting() + gridIncrement);
            runningMeterCount += gridIncrement;

            if (((int) eastValueUTMCoord.getEasting() % MGRS_100K_METER_GRID) == 0) {
                mgrsGridBoxEastingStart = (int) eastValueUTMCoord.getEasting();
            }

            //eastValueUTMCoord.toLatLong(valuePos);
            // Now check to see if we crossed the zone.
            //zoneNumber = UTMCoordinate.getZoneNumber(valuePos.getLatitude(), valuePos.getLongitude());
            //if  (eastValueUTMCoord.getZoneNumber() != zoneNumber) {
            if  (runningMeterCount >= widthOfGridZoneAtLabelLat) {
                gridZoneUTMCoord.setZoneNumber((gridZoneUTMCoord.getZoneNumber() % 60) + 1);
                UTMCoordinate.fromLatLong(labelLatitude, gridZoneUTMCoord.getZoneWestLongitude(), gridZoneUTMCoord);

                eastValueUTMCoord.copyFrom(gridZoneUTMCoord);

                gridZoneBounds.setSouth(gridZoneUTMCoord.getZoneSouthLatitude());
                gridZoneBounds.setNorth(gridZoneBounds.getSouth() + gridZoneUTMCoord.getGridZoneHeightInDegrees());
                gridZoneBounds.setWest(gridZoneUTMCoord.getZoneWestLongitude());
                gridZoneBounds.setEast(gridZoneBounds.getWest() + gridZoneUTMCoord.getGridZoneWidthInDegrees());

                //eastValueUTMCoord.setNorthing(northingValue);

                mgrsGridBoxEastingStart = (int) Math.floor(eastValueUTMCoord.getEasting() / MGRS_100K_METER_GRID) * MGRS_100K_METER_GRID;
                tempValue = (int) Math.floor(eastValueUTMCoord.getEasting() / gridIncrement) * gridIncrement;
                if ((int) eastValueUTMCoord.getEasting() != tempValue) {
                    eastValueUTMCoord.setEasting(tempValue);
                }
                eastValueUTMCoord.setEasting(eastValueUTMCoord.getEasting() + gridIncrement);

                eastValueUTMCoord.toLatLong(valuePos);
                if (!mapBounds.contains(labelLatitude, valuePos.getLongitude())) {
                    if (gridIncrement != gridSize) {
                        tempValue = (int) Math.floor(eastValueUTMCoord.getEasting() / gridIncrement) * gridIncrement;
                        eastValueUTMCoord.setEasting(tempValue + gridIncrement);
                    } else {
                        eastValueUTMCoord.setEasting(eastValueUTMCoord.getEasting() + gridSize);
                    }
                    eastValueUTMCoord.toLatLong(valuePos);
                }

                tempPos.setLatitude(labelLatitude);
                tempPos.setLongitude(gridZoneBounds.getWest() + gridZoneUTMCoord.getGridZoneWidthInDegrees());
                tempPos.setAltitude(0.0);
                widthOfGridZoneAtLabelLat = GeoLibrary.computeDistanceBetween(valuePos, tempPos) + eastValueUTMCoord.getEasting() - mgrsGridBoxEastingStart;
                runningMeterCount = eastValueUTMCoord.getEasting() - mgrsGridBoxEastingStart;
            } else {
                eastValueUTMCoord.toLatLong(valuePos);
            }
        }
    }

    private void createMGRSGridLabels(UTMCoordinate gridZoneUTMCoord, EmpBoundingBox gridZoneBounds, UTMCoordinate[] tempUTMCoordList,
            EmpBoundingBox mapBounds, EmpBoundingBox drawBounds, int gridSize, double metersPerPixel, double metersInOneEighthOfAnInch) {
        IFeature gridObject;
        String mgrsGridLabel;
        IGeoPosition labelPos;
        IGeoPosition mgrs100KGridBoxSWPos = new GeoPosition();
        IGeoPosition tempPos = new GeoPosition();
        double initialEasting;
        UTMCoordinate mgrsGridBoxCoord = tempUTMCoordList[0];
        UTMCoordinate mgrsGridBoxInZoneWEUTMCoord = tempUTMCoordList[1];
        UTMCoordinate tempUTMCoord = tempUTMCoordList[2];
        EmpBoundingBox mgrs100KGridBounds = new EmpBoundingBox();
        EmpBoundingBox labelBounds = new EmpBoundingBox();
        double gridLabelPixelWidth = (getCharacterPixelWidth(MGRS_GRID_BOX_LABEL_CENTERED) * 2.0);
        double gridLabelPixelHeight = getCharacterPixelWidth(MGRS_GRID_BOX_LABEL_CENTERED);
        double gridZoneLabelPixelWidth = (getCharacterPixelWidth(MGRS_GRID_ZONE_LABEL) * 3.0);

        UTMCoordinate.fromLatLong(drawBounds.getSouth(), drawBounds.getWest(), mgrsGridBoxCoord);

        if (null != gridZoneBounds.intersection(drawBounds, labelBounds)) {
            if ((labelBounds.heightAcrossCenter() / metersPerPixel) > gridZoneLabelPixelWidth) {
                if ((labelBounds.widthAcrossCenter() / metersPerPixel) > gridZoneLabelPixelWidth) {
                    labelPos = GridLineUtils.newPosition((labelBounds.getNorth() + labelBounds.centerLatitude()) / 2.0, labelBounds.centerLongitude(), 0);
                    gridObject = createLabelFeature(labelPos, gridZoneUTMCoord.getZoneNumber() + gridZoneUTMCoord.getZoneLetter(), MGRS_GRID_ZONE_LABEL);
                    addFeature(gridObject);
                }
            }
        }

        initialEasting = mgrsGridBoxCoord.getEasting();
        mgrs100KGridBoxSWPos.setLatitude(drawBounds.getSouth());
        mgrs100KGridBoxSWPos.setLongitude(drawBounds.getWest());
        mgrs100KGridBoxSWPos.setAltitude(0.0);

        while (mgrs100KGridBoxSWPos.getLatitude() <= drawBounds.getNorth()) {
            while (drawBounds.contains(mgrs100KGridBoxSWPos.getLatitude(), mgrs100KGridBoxSWPos.getLongitude())) {
                // Copy the UTM coordinate.
                mgrsGridBoxInZoneWEUTMCoord.copyFrom(mgrsGridBoxCoord);

                // Get the lat/lon of the south west of the 100K area.
                mgrsGridBoxInZoneWEUTMCoord.toLatLong(tempPos);
                // Set the south of area.
                mgrs100KGridBounds.setSouth(tempPos.getLatitude());
                // Make sure that the west is the larger of the 100KGrid and Grid zone west value.
                mgrs100KGridBounds.setWest(Math.max(tempPos.getLongitude(), gridZoneBounds.getWest()));
                // Increase the northing and easting by 100K
                mgrsGridBoxInZoneWEUTMCoord.setNorthing((Math.floor(mgrsGridBoxInZoneWEUTMCoord.getNorthing() / MGRS_100K_METER_GRID) + 1) * MGRS_100K_METER_GRID);
                mgrsGridBoxInZoneWEUTMCoord.setEasting((Math.floor(mgrsGridBoxInZoneWEUTMCoord.getEasting() / MGRS_100K_METER_GRID) + 1) * MGRS_100K_METER_GRID);
                // Get the lat/lon of the north east of the 100K area.
                mgrsGridBoxInZoneWEUTMCoord.toLatLong(tempPos);
                // Set the north of the area as the smaller of the pos latitude and the draw area north.
                mgrs100KGridBounds.setNorth(Math.min(tempPos.getLatitude(), drawBounds.getNorth()));
                //mgrs100KGridBounds.setNorth(tempPos.getLatitude());
                // Make sure that the east is the smaller of the 100KGrid and Grid zone east value.
                mgrs100KGridBounds.setEast(Math.min(tempPos.getLongitude(), gridZoneBounds.getEast()));

                // Make sure that the west is the larger of the 100KGrid, Grid zone, or draw bounds west value.
                mgrs100KGridBounds.setWest(Math.max(mgrs100KGridBounds.getWest(), drawBounds.getWest()));
                // Make sure that the east is the smaller of the 100KGrid, draw bound or Grid zone east value.
                mgrs100KGridBounds.setEast(Math.min(mgrs100KGridBounds.getEast(), drawBounds.getEast()));

                // In some cases the mgrs100KGridBounds get a west > than the east
                if (mgrs100KGridBounds.getWest() > mgrs100KGridBounds.getEast()) {
                    // So we set them equal.
                    mgrs100KGridBounds.setWest(mgrs100KGridBounds.getEast());
                }
                // If the 100K grid bound intersects with the map bounds place it in label bounds.
                if (null != mgrs100KGridBounds.intersection(mapBounds, labelBounds)) {
                    // If the label fits in the bounds, create the label.
                    if ((labelBounds.heightAcrossCenter() / metersPerPixel) > gridLabelPixelHeight) {
                        if ((labelBounds.widthAcrossCenter() / metersPerPixel) > gridLabelPixelWidth) {
                            labelPos = GridLineUtils.newPosition(labelBounds.centerLatitude(), labelBounds.centerLongitude(), 0);
                            mgrsGridLabel = get100kID(mgrsGridBoxCoord.getEasting(), mgrsGridBoxCoord.getNorthing(), mgrsGridBoxCoord.getZoneNumber());
                            //Log.i(TAG, "Label " + mgrsGridBoxCoord.getZoneNumber() + mgrsGridBoxCoord.getZoneLetter() + " " + mgrsGridLabel + "  lat/lng " + labelPos.getLatitude() + "/" + labelPos.getLongitude());
                            gridObject = createLabelFeature(labelPos, mgrsGridLabel, MGRS_GRID_BOX_LABEL_CENTERED);
                            addFeature(gridObject);
                        }
                    }
                }

                mgrsGridBoxCoord.setEasting((Math.floor(mgrsGridBoxCoord.getEasting() / MGRS_100K_METER_GRID) + 1) * MGRS_100K_METER_GRID);
                mgrsGridBoxCoord.toLatLong(mgrs100KGridBoxSWPos);
            }
            mgrsGridBoxCoord.setNorthing((Math.floor(mgrsGridBoxCoord.getNorthing() / MGRS_100K_METER_GRID) + 1) * MGRS_100K_METER_GRID);
            mgrsGridBoxCoord.setEasting(initialEasting);
            mgrsGridBoxCoord.toLatLong(mgrs100KGridBoxSWPos);
            mgrs100KGridBoxSWPos.setLongitude(drawBounds.getWest());
            UTMCoordinate.fromLatLong(mgrs100KGridBoxSWPos.getLatitude(), mgrs100KGridBoxSWPos.getLongitude(), tempUTMCoord);
            mgrsGridBoxCoord.setEasting(tempUTMCoord.getEasting());
        }
    }

    private void createMGRSGridsForGridzone(UTMCoordinate gridZoneUTMCoord, EmpBoundingBox GridZoneBounds, UTMCoordinate[] tempUTMCoordList,
            EmpBoundingBox mapBounds, EmpBoundingBox drawBounds, int gridSize, double metersPerPixel, double metersInOneEighthOfAnInch) {

        if (null == drawBounds) {
            return;
        }

        // Within the UTM grid zone we draw the MGRS grid parallels first, then meridians, then the ID from the left to right, bottom up.
        createMGRSGridParallels(gridZoneUTMCoord, tempUTMCoordList, drawBounds, gridSize);

        // Generate the meridian lines of the box (the vertical lines).
        createMGRSGridMeridians(gridZoneUTMCoord, GridZoneBounds, tempUTMCoordList, drawBounds, gridSize);

        // Now generate the MGRS grid IDs.
        createMGRSGridLabels(gridZoneUTMCoord, GridZoneBounds, tempUTMCoordList, mapBounds, drawBounds, gridSize, metersPerPixel, metersInOneEighthOfAnInch);
    }

    private void createMGRSGridZones(EmpBoundingBox mapBounds, ICamera camera, int viewWidth, int viewHeight, boolean displayLabels) {
        double longitude;
        double latitude;
        int intLongitude;
        int startZoneIndex;
        int endZoneIndex;
        int zoneIndex;
        int minRow;
        int maxRow;
        int iIndex;
        List<IGeoPosition> positionList;
        IFeature gridObject;
        EmpBoundingBox labelBounds = new EmpBoundingBox();
        double metersPerPixel = mapBounds.widthAcrossCenter() / viewWidth;

        double minLatitude = Math.floor(mapBounds.south());
        double maxLatitude = Math.ceil((mapBounds.north()));

        startZoneIndex = (int) Math.floor((mapBounds.west() + 180) / 6.0);
        endZoneIndex = (int) Math.ceil((mapBounds.east() + 180) / 6.0);

        double minLongitude = (double) ((startZoneIndex * 6) - 180);
        double maxLongitude = (double) ((endZoneIndex * 6) - 180);

        zoneIndex = startZoneIndex - 1;
        do {
            zoneIndex = ++zoneIndex % 60;
            intLongitude = (zoneIndex * 6) - 180;
            longitude = (double) intLongitude;
            positionList = new ArrayList<>();

            // Meridian
            latitude = Math.max(minLatitude, -80.0);
            positionList.add(GridLineUtils.newPosition(latitude, longitude, 0));

            if (intLongitude < 6 || intLongitude > 36) {
                latitude = Math.min(maxLatitude, 84.0);
                positionList.add(GridLineUtils.newPosition(latitude, longitude, 0));
            } else if (intLongitude == 6) {
                latitude = Math.min(maxLatitude, 56.0);
                positionList.add(GridLineUtils.newPosition(latitude, longitude, 0));

                if (maxLatitude > 56.0) {
                    gridObject = createPathFeature(positionList, MGRS_GRID_ZONE_MERIDIAN);
                    addFeature(gridObject);

                    latitude = Math.min(maxLatitude, 64.0);
                    positionList = new ArrayList<>();
                    positionList.add(GridLineUtils.newPosition(56.0, longitude - 3.0, 0));
                    positionList.add(GridLineUtils.newPosition(latitude, longitude -3.0, 0));

                    if (maxLatitude > 64.0) {
                        gridObject = createPathFeature(positionList, MGRS_GRID_ZONE_MERIDIAN);
                        addFeature(gridObject);

                        latitude = Math.min(maxLatitude, 72.0);
                        positionList = new ArrayList<>();
                        positionList.add(GridLineUtils.newPosition(64.0, longitude, 0));
                        positionList.add(GridLineUtils.newPosition(latitude, longitude, 0));

                        if (maxLatitude > 72.0) {
                            gridObject = createPathFeature(positionList, MGRS_GRID_ZONE_MERIDIAN);
                            addFeature(gridObject);

                            latitude = Math.min(maxLatitude, 84.0);
                            positionList = new ArrayList<>();
                            positionList.add(GridLineUtils.newPosition(72.0, longitude + 3.0, 0));
                            positionList.add(GridLineUtils.newPosition(latitude, longitude + 3.0, 0));
                        }
                    }
                }
            } else {
                latitude = Math.min(maxLatitude, 72.0);
                positionList.add(GridLineUtils.newPosition(latitude, longitude, 0));

                if (maxLatitude > 72.0) {
                    switch (intLongitude) {
                        case 18:
                        case 30:
                            gridObject = createPathFeature(positionList, MGRS_GRID_ZONE_MERIDIAN);
                            addFeature(gridObject);

                            latitude = Math.min(maxLatitude, 84.0);
                            positionList = new ArrayList<>();
                            positionList.add(GridLineUtils.newPosition(72.0, longitude + 3.0, 0));
                            positionList.add(GridLineUtils.newPosition(latitude, longitude + 3.0, 0));
                            break;
                    }
                }
            }

            gridObject = createPathFeature(positionList, MGRS_GRID_ZONE_MERIDIAN);
            addFeature(gridObject);
        } while (zoneIndex != endZoneIndex);

        // Generate parallels
        minRow = (int) Math.floor((Math.max(minLatitude, -80.0) + 80.0) / 8.0);
        maxRow = (int) Math.ceil((Math.min(maxLatitude, 84.0) + 80.0) / 8.0);

        for (iIndex = minRow; iIndex < maxRow; iIndex++) {
            latitude = (double) ((iIndex * 8) - 80);
            positionList = new ArrayList<>();
            positionList.add(GridLineUtils.newPosition(latitude, minLongitude, 0));
            positionList.add(GridLineUtils.newPosition(latitude, maxLongitude, 0));
            gridObject = createPathFeature(positionList, MGRS_GRID_ZONE_PARALLELS);
            addFeature(gridObject);
        }

        if (displayLabels) {
            // Add The grid zone labels
            zoneIndex = startZoneIndex;
            String zoneLetter;

            while (zoneIndex != endZoneIndex) {
                intLongitude = (zoneIndex * 6) - 180;

                for (iIndex = minRow; iIndex < maxRow; iIndex++) {
                    zoneLetter = latBands.charAt(iIndex) + "";
                    labelBounds.setSouth(UTMCoordinate.getZoneSouthLatitude(zoneIndex + 1, zoneLetter));
                    labelBounds.setWest(UTMCoordinate.getZoneWestLongitude(zoneIndex + 1, zoneLetter));
                    labelBounds.setNorth(labelBounds.getSouth() + UTMCoordinate.getGridZoneHeightInDegrees(zoneIndex + 1, zoneLetter));
                    labelBounds.setEast(labelBounds.getWest() + UTMCoordinate.getGridZoneWidthInDegrees(zoneIndex + 1, zoneLetter));

                    if (null != mapBounds.intersection(labelBounds, labelBounds)) {
                        if ((labelBounds.widthAcrossCenter() / metersPerPixel) > THREE_CHARACTER_12PT_PIXEL_WIDTH) {
                            if ((labelBounds.heightAcrossCenter() / metersPerPixel) > THREE_CHARACTER_12PT_PIXEL_WIDTH) {
                                gridObject = createLabelFeature(GridLineUtils.newPosition(labelBounds.centerLatitude(), labelBounds.centerLongitude(), 0), "" + (zoneIndex + 1) + zoneLetter, MGRS_GRID_ZONE_LABEL);
                                addFeature(gridObject);
                            }
                        }
                    }

                }
                zoneIndex = ++zoneIndex % 60;
            }

            // See if the south UPS is visible.
            if (mapBounds.contains(-85.0, -90.0)) {
                gridObject = createLabelFeature(GridLineUtils.newPosition(-85.0, -90.0, 0), "A", MGRS_GRID_ZONE_LABEL);
                addFeature(gridObject);
            }
            if (mapBounds.contains(-85.0, 90.0)) {
                // Add the south UPS.
                gridObject = createLabelFeature(GridLineUtils.newPosition(-85.0, 90.0, 0), "B", MGRS_GRID_ZONE_LABEL);
                addFeature(gridObject);
            }

            // See if the north UPS is visible.
            if (mapBounds.contains(87.0, -90.0)) {
                gridObject = createLabelFeature(GridLineUtils.newPosition(87.0, -90.0, 0), "Y", MGRS_GRID_ZONE_LABEL);
                addFeature(gridObject);
            }
            if (mapBounds.contains(87.0, 90.0)) {
                // Add the north UPS.
                gridObject = createLabelFeature(GridLineUtils.newPosition(87.0, 90.0, 0), "Z", MGRS_GRID_ZONE_LABEL);
                addFeature(gridObject);
            }
        }
    }

    @Override
    protected void setPathAttributes(Path path, String gridObjectType) {
        if (strokeStyleMap.containsKey(gridObjectType)) {
            path.setStrokeStyle(strokeStyleMap.get(gridObjectType));
        } else {
            super.setPathAttributes(path, gridObjectType);
        }
    }

    @Override
    protected void setLabelAttributes(Text label, String gridObjectType) {
        if (labelStyleMap.containsKey(gridObjectType)) {
            label.setLabelStyle(labelStyleMap.get(gridObjectType));
            switch (gridObjectType) {
                case MGRS_GRID_BOX_EAST_VALUE:
                    label.setAzimuth(-90.0);
                    break;
            }
            double azimuth = label.getAzimuth() - this.currentCamera.getHeading();
            if (azimuth < -360.0) {
                azimuth += 360;
            } else if (azimuth > 360.0) {
                azimuth -= 360.0;
            }
            label.setAzimuth(azimuth);
        } else {
            super.setLabelAttributes(label, gridObjectType);
        }
    }

    /**
     * Given a UTM zone number, figure out the MGRS 100K set it is in.
     *
     * @param zoneNumber An UTM zone number.
     * @return {number} the 100k set the UTM zone is in.
     */
    private int get100kSetForZone(int zoneNumber) {
        int setParm = zoneNumber % NUM_100K_SETS;
        if (setParm == 0) {
            setParm = NUM_100K_SETS;
        }

        return setParm;
    }

    /**
     * Get the two letter 100k designator for a given UTM easting,
     * northing and zone number value.
     *
     * @param easting
     * @param northing
     * @param zoneNumber
     * @return the two letter 100k designator for the given UTM location.
     */
    private String get100kID(double easting, double northing, int zoneNumber) {
        int setParm = get100kSetForZone(zoneNumber);
        int setColumn = (int) Math.floor(easting / 100000);
        int setRow = (int) Math.floor(northing / 100000) % 20;
        return getLetter100kID(setColumn, setRow, setParm);
    }

    /**
     * Get the two-letter MGRS 100k designator given information
     * translated from the UTM northing, easting and zone number.
     *
     * @param column the column index as it relates to the MGRS
     *        100k set spreadsheet, created from the UTM easting.
     *        Values are 1-8.
     * @param row the row index as it relates to the MGRS 100k set
     *        spreadsheet, created from the UTM northing value. Values
     *        are from 0-19.
     * @param parm the set block, as it relates to the MGRS 100k set
     *        spreadsheet, created from the UTM zone. Values are from
     *        1-60.
     * @return two letter MGRS 100k code.
     */
    private String getLetter100kID(int column, int row, int parm) {
        // colOrigin and rowOrigin are the letters at the origin of the set
        int index = parm - 1;
        char colOrigin = SET_ORIGIN_COLUMN_LETTERS.charAt(index);
        char rowOrigin = SET_ORIGIN_ROW_LETTERS.charAt(index);

        // colInt and rowInt are the letters to build to return
        int colInt = colOrigin + column - 1;
        int rowInt = rowOrigin + row;
        boolean rollover = false;

        if (colInt > Z) {
            colInt = colInt - Z + A - 1;
            rollover = true;
        }

        if ((colInt == I) || ((colOrigin < I) && (colInt > I)) || (((colInt > I) || (colOrigin < I)) && rollover)) {
            colInt++;
        }

        if ((colInt == O) || (colOrigin < O && colInt > O) || ((colInt > O || colOrigin < O) && rollover)) {
            colInt++;

            if (colInt == I) {
                colInt++;
            }
        }

        if (colInt > Z) {
            colInt = colInt - Z + A - 1;
        }

        if (rowInt > V) {
            rowInt = rowInt - V + A - 1;
            rollover = true;
        }
        else {
            rollover = false;
        }

        if (((rowInt == I) || ((rowOrigin < I) && (rowInt > I))) || (((rowInt > I) || (rowOrigin < I)) && rollover)) {
            rowInt++;
        }

        if (((rowInt == O) || ((rowOrigin < O) && (rowInt > O))) || (((rowInt > O) || (rowOrigin < O)) && rollover)) {
            rowInt++;

            if (rowInt == I) {
                rowInt++;
            }
        }

        if (rowInt > V) {
            rowInt = rowInt - V + A - 1;
        }

        //String twoLetter = String.fromCharCode(colInt) + String.fromCharCode(rowInt);
        String twoLetter = Character.toString((char) colInt) + Character.toString((char) rowInt);
        return twoLetter;
    }

    private int getCharacterPixelWidth(String featureType) {
        IGeoLabelStyle labelStyle = this.labelStyleMap.get(featureType);

        if (null == labelStyle) {
            return 0;
        }

        return FontUtilities.fontPointsToPixels((int) labelStyle.getSize());
    }

    private void displayGridLabel(String label, EmpBoundingBox mapBounds, double metersPerPixel) {
        IGeoPosition labelPos = this.mapInstance.containerToGeo(new Point(PIXELS_PER_INCH / 8, PIXELS_PER_INCH / 8));
        if (null == labelPos) {
            double charPixelWidth = getCharacterPixelWidth(MGRS_GRID_LABEL) * metersPerPixel;
            labelPos = new GeoPosition();
            labelPos.setLatitude(mapBounds.getNorth());
            labelPos.setLongitude(mapBounds.getWest());
            GeoLibrary.computePositionAt(135.0, charPixelWidth, labelPos, labelPos);
        }

        addFeature(createLabelFeature(labelPos, label, MGRS_GRID_LABEL));
    }
}
