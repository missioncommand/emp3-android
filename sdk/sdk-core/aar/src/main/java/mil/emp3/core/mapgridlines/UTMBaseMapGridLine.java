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
import mil.emp3.mapengine.interfaces.IMapInstance;

/**
 * this class implements the top level UTM map grid line generator. It is the base class for other grid line generators.
 */

public abstract class UTMBaseMapGridLine extends AbstractMapGridLine {
    private static final String TAG = UTMBaseMapGridLine.class.getSimpleName();
    // Latitude bands letters - from south to north
    protected static final String latBands = "CDEFGHJKLMNPQRSTUVWX";

    private static final double MAX_GRID_ALTITUDE = 5e7;

    private static final String GRID_MERIDIAN = "UTM.meridian";
    private static final String GRID_PARALLELS = "UTM.parallels";
    private static final String GRID_MERIDIAN_LABEL = "UTM.meridianlabel";

    private final IGeoStrokeStyle meridianLineStyle;
    private final IGeoLabelStyle meridianLabelStyle;

    // Exceptions for some meridians. Values: longitude, min latitude, max latitude
    private static final int[][] specialMeridians = {{3, 56, 64}, {6, 64, 72}, {9, 72, 84}, {21, 72, 84}, {33, 72, 84}};

    protected UTMBaseMapGridLine(IMapInstance mapInstance) {
        super(mapInstance);
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

        double minLatitude = Math.max(mapBounds.south(), -80.0);
        double maxLatitude = Math.min(mapBounds.north(), 84.0);

        if (!shouldGridRedraw(camera)) {
            return;
        }
        clearFeatureList();

        startZoneIndex = (int) Math.floor((mapBounds.west() + 180) / 6.0);
        endZoneIndex = (int) Math.ceil((mapBounds.east() + 180) / 6.0);

        double minLongitude = (double) ((startZoneIndex * 6) - 180);
        double maxLongitude = (double) ((endZoneIndex * 6) - 180);

        zoneIndex = startZoneIndex;
        while (zoneIndex != (endZoneIndex + 1)) {
            intLon = (zoneIndex * 6) - 180;
            longitude = (double) intLon;
            positionList = new ArrayList<>();

            // Meridian
            latitude = Math.max(minLatitude, -80.0);
            positionList.add(GridLineUtils.newPosition(latitude, longitude, 0));

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

                    if (maxLatitude > 56.0) {
                        gridObject = createPathFeature(positionList, GRID_MERIDIAN);
                        addFeature(gridObject);

                        positionList = new ArrayList<>();
                        positionList.add(GridLineUtils.newPosition(56.0, longitude - 3.0, 0));
                        positionList.add(GridLineUtils.newPosition(64.0, longitude -3.0, 0));

                        if (maxLatitude > 64.0) {
                            gridObject = createPathFeature(positionList, GRID_MERIDIAN);
                            addFeature(gridObject);

                            positionList = new ArrayList<>();
                            positionList.add(GridLineUtils.newPosition(64.0, longitude, 0));
                            positionList.add(GridLineUtils.newPosition(72.0, longitude, 0));

                            if (maxLatitude > 72.0) {
                                gridObject = createPathFeature(positionList, GRID_MERIDIAN);
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
                                gridObject = createPathFeature(positionList, GRID_MERIDIAN);
                                addFeature(gridObject);

                                positionList = new ArrayList<>();
                                positionList.add(GridLineUtils.newPosition(72.0, longitude + 3.0, 0));
                                positionList.add(GridLineUtils.newPosition(84.0, longitude + 3.0, 0));
                                break;
                        }
                    }
                }
            }

            gridObject = createPathFeature(positionList, GRID_MERIDIAN);
            addFeature(gridObject);

            // Zone label
            // Add 3 deg so the label appears in the center of the zone.
            if (camera.getLatitude() < 0) {
                // For zones in the southern hemisphere place the labels at the top.
                gridObject = createLabelFeature(GridLineUtils.newPosition(maxLatitude, longitude + 3.0, 0), (zoneIndex + 1) + "", GRID_MERIDIAN_LABEL);
            } else {
                gridObject = createLabelFeature(GridLineUtils.newPosition(minLatitude, longitude + 3.0, 0), (zoneIndex + 1) + "", GRID_MERIDIAN_LABEL);
            }
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
            zoneIndex = ++zoneIndex % 60;
        }

        // Generate parallels
        int minRow = (int) Math.floor((Math.max(minLatitude, -80.0) + 80.0) / 8.0);
        int maxRow = (int) Math.floor((Math.min(maxLatitude, 84.0) + 80.0) / 8.0);
        int iIndex;

        for (iIndex = minRow; iIndex < maxRow; iIndex++) {
            latitude = (double) ((iIndex * 8) - 80);

            positionList = new ArrayList<>();
            positionList.add(GridLineUtils.newPosition(latitude, minLongitude, 0));
            positionList.add(GridLineUtils.newPosition(latitude, maxLongitude, 0));
            gridObject = createPathFeature(positionList, GRID_PARALLELS);
            addFeature(gridObject);

            // Latitude band label
            if ((minLongitude + 3) < mapBounds.west()) {
                // Add 9 deg so the label appears in the center of the second grid from the left.
                gridObject = createLabelFeature(GridLineUtils.newPosition(latitude + 4, minLongitude + 9, 0), latBands.charAt(iIndex) + "", GRID_MERIDIAN_LABEL);
            } else {
                // Add 3 deg so the label appears in the center of the grid to the left.
                gridObject = createLabelFeature(GridLineUtils.newPosition(latitude + 4, minLongitude + 3, 0), latBands.charAt(iIndex) + "", GRID_MERIDIAN_LABEL);
            }
            addFeature(gridObject);
        }

        // See if the south UPS is visible.
        if ( mapBounds.south() < -80.0) {
            // Add the south UPS.
            gridObject = createLabelFeature(GridLineUtils.newPosition(-85.0, -90.0, 0), "A", GRID_MERIDIAN_LABEL);
            addFeature(gridObject);
            gridObject = createLabelFeature(GridLineUtils.newPosition(-85.0, 90.0, 0), "B", GRID_MERIDIAN_LABEL);
            addFeature(gridObject);
        }

        // See if the north UPS is visible.
        if ( mapBounds.north() > 84.0) {
            // Add the north UPS.
            gridObject = createLabelFeature(GridLineUtils.newPosition(87.0, -90.0, 0), "Y", GRID_MERIDIAN_LABEL);
            addFeature(gridObject);
            gridObject = createLabelFeature(GridLineUtils.newPosition(87.0, 90.0, 0), "Z", GRID_MERIDIAN_LABEL);
            addFeature(gridObject);
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
}
