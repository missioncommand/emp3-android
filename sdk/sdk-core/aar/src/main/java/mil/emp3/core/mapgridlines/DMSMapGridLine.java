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
    private static final int DMS_500_MSEC_INTERVAL = DMS_100_MSEC_INTERVAL + 1;
    private static final int DMS_1_SEC_INTERVAL = DMS_500_MSEC_INTERVAL + 1;
    private static final int DMS_5_SEC_INTERVAL = DMS_1_SEC_INTERVAL + 1;
    private static final int DMS_10_SEC_INTERVAL = DMS_5_SEC_INTERVAL + 1;
    private static final int DMS_30_SEC_INTERVAL = DMS_10_SEC_INTERVAL + 1;
    private static final int DMS_1_MIN_INTERVAL = DMS_30_SEC_INTERVAL + 1;
    private static final int DMS_5_MIN_INTERVAL = DMS_1_MIN_INTERVAL + 1;
    private static final int DMS_10_MIN_INTERVAL = DMS_5_MIN_INTERVAL + 1;
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

    private static final double DEGREES_PER_100_MILLISECONDS = DMSAngle.DEGREES_PER_MILLISECOND * 100.0;
    private static final double DEGREES_PER_500_MILLISECONDS = DMSAngle.DEGREES_PER_MILLISECOND * 500.0;
    private static final double DEGREES_PER_SECOND = DMSAngle.DEGREES_PER_SECOND;
    private static final double DEGREES_PER_5_SECONDS = DMSAngle.DEGREES_PER_SECOND * 5.0;
    private static final double DEGREES_PER_10_SECONDS = DMSAngle.DEGREES_PER_SECOND * 10.0;
    private static final double DEGREES_PER_30_SECONDS = DMSAngle.DEGREES_PER_SECOND * 30.0;
    private static final double DEGREES_PER_MINUTE = DMSAngle.DEGREES_PER_MINUTE;
    private static final double DEGREES_PER_5_MINUTES = DMSAngle.DEGREES_PER_MINUTE * 5.0;
    private static final double DEGREES_PER_10_MINUTES = DMSAngle.DEGREES_PER_MINUTE * 10.0;
    private static final double DEGREES_PER_30_MINUTES = DMSAngle.DEGREES_PER_MINUTE * 30.0;

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
        int[] gridSetting = new int[] { 0, 0};

        clearFeatureList();

        double degreesPerUnit;

        try {
            for (int iIndex = 0; iIndex < 2; iIndex++) {
                degreesPerUnit = degreesPerFractionInch[iIndex];
                if (degreesPerUnit < DEGREES_PER_100_MILLISECONDS) {
                    gridSetting[iIndex] = DMS_100_MSEC_INTERVAL;
                    Log.i(TAG, ((iIndex == 0)? "Lat": "Long") + " 0.1\" Grid :" + degreesPerUnit);
                } else if (degreesPerUnit < DEGREES_PER_500_MILLISECONDS) {
                    gridSetting[iIndex] = DMS_500_MSEC_INTERVAL;
                    Log.i(TAG, ((iIndex == 0)? "Lat": "Long") + " 0.5\" Grid :" + degreesPerUnit);
                } else if (degreesPerUnit < DEGREES_PER_SECOND) {
                    gridSetting[iIndex] = DMS_1_SEC_INTERVAL;
                    Log.i(TAG, ((iIndex == 0)? "Lat": "Long") + " 1\" Grid :" + degreesPerUnit);
                } else if (degreesPerUnit < DEGREES_PER_5_SECONDS) {
                    gridSetting[iIndex] = DMS_5_SEC_INTERVAL;
                    Log.i(TAG, ((iIndex == 0)? "Lat": "Long") + " 5\" Grid :" + degreesPerUnit);
                } else if (degreesPerUnit < DEGREES_PER_10_SECONDS) {
                    gridSetting[iIndex] = DMS_10_SEC_INTERVAL;
                    Log.i(TAG, ((iIndex == 0)? "Lat": "Long") + " 10\" Grid :" + degreesPerUnit);
                } else if (degreesPerUnit < DEGREES_PER_30_SECONDS) {
                    gridSetting[iIndex] = DMS_30_SEC_INTERVAL;
                    Log.i(TAG, ((iIndex == 0)? "Lat": "Long") + " 30\" Grid :" + degreesPerUnit);
                } else if (degreesPerUnit <= DEGREES_PER_MINUTE) {
                    gridSetting[iIndex] = DMS_1_MIN_INTERVAL;
                    Log.i(TAG, ((iIndex == 0)? "Lat": "Long") + " 1' Grid :" + degreesPerUnit);
                } else if (degreesPerUnit <= DEGREES_PER_5_MINUTES) {
                    gridSetting[iIndex] = DMS_5_MIN_INTERVAL;
                    Log.i(TAG, ((iIndex == 0)? "Lat": "Long") + " 5' Grid :" + degreesPerUnit);
                } else if (degreesPerUnit <= DEGREES_PER_10_MINUTES) {
                    gridSetting[iIndex] = DMS_10_MIN_INTERVAL;
                    Log.i(TAG, ((iIndex == 0)? "Lat": "Long") + " 10' Grid :" + degreesPerUnit);
                } else if (degreesPerUnit <= DEGREES_PER_30_MINUTES) {
                    gridSetting[iIndex] = DMS_30_MIN_INTERVAL;
                    Log.i(TAG, ((iIndex == 0)? "Lat": "Long") + " 30' Grid :" + degreesPerUnit);
                } else if (degreesPerUnit <= 1.0) {
                    gridSetting[iIndex] = DMS_1_DEGREE_INTERVAL;
                    Log.i(TAG, ((iIndex == 0)? "Lat": "Long") + " 1° Grid :" + degreesPerUnit);
                } else if (degreesPerUnit <= 5.0) {
                    gridSetting[iIndex] = DMS_5_DEGREE_INTERVAL;
                    Log.i(TAG, ((iIndex == 0)? "Lat": "Long") + " 5° Grid :" + degreesPerUnit);
                } else if (degreesPerUnit <= 10.0) {
                    gridSetting[iIndex] = DMS_10_DEGREE_INTERVAL;
                    Log.i(TAG, ((iIndex == 0)? "Lat": "Long") + " 10° Grid :" + degreesPerUnit);
                }
            }

            if ((gridSetting[0] > 0) && (gridSetting[1] > 0)) {
                createDMSGridLines(gridSetting[0], gridSetting[1], mapBounds, metersPerPixel, metersInOneEighthOfAnInch);
                displayGridLabel("DMS Grid", mapBounds, metersPerPixel);
            } else {
                displayGridLabel("DMS Grid Off", mapBounds, metersPerPixel);
                Log.i(TAG, "DMS Grid Off :");
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
            case DMS_500_MSEC_INTERVAL: {
                double dd = Math.floor(angle.toDD() / 500.0 / DMSAngle.DEGREES_PER_MILLISECOND) * 500.0 * DMSAngle.DEGREES_PER_MILLISECOND;

                if (angle.toDD() > dd) {
                    dd += (DMSAngle.DEGREES_PER_MILLISECOND * 500.0);
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
            case DMS_5_SEC_INTERVAL: {
                double dd = Math.floor(angle.toDD() / 5.0 / DMSAngle.DEGREES_PER_SECOND) * 5.0 * DMSAngle.DEGREES_PER_SECOND;

                if (angle.toDD() > dd) {
                    dd += (DMSAngle.DEGREES_PER_SECOND * 5.0);
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
            case DMS_5_MIN_INTERVAL: {
                double dd = Math.floor(angle.toDD() / 5.0 / DMSAngle.DEGREES_PER_MINUTE) * 5.0 * DMSAngle.DEGREES_PER_MINUTE;

                if (angle.toDD() > dd) {
                    dd += (DMSAngle.DEGREES_PER_MINUTE * 5.0);
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
            case DMS_500_MSEC_INTERVAL: {
                angle.addSeconds(0.5);
                break;
            }
            case DMS_1_SEC_INTERVAL: {
                angle.addSeconds(1.0);
                break;
            }
            case DMS_5_SEC_INTERVAL: {
                angle.addSeconds(5.0);
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
            case DMS_5_MIN_INTERVAL: {
                angle.addMinutes(5);
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
            case DMS_1_MSEC_INTERVAL:
                fmtString = "%D2d\u00b0 %M2d' %S5.3f\"";
                break;
            case DMS_10_MSEC_INTERVAL:
                fmtString = "%D2d\u00b0 %M2d' %S4.2f\"";
                break;
            case DMS_100_MSEC_INTERVAL:
            case DMS_500_MSEC_INTERVAL:
                fmtString = "%D2d\u00b0 %M2d' %S3.1f\"";
                break;
            case DMS_1_SEC_INTERVAL:
            case DMS_5_SEC_INTERVAL:
            case DMS_10_SEC_INTERVAL:
            case DMS_30_SEC_INTERVAL:
                fmtString = "%D2d\u00b0 %M2d' %S2.0f\"";
                break;
            case DMS_1_MIN_INTERVAL:
            case DMS_5_MIN_INTERVAL:
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

    private void createDMSGridLines(int latitudeGridSetting, int longitudeGridSetting, IEmpBoundingBox mapBounds, double metersPerPixel, double metersInOneEighthOfAnInch) {
        DMSCoordinate meridianNorthCoordinate = DMSCoordinate.fromLatLong(mapBounds.getNorth(), mapBounds.getWest());
        DMSCoordinate meridianSouthCoordinate = DMSCoordinate.fromLatLong(mapBounds.getSouth(), mapBounds.getWest());
        DMSCoordinate parallelWestCoordinate = DMSCoordinate.fromLatLong(mapBounds.getSouth(), mapBounds.getWest());
        DMSCoordinate parallelEastCoordinate = DMSCoordinate.fromLatLong(mapBounds.getSouth(), mapBounds.getEast());

        initLongitude(longitudeGridSetting, meridianNorthCoordinate);
        initLongitude(longitudeGridSetting, meridianSouthCoordinate);
        //initLatitude(gridSetting, meridianSouthCoordinate);

        initLatitude(latitudeGridSetting, parallelWestCoordinate);
        initLatitude(latitudeGridSetting, parallelEastCoordinate);
        //initLongitude(gridSetting, parallelWestCoordinate);

        createMeridians(longitudeGridSetting, mapBounds, meridianNorthCoordinate, meridianSouthCoordinate, metersInOneEighthOfAnInch);
        createParallels(latitudeGridSetting, mapBounds, parallelWestCoordinate, parallelEastCoordinate, metersInOneEighthOfAnInch);
    }

    private void createMeridians(int longitudeGridSetting, IEmpBoundingBox mapBounds, DMSCoordinate northCoordinate, DMSCoordinate southCoordinate, double metersInOneEighthOfAnInch) {
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
            label = southCoordinate.longitudeToString(getFormatString(longitudeGridSetting));
            gridObject = createLabelFeature(pos, label, DMS_GRID_LONG_MINOR_VALUE);
            addFeature(gridObject);

            incrementLongitude(longitudeGridSetting, northCoordinate);
            incrementLongitude(longitudeGridSetting, southCoordinate);
            longitude = northCoordinate.getLongitude().toDD();
        }
    }

    private void createParallels(int latitudeGridSetting, IEmpBoundingBox mapBounds, DMSCoordinate westCoordinate, DMSCoordinate eastCoordinate, double metersInOneEighthOfAnInch) {
        IFeature gridObject;
        IGeoPosition pos;
        String label;
        double westLong;
        double eastLong;
        double latitude = westCoordinate.getLatitude().toDD();
        List<IGeoPosition> posList = new ArrayList<>();
        double centerLongitude = mapBounds.centerLongitude();
        double labelOffset =  metersInOneEighthOfAnInch;

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
            label = westCoordinate.latitudeToString(getFormatString(latitudeGridSetting));
            gridObject = createLabelFeature(pos, label, DMS_GRID_LAT_MINOR_VALUE);
            addFeature(gridObject);

            incrementLatitude(latitudeGridSetting, westCoordinate);
            incrementLatitude(latitudeGridSetting, eastCoordinate);
            latitude = westCoordinate.getLatitude().toDD();
        }
    }
}
