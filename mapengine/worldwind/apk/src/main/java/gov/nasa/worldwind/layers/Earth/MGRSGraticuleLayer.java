/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind.layers.Earth;


import android.graphics.Rect;
import android.graphics.Typeface;

import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.geom.coords.MGRSCoord;
import gov.nasa.worldwind.geom.coords.UTMCoord;
import gov.nasa.worldwind.layers.GraticuleRenderingParams;
import gov.nasa.worldwind.render.Color;
import gov.nasa.worldwind.render.RenderContext;
import gov.nasa.worldwind.shape.Label;
import gov.nasa.worldwind.util.Logger;
import gov.nasa.worldwind.utils.SectorUtils;
import mil.emp3.mapengine.interfaces.IMapInstance;

import java.util.ArrayList;

/**
 * @author Patrick Murris
 * @version $Id: MGRSGraticuleLayer.java 2153 2014-07-17 17:33:13Z tgaskins $
 */

public class MGRSGraticuleLayer extends UTMBaseGraticuleLayer {
    final static private String TAG = MGRSGraticuleLayer.class.getSimpleName();

    /**
     * Graticule for the UTM grid.
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

    private GridZone[][] gridZones = new GridZone[20][60]; // row/col
    private GridZone[] poleZones = new GridZone[4]; // North x2 + South x2
    private double zoneMaxAltitude = 5000e3;
    private double squareMaxAltitude = 3000e3;

    /**
     * Creates a new <code>MGRSGraticuleLayer</code>, with default graticule attributes.
     */
    public MGRSGraticuleLayer(IMapInstance mapInstance) {
        super(mapInstance);
        initRenderingParams();
        this.metricScaleSupport.setScaleModulo((int) 100e3);
        //this.setName(Logging.getMessage("layers.Earth.MGRSGraticule.Name"));
        this.setOpacity(0.6);
    }

    /**
     * Returns the maximum resolution graticule that will be rendered, or null if no graticules will be rendered. By
     * default, all graticules are rendered, and this will return GRATICULE_1M.
     *
     * @return maximum resolution rendered.
     */
    public String getMaximumGraticuleResolution() {
        String maxTypeDrawn = null;
        String[] orderedTypeList = getOrderedTypes();
        for (String type : orderedTypeList) {
            GraticuleRenderingParams params = getRenderingParams(type);
            if (params.isDrawLines()) {
                maxTypeDrawn = type;
            }
        }
        return maxTypeDrawn;
    }

    /**
     * Sets the maxiumum resolution graticule that will be rendered.
     *
     * @param graticuleType one of GRATICULE_UTM, GRATICULE_UTM_GRID, GRATICULE_100000M, GRATICULE_10000M,
     *                      GRATICULE_1000M, GRATICULE_100M, GRATICULE_10M, or GRATICULE_1M.
     * @throws IllegalArgumentException if <code>graticuleType</code> is null, or if <code>graticuleType</code> is not a
     *                                  valid type.
     */
    public void setMaximumGraticuleResolution(String graticuleType) {
        if (graticuleType == null) {
            String message = Logger.makeMessage(TAG, "setMaximumGraticuleResolution", "nullValue.StringIsNull");
            Logger.log(Logger.ERROR, message);
            throw new IllegalArgumentException(message);
        }

        boolean pastTarget = false;
        String[] orderedTypeList = getOrderedTypes();
        for (String type : orderedTypeList) {
            // Enable all graticulte BEFORE and INCLUDING the target.
            // Disable all graticules AFTER the target.
            GraticuleRenderingParams params = getRenderingParams(type);
            params.setDrawLines(!pastTarget);
            params.setDrawLabels(!pastTarget);
            if (!pastTarget && type.equals(graticuleType)) {
                pastTarget = true;
            }
        }
    }

    /**
     * Returns the line color of the specified graticule.
     *
     * @param graticuleType one of GRATICULE_UTM, GRATICULE_UTM_GRID, GRATICULE_100000M, GRATICULE_10000M,
     *                      GRATICULE_1000M, GRATICULE_100M, GRATICULE_10M, or GRATICULE_1M.
     * @return Color of the the graticule line.
     * @throws IllegalArgumentException if <code>graticuleType</code> is null, or if <code>graticuleType</code> is not a
     *                                  valid type.
     */
    public Color getGraticuleLineColor(String graticuleType) {
        if (graticuleType == null) {
            String message = Logger.makeMessage(TAG, "getGraticuleLineColor", "nullValue.StringIsNull");
            Logger.log(Logger.ERROR, message);
            throw new IllegalArgumentException(message);
        }

        return getRenderingParams(graticuleType).getLineColor();
    }

    /**
     * Sets the line rendering color for the specified graticule.
     *
     * @param color         the line color for the specified graticule.
     * @param graticuleType one of GRATICULE_UTM, GRATICULE_UTM_GRID, GRATICULE_100000M, GRATICULE_10000M,
     *                      GRATICULE_1000M, GRATICULE_100M, GRATICULE_10M, or GRATICULE_1M.
     * @throws IllegalArgumentException if<code>color</code> is null, if <code>graticuleType</code> is null, or if
     *                                  <code>graticuleType</code> is not a valid type.
     */
    public void setGraticuleLineColor(Color color, String graticuleType) {
        if (color == null) {
            String message = Logger.makeMessage(TAG, "setGraticuleLineColor", "nullValue.ColorIsNull");
            Logger.log(Logger.ERROR, message);
            throw new IllegalArgumentException(message);
        }
        if (graticuleType == null) {
            String message = Logger.makeMessage(TAG, "setGraticuleLineColor", "nullValue.StringIsNull");
            Logger.log(Logger.ERROR, message);
            throw new IllegalArgumentException(message);
        }

        getRenderingParams(graticuleType).setLineColor(color);
    }

    /**
     * Sets the line rendering color for the specified graticules.
     *
     * @param color         the line color for the specified graticules.
     * @param graticuleType one of GRATICULE_UTM, GRATICULE_UTM_GRID, GRATICULE_100000M, GRATICULE_10000M,
     *                      GRATICULE_1000M, GRATICULE_100M, GRATICULE_10M, or GRATICULE_1M.
     * @throws IllegalArgumentException if<code>color</code> is null, if <code>graticuleType</code> is null, or if
     *                                  <code>graticuleType</code> is not a valid type.
     */
    public void setGraticuleLineColor(Color color, Iterable<String> graticuleType) {
        if (color == null) {
            String message = Logger.makeMessage(TAG, "setGraticuleLineColor", "nullValue.ColorIsNull");
            Logger.log(Logger.ERROR, message);
            throw new IllegalArgumentException(message);
        }
        if (graticuleType == null) {
            String message = Logger.makeMessage(TAG, "setGraticuleLineColor", "nullValue.IterableIsNull");
            Logger.log(Logger.ERROR, message);
            throw new IllegalArgumentException(message);
        }

        for (String type : graticuleType) {
            setGraticuleLineColor(color, type);
        }
    }

    /**
     * Sets the line rendering color for all graticules.
     *
     * @param color the line color.
     * @throws IllegalArgumentException if <code>color</code> is null.
     */
    public void setGraticuleLineColor(Color color) {
        if (color == null) {
            String message = Logger.makeMessage(TAG, "setGraticuleLineColor", "nullValue.ColorIsNull");
            Logger.log(Logger.ERROR, message);
            throw new IllegalArgumentException(message);
        }

        String[] graticuleType = getOrderedTypes();
        for (String type : graticuleType) {
            setGraticuleLineColor(color, type);
        }
    }

    /**
     * Returns the line width of the specified graticule.
     *
     * @param graticuleType one of GRATICULE_UTM, GRATICULE_UTM_GRID, GRATICULE_100000M, GRATICULE_10000M,
     *                      GRATICULE_1000M, GRATICULE_100M, GRATICULE_10M, or GRATICULE_1M.
     * @return width of the graticule line.
     * @throws IllegalArgumentException if <code>graticuleType</code> is null, or if <code>graticuleType</code> is not a
     *                                  valid type.
     */
    public double getGraticuleLineWidth(String graticuleType) {
        if (graticuleType == null) {
            String message = Logger.makeMessage(TAG, "getGraticuleLineWidth", "nullValue.StringIsNull");
            Logger.log(Logger.ERROR, message);
            throw new IllegalArgumentException(message);
        }

        return getRenderingParams(graticuleType).getLineWidth();
    }

    /**
     * Sets the line rendering width for the specified graticule.
     *
     * @param lineWidth     the line rendering width for the specified graticule.
     * @param graticuleType one of GRATICULE_UTM, GRATICULE_UTM_GRID, GRATICULE_100000M, GRATICULE_10000M,
     *                      GRATICULE_1000M, GRATICULE_100M, GRATICULE_10M, or GRATICULE_1M.
     * @throws IllegalArgumentException if <code>graticuleType</code> is null, or if <code>graticuleType</code> is not a
     *                                  valid type.
     */
    public void setGraticuleLineWidth(double lineWidth, String graticuleType) {
        if (graticuleType == null) {
            String message = Logger.makeMessage(TAG, "setGraticuleLineWidth", "nullValue.StringIsNull");
            Logger.log(Logger.ERROR, message);
            throw new IllegalArgumentException(message);
        }

        getRenderingParams(graticuleType).setLineWidth(lineWidth);
    }

    /**
     * Sets the line rendering width for the specified graticules.
     *
     * @param lineWidth     the line rendering width for the specified graticules.
     * @param graticuleType one of GRATICULE_UTM, GRATICULE_UTM_GRID, GRATICULE_100000M, GRATICULE_10000M,
     *                      GRATICULE_1000M, GRATICULE_100M, GRATICULE_10M, or GRATICULE_1M.
     * @throws IllegalArgumentException if <code>graticuleType</code> is null, or if <code>graticuleType</code> is not a
     *                                  valid type.
     */
    public void setGraticuleLineWidth(double lineWidth, Iterable<String> graticuleType) {
        if (graticuleType == null) {
            String message = Logger.makeMessage(TAG, "setGraticuleLineWidth", "nullValue.IterableIsNull");
            Logger.log(Logger.ERROR, message);
            throw new IllegalArgumentException(message);
        }

        for (String type : graticuleType) {
            setGraticuleLineWidth(lineWidth, type);
        }
    }

    /**
     * Sets the line rendering width for all graticules.
     *
     * @param lineWidth the line rendering width.
     */
    public void setGraticuleLineWidth(double lineWidth) {
        String[] graticuleType = getOrderedTypes();
        for (String type : graticuleType) {
            setGraticuleLineWidth(lineWidth, type);
        }
    }

    /**
     * Returns the line rendering style of the specified graticule.
     *
     * @param graticuleType one of GRATICULE_UTM, GRATICULE_UTM_GRID, GRATICULE_100000M, GRATICULE_10000M,
     *                      GRATICULE_1000M, GRATICULE_100M, GRATICULE_10M, or GRATICULE_1M.
     * @return line rendering style of the graticule.
     * @throws IllegalArgumentException if <code>graticuleType</code> is null, or if <code>graticuleType</code> is not a
     *                                  valid type.
     */
    public String getGraticuleLineStyle(String graticuleType) {
        if (graticuleType == null) {
            String message = Logger.makeMessage(TAG, "getGraticuleLineStyle", "nullValue.StringIsNull");
            Logger.log(Logger.ERROR, message);
            throw new IllegalArgumentException(message);
        }

        return getRenderingParams(graticuleType).getLineStyle();
    }

    /**
     * Sets the line rendering style for the specified graticule.
     *
     * @param lineStyle     the line rendering style for the specified graticule. One of LINE_STYLE_PLAIN,
     *                      LINE_STYLE_DASHED, or LINE_STYLE_DOTTED.
     * @param graticuleType one of GRATICULE_UTM, GRATICULE_UTM_GRID, GRATICULE_100000M, GRATICULE_10000M,
     *                      GRATICULE_1000M, GRATICULE_100M, GRATICULE_10M, or GRATICULE_1M
     * @throws IllegalArgumentException if <code>lineStyle</code> is null, if <code>graticuleType</code> is null, or if
     *                                  <code>graticuleType</code> is not a valid type.
     */
    public void setGraticuleLineStyle(String lineStyle, String graticuleType) {
        if (lineStyle == null) {
            String message = Logger.makeMessage(TAG, "setGraticuleLineStyle", "nullValue.StringIsNull");
            Logger.log(Logger.ERROR, message);
            throw new IllegalArgumentException(message);
        }
        if (graticuleType == null) {
            String message = Logger.makeMessage(TAG, "setGraticuleLineStyle", "nullValue.StringIsNull");
            Logger.log(Logger.ERROR, message);
            throw new IllegalArgumentException(message);
        }

        getRenderingParams(graticuleType).setLineStyle(lineStyle);
    }

    /**
     * Sets the line rendering style for the specified graticules.
     *
     * @param lineStyle     the line rendering style for the specified graticules. One of LINE_STYLE_PLAIN,
     *                      LINE_STYLE_DASHED, or LINE_STYLE_DOTTED.
     * @param graticuleType one of GRATICULE_UTM, GRATICULE_UTM_GRID, GRATICULE_100000M, GRATICULE_10000M,
     *                      GRATICULE_1000M, GRATICULE_100M, GRATICULE_10M, or GRATICULE_1M
     * @throws IllegalArgumentException if <code>lineStyle</code> is null, if <code>graticuleType</code> is null, or if
     *                                  <code>graticuleType</code> is not a valid type.
     */
    public void setGraticuleLineStyle(String lineStyle, Iterable<String> graticuleType) {
        if (lineStyle == null) {
            String message = Logger.makeMessage(TAG, "setGraticuleLineStyle", "nullValue.StringIsNull");
            Logger.log(Logger.ERROR, message);
            throw new IllegalArgumentException(message);
        }
        if (graticuleType == null) {
            String message = Logger.makeMessage(TAG, "setGraticuleLineStyle", "nullValue.IterableIsNull");
            Logger.log(Logger.ERROR, message);
            throw new IllegalArgumentException(message);
        }

        for (String type : graticuleType) {
            setGraticuleLineStyle(lineStyle, type);
        }
    }

    /**
     * Sets the line rendering style for all graticules.
     *
     * @param lineStyle the line rendering style. One of LINE_STYLE_PLAIN, LINE_STYLE_DASHED, or LINE_STYLE_DOTTED.
     * @throws IllegalArgumentException if <code>lineStyle</code> is null.
     */
    public void setGraticuleLineStyle(String lineStyle) {
        if (lineStyle == null) {
            String message = Logger.makeMessage(TAG, "setGraticuleLineStyle", "nullValue.StringIsNull");
            Logger.log(Logger.ERROR, message);
            throw new IllegalArgumentException(message);
        }

        String[] graticuleType = getOrderedTypes();
        for (String type : graticuleType) {
            setGraticuleLineStyle(lineStyle, type);
        }
    }

    /**
     * Returns whether specified graticule labels will be rendered.
     *
     * @param graticuleType one of GRATICULE_UTM, GRATICULE_UTM_GRID, GRATICULE_100000M, GRATICULE_10000M,
     *                      GRATICULE_1000M, GRATICULE_100M, GRATICULE_10M, or GRATICULE_1M.
     * @return true if graticule labels are will be rendered; false otherwise.
     * @throws IllegalArgumentException if <code>graticuleType</code> is null, or if <code>graticuleType</code> is not a
     *                                  valid type.
     */
    public boolean isDrawLabels(String graticuleType) {
        if (graticuleType == null) {
            String message = Logger.makeMessage(TAG, "isDrawLabels", "nullValue.StringIsNull");
            Logger.log(Logger.ERROR, message);
            throw new IllegalArgumentException(message);
        }

        return getRenderingParams(graticuleType).isDrawLabels();
    }

    /**
     * Sets whether the specified graticule labels will be rendered. If true, the graticule labels will be rendered.
     * Otherwise, the graticule labels will not be rendered, but other graticules will not be affected.
     *
     * @param drawLabels    true to render graticule labels; false to disable rendering.
     * @param graticuleType one of GRATICULE_UTM, GRATICULE_UTM_GRID, GRATICULE_100000M, GRATICULE_10000M,
     *                      GRATICULE_1000M, GRATICULE_100M, GRATICULE_10M, or GRATICULE_1M.
     * @throws IllegalArgumentException if <code>graticuleType</code> is null, or if <code>graticuleType</code> is not a
     *                                  valid type.
     */
    public void setDrawLabels(boolean drawLabels, String graticuleType) {
        if (graticuleType == null) {
            String message = Logger.makeMessage(TAG, "setDrawLabels", "nullValue.StringIsNull");
            Logger.log(Logger.ERROR, message);
            throw new IllegalArgumentException(message);
        }

        getRenderingParams(graticuleType).setDrawLabels(drawLabels);
    }

    /**
     * Sets whether the specified graticule labels will be rendered. If true, the graticule labels will be rendered.
     * Otherwise, the graticule labels will not be rendered, but other graticules will not be affected.
     *
     * @param drawLabels    true to render graticule labels; false to disable rendering.
     * @param graticuleType one of GRATICULE_UTM, GRATICULE_UTM_GRID, GRATICULE_100000M, GRATICULE_10000M,
     *                      GRATICULE_1000M, GRATICULE_100M, GRATICULE_10M, or GRATICULE_1M.
     * @throws IllegalArgumentException if <code>graticuleType</code> is null, or if <code>graticuleType</code> is not a
     *                                  valid type.
     */
    public void setDrawLabels(boolean drawLabels, Iterable<String> graticuleType) {
        if (graticuleType == null) {
            String message = Logger.makeMessage(TAG, "setDrawLabels", "nullValue.IterableIsNull");
            Logger.log(Logger.ERROR, message);
            throw new IllegalArgumentException(message);
        }

        for (String type : graticuleType) {
            setDrawLabels(drawLabels, type);
        }
    }

    /**
     * Sets whether all graticule labels will be rendered. If true, all graticule labels will be rendered. Otherwise,
     * all graticule labels will not be rendered.
     *
     * @param drawLabels true to render all graticule labels; false to disable rendering.
     */
    public void setDrawLabels(boolean drawLabels) {
        String[] graticuleType = getOrderedTypes();
        for (String type : graticuleType) {
            setDrawLabels(drawLabels, type);
        }
    }

    /**
     * Returns the label color of the specified graticule.
     *
     * @param graticuleType one of GRATICULE_UTM, GRATICULE_UTM_GRID, GRATICULE_100000M, GRATICULE_10000M,
     *                      GRATICULE_1000M, GRATICULE_100M, GRATICULE_10M, or GRATICULE_1M.
     * @return Color of the the graticule label.
     * @throws IllegalArgumentException if <code>graticuleType</code> is null, or if <code>graticuleType</code> is not a
     *                                  valid type.
     */
    public Color getLabelColor(String graticuleType) {
        if (graticuleType == null) {
            String message = Logger.makeMessage(TAG, "getLabelColor", "nullValue.StringIsNull");
            Logger.log(Logger.ERROR, message);
            throw new IllegalArgumentException(message);
        }

        return getRenderingParams(graticuleType).getLabelColor();
    }

    /**
     * Sets the label rendering color for the specified graticule.
     *
     * @param color         the label color for the specified graticule.
     * @param graticuleType one of GRATICULE_UTM, GRATICULE_UTM_GRID, GRATICULE_100000M, GRATICULE_10000M,
     *                      GRATICULE_1000M, GRATICULE_100M, GRATICULE_10M, or GRATICULE_1M.
     * @throws IllegalArgumentException if<code>color</code> is null, if <code>graticuleType</code> is null, or if
     *                                  <code>graticuleType</code> is not a valid type.
     */
    public void setLabelColor(Color color, String graticuleType) {
        if (color == null) {
            String message = Logger.makeMessage(TAG, "setLabelColor", "nullValue.ColorIsNull");
            Logger.log(Logger.ERROR, message);
            throw new IllegalArgumentException(message);
        }
        if (graticuleType == null) {
            String message = Logger.makeMessage(TAG, "setLabelColor", "nullValue.StringIsNull");
            Logger.log(Logger.ERROR, message);
            throw new IllegalArgumentException(message);
        }

        getRenderingParams(graticuleType).setLabelColor(color);
    }

    /**
     * Sets the label rendering color for the specified graticules.
     *
     * @param color         the label color for the specified graticules.
     * @param graticuleType one of GRATICULE_UTM, GRATICULE_UTM_GRID, GRATICULE_100000M, GRATICULE_10000M,
     *                      GRATICULE_1000M, GRATICULE_100M, GRATICULE_10M, or GRATICULE_1M.
     * @throws IllegalArgumentException if<code>color</code> is null, if <code>graticuleType</code> is null, or if
     *                                  <code>graticuleType</code> is not a valid type.
     */
    public void setLabelColor(Color color, Iterable<String> graticuleType) {
        if (color == null) {
            String message = Logger.makeMessage(TAG, "setLabelColor", "nullValue.ColorIsNull");
            Logger.log(Logger.ERROR, message);
            throw new IllegalArgumentException(message);
        }
        if (graticuleType == null) {
            String message = Logger.makeMessage(TAG, "setLabelColor", "nullValue.IterableIsNull");
            Logger.log(Logger.ERROR, message);
            throw new IllegalArgumentException(message);
        }

        for (String type : graticuleType) {
            setLabelColor(color, type);
        }
    }

    /**
     * Sets the label rendering color for all graticules.
     *
     * @param color the label color.
     * @throws IllegalArgumentException if <code>color</code> is null.
     */
    public void setLabelColor(Color color) {
        if (color == null) {
            String message = Logger.makeMessage(TAG, "setLabelColor", "nullValue.ColorIsNull");
            Logger.log(Logger.ERROR, message);
            throw new IllegalArgumentException(message);
        }

        String[] graticuleType = getOrderedTypes();
        for (String type : graticuleType) {
            setLabelColor(color, type);
        }
    }

    /**
     * Returns the label font of the specified graticule.
     *
     * @param graticuleType one of GRATICULE_UTM, GRATICULE_UTM_GRID, GRATICULE_100000M, GRATICULE_10000M,
     *                      GRATICULE_1000M, GRATICULE_100M, GRATICULE_10M, or GRATICULE_1M.
     *
     * @return Font of the graticule label.
     *
     * @throws IllegalArgumentException if <code>graticuleType</code> is null, or if <code>graticuleType</code> is not a
     *                                  valid type.
     */
/*
    public Font getLabelFont(String graticuleType)
    {
        if (graticuleType == null)
        {
            String message = Logger.makeMessage(TAG, "getLabelFont", "nullValue.StringIsNull");
            Logger.log(Logger.ERROR, message);
            throw new IllegalArgumentException(message);
        }

        return getRenderingParams(graticuleType).getLabelFont();
    }
*/
    /**
     * Sets the label rendering font for the specified graticule.
     *
     * @param font          the label font for the specified graticule.
     * @param graticuleType one of GRATICULE_UTM, GRATICULE_UTM_GRID, GRATICULE_100000M, GRATICULE_10000M,
     *                      GRATICULE_1000M, GRATICULE_100M, GRATICULE_10M, or GRATICULE_1M.
     *
     * @throws IllegalArgumentException if<code>font</code> is null, if <code>graticuleType</code> is null, or if
     *                                  <code>graticuleType</code> is not a valid type.
     */
/*
    public void setLabelFont(Font font, String graticuleType)
    {
        if (font == null)
        {
            String message = Logger.makeMessage(TAG, "setLabelFont", "nullValue.FontIsNull");
            Logger.log(Logger.ERROR, message);
            throw new IllegalArgumentException(message);
        }
        if (graticuleType == null)
        {
            String message = Logger.makeMessage(TAG, "setLabelFont", "nullValue.StringIsNull");
            Logger.log(Logger.ERROR, message);
            throw new IllegalArgumentException(message);
        }

        getRenderingParams(graticuleType).setLabelFont(font);
    }
*/
    /**
     * Sets the label rendering font for the specified graticules.
     *
     * @param font          the label font for the specified graticules.
     * @param graticuleType one of GRATICULE_UTM, GRATICULE_UTM_GRID, GRATICULE_100000M, GRATICULE_10000M,
     *                      GRATICULE_1000M, GRATICULE_100M, GRATICULE_10M, or GRATICULE_1M.
     *
     * @throws IllegalArgumentException if<code>font</code> is null, if <code>graticuleType</code> is null, or if
     *                                  <code>graticuleType</code> is not a valid type.
     */
/*
    public void setLabelFont(Font font, Iterable<String> graticuleType)
    {
        if (font == null)
        {
            String message = Logger.makeMessage(TAG, "setLabelFont", "nullValue.FontIsNull");
            Logger.log(Logger.ERROR, message);
            throw new IllegalArgumentException(message);
        }
        if (graticuleType == null)
        {
            String message = Logger.makeMessage(TAG, "setLabelFont", "nullValue.IterableIsNull");
            Logger.log(Logger.ERROR, message);
            throw new IllegalArgumentException(message);
        }

        for (String type : graticuleType)
        {
            setLabelFont(font, type);
        }
    }
*/

    /**
     * Sets the label rendering font for all graticules.
     * <p>
     * //* @param font the label font.
     *
     * @throws IllegalArgumentException if <code>font</code> is null.
     */
/*
    public void setLabelFont(Font font)
    {
        if (font == null)
        {
            String message = Logger.makeMessage(TAG, "setLabelFont", "nullValue.FontIsNull");
            Logger.log(Logger.ERROR, message);
            throw new IllegalArgumentException(message);
        }

        String[] graticuleType = getOrderedTypes();
        for (String type : graticuleType)
        {
            setLabelFont(font, type);
        }
    }
*/
    protected void initRenderingParams() {
        super.initRenderingParams();
        GraticuleRenderingParams params;
        // UTM graticule
        params = new GraticuleRenderingParams();
        params.setLineColor(new Color(1.0f, 1.0f, 0, 0.8f));
        params.setLabelColor(new Color(1.0f, 1.0f, 0, 0.8f));
        params.setLabelTypeface(Typeface.create("Arial", Typeface.BOLD));
        params.setLablePointSize(14.0f);
        setRenderingParams(GRATICULE_UTM_GRID, params);

        // 100,000 meter graticule
        params = new GraticuleRenderingParams();
        params.setLineColor(new Color(0.0f, 1.0f, 0, 0.8f));
        params.setLabelColor(new Color(0.0f, 1.0f, 0, 0.8f));
        params.setLabelTypeface(Typeface.create("Arial", Typeface.BOLD));
        params.setLablePointSize(12.0f);
        setRenderingParams(GRATICULE_100000M, params);

        // 10,000 meter graticule
        params = new GraticuleRenderingParams();
        params.setLineColor(new Color(0.0f, 102 / 255, 1.0f, 0.8f));
        params.setLabelColor(new Color(0f, 102 / 255, 1.0f, 0.8f));
        setRenderingParams(GRATICULE_10000M, params);

        // 1,000 meter graticule
        params = new GraticuleRenderingParams();
        params.setLineColor(new Color(0.0f, 1.0f, 1.0f, 0.8f));
        params.setLabelColor(new Color(0.0f, 1.0f, 1.0f, 0.8f));
        setRenderingParams(GRATICULE_1000M, params);

        // 100 meter graticule
        params = new GraticuleRenderingParams();
        params.setLineColor(new Color(0.0f, 153 / 255, 153 / 255, 0.0f));
        params.setLabelColor(new Color(0.0f, 153 / 255, 153 / 255, 0.8f));
        setRenderingParams(GRATICULE_100M, params);

        // 10 meter graticule
        params = new GraticuleRenderingParams();
        params.setLineColor(new Color(102 / 255, 1.0f, 204 / 255, 0.8f));
        params.setLabelColor(new Color(102 / 255, 1.0f, 204 / 255, 0.8f));
        setRenderingParams(GRATICULE_10M, params);

        // 1 meter graticule
        params = new GraticuleRenderingParams();
        params.setLineColor(new Color(153 / 255, 153 / 255, 1.0f, 0.8f));
        params.setLabelColor(new Color(153 / 255, 153 / 255, 1.0f, 0.8f));
        setRenderingParams(GRATICULE_1M, params);
    }

    protected String[] getOrderedTypes() {
        return new String[]{GRATICULE_UTM_GRID, GRATICULE_100000M, GRATICULE_10000M, GRATICULE_1000M, GRATICULE_100M, GRATICULE_10M, GRATICULE_1M,};
    }

    protected String getTypeFor(int resolution) {
        String graticuleType = null;
        switch (resolution) {
            case 100000: // 100,000 meters
                graticuleType = GRATICULE_100000M;
                break;
            case 10000:  // 10,000 meters
                graticuleType = GRATICULE_10000M;
                break;
            case 1000:   // 1000 meters
                graticuleType = GRATICULE_1000M;
                break;
            case 100:    // 100 meters
                graticuleType = GRATICULE_100M;
                break;
            case 10:     // 10 meters
                graticuleType = GRATICULE_10M;
                break;
            case 1:      // 1 meter
                graticuleType = GRATICULE_1M;
                break;
        }

        return graticuleType;
    }

    // --- Renderable layer --------------------------------------------------------------

    protected void clear(RenderContext dc) {
        super.clear(dc);

        this.frameCount++;
        this.applyTerrainConformance();

        this.metricScaleSupport.clear();
        this.metricScaleSupport.computeZone(dc);
    }

    private void applyTerrainConformance() {
        String[] graticuleType = getOrderedTypes();
        for (String type : graticuleType) {
            getRenderingParams(type).setValue(GraticuleRenderingParams.KEY_LINE_CONFORMANCE, this.terrainConformance);
        }
    }

    protected Sector computeVisibleSector(RenderContext dc) {
        return this.mapSector; //dc.terrain.getMapSector(); //.getVisibleSector();
    }

    protected void selectRenderables(RenderContext dc) {
        if (dc.camera.altitude <= this.zoneMaxAltitude) {
            this.selectMGRSRenderables(dc, this.computeVisibleSector(dc));
            this.metricScaleSupport.selectRenderables(dc);
        } else {
            super.selectRenderables(dc);
        }
    }

    protected void selectMGRSRenderables(RenderContext dc, Sector vs) {
        ArrayList<GridZone> zoneList = getVisibleZones(dc);
        if (zoneList.size() > 0) {
            for (GridZone gz : zoneList) {
                // Select visible grid zones elements
                gz.selectRenderables(dc, vs, this);
            }
        }
    }

    private ArrayList<GridZone> getVisibleZones(RenderContext dc) {
        ArrayList<GridZone> zoneList = new ArrayList<GridZone>();
        Sector vs = this.mapSector;//dc.terrain.getMapSector(); //.getVisibleSector();
        if (vs != null) {
            // UTM Grid
            Rect gridRectangle = getGridRectangleForSector(vs);
            if (gridRectangle != null) {
                for (int row = gridRectangle.top; row <= gridRectangle.bottom; row++) {
                    for (int col = gridRectangle.left; col <= gridRectangle.right; col++) {
                        if (row != 19 || (col != 31 && col != 33 && col != 35)) { // ignore X32, 34 and 36
                            if (gridZones[row][col] == null) {
                                gridZones[row][col] = new GridZone(getGridSector(row, col));
                            }
                            if (gridZones[row][col].isInView(dc)) {
                                zoneList.add(gridZones[row][col]);
                            } else {
                                gridZones[row][col].clearRenderables();
                            }
                        }
                    }
                }
            }
            // Poles
            if (vs.maxLatitude() > 84) {
                // North pole
                if (poleZones[2] == null) {
                    poleZones[2] = new GridZone(Sector.fromDegrees(84, -180, 6, 180)); // Y
                }
                if (poleZones[3] == null) {
                    poleZones[3] = new GridZone(Sector.fromDegrees(84, 0, 6, 180));  // Z
                }
                zoneList.add(poleZones[2]);
                zoneList.add(poleZones[3]);
            }
            if (vs.minLatitude() < -80) {
                // South pole
                if (poleZones[0] == null) {
                    poleZones[0] = new GridZone(Sector.fromDegrees(-90, -180, 10, 180)); // B
                }
                if (poleZones[1] == null) {
                    poleZones[1] = new GridZone(Sector.fromDegrees(-90, 0, 10, 180));  // A
                }
                zoneList.add(poleZones[0]);
                zoneList.add(poleZones[1]);
            }
        }
        return zoneList;
    }

    private Rect getGridRectangleForSector(Sector sector) {
        Rect rectangle = null;

        if (sector.minLatitude() < 84 && sector.maxLatitude() > -80) {
            //Sector gridSector = Sector.fromDegrees(
            //        Math.max(sector.getMinLatitude().degrees, -80), Math.min(sector.getMaxLatitude().degrees, 84),
            //        sector.getMinLongitude().degrees, sector.getMaxLongitude().degrees);
            Sector gridSector = new Sector();

            gridSector.union(Math.max(sector.minLatitude(), -80), sector.minLongitude());
            gridSector.union(Math.min(sector.maxLatitude(), 84), sector.maxLongitude());

            int x1 = getGridColumn(gridSector.minLongitude());
            int x2 = getGridColumn(gridSector.maxLongitude());
            int y1 = getGridRow(gridSector.minLatitude());
            int y2 = getGridRow(gridSector.maxLatitude());
            // Adjust rectangle to include special zones
            if (y1 <= 17 && y2 >= 17 && x2 == 30) { // 32V Norway
                x2 = 31;
            }
            if (y1 <= 19 && y2 >= 19) { // X band
                if (x1 == 31) { // 31X
                    x1 = 30;
                }
                if (x2 == 31) { // 33X
                    x2 = 32;
                }
                if (x1 == 33) { // 33X
                    x1 = 32;
                }
                if (x2 == 33) { // 35X
                    x2 = 34;
                }
                if (x1 == 35) { // 35X
                    x1 = 34;
                }
                if (x2 == 35) { // 37X
                    x2 = 36;
                }
            }
            //rectangle = new Rect(x1, y1, x2 - x1, y2 - y1);
            rectangle = new Rect(x1, y1, x2, y2);
        }
        return rectangle;
    }

    private int getGridColumn(Double longitude) {
        int col = (int) Math.floor((longitude + 180) / 6d);
        return Math.min(col, 59);
    }

    private int getGridRow(Double latitude) {
        int row = (int) Math.floor((latitude + 80) / 8d);
        return Math.min(row, 19);
    }

    private Sector getGridSector(int row, int col) {
        int minLat = -80 + row * 8;
        int maxLat = minLat + (minLat != 72 ? 8 : 12);
        int minLon = -180 + col * 6;
        int maxLon = minLon + 6;
        // Special sectors
        if (row == 17 && col == 30)         // 31V
        {
            maxLon -= 3;
        } else if (row == 17 && col == 31)    // 32V
        {
            minLon -= 3;
        } else if (row == 19 && col == 30)   // 31X
        {
            maxLon += 3;
        } else if (row == 19 && col == 31)   // 32X does not exist
        {
            minLon += 3;
            maxLon -= 3;
        } else if (row == 19 && col == 32)   // 33X
        {
            minLon -= 3;
            maxLon += 3;
        } else if (row == 19 && col == 33)   // 34X does not exist
        {
            minLon += 3;
            maxLon -= 3;
        } else if (row == 19 && col == 34)   // 35X
        {
            minLon -= 3;
            maxLon += 3;
        } else if (row == 19 && col == 35)   // 36X does not exist
        {
            minLon += 3;
            maxLon -= 3;
        } else if (row == 19 && col == 36)   // 37X
        {
            minLon -= 3;
        }

        Sector sector = new Sector();
        sector.union(minLat, minLon);
        sector.union(maxLat, maxLon);
        //return Sector.fromDegrees(minLat, minLon, maxLat - minLat, maxLon - minLon);
        return sector;
    }

    private boolean isNorthNeighborInView(GridZone gz, RenderContext dc) {
        if (gz.isUPS) {
            return true;
        }

        int row = getGridRow(gz.sector.centroidLatitude());
        int col = getGridColumn(gz.sector.centroidLongitude());
        GridZone neighbor = row + 1 <= 19 ? this.gridZones[row + 1][col] : null;
        return neighbor != null && neighbor.isInView(dc);
    }

    private boolean isEastNeighborInView(GridZone gz, RenderContext dc) {
        if (gz.isUPS) {
            return true;
        }

        int row = getGridRow(gz.sector.centroidLatitude());
        int col = getGridColumn(gz.sector.centroidLongitude());
        GridZone neighbor = col + 1 <= 59 ? this.gridZones[row][col + 1] : null;
        return neighbor != null && neighbor.isInView(dc);
    }

    //--- Grid zone ----------------------------------------------------------------------

    /**
     * Represent a UTM zone / latitude band intersection
     */
    private class GridZone {
        private static final double ONEHT = 100e3;
        private static final double TWOMIL = 2e6;

        private Sector sector;
        private boolean isUPS = false;
        private String name = "";
        private int UTMZone = 0;
        private String hemisphere = null;

        private ArrayList<GridElement> gridElements;
        private ArrayList<SquareZone> squares;

        public GridZone(Sector sector) {
            this.sector = sector;
            this.isUPS = (sector.maxLatitude() > UTM_MAX_LATITUDE || sector.minLatitude() < UTM_MIN_LATITUDE);
            try {
                MGRSCoord MGRS = MGRSCoord.fromLatLon(sector.centroidLatitude(), sector.centroidLongitude(), globe);
                if (this.isUPS) {
                    this.name = MGRS.toString().substring(2, 3);
                    this.hemisphere = sector.minLatitude() > 0 ? AVKey.NORTH : AVKey.SOUTH;
                } else {
                    this.name = MGRS.toString().substring(0, 3);
                    UTMCoord UTM = UTMCoord.fromLatLon(sector.centroidLatitude(), sector.centroidLongitude(), globe);
                    this.UTMZone = UTM.getZone();
                    this.hemisphere = UTM.getHemisphere();
                }
            } catch (IllegalArgumentException ignore) {
            }
        }

        /*
                public Extent getExtent(Globe globe, double ve)
                {
                    return Sector.computeBoundingCylinder(globe, ve, this.sector);
                }
        */
        public boolean isInView(RenderContext dc) {
            return this.sector.intersects(MGRSGraticuleLayer.this.mapSector); //dc.frustum.intersectsViewport(dc.viewport);
        }

        public void selectRenderables(RenderContext dc, Sector vs, MGRSGraticuleLayer layer) {
            // Select zone elements
            if (this.gridElements == null) {
                createRenderables();
            }

            for (GridElement ge : this.gridElements) {
                if (ge.isInView(dc, vs)) {
                    if (ge.type.equals(GridElement.TYPE_LINE_NORTH) && isNorthNeighborInView(this, dc)) {
                        continue;
                    }
                    if (ge.type.equals(GridElement.TYPE_LINE_EAST) && isEastNeighborInView(this, dc)) {
                        continue;
                    }

                    layer.addRenderable(ge.renderable, GRATICULE_UTM_GRID);
                }
            }

            if (dc.camera.altitude > MGRSGraticuleLayer.this.squareMaxAltitude) {
                if (null != this.squares) {
                    for (SquareZone sz : this.squares) {
                        sz.clearRenderables();
                    }
                }
                return;
            }

            // Select 100km squares elements
            if (this.squares == null) {
                createSquares();
            }
            for (SquareZone sz : this.squares) {
                if (sz.isInView(dc)) {
                    sz.selectRenderables(dc, vs);
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
            if (this.isUPS) {
                createSquaresUPS();
            } else {
                createSquaresUTM();
            }
        }

        private void createSquaresUTM() {
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

                // Compensate for some distorted zones
                if (this.name.equals("32V")) { // catch KS and LS in 32V
                    maxNorthing += 20e3;
                }
                if (this.name.equals("31X")) { // catch GA and GV in 31X
                    maxEasting += ONEHT;
                }

                // Create squares
                this.squares = createSquaresGrid(this.UTMZone, this.hemisphere, this.sector, minEasting, maxEasting, minNorthing, maxNorthing);
                this.setSquareNames();
            } catch (IllegalArgumentException ignore) {
            }
        }

        private void createSquaresUPS() {
            this.squares = new ArrayList<SquareZone>();
            double minEasting, maxEasting, minNorthing, maxNorthing;

            if (AVKey.NORTH.equals(this.hemisphere)) {
                minNorthing = TWOMIL - ONEHT * 7;
                maxNorthing = TWOMIL + ONEHT * 7;
                minEasting = this.name.equals("Y") ? TWOMIL - ONEHT * 7 : TWOMIL;
                maxEasting = this.name.equals("Y") ? TWOMIL : TWOMIL + ONEHT * 7;
            } else // AVKey.SOUTH.equals(this.hemisphere)
            {
                minNorthing = TWOMIL - ONEHT * 12;
                maxNorthing = TWOMIL + ONEHT * 12;
                minEasting = this.name.equals("A") ? TWOMIL - ONEHT * 12 : TWOMIL;
                maxEasting = this.name.equals("A") ? TWOMIL : TWOMIL + ONEHT * 12;
            }

            // Create squares
            this.squares = createSquaresGrid(this.UTMZone, this.hemisphere, this.sector, minEasting, maxEasting, minNorthing, maxNorthing);
            this.setSquareNames();
        }

        private void setSquareNames() {
            for (SquareZone sz : this.squares) {
                this.setSquareName(sz);
            }
        }

        private void setSquareName(SquareZone sz) {
            // Find out MGRS 100Km square name
            double tenMeterDegrees = Math.toDegrees(10d / 6378137d);
            try {
                MGRSCoord MGRS = null;
                if (sz.centroid != null && sz.isPositionInside(new Position(sz.centroid.latitude, sz.centroid.longitude, 0))) {
                    MGRS = MGRSCoord.fromLatLon(sz.centroid.latitude, sz.centroid.longitude, globe);
                } else if (sz.isPositionInside(sz.sw)) {
                    MGRS = MGRSCoord.fromLatLon(sz.sw.latitude + tenMeterDegrees, sz.sw.longitude + tenMeterDegrees, globe);
                } else if (sz.isPositionInside(sz.se)) {
                    MGRS = MGRSCoord.fromLatLon(sz.se.latitude + tenMeterDegrees, sz.se.longitude - tenMeterDegrees, globe);
                } else if (sz.isPositionInside(sz.nw)) {
                    MGRS = MGRSCoord.fromLatLon(sz.nw.latitude - tenMeterDegrees, sz.nw.longitude + tenMeterDegrees, globe);
                } else if (sz.isPositionInside(sz.ne)) {
                    MGRS = MGRSCoord.fromLatLon(sz.ne.latitude - tenMeterDegrees, sz.ne.longitude - tenMeterDegrees, globe);
                }
                // Set square zone name
                if (MGRS != null) {
                    sz.setName(MGRS.toString().substring(3, 5));
                }
            } catch (IllegalArgumentException ignore) {
            }
        }

        private void createRenderables() {
            this.gridElements = new ArrayList<GridElement>();

            ArrayList<Position> positions = new ArrayList<Position>();

            // left meridian segment
            positions.clear();
            positions.add(new Position(this.sector.minLatitude(), this.sector.minLongitude(), 10e3));
            positions.add(new Position(this.sector.maxLatitude(), this.sector.minLongitude(), 10e3));
            Object polyline = createLineRenderable(new ArrayList<Position>(positions), WorldWind.GREAT_CIRCLE);
            //Sector lineSector = new Sector(this.sector.minLatitude(), this.sector.minLongitude(), this.sector.deltaLatitude(), 0.0000001);
            Sector lineSector = new Sector();
            lineSector.union(this.sector.minLatitude(), this.sector.minLongitude());
            lineSector.union(this.sector.maxLatitude(), this.sector.minLongitude());
            this.gridElements.add(new GridElement(lineSector, polyline, GridElement.TYPE_LINE_WEST));

            if (!this.isUPS) {
                // right meridian segment
                positions.clear();
                positions.add(new Position(this.sector.minLatitude(), this.sector.maxLongitude(), 10e3));
                positions.add(new Position(this.sector.maxLatitude(), this.sector.maxLongitude(), 10e3));
                polyline = createLineRenderable(new ArrayList<Position>(positions), WorldWind.GREAT_CIRCLE);
                //lineSector = new Sector(this.sector.minLatitude(), this.sector.maxLongitude(), this.sector.deltaLatitude(), 0.0000001);
                lineSector = new Sector();
                lineSector.union(this.sector.minLatitude(), this.sector.maxLongitude());
                lineSector.union(this.sector.maxLatitude(), this.sector.maxLongitude());
                this.gridElements.add(new GridElement(lineSector, polyline, GridElement.TYPE_LINE_EAST));

                // bottom parallel segment
                positions.clear();
                positions.add(new Position(this.sector.minLatitude(), this.sector.minLongitude(), 10e3));
                positions.add(new Position(this.sector.minLatitude(), this.sector.maxLongitude(), 10e3));
                polyline = createLineRenderable(new ArrayList<Position>(positions), WorldWind.GREAT_CIRCLE);
                //lineSector = new Sector(this.sector.minLatitude(), this.sector.minLongitude(), 0.0000001, this.sector.deltaLongitude());
                lineSector = new Sector();
                lineSector.union(this.sector.minLatitude(), this.sector.minLongitude());
                lineSector.union(this.sector.minLatitude(), this.sector.maxLongitude());
                this.gridElements.add(new GridElement(lineSector, polyline, GridElement.TYPE_LINE_SOUTH));

                // top parallel segment
                positions.clear();
                positions.add(new Position(this.sector.maxLatitude(), this.sector.minLongitude(), 10e3));
                positions.add(new Position(this.sector.maxLatitude(), this.sector.maxLongitude(), 10e3));
                polyline = createLineRenderable(new ArrayList<Position>(positions), WorldWind.GREAT_CIRCLE);
                //lineSector = new Sector(this.sector.maxLatitude(), this.sector.minLongitude(), 0.0000001, this.sector.deltaLongitude());
                lineSector = new Sector();
                lineSector.union(this.sector.maxLatitude(), this.sector.minLongitude());
                lineSector.union(this.sector.maxLatitude(), this.sector.maxLongitude());
                this.gridElements.add(new GridElement(lineSector, polyline, GridElement.TYPE_LINE_NORTH));
            }

            // Label
            Label text = new Label(new Position(this.sector.centroidLatitude(), this.sector.centroidLongitude(), 0), this.name);
            //text.setPriority(10e6);
            this.gridElements.add(new GridElement(this.sector, text, GridElement.TYPE_GRIDZONE_LABEL));
        }
    }
}
