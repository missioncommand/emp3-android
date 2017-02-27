package mil.emp3.core.mapgridlines;

import android.util.Log;

import org.cmapi.primitives.GeoLabelStyle;
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
import mil.emp3.api.utils.GeoLibrary;
import mil.emp3.core.mapgridlines.coordinates.UTMCoordainte;
import mil.emp3.core.mapgridlines.utils.GridLineUtils;
import mil.emp3.mapengine.interfaces.IMapInstance;

/**
 * This class implements the main MGRS grid line generator class.
 */

public class MGRSMapGridLine extends UTMBaseMapGridLine {
    private static final String TAG = MGRSMapGridLine.class.getSimpleName();

    private static final int MGRS_100K_METER_GRID = 100000;
    private static final int MGRS_10K_METER_GRID = 10000;
    private static final int MGRS_1K_METER_GRID = 1000;
    private static final int MGRS_100_METER_GRID = 100;
    private static final int MGRS_10_METER_GRID = 10;
    private static final int MGRS_1_METER_GRID = 1;

    private static final double MAX_MGRS_GRID_THRESHOLD = 5.0e6;
    private static final double MAX_MGRS_GRID_ZONE_THRESHOLD = 2.0e6;
    private static final double MAX_MGRS_100K_GRID_THRESHOLD = MGRS_100K_METER_GRID * 10;
    private static final double MAX_MGRS_10K_GRID_THRESHOLD = MGRS_10K_METER_GRID * 10;
    private static final double MAX_MGRS_1K_GRID_THRESHOLD = MGRS_1K_METER_GRID * 10;
    private static final double MAX_MGRS_100_GRID_THRESHOLD = MGRS_100_METER_GRID * 10;
    private static final double MAX_MGRS_10_GRID_THRESHOLD = MGRS_10_METER_GRID * 10;

    private static final String MGRS_GRID_ZONE_MERIDIAN = "gridzone.meridian";
    private static final String MGRS_GRID_ZONE_PARALLELS = "gridzone.parallels";
    private static final String MGRS_GRID_ZONE_LABEL = "gridzone.label";

    private static final String MGRS_GRID_BOX_MERIDIAN = "MGRS.gridbox.meridian";
    private static final String MGRS_GRID_BOX_PARALLELS = "MGRS.gridbox.parallels";
    private static final String MGRS_GRID_LINE_MERIDIAN = "MGRS.gridline.meridian";
    private static final String MGRS_GRID_LINE_PARALLELS = "MGRS.gridline.parallels";
    private static final String MGRS_GRID_BOX_LABEL = "MGRS.label";
    private static final String MGRS_GRID_BOX_NORTH_VALUE = "MGRS.north.values";
    private static final String MGRS_GRID_BOX_EAST_VALUE = "MGRS.east.values";

    private final Map<String, IGeoStrokeStyle> strokeStyleMap;
    private final Map<String, IGeoLabelStyle> labelStyleMap;

    private final String MGRSColumns = "ABCDEFGHJKLMNPQRSTUVWXYZ"; // 24
    private final String[] MGRSRows = {"ABCDEFGHJKLMNPQRSTUV","FGHJKLMNPQRSTUVABCDE"}; // 20 each. The odd zone use the 1st set the even zone use the 2nd.

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

        // MGRS Grid Box
        color = new EmpGeoColor(0.7, 100, 100, 255);
        strokeStyle = new GeoStrokeStyle();
        strokeStyle.setStrokeColor(color);
        strokeStyle.setStrokeWidth(3.0);
        strokeStyleMap.put(MGRS_GRID_BOX_MERIDIAN, strokeStyle);
        strokeStyleMap.put(MGRS_GRID_BOX_PARALLELS, strokeStyle);

        // MGRS Grid Lines
        color = new EmpGeoColor(0.7, 200, 200, 255);
        strokeStyle = new GeoStrokeStyle();
        strokeStyle.setStrokeColor(color);
        strokeStyle.setStrokeWidth(3.0);
        strokeStyleMap.put(MGRS_GRID_LINE_MERIDIAN, strokeStyle);
        strokeStyleMap.put(MGRS_GRID_LINE_PARALLELS, strokeStyle);

        // MGRS ID
        labelStyle = new GeoLabelStyle();
        color = new EmpGeoColor(0.7, 100, 100, 255);
        labelStyle.setColor(color);
        labelStyle.setSize(12.0);
        labelStyle.setJustification(IGeoLabelStyle.Justification.CENTER);
        labelStyle.setFontFamily("Ariel");
        labelStyle.setTypeface(IGeoLabelStyle.Typeface.BOLD);
        labelStyleMap.put(MGRS_GRID_BOX_LABEL, labelStyle);

        // MGRS northing values
        labelStyle = new GeoLabelStyle();
        color = new EmpGeoColor(1.0, 0, 0, 0);
        labelStyle.setColor(color);
        labelStyle.setSize(8.0);
        labelStyle.setJustification(IGeoLabelStyle.Justification.LEFT);
        labelStyle.setFontFamily("Ariel");
        labelStyle.setTypeface(IGeoLabelStyle.Typeface.BOLD);
        labelStyleMap.put(MGRS_GRID_BOX_NORTH_VALUE, labelStyle);

        // MGRS easting values.
        labelStyle = new GeoLabelStyle();
        labelStyle.setColor(color);
        labelStyle.setSize(8.0);
        labelStyle.setJustification(IGeoLabelStyle.Justification.LEFT);
        labelStyle.setFontFamily("Ariel");
        labelStyle.setTypeface(IGeoLabelStyle.Typeface.BOLD);
        labelStyleMap.put(MGRS_GRID_BOX_EAST_VALUE, labelStyle);
    }

    @Override
    protected void processViewChange(EmpBoundingBox mapBounds, ICamera camera, int viewWidth, int viewHeight) {
        double widthAcrossCenter = Math.floor(mapBounds.widthAcrossCenter());

        if (widthAcrossCenter > MAX_MGRS_GRID_THRESHOLD) {
            super.processViewChange(mapBounds, camera, viewWidth, viewHeight);
            return;
        }

        if (widthAcrossCenter > MAX_MGRS_GRID_ZONE_THRESHOLD) {
            createMGRSGridZones(mapBounds, camera, viewWidth, viewHeight, true);
        } else if (widthAcrossCenter > MAX_MGRS_100K_GRID_THRESHOLD) {
            createMGRSGridZones(mapBounds, camera, viewWidth, viewHeight, true);
            createMGRSGrid(mapBounds, camera, viewWidth, viewHeight, MGRS_100K_METER_GRID);
        } else if (widthAcrossCenter > MAX_MGRS_10K_GRID_THRESHOLD) {
            createMGRSGrid(mapBounds, camera, viewWidth, viewHeight, MGRS_10K_METER_GRID);
        } else if (widthAcrossCenter > MAX_MGRS_1K_GRID_THRESHOLD) {
            createMGRSGrid(mapBounds, camera, viewWidth, viewHeight, MGRS_1K_METER_GRID);
        } else if (widthAcrossCenter > MAX_MGRS_100_GRID_THRESHOLD) {
            createMGRSGrid(mapBounds, camera, viewWidth, viewHeight, MGRS_100_METER_GRID);
        } else {
            createMGRSGrid(mapBounds, camera, viewWidth, viewHeight, MGRS_10_METER_GRID);
        }
    }

    private void createMGRSGrid(EmpBoundingBox mapBounds, ICamera camera, int viewWidth, int viewHeight, int gridSize) {
        int longitude;
        int latitude;
        int eastZoneNumber;
        double metersPerPixel = mapBounds.widthAcrossCenter() / (double) viewWidth;
        EmpBoundingBox gridZoneBounds = new EmpBoundingBox();
        UTMCoordainte UTMCoord = new UTMCoordainte();
        UTMCoordainte tempUTMCoord1 = new UTMCoordainte();
        UTMCoordainte tempUTMCoord2 = new UTMCoordainte();
        EmpBoundingBox tempBoundingBox = new EmpBoundingBox();

        if (0 == UTMCoordainte.getZoneNumber(mapBounds.centerLatitude(), mapBounds.centerLongitude())) {
            // The bounding box is in one of the poles. UPS
            // TODO add UPS.
            return;
        }

        double minLatitude = Math.floor(mapBounds.south());
        double maxLatitude = Math.ceil((mapBounds.north()));
        double minLongitude = Math.floor(mapBounds.west());
        double maxLongitude = Math.ceil((mapBounds.east()));

        eastZoneNumber = (UTMCoordainte.getZoneNumber(minLatitude, maxLongitude) % 60) + 1;

        latitude = (int) minLatitude;
        UTMCoordainte.fromLatLong(minLatitude, minLongitude, UTMCoord);

        // Process grid zones
        while (latitude <= maxLatitude) {
            gridZoneBounds.setSouth(UTMCoord.getZoneSouthLatitude());
            gridZoneBounds.setNorth(gridZoneBounds.getSouth() + UTMCoord.getGridZoneHeightInDegrees());
            while (UTMCoord.getZoneNumber() != eastZoneNumber) {
                Log.i(TAG, "Processing Zone: " + UTMCoord.getZoneNumber() + UTMCoord.getZoneLetter() + " width:" + UTMCoord.getGridZoneWidthInDegrees());
                gridZoneBounds.setWest(UTMCoord.getZoneWestLongitude());
                gridZoneBounds.setEast(gridZoneBounds.getWest() + UTMCoord.getGridZoneWidthInDegrees());
                createMGRSGridsForGridzone(UTMCoord, tempUTMCoord1, tempUTMCoord2, mapBounds.intersection(gridZoneBounds, tempBoundingBox), gridSize, metersPerPixel);
                UTMCoordainte.fromLatLong(latitude, gridZoneBounds.getEast(), UTMCoord);
            }
            latitude = (int) gridZoneBounds.getNorth();
            UTMCoordainte.fromLatLong(latitude, minLongitude, UTMCoord);
        }
    }

    private void createMGRSGridsForGridzone(UTMCoordainte UTMCoord, UTMCoordainte tempUTMCoord1, UTMCoordainte tempUTMCoord2,
            EmpBoundingBox drawBounds,
            int gridSize, double metersPerPixel) {
        double latitude;
        double longitude;
        List<IGeoPosition> positionList;
        IFeature gridObject;
        IGeoPosition WestPos = null;
        IGeoPosition EastPos = null;
        IGeoPosition TopPos = null;
        IGeoPosition BottomPos = null;
        String mgrsGridLabel;
        IGeoPosition labelPos;
        String letter;
        boolean northernHemesphere = UTMCoord.isNorthernHemisphere();

        if (null == drawBounds) {
            return;
        }

        // Within the UTM grid zone we draw the MGRS grid boxes from the bottom up, left to right.
        latitude = drawBounds.getSouth();

        // Generate the parallel lines of the box (the horizontal lines).
        UTMCoordainte.fromLatLong(latitude, drawBounds.getWest(), tempUTMCoord1);
        UTMCoordainte.fromLatLong(latitude, ((drawBounds.getEast() == 180.0)? 179.99999999999: drawBounds.getEast()), tempUTMCoord2);

        // Make sure that the northing value is a multiple of 100K.
        if ((int) tempUTMCoord1.getNorthing() != (((int) tempUTMCoord1.getNorthing() / MGRS_100K_METER_GRID) * MGRS_100K_METER_GRID)) {
            tempUTMCoord1.setNorthing(((int) tempUTMCoord1.getNorthing() / MGRS_100K_METER_GRID) * MGRS_100K_METER_GRID);
        }
        tempUTMCoord2.setNorthing(tempUTMCoord1.getNorthing());
        WestPos = tempUTMCoord1.toLatLong();

        while (latitude < drawBounds.getNorth()) {
            positionList = new ArrayList<>();
            positionList.add(WestPos);
            EastPos = tempUTMCoord2.toLatLong();
            positionList.add(EastPos);
            gridObject = createPathFeature(positionList, MGRS_GRID_BOX_PARALLELS);
            addFeature(gridObject);
            tempUTMCoord1.setNorthing(tempUTMCoord1.getNorthing() + MGRS_100K_METER_GRID);
            // Now we must make sure we didn't move onto the next grid zone.
            if (tempUTMCoord1.getNorthing() > tempUTMCoord1.getMaxNorthingForZone()) {
                if (northernHemesphere) {
                    letter = tempUTMCoord1.getNextLetter();
                } else {
                    letter = tempUTMCoord1.getPreviousLetter();
                }
                if (null == letter) {
                    break;
                }
                tempUTMCoord1.setZoneLetter(letter);
                tempUTMCoord2.setZoneLetter(letter);
            }
            tempUTMCoord2.setNorthing(tempUTMCoord1.getNorthing());
            WestPos = tempUTMCoord1.toLatLong();
            latitude = WestPos.getLatitude();
        }

        // Generate the meridian lines of the box (the vertical lines).
        longitude = drawBounds.getWest();

        UTMCoordainte.fromLatLong(drawBounds.getNorth(),longitude, tempUTMCoord1);
        UTMCoordainte.fromLatLong(drawBounds.getSouth(), longitude, tempUTMCoord2);

        // Make sure that the easting value is a multiple of 100K.
        if ((int) tempUTMCoord1.getEasting() != (((int) tempUTMCoord1.getEasting() / MGRS_100K_METER_GRID) * MGRS_100K_METER_GRID)) {
            tempUTMCoord1.setEasting(((int) tempUTMCoord1.getEasting() / MGRS_100K_METER_GRID) * MGRS_100K_METER_GRID);
        }
        tempUTMCoord2.setEasting(tempUTMCoord1.getEasting());
        TopPos = tempUTMCoord1.toLatLong();
        longitude = TopPos.getLongitude();

        while (longitude < drawBounds.getEast()) {
            if (longitude > drawBounds.getWest()) {
                positionList = new ArrayList<>();
                positionList.add(TopPos);
                BottomPos = tempUTMCoord2.toLatLong();
                positionList.add(BottomPos);
                gridObject = createPathFeature(positionList, MGRS_GRID_BOX_MERIDIAN);
                addFeature(gridObject);
            }
            tempUTMCoord1.setEasting(tempUTMCoord1.getEasting() + MGRS_100K_METER_GRID);
            tempUTMCoord2.setEasting(tempUTMCoord1.getEasting());
            TopPos = tempUTMCoord1.toLatLong();
            longitude = TopPos.getLongitude();
        }

        // Now generate the MGRS grid IDs.
        latitude = drawBounds.getSouth();
        longitude = drawBounds.getWest();
        UTMCoordainte.fromLatLong(latitude, longitude, tempUTMCoord1);

        // Make sure that the northing value is a multiple of 100K.
        if ((int) tempUTMCoord1.getNorthing() != (((int) tempUTMCoord1.getNorthing() / MGRS_100K_METER_GRID) * MGRS_100K_METER_GRID)) {
            tempUTMCoord1.setNorthing(((int) tempUTMCoord1.getNorthing() / MGRS_100K_METER_GRID) * MGRS_100K_METER_GRID);
        }
        // Place the northing half way.
        tempUTMCoord1.setNorthing(tempUTMCoord1.getNorthing() + (double) (MGRS_100K_METER_GRID / 2));

        // Make sure that the easting value is a multiple of 100K.
        if ((int) tempUTMCoord1.getEasting() != (((int) tempUTMCoord1.getEasting() / MGRS_100K_METER_GRID) * MGRS_100K_METER_GRID)) {
            tempUTMCoord1.setEasting(((int) tempUTMCoord1.getEasting() / MGRS_100K_METER_GRID) * MGRS_100K_METER_GRID);
        }
        tempUTMCoord1.setEasting(tempUTMCoord1.getEasting() + (double) (MGRS_100K_METER_GRID / 2));

        tempUTMCoord2.setZoneNumber(tempUTMCoord1.getZoneNumber());
        tempUTMCoord2.setZoneLetter(tempUTMCoord1.getZoneLetter());
        tempUTMCoord2.setNorthing(tempUTMCoord1.getNorthing());

        while (latitude < drawBounds.getNorth()) {
            tempUTMCoord2.setEasting(tempUTMCoord1.getEasting());
            labelPos = tempUTMCoord2.toLatLong();
            longitude = labelPos.getLongitude();
            while (longitude < drawBounds.getEast()) {
                if (drawBounds.contains(labelPos.getLatitude(), labelPos.getLongitude())) {
                    mgrsGridLabel = get100kID(tempUTMCoord2.getEasting(), tempUTMCoord2.getNorthing(), tempUTMCoord2.getZoneNumber());
                    Log.i(TAG, "Lable " + tempUTMCoord2.getZoneNumber() + tempUTMCoord2.getZoneLetter() + " " + mgrsGridLabel + "  lat/lng " + labelPos.getLatitude() + "/" + labelPos.getLongitude());
                    gridObject = createLabelFeature(labelPos, mgrsGridLabel, MGRS_GRID_BOX_LABEL);
                    addFeature(gridObject);
                }
                tempUTMCoord2.setEasting(tempUTMCoord2.getEasting() + MGRS_100K_METER_GRID);
                labelPos = tempUTMCoord2.toLatLong();
                longitude = labelPos.getLongitude();
            }
            tempUTMCoord2.setNorthing(tempUTMCoord2.getNorthing() + MGRS_100K_METER_GRID);
            labelPos = tempUTMCoord2.toLatLong();
            latitude = labelPos.getLatitude();
        }

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

        if (!shouldGridRedraw(camera)) {
            return;
        }

        clearFeatureList();

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

            while (zoneIndex != endZoneIndex) {
                intLongitude = (zoneIndex * 6) - 180;

                for (iIndex = minRow; iIndex < maxRow; iIndex++) {
                    latitude = (double) ((iIndex * 8) - 80) + 4.0;
                    if ((intLongitude < 0) || (intLongitude > 36)) {
                        // Offset the longitude by 3 deg to place the label in the center of the gird zone.
                        longitude = (double) intLongitude + 3.0;
                    } else if (intLongitude == 0) {
                        switch (iIndex) {
                            case 17:
                                longitude = (double) intLongitude + 1.5;
                                break;
                            case 19:
                                longitude = (double) intLongitude + 4.5;
                                break;
                            default:
                                longitude = (double) intLongitude + 3.0;
                                break;
                        }
                    } else if (intLongitude == 6) {
                        switch (iIndex) {
                            case 17:
                                longitude = (double) intLongitude + 1.5;
                                break;
                            case 19:
                                continue;
                            default:
                                longitude = (double) intLongitude + 3.0;
                                break;
                        }
                    } else if (iIndex == 19) {
                        switch (intLongitude) {
                            case 18:
                            case 30:
                                continue;
                            case 36:
                                longitude = (double) intLongitude + 2.25;
                                break;
                            default:
                                longitude = (double) intLongitude + 3.0;
                                break;
                        }
                    } else {
                        longitude = (double) intLongitude + 3.0;
                    }

                    if (latitude > mapBounds.getNorth()) {
                        latitude -= 3;
                    } else if (latitude < mapBounds.getSouth()) {
                        latitude += 3;
                    }

                    gridObject = createLabelFeature(GridLineUtils.newPosition(latitude, longitude, 0), "" + (zoneIndex + 1) + latBands.charAt(iIndex), MGRS_GRID_ZONE_LABEL);
                    addFeature(gridObject);
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
}
