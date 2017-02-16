/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind.layers;

import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.geom.Location;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.geom.Vec3;
import gov.nasa.worldwind.globe.GeographicProjection;
import gov.nasa.worldwind.globe.Globe;
import gov.nasa.worldwind.layer.AbstractLayer;
import gov.nasa.worldwind.render.Color;
import gov.nasa.worldwind.render.RenderContext;
import gov.nasa.worldwind.shape.Path;
import gov.nasa.worldwind.util.Logger;
import gov.nasa.worldwind.utils.SectorUtils;
import mil.emp3.mapengine.interfaces.IMapInstance;

import java.util.*;

/**
 * Displays a graticule.
 *
 * @author Patrick Murris
 * @version $Id: AbstractGraticuleLayer.java 2153 2014-07-17 17:33:13Z tgaskins $
 */
public class AbstractGraticuleLayer extends AbstractLayer {
    final static private String TAG = AbstractGraticuleLayer.class.getSimpleName();
    /**
     * Solid line rendering style. This style specifies that a line will be drawn without any breaks. <br>
     * <pre><code>_________</code></pre>
     * <br> is an example of a solid line.
     */
    public static final String LINE_STYLE_SOLID = GraticuleRenderingParams.VALUE_LINE_STYLE_SOLID;
    /**
     * Dashed line rendering style. This style specifies that a line will be drawn as a series of long strokes, with
     * space in between. <br>
     * <pre><code>- - - - -</code></pre>
     * <br> is an example of a dashed line.
     */
    public static final String LINE_STYLE_DASHED = GraticuleRenderingParams.VALUE_LINE_STYLE_DASHED;
    /**
     * Dotted line rendering style. This style specifies that a line will be drawn as a series of evenly spaced "square"
     * dots. <br>
     * <pre><code>. . . . .</code></pre>
     * is an example of a dotted line.
     */
    public static final String LINE_STYLE_DOTTED = GraticuleRenderingParams.VALUE_LINE_STYLE_DOTTED;

    protected ArrayList<GridElement> gridElements;
    protected GraticuleSupport graticuleSupport;
    protected double terrainConformance = 50;
    protected Globe globe;

    // Update reference states
    protected Vec3 lastEyePoint;
    protected double lastViewHeading = 0;
    protected double lastViewPitch = 0;
    protected double lastViewFOV = 0;
    protected double lastVerticalExaggeration = 1;
    protected GeographicProjection lastProjection;
    protected long frameTimeStamp; // used only for 2D continuous globes to determine whether render is in same frame

    protected IMapInstance mapInstance;
    protected Sector mapSector;

    public AbstractGraticuleLayer(IMapInstance mapInstance) {
        this.mapInstance = mapInstance;
        graticuleSupport = new GraticuleSupport(mapInstance);
    }

    /**
     * Returns whether or not graticule lines will be rendered.
     *
     * @param key the rendering parameters key.
     * @return true if graticule lines will be rendered; false otherwise.
     * @throws IllegalArgumentException <code>key</code> is null.
     */
    public boolean isDrawGraticule(String key) {
        if (key == null) {
            String message = Logger.makeMessage(TAG, "isDrawGraticule", "nullValue.StringIsNull");
            Logger.log(Logger.ERROR, message);
            throw new IllegalArgumentException(message);
        } return getRenderingParams(key).isDrawLines();
    }

    /**
     * Sets whether or not graticule lines will be rendered.
     *
     * @param drawGraticule true to render graticule lines; false to disable rendering.
     * @param key           the rendering parameters key.
     * @throws IllegalArgumentException <code>key</code> is null.
     */
    public void setDrawGraticule(boolean drawGraticule, String key) {
        if (key == null) {
            String message = Logger.makeMessage(TAG, "setDrawGraticule", "nullValue.StringIsNull");
            Logger.log(Logger.ERROR, message);
            throw new IllegalArgumentException(message);
        }
        getRenderingParams(key).setDrawLines(drawGraticule);
    }

    /**
     * Returns the graticule line Color.
     *
     * @param key the rendering parameters key.
     * @return Color used to render graticule lines.
     * @throws IllegalArgumentException <code>key</code> is null.
     */
    public Color getGraticuleLineColor(String key) {
        if (key == null) {
            String message = Logger.makeMessage(TAG, "getGraticuleLineColor", "nullValue.StringIsNull");
            Logger.log(Logger.ERROR, message);
            throw new IllegalArgumentException(message);
        }
        return getRenderingParams(key).getLineColor();
    }

    /**
     * Sets the graticule line Color.
     *
     * @param color Color that will be used to render graticule lines.
     * @param key   the rendering parameters key.
     * @throws IllegalArgumentException if <code>color</code> or <code>key</code> is null.
     */
    public void setGraticuleLineColor(Color color, String key) {
        if (color == null) {
            String message = Logger.makeMessage(TAG, "setGraticuleLineColor", "nullValue.StringIsNull");
            Logger.log(Logger.ERROR, message);
            throw new IllegalArgumentException(message);
        }
        if (key == null) {
            String message = Logger.makeMessage(TAG, "setGraticuleLineColor", "nullValue.StringIsNull");
            Logger.log(Logger.ERROR, message);
            throw new IllegalArgumentException(message);
        }

        getRenderingParams(key).setLineColor(color);
    }

    /**
     * Returns the graticule line width.
     *
     * @param key the rendering parameters key.
     * @return width of the graticule lines.
     * @throws IllegalArgumentException <code>key</code> is null.
     */
    public double getGraticuleLineWidth(String key) {
        if (key == null) {
            String message = Logger.makeMessage(TAG, "getGraticuleLineWidth", "nullValue.StringIsNull");
            Logger.log(Logger.ERROR, message);
            throw new IllegalArgumentException(message);
        }
        return getRenderingParams(key).getLineWidth();
    }

    /**
     * Sets the graticule line width.
     *
     * @param lineWidth width of the graticule lines.
     * @param key       the rendering parameters key.
     * @throws IllegalArgumentException <code>key</code> is null.
     */
    public void setGraticuleLineWidth(double lineWidth, String key) {
        if (key == null) {
            String message = Logger.makeMessage(TAG, "setGraticuleLineWidth", "nullValue.StringIsNull");
            Logger.log(Logger.ERROR, message);
            throw new IllegalArgumentException(message);
        }
        getRenderingParams(key).setLineWidth(lineWidth);
    }

    /**
     * Returns the graticule line rendering style.
     *
     * @param key the rendering parameters key.
     * @return rendering style of the graticule lines.
     * @throws IllegalArgumentException <code>key</code> is null.
     */
    public String getGraticuleLineStyle(String key) {
        if (key == null) {
            String message = Logger.makeMessage(TAG, "getGraticuleLineStyle", "nullValue.StringIsNull");
            Logger.log(Logger.ERROR, message);
            throw new IllegalArgumentException(message);
        }
        return getRenderingParams(key).getLineStyle();
    }

    /**
     * Sets the graticule line rendering style.
     *
     * @param lineStyle rendering style of the graticule lines. One of LINE_STYLE_PLAIN, LINE_STYLE_DASHED, or
     *                  LINE_STYLE_DOTTED.
     * @param key       the rendering parameters key.
     * @throws IllegalArgumentException if <code>lineStyle</code> or <code>key</code> is null.
     */
    public void setGraticuleLineStyle(String lineStyle, String key) {
        if (lineStyle == null) {
            String message = Logger.makeMessage(TAG, "setGraticuleLineStyle", "nullValue.StringIsNull");
            Logger.log(Logger.ERROR, message);
            throw new IllegalArgumentException(message);
        }
        if (key == null) {
            String message = Logger.makeMessage(TAG, "setGraticuleLineStyle", "nullValue.StringIsNull");
            Logger.log(Logger.ERROR, message);
            throw new IllegalArgumentException(message);
        }

        getRenderingParams(key).setLineStyle(lineStyle);
    }

    /**
     * Returns whether or not graticule labels will be rendered.
     *
     * @param key the rendering parameters key.
     * @return true if graticule labels will be rendered; false otherwise.
     * @throws IllegalArgumentException <code>key</code> is null.
     */
    public boolean isDrawLabels(String key) {
        if (key == null) {
            String message = Logger.makeMessage(TAG, "isDrawLabels", "nullValue.StringIsNull");
            Logger.log(Logger.ERROR, message);
            throw new IllegalArgumentException(message);
        }
        return getRenderingParams(key).isDrawLabels();
    }

    /**
     * Sets whether or not graticule labels will be rendered.
     *
     * @param drawLabels true to render graticule labels; false to disable rendering.
     * @param key        the rendering parameters key.
     * @throws IllegalArgumentException <code>key</code> is null.
     */
    public void setDrawLabels(boolean drawLabels, String key) {
        if (key == null) {
            String message = Logger.makeMessage(TAG, "setDrawLabels", "nullValue.StringIsNull");
            Logger.log(Logger.ERROR, message);
            throw new IllegalArgumentException(message);
        }
        getRenderingParams(key).setDrawLabels(drawLabels);
    }

    /**
     * Returns the graticule label Color.
     *
     * @param key the rendering parameters key.
     * @return Color used to render graticule labels.
     * @throws IllegalArgumentException <code>key</code> is null.
     */
    public Color getLabelColor(String key) {
        if (key == null) {
            String message = Logger.makeMessage(TAG, "getLabelColor", "nullValue.StringIsNull");
            Logger.log(Logger.ERROR, message);
            throw new IllegalArgumentException(message);
        }
        return getRenderingParams(key).getLabelColor();
    }

    /**
     * Sets the graticule label Color.
     *
     * @param color Color that will be used to render graticule labels.
     * @param key   the rendering parameters key.
     * @throws IllegalArgumentException if <code>color</code> or <code>key</code> is null.
     */
    public void setLabelColor(Color color, String key) {
        if (color == null) {
            String message = Logger.makeMessage(TAG, "setLabelColor", "nullValue.ColorIsNull");
            Logger.log(Logger.ERROR, message);
            throw new IllegalArgumentException(message);
        }
        if (key == null) {
            String message = Logger.makeMessage(TAG, "setLabelColor", "nullValue.StringIsNull");
            Logger.log(Logger.ERROR, message);
            throw new IllegalArgumentException(message);
        }

        getRenderingParams(key).setLabelColor(color);
    }

    /**
     * Returns the Font used for graticule labels.
     *
     * @param key the rendering parameters key.
     * @return Font used to render graticule labels.
     * @throws IllegalArgumentException <code>key</code> is null.
     */
/*
    public Font getLabelFont(String key) {
        if (key == null) {
            String message = Logger.makeMessage(TAG, "getLabelFont", "nullValue.StringIsNull");
            Logger.log(Logger.ERROR, message);
            throw new IllegalArgumentException(message);
        }
        return getRenderingParams(key).getLabelFont();
    }
*/
    /**
     * Sets the Font used for graticule labels.
     *
     //* @param font Font that will be used to render graticule labels.
     * @param key  the rendering parameters key.
     * @throws IllegalArgumentException if <code>font</code> or <code>key</code> is null.
     */
/*
     public void setLabelFont(Font font, String key) {
        if (font == null) {
            String message = Logger.makeMessage(TAG, "setLabelFont", "nullValue.FontIsNull");
            Logger.log(Logger.ERROR, message);
            throw new IllegalArgumentException(message);
        }
        if (key == null) {
            String message = Logger.makeMessage(TAG, "setLabelFont", "nullValue.StringIsNull");
            Logger.log(Logger.ERROR, message);
            throw new IllegalArgumentException(message);
        }

        getRenderingParams(key).setLabelFont(font);
    }
*/
/*
    public String getRestorableState() {
        RestorableSupport rs = RestorableSupport.newRestorableSupport();
        // Creating a new RestorableSupport failed. RestorableSupport logged the problem, so just return null.
        if (rs == null) {
            return null;
        }

        RestorableSupport.StateObject so = rs.addStateObject("renderingParams");
        for (Map.Entry<String, GraticuleRenderingParams> entry : this.graticuleSupport.getAllRenderingParams()) {
            if (entry.getKey() != null && entry.getValue() != null) {
                RestorableSupport.StateObject eso = rs.addStateObject(so, entry.getKey());
                makeRestorableState(entry.getValue(), rs, eso);
            }
        }

        return rs.getStateAsXml();
    }
*/
/*
    private static void makeRestorableState(GraticuleRenderingParams params, RestorableSupport rs, RestorableSupport.StateObject context) {
        if (params != null && rs != null) {
            for (Map.Entry<String, Object> p : params.getEntries()) {
                if (p.getValue() instanceof Color) {
                    rs.addStateValueAsInteger(context, p.getKey() + ".Red", ((Color) p.getValue()).getRed());
                    rs.addStateValueAsInteger(context, p.getKey() + ".Green", ((Color) p.getValue()).getGreen());
                    rs.addStateValueAsInteger(context, p.getKey() + ".Blue", ((Color) p.getValue()).getBlue());
                    rs.addStateValueAsInteger(context, p.getKey() + ".Alpha", ((Color) p.getValue()).getAlpha());
                } else if (p.getValue() instanceof Font) {
                    rs.addStateValueAsString(context, p.getKey() + ".Name", ((Font) p.getValue()).getName());
                    rs.addStateValueAsInteger(context, p.getKey() + ".Style", ((Font) p.getValue()).getStyle());
                    rs.addStateValueAsInteger(context, p.getKey() + ".Size", ((Font) p.getValue()).getSize());
                } else {
                    params.getRestorableStateForAVPair(p.getKey(), p.getValue(), rs, context);
                }
            }
        }
    }
*/
/*
    public void restoreState(String stateInXml) {
        if (stateInXml == null) {
            String message = Logger.makeMessage(TAG, "restoreState", "nullValue.StringIsNull");
            Logger.log(Logger.ERROR, message);
            throw new IllegalArgumentException(message);
        }

        RestorableSupport rs;
        try {
            rs = RestorableSupport.parse(stateInXml);
        } catch (Exception e) {
            // Parsing the document specified by stateInXml failed.
            String message = Logger.makeMessage(TAG, "restoreState", "generic.ExceptionAttemptingToParseStateXml" + stateInXml);
            Logger.log(Logger.ERROR, message);
            throw new IllegalArgumentException(message, e);
        }

        RestorableSupport.StateObject so = rs.getStateObject("renderingParams");
        if (so != null) {
            RestorableSupport.StateObject[] renderParams = rs.getAllStateObjects(so);
            for (RestorableSupport.StateObject rp : renderParams) {
                if (rp != null) {
                    GraticuleRenderingParams params = getRenderingParams(rp.getName());
                    if (params == null) {
                        params = new GraticuleRenderingParams();
                    }
                    restorableStateToParams(params, rs, rp);
                    setRenderingParams(rp.getName(), params);
                }
            }
        }
    }
*/
/*
    private static void restorableStateToParams(AVList params, RestorableSupport rs, RestorableSupport.StateObject context) {
        if (params != null && rs != null) {
            Boolean b = rs.getStateValueAsBoolean(context, GraticuleRenderingParams.KEY_DRAW_LINES);
            if (b != null) {
                params.setValue(GraticuleRenderingParams.KEY_DRAW_LINES, b);
            }

            Integer red = rs.getStateValueAsInteger(context, GraticuleRenderingParams.KEY_LINE_COLOR + ".Red");
            Integer green = rs.getStateValueAsInteger(context, GraticuleRenderingParams.KEY_LINE_COLOR + ".Green");
            Integer blue = rs.getStateValueAsInteger(context, GraticuleRenderingParams.KEY_LINE_COLOR + ".Blue");
            Integer alpha = rs.getStateValueAsInteger(context, GraticuleRenderingParams.KEY_LINE_COLOR + ".Alpha");
            if (red != null && green != null && blue != null && alpha != null) {
                params.setValue(GraticuleRenderingParams.KEY_LINE_COLOR, new Color(red, green, blue, alpha));
            }

            Double d = rs.getStateValueAsDouble(context, GraticuleRenderingParams.KEY_LINE_WIDTH);
            if (d != null) {
                params.setValue(GraticuleRenderingParams.KEY_LINE_WIDTH, d);
            }

            String s = rs.getStateValueAsString(context, GraticuleRenderingParams.KEY_LINE_STYLE);
            if (s != null) {
                params.setValue(GraticuleRenderingParams.KEY_LINE_STYLE, s);
            }

            b = rs.getStateValueAsBoolean(context, GraticuleRenderingParams.KEY_DRAW_LABELS);
            if (b != null) {
                params.setValue(GraticuleRenderingParams.KEY_DRAW_LABELS, b);
            }

            red = rs.getStateValueAsInteger(context, GraticuleRenderingParams.KEY_LABEL_COLOR + ".Red");
            green = rs.getStateValueAsInteger(context, GraticuleRenderingParams.KEY_LABEL_COLOR + ".Green");
            blue = rs.getStateValueAsInteger(context, GraticuleRenderingParams.KEY_LABEL_COLOR + ".Blue");
            alpha = rs.getStateValueAsInteger(context, GraticuleRenderingParams.KEY_LABEL_COLOR + ".Alpha");
            if (red != null && green != null && blue != null && alpha != null) {
                params.setValue(GraticuleRenderingParams.KEY_LABEL_COLOR, new Color(red, green, blue, alpha));
            }

            String name = rs.getStateValueAsString(context, GraticuleRenderingParams.KEY_LABEL_FONT + ".Name");
            Integer style = rs.getStateValueAsInteger(context, GraticuleRenderingParams.KEY_LABEL_FONT + ".Style");
            Integer size = rs.getStateValueAsInteger(context, GraticuleRenderingParams.KEY_LABEL_FONT + ".Size");
            if (name != null && style != null && size != null) {
                params.setValue(GraticuleRenderingParams.KEY_LABEL_FONT, new Font(name, style, size));
            }
        }
    }
*/
    // --- Graticule Rendering --------------------------------------------------------------

    protected GraticuleRenderingParams getRenderingParams(String key) {
        if (key == null) {
            String message = Logger.makeMessage(TAG, "getRenderingParams", "nullValue.KeyIsNull");
            Logger.log(Logger.ERROR, message);
            throw new IllegalArgumentException(message);
        }

        return this.graticuleSupport.getRenderingParams(key);
    }

    protected void setRenderingParams(String key, GraticuleRenderingParams renderingParams) {
        if (key == null) {
            String message = Logger.makeMessage(TAG, "setRenderingParams", "nullValue.KeyIsNull");
            Logger.log(Logger.ERROR, message);
            throw new IllegalArgumentException(message);
        }

        this.graticuleSupport.setRenderingParams(key, renderingParams);
    }

    protected void addRenderable(Object renderable, String paramsKey) {
        if (renderable == null) {
            String message = Logger.makeMessage(TAG, "addRenderable", "nullValue.ObjectIsNull");
            Logger.log(Logger.ERROR, message);
            throw new IllegalArgumentException(message);
        }

        this.graticuleSupport.addRenderable(renderable, paramsKey);
    }

    protected void removeAllRenderables() {
        this.graticuleSupport.removeAllRenderables();
    }

    public void doPreRender(RenderContext dc) {
        if (dc == null) {
            String message = Logger.makeMessage(TAG, "doPreRender", "nullValue.DrawContextIsNull");
            Logger.log(Logger.ERROR, message);
            throw new IllegalArgumentException(message);
        }
/*
        if (dc.isContinuous2DGlobe()) {
            if (this.needsToUpdate(dc)) {
                this.clear(dc);
                this.selectRenderables(dc);
            }

            // If the frame time stamp is the same, then this is the second or third pass of the same frame. We continue
            // selecting renderables in these passes.
            if (dc.getFrameTimeStamp() == this.frameTimeStamp) {
                this.selectRenderables(dc);
            }

            this.frameTimeStamp = dc.getFrameTimeStamp();
        } else {
*/
            this.mapSector = SectorUtils.getMapSector(this.mapInstance);

            if (this.needsToUpdate(dc)) {
                this.clear(dc);
                this.selectRenderables(dc);
            }
//        }
    }

    public void doRender(RenderContext dc) {
        if (dc == null) {
            String message = Logger.makeMessage(TAG, "doRender", "nullValue.DrawContextIsNull");
            Logger.log(Logger.ERROR, message);
            throw new IllegalArgumentException(message);
        }

        this.doPreRender(dc);

        // Render
        this.renderGraticule(dc);
    }

    protected void renderGraticule(RenderContext dc) {
        if (dc == null) {
            String message = Logger.makeMessage(TAG, "renderGraticule", "nullValue.DrawContextIsNull");
            Logger.log(Logger.ERROR, message);
            throw new IllegalArgumentException(message);
        }

        this.graticuleSupport.render(dc, this.getOpacity());
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

        // This method is intended to be overriden by subclasses

    }

    /**
     * Determines whether the grid should be updated. It returns true if: <ul> <li>the eye has moved more than 1% of its
     * altitude above ground <li>the view FOV, heading or pitch have changed more than 1 degree <li>vertical
     * exaggeration has changed </ul
     *
     * @param dc the current <code>DrawContext</code>.
     * @return true if the graticule should be updated.
     */
    @SuppressWarnings({"RedundantIfStatement"})
    protected boolean needsToUpdate(RenderContext dc) {
        if (this.lastEyePoint == null) {
            return true;
        }

        double altitudeAboveGround = computeAltitudeAboveGround(dc);
        if (dc.cameraPoint.distanceTo(this.lastEyePoint) > altitudeAboveGround / 100)  // 1% of AAG
        {
            return true;
        }

        if (this.lastVerticalExaggeration != dc.verticalExaggeration) {
            return true;
        }

        if (Math.abs(this.lastViewHeading - dc.camera.heading) > 1) {
            return true;
        }
        if (Math.abs(this.lastViewPitch - dc.camera.tilt) > 1) {
            return true;
        }

        if (Math.abs(this.lastViewFOV - dc.fieldOfView) > 1) {
            return true;
        }

        // We must test the globe and its projection to see if either changed. We can't simply use the globe state
        // key for this because we don't want a 2D globe offset change to cause an update. Offset changes don't
        // invalidate the current set of renderables.

        if (dc.globe != this.globe) {
            return true;
        }

        return false;
    }

    protected void clear(RenderContext dc) {
        this.removeAllRenderables();
        this.terrainConformance = computeTerrainConformance(dc);
        this.globe = dc.globe;
        //this.lastEyePoint = dc.getView().getEyePoint();
        this.lastEyePoint = new Vec3(dc.cameraPoint);
        //this.lastViewFOV = dc.getView().getFieldOfView().degrees;
        this.lastViewFOV = dc.fieldOfView;
        //this.lastViewHeading = dc.getView().getHeading().degrees;
        this.lastViewHeading = dc.camera.heading;
        //this.lastViewPitch = dc.getView().getPitch().degrees;
        this.lastViewPitch = dc.camera.tilt;
        //this.lastVerticalExaggeration = dc.getVerticalExaggeration();
        this.lastVerticalExaggeration = dc.verticalExaggeration;

        //if (dc.is2DGlobe()) {
        //    this.lastProjection = ((Globe2D) dc.getGlobe()).getProjection();
        //}
    }

    protected double computeTerrainConformance(RenderContext dc) {
        int value = 100;
        //double alt = dc.getView().getEyePosition().getElevation();
        double alt = dc.camera.altitude;
        if (alt < 10e3) {
            value = 20;
        } else if (alt < 50e3) {
            value = 30;
        } else if (alt < 100e3) {
            value = 40;
        } else if (alt < 1000e3) {
            value = 60;
        }

        return value;
    }

    protected Location computeLabelOffset(RenderContext dc) {
        Location labelPos;
        // Compute labels offset from view center
        //if (dc.getView() instanceof OrbitView) {
            //OrbitView view = (OrbitView) dc.getView();
            //Position centerPos = view.getCenterPosition();
        Position centerPos = new Position(dc.camera.latitude, dc.camera.longitude, dc.camera.altitude);
            //Double pixelSizeDegrees = Angle.fromRadians(view.computePixelSizeAtDistance(view.getZoom()) / dc.getGlobe().getEquatorialRadius()).degrees;
        Double pixelSizeDegrees = Math.toDegrees(dc.pixelSizeAtDistance(dc.camera.altitude) / dc.globe.getEquatorialRadius());
            //Double labelOffsetDegrees = pixelSizeDegrees * view.getViewport().getWidth() / 4;
        Double labelOffsetDegrees = pixelSizeDegrees * dc.viewport.width / 4;
            //labelPos = LatLon.fromDegrees(centerPos.getLatitude().degrees - labelOffsetDegrees, centerPos.getLongitude().degrees - labelOffsetDegrees);
        labelPos = Location.fromDegrees(centerPos.latitude - labelOffsetDegrees, centerPos.longitude - labelOffsetDegrees);
            //Double labelLatDegrees = labelPos.getLatitude().normalizedLatitude().degrees;
        Double labelLatDegrees = labelPos.latitude;
            labelLatDegrees = Math.min(Math.max(labelLatDegrees, -70), 70);
            //labelPos = new LatLon(Angle.fromDegrees(labelLatDegrees), labelPos.getLongitude().normalizedLongitude());
        labelPos = new Location(labelLatDegrees, labelPos.longitude);
        //} else {
        //    labelPos = dc.getView().getEyePosition(); // fall back if no orbit view
        //}

        return labelPos;
    }

    protected Object createLineRenderable(List<Position> positions, int pathType) {
        Path path = new Path(positions);
        path.setPathType(pathType);
        path.setFollowTerrain(true); //.setTerrainConformance(1);
        path.setAltitudeMode(WorldWind.CLAMP_TO_GROUND);
        return path;
    }

    protected class GridElement {
        public final static String TYPE_LINE = "GridElement_Line";
        public final static String TYPE_LINE_NORTH = "GridElement_LineNorth";
        public final static String TYPE_LINE_SOUTH = "GridElement_LineSouth";
        public final static String TYPE_LINE_WEST = "GridElement_LineWest";
        public final static String TYPE_LINE_EAST = "GridElement_LineEast";
        public final static String TYPE_LINE_NORTHING = "GridElement_LineNorthing";
        public final static String TYPE_LINE_EASTING = "GridElement_LineEasting";
        public final static String TYPE_GRIDZONE_LABEL = "GridElement_GridZoneLabel";
        public final static String TYPE_LONGITUDE_LABEL = "GridElement_LongitudeLabel";
        public final static String TYPE_LATITUDE_LABEL = "GridElement_LatitudeLabel";
        private final String TAG = GridElement.class.getSimpleName();

        public final Sector sector;
        public final Object renderable;
        public final String type;
        public double value;

        public GridElement(Sector sector, Object renderable, String type) {
            if (sector == null) {
                String message = Logger.makeMessage(TAG, "GridElement", "nullValue.SectorIsNull");
                Logger.log(Logger.ERROR, message);
                throw new IllegalArgumentException(message);
            }
            if (renderable == null) {
                String message = Logger.makeMessage(TAG, "GridElement", "nullValue.ObjectIsNull");
                Logger.log(Logger.ERROR, message);
                throw new IllegalArgumentException(message);
            }
            if (type == null) {
                String message = Logger.makeMessage(TAG, "GridElement", "nullValue.StringIsNull");
                Logger.log(Logger.ERROR, message);
                throw new IllegalArgumentException(message);
            }
            this.sector = sector;
            this.renderable = renderable;
            this.type = type;
        }

        public void setValue(double value) {
            this.value = value;
        }

        public boolean isInView(RenderContext dc) {
            if (dc == null) {
                String message = Logger.makeMessage(TAG, "isInView", "nullValue.DrawContextIsNull");
                Logger.log(Logger.ERROR, message);
                throw new IllegalArgumentException(message);
            }
            //return isInView(dc, dc.terrain.getMapSector());
            return isInView(dc, AbstractGraticuleLayer.this.mapSector);
        }

        @SuppressWarnings({"RedundantIfStatement"})
        public boolean isInView(RenderContext dc, Sector vs) {
            if (dc == null) {
                String message = Logger.makeMessage(TAG, "isInView", "nullValue.DrawContextIsNull");
                Logger.log(Logger.ERROR, message);
                throw new IllegalArgumentException(message);
            }
            if (vs == null) {
                String message = Logger.makeMessage(TAG, "isInView", "nullValue.SectorIsNull");
                Logger.log(Logger.ERROR, message);
                throw new IllegalArgumentException(message);
            }
            if (!this.sector.intersects(vs)) {
                return false;
            }

            return true;
        }
    }

    // === Support methods ===

    protected Vec3 getSurfacePoint(RenderContext dc, double latitude, double longitude) {
        Vec3 newVec = new Vec3();
        Vec3 surfacePoint = dc.globe.geographicToCartesianNormal(latitude, longitude, newVec); //.getSurfaceGeometry().getSurfacePoint(latitude, longitude);
        if (surfacePoint == null) {
            surfacePoint = dc.globe.geographicToCartesian(latitude, longitude, dc.globe.getRadiusAt(latitude, longitude), newVec);
        }

        return surfacePoint;
    }

    protected double computeAltitudeAboveGround(RenderContext dc) {
        //View view = dc.getView();
        //Position eyePosition = view.getEyePosition();
        //Vec4 surfacePoint = getSurfacePoint(dc, eyePosition.getLatitude(), eyePosition.getLongitude());
        //Vec3 surfacePoint = getSurfacePoint(dc, dc.camera.latitude, dc.camera.longitude);

        //return view.getEyePoint().distanceTo3(surfacePoint);
        return dc.camera.altitude; //dc.cameraPoint.distanceTo(surfacePoint);
    }

    protected void computeTruncatedSegment(Position p1, Position p2, Sector sector, ArrayList<Position> positions) {
        if (p1 == null || p2 == null) {
            return;
        }

        boolean p1In = sector.contains(p1.latitude, p1.longitude);
        boolean p2In = sector.contains(p2.latitude, p2.longitude);
        if (!p1In && !p2In) {
            // whole segment is (likely) outside
            return;
        }
        if (p1In && p2In) {
            // whole segment is (likely) inside
            positions.add(p1);
            positions.add(p2);
        } else {
            // segment does cross the boundary
            Position outPoint = !p1In ? p1 : p2;
            Position inPoint = p1In ? p1 : p2;
            for (int i = 1; i <= 2; i++)  // there may be two intersections
            {
                Location intersection = null;
                if (outPoint.longitude > sector.maxLongitude() || (sector.maxLongitude() == 180 && outPoint.longitude < 0)) {
                    // intersect with east meridian
                    intersection = greatCircleIntersectionAtLongitude(inPoint, outPoint, sector.maxLongitude());
                } else if (outPoint.longitude < sector.minLongitude() || (sector.minLongitude() == -180 && outPoint.longitude > 0)) {
                    // intersect with west meridian
                    intersection = greatCircleIntersectionAtLongitude(inPoint, outPoint, sector.minLongitude());
                } else if (outPoint.latitude > sector.maxLatitude()) {
                    // intersect with top parallel
                    intersection = greatCircleIntersectionAtLatitude(inPoint, outPoint, sector.maxLatitude());
                } else if (outPoint.latitude < sector.minLatitude()) {
                    // intersect with bottom parallel
                    intersection = greatCircleIntersectionAtLatitude(inPoint, outPoint, sector.minLatitude());
                }
                if (intersection != null) {
                    outPoint = new Position(intersection.latitude, intersection.longitude, outPoint.altitude);
                } else {
                    break;
                }
            }
            positions.add(inPoint);
            positions.add(outPoint);
        }
    }

    /**
     * Computes the intersection point position between a great circle segment and a meridian.
     *
     * @param p1        the great circle segment start position.
     * @param p2        the great circle segment end position.
     * @param longitude the meridian longitude <code>Angle</code>
     * @return the intersection <code>Position</code> or null if there was no intersection found.
     */
    protected Location greatCircleIntersectionAtLongitude(Location p1, Location p2, double longitude) {
        if (p1.longitude == longitude) {
            return p1;
        }
        if (p2.longitude == longitude) {
            return p2;
        }
        Location pos = null;
        Double deltaLon = getDeltaLongitude(p1, p2.longitude);
        if (getDeltaLongitude(p1, longitude) < deltaLon && getDeltaLongitude(p2, longitude) < deltaLon) {
            int count = 0;
            double precision = 1d / 6378137d; // 1m angle in radians
            Location a = p1;
            Location b = p2;
            Location midPoint = greatCircleMidPoint(a, b);
            while (getDeltaLongitude(midPoint, longitude) > precision && count <= 20) {
                count++;
                if (getDeltaLongitude(a, longitude) < getDeltaLongitude(b, longitude)) {
                    b = midPoint;
                } else {
                    a = midPoint;
                }
                midPoint = greatCircleMidPoint(a, b);
            }
            pos = midPoint;
            //if (count >= 20)
            //    System.out.println("Warning dichotomy loop aborted: " + p1 + " - " + p2 + " for lon " + longitude + " = " + pos);
        }
        // Adjust final longitude for an exact match
        if (pos != null) {
            pos = new Location(pos.latitude, longitude);
        }
        return pos;
    }

    /**
     * Computes the intersection point position between a great circle segment and a parallel.
     *
     * @param p1       the great circle segment start position.
     * @param p2       the great circle segment end position.
     * @param latitude the parallel latitude <code>Angle</code>
     * @return the intersection <code>Position</code> or null if there was no intersection found.
     */
    protected Location greatCircleIntersectionAtLatitude(Location p1, Location p2, double latitude) {
        Location pos = null;
        if (Math.signum(p1.latitude - latitude) != Math.signum(p2.latitude - latitude)) {
            int count = 0;
            double precision = Math.toDegrees(1d / 6378137d); // 1m angle in radians
            Location a = p1;
            Location b = p2;
            Location midPoint = greatCircleMidPoint(a, b);
            while (Math.abs(midPoint.latitude - latitude) > precision && count <= 20) {
                count++;
                if (Math.signum(a.latitude - latitude) != Math.signum(midPoint.latitude - latitude)) {
                    b = midPoint;
                } else {
                    a = midPoint;
                }
                midPoint = greatCircleMidPoint(a, b);
            }
            pos = midPoint;
            //if (count >= 20)
            //    System.out.println("Warning dichotomy loop aborted: " + p1 + " - " + p2 + " for lat " + latitude + " = " + pos);
        }
        // Adjust final latitude for an exact match
        if (pos != null) {
            pos = new Location(latitude, pos.longitude);
        }
        return pos;
    }

    protected Location greatCircleMidPoint(Location p1, Location p2) {
        double azimuth = p1.greatCircleAzimuth(p2);
        double distance = p1.greatCircleDistance(p2);
        return p1.greatCircleLocation(azimuth, distance / 2, new Location());
    }

    protected double getDeltaLongitude(Location p1, double longitude) {
        double deltaLon = Math.abs(p1.longitude - longitude);
        return deltaLon < 180 ? deltaLon : 360 - deltaLon;
    }
}
