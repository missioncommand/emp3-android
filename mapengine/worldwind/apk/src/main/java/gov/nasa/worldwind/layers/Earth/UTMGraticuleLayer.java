/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind.layers.Earth;

import android.graphics.Rect;

import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.geom.Vec3;
import gov.nasa.worldwind.geom.coords.UTMCoord;
import gov.nasa.worldwind.globe.Globe;
import gov.nasa.worldwind.layers.GraticuleRenderingParams;
import gov.nasa.worldwind.render.Color;
import gov.nasa.worldwind.render.RenderContext;
import gov.nasa.worldwind.shape.Label;
import mil.emp3.mapengine.interfaces.IMapInstance;

import java.util.ArrayList;

/**
 * Displays the UTM graticule.
 *
 * @author Patrick Murris
 * @version $Id: UTMGraticuleLayer.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class UTMGraticuleLayer extends UTMBaseGraticuleLayer {
    final static private String TAG = UTMGraticuleLayer.class.getSimpleName();

    /**
     * Graticule for the UTM zone grid.
     */
    public static final String GRATICULE_UTM_GRID = "Graticule.UTM.Grid";
    /**
     * Graticule for the 100,000 meter grid, nested inside the UTM grid.
     */
    public static final String GRATICULE_100000M = "Graticule.100000m";
    /**
     * Graticule for the 10,000 meter grid, nested inside the UTM grid.
     */
    public static final String GRATICULE_10000M = "Graticule.10000m";
    /**
     * Graticule for the 1,000 meter grid, nested inside the UTM grid.
     */
    public static final String GRATICULE_1000M = "Graticule.1000m";
    /**
     * Graticule for the 100 meter grid, nested inside the UTM grid.
     */
    public static final String GRATICULE_100M = "Graticule.100m";
    /**
     * Graticule for the 10 meter grid, nested inside the UTM grid.
     */
    public static final String GRATICULE_10M = "Graticule.10m";
    /**
     * Graticule for the 1 meter grid, nested inside the UTM grid.
     */
    public static final String GRATICULE_1M = "Graticule.1m";

    protected static final int MIN_CELL_SIZE_PIXELS = 40; // TODO: make settable
    protected static final int GRID_ROWS = 8;
    protected static final int GRID_COLS = 60;

    protected GraticuleTile[][] gridTiles = new GraticuleTile[GRID_ROWS][GRID_COLS];

    public UTMGraticuleLayer(IMapInstance mapInstance) {
        super(mapInstance);
        initRenderingParams();
        this.setPickEnabled(false);
        //this.setName(Logging.getMessage("layers.Earth.UTMGraticule.Name"));
        this.metricScaleSupport.setMaxResolution(1e6);
    }

    // --- Graticule Rendering --------------------------------------------------------------

    protected void initRenderingParams() {
        GraticuleRenderingParams params;
        // UTM zone grid
        params = new GraticuleRenderingParams();
        params.setValue(GraticuleRenderingParams.KEY_LINE_COLOR, new Color(1.0f, 1.0f, 1.0f, 0.8f));
        params.setValue(GraticuleRenderingParams.KEY_LABEL_COLOR, new Color(1.0f, 1.0f, 1.0f, 0.8f));
        //params.setValue(GraticuleRenderingParams.KEY_LABEL_FONT, Font.decode("Arial-Bold-16"));
        setRenderingParams(GRATICULE_UTM_GRID, params);
        // 100km
        params = new GraticuleRenderingParams();
        params.setValue(GraticuleRenderingParams.KEY_LINE_COLOR, new Color(0, 1.0f, 0, 0.8f));
        params.setValue(GraticuleRenderingParams.KEY_LABEL_COLOR, new Color(0, 1.0f, 0, 0.8f));
        //params.setValue(GraticuleRenderingParams.KEY_LABEL_FONT, Font.decode("Arial-Bold-14"));
        setRenderingParams(GRATICULE_100000M, params);
        // 10km
        params = new GraticuleRenderingParams();
        params.setValue(GraticuleRenderingParams.KEY_LINE_COLOR, new Color(0, 102/255, 1.0f, 0.8f));
        params.setValue(GraticuleRenderingParams.KEY_LABEL_COLOR, new Color(0, 102/255, 1.0f, 0.8f));
        setRenderingParams(GRATICULE_10000M, params);
        // 1km
        params = new GraticuleRenderingParams();
        params.setValue(GraticuleRenderingParams.KEY_LINE_COLOR, new Color(0, 1.0f, 1.0f, 0.8f));
        params.setValue(GraticuleRenderingParams.KEY_LABEL_COLOR, new Color(0, 1.0f, 1.0f, 0.8f));
        setRenderingParams(GRATICULE_1000M, params);
        // 100m
        params = new GraticuleRenderingParams();
        params.setValue(GraticuleRenderingParams.KEY_LINE_COLOR, new Color(0, 153/255, 153/255, 0.8f));
        params.setValue(GraticuleRenderingParams.KEY_LABEL_COLOR, new Color(0, 153/255, 153/255, 0.8f));
        setRenderingParams(GRATICULE_100M, params);
        // 10m
        params = new GraticuleRenderingParams();
        params.setValue(GraticuleRenderingParams.KEY_LINE_COLOR, new Color(102/255, 1.0f, 204/255, 0.8f));
        params.setValue(GraticuleRenderingParams.KEY_LABEL_COLOR, new Color(102/255, 1.0f, 204/255, 0.8f));
        setRenderingParams(GRATICULE_10M, params);
        // 1m
        params = new GraticuleRenderingParams();
        params.setValue(GraticuleRenderingParams.KEY_LINE_COLOR, new Color(153/255, 153/255, 1.0f, 0.8f));
        params.setValue(GraticuleRenderingParams.KEY_LABEL_COLOR, new Color(153/255, 153/255, 1.0f, 0.8f));
        setRenderingParams(GRATICULE_1M, params);
    }

    protected String[] getOrderedTypes() {
        return new String[]{GRATICULE_UTM_GRID, GRATICULE_100000M, GRATICULE_10000M, GRATICULE_1000M, GRATICULE_100M, GRATICULE_10M, GRATICULE_1M,};
    }

    protected String getTypeFor(int resolution) {
        if (resolution >= 500000) {
            return GRATICULE_UTM_GRID;
        }
        if (resolution >= 100000) {
            return GRATICULE_100000M;
        } else if (resolution >= 10000) {
            return GRATICULE_10000M;
        } else if (resolution >= 1000) {
            return GRATICULE_1000M;
        } else if (resolution >= 100) {
            return GRATICULE_100M;
        } else if (resolution >= 10) {
            return GRATICULE_10M;
        } else if (resolution >= 1) {
            return GRATICULE_1M;
        }

        return null;
    }

    protected void clear(RenderContext dc) {
        super.clear(dc);
        this.applyTerrainConformance();
        this.metricScaleSupport.clear();
        this.metricScaleSupport.computeZone(dc);
    }

    private void applyTerrainConformance() {
        String[] graticuleType = getOrderedTypes();
        for (String type : graticuleType) {
            double lineConformance = type.equals(GRATICULE_UTM_GRID) ? 20 : this.terrainConformance; //.polylineTerrainConformance;
            getRenderingParams(type).setValue(GraticuleRenderingParams.KEY_LINE_CONFORMANCE, lineConformance);
        }
    }

    protected void selectRenderables(RenderContext dc) {
        this.selectUTMRenderables(dc);
        this.metricScaleSupport.selectRenderables(dc);
    }

    /**
     * Select the visible grid elements
     *
     * @param dc the current <code>DrawContext</code>.
     */
    protected void selectUTMRenderables(RenderContext dc) {
        ArrayList<GraticuleTile> tileList = getVisibleTiles(dc);
        if (tileList.size() > 0) {
            for (GraticuleTile gt : tileList) {
                // Select tile visible elements
                gt.selectRenderables(dc);
            }
        }
    }

    protected ArrayList<GraticuleTile> getVisibleTiles(RenderContext dc) {
        ArrayList<GraticuleTile> tileList = new ArrayList<GraticuleTile>();
        Sector vs = this.mapSector;
        if (vs != null) {
            Rect gridRectangle = getGridRectangleForSector(vs);
            if (gridRectangle != null) {
                for (int row = (int) gridRectangle.top; row <= gridRectangle.bottom; row++) {
                    for (int col = (int) gridRectangle.left; col <= gridRectangle.right; col++) {
                        if (gridTiles[row][col] == null) {
                            gridTiles[row][col] = new GraticuleTile(getGridSector(row, col));
                        }
                        if (gridTiles[row][col].isInView(dc)) {
                            tileList.add(gridTiles[row][col]);
                        } else {
                            gridTiles[row][col].clearRenderables();
                        }
                    }
                }
            }
        }
        return tileList;
    }

    private Rect getGridRectangleForSector(Sector sector) {
        int x1 = getGridColumn(sector.minLongitude());
        int x2 = getGridColumn(sector.maxLongitude());
        int y1 = getGridRow(sector.minLatitude());
        int y2 = getGridRow(sector.maxLatitude());
        return new Rect(x1, y1, x2, y2);
    }

    private Sector getGridSector(int row, int col) {
        double deltaLat = UTM_MAX_LATITUDE * 2 / GRID_ROWS;
        double deltaLon = 360 / GRID_COLS;
        double minLat = row == 0 ? UTM_MIN_LATITUDE : -UTM_MAX_LATITUDE + deltaLat * row;
        double maxLat = -UTM_MAX_LATITUDE + deltaLat * (row + 1);
        double minLon = -180 + deltaLon * col;
        double maxLon = minLon + deltaLon;
        return Sector.fromDegrees(minLat, maxLat, minLon, maxLon);
    }

    private int getGridColumn(double longitude) {
        double deltaLon = 360 / GRID_COLS;
        int col = (int) Math.floor((longitude + 180) / deltaLon);
        return Math.min(col, GRID_COLS - 1);
    }

    private int getGridRow(double latitude) {
        double deltaLat = UTM_MAX_LATITUDE * 2 / GRID_ROWS;
        int row = (int) Math.floor((latitude + UTM_MAX_LATITUDE) / deltaLat);
        return Math.max(0, Math.min(row, GRID_ROWS - 1));
    }

    protected void clearTiles() {
        for (int row = 0; row < 2; row++) {
            for (int col = 0; col < 60; col++) {
                if (this.gridTiles[row][col] != null) {
                    this.gridTiles[row][col].clearRenderables();
                    this.gridTiles[row][col] = null;
                }
            }
        }
    }

    // --- Graticule tile ----------------------------------------------------------------------

    protected class GraticuleTile {
        private Sector sector;
        private int zone;
        private String hemisphere;

        private ArrayList<GridElement> gridElements;
        private ArrayList<SquareZone> squares;

        public GraticuleTile(Sector sector) {
            this.sector = sector;
            this.zone = getGridColumn(this.sector.centroidLongitude()) + 1;
            this.hemisphere = this.sector.centroidLatitude() > 0 ? AVKey.NORTH : AVKey.SOUTH;
        }
/*
        public Extent getExtent(Globe globe, double ve) {
            return Sector.computeBoundingCylinder(globe, ve, this.sector);
        }
*/
        @SuppressWarnings({"RedundantIfStatement"})
        public boolean isInView(RenderContext dc) {
            return this.sector.intersects(UTMGraticuleLayer.this.mapSector);
/*
            if (!viewFrustum.intersects(this.getExtent(dc.getGlobe(), dc.getVerticalExaggeration()))) {
                return false;
            }
            return true;
*/
        }

        public double getSizeInPixels(RenderContext dc) {
            //View view = dc.getView();
            Vec3 centerPoint = getSurfacePoint(dc, this.sector.centroidLatitude(), this.sector.centroidLongitude());
            double distance = dc.cameraPoint.distanceTo(centerPoint);
            double tileSizeMeter = Math.toRadians(this.sector.deltaLatitude() * dc.globe.getRadiusAt(this.sector.centroidLatitude(), this.sector.centroidLongitude()));
            return tileSizeMeter / dc.pixelSizeAtDistance(dc.camera.altitude); //view.computePixelSizeAtDistance(distance);
        }

        public void selectRenderables(RenderContext dc) {
            if (this.gridElements == null) {
                this.createRenderables();
            }

            // Select tile grid elements
            int resolution = 500000;  // Top level 6 degrees zones
            String graticuleType = getTypeFor(resolution);
            for (GridElement ge : this.gridElements) {
                if (ge.isInView(dc)) {
                    addRenderable(ge.renderable, graticuleType);
                }
            }

            if (getSizeInPixels(dc) / 10 < MIN_CELL_SIZE_PIXELS * 2) {
                return;
            }

            // Select child elements
            if (this.squares == null) {
                createSquares();
            }
            for (SquareZone sz : this.squares) {
                if (sz.isInView(dc)) {
                    sz.selectRenderables(dc, UTMGraticuleLayer.this.mapSector); //dc.getVisibleSector());
                } else {
                    sz.clearRenderables();
                }
            }
        }

        public void clearRenderables() {
            if (this.gridElements != null) {
                this.gridElements.clear();
                this.gridElements = null;
            }
            if (this.squares != null) {
                for (SquareZone sz : this.squares) {
                    sz.clearRenderables();
                }
                this.squares.clear();
                this.squares = null;
            }
        }

        private void createSquares() {
            try {
                // Find grid zone easting and northing boundaries
                UTMCoord UTM;
                UTM = UTMCoord.fromLatLon(this.sector.minLatitude(), this.sector.centroidLongitude(), globe);
                double minNorthing = UTM.getNorthing();
                UTM = UTMCoord.fromLatLon(this.sector.maxLatitude(), this.sector.centroidLongitude(), globe);
                double maxNorthing = UTM.getNorthing();
                maxNorthing = maxNorthing == 0 ? 10e6 : maxNorthing;
                UTM = UTMCoord.fromLatLon(this.sector.minLatitude(), this.sector.minLongitude(), globe);
                double minEasting = UTM.getEasting();
                UTM = UTMCoord.fromLatLon(this.sector.maxLatitude(), this.sector.minLongitude(), globe);
                minEasting = UTM.getEasting() < minEasting ? UTM.getEasting() : minEasting;
                double maxEasting = 1e6 - minEasting;

                // Create squares
                this.squares = createSquaresGrid(this.zone, this.hemisphere, this.sector, minEasting, maxEasting, minNorthing, maxNorthing);
            } catch (IllegalArgumentException ignore) {
            }
        }

        /**
         * Create the grid elements
         */
        private void createRenderables() {
            this.gridElements = new ArrayList<GridElement>();

            ArrayList<Position> positions = new ArrayList<Position>();

            // Generate west meridian
            positions.clear();
            positions.add(new Position(this.sector.minLatitude(), this.sector.minLongitude(), 0));
            positions.add(new Position(this.sector.maxLatitude(), this.sector.minLongitude(), 0));
            Object polyline = createLineRenderable(positions, WorldWind.LINEAR);
            //Sector lineSector = new Sector(this.sector.minLatitude(), this.sector.maxLatitude(), this.sector.minLongitude(), this.sector.minLongitude());
            Sector lineSector = new Sector();
            lineSector.union(this.sector.minLatitude(), this.sector.minLongitude());
            lineSector.union(this.sector.maxLatitude(), this.sector.minLongitude());
            GridElement ge = new GridElement(lineSector, polyline, GridElement.TYPE_LINE);
            ge.value = this.sector.minLongitude();
            this.gridElements.add(ge);

            // Generate south parallel at south pole and equator
            if (this.sector.minLatitude() == UTM_MIN_LATITUDE || this.sector.minLatitude() == 0) {
                positions.clear();
                positions.add(new Position(this.sector.minLatitude(), this.sector.minLongitude(), 0));
                positions.add(new Position(this.sector.minLatitude(), this.sector.maxLongitude(), 0));
                polyline = createLineRenderable(positions, WorldWind.LINEAR);
                //lineSector = new Sector(this.sector.getMinLatitude(), this.sector.getMinLatitude(), this.sector.getMinLongitude(), this.sector.getMaxLongitude());
                lineSector = new Sector();
                lineSector.union(this.sector.minLatitude(), this.sector.minLongitude());
                lineSector.union(this.sector.minLatitude(), this.sector.maxLongitude());
                ge = new GridElement(lineSector, polyline, GridElement.TYPE_LINE);
                ge.value = this.sector.minLatitude();
                this.gridElements.add(ge);
            }

            // Generate north parallel at north pole
            if (this.sector.maxLatitude() == UTM_MAX_LATITUDE) {
                positions.clear();
                positions.add(new Position(this.sector.maxLatitude(), this.sector.minLongitude(), 0));
                positions.add(new Position(this.sector.maxLatitude(), this.sector.maxLongitude(), 0));
                polyline = createLineRenderable(positions, WorldWind.LINEAR);
                //lineSector = new Sector(this.sector.getMaxLatitude(), this.sector.getMaxLatitude(), this.sector.getMinLongitude(), this.sector.getMaxLongitude());
                lineSector.union(this.sector.maxLatitude(), this.sector.minLongitude());
                lineSector.union(this.sector.maxLatitude(), this.sector.maxLongitude());
                ge = new GridElement(lineSector, polyline, GridElement.TYPE_LINE);
                ge.value = this.sector.maxLatitude();
                this.gridElements.add(ge);
            }

            // Add label
            if (this.hasLabel()) {
                StringBuilder sb = new StringBuilder();
                sb.append(this.zone).append(AVKey.NORTH.equals(this.hemisphere) ? "N" : "S");
                Label text = new Label(new Position(this.sector.centroidLatitude(), this.sector.centroidLongitude(), 0), sb.toString());
                this.gridElements.add(new GridElement(this.sector, text, GridElement.TYPE_GRIDZONE_LABEL));
            }
        }

        private boolean hasLabel() {
            // Has label if it contains hemisphere mid latitude
            double southLat = UTM_MIN_LATITUDE / 2;
            boolean southLabel = this.sector.minLatitude() < southLat && southLat <= this.sector.maxLatitude();

            double northLat = UTM_MAX_LATITUDE / 2;
            boolean northLabel = this.sector.minLatitude() < northLat && northLat <= this.sector.maxLatitude();

            return southLabel || northLabel;
        }
    }
}