package mil.emp3.core.mapgridlines;

import android.util.Log;

import org.cmapi.primitives.GeoLabelStyle;
import org.cmapi.primitives.GeoStrokeStyle;
import org.cmapi.primitives.IGeoLabelStyle;
import org.cmapi.primitives.IGeoPosition;
import org.cmapi.primitives.IGeoStrokeStyle;

import java.util.ArrayList;
import java.util.List;

import mil.emp3.api.Path;
import mil.emp3.api.Text;
import mil.emp3.api.interfaces.ICamera;
import mil.emp3.api.interfaces.IFeature;
import mil.emp3.api.utils.EmpBoundingBox;
import mil.emp3.api.utils.EmpGeoColor;
import mil.emp3.core.mapgridlines.utils.GridLineUtils;

/**
 * this class implements the top level UTM map grid line generator. It is the base class for other grid line generators.
 */

public abstract class UTMBaseMapGridLine extends AbstractMapGridLine {
    private static final String TAG = UTMBaseMapGridLine.class.getSimpleName();

    private static final double MAX_GRID_ALTITUDE = 5e7;

    private static final String GRID_MERIDIAN = "UTM.meridian";
    private static final String GRID_PARALLELS = "UTM.parallels";
    private static final String GRID_MERIDIAN_LABEL = "UTM.meridianlabel";

    private final IGeoStrokeStyle meridianLineStyle;
    private final IGeoLabelStyle meridianLabelStyle;

    // Exceptions for some meridians. Values: longitude, min latitude, max latitude
    private static final int[][] specialMeridians = {{3, 56, 64}, {6, 64, 72}, {9, 72, 84}, {21, 72, 84}, {33, 72, 84}};
    // Latitude bands letters - from south to north
    private static final String latBands = "CDEFGHJKLMNPQRSTUVWX";

    protected UTMBaseMapGridLine() {
        this.meridianLineStyle = new GeoStrokeStyle();
        this.meridianLabelStyle = new GeoLabelStyle();
        setStyles();
    }

    private void setStyles() {
        EmpGeoColor lineColor = new EmpGeoColor(0.8, 200, 200, 200);
        EmpGeoColor labelColor = new EmpGeoColor(1.0, 255, 255, 255);

        this.meridianLineStyle.setStrokeColor(lineColor);
        this.meridianLineStyle.setStrokeWidth(3.0);

        this.meridianLabelStyle.setColor(labelColor);
        this.meridianLabelStyle.setSize(12.0);
        this.meridianLabelStyle.setJustification(IGeoLabelStyle.Justification.CENTER);
        this.meridianLabelStyle.setFontFamily("Ariel");
        this.meridianLabelStyle.setTypeface(IGeoLabelStyle.Typeface.BOLD);
    }

    @Override
    protected void processViewChange(EmpBoundingBox mapBounds, ICamera camera, int viewWidth, int viewHeight) {
        double longitude;
        double latitude;
        int intLon;
        double maxLat;
        int startZoneIndex;
        int endZoneIndex;
        int zoneIndex;
        List<IGeoPosition> positionList;
        IFeature gridObject;

        if (camera.getAltitude() > MAX_GRID_ALTITUDE) {
            clearFeatureList();
            return;
        }

        long startTS = System.currentTimeMillis();

        double minLatitude = Math.floor(mapBounds.south());
        double maxLatitude = Math.ceil((mapBounds.north()));

        if (!shouldGridRedraw(camera)) {
            return;
        }
        clearFeatureList();

        startZoneIndex = (int) Math.floor((mapBounds.west() + 180) / 6.0);
        endZoneIndex = (int) Math.ceil((mapBounds.east() + 180) / 6.0);

        double minLongitude = (double) ((startZoneIndex * 6) - 180);
        double maxLongitude = (double) ((endZoneIndex * 6) - 180);

        zoneIndex = startZoneIndex - 1;
        do {
            zoneIndex = ++zoneIndex % 60;
            intLon = (zoneIndex * 6) - 180;
            longitude = (double) intLon;
            positionList = new ArrayList<>();

            // Meridian
            latitude = Math.max(minLatitude, -80.0);
            positionList.add(GridLineUtils.newPosition(latitude, longitude, 0));
/*
            if (maxLatitude > -60) {
                positionList.add(GridLineUtils.newPosition(-60, longitude, 0));
            }
            positionList.add(GridLineUtils.newPosition(-30, longitude, 0));
            positionList.add(GridLineUtils.newPosition(0, longitude, 0));
            positionList.add(GridLineUtils.newPosition(30, longitude, 0));
*/
            if (intLon < 6 || intLon > 36) {
                // 'regular' UTM meridians
                maxLat = 84;
                //positionList.add(GridLineUtils.newPosition(60, longitude, 0));
                latitude = Math.min(maxLatitude, maxLat);
                positionList.add(GridLineUtils.newPosition(latitude, longitude, 0));
            } else {
                // Exceptions: shorter meridians around and north-east of Norway
                if (intLon == 6) {
                    maxLat = 56;
                    latitude = Math.min(maxLatitude, maxLat);
                    positionList.add(GridLineUtils.newPosition(latitude, longitude, 0));
                } else {
                    maxLat = 72;
                    latitude = Math.min(maxLatitude, maxLat);
                    //positionList.add(GridLineUtils.newPosition(60, longitude, 0));
                    positionList.add(GridLineUtils.newPosition(latitude, longitude, 0));
                }
            }

            gridObject = createPathFeature(positionList, GRID_MERIDIAN);
            addFeature(gridObject);

            // Zone label
            gridObject = createLabelFeature(GridLineUtils.newPosition(minLatitude, longitude + 3.0 , 0), (zoneIndex + 1) + "", GRID_MERIDIAN_LABEL);
            addFeature(gridObject);


            // Generate special meridian segments for exceptions around and north-east of Norway
            if ((zoneIndex >= 0) && (zoneIndex < 5)) {
                positionList = new ArrayList<>();
                longitude = specialMeridians[zoneIndex][0];
                positionList.add(GridLineUtils.newPosition(specialMeridians[zoneIndex][1], longitude, 0));
                positionList.add(GridLineUtils.newPosition(specialMeridians[zoneIndex][2], longitude, 0));
                gridObject = createPathFeature(positionList, GRID_MERIDIAN);
                addFeature(gridObject);
            }
        } while (zoneIndex != endZoneIndex);

        // Generate parallels
        int lat = -80;
        for (int i = 0; i < 21; i++) {
            latitude = lat;
            if ((minLatitude <= latitude) && (latitude <= maxLatitude)) {
/*
                for (int j = 0; j < 4; j++) {
                    positionList = new ArrayList<>();
                    longitude = -180 + j * 90;
                    positionList.add(GridLineUtils.newPosition(latitude, longitude, 0));
                    positionList.add(GridLineUtils.newPosition(latitude, longitude + 30, 0));
                    positionList.add(GridLineUtils.newPosition(latitude, longitude + 60, 0));
                    positionList.add(GridLineUtils.newPosition(latitude, longitude + 90, 0));
                    gridObject = createPathFeature(positionList, GRID_MERIDIAN);
                    addFeature(gridObject);
                }
*/
                positionList = new ArrayList<>();
                positionList.add(GridLineUtils.newPosition(latitude, minLongitude, 0));
                positionList.add(GridLineUtils.newPosition(latitude, maxLongitude, 0));
                gridObject = createPathFeature(positionList, GRID_PARALLELS);
                addFeature(gridObject);

                // Latitude band label
                gridObject = createLabelFeature(GridLineUtils.newPosition(latitude + 4, minLongitude + 3, 0), latBands.charAt(i) + "", GRID_MERIDIAN_LABEL);
                addFeature(gridObject);
            }

            // Increase latitude
            lat += lat < 72 ? 8 : 12;
        }
        Log.i(TAG, "feature generation in " + (System.currentTimeMillis() - startTS) + " ms.");
    }

    @Override
    protected void setPathAttributes(Path path, String gridObjectType) {
        switch (gridObjectType) {
            case GRID_MERIDIAN:
                path.setStrokeStyle(this.meridianLineStyle);
                break;
            case GRID_PARALLELS:
                // Parallels must be rhumb lines.
                path.setStrokeStyle(this.meridianLineStyle);
                break;
        }
    }

    @Override
    protected void setLabelAttributes(Text label, String gridObjectType) {
        label.setLabelStyle(this.meridianLabelStyle);
    }

    private boolean shouldGridRedraw(ICamera camera) {

        if (Math.abs(this.previousCamera.getLongitude() - camera.getLongitude()) >= 3.0) {
            // Redraw if the longitude changes by 3 deg or more.
            return true;
        }

        if (Math.abs(this.previousCamera.getLatitude() - camera.getLatitude()) >= 4.0) {
            // redraw if the latitude changed by 4 or more deg.
            return true;
        }

        if (Math.abs(this.previousCamera.getAltitude() - camera.getAltitude()) > 1e3) {
            return true;
        }

        if (Math.abs(this.previousCamera.getTilt() - camera.getTilt()) > 2.0) {
            return true;
        }
        return false;
    }
}
