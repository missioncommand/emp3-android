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
import mil.emp3.api.global;
import mil.emp3.api.interfaces.ICamera;
import mil.emp3.api.interfaces.IEmpBoundingBox;
import mil.emp3.api.interfaces.IFeature;
import mil.emp3.api.utils.EmpGeoColor;
import mil.emp3.api.utils.EmpGeoPosition;
import mil.emp3.api.utils.GeoLibrary;
import mil.emp3.core.mapgridlines.coordinates.DMSCoordinate;
import mil.emp3.core.mapgridlines.utils.DMSAngle;
import mil.emp3.mapengine.interfaces.IMapInstance;

/**
 * This class implements the Degree Minute Seconds map grid lines.
 */

public class DMSMapGridLine extends AbstractMapGridLine {
    private static final String TAG = DMSMapGridLine.class.getSimpleName();

    private static final int DMS_1_MSEC_INTERVAL = 1;
    private static final int DMS_10_MSEC_INTERVAL = DMS_1_MSEC_INTERVAL + 1;
    private static final int DMS_100_MSEC_INTERVAL = DMS_10_MSEC_INTERVAL + 1;
    private static final int DMS_1_SEC_INTERVAL = DMS_100_MSEC_INTERVAL + 1;
    private static final int DMS_10_SEC_INTERVAL = DMS_1_SEC_INTERVAL + 1;
    private static final int DMS_30_SEC_INTERVAL = DMS_10_SEC_INTERVAL + 1;
    private static final int DMS_1_MIN_INTERVAL = DMS_30_SEC_INTERVAL + 1;
    private static final int DMS_10_MIN_INTERVAL = DMS_1_MIN_INTERVAL + 1;
    private static final int DMS_30_MIN_INTERVAL = DMS_10_MIN_INTERVAL + 1;
    private static final int DMS_1_DEGREE_INTERVAL = DMS_30_MIN_INTERVAL + 1;
    private static final int DMS_5_DEGREE_INTERVAL = DMS_1_DEGREE_INTERVAL + 1;
    private static final int DMS_10_DEGREE_INTERVAL = DMS_5_DEGREE_INTERVAL + 1;

    private static final String DMS_GRID_LINE_MAJOR = "DMS.gridline.major";
    private static final String DMS_GRID_LINE_MINOR = "DMS.gridline.minor";
    private static final String DMS_GRID_LAT_MAJOR_VALUE = "DMS.grid.lat.major";
    private static final String DMS_GRID_LONG_MAJOR_VALUE = "DMS.grid.long.major";
    private static final String DMS_GRID_LAT_MINOR_VALUE = "DMS.grid.lat.minor";
    private static final String DMS_GRID_LONG_MINOR_VALUE = "DMS.grid.long.minor";

    private static final double DEGREES_PER_MILLISECOND = 1.0 / 3600000.0;
    private static final double DEGREES_PER_10_MILLISECONDS = 1.0 / 360000.0;
    private static final double DEGREES_PER_100_MILLISECONDS = 1.0 / 36000.0;
    private static final double DEGREES_PER_SECOND = 1.0 / 3600.0;
    private static final double DEGREES_PER_10_SECONDS = 1.0 / 360.0;
    private static final double DEGREES_PER_30_SECONDS = 3.0 / 360.0;
    private static final double DEGREES_PER_MINUTE = 1.0 / 60.0;
    private static final double DEGREES_PER_10_MINUTES = 1.0 / 6.0;
    private static final double DEGREES_PER_30_MINUTES = 0.5;

    public DMSMapGridLine(IMapInstance mapInstance) {
        super(mapInstance);
        setStyles();
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
        addStrokeStyle(DMS_GRID_LINE_MAJOR, strokeStyle);

        color = new EmpGeoColor(0.5, 150, 150, 255);
        strokeStyle = new GeoStrokeStyle();
        strokeStyle.setStrokeColor(color);
        strokeStyle.setStrokeWidth(1.0);
        addStrokeStyle(DMS_GRID_LINE_MINOR, strokeStyle);

        labelStyle = new GeoLabelStyle();
        color = new EmpGeoColor(1.0, 150, 150, 150);
        labelStyle.setColor(color);
        labelStyle.setSize(8.0);
        labelStyle.setJustification(IGeoLabelStyle.Justification.LEFT);
        labelStyle.setFontFamily("Ariel");
        labelStyle.setTypeface(IGeoLabelStyle.Typeface.REGULAR);
        addLabelStyle(DMS_GRID_LAT_MAJOR_VALUE, labelStyle);
        addLabelStyle(DMS_GRID_LONG_MAJOR_VALUE, labelStyle);

        labelStyle = new GeoLabelStyle();
        color = new EmpGeoColor(1.0, 150, 150, 150);
        labelStyle.setColor(color);
        labelStyle.setSize(6.0);
        labelStyle.setJustification(IGeoLabelStyle.Justification.LEFT);
        labelStyle.setFontFamily("Ariel");
        labelStyle.setTypeface(IGeoLabelStyle.Typeface.REGULAR);
        addLabelStyle(DMS_GRID_LAT_MINOR_VALUE, labelStyle);
        addLabelStyle(DMS_GRID_LONG_MINOR_VALUE, labelStyle);
    }

    @Override
    protected void setPathAttributes(Path path, String gridObjectType) {
        if (gridObjectType.startsWith("DMS.")) {
            path.setPathType(IGeoRenderable.PathType.RHUMB_LINE);
        } else {
            super.setPathAttributes(path, gridObjectType);
        }
    }

    @Override
    protected void setLabelAttributes(Text label, String gridObjectType) {
        if (gridObjectType.startsWith("DMS.")) {
            switch (gridObjectType) {
                case DMS_GRID_LONG_MAJOR_VALUE:
                case DMS_GRID_LONG_MINOR_VALUE:
                    label.setAzimuth(-90.0);
                    break;
            }
        } else {
            super.setLabelAttributes(label, gridObjectType);
        }
    }

    @Override
    protected void processViewChange(IEmpBoundingBox mapBounds, ICamera camera, double metersPerPixel) {
        // Calculate the mters per 1/8 of an inch on the display.
        double metersInOneEighthOfAnInch = metersPerPixel * PIXELS_PER_INCH / 8.0;
        // Get the smallest delta, longitude or latitude.
        double deltaDegree = ((mapBounds.deltaLatitude() > mapBounds.deltaLongitude())? mapBounds.deltaLongitude(): mapBounds.deltaLatitude());

        clearFeatureList();

        try {
            if (deltaDegree < DEGREES_PER_10_MILLISECONDS) {
                createDMSGridLines(DMS_1_MSEC_INTERVAL, mapBounds, metersPerPixel, metersInOneEighthOfAnInch);
                displayGridLabel("DMS 0.001\" Grid", mapBounds, metersPerPixel);
                Log.i(TAG, "DMS 0.001\" Grid :" + deltaDegree);
            } else if (deltaDegree < DEGREES_PER_100_MILLISECONDS) {
                createDMSGridLines(DMS_10_MSEC_INTERVAL, mapBounds, metersPerPixel, metersInOneEighthOfAnInch);
                displayGridLabel("DMS 0.01\" Grid", mapBounds, metersPerPixel);
                Log.i(TAG, "DMS 0.01\" Grid :" + deltaDegree);
            } else if (deltaDegree < DEGREES_PER_SECOND) {
                createDMSGridLines(DMS_100_MSEC_INTERVAL, mapBounds, metersPerPixel, metersInOneEighthOfAnInch);
                displayGridLabel("DMS 0.1\" Grid", mapBounds, metersPerPixel);
                Log.i(TAG, "DMS 0.1\" Grid :" + deltaDegree);
            } else if (deltaDegree < DEGREES_PER_10_SECONDS) {
                createDMSGridLines(DMS_1_SEC_INTERVAL, mapBounds, metersPerPixel, metersInOneEighthOfAnInch);
                displayGridLabel("DMS 1\" Grid", mapBounds, metersPerPixel);
                Log.i(TAG, "DMS 1\" Grid :" + deltaDegree);
            } else if (deltaDegree < DEGREES_PER_MINUTE) {
                createDMSGridLines(DMS_10_SEC_INTERVAL, mapBounds, metersPerPixel, metersInOneEighthOfAnInch);
                displayGridLabel("DMS 10\" Grid", mapBounds, metersPerPixel);
                Log.i(TAG, "DMS 10\" Grid :" + deltaDegree);
            } else if (deltaDegree < DEGREES_PER_MINUTE) {
                createDMSGridLines(DMS_30_SEC_INTERVAL, mapBounds, metersPerPixel, metersInOneEighthOfAnInch);
                displayGridLabel("DMS 30\" Grid", mapBounds, metersPerPixel);
                Log.i(TAG, "DMS 30\" Grid :" + deltaDegree);
            } else if (deltaDegree < DEGREES_PER_10_MINUTES) {
                createDMSGridLines(DMS_1_MIN_INTERVAL, mapBounds, metersPerPixel, metersInOneEighthOfAnInch);
                displayGridLabel("DMS 1' Grid", mapBounds, metersPerPixel);
                Log.i(TAG, "DMS 1' Grid :" + deltaDegree);
            } else if (deltaDegree < 2.0) {
                createDMSGridLines(DMS_10_MIN_INTERVAL, mapBounds, metersPerPixel, metersInOneEighthOfAnInch);
                displayGridLabel("DMS 10' Grid", mapBounds, metersPerPixel);
                Log.i(TAG, "DMS 10' Grid :" + deltaDegree);
            } else if (deltaDegree < 8.0) {
                createDMSGridLines(DMS_30_MIN_INTERVAL, mapBounds, metersPerPixel, metersInOneEighthOfAnInch);
                displayGridLabel("DMS 30' Grid", mapBounds, metersPerPixel);
                Log.i(TAG, "DMS 30' Grid :" + deltaDegree);
            } else if (deltaDegree < 12.0) {
                createDMSGridLines(DMS_1_DEGREE_INTERVAL, mapBounds, metersPerPixel, metersInOneEighthOfAnInch);
                displayGridLabel("DMS 1\u00b0 Grid", mapBounds, metersPerPixel);
                Log.i(TAG, "DMS 1° Grid :" + deltaDegree);
            } else if (deltaDegree < 25.0) {
                createDMSGridLines(DMS_5_DEGREE_INTERVAL, mapBounds, metersPerPixel, metersInOneEighthOfAnInch);
                displayGridLabel("DMS 5\u00b0 Grid", mapBounds, metersPerPixel);
                Log.i(TAG, "DMS 5° Grid :" + deltaDegree);
            } else if (deltaDegree <= 100.0) {
                createDMSGridLines(DMS_10_DEGREE_INTERVAL, mapBounds, metersPerPixel, metersInOneEighthOfAnInch);
                displayGridLabel("DMS 10\u00b0 Grid", mapBounds, metersPerPixel);
                Log.i(TAG, "DMS 10\u00b0 Grid :" + deltaDegree);
            } else {
                displayGridLabel("DMS Grid Off", mapBounds, metersPerPixel);
                Log.i(TAG, "DMS Grid Off :" + deltaDegree);
            }
            // Else the grid turns off.
        } catch (Exception Ex) {
            Log.e(TAG, "DMS grid generation failed.", Ex);
        }
    }

    private void initDMSAngle(int gridSetting, DMSAngle angle) {
        switch (gridSetting) {
            case DMS_1_MSEC_INTERVAL: {
                double dd = Math.floor(angle.toDD() / DMSAngle.DEGREES_PER_MILLISECOND) * DMSAngle.DEGREES_PER_MILLISECOND;

                if (angle.toDD() > dd) {
                    dd += DMSAngle.DEGREES_PER_MILLISECOND;
                }

                angle.setDecimalDegrees(dd);
                break;
            }
            case DMS_10_MSEC_INTERVAL: {
                double dd = Math.floor(angle.toDD() / 10.0 / DMSAngle.DEGREES_PER_MILLISECOND) * 10.0 * DMSAngle.DEGREES_PER_MILLISECOND;

                if (angle.toDD() > dd) {
                    dd += (DMSAngle.DEGREES_PER_MILLISECOND * 10.0);
                }

                angle.setDecimalDegrees(dd);
                break;
            }
            case DMS_100_MSEC_INTERVAL: {
                double dd = Math.floor(angle.toDD() / 100.0 / DMSAngle.DEGREES_PER_MILLISECOND) * 100.0 * DMSAngle.DEGREES_PER_MILLISECOND;

                if (angle.toDD() > dd) {
                    dd += (DMSAngle.DEGREES_PER_MILLISECOND * 100.0);
                }

                angle.setDecimalDegrees(dd);
                break;
            }
            case DMS_1_SEC_INTERVAL: {
                double dd = Math.floor(angle.toDD() / DMSAngle.DEGREES_PER_SECOND) * DMSAngle.DEGREES_PER_SECOND;

                if (angle.toDD() > dd) {
                    dd += DMSAngle.DEGREES_PER_SECOND;
                }

                angle.setDecimalDegrees(dd);
                break;
            }
            case DMS_10_SEC_INTERVAL: {
                double dd = Math.floor(angle.toDD() / 10.0 / DMSAngle.DEGREES_PER_SECOND) * 10.0 * DMSAngle.DEGREES_PER_SECOND;

                if (angle.toDD() > dd) {
                    dd += (DMSAngle.DEGREES_PER_SECOND * 10.0);
                }

                angle.setDecimalDegrees(dd);
                break;
            }
            case DMS_30_SEC_INTERVAL: {
                double dd = Math.floor(angle.toDD() / 30.0 / DMSAngle.DEGREES_PER_SECOND) * 30.0 * DMSAngle.DEGREES_PER_SECOND;

                if (angle.toDD() > dd) {
                    dd += (DMSAngle.DEGREES_PER_SECOND * 30.0);
                }

                angle.setDecimalDegrees(dd);
                break;
            }
            case DMS_1_MIN_INTERVAL: {
                double dd = Math.floor(angle.toDD() / DMSAngle.DEGREES_PER_MINUTE) * DMSAngle.DEGREES_PER_MINUTE;

                if (angle.toDD() > dd) {
                    dd += DMSAngle.DEGREES_PER_MINUTE;
                }

                angle.setDecimalDegrees(dd);
                break;
            }
            case DMS_10_MIN_INTERVAL: {
                double dd = Math.floor(angle.toDD() / 10.0 / DMSAngle.DEGREES_PER_MINUTE) * 10.0 * DMSAngle.DEGREES_PER_MINUTE;

                if (angle.toDD() > dd) {
                    dd += (DMSAngle.DEGREES_PER_MINUTE * 10.0);
                }

                angle.setDecimalDegrees(dd);
                break;
            }
            case DMS_30_MIN_INTERVAL: {
                double dd = Math.floor(angle.toDD() / 30.0 / DMSAngle.DEGREES_PER_MINUTE) * 30.0 * DMSAngle.DEGREES_PER_MINUTE;

                if (angle.toDD() > dd) {
                    dd += (DMSAngle.DEGREES_PER_MINUTE * 30.0);
                }

                angle.setDecimalDegrees(dd);
                break;
            }
            case DMS_1_DEGREE_INTERVAL: {
                double dd = Math.floor(angle.toDD());

                if (angle.toDD() > dd) {
                    dd += 1.0;
                }

                angle.setDecimalDegrees(dd);
                break;
            }
            case DMS_5_DEGREE_INTERVAL: {
                double dd = Math.floor(angle.toDD() / 5.0) * 5.0;

                if (angle.toDD() > dd) {
                    dd += 5.0;
                }

                angle.setDecimalDegrees(dd);
                break;
            }
            case DMS_10_DEGREE_INTERVAL: {
                double dd = Math.floor(angle.toDD() / 10.0) * 10.0;

                if (angle.toDD() > dd) {
                    dd += 10.0;
                }

                angle.setDecimalDegrees(dd);
                break;
            }
        }
    }

    private void incrementDMSAngle(int gridSetting, DMSAngle angle) {
        switch (gridSetting) {
            case DMS_1_MSEC_INTERVAL: {
                angle.addSeconds(0.001);
                break;
            }
            case DMS_10_MSEC_INTERVAL: {
                angle.addSeconds(0.01);
                break;
            }
            case DMS_100_MSEC_INTERVAL: {
                angle.addSeconds(0.1);
                break;
            }
            case DMS_1_SEC_INTERVAL: {
                angle.addSeconds(1.0);
                break;
            }
            case DMS_10_SEC_INTERVAL: {
                angle.addSeconds(10.0);
                break;
            }
            case DMS_30_SEC_INTERVAL: {
                angle.addSeconds(30.0);
                break;
            }
            case DMS_1_MIN_INTERVAL: {
                angle.addMinutes(1);
                break;
            }
            case DMS_10_MIN_INTERVAL: {
                angle.addMinutes(10);
                break;
            }
            case DMS_30_MIN_INTERVAL: {
                angle.addMinutes(30);
                break;
            }
            case DMS_1_DEGREE_INTERVAL: {
                angle.addDegrees(1);
                break;
            }
            case DMS_5_DEGREE_INTERVAL:
                angle.addDegrees(5);
                break;
            case DMS_10_DEGREE_INTERVAL:
            default: {
                angle.addDegrees(10);
                break;
            }
        }
    }

    private void initLatitude(int gridSetting, DMSCoordinate DMSCoord) {
        DMSAngle latitude = DMSCoord.getLatitude();

        initDMSAngle(gridSetting, latitude);

        if (latitude.toDD() > 90.0) {
            latitude.setDecimalDegrees(90.0);
        }
    }

    private void initLongitude(int gridSetting, DMSCoordinate DMSCoord) {
        DMSAngle longitude = DMSCoord.getLongitude();

        initDMSAngle(gridSetting, longitude);

        longitude.setDecimalDegrees(global.modulus(longitude.toDD() + 180, 360.0) - 180.0);
    }

    private void incrementLatitude(int gridSetting, DMSCoordinate DMSCoord) {
        DMSAngle latitude = DMSCoord.getLatitude();

        incrementDMSAngle(gridSetting, latitude);
    }

    private void incrementLongitude(int gridSetting, DMSCoordinate DMSCoord) {
        DMSAngle longitude = DMSCoord.getLongitude();

        incrementDMSAngle(gridSetting, longitude);

        longitude.setDecimalDegrees(global.modulus(longitude.toDD() + 180, 360.0) - 180.0);
    }

    private String getFormatString(int gridSetting) {
        String fmtString = "";

        switch (gridSetting) {
            case DMS_1_MSEC_INTERVAL: {
                fmtString = "%D2d\u00b0 %M2d' %S5.3f\"";
                break;
            }
            case DMS_10_MSEC_INTERVAL: {
                fmtString = "%D2d\u00b0 %M2d' %S4.2f\"";
                break;
            }
            case DMS_100_MSEC_INTERVAL: {
                fmtString = "%D2d\u00b0 %M2d' %S3.1f\"";
                break;
            }
            case DMS_1_SEC_INTERVAL:
            case DMS_10_SEC_INTERVAL:
            case DMS_30_SEC_INTERVAL:
                fmtString = "%D2d\u00b0 %M2d' %S2.0f\"";
                break;
            case DMS_1_MIN_INTERVAL:
            case DMS_10_MIN_INTERVAL:
            case DMS_30_MIN_INTERVAL:
                fmtString = "%D2d\u00b0 %M2d'";
                break;
            case DMS_1_DEGREE_INTERVAL:
            case DMS_5_DEGREE_INTERVAL:
            case DMS_10_DEGREE_INTERVAL:
                fmtString = "%D2d\u00b0";
                break;
            default:
                fmtString = "%D2d\u00b0 %M2d' %S5.3f\"";
                break;
        }

        return fmtString;
    }

    private void createDMSGridLines(int gridSetting, IEmpBoundingBox mapBounds, double metersPerPixel, double metersInOneEighthOfAnInch) {
        DMSCoordinate meridianNorthCoordinate = DMSCoordinate.fromLatLong(mapBounds.getNorth(), mapBounds.getWest());
        DMSCoordinate meridianSouthCoordinate = DMSCoordinate.fromLatLong(mapBounds.getSouth(), mapBounds.getWest());
        DMSCoordinate parallelWestCoordinate = DMSCoordinate.fromLatLong(mapBounds.getSouth(), mapBounds.getWest());
        DMSCoordinate parallelEastCoordinate = DMSCoordinate.fromLatLong(mapBounds.getSouth(), mapBounds.getEast());

        initLongitude(gridSetting, meridianNorthCoordinate);
        initLongitude(gridSetting, meridianSouthCoordinate);
        //initLatitude(gridSetting, meridianSouthCoordinate);

        initLatitude(gridSetting, parallelWestCoordinate);
        initLatitude(gridSetting, parallelEastCoordinate);
        //initLongitude(gridSetting, parallelWestCoordinate);

        createMeridians(gridSetting, mapBounds, meridianNorthCoordinate, meridianSouthCoordinate, metersInOneEighthOfAnInch);
        createParallels(gridSetting, mapBounds, parallelWestCoordinate, parallelEastCoordinate, metersInOneEighthOfAnInch);
    }

    private void createMeridians(int gridSetting, IEmpBoundingBox mapBounds, DMSCoordinate northCoordinate, DMSCoordinate southCoordinate, double metersInOneEighthOfAnInch) {
        IFeature gridObject;
        IGeoPosition pos;
        double northLat;
        double southLat;
        String label;
        List<IGeoPosition> posList = new ArrayList<>();
        double longitude = northCoordinate.getLongitude().toDD();
        double centerLat = mapBounds.centerLatitude();
        double labelOffset =  metersInOneEighthOfAnInch / 2.0;

        while (mapBounds.contains(centerLat, longitude)) {
            posList.clear();
            northLat = northCoordinate.getLatitude().toDD();
            pos = new EmpGeoPosition(northLat, longitude);
            posList.add(pos);
            southLat = southCoordinate.getLatitude().toDD();
            pos = new EmpGeoPosition(southLat, longitude);
            posList.add(pos);

            gridObject = createPathFeature(posList, DMS_GRID_LINE_MINOR);
            addFeature(gridObject);

            pos = GeoLibrary.calculateRhumbPositionAt(0.0, labelOffset, pos);
            label = southCoordinate.longitudeToString(getFormatString(gridSetting));
            gridObject = createLabelFeature(pos, label, DMS_GRID_LONG_MINOR_VALUE);
            addFeature(gridObject);

            incrementLongitude(gridSetting, northCoordinate);
            incrementLongitude(gridSetting, southCoordinate);
            longitude = northCoordinate.getLongitude().toDD();
        }
    }

    private void createParallels(int gridSetting, IEmpBoundingBox mapBounds, DMSCoordinate westCoordinate, DMSCoordinate eastCoordinate, double metersInOneEighthOfAnInch) {
        IFeature gridObject;
        IGeoPosition pos;
        String label;
        double westLong;
        double eastLong;
        double latitude = westCoordinate.getLatitude().toDD();
        List<IGeoPosition> posList = new ArrayList<>();
        double centerLongitude = mapBounds.centerLongitude();
        double labelOffset =  metersInOneEighthOfAnInch / 2.0;

        while (mapBounds.contains(latitude, centerLongitude)) {
            posList.clear();
            eastLong = eastCoordinate.getLongitude().toDD();
            pos = new EmpGeoPosition(latitude, eastLong);
            posList.add(pos);

            westLong = westCoordinate.getLongitude().toDD();
            pos = new EmpGeoPosition(latitude, westLong);
            posList.add(pos);

            gridObject = createPathFeature(posList, DMS_GRID_LINE_MINOR);
            addFeature(gridObject);

            pos = GeoLibrary.calculateRhumbPositionAt(90.0, labelOffset, pos);
            label = westCoordinate.latitudeToString(getFormatString(gridSetting));
            gridObject = createLabelFeature(pos, label, DMS_GRID_LAT_MINOR_VALUE);
            addFeature(gridObject);

            incrementLatitude(gridSetting, westCoordinate);
            incrementLatitude(gridSetting, eastCoordinate);
            latitude = westCoordinate.getLatitude().toDD();
        }
    }
}
