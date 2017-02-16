/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind.layers.Earth;


import android.content.res.Resources;
import android.graphics.Typeface;

import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.geom.coords.UPSCoord;
import gov.nasa.worldwind.geom.coords.UTMCoord;
import gov.nasa.worldwind.layers.*;
import gov.nasa.worldwind.render.*;
import gov.nasa.worldwind.shape.Label;
import gov.nasa.worldwind.util.Logger;
import gov.nasa.worldwind.utils.SectorUtils;
import mil.emp3.mapengine.interfaces.IMapInstance;

import java.util.*;

/**
 * Displays the UTM graticule.
 *
 * @author Patrick Murris
 * @version $Id: UTMBaseGraticuleLayer.java 2153 2014-07-17 17:33:13Z tgaskins $
 */
public class UTMBaseGraticuleLayer extends AbstractGraticuleLayer {
    final static private String TAG = UTMBaseGraticuleLayer.class.getSimpleName();

    public static final int MIN_CELL_SIZE_PIXELS = (new Double(50.0 / 96.0 * Resources.getSystem().getDisplayMetrics().densityDpi)).intValue();//25;//50;

    public static final String GRATICULE_UTM = "Graticule.UTM";

    protected static final double ONEHT = 100e3;
    protected static final int UTM_MIN_LATITUDE = -80;
    protected static final int UTM_MAX_LATITUDE = 84;

    protected MetricScaleSupport metricScaleSupport = new MetricScaleSupport();
    protected long frameCount = 0;

    // Exceptions for some meridians. Values: longitude, min latitude, max latitude
    private static final int[][] specialMeridians = {{3, 56, 64}, {6, 64, 72}, {9, 72, 84}, {21, 72, 84}, {33, 72, 84}};
    // Latitude bands letters - from south to north
    private static final String latBands = "CDEFGHJKLMNPQRSTUVWX";

    public UTMBaseGraticuleLayer(IMapInstance mapInstance) {
        super(mapInstance);
        createUTMRenderables();
        initRenderingParams();
        this.setPickEnabled(false);
        //this.setName(Logger.makeMessage(TAG, "isDrawGraticule", "layers.Earth.UTMGraticule.Name"));
    }

    /**
     * Returns whether or not graticule lines will be rendered.
     *
     * @return true if graticule lines will be rendered; false otherwise.
     */
    public boolean isDrawGraticule() {
        return getUTMRenderingParams().isDrawLines();
    }

    /**
     * Sets whether or not graticule lines will be rendered.
     *
     * @param drawGraticule true to render graticule lines; false to disable rendering.
     */
    public void setDrawGraticule(boolean drawGraticule) {
        getUTMRenderingParams().setDrawLines(drawGraticule);
    }

    /**
     * Returns the graticule line Color.
     *
     * @return Color used to render graticule lines.
     */
    public Color getGraticuleLineColor() {
        return getUTMRenderingParams().getLineColor();
    }

    /**
     * Sets the graticule line Color.
     *
     * @param color Color that will be used to render graticule lines.
     * @throws IllegalArgumentException if <code>color</code> is null.
     */
    public void setGraticuleLineColor(Color color) {
        if (color == null) {
            String message = Logger.makeMessage(TAG, "setGraticuleLineColor", "nullValue.ColorIsNull");
            Logger.log(Logger.ERROR, message);
            throw new IllegalArgumentException(message);
        }

        getUTMRenderingParams().setLineColor(color);
    }

    /**
     * Returns the graticule line width.
     *
     * @return width of the graticule lines.
     */
    public double getGraticuleLineWidth() {
        return getUTMRenderingParams().getLineWidth();
    }

    /**
     * Sets the graticule line width.
     *
     * @param lineWidth width of the graticule lines.
     */
    public void setGraticuleLineWidth(double lineWidth) {
        getUTMRenderingParams().setLineWidth(lineWidth);
    }

    /**
     * Returns the graticule line rendering style.
     *
     * @return rendering style of the graticule lines.
     */
    public String getGraticuleLineStyle() {
        return getUTMRenderingParams().getLineStyle();
    }

    /**
     * Sets the graticule line rendering style.
     *
     * @param lineStyle rendering style of the graticule lines. One of LINE_STYLE_PLAIN, LINE_STYLE_DASHED, or
     *                  LINE_STYLE_DOTTED.
     * @throws IllegalArgumentException if <code>lineStyle</code> is null.
     */
    public void setGraticuleLineStyle(String lineStyle) {
        if (lineStyle == null) {
            String message = Logger.makeMessage(TAG, "setGraticuleLineStyle", "nullValue.StringIsNull");
            Logger.log(Logger.ERROR, message);
            throw new IllegalArgumentException(message);
        }

        getUTMRenderingParams().setLineStyle(lineStyle);
    }

    /**
     * Returns whether or not graticule labels will be rendered.
     *
     * @return true if graticule labels will be rendered; false otherwise.
     */
    public boolean isDrawLabels() {
        return getUTMRenderingParams().isDrawLabels();
    }

    /**
     * Sets whether or not graticule labels will be rendered.
     *
     * @param drawLabels true to render graticule labels; false to disable rendering.
     */
    public void setDrawLabels(boolean drawLabels) {
        getUTMRenderingParams().setDrawLabels(drawLabels);
    }

    /**
     * Returns the graticule label Color.
     *
     * @return Color used to render graticule labels.
     */
    public Color getLabelColor() {
        return getUTMRenderingParams().getLabelColor();
    }

    /**
     * Sets the graticule label Color.
     *
     * @param color Color that will be used to render graticule labels.
     * @throws IllegalArgumentException if <code>color</code> is null.
     */
    public void setLabelColor(Color color) {
        if (color == null) {
            String message = Logger.makeMessage(TAG, "setLabelColor", "nullValue.ColorIsNull");
            Logger.log(Logger.ERROR, message);
            throw new IllegalArgumentException(message);
        }

        getUTMRenderingParams().setLabelColor(color);
    }

    /**
     * Returns the Font used for graticule labels.
     *
     * @return Font used to render graticule labels.
     */
/*
    public Font getLabelFont() {
        return getUTMRenderingParams().getLabelFont();
    }
*/
    /**
     * Sets the Font used for graticule labels.
     *
     //* @param font Font that will be used to render graticule labels.
     * @throws IllegalArgumentException if <code>font</code> is null.
     */
/*
     public void setLabelFont(Font font) {
        if (font == null) {
            String message = Logger.makeMessage(TAG, "isDrawGraticule", "nullValue.FontIsNull");
            Logger.log(Logger.ERROR, message);
            throw new IllegalArgumentException(message);
        }

        getUTMRenderingParams().setLabelFont(font);
    }
*/
    // --- Graticule Rendering --------------------------------------------------------------

    protected String getTypeFor(int resolution) {
        return GRATICULE_UTM;
    }

    protected void initRenderingParams() {
        GraticuleRenderingParams params = new GraticuleRenderingParams();
        params.setValue(GraticuleRenderingParams.KEY_LINE_COLOR, new Color(0.8f, 0.8f, 0.8f, 0.5f));
        params.setValue(GraticuleRenderingParams.KEY_LABEL_COLOR, new Color(1f, 1f, 1f, .8f));
        //params.setValue(GraticuleRenderingParams.KEY_LABEL_FONT, Font.decode("Arial-Bold-14"));
        params.setValue(GraticuleRenderingParams.KEY_LABEL_FONT_TYPE_FACE, Typeface.create("Arial", Typeface.BOLD));
        params.setValue(GraticuleRenderingParams.KEY_LABEL_FONT_POINT_SIZE, new Float(12.0f));
        params.setValue(GraticuleRenderingParams.KEY_DRAW_LABELS, Boolean.TRUE);
        setRenderingParams(GRATICULE_UTM, params);
    }

    private GraticuleRenderingParams getUTMRenderingParams() {
        return this.graticuleSupport.getRenderingParams(GRATICULE_UTM);
    }

    /**
     * Select the visible grid elements
     *
     * @param dc the current <code>DrawContext</code>.
     */
    protected void selectRenderables(RenderContext dc) {
        if (dc == null) {
            String message = Logger.makeMessage(TAG, "selectRenderables", "nullValue.DrawContextIsNull");
            Logger.log(Logger.ERROR, message);
            throw new IllegalArgumentException(message);
        }
        //Sector vs = SectorUtils.getMapSector(this.mapInstance); //dc.terrain.getMapSector();
        //OrbitView view = (OrbitView) dc.getView();
        // Compute labels offset from view center
        Position centerPos = new Position(dc.camera.latitude, dc.camera.longitude, dc.camera.altitude);
        Double pixelSizeDegrees = Math.toDegrees(dc.pixelSizeAtDistance(dc.camera.altitude) / dc.globe.getEquatorialRadius());
        Double labelOffsetDegrees = pixelSizeDegrees * dc.viewport.width / 4;
        Position labelPos = Position.fromDegrees(centerPos.latitude - labelOffsetDegrees, centerPos.longitude - labelOffsetDegrees, 0);
        Double labelLatDegrees = labelPos.latitude;
        labelLatDegrees = Math.min(Math.max(labelLatDegrees, -76), 78);
        labelPos = new Position(labelLatDegrees, labelPos.longitude, 0);

        if (this.mapSector != null) {
            for (GridElement ge : this.gridElements) {
                if (ge.isInView(dc)) {
                    if (ge.renderable instanceof Label) {
                        Label gt = (Label) ge.renderable;
                        if (labelPos.latitude < 72 || "*32*34*36*".indexOf("*" + gt.getText() + "*") == -1) {
                            // Adjust label position according to eye position
                            Position pos = gt.getPosition();
                            if (ge.type.equals(GridElement.TYPE_LATITUDE_LABEL)) {
                                pos = new Position(pos.latitude, labelPos.longitude, pos.altitude);
                            } else if (ge.type.equals(GridElement.TYPE_LONGITUDE_LABEL)) {
                                pos = new Position(labelPos.latitude, pos.longitude, pos.altitude);
                            }

                            gt.setPosition(pos);
                        }
                    }

                    this.graticuleSupport.addRenderable(ge.renderable, GRATICULE_UTM);
                }
            }
            //System.out.println("Total elements: " + count + " visible sector: " + vs);
        }
    }

    /**
     * Create the graticule grid elements
     */
    private void createUTMRenderables() {
        this.gridElements = new ArrayList<GridElement>();

        ArrayList<Position> positions = new ArrayList<Position>();

        // Generate meridians and zone labels
        int lon = -180;
        int zoneNumber = 1;
        int maxLat;
        for (int i = 0; i < 60; i++) {
            double longitude = lon;
            // Meridian
            positions.clear();
            positions.add(new Position(-80, longitude, 10e3));
            positions.add(new Position(-60, longitude, 10e3));
            positions.add(new Position(-30, longitude, 10e3));
            positions.add(new Position(0, longitude, 10e3));
            positions.add(new Position(30, longitude, 10e3));
            if (lon < 6 || lon > 36) {
                // 'regular' UTM meridians
                maxLat = 84;
                positions.add(new Position(60, longitude, 10e3));
                positions.add(new Position(maxLat, longitude, 10e3));
            } else {
                // Exceptions: shorter meridians around and north-east of Norway
                if (lon == 6) {
                    maxLat = 56;
                    positions.add(new Position(maxLat, longitude, 10e3));
                } else {
                    maxLat = 72;
                    positions.add(new Position(60, longitude, 10e3));
                    positions.add(new Position(maxLat, longitude, 10e3));
                }
            }
            Object polyline = createLineRenderable(new ArrayList<Position>(positions), WorldWind.GREAT_CIRCLE);
            //Sector sector = Sector.fromDegrees(-80, lon, maxLat + 80, 0.000001);
            Sector sector = new Sector();
            sector.union(-80, lon);
            sector.union(maxLat, lon);
            this.gridElements.add(new GridElement(sector, polyline, GridElement.TYPE_LINE));

            // Zone label
            Label text = new Label(Position.fromDegrees(0, lon + 3, 0), zoneNumber + "");
            //sector = Sector.fromDegrees(-90, lon + 3, 180, 1.0e-6);
            sector = new Sector();
            sector.union(-90, lon + 3);
            sector.union(90, lon + 3);
            this.gridElements.add(new GridElement(sector, text, GridElement.TYPE_LONGITUDE_LABEL));

            // Increase longitude and zone number
            lon += 6;
            zoneNumber++;
        }

        // Generate special meridian segments for exceptions around and north-east of Norway
        for (int i = 0; i < 5; i++) {
            positions.clear();
            lon = specialMeridians[i][0];
            positions.add(new Position(specialMeridians[i][1], lon, 10e3));
            positions.add(new Position(specialMeridians[i][2], lon, 10e3));
            Object polyline = createLineRenderable(new ArrayList<Position>(positions), WorldWind.GREAT_CIRCLE);
            //Sector sector = Sector.fromDegrees(specialMeridians[i][1], lon, specialMeridians[i][2] - specialMeridians[i][1], 0.0000001);
            Sector sector = new Sector();
            sector.union(specialMeridians[i][1], lon);
            sector.union(specialMeridians[i][2], lon);
            this.gridElements.add(new GridElement(sector, polyline, GridElement.TYPE_LINE));
        }

        // Generate parallels - no exceptions
        int lat = -80;
        for (int i = 0; i < 21; i++) {
            double latitude = lat;
            for (int j = 0; j < 4; j++) {
                // Each parallel is divided into four 90 degrees segments
                positions.clear();
                lon = -180 + j * 90;
                positions.add(new Position(latitude, lon, 10e3));
                positions.add(new Position(latitude, lon + 30, 10e3));
                positions.add(new Position(latitude, lon + 60, 10e3));
                positions.add(new Position(latitude, lon + 90, 10e3));
                Object polyline = createLineRenderable(new ArrayList<Position>(positions), WorldWind.GREAT_CIRCLE);
                //Sector sector = Sector.fromDegrees(lat, lon, 0.0000001, 90);
                Sector sector = new Sector();
                sector.union(lat, lon);
                sector.union(lat, lon + 90);
                this.gridElements.add(new GridElement(sector, polyline, GridElement.TYPE_LINE));
            }
            // Latitude band label
            if (i < 20) {
                Label text = new Label(Position.fromDegrees(lat + 4, 0, 0), latBands.charAt(i) + "");
                //Sector sector = Sector.fromDegrees(lat + 4, -180, 0.0000001, 360);
                Sector sector = new Sector();
                sector.union(lat + 4, -180);
                sector.union(lat + 4, 180);
                this.gridElements.add(new GridElement(sector, text, GridElement.TYPE_LATITUDE_LABEL));
            }

            // Increase latitude
            lat += lat < 72 ? 8 : 12;
        }
    }

    //=== Support classes and methods ====================================================

    protected Position computePosition(int zone, String hemisphere, double easting, double northing) {
        return zone > 0 ? computePositionFromUTM(zone, hemisphere, easting, northing) : computePositionFromUPS(hemisphere, easting, northing);
    }

    protected Position computePositionFromUTM(int zone, String hemisphere, double easting, double northing) {
        try {
            UTMCoord UTM = UTMCoord.fromUTM(zone, hemisphere, easting, northing, globe);
            return new Position(UTM.getLatitude(), UTM.getLongitude(), 10e3);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    protected Position computePositionFromUPS(String hemisphere, double easting, double northing) {
        try {
            UPSCoord UPS = UPSCoord.fromUPS(hemisphere, easting, northing, globe);
            return new Position(UPS.getLatitude(), UPS.getLongitude(), 10e3);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    //--- Metric scale support -----------------------------------------------------------

    protected class MetricScaleSupport {
        private int zone;

        private double offsetFactorX = -.5;
        private double offsetFactorY = -.5;
        private double visibleDistanceFactor = 10;
        private int scaleModulo = (int) 10e6;
        private double maxResolution = 1e5;

        // 5 levels 100km to 10m
        UTMExtremes[] extremes;

        private class UTMExtremes {
            protected double minX, maxX, minY, maxY;
            protected String minYHemisphere, maxYHemisphere;

            public UTMExtremes() {
                this.clear();
            }

            public void clear() {
                minX = 1e6;
                maxX = 0;
                minY = 10e6;
                minYHemisphere = AVKey.NORTH;
                maxY = 0;
                maxYHemisphere = AVKey.SOUTH;
            }
        }

        public int getZone() {
            return this.zone;
        }

        public void setScaleModulo(int modulo) {
            this.scaleModulo = modulo;
        }

        public void setMaxResolution(double resolutionInMeter) {
            this.maxResolution = resolutionInMeter;
            this.clear();
        }

        public void computeZone(RenderContext dc) {
            try {
                Position centerPos =  new Position(dc.camera.latitude, dc.camera.longitude, dc.camera.altitude);
                if (centerPos != null) {
                    if (centerPos.latitude <= UTM_MAX_LATITUDE && centerPos.latitude >= UTM_MIN_LATITUDE) {
                        UTMCoord UTM = UTMCoord.fromLatLon(centerPos.latitude, centerPos.longitude, dc.globe);
                        this.zone = UTM.getZone();
                    } else {
                        this.zone = 0;
                    }
                }
            } catch (Exception ex) {
                this.zone = 0;
            }
        }

        public void clear() {
            int numLevels = (int) Math.log10(this.maxResolution);
            this.extremes = new UTMExtremes[numLevels];
            for (int i = 0; i < numLevels; i++) {
                this.extremes[i] = new UTMExtremes();
                this.extremes[i].clear();
            }
        }

        public void computeMetricScaleExtremes(int UTMZone, String hemisphere, GridElement ge, double size) {
            if (UTMZone != this.zone) {
                return;
            }
            if (size < 1 || size > this.maxResolution) {
                return;
            }

            UTMExtremes levelExtremes = this.extremes[(int) Math.log10(size) - 1];

            if (ge.type.equals(GridElement.TYPE_LINE_EASTING) || ge.type.equals(GridElement.TYPE_LINE_EAST) || ge.type.equals(GridElement.TYPE_LINE_WEST)) {
                levelExtremes.minX = ge.value < levelExtremes.minX ? ge.value : levelExtremes.minX;
                levelExtremes.maxX = ge.value > levelExtremes.maxX ? ge.value : levelExtremes.maxX;
            } else if (ge.type.equals(GridElement.TYPE_LINE_NORTHING) || ge.type.equals(GridElement.TYPE_LINE_SOUTH) || ge.type.equals(GridElement.TYPE_LINE_NORTH)) {
                if (hemisphere.equals(levelExtremes.minYHemisphere)) {
                    levelExtremes.minY = ge.value < levelExtremes.minY ? ge.value : levelExtremes.minY;
                } else if (hemisphere.equals(AVKey.SOUTH)) {
                    levelExtremes.minY = ge.value;
                    levelExtremes.minYHemisphere = hemisphere;
                }
                if (hemisphere.equals(levelExtremes.maxYHemisphere)) {
                    levelExtremes.maxY = ge.value > levelExtremes.maxY ? ge.value : levelExtremes.maxY;
                } else if (hemisphere.equals(AVKey.NORTH)) {
                    levelExtremes.maxY = ge.value;
                    levelExtremes.maxYHemisphere = hemisphere;
                }
            }
        }

        public void selectRenderables(RenderContext dc) {
            try {
                //OrbitView view = (OrbitView) dc.getView();
                // Compute easting and northing label offsets
                Double pixelSize = dc.pixelSizeAtDistance(dc.camera.altitude); //view.computePixelSizeAtDistance(view.getZoom());
                Double eastingOffset = dc.viewport.width * pixelSize * offsetFactorX / 2;
                Double northingOffset = dc.viewport.height * pixelSize * offsetFactorY / 2;
                // Derive labels center pos from the view center
                Position centerPos = new Position(dc.camera.latitude, dc.camera.longitude, dc.camera.altitude);
                double labelEasting;
                double labelNorthing;
                String labelHemisphere;
                if (this.zone > 0) {
                    UTMCoord UTM = UTMCoord.fromLatLon(centerPos.latitude, centerPos.longitude, dc.globe);
                    labelEasting = UTM.getEasting() + eastingOffset;
                    labelNorthing = UTM.getNorthing() + northingOffset;
                    labelHemisphere = UTM.getHemisphere();
                    if (labelNorthing < 0) {
                        labelNorthing = 10e6 + labelNorthing;
                        labelHemisphere = AVKey.SOUTH;
                    }
                } else {
                    UPSCoord UPS = UPSCoord.fromLatLon(centerPos.latitude, centerPos.longitude, dc.globe);
                    labelEasting = UPS.getEasting() + eastingOffset;
                    labelNorthing = UPS.getNorthing() + northingOffset;
                    labelHemisphere = UPS.getHemisphere();
                }

                Frustum viewFrustum = dc.frustum;

                Position labelPos;
                for (int i = 0; i < this.extremes.length; i++) {
                    UTMExtremes levelExtremes = this.extremes[i];
                    double gridStep = Math.pow(10, i);
                    double gridStepTimesTen = gridStep * 10;
                    String graticuleType = getTypeFor((int) gridStep);
                    if (levelExtremes.minX <= levelExtremes.maxX) {
                        // Process easting scale labels for this level
                        for (double easting = levelExtremes.minX; easting <= levelExtremes.maxX; easting += gridStep) {
                            // Skip multiples of ten grid steps except for last (higher) level
                            if (i == this.extremes.length - 1 || easting % gridStepTimesTen != 0) {
                                try {
                                    labelPos = computePosition(this.zone, labelHemisphere, easting, labelNorthing);
                                    if (labelPos == null) {
                                        continue;
                                    }
                                    double lat = labelPos.latitude;
                                    double lon = labelPos.longitude;
                                    Vec3 surfacePoint = getSurfacePoint(dc, lat, lon);
                                    if (viewFrustum.containsPoint(surfacePoint) && isPointInRange(dc, surfacePoint)) {
                                        String text = String.valueOf((int) (easting % this.scaleModulo));
                                        Label gt = new Label(new Position(lat, lon, 0), text);
                                        //gt.setPriority(gridStepTimesTen);
                                        addRenderable(gt, graticuleType);
                                    }
                                } catch (IllegalArgumentException ignore) {
                                }
                            }
                        }
                    }
                    if (!(levelExtremes.maxYHemisphere.equals(AVKey.SOUTH) && levelExtremes.maxY == 0)) {
                        // Process northing scale labels for this level
                        String currentHemisphere = levelExtremes.minYHemisphere;
                        for (double northing = levelExtremes.minY; (northing <= levelExtremes.maxY) || !currentHemisphere.equals(levelExtremes.maxYHemisphere); northing += gridStep) {
                            // Skip multiples of ten grid steps except for last (higher) level
                            if (i == this.extremes.length - 1 || northing % gridStepTimesTen != 0) {
                                try {
                                    labelPos = computePosition(this.zone, currentHemisphere, labelEasting, northing);
                                    if (labelPos == null) {
                                        continue;
                                    }
                                    double lat = labelPos.latitude;
                                    double lon = labelPos.longitude;
                                    Vec3 surfacePoint = getSurfacePoint(dc, lat, lon);
                                    if (viewFrustum.containsPoint(surfacePoint) && isPointInRange(dc, surfacePoint)) {
                                        String text = String.valueOf((int) (northing % this.scaleModulo));
                                        Label gt = new Label(new Position(lat, lon, 0), text);
                                        //gt.setPriority(gridStepTimesTen);
                                        addRenderable(gt, graticuleType);
                                    }
                                } catch (IllegalArgumentException ignore) {
                                }

                                if (!currentHemisphere.equals(levelExtremes.maxYHemisphere) && northing >= 10e6 - gridStep) {
                                    // Switch hemisphere
                                    currentHemisphere = levelExtremes.maxYHemisphere;
                                    northing = -gridStep;
                                }
                            }
                        }
                    } // end northing
                } // for levels
            } catch (IllegalArgumentException ignore) {
            }
        }

        private boolean isPointInRange(RenderContext dc, Vec3 point) {
            double altitudeAboveGround = computeAltitudeAboveGround(dc);
            return dc.cameraPoint.distanceTo(point) < altitudeAboveGround * this.visibleDistanceFactor;
        }

        public String toString() {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < 5; i++) {
                sb.append("level ");
                sb.append(String.valueOf(i));
                sb.append(" : ");
                UTMExtremes levelExtremes = this.extremes[i];
                if (levelExtremes.minX < levelExtremes.maxX || !(levelExtremes.maxYHemisphere.equals(AVKey.SOUTH) && levelExtremes.maxY == 0)) {
                    sb.append(levelExtremes.minX);
                    sb.append(", ");
                    sb.append(levelExtremes.maxX);
                    sb.append(" - ");
                    sb.append(levelExtremes.minY);
                    sb.append(AVKey.NORTH.equals(levelExtremes.minYHemisphere) ? "N" : "S");
                    sb.append(", ");
                    sb.append(levelExtremes.maxY);
                    sb.append(AVKey.NORTH.equals(levelExtremes.maxYHemisphere) ? "N" : "S");
                } else {
                    sb.append("empty");
                }
                sb.append("\n");
            }
            return sb.toString();
        }
    }

    // --- UTM/UPS square zone ------------------------------------------------------------------

    protected ArrayList<SquareZone> createSquaresGrid(int UTMZone, String hemisphere, Sector UTMZoneSector, double minEasting, double maxEasting, double minNorthing, double maxNorthing) {
        ArrayList<SquareZone> squares = new ArrayList<SquareZone>();
        double startEasting = Math.floor(minEasting / ONEHT) * ONEHT;
        double startNorthing = Math.floor(minNorthing / ONEHT) * ONEHT;
        int cols = (int) Math.ceil((maxEasting - startEasting) / ONEHT);
        int rows = (int) Math.ceil((maxNorthing - startNorthing) / ONEHT);
        SquareZone[][] squaresArray = new SquareZone[rows][cols];
        int col = 0;
        for (double easting = startEasting; easting < maxEasting; easting += ONEHT) {
            int row = 0;
            for (double northing = startNorthing; northing < maxNorthing; northing += ONEHT) {
                SquareZone sz = new SquareZone(UTMZone, hemisphere, UTMZoneSector, easting, northing, ONEHT);
                if (sz.boundingSector != null && !sz.isOutsideGridZone()) {
                    squares.add(sz);
                    squaresArray[row][col] = sz;
                }
                row++;
            }
            col++;
        }

        // Keep track of neighbors
        for (col = 0; col < cols; col++) {
            for (int row = 0; row < rows; row++) {
                SquareZone sz = squaresArray[row][col];
                if (sz != null) {
                    sz.setNorthNeighbor(row + 1 < rows ? squaresArray[row + 1][col] : null);
                    sz.setEastNeighbor(col + 1 < cols ? squaresArray[row][col + 1] : null);
                }
            }
        }

        return squares;
    }

    /**
     * Represent a generic UTM/UPS square area
     */
    private class SquareSector {
        //public static final int MIN_CELL_SIZE_PIXELS = 25;//50;

        protected int UTMZone;
        protected String hemisphere;
        protected Sector UTMZoneSector;
        protected double SWEasting;
        protected double SWNorthing;
        protected double size;

        protected Position sw, se, nw, ne;  // Four corners position
        protected Sector boundingSector;
        protected Location centroid;
        protected Location squareCenter;
        protected boolean isTruncated = false;

        public SquareSector(int UTMZone, String hemisphere, Sector UTMZoneSector, double SWEasting, double SWNorthing, double sectorSize) {
            this.UTMZone = UTMZone;
            this.hemisphere = hemisphere;
            this.UTMZoneSector = UTMZoneSector;
            this.SWEasting = SWEasting;
            this.SWNorthing = SWNorthing;
            this.size = sectorSize;

            // Compute corners positions
            this.sw = computePosition(this.UTMZone, this.hemisphere, SWEasting, SWNorthing);
            this.se = computePosition(this.UTMZone, this.hemisphere, SWEasting + size, SWNorthing);
            this.nw = computePosition(this.UTMZone, this.hemisphere, SWEasting, SWNorthing + size);
            this.ne = computePosition(this.UTMZone, this.hemisphere, SWEasting + size, SWNorthing + size);
            this.squareCenter = computePosition(this.UTMZone, this.hemisphere, SWEasting + size / 2, SWNorthing + size / 2);

            // Compute approximate bounding sector and center point
            if (this.sw != null && this.se != null && this.nw != null && this.ne != null) {
                adjustDateLineCrossingPoints();
                this.boundingSector = new Sector();
                this.boundingSector.union(sw.latitude, sw.longitude);
                this.boundingSector.union(ne.latitude, ne.longitude);

                if (!isInsideGridZone()) {
                    this.boundingSector = SectorUtils.intersection(this.UTMZoneSector, this.boundingSector);
                }

                this.centroid = this.boundingSector != null ? this.boundingSector.centroid(new Location()) : this.squareCenter;
                //this.squareCenter = this.boundingSector.getCentroid();
            }

            // Check whether this square is truncated by the grid zone boundary
            this.isTruncated = !isInsideGridZone();
        }

        private void adjustDateLineCrossingPoints() {
            ArrayList<Location> corners = new ArrayList<Location>(Arrays.asList(sw, se, nw, ne));
            if (!Location.locationsCrossAntimeridian(corners)) {
                return;
            }

            double lonSign = 0;
            for (Location corner : corners) {
                if (Math.abs(corner.longitude) != 180) {
                    lonSign = Math.signum(corner.longitude);
                }
            }

            if (lonSign == 0) {
                return;
            }

            if (Math.abs(sw.longitude) == 180 && Math.signum(sw.longitude) != lonSign) {
                sw = new Position(sw.latitude, sw.longitude * -1, sw.altitude);
            }
            if (Math.abs(se.longitude) == 180 && Math.signum(se.longitude) != lonSign) {
                se = new Position(se.latitude, se.longitude * -1, se.altitude);
            }
            if (Math.abs(nw.longitude) == 180 && Math.signum(nw.longitude) != lonSign) {
                nw = new Position(nw.latitude, nw.longitude * -1, nw.altitude);
            }
            if (Math.abs(ne.longitude) == 180 && Math.signum(ne.longitude) != lonSign) {
                ne = new Position(ne.latitude, ne.longitude * -1, ne.altitude);
            }
        }
/*
        public Extent getExtent(Globe globe, double ve) {
            return Sector.computeBoundingCylinder(globe, ve, this.boundingSector);
        }
*/

        public boolean isInView(RenderContext dc) {
            //if (!dc.getView().getFrustumInModelCoordinates().intersects(this.getExtent(dc.getGlobe(), dc.getVerticalExaggeration()))) {
            //    return false;
            //}
            //if (!dc.frustum.intersectsViewport(dc.viewport)) {
            //    return false;
            //}
            if (!this.boundingSector.intersects(UTMBaseGraticuleLayer.this.mapSector)) {
                return false;
            }

            // Check apparent size
            if (getSizeInPixels(dc) <= MIN_CELL_SIZE_PIXELS) {
                return false;
            }

            return true;
        }

        /**
         * Determines whether this square is fully inside its parent grid zone.
         *
         * @return true if this square is totaly inside its parent grid zone.
         */
        @SuppressWarnings({"RedundantIfStatement"})
        public boolean isInsideGridZone() {
            if (!this.isPositionInside(this.nw)) {
                return false;
            }
            if (!this.isPositionInside(this.ne)) {
                return false;
            }
            if (!this.isPositionInside(this.sw)) {
                return false;
            }
            if (!this.isPositionInside(this.se)) {
                return false;
            }
            return true;
        }

        /**
         * Determines whether this square is fully outside its parent grid zone.
         *
         * @return true if this square is totally outside its parent grid zone.
         */
        @SuppressWarnings({"RedundantIfStatement"})
        public boolean isOutsideGridZone() {
            if (this.isPositionInside(this.nw)) {
                return false;
            }
            if (this.isPositionInside(this.ne)) {
                return false;
            }
            if (this.isPositionInside(this.sw)) {
                return false;
            }
            if (this.isPositionInside(this.se)) {
                return false;
            }
            return true;
        }

        public boolean isPositionInside(Position position) {
            return position != null && this.UTMZoneSector.contains(position.latitude, position.longitude);
        }

        public double getSizeInPixels(RenderContext dc) {
            //View view = dc.getView();
            Vec3 centerPoint = getSurfacePoint(dc, this.centroid.latitude, this.centroid.longitude);
            Double distance = dc.cameraPoint.distanceTo(centerPoint);
            return this.size / dc.pixelSizeAtDistance(dc.camera.altitude);
        }
    }

    /**
     * Represent a 100km square zone inside an UTM zone.
     */
    protected class SquareZone extends SquareSector {
        protected String name;
        protected SquareGrid squareGrid;
        protected ArrayList<GridElement> gridElements;

        private SquareZone northNeighbor, eastNeighbor;

        public SquareZone(int UTMZone, String hemisphere, Sector UTMZoneSector, double SWEasting, double SWNorthing, double squareSize) {
            super(UTMZone, hemisphere, UTMZoneSector, SWEasting, SWNorthing, squareSize);
        }

        public void setName(String name) {
            this.name = name;
        }

        public void setNorthNeighbor(SquareZone sz) {
            this.northNeighbor = sz;
        }

        public void setEastNeighbor(SquareZone sz) {
            this.eastNeighbor = sz;
        }

        public void selectRenderables(RenderContext dc, Sector vs) {
            // Select our renderables
            if (this.gridElements == null) {
                createRenderables();
            }

            boolean drawMetricLabels = getSizeInPixels(dc) > MIN_CELL_SIZE_PIXELS * 2;
            String graticuleType = getTypeFor((int) this.size);
            for (GridElement ge : this.gridElements) {
                if (ge.isInView(dc, vs)) {
                    if (ge.type.equals(GridElement.TYPE_LINE_NORTH) && this.isNorthNeighborInView(dc)) {
                        continue;
                    }
                    if (ge.type.equals(GridElement.TYPE_LINE_EAST) && this.isEastNeighborInView(dc)) {
                        continue;
                    }

                    if (drawMetricLabels) {
                        metricScaleSupport.computeMetricScaleExtremes(this.UTMZone, this.hemisphere, ge, this.size * 10);
                    }
                    addRenderable(ge.renderable, graticuleType);
                }
            }

            if (getSizeInPixels(dc) <= MIN_CELL_SIZE_PIXELS * 2) {
                return;
            }

            // Select grid renderables
            if (this.squareGrid == null) {
                this.squareGrid = new SquareGrid(this.UTMZone, this.hemisphere, this.UTMZoneSector, this.SWEasting, this.SWNorthing, this.size);
            }
            if (this.squareGrid.isInView(dc)) {
                this.squareGrid.selectRenderables(dc, vs);
            } else {
                this.squareGrid.clearRenderables();
            }
        }

        private boolean isNorthNeighborInView(RenderContext dc) {
            return this.northNeighbor != null && this.northNeighbor.isInView(dc);
        }

        private boolean isEastNeighborInView(RenderContext dc) {
            return this.eastNeighbor != null && this.eastNeighbor.isInView(dc);
        }

        public void clearRenderables() {
            if (this.gridElements != null) {
                this.gridElements.clear();
                this.gridElements = null;
            }
            if (this.squareGrid != null) {
                this.squareGrid.clearRenderables();
                this.squareGrid = null;
            }
        }

        public void createRenderables() {
            this.gridElements = new ArrayList<GridElement>();

            ArrayList<Position> positions = new ArrayList<Position>();
            Position p1, p2;
            Object polyline;
            Sector lineSector;

            // left segment
            positions.clear();
            if (this.isTruncated) {
                computeTruncatedSegment(sw, nw, this.UTMZoneSector, positions);
            } else {
                positions.add(sw);
                positions.add(nw);
            }
            if (positions.size() > 0) {
                p1 = positions.get(0);
                p2 = positions.get(1);
                polyline = createLineRenderable(new ArrayList<Position>(positions), WorldWind.GREAT_CIRCLE);
                lineSector = new Sector();
                lineSector.union(p1.latitude, p1.longitude);
                lineSector.union(p2.latitude, p2.longitude);
                GridElement ge = new GridElement(lineSector, polyline, GridElement.TYPE_LINE_WEST);
                ge.setValue(this.SWEasting);
                this.gridElements.add(ge);
            }

            // right segment
            positions.clear();
            if (this.isTruncated) {
                computeTruncatedSegment(se, ne, this.UTMZoneSector, positions);
            } else {
                positions.add(se);
                positions.add(ne);
            }
            if (positions.size() > 0) {
                p1 = positions.get(0);
                p2 = positions.get(1);
                polyline = createLineRenderable(new ArrayList<Position>(positions), WorldWind.GREAT_CIRCLE);
                lineSector = new Sector();
                lineSector.union(p1.latitude, p1.longitude);
                lineSector.union(p2.latitude, p2.longitude);
                GridElement ge = new GridElement(lineSector, polyline, GridElement.TYPE_LINE_EAST);
                ge.setValue(this.SWEasting + this.size);
                this.gridElements.add(ge);
            }

            // bottom segment
            positions.clear();
            if (this.isTruncated) {
                computeTruncatedSegment(sw, se, this.UTMZoneSector, positions);
            } else {
                positions.add(sw);
                positions.add(se);
            }
            if (positions.size() > 0) {
                p1 = positions.get(0);
                p2 = positions.get(1);
                polyline = createLineRenderable(new ArrayList<Position>(positions), WorldWind.GREAT_CIRCLE);
                lineSector = new Sector();
                lineSector.union(p1.latitude, p1.longitude);
                lineSector.union(p2.latitude, p2.longitude);
                GridElement ge = new GridElement(lineSector, polyline, GridElement.TYPE_LINE_SOUTH);
                ge.setValue(this.SWNorthing);
                this.gridElements.add(ge);
            }

            // top segment
            positions.clear();
            if (this.isTruncated) {
                computeTruncatedSegment(nw, ne, this.UTMZoneSector, positions);
            } else {
                positions.add(nw);
                positions.add(ne);
            }
            if (positions.size() > 0) {
                p1 = positions.get(0);
                p2 = positions.get(1);
                polyline = createLineRenderable(new ArrayList<Position>(positions), WorldWind.GREAT_CIRCLE);
                lineSector = new Sector();
                lineSector.union(p1.latitude, p1.longitude);
                lineSector.union(p2.latitude, p2.longitude);
                GridElement ge = new GridElement(lineSector, polyline, GridElement.TYPE_LINE_NORTH);
                ge.setValue(this.SWNorthing + this.size);
                this.gridElements.add(ge);
            }

            // Label
            if (this.name != null) {
                // Only add a label to squares above some dimension
                if (this.boundingSector.deltaLongitude() * Math.cos(this.centroid.latitude) > .2 && this.boundingSector.deltaLatitude() > .2) {
                    Location labelPos = null;
                    if (this.UTMZone != 0) // Not at poles
                    {
                        labelPos = this.centroid;
                    } else if (this.isPositionInside(new Position(this.squareCenter.latitude, this.squareCenter.longitude, 0))) {
                        labelPos = this.squareCenter;
                    } else if (this.squareCenter.latitude <= this.UTMZoneSector.maxLatitude() && this.squareCenter.latitude >= this.UTMZoneSector.minLatitude()) {
                        labelPos = this.centroid;
                    }
                    if (labelPos != null) {
                        Label text = new Label(new Position(labelPos.latitude, labelPos.longitude, 0), this.name);
                        //text.setPriority(this.size * 10);
                        this.gridElements.add(new GridElement(this.boundingSector, text, GridElement.TYPE_GRIDZONE_LABEL));
                    }
                }
            }
        }
    }

    /**
     * Represent a square 10x10 grid and recursive tree in easting/northing coordinates
     */
    protected class SquareGrid extends SquareSector {
        private ArrayList<GridElement> gridElements;
        private ArrayList<SquareGrid> subGrids;

        public SquareGrid(int UTMZone, String hemisphere, Sector UTMZoneSector, double SWEasting, double SWNorthing, double size) {
            super(UTMZone, hemisphere, UTMZoneSector, SWEasting, SWNorthing, size);
        }

        @SuppressWarnings({"RedundantIfStatement"})
        public boolean isInView(RenderContext dc) {
            //if (!dc.getView().getFrustumInModelCoordinates().intersects(this.getExtent(dc.getGlobe(), dc.getVerticalExaggeration()))) {
            //    return false;
            //}
            if (!dc.frustum.intersectsViewport(dc.viewport)) {
                return false;
            }

            // Check apparent size
            if (getSizeInPixels(dc) <= MIN_CELL_SIZE_PIXELS * 4) {
                return false;
            }

            return true;
        }

        public void selectRenderables(RenderContext dc, Sector vs) {
            // Select our renderables
            if (this.gridElements == null) {
                createRenderables();
            }

            int gridStep = (int) this.size / 10;
            boolean drawMetricLabels = getSizeInPixels(dc) > MIN_CELL_SIZE_PIXELS * 4 * 1.7;
            String graticuleType = getTypeFor(gridStep);

            for (GridElement ge : this.gridElements) {
                if (ge.isInView(dc, vs)) {
                    if (drawMetricLabels) {
                        metricScaleSupport.computeMetricScaleExtremes(this.UTMZone, this.hemisphere, ge, this.size);
                    }

                    addRenderable(ge.renderable, graticuleType);
                }
            }

            if (getSizeInPixels(dc) <= MIN_CELL_SIZE_PIXELS * 4 * 2) {
                return;
            }

            // Select sub grids renderables
            if (this.subGrids == null) {
                createSubGrids();
            }
            for (SquareGrid sg : this.subGrids) {
                if (sg.isInView(dc)) {
                    sg.selectRenderables(dc, vs);
                } else {
                    sg.clearRenderables();
                }
            }
        }

        public void clearRenderables() {
            if (this.gridElements != null) {
                this.gridElements.clear();
                this.gridElements = null;
            }
            if (this.subGrids != null) {
                for (SquareGrid sg : this.subGrids) {
                    sg.clearRenderables();
                }
                this.subGrids.clear();
                this.subGrids = null;
            }
        }

        public void createSubGrids() {
            this.subGrids = new ArrayList<SquareGrid>();
            double gridStep = this.size / 10;
            for (int i = 0; i < 10; i++) {
                double easting = this.SWEasting + gridStep * i;
                for (int j = 0; j < 10; j++) {
                    double northing = this.SWNorthing + gridStep * j;
                    SquareGrid sg = new SquareGrid(this.UTMZone, this.hemisphere, this.UTMZoneSector, easting, northing, gridStep);
                    if (!sg.isOutsideGridZone()) {
                        this.subGrids.add(sg);
                    }
                }
            }
        }

        public void createRenderables() {
            this.gridElements = new ArrayList<GridElement>();
            double gridStep = this.size / 10;
            Position p1, p2;
            ArrayList<Position> positions = new ArrayList<Position>();

            // South-North lines
            for (int i = 1; i <= 9; i++) {
                double easting = this.SWEasting + gridStep * i;
                positions.clear();
                p1 = computePosition(this.UTMZone, this.hemisphere, easting, SWNorthing);
                p2 = computePosition(this.UTMZone, this.hemisphere, easting, SWNorthing + this.size);
                if (this.isTruncated) {
                    computeTruncatedSegment(p1, p2, this.UTMZoneSector, positions);
                } else {
                    positions.add(p1);
                    positions.add(p2);
                }
                if (positions.size() > 0) {
                    p1 = positions.get(0);
                    p2 = positions.get(1);
                    Object polyline = createLineRenderable(new ArrayList<Position>(positions), WorldWind.GREAT_CIRCLE);
                    Sector lineSector = new Sector();
                    lineSector.union(p1.latitude, p1.longitude);
                    lineSector.union(p2.latitude, p2.longitude);
                    GridElement ge = new GridElement(lineSector, polyline, GridElement.TYPE_LINE_EASTING);
                    ge.setValue(easting);
                    this.gridElements.add(ge);
                }
            }
            // West-East lines
            for (int i = 1; i <= 9; i++) {
                double northing = this.SWNorthing + gridStep * i;
                positions.clear();
                p1 = computePosition(this.UTMZone, this.hemisphere, SWEasting, northing);
                p2 = computePosition(this.UTMZone, this.hemisphere, SWEasting + this.size, northing);
                if (this.isTruncated) {
                    computeTruncatedSegment(p1, p2, this.UTMZoneSector, positions);
                } else {
                    positions.add(p1);
                    positions.add(p2);
                }
                if (positions.size() > 0) {
                    p1 = positions.get(0);
                    p2 = positions.get(1);
                    Object polyline = createLineRenderable(new ArrayList<Position>(positions), WorldWind.GREAT_CIRCLE);
                    Sector lineSector = new Sector();
                    lineSector.union(p1.latitude, p1.longitude);
                    lineSector.union(p2.latitude, p2.longitude);
                    GridElement ge = new GridElement(lineSector, polyline, GridElement.TYPE_LINE_NORTHING);
                    ge.setValue(northing);
                    this.gridElements.add(ge);
                }
            }
        }
    }
}
