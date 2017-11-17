package mil.emp3.core.mapgridlines;

import android.util.Log;

import org.cmapi.primitives.GeoLabelStyle;
import org.cmapi.primitives.GeoPosition;
import org.cmapi.primitives.GeoStrokeStyle;
import org.cmapi.primitives.IGeoLabelStyle;
import org.cmapi.primitives.IGeoPosition;
import org.cmapi.primitives.IGeoRenderable;
import org.cmapi.primitives.IGeoStrokeStyle;

import java.util.ArrayList;
import java.util.List;

import mil.emp3.api.Path;
import mil.emp3.api.Text;
import mil.emp3.api.global;
import mil.emp3.api.interfaces.ICamera;
import mil.emp3.api.interfaces.IEmpBoundingBox;
import mil.emp3.api.interfaces.IFeature;
import mil.emp3.api.utils.EmpBoundingBox;
import mil.emp3.api.utils.EmpGeoColor;
import mil.emp3.core.mapgridlines.coordinates.UTMCoordinate;
import mil.emp3.core.mapgridlines.utils.GridLineUtils;
import mil.emp3.mapengine.interfaces.IMapInstance;

/**
 * This class implements the UTM grid line generator
 */

public class UTMMapGridLine extends UTMBaseMapGridLine {
    private static final String TAG = UTMMapGridLine.class.getSimpleName();

    private static final int UTM_100K_METER_GRID = 100000;
    private static final int UTM_10K_METER_GRID = 10000;
    private static final int UTM_1K_METER_GRID = 1000;
    private static final int UTM_100_METER_GRID = 100;
    private static final int UTM_10_METER_GRID = 10;
    private static final int UTM_1_METER_GRID = 1;

    // The object types for the MGRS grid components.
    private static final String UTM_GRID_LABEL = "UTM.grid.label";
    private static final String UTM_GRID_LINE_100K_MERIDIAN = "UTM.gridline.meridian.100k";
    private static final String UTM_GRID_LINE_100K_PARALLEL = "UTM.gridline.parallele.100k";
    private static final String UTM_GRID_LINE_MAJOR_MERIDIAN = "UTM.gridline.meridian.major";
    private static final String UTM_GRID_LINE_MAJOR_PARALLEL = "UTM.gridline.parallele.major";
    private static final String UTM_GRID_LINE_MINOR_MERIDIAN = "UTM.gridline.meridian.minor";
    private static final String UTM_GRID_LINE_MINOR_PARALLEL = "UTM.gridline.parallele.minor";
    private static final String UTM_GRID_NORTHING_VALUE = "UTM.grid.northing.value";
    private static final String UTM_GRID_EASTING_VALUE = "UTM.grid.easting.value";

    public UTMMapGridLine(IMapInstance mapInstance) {
        super(mapInstance);
        setStyles();
    }

    // This method loads the styles.
    private void setStyles() {
        EmpGeoColor color;
        IGeoStrokeStyle strokeStyle;
        IGeoLabelStyle labelStyle;

        // MGRS Grid Box
        color = new EmpGeoColor(0.7, 0, 0, 255);
        strokeStyle = new GeoStrokeStyle();
        strokeStyle.setStrokeColor(color);
        strokeStyle.setStrokeWidth(4.0);
        addStrokeStyle(UTM_GRID_LINE_100K_MERIDIAN, strokeStyle);
        addStrokeStyle(UTM_GRID_LINE_100K_PARALLEL, strokeStyle);

        // UTM grid label
        labelStyle = new GeoLabelStyle();
        color = new EmpGeoColor(0.6, 200, 200, 200);
        labelStyle.setColor(color);
        labelStyle.setSize(8.0);
        labelStyle.setJustification(IGeoLabelStyle.Justification.LEFT);
        labelStyle.setFontFamily("Arial");
        labelStyle.setTypeface(IGeoLabelStyle.Typeface.REGULAR);
        addLabelStyle(UTM_GRID_LABEL, labelStyle);

        // UTM Grid Lines Major
        color = new EmpGeoColor(0.5, 75, 75, 255);
        strokeStyle = new GeoStrokeStyle();
        strokeStyle.setStrokeColor(color);
        strokeStyle.setStrokeWidth(3.0);
        addStrokeStyle(UTM_GRID_LINE_MAJOR_MERIDIAN, strokeStyle);
        addStrokeStyle(UTM_GRID_LINE_MAJOR_PARALLEL, strokeStyle);

        // UTM Grid Lines Minor
        color = new EmpGeoColor(0.5, 150, 150, 255);
        strokeStyle = new GeoStrokeStyle();
        strokeStyle.setStrokeColor(color);
        strokeStyle.setStrokeWidth(1.0);
        addStrokeStyle(UTM_GRID_LINE_MINOR_MERIDIAN, strokeStyle);
        addStrokeStyle(UTM_GRID_LINE_MINOR_PARALLEL, strokeStyle);

        // UTM northing values
        labelStyle = new GeoLabelStyle();
        color = new EmpGeoColor(1.0, 150, 150, 150);
        labelStyle.setColor(color);
        labelStyle.setSize(8.0);
        labelStyle.setJustification(IGeoLabelStyle.Justification.LEFT);
        labelStyle.setFontFamily("Arial");
        labelStyle.setTypeface(IGeoLabelStyle.Typeface.REGULAR);
        addLabelStyle(UTM_GRID_NORTHING_VALUE, labelStyle);

        // UTM easting values.
        labelStyle = new GeoLabelStyle();
        labelStyle.setColor(color);
        labelStyle.setSize(8.0);
        labelStyle.setJustification(IGeoLabelStyle.Justification.LEFT);
        labelStyle.setFontFamily("Arial");
        labelStyle.setTypeface(IGeoLabelStyle.Typeface.REGULAR);
        addLabelStyle(UTM_GRID_EASTING_VALUE, labelStyle);
    }

    @Override
    protected void processViewChange(IEmpBoundingBox mapBounds, ICamera camera, double metersPerPixel) {
        double metersInOneEighthOfAnInch = metersPerPixel * PIXELS_PER_INCH / 8.0;

        clearFeatureList();

        try {
            // The UTM grid setting is based on the distance (meters) in 1/8 of an inch on the display device based on the screen density.
            if (metersInOneEighthOfAnInch <= 2) {
                Log.i(TAG, "1 threshold. " + metersInOneEighthOfAnInch);
                createUTMGridZones(mapBounds, metersPerPixel);
                createUTMGrid(mapBounds, metersPerPixel, metersInOneEighthOfAnInch, UTM_1_METER_GRID);
                displayGridLabel("UTM 1m Grid", mapBounds, metersPerPixel);
            } else if (metersInOneEighthOfAnInch <= 17) {
                Log.i(TAG, "10 threshold. " + metersInOneEighthOfAnInch);
                createUTMGridZones(mapBounds, metersPerPixel);
                createUTMGrid(mapBounds, metersPerPixel, metersInOneEighthOfAnInch, UTM_10_METER_GRID);
                displayGridLabel("UTM 10m Grid", mapBounds, metersPerPixel);
            } else if (metersInOneEighthOfAnInch <= 160) {
                Log.i(TAG, "100 threshold. " + metersInOneEighthOfAnInch);
                createUTMGridZones(mapBounds, metersPerPixel);
                createUTMGrid(mapBounds, metersPerPixel, metersInOneEighthOfAnInch, UTM_100_METER_GRID);
                displayGridLabel("UTM 100m Grid", mapBounds, metersPerPixel);
            } else if (metersInOneEighthOfAnInch <= 1600) {
                Log.i(TAG, "1K threshold. " + metersInOneEighthOfAnInch);
                createUTMGridZones(mapBounds, metersPerPixel);
                createUTMGrid(mapBounds, metersPerPixel, metersInOneEighthOfAnInch, UTM_1K_METER_GRID);
                displayGridLabel("UTM 1Km Grid", mapBounds, metersPerPixel);
            } else if (metersInOneEighthOfAnInch <= 14000) {
                Log.i(TAG, "10K threshold. " + metersInOneEighthOfAnInch);
                createUTMGridZones(mapBounds, metersPerPixel);
                createUTMGrid(mapBounds, metersPerPixel, metersInOneEighthOfAnInch, UTM_10K_METER_GRID);
                displayGridLabel("UTM 10Km Grid", mapBounds, metersPerPixel);
            } else if (metersInOneEighthOfAnInch <= 39000) {
                Log.i(TAG, "100K threshold. " + metersInOneEighthOfAnInch);
                createUTMGridZones(mapBounds, metersPerPixel);
                createUTMGrid(mapBounds, metersPerPixel, metersInOneEighthOfAnInch, UTM_100K_METER_GRID);
                displayGridLabel("UTM 100Km Grid", mapBounds, metersPerPixel);
            } else if (metersInOneEighthOfAnInch <= 250000) {
                Log.i(TAG, "Grid Zones threshold. " + metersInOneEighthOfAnInch);
                createUTMGridZones(mapBounds, metersPerPixel);
                displayGridLabel("UTM Grid Zones", mapBounds, metersPerPixel);
            //} else if (metersInOneEighthOfAnInch <= 500000) {
             //   Log.i(TAG, "UTM grid. " + metersInOneEighthOfAnInch);
             //   super.processViewChange(mapBounds, camera, metersPerPixel);
            } else {
                Log.i(TAG, "Grid off. " + metersInOneEighthOfAnInch);
                // The grid turns off.
            }
        } catch (Exception Ex) {
            Log.e(TAG, "UTM grid generation failed.", Ex);
        }
    }

    @Override
    protected void setPathAttributes(Path path, String gridObjectType) {
        if (gridObjectType.startsWith("UTM.")) {
            switch (gridObjectType) {
                case UTM_GRID_LINE_100K_MERIDIAN:
                case UTM_GRID_LINE_100K_PARALLEL:
                case UTM_GRID_LINE_MAJOR_MERIDIAN:
                case UTM_GRID_LINE_MAJOR_PARALLEL:
                case UTM_GRID_LINE_MINOR_MERIDIAN:
                case UTM_GRID_LINE_MINOR_PARALLEL:
                    path.setPathType(IGeoRenderable.PathType.GREAT_CIRCLE);
                    break;
            }
        } else {
            super.setPathAttributes(path, gridObjectType);
        }
    }

    @Override
    protected void setLabelAttributes(Text label, String gridObjectType) {
        if (gridObjectType.startsWith("UTM.")) {
            switch (gridObjectType) {
                case UTM_GRID_EASTING_VALUE:
                    label.setAzimuth(-90.0);
                    break;
            }
        } else {
            super.setLabelAttributes(label, gridObjectType);
        }
    }

    private void createUTMMeridians(IEmpBoundingBox mapBounds, UTMCoordinate utmZoneCoord, double metersPerPixel,
            double metersInOneEighthOfAnInch, int gridSize, boolean createValues, UTMCoordinate[] tempUTMCoordList) {
        IGeoPosition southPos = new GeoPosition();
        IGeoPosition northPos = new GeoPosition();
        IGeoPosition tempPos = new GeoPosition();
        IGeoPosition labelPos;
        String labelText;
        int tempValue;
        double maxLongitude;
        List<IGeoPosition> positionList;
        IFeature gridObject;
        boolean isNorthernHemisphere = utmZoneCoord.isNorthernHemisphere();
        UTMCoordinate southUTMCoord = tempUTMCoordList[0];
        UTMCoordinate northUTMCoord = tempUTMCoordList[1];
        UTMCoordinate labelUTMCoord = tempUTMCoordList[2];
        IEmpBoundingBox gridZoneBounds = new EmpBoundingBox();
        double labelHeightMeters = getCharacterPixelWidth(UTM_GRID_NORTHING_VALUE) * metersPerPixel;
        boolean labelDetail = ((labelHeightMeters * 2) < metersInOneEighthOfAnInch);
        int majorGridSize = ((gridSize == UTM_100K_METER_GRID)? gridSize: ((metersInOneEighthOfAnInch < gridSize)? gridSize: gridSize * 10));

        if (mapBounds.containsIDL()) {
            if (utmZoneCoord.getZoneNumber() == 1) {
                UTMCoordinate.fromLatLong(Math.max(mapBounds.getSouth(), utmZoneCoord.getZoneSouthLatitude()), -180.0, southUTMCoord);
                UTMCoordinate.fromLatLong(Math.min(mapBounds.getNorth(), utmZoneCoord.getZoneSouthLatitude() + utmZoneCoord.getGridZoneHeightInDegrees()),
                        -180.0, northUTMCoord);
            } else {
                UTMCoordinate.fromLatLong(Math.max(mapBounds.getSouth(), utmZoneCoord.getZoneSouthLatitude()),
                        Math.max(mapBounds.getWest(), utmZoneCoord.getZoneWestLongitude()), southUTMCoord);
                UTMCoordinate.fromLatLong(Math.min(mapBounds.getNorth(), utmZoneCoord.getZoneSouthLatitude() + utmZoneCoord.getGridZoneHeightInDegrees()),
                        Math.max(mapBounds.getWest(), utmZoneCoord.getZoneWestLongitude()), northUTMCoord);
            }
        } else {
            UTMCoordinate.fromLatLong(Math.max(mapBounds.getSouth(), utmZoneCoord.getZoneSouthLatitude()), Math.max(mapBounds.getWest(), utmZoneCoord.getZoneWestLongitude()), southUTMCoord);
            UTMCoordinate.fromLatLong(Math.min(mapBounds.getNorth(), utmZoneCoord.getZoneSouthLatitude() + utmZoneCoord.getGridZoneHeightInDegrees()), Math.max(mapBounds.getWest(), utmZoneCoord.getZoneWestLongitude()), northUTMCoord);
        }

        gridZoneBounds.setSouth(utmZoneCoord.getZoneSouthLatitude());
        gridZoneBounds.setNorth(gridZoneBounds.getSouth() + utmZoneCoord.getGridZoneHeightInDegrees());
        gridZoneBounds.setWest(utmZoneCoord.getZoneWestLongitude());
        gridZoneBounds.setEast(gridZoneBounds.getWest() + utmZoneCoord.getGridZoneWidthInDegrees());

        if (isNorthernHemisphere) {
            // In the nortrhern hemisphere the south edge is wider.
            // Set the easting value to a multiple of the grid size.
            tempValue = (int) Math.floor(southUTMCoord.getEasting() / gridSize) * gridSize;
            if (southUTMCoord.getEasting() > (double) tempValue) {
                southUTMCoord.setEasting(tempValue + gridSize);
            } else if (mapBounds.containsIDL() && (utmZoneCoord.getZoneNumber() == 1) && (gridSize == UTM_1_METER_GRID)) {
                southUTMCoord.setEasting(tempValue + 1);
            } else {
                southUTMCoord.setEasting(tempValue);
            }
            northUTMCoord.setEasting(southUTMCoord.getEasting());
        } else {
            // Set the easting value to a multiple of the grid size.
            tempValue = (int) Math.floor(northUTMCoord.getEasting() / gridSize) * gridSize;
            if (northUTMCoord.getEasting() > (double) tempValue) {
                northUTMCoord.setEasting(tempValue + gridSize);
            } else if (mapBounds.containsIDL() && (utmZoneCoord.getZoneNumber() == 1) && (gridSize == UTM_1_METER_GRID)) {
                northUTMCoord.setEasting(tempValue + 1);
            } else {
                northUTMCoord.setEasting(tempValue);
            }
            southUTMCoord.setEasting(northUTMCoord.getEasting());
        }

        southUTMCoord.toLatLong(southPos);
        northUTMCoord.toLatLong(northPos);

        if (mapBounds.containsIDL()) {
            if (utmZoneCoord.getZoneNumber() == 1) {
                maxLongitude = Math.min(mapBounds.getEast(), utmZoneCoord.getZoneWestLongitude() + utmZoneCoord.getGridZoneWidthInDegrees());
            } else if (utmZoneCoord.getZoneNumber() == 60) {
                maxLongitude = 180.0;
            } else {
                maxLongitude = Math.min(180.0, utmZoneCoord.getZoneWestLongitude() + utmZoneCoord.getGridZoneWidthInDegrees());
            }
        } else {
            maxLongitude = Math.min(mapBounds.getEast(), utmZoneCoord.getZoneWestLongitude() + utmZoneCoord.getGridZoneWidthInDegrees());
        }

        while (southPos.getLongitude() < maxLongitude) {
            // The first and last meridian in a grid zone may not extend from the north to the south of the grid zone.
            if (!gridZoneBounds.contains(gridZoneBounds.centerLatitude(), northPos.getLongitude())) {
                // This one does not reach the northern latitude of the grid zone.
                // Adjust the latitude.
                double tempNorthing = northUTMCoord.getNorthing();
                tempPos.setLatitude(northPos.getLatitude());
                tempPos.setLongitude(northPos.getLongitude());
                while (!gridZoneBounds.contains(tempPos.getLatitude(), tempPos.getLongitude()) && (tempPos.getLatitude() > gridZoneBounds.getSouth())) {
                    northUTMCoord.setNorthing((int) northUTMCoord.getNorthing() - gridSize);
                    northUTMCoord.toLatLong(tempPos);
                }
                northPos.setLatitude(tempPos.getLatitude());
                northPos.setLongitude(tempPos.getLongitude());
                northUTMCoord.setNorthing(tempNorthing);
            }
            if (!gridZoneBounds.contains(gridZoneBounds.centerLatitude(), southPos.getLongitude())) {
                // This one does not reach the southern latitude of the grid zone.
                // Adjust the latitude.
                double tempNorthing = southUTMCoord.getNorthing();
                tempPos.setLatitude(southPos.getLatitude());
                tempPos.setLongitude(southPos.getLongitude());
                while (!gridZoneBounds.contains(tempPos.getLatitude(), tempPos.getLongitude()) && (tempPos.getLatitude() < gridZoneBounds.getNorth())) {
                    southUTMCoord.setNorthing((int) southUTMCoord.getNorthing() + gridSize);
                    southUTMCoord.toLatLong(tempPos);
                }
                southPos.setLatitude(tempPos.getLatitude());
                southPos.setLongitude(tempPos.getLongitude());
                southUTMCoord.setNorthing(tempNorthing);
            }

            positionList = new ArrayList<>();
            positionList.add(GridLineUtils.newPosition(southPos.getLatitude(), southPos.getLongitude(), 0.0));
            positionList.add(GridLineUtils.newPosition(northPos.getLatitude(), northPos.getLongitude(), 0.0));

            if (gridSize == UTM_100K_METER_GRID) {
                // The grid size is 100K.
                gridObject = createPathFeature(positionList, UTM_GRID_LINE_100K_MERIDIAN);
            } else if ((int) global.modulus(southUTMCoord.getEasting(), UTM_100K_METER_GRID) == 0) {
                // The grid size is NOT 100K but we are at a 100K boundary.
                gridObject = createPathFeature(positionList, UTM_GRID_LINE_100K_MERIDIAN);
            } else if ((int) global.modulus(southUTMCoord.getEasting(), majorGridSize) == 0) {
                // The grid size is not 100K and we are at a boundary of the grid size * 10.
                gridObject = createPathFeature(positionList, UTM_GRID_LINE_MAJOR_MERIDIAN);
            } else {
                // we are at a grid size boundary.
                gridObject = createPathFeature(positionList, UTM_GRID_LINE_MINOR_MERIDIAN);
            }

            addFeature(gridObject);

            if (createValues) {
                if (labelDetail || ((int) global.modulus(southUTMCoord.getEasting(), majorGridSize) == 0)) {
                    labelUTMCoord.copyFrom(southUTMCoord);
                    labelUTMCoord.setNorthing((Math.floor(labelUTMCoord.getNorthing() / gridSize) * gridSize) + gridSize);
                    labelPos = labelUTMCoord.toLatLong();
                    labelText = (int) southUTMCoord.getEasting() + "";
                    gridObject = createLabelFeature(labelPos, labelText, UTM_GRID_EASTING_VALUE);
                    addFeature(gridObject);
                }
            }

            // Increment the northing.
            if (isNorthernHemisphere) {
                southUTMCoord.setEasting((int) southUTMCoord.getEasting() + gridSize);
                northUTMCoord.setEasting(southUTMCoord.getEasting());
            } else {
                northUTMCoord.setEasting((int) northUTMCoord.getEasting() + gridSize);
                southUTMCoord.setEasting(northUTMCoord.getEasting());
            }
            southUTMCoord.toLatLong(southPos);
            northUTMCoord.toLatLong(northPos);
        }
    }

    private void createUTMParallels(IEmpBoundingBox mapBounds, UTMCoordinate utmZoneCoord, double metersPerPixel,
            double metersInOneEighthOfAnInch, int gridSize, boolean createValues, UTMCoordinate[] tempUTMCoordList) {
        IGeoPosition westPos = new GeoPosition();
        IGeoPosition eastPos = new GeoPosition();
        IGeoPosition labelPos;
        String labelText;
        double maxLatitude;
        double minLogintude;
        double maxLongitude;
        double tempNorthing;
        int tempValue;
        List<IGeoPosition> positionList;
        IFeature gridObject;
        UTMCoordinate westUTMCoord = tempUTMCoordList[0];
        UTMCoordinate eastUTMCoord = tempUTMCoordList[1];
        UTMCoordinate labelUTMCoord = tempUTMCoordList[2];
        double labelHeightMeters = getCharacterPixelWidth(UTM_GRID_NORTHING_VALUE) * metersPerPixel;
        boolean labelDetail = ((labelHeightMeters * 2) < metersInOneEighthOfAnInch);
        int majorGridSize = ((gridSize == UTM_100K_METER_GRID)? gridSize: ((metersInOneEighthOfAnInch < gridSize)? gridSize: gridSize * 10));

        if (!mapBounds.containsIDL()) {
            minLogintude = Math.max(mapBounds.getWest(), utmZoneCoord.getZoneWestLongitude());
            maxLongitude = Math.min(mapBounds.getEast(), utmZoneCoord.getZoneWestLongitude() + utmZoneCoord.getGridZoneWidthInDegrees());
        } else if (utmZoneCoord.getZoneNumber() == 1) {
            minLogintude = -180.0;
            maxLongitude = Math.min(mapBounds.getEast(), utmZoneCoord.getZoneWestLongitude() + utmZoneCoord.getGridZoneWidthInDegrees());
        } else if (utmZoneCoord.getZoneNumber() == 60) {
            minLogintude = Math.max(mapBounds.getWest(), utmZoneCoord.getZoneWestLongitude());
            maxLongitude = 180.0;
        } else if (utmZoneCoord.getZoneNumber() < 30) {
            minLogintude = utmZoneCoord.getZoneWestLongitude();
            maxLongitude = Math.min(mapBounds.getEast(), utmZoneCoord.getZoneWestLongitude() + utmZoneCoord.getGridZoneWidthInDegrees());
        } else {
            minLogintude = Math.max(mapBounds.getWest(), utmZoneCoord.getZoneWestLongitude());
            maxLongitude = utmZoneCoord.getZoneWestLongitude() + utmZoneCoord.getGridZoneWidthInDegrees();
        }
        maxLongitude = ((maxLongitude == 180.0)? maxLongitude - 0.00000001: maxLongitude);

        if ((utmZoneCoord.getZoneNumber() == 31) && (utmZoneCoord.getZoneLetter().equals("V"))) {
            maxLongitude = maxLongitude - 0.00000001;
        }

        UTMCoordinate.fromLatLong(Math.max(mapBounds.getSouth(), utmZoneCoord.getZoneSouthLatitude()),
                minLogintude, westUTMCoord);
        UTMCoordinate.fromLatLong(Math.max(mapBounds.getSouth(), utmZoneCoord.getZoneSouthLatitude()),
                maxLongitude, eastUTMCoord);

        // Set the northing value to a multiple of the grid size.
        tempValue = (int) Math.floor(westUTMCoord.getNorthing() / gridSize) * gridSize;
        if ((int) westUTMCoord.getNorthing() != tempValue) {
            westUTMCoord.setNorthing(tempValue + gridSize);
        }
        eastUTMCoord.setNorthing(westUTMCoord.getNorthing());

        westUTMCoord.toLatLong(westPos);

        if (westPos.getLongitude() < minLogintude) {
            westPos.setLongitude(minLogintude);
        }
        eastUTMCoord.toLatLong(eastPos);

        maxLatitude = Math.min(mapBounds.getNorth(), utmZoneCoord.getZoneSouthLatitude() + utmZoneCoord.getGridZoneHeightInDegrees());

        //Log.i(TAG, "    West Lat/Lon " + westPos.getLatitude() + "/" + westPos.getLongitude());

        while (westPos.getLatitude() <= maxLatitude) {
            positionList = new ArrayList<>();
            positionList.add(GridLineUtils.newPosition(westPos.getLatitude(), westPos.getLongitude(), 0.0));
            positionList.add(GridLineUtils.newPosition(eastPos.getLatitude(), eastPos.getLongitude(), 0.0));

            //Log.i(TAG, "Northing: " + (int) westUTMCoord.getNorthing());
            if (gridSize == UTM_100K_METER_GRID) {
                // The grid size is 100K.
                gridObject = createPathFeature(positionList, UTM_GRID_LINE_100K_PARALLEL);
            } else if ((int) global.modulus(westUTMCoord.getNorthing(), UTM_100K_METER_GRID) == 0) {
                // The grid size is NOT 100K but we are at a 100K boundary.
                gridObject = createPathFeature(positionList, UTM_GRID_LINE_100K_PARALLEL);
            } else if ((int) global.modulus(westUTMCoord.getNorthing(), majorGridSize) == 0) {
                // The grid size is not 100K and we are at a boundary of the grid size * 10.
                gridObject = createPathFeature(positionList, UTM_GRID_LINE_MAJOR_PARALLEL);
            } else {
                // we are at a grid size boundary.
                gridObject = createPathFeature(positionList, UTM_GRID_LINE_MINOR_PARALLEL);
            }

            addFeature(gridObject);

            if (createValues) {
                if (labelDetail || ((int) global.modulus(westUTMCoord.getNorthing(), majorGridSize) == 0)) {
                    labelUTMCoord.copyFrom(westUTMCoord);
                    labelUTMCoord.setEasting((Math.floor(labelUTMCoord.getEasting() / gridSize) * gridSize) + gridSize);
                    labelPos = labelUTMCoord.toLatLong();
                    labelText = (int) westUTMCoord.getNorthing() + "";
                    gridObject = createLabelFeature(labelPos, labelText, UTM_GRID_NORTHING_VALUE);
                    addFeature(gridObject);
                }
            }

            // Increment the northing.
            westUTMCoord.setNorthing((int) westUTMCoord.getNorthing() + gridSize);
            tempNorthing = westUTMCoord.getNorthing();
            eastUTMCoord.setNorthing((int) westUTMCoord.getNorthing());

            westUTMCoord.toLatLong(westPos);
            eastUTMCoord.toLatLong(eastPos);

            UTMCoordinate.fromLatLong(westPos.getLatitude(), minLogintude, westUTMCoord);
            UTMCoordinate.fromLatLong(eastPos.getLatitude(), maxLongitude, eastUTMCoord);

            westUTMCoord.setNorthing(tempNorthing);
            eastUTMCoord.setNorthing(tempNorthing);

            westPos = westUTMCoord.toLatLong();
            eastPos = eastUTMCoord.toLatLong();
        }
    }

    private void createUTMGrid(IEmpBoundingBox mapBounds, double metersPerPixel, double metersInOneEighthOfAnInch, int gridSize) {
        int westLongitude;
        int eastLongitude;
        double longitude;
        double latitude;
        boolean createNorthingValues = true;
        boolean createEastingValues = true;
        UTMCoordinate utmZoneCoord = new UTMCoordinate();
        UTMCoordinate[] tempUTMCoordList = {new UTMCoordinate(), new UTMCoordinate(), new UTMCoordinate()};

        UTMCoordinate.fromLatLong(mapBounds.getSouth(), mapBounds.getWest(), utmZoneCoord);
        westLongitude = utmZoneCoord.getZoneWestLongitude();
        eastLongitude = westLongitude + utmZoneCoord.getGridZoneWidthInDegrees();

        longitude = mapBounds.getWest();
        latitude = mapBounds.getSouth();
        while (mapBounds.contains(latitude, longitude)) {
            while (mapBounds.contains(latitude, longitude)) {
                //Log.i(TAG, utmZoneCoord.getZoneNumber() + utmZoneCoord.getZoneLetter());
                createUTMParallels(mapBounds, utmZoneCoord, metersPerPixel, metersInOneEighthOfAnInch, gridSize, createNorthingValues, tempUTMCoordList);
                createNorthingValues = false;
                createUTMMeridians(mapBounds, utmZoneCoord, metersPerPixel, metersInOneEighthOfAnInch, gridSize, createEastingValues, tempUTMCoordList);

                UTMCoordinate.fromLatLong(latitude, eastLongitude, utmZoneCoord);
                westLongitude = utmZoneCoord.getZoneWestLongitude();
                eastLongitude = westLongitude + utmZoneCoord.getGridZoneWidthInDegrees();
                longitude = westLongitude;
            }
            longitude = mapBounds.getWest();
            latitude = utmZoneCoord.getZoneSouthLatitude() + utmZoneCoord.getGridZoneHeightInDegrees();
            UTMCoordinate.fromLatLong(latitude, longitude, utmZoneCoord);
            westLongitude = utmZoneCoord.getZoneWestLongitude();
            eastLongitude = westLongitude + utmZoneCoord.getGridZoneWidthInDegrees();
            createNorthingValues = true;
            createEastingValues = false;
        }
    }
}
