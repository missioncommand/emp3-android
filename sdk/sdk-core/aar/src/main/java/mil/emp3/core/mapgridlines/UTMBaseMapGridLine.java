package mil.emp3.core.mapgridlines;

import org.cmapi.primitives.GeoLabelStyle;
import org.cmapi.primitives.GeoStrokeStyle;
import org.cmapi.primitives.IGeoColor;
import org.cmapi.primitives.IGeoLabelStyle;
import org.cmapi.primitives.IGeoPosition;
import org.cmapi.primitives.IGeoRenderable;
import org.cmapi.primitives.IGeoStrokeStyle;

import java.util.ArrayList;
import java.util.List;

import mil.emp3.api.Path;
import mil.emp3.api.interfaces.ICamera;
import mil.emp3.api.interfaces.IFeature;
import mil.emp3.api.utils.EmpBoundingBox;
import mil.emp3.api.utils.EmpGeoColor;
import mil.emp3.core.mapgridlines.coordinates.UTMCoordinate;
import mil.emp3.core.mapgridlines.utils.GridLineUtils;
import mil.emp3.mapengine.interfaces.IMapInstance;

/**
 * this class implements the top level UTM map grid line generator. It is the base class for other grid line generators.
 */

public abstract class UTMBaseMapGridLine extends AbstractMapGridLine {
    private static final String TAG = UTMBaseMapGridLine.class.getSimpleName();
    // Latitude bands letters - from south to north
    protected static final String latBands = "CDEFGHJKLMNPQRSTUVWX";

    private static final double MAX_GRID_ALTITUDE = 5e7;

    // The object types for the UTM grid Zone meridian, parallels, and labels.
    private static final String UTM_GRID_ZONE_MERIDIAN = "UTMBase.gridzone.meridian";
    private static final String UTM_GRID_ZONE_PARALLELS = "UTMBase.gridzone.parallels";
    private static final String UTM_GRID_ZONE_LABEL_CENTER = "UTMBase.gridzone.label.center";
    private static final String UTM_GRID_ZONE_LABEL_LEFT = "UTMBase.gridzone.label.left";
    private static final String UTM_GRID_ZONE_LABEL_RIGHT = "UTMBase.gridzone.label.right";

    //private static final String UTM_GRID_MERIDIAN = "UTMBase.meridian";
    //private static final String UTM_GRID_PARALLELS = "UTMBase.parallels";
    //private static final String UTM_GRID_MERIDIAN_LABEL = "UTMBase.meridianlabel";

    // Exceptions for some meridians. Values: longitude, min latitude, max latitude
    private static final int[][] specialMeridians = {{3, 56, 64}, {6, 64, 72}, {9, 72, 84}, {21, 72, 84}, {33, 72, 84}};

    protected UTMBaseMapGridLine(IMapInstance mapInstance) {
        super(mapInstance);
        setStyles();
    }

    private void setStyles() {
        IGeoColor color;
        IGeoStrokeStyle strokeStyle;
        IGeoLabelStyle labelStyle;

        // Grid zone styles.
        color = new EmpGeoColor(0.6, 255, 255, 0);
        strokeStyle = new GeoStrokeStyle();
        strokeStyle.setStrokeColor(color);
        strokeStyle.setStrokeWidth(3.0);
        addStrokeStyle(UTM_GRID_ZONE_MERIDIAN, strokeStyle);
        addStrokeStyle(UTM_GRID_ZONE_PARALLELS, strokeStyle);

        color = new EmpGeoColor(1.0, 255, 255, 0);
        labelStyle = new GeoLabelStyle();
        labelStyle.setColor(color);
        labelStyle.setSize(12.0);
        labelStyle.setJustification(IGeoLabelStyle.Justification.CENTER);
        labelStyle.setFontFamily("Ariel");
        labelStyle.setTypeface(IGeoLabelStyle.Typeface.REGULAR);
        addLabelStyle(UTM_GRID_ZONE_LABEL_CENTER, labelStyle);

        labelStyle = new GeoLabelStyle();
        labelStyle.setColor(color);
        labelStyle.setSize(12.0);
        labelStyle.setJustification(IGeoLabelStyle.Justification.LEFT);
        labelStyle.setFontFamily("Ariel");
        labelStyle.setTypeface(IGeoLabelStyle.Typeface.REGULAR);
        addLabelStyle(UTM_GRID_ZONE_LABEL_LEFT, labelStyle);

        labelStyle = new GeoLabelStyle();
        labelStyle.setColor(color);
        labelStyle.setSize(12.0);
        labelStyle.setJustification(IGeoLabelStyle.Justification.RIGHT);
        labelStyle.setFontFamily("Ariel");
        labelStyle.setTypeface(IGeoLabelStyle.Typeface.REGULAR);
        addLabelStyle(UTM_GRID_ZONE_LABEL_RIGHT, labelStyle);
    }

    @Override
    protected void processViewChange(EmpBoundingBox mapBounds, ICamera camera, double metersPerPixel) {
        double longitude;
        double latitude;
        int intLon;
        double maxLat;
        int startZoneIndex;
        int endZoneIndex;
        int zoneIndex;
        List<IGeoPosition> positionList;
        IFeature gridObject;

        clearFeatureList();
        if (camera.getAltitude() > MAX_GRID_ALTITUDE) {
            return;
        }

        double minLatitude = Math.max(mapBounds.getSouth(), -80.0);
        double maxLatitude = Math.min(mapBounds.getNorth(), 84.0);

        startZoneIndex = (int) Math.floor((mapBounds.getWest() + 180) / 6.0);
        endZoneIndex = (int) Math.ceil((mapBounds.getEast() + 180) / 6.0);

        double minLongitude = (double) ((startZoneIndex * 6) - 180);
        double maxLongitude = (double) ((endZoneIndex * 6) - 180);

        endZoneIndex = ++endZoneIndex % 60;

        zoneIndex = startZoneIndex;
        while (zoneIndex != endZoneIndex) {
            intLon = (zoneIndex * 6) - 180;
            longitude = (double) intLon;
            positionList = new ArrayList<>();

            // Meridian
            latitude = Math.max(minLatitude, -80.0);
            positionList.add(GridLineUtils.newPosition(latitude, longitude, 0));

            if (intLon < 6 || intLon > 36) {
                // 'regular' UTM meridians
                maxLat = 84;
                latitude = Math.min(maxLatitude, maxLat);
                positionList.add(GridLineUtils.newPosition(latitude, longitude, 0));
            } else {
                // Exceptions: shorter meridians around and north-east of Norway
                if (intLon == 6) {
                    maxLat = 56;
                    latitude = Math.min(maxLatitude, maxLat);
                    positionList.add(GridLineUtils.newPosition(latitude, longitude, 0));

                    if (maxLatitude > 56.0) {
                        gridObject = createPathFeature(positionList, UTM_GRID_ZONE_MERIDIAN);
                        addFeature(gridObject);

                        positionList = new ArrayList<>();
                        positionList.add(GridLineUtils.newPosition(56.0, longitude - 3.0, 0));
                        positionList.add(GridLineUtils.newPosition(64.0, longitude -3.0, 0));

                        if (maxLatitude > 64.0) {
                            gridObject = createPathFeature(positionList, UTM_GRID_ZONE_MERIDIAN);
                            addFeature(gridObject);

                            positionList = new ArrayList<>();
                            positionList.add(GridLineUtils.newPosition(64.0, longitude, 0));
                            positionList.add(GridLineUtils.newPosition(72.0, longitude, 0));

                            if (maxLatitude > 72.0) {
                                gridObject = createPathFeature(positionList, UTM_GRID_ZONE_MERIDIAN);
                                addFeature(gridObject);

                                positionList = new ArrayList<>();
                                positionList.add(GridLineUtils.newPosition(72.0, longitude + 3.0, 0));
                                positionList.add(GridLineUtils.newPosition(84.0, longitude + 3.0, 0));
                            }
                        }
                    }
                } else {
                    maxLat = 72;
                    latitude = Math.min(maxLatitude, maxLat);
                    //positionList.add(GridLineUtils.newPosition(60, longitude, 0));
                    positionList.add(GridLineUtils.newPosition(latitude, longitude, 0));

                    if (maxLatitude > 72.0) {
                        switch (intLon) {
                            case 18:
                            case 30:
                                gridObject = createPathFeature(positionList, UTM_GRID_ZONE_MERIDIAN);
                                addFeature(gridObject);

                                positionList = new ArrayList<>();
                                positionList.add(GridLineUtils.newPosition(72.0, longitude + 3.0, 0));
                                positionList.add(GridLineUtils.newPosition(84.0, longitude + 3.0, 0));
                                break;
                        }
                    }
                }
            }

            gridObject = createPathFeature(positionList, UTM_GRID_ZONE_MERIDIAN);
            addFeature(gridObject);

            // Zone label
            // Add 3 deg so the label appears in the center of the zone.
            if (camera.getLatitude() < 0) {
                // For zones in the southern hemisphere place the labels at the top.
                gridObject = createLabelFeature(GridLineUtils.newPosition(maxLatitude, longitude + 3.0, 0), (zoneIndex + 1) + "", UTM_GRID_ZONE_LABEL_CENTER);
            } else {
                gridObject = createLabelFeature(GridLineUtils.newPosition(minLatitude, longitude + 3.0, 0), (zoneIndex + 1) + "", UTM_GRID_ZONE_LABEL_CENTER);
            }
            addFeature(gridObject);


            // Generate special meridian segments for exceptions around and north-east of Norway
            if ((zoneIndex >= 0) && (zoneIndex < 5)) {
                positionList = new ArrayList<>();
                longitude = specialMeridians[zoneIndex][0];
                positionList.add(GridLineUtils.newPosition(specialMeridians[zoneIndex][1], longitude, 0));
                positionList.add(GridLineUtils.newPosition(specialMeridians[zoneIndex][2], longitude, 0));
                gridObject = createPathFeature(positionList, UTM_GRID_ZONE_MERIDIAN);
                addFeature(gridObject);
            }
            zoneIndex = ++zoneIndex % 60;
        }

        // Generate parallels
        int minRow = (int) Math.floor((Math.max(minLatitude, -80.0) + 80.0) / 8.0);
        int maxRow = (int) Math.floor((Math.min(maxLatitude, 72.0) + 80.0) / 8.0);
        int iIndex;

        for (iIndex = minRow; iIndex <= maxRow; iIndex++) {
            latitude = (double) ((iIndex * 8) - 80);

            positionList = new ArrayList<>();
            positionList.add(GridLineUtils.newPosition(latitude, minLongitude, 0));
            positionList.add(GridLineUtils.newPosition(latitude, maxLongitude, 0));
            gridObject = createPathFeature(positionList, UTM_GRID_ZONE_PARALLELS);
            addFeature(gridObject);

            // Latitude band label
            if ((minLongitude + 3) < mapBounds.west()) {
                // Add 9 deg so the label appears in the center of the second grid from the left.
                gridObject = createLabelFeature(GridLineUtils.newPosition(latitude + 4, minLongitude + 9, 0), latBands.charAt(iIndex) + "", UTM_GRID_ZONE_LABEL_CENTER);
            } else {
                // Add 3 deg so the label appears in the center of the grid to the left.
                gridObject = createLabelFeature(GridLineUtils.newPosition(latitude + 4, minLongitude + 3, 0), latBands.charAt(iIndex) + "", UTM_GRID_ZONE_LABEL_CENTER);
            }
            addFeature(gridObject);
        }
        if ((maxLatitude >= 84.0) && (minLatitude < 84.0)) {
            positionList = new ArrayList<>();
            positionList.add(GridLineUtils.newPosition(84.0, minLongitude, 0));
            positionList.add(GridLineUtils.newPosition(84.0, maxLongitude, 0));
            gridObject = createPathFeature(positionList, UTM_GRID_ZONE_PARALLELS);
            addFeature(gridObject);
        }

        // See if the south UPS is visible.
        if ( mapBounds.south() < -80.0) {
            // Add the south UPS.
            gridObject = createLabelFeature(GridLineUtils.newPosition(-85.0, -90.0, 0), "A", UTM_GRID_ZONE_LABEL_CENTER);
            addFeature(gridObject);
            gridObject = createLabelFeature(GridLineUtils.newPosition(-85.0, 90.0, 0), "B", UTM_GRID_ZONE_LABEL_CENTER);
            addFeature(gridObject);
        }

        // See if the north UPS is visible.
        if ( mapBounds.north() > 84.0) {
            // Add the north UPS.
            gridObject = createLabelFeature(GridLineUtils.newPosition(87.0, -90.0, 0), "Y", UTM_GRID_ZONE_LABEL_CENTER);
            addFeature(gridObject);
            gridObject = createLabelFeature(GridLineUtils.newPosition(87.0, 90.0, 0), "Z", UTM_GRID_ZONE_LABEL_CENTER);
            addFeature(gridObject);
        }
    }

    /**
     * This method places the UTM grid zone lines on the map.
     * @param mapBounds        The bounding area of the map's viewing area.
     * @param metersPerPixel   Meters per pixel across the center of the map.
     */
    protected void createUTMGridZones(EmpBoundingBox mapBounds, double metersPerPixel) {
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
        double gridZoneLableHeight = getCharacterPixelWidth(UTM_GRID_ZONE_LABEL_CENTER) * metersPerPixel;
        double gridZoneLabelWidth = getCharacterPixelWidth(UTM_GRID_ZONE_LABEL_CENTER) * 1.5 * metersPerPixel;

        double minLatitude = Math.max(mapBounds.getSouth(), -80.0);
        double maxLatitude = Math.min(mapBounds.getNorth(), 84.0);

        if ((minLatitude >= 84.0) || (maxLatitude <= -80.0)) {
            // The bounding box is in one of the poles. UPS
            // TODO add UPS.
            return;
        }

        startZoneIndex = (int) Math.floor((mapBounds.west() + 180) / 6.0);
        endZoneIndex = (int) Math.ceil((mapBounds.east() + 180) / 6.0);

        double minLongitude = (double) ((startZoneIndex * 6) - 180);
        double maxLongitude = (double) ((endZoneIndex * 6) - 180);

        endZoneIndex = ++endZoneIndex % 60;
        zoneIndex = startZoneIndex;
        //zoneIndex = (startZoneIndex + 59) % 60;
        while (zoneIndex != endZoneIndex) {
            //zoneIndex = ++zoneIndex % 60;
            intLongitude = (zoneIndex * 6) - 180;
            longitude = (double) intLongitude;
            positionList = new ArrayList<>();

            // Meridian
            latitude = minLatitude;
            positionList.add(GridLineUtils.newPosition(latitude, longitude, 0));

            if (intLongitude < 6 || intLongitude > 36) {
                latitude = maxLatitude;
                positionList.add(GridLineUtils.newPosition(latitude, longitude, 0));
            } else if (intLongitude == 6) {
                latitude = Math.min(maxLatitude, 56.0);
                positionList.add(GridLineUtils.newPosition(latitude, longitude, 0));

                if (maxLatitude > 56.0) {
                    gridObject = createPathFeature(positionList, UTM_GRID_ZONE_MERIDIAN);
                    addFeature(gridObject);

                    latitude = Math.min(maxLatitude, 64.0);
                    positionList = new ArrayList<>();
                    positionList.add(GridLineUtils.newPosition(56.0, longitude - 3.0, 0));
                    positionList.add(GridLineUtils.newPosition(latitude, longitude -3.0, 0));

                    if (maxLatitude > 64.0) {
                        gridObject = createPathFeature(positionList, UTM_GRID_ZONE_MERIDIAN);
                        addFeature(gridObject);

                        latitude = Math.min(maxLatitude, 72.0);
                        positionList = new ArrayList<>();
                        positionList.add(GridLineUtils.newPosition(64.0, longitude, 0));
                        positionList.add(GridLineUtils.newPosition(latitude, longitude, 0));

                        if (maxLatitude > 72.0) {
                            gridObject = createPathFeature(positionList, UTM_GRID_ZONE_MERIDIAN);
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
                            gridObject = createPathFeature(positionList, UTM_GRID_ZONE_MERIDIAN);
                            addFeature(gridObject);

                            latitude = Math.min(maxLatitude, 84.0);
                            positionList = new ArrayList<>();
                            positionList.add(GridLineUtils.newPosition(72.0, longitude + 3.0, 0));
                            positionList.add(GridLineUtils.newPosition(latitude, longitude + 3.0, 0));
                            break;
                    }
                }
            }

            gridObject = createPathFeature(positionList, UTM_GRID_ZONE_MERIDIAN);
            addFeature(gridObject);
            zoneIndex = ++zoneIndex % 60;
        };

        // Generate parallels
        minRow = (int) Math.floor((minLatitude + 80.0) / 8.0);
        maxRow = (int) Math.floor((Math.min(maxLatitude, 72.0) + 80.0) / 8.0);

        for (iIndex = minRow; iIndex <= maxRow; iIndex++) {
            latitude = (double) ((iIndex * 8) - 80);
            positionList = new ArrayList<>();
            positionList.add(GridLineUtils.newPosition(latitude, minLongitude, 0));
            positionList.add(GridLineUtils.newPosition(latitude, maxLongitude, 0));
            gridObject = createPathFeature(positionList, UTM_GRID_ZONE_PARALLELS);
            addFeature(gridObject);
        }
        if ((maxLatitude >= 84.0) && (minLatitude < 84.0)) {
            positionList = new ArrayList<>();
            positionList.add(GridLineUtils.newPosition(84.0, minLongitude, 0));
            positionList.add(GridLineUtils.newPosition(84.0, maxLongitude, 0));
            gridObject = createPathFeature(positionList, UTM_GRID_ZONE_PARALLELS);
            addFeature(gridObject);
        }

        // Add The grid zone labels
        zoneIndex = startZoneIndex;
        String zoneLetter;
        int tempZoneNumber;

        if ((maxLatitude >= 84.0) && (minLatitude < 84.0)) {
            maxRow = 19;
        }
        while (zoneIndex != endZoneIndex) {
            tempZoneNumber = zoneIndex + 1;
            for (iIndex = minRow; iIndex <= maxRow; iIndex++) {
                if (iIndex < latBands.length()) {
                    zoneLetter = latBands.charAt(iIndex) + "";
                    if (zoneLetter.equals("X")) {
                        switch (zoneIndex + 1) {
                            case 32:
                            case 34:
                            case 36:
                                // In the X band these zones do not exists.
                                continue;
                        }
                    }
                    labelBounds.setSouth(UTMCoordinate.getZoneSouthLatitude(tempZoneNumber, zoneLetter));
                    labelBounds.setWest(UTMCoordinate.getZoneWestLongitude(tempZoneNumber, zoneLetter));
                    labelBounds.setNorth(labelBounds.getSouth() + UTMCoordinate.getGridZoneHeightInDegrees(tempZoneNumber, zoneLetter));
                    labelBounds.setEast(labelBounds.getWest() + UTMCoordinate.getGridZoneWidthInDegrees(tempZoneNumber, zoneLetter));

                    // Create the label if it fits.
                    if (null != mapBounds.intersection(labelBounds, labelBounds)) {
                        if (labelBounds.widthAcrossCenter() >= gridZoneLabelWidth) {
                            String objectStyleType = UTM_GRID_ZONE_LABEL_CENTER;
                            if (labelBounds.heightAcrossCenter() >= gridZoneLableHeight) {
                                double labelLatitude;
                                double labelLongitude;
                                double offsetLatitude = labelBounds.deltaLatitude() / 18.0;
                                double offsetLongitude = labelBounds.deltaLongitude() / 18.0;

                                if (labelBounds.getWest() == mapBounds.getWest()) {
                                    // The grid zone is against the west edge of the map viewing area.
                                    if (labelBounds.getEast() == mapBounds.getEast()) {
                                        // The grid zone is against the west and the east edge of the map viewing area.
                                        if (labelBounds.getNorth() == mapBounds.getNorth()) {
                                            // The grid zone is against the west, east, and the north edge of the map viewing area.
                                            if (labelBounds.getSouth() == mapBounds.getSouth()) {
                                                // The grid zone is against all four edges of the map viewing area.
                                                labelLatitude = labelBounds.getNorth() - offsetLatitude;
                                                labelLongitude = labelBounds.centerLongitude();
                                            } else {
                                                labelLatitude = labelBounds.getSouth() + (offsetLatitude * 2);
                                                labelLongitude = labelBounds.getEast() - offsetLongitude;
                                                objectStyleType = UTM_GRID_ZONE_LABEL_RIGHT;
                                            }
                                        } else if (labelBounds.getSouth() == mapBounds.getSouth()) {
                                            // The grid zone is against the west, east, and south edge the of the map viewing area.
                                            labelLatitude = labelBounds.getNorth() - (offsetLatitude * 2);
                                            labelLongitude = labelBounds.getEast() - offsetLongitude;
                                            objectStyleType = UTM_GRID_ZONE_LABEL_RIGHT;
                                        } else {
                                            // I don't think this can ever happen.
                                            labelLatitude = labelBounds.centerLatitude();
                                            labelLongitude = labelBounds.centerLongitude();
                                        }
                                    } else if (labelBounds.getNorth() == mapBounds.getNorth()) {
                                        // The grid zone is against the north west of the map viewing area.
                                        labelLatitude = labelBounds.getNorth() - offsetLatitude;
                                        labelLongitude = labelBounds.centerLongitude();
                                        //objectStyleType = UTM_GRID_ZONE_LABEL_LEFT;
                                    } else if (labelBounds.getSouth() == mapBounds.getSouth()) {
                                        // The grid zone is against the south west edges of the map viewing area.
                                        labelLatitude = labelBounds.getSouth() + (offsetLatitude * 2);
                                        labelLongitude = labelBounds.centerLongitude();
                                        //objectStyleType = UTM_GRID_ZONE_LABEL_LEFT;
                                    } else {
                                        // I don't think this can ever happen.
                                        labelLatitude = labelBounds.centerLatitude();
                                        labelLongitude = labelBounds.centerLongitude();
                                    }
                                } else if (labelBounds.getEast() == mapBounds.getEast()) {
                                    // The grid zone is against the east edge of the map viewing area.
                                    if (labelBounds.getNorth() == mapBounds.getNorth()) {
                                        // The grid zone is against the north east edges of the map viewing area.
                                        labelLatitude = labelBounds.getNorth() - offsetLatitude;
                                        labelLongitude = labelBounds.centerLongitude();
                                        //objectStyleType = UTM_GRID_ZONE_LABEL_RIGHT;
                                    } else if (labelBounds.getSouth() == mapBounds.getSouth()) {
                                        // The grid zone is against the east, and south edge the of the map viewing area.
                                        labelLatitude = labelBounds.getSouth() + (offsetLatitude * 2);
                                        labelLongitude = labelBounds.centerLongitude();
                                        //objectStyleType = UTM_GRID_ZONE_LABEL_RIGHT;
                                    } else {
                                        // I don't think this can ever happen.
                                        labelLatitude = labelBounds.centerLatitude();
                                        labelLongitude = labelBounds.centerLongitude();
                                        //objectStyleType = UTM_GRID_ZONE_LABEL_RIGHT;
                                    }
                                } else if (labelBounds.getNorth() == mapBounds.getNorth()) {
                                    // The grid zone is against the north east edges of the map viewing area.
                                    labelLatitude = labelBounds.getNorth() - offsetLatitude;
                                    labelLongitude = labelBounds.centerLongitude();
                                } else if (labelBounds.getSouth() == mapBounds.getSouth()) {
                                    // The grid zone is against the west, east, and south edge the of the map viewing area.
                                    labelLatitude = labelBounds.getSouth() + (offsetLatitude * 2);
                                    labelLongitude = labelBounds.centerLongitude();
                                } else {
                                    labelLatitude = labelBounds.centerLatitude();
                                    labelLongitude = labelBounds.centerLongitude();
                                }

                                gridObject = createLabelFeature(GridLineUtils.newPosition(labelLatitude, labelLongitude, 0.0), "" + tempZoneNumber + zoneLetter, objectStyleType);
                                addFeature(gridObject);
                            }
                        }
                    }
                }
            }
            zoneIndex = ++zoneIndex % 60;
        }

        // See if the south UPS is visible.
        if (mapBounds.contains(-85.0, -90.0)) {
            gridObject = createLabelFeature(GridLineUtils.newPosition(-85.0, -90.0, 0), "A", UTM_GRID_ZONE_LABEL_CENTER);
            addFeature(gridObject);
        }
        if (mapBounds.contains(-85.0, 90.0)) {
            // Add the south UPS.
            gridObject = createLabelFeature(GridLineUtils.newPosition(-85.0, 90.0, 0), "B", UTM_GRID_ZONE_LABEL_CENTER);
            addFeature(gridObject);
        }

        // See if the north UPS is visible.
        if (mapBounds.contains(87.0, -90.0)) {
            gridObject = createLabelFeature(GridLineUtils.newPosition(87.0, -90.0, 0), "Y", UTM_GRID_ZONE_LABEL_CENTER);
            addFeature(gridObject);
        }
        if (mapBounds.contains(87.0, 90.0)) {
            // Add the north UPS.
            gridObject = createLabelFeature(GridLineUtils.newPosition(87.0, 90.0, 0), "Z", UTM_GRID_ZONE_LABEL_CENTER);
            addFeature(gridObject);
        }
    }

    @Override
    protected void setPathAttributes(Path path, String gridObjectType) {
        if (gridObjectType.startsWith("UTMBase")) {
            switch (gridObjectType) {
                case UTM_GRID_ZONE_MERIDIAN:
                case UTM_GRID_ZONE_PARALLELS:
                    path.setPathType(IGeoRenderable.PathType.RHUMB_LINE);
                    break;
            }
        } else {
            super.setPathAttributes(path, gridObjectType);
        }
    }
}
