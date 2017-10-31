package mil.emp3.api.utils;

import android.graphics.Bitmap;

import org.cmapi.primitives.GeoIconStyle;
import org.cmapi.primitives.GeoPoint;
import org.cmapi.primitives.GeoPolygon;
import org.cmapi.primitives.GeoPosition;
import org.cmapi.primitives.GeoRectangle;
import org.cmapi.primitives.GeoSquare;
import org.cmapi.primitives.GeoText;
import org.cmapi.primitives.IGeoAltitudeMode;
import org.cmapi.primitives.IGeoColor;
import org.cmapi.primitives.IGeoFillStyle;
import org.cmapi.primitives.IGeoIconStyle;
import org.cmapi.primitives.IGeoLabelStyle;
import org.cmapi.primitives.IGeoPoint;
import org.cmapi.primitives.IGeoPolygon;
import org.cmapi.primitives.IGeoPosition;
import org.cmapi.primitives.IGeoRectangle;
import org.cmapi.primitives.IGeoSquare;
import org.cmapi.primitives.IGeoStrokeStyle;
import org.cmapi.primitives.IGeoText;

import java.net.URL;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import mil.emp3.api.Point;
import mil.emp3.api.Polygon;
import mil.emp3.api.Rectangle;
import mil.emp3.api.Square;
import mil.emp3.api.Text;
import mil.emp3.api.WMTS;
import mil.emp3.api.enums.FeatureTypeEnum;
import mil.emp3.api.enums.WMTSVersionEnum;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;


/**
 * Created by matt.miller@rgi-corp.local on 10/27/17.
 */

public class ComparisonUtils {
    public static final double Epsilon = 1e-8;

    private static void compareGeoPosition(final IGeoPosition p1, final IGeoPosition p2) {
        if(p1 == null && p2 == null) {
            return;
        }
        assertEquals(p1.getLatitude(), p2.getLatitude(), Epsilon);
        assertEquals(p1.getLongitude(), p2.getLongitude(), Epsilon);
        assertEquals(p1.getAltitude(), p2.getAltitude(), Epsilon);
    }

    private static void compareGeoPositionArray(final List<IGeoPosition> p1, final List<IGeoPosition> p2) {
        final double size = p1.size();
        if(size != p2.size()){
            fail("Two Geoposition Lists are of unequal length");
        }
        for(int i = 0; i < size; i++) {
            compareGeoPosition(p1.get(i), p2.get(i));
        }
    }

    private static void compareGeoColor(final IGeoColor c1, final IGeoColor c2) {
        assertEquals(c1.getAlpha(), c2.getAlpha(), Epsilon);
        assertEquals(c1.getRed(), c2.getRed());
        assertEquals(c1.getBlue(), c2.getBlue());
        assertEquals(c1.getGreen(), c2.getGreen());
    }

    private static void compareFillStyle(final IGeoFillStyle f1, final IGeoFillStyle f2) {
        if(f1 == null && f2 == null) {
            return;
        }
        assertEquals(f1.getDescription(), f2.getDescription());
        compareGeoColor(f1.getFillColor(), f2.getFillColor());
        assertEquals(f1.getFillPattern(), f2.getFillPattern());
    }

    private static void compareStrokeStyle(final IGeoStrokeStyle s1, final IGeoStrokeStyle s2) {
        assertEquals(s1.getStipplingFactor(), s2.getStipplingFactor());
        assertEquals(s1.getStipplingPattern(), s2.getStipplingPattern());
        compareGeoColor(s1.getStrokeColor(), s2.getStrokeColor());
        assertEquals(s1.getStrokeWidth(), s2.getStrokeWidth(), Epsilon);
    }

    private static void compareIconStyle(final IGeoIconStyle i1, final IGeoIconStyle i2) {
        assertEquals(i1.getOffSetX(), i2.getOffSetX(), Epsilon);
        assertEquals(i1.getOffSetY(), i2.getOffSetY(), Epsilon);
        assertEquals(i1.getSize(), i2.getSize(), Epsilon);
    }

    private static void compareLabelStyle(final IGeoLabelStyle l1, final IGeoLabelStyle l2) {
        compareGeoColor(l1.getColor(), l2.getColor());
        assertEquals(l1.getFontFamily(), l2.getFontFamily());
        assertEquals(l1.getJustification(), l2.getJustification());
        compareGeoColor(l1.getOutlineColor(), l2.getOutlineColor());
        assertEquals(l1.getSize(), l2.getSize(), Epsilon);
        assertEquals(l1.getTypeface(), l2.getTypeface());
    }

    private static void compareGeoPoint(final IGeoPoint p1, final IGeoPoint p2) {
        assertEquals(p1.getTimeStamp(), p2.getTimeStamp());
        assertEquals(p1.getTimeSpans(), p2.getTimeSpans());
        assertEquals(p1.getTessellate(), p2.getTessellate());
        compareStrokeStyle(p1.getStrokeStyle(), p2.getStrokeStyle());
        assertEquals(p1.getReadOnly(), p2.getReadOnly());
        assertEquals(p1.getProperties(), p2.getProperties());
        compareGeoPositionArray(p1.getPositions(), p2.getPositions());
        assertEquals(p1.getPathType(), p2.getPathType());
        assertEquals(p1.getName(), p2.getName());
        compareLabelStyle(p1.getLabelStyle(), p2.getLabelStyle());
        compareFillStyle(p1.getFillStyle(), p2.getFillStyle());
        assertEquals(p1.getExtrude(), p2.getExtrude());
        assertEquals(p1.getDescription(), p2.getDescription());
        assertEquals(p1.getDataProviderId(), p2.getDataProviderId());
        assertEquals(p1.getChildren(), p2.getChildren());
        assertEquals(p1.getBuffer(), p2.getBuffer(), Epsilon);
        assertEquals(p1.getAzimuth(), p2.getAzimuth(), Epsilon);
        assertEquals(p1.getAltitudeMode(), p2.getAltitudeMode());
        assertEquals(p1.getIconURI(), p2.getIconURI());
        compareIconStyle(p1.getIconStyle(), p2.getIconStyle());
    }

    private static void compareGeoPolygon(final IGeoPolygon p1, final IGeoPolygon p2) {
        assertEquals(p1.getTimeStamp(), p2.getTimeStamp());
        assertEquals(p1.getTimeSpans(), p2.getTimeSpans());
        assertEquals(p1.getTessellate(), p2.getTessellate());
        compareStrokeStyle(p1.getStrokeStyle(), p2.getStrokeStyle());
        assertEquals(p1.getReadOnly(), p2.getReadOnly());
        assertEquals(p1.getProperties(), p2.getProperties());
        compareGeoPositionArray(p1.getPositions(), p2.getPositions());
        assertEquals(p1.getPathType(), p2.getPathType());
        assertEquals(p1.getName(), p2.getName());
        compareLabelStyle(p1.getLabelStyle(), p2.getLabelStyle());
        compareFillStyle(p1.getFillStyle(), p2.getFillStyle());
        assertEquals(p1.getExtrude(), p2.getExtrude());
        assertEquals(p1.getDescription(), p2.getDescription());
        assertEquals(p1.getDataProviderId(), p2.getDataProviderId());
        assertEquals(p1.getChildren(), p2.getChildren());
        assertEquals(p1.getBuffer(), p2.getBuffer(), Epsilon);
        assertEquals(p1.getAzimuth(), p2.getAzimuth(), Epsilon);
        assertEquals(p1.getAltitudeMode(), p2.getAltitudeMode());
    }

    private static void compareGeoRectangle(final IGeoRectangle r1, final IGeoRectangle r2) {
        assertEquals(r1.getHeight(), r2.getHeight(), Epsilon);
        assertEquals(r1.getWidth(), r2.getWidth(), Epsilon);
        assertEquals(r1.getTimeStamp(), r2.getTimeStamp());
        assertEquals(r1.getTimeSpans(), r2.getTimeSpans());
        assertEquals(r1.getTessellate(), r2.getTessellate());
        compareStrokeStyle(r1.getStrokeStyle(), r2.getStrokeStyle());
        assertEquals(r1.getReadOnly(), r2.getReadOnly());
        assertEquals(r1.getProperties(), r2.getProperties());
        compareGeoPositionArray(r1.getPositions(), r2.getPositions());
        assertEquals(r1.getPathType(), r2.getPathType());
        assertEquals(r1.getName(), r2.getName());
        compareLabelStyle(r1.getLabelStyle(), r2.getLabelStyle());
        compareFillStyle(r1.getFillStyle(), r2.getFillStyle());
        assertEquals(r1.getExtrude(), r2.getExtrude());
        assertEquals(r1.getDescription(), r2.getDescription());
        assertEquals(r1.getDataProviderId(), r2.getDataProviderId());
        assertEquals(r1.getChildren(), r2.getChildren());
        assertEquals(r1.getBuffer(), r2.getBuffer(), Epsilon);
        assertEquals(r1.getAzimuth(), r2.getAzimuth(), Epsilon);
        assertEquals(r1.getAltitudeMode(), r2.getAltitudeMode());
    }

    private static void compareGeoSquare(final IGeoSquare s1, final IGeoSquare s2) {
        assertEquals(s1.getWidth(), s2.getWidth(), Epsilon);
        assertEquals(s1.getTimeStamp(), s2.getTimeStamp());
        assertEquals(s1.getTimeSpans(), s2.getTimeSpans());
        assertEquals(s1.getTessellate(), s2.getTessellate());
        compareStrokeStyle(s1.getStrokeStyle(), s2.getStrokeStyle());
        assertEquals(s1.getReadOnly(), s2.getReadOnly());
        assertEquals(s1.getProperties(), s2.getProperties());
        compareGeoPositionArray(s1.getPositions(), s2.getPositions());
        assertEquals(s1.getPathType(), s2.getPathType());
        assertEquals(s1.getName(), s2.getName());
        compareLabelStyle(s1.getLabelStyle(), s2.getLabelStyle());
        compareFillStyle(s1.getFillStyle(), s2.getFillStyle());
        assertEquals(s1.getExtrude(), s2.getExtrude());
        assertEquals(s1.getDescription(), s2.getDescription());
        assertEquals(s1.getDataProviderId(), s2.getDataProviderId());
        assertEquals(s1.getChildren(), s2.getChildren());
        assertEquals(s1.getBuffer(), s2.getBuffer(), Epsilon);
        assertEquals(s1.getAzimuth(), s2.getAzimuth(), Epsilon);
        assertEquals(s1.getAltitudeMode(), s2.getAltitudeMode());
    }

    private static void compareGeoText(final IGeoText t1, final IGeoText t2) {
        assertEquals(t1.getTimeStamp(), t2.getTimeStamp());
        assertEquals(t1.getTimeSpans(), t2.getTimeSpans());
        assertEquals(t1.getTessellate(), t2.getTessellate());
        compareStrokeStyle(t1.getStrokeStyle(), t2.getStrokeStyle());
        assertEquals(t1.getReadOnly(), t2.getReadOnly());
        assertEquals(t1.getProperties(), t2.getProperties());
        compareGeoPositionArray(t1.getPositions(), t2.getPositions());
        assertEquals(t1.getPathType(), t2.getPathType());
        assertEquals(t1.getName(), t2.getName());
        compareLabelStyle(t1.getLabelStyle(), t2.getLabelStyle());
        compareFillStyle(t1.getFillStyle(), t2.getFillStyle());
        assertEquals(t1.getExtrude(), t2.getExtrude());
        assertEquals(t1.getDescription(), t2.getDescription());
        assertEquals(t1.getDataProviderId(), t2.getDataProviderId());
        assertEquals(t1.getChildren(), t2.getChildren());
        assertEquals(t1.getBuffer(), t2.getBuffer(), Epsilon);
        assertEquals(t1.getAzimuth(), t2.getAzimuth(), Epsilon);
        assertEquals(t1.getAltitudeMode(), t2.getAltitudeMode());
    }

    public static void validatePoint(final Point point,
                                     final double iconScale,
                                     final int resourceId,
                                     final GeoIconStyle iconStyle,
                                     final String iconURI,
                                     final GeoPoint geoPoint,
                                     final FeatureTypeEnum fte,
                                     final List childFeatures,
                                     final List parentOverlays,
                                     final List parentFeatures,
                                     final List<IGeoPosition> positions,
                                     final Date date,
                                     final List timeSpans,
                                     final IGeoAltitudeMode.AltitudeMode altitudeMode,
                                     final IGeoStrokeStyle strokeStyle,
                                     final IGeoFillStyle fillStyle,
                                     final IGeoLabelStyle labelStyle,
                                     final Boolean extrude,
                                     final Boolean tessellate,
                                     final double buffer,
                                     final double azimuth,
                                     final GeoPosition geoPosition,
                                     final Boolean readOnly,
                                     final List parents,
                                     final Boolean hasChildren,
                                     final List children,
                                     final String name,
                                     final String dataProvider,
                                     final String description,
                                     final HashMap properties) {
        assertEquals(point.getIconScale(), iconScale, Epsilon);
        assertEquals(point.getResourceId(), resourceId, Epsilon);
        compareIconStyle(point.getIconStyle(), iconStyle);
        assertEquals(point.getIconURI(), iconURI);
        compareGeoPoint(point.getRenderable(), geoPoint);
        assertEquals(point.getFeatureType(), fte);
        assertEquals(point.getChildFeatures(), childFeatures);
        assertEquals(point.getParentOverlays(), parentOverlays);
        assertEquals(point.getParentFeatures(), parentFeatures);
        compareGeoPositionArray(point.getPositions(), positions);
        assertEquals(point.getTimeStamp(), date);
        assertEquals(point.getTimeSpans(), timeSpans);
        assertEquals(point.getAltitudeMode(), altitudeMode);
        compareStrokeStyle(point.getStrokeStyle(), strokeStyle);
        compareFillStyle(point.getFillStyle(), fillStyle);
        compareLabelStyle(point.getLabelStyle(), labelStyle);
        assertEquals(point.getExtrude(), extrude);
        assertEquals(point.getTessellate(), tessellate);
        assertEquals(point.getBuffer(), buffer, Epsilon);
        assertEquals(point.getAzimuth(), azimuth, Epsilon);
        compareGeoPosition(point.getPosition(), geoPosition);
        assertEquals(point.getReadOnly(), readOnly);
        assertEquals(point.getParents(), parents);
        assertEquals(point.hasChildren(), hasChildren);
        assertEquals(point.getChildren(), children);
        assertEquals(point.getName(), name);
        assertEquals(point.getDataProviderId(), dataProvider);
        assertEquals(point.getDescription(), description);
        assertEquals(point.getProperties(), properties);
    }

    public static void validatePolygon(final Polygon poly,
                                       final Bitmap bmp,
                                       final GeoPolygon geoPolygon,
                                       final FeatureTypeEnum fte,
                                       final List childFeatures,
                                       final List parentOverlays,
                                       final List parentFeatures,
                                       final List<IGeoPosition> positions,
                                       final Date date,
                                       final List timeSpans,
                                       final IGeoAltitudeMode.AltitudeMode altitudeMode,
                                       final IGeoStrokeStyle strokeStyle,
                                       final IGeoFillStyle fillStyle,
                                       final IGeoLabelStyle labelStyle,
                                       final Boolean extrude,
                                       final Boolean tessellate,
                                       final double buffer,
                                       final double azimuth,
                                       final GeoPosition geoPosition,
                                       final Boolean readOnly,
                                       final List parents,
                                       final Boolean hasChildren,
                                       final List children,
                                       final String name,
                                       final String dataProvider,
                                       final String description,
                                       final HashMap properties) {
        compareGeoPolygon(poly.getRenderable(), geoPolygon);
        assertEquals(poly.getFeatureType(), fte);
        assertEquals(poly.getChildFeatures(), childFeatures);
        assertEquals(poly.getParentOverlays(), parentOverlays);
        assertEquals(poly.getParentFeatures(), parentFeatures);
        compareGeoPositionArray(poly.getPositions(), positions);
        assertEquals(poly.getTimeStamp(), date);
        assertEquals(poly.getTimeSpans(), timeSpans);
        assertEquals(poly.getAltitudeMode(), altitudeMode);
        compareStrokeStyle(poly.getStrokeStyle(), strokeStyle);
        compareFillStyle(poly.getFillStyle(), fillStyle);
        compareLabelStyle(poly.getLabelStyle(), labelStyle);
        assertEquals(poly.getExtrude(), extrude);
        assertEquals(poly.getTessellate(), tessellate);
        assertEquals(poly.getBuffer(), buffer, Epsilon);
        assertEquals(poly.getAzimuth(), azimuth, Epsilon);
        compareGeoPosition(poly.getPosition(), geoPosition);
        assertEquals(poly.getReadOnly(), readOnly);
        assertEquals(poly.getParents(), parents);
        assertEquals(poly.hasChildren(), hasChildren);
        assertEquals(poly.getChildren(), children);
        assertEquals(poly.getName(), name);
        assertEquals(poly.getDataProviderId(), dataProvider);
        assertEquals(poly.getDescription(), description);
        assertEquals(poly.getProperties(), properties);
    }

    public static void validateRectangle(final Rectangle rect,
                                         final GeoRectangle geoRectangle,
                                         final double width,
                                         final double height,
                                         final FeatureTypeEnum fte,
                                         final List childFeatures,
                                         final List parentOverlays,
                                         final List parentFeatures,
                                         final List<IGeoPosition> positions,
                                         final Date date,
                                         final List timeSpans,
                                         final IGeoAltitudeMode.AltitudeMode altitudeMode,
                                         final IGeoStrokeStyle strokeStyle,
                                         final IGeoFillStyle fillStyle,
                                         final IGeoLabelStyle labelStyle,
                                         final Boolean extrude,
                                         final Boolean tessellate,
                                         final double buffer,
                                         final double azimuth,
                                         final GeoPosition geoPosition,
                                         final Boolean readOnly,
                                         final List parents,
                                         final Boolean hasChildren,
                                         final List children,
                                         final String name,
                                         final String dataProvider,
                                         final String description,
                                         final HashMap properties) {
        compareGeoRectangle(rect.getRenderable(), geoRectangle);
        assertEquals(rect.getWidth(), width, Epsilon);
        assertEquals(rect.getHeight(), height, Epsilon);
        assertEquals(rect.getFeatureType(), fte);
        assertEquals(rect.getChildFeatures(), childFeatures);
        assertEquals(rect.getParentOverlays(), parentOverlays);
        assertEquals(rect.getParentFeatures(), parentFeatures);
        compareGeoPositionArray(rect.getPositions(), positions);
        assertEquals(rect.getTimeStamp(), date);
        assertEquals(rect.getTimeSpans(), timeSpans);
        assertEquals(rect.getAltitudeMode(), altitudeMode);
        compareStrokeStyle(rect.getStrokeStyle(), strokeStyle);
        compareFillStyle(rect.getFillStyle(), fillStyle);
        compareLabelStyle(rect.getLabelStyle(), labelStyle);
        assertEquals(rect.getExtrude(), extrude);
        assertEquals(rect.getTessellate(), tessellate);
        assertEquals(rect.getBuffer(), buffer, Epsilon);
        assertEquals(rect.getAzimuth(), azimuth, Epsilon);
        compareGeoPosition(rect.getPosition(), geoPosition);
        assertEquals(rect.getReadOnly(), readOnly);
        assertEquals(rect.getParents(), parents);
        assertEquals(rect.hasChildren(), hasChildren);
        assertEquals(rect.getChildren(), children);
        assertEquals(rect.getName(), name);
        assertEquals(rect.getDataProviderId(), dataProvider);
        assertEquals(rect.getDescription(), description);
        assertEquals(rect.getProperties(), properties);
    }

    public static void validateSquare(final Square square,
                                      final GeoSquare geoSquare,
                                      final double width,
                                      final FeatureTypeEnum fte,
                                      final List childFeatures,
                                      final List parentOverlays,
                                      final List parentFeatures,
                                      final List<IGeoPosition> positions,
                                      final Date date,
                                      final List timeSpans,
                                      final IGeoAltitudeMode.AltitudeMode altitudeMode,
                                      final IGeoStrokeStyle strokeStyle,
                                      final IGeoFillStyle fillStyle,
                                      final IGeoLabelStyle labelStyle,
                                      final Boolean extrude,
                                      final Boolean tessellate,
                                      final double buffer,
                                      final double azimuth,
                                      final GeoPosition geoPosition,
                                      final Boolean readOnly,
                                      final List parents,
                                      final Boolean hasChildren,
                                      final List children,
                                      final String name,
                                      final String dataProvider,
                                      final String description,
                                      final HashMap properties) {
        compareGeoSquare(square.getRenderable(), geoSquare);
        assertEquals(square.getWidth(), width, Epsilon);
        assertEquals(square.getFeatureType(), fte);
        assertEquals(square.getChildFeatures(), childFeatures);
        assertEquals(square.getParentOverlays(), parentOverlays);
        assertEquals(square.getParentFeatures(), parentFeatures);
        compareGeoPositionArray(square.getPositions(), positions);
        assertEquals(square.getTimeStamp(), date);
        assertEquals(square.getTimeSpans(), timeSpans);
        assertEquals(square.getAltitudeMode(), altitudeMode);
        compareStrokeStyle(square.getStrokeStyle(), strokeStyle);
        compareFillStyle(square.getFillStyle(), fillStyle);
        compareLabelStyle(square.getLabelStyle(), labelStyle);
        assertEquals(square.getExtrude(), extrude);
        assertEquals(square.getTessellate(), tessellate);
        assertEquals(square.getBuffer(), buffer, Epsilon);
        assertEquals(square.getAzimuth(), azimuth, Epsilon);
        compareGeoPosition(square.getPosition(), geoPosition);
        assertEquals(square.getReadOnly(), readOnly);
        assertEquals(square.getParents(), parents);
        assertEquals(square.hasChildren(), hasChildren);
        assertEquals(square.getChildren(), children);
        assertEquals(square.getName(), name);
        assertEquals(square.getDataProviderId(), dataProvider);
        assertEquals(square.getDescription(), description);
        assertEquals(square.getProperties(), properties);
    }

    public static void validateText(final Text text,
                                    final GeoText geoText,
                                    final String textString,
                                    final double rotationAngle,
                                    final FeatureTypeEnum fte,
                                    final List childFeatures,
                                    final List parentOverlays,
                                    final List parentFeatures,
                                    final List<IGeoPosition> positions,
                                    final Date date,
                                    final List timeSpans,
                                    final IGeoAltitudeMode.AltitudeMode altitudeMode,
                                    final IGeoStrokeStyle strokeStyle,
                                    final IGeoFillStyle fillStyle,
                                    final IGeoLabelStyle labelStyle,
                                    final Boolean extrude,
                                    final Boolean tessellate,
                                    final double buffer,
                                    final double azimuth,
                                    final GeoPosition geoPosition,
                                    final Boolean readOnly,
                                    final List parents,
                                    final Boolean hasChildren,
                                    final List children,
                                    final String name,
                                    final String dataProvider,
                                    final String description,
                                    final HashMap properties) {
        compareGeoText(text.getRenderable(), geoText);
        assertEquals(text.getText(), textString);
        assertEquals(text.getRotationAngle(), rotationAngle, Epsilon);
        assertEquals(text.getFeatureType(), fte);
        assertEquals(text.getChildFeatures(), childFeatures);
        assertEquals(text.getParentOverlays(), parentOverlays);
        assertEquals(text.getParentFeatures(), parentFeatures);
        compareGeoPositionArray(text.getPositions(), positions);
        assertEquals(text.getTimeStamp(), date);
        assertEquals(text.getTimeSpans(), timeSpans);
        assertEquals(text.getAltitudeMode(), altitudeMode);
        compareStrokeStyle(text.getStrokeStyle(), strokeStyle);
        compareFillStyle(text.getFillStyle(), fillStyle);
        compareLabelStyle(text.getLabelStyle(), labelStyle);
        assertEquals(text.getExtrude(), extrude);
        assertEquals(text.getTessellate(), tessellate);
        assertEquals(text.getBuffer(), buffer, Epsilon);
        assertEquals(text.getAzimuth(), azimuth, Epsilon);
        compareGeoPosition(text.getPosition(), geoPosition);
        assertEquals(text.getReadOnly(), readOnly);
        assertEquals(text.getParents(), parents);
        assertEquals(text.hasChildren(), hasChildren);
        assertEquals(text.getChildren(), children);
        assertEquals(text.getName(), name);
        assertEquals(text.getDataProviderId(), dataProvider);
        assertEquals(text.getDescription(), description);
        assertEquals(text.getProperties(), properties);
    }

    public static void validateWMTS(final WMTS wmts1,
                                    final WMTSVersionEnum vers,
                                    final String tileFormat,
                                    final List<String> styles,
                                    final List<String> dims,
                                    final List<String> layers,
                                    final String url,
                                    final String name,
                                    final String dataProvider,
                                    final String description,
                                    final HashMap properties
                                    ) throws Exception {
        assertEquals(wmts1.getWMTSVersion(), vers);
        assertEquals(wmts1.getTileFormat(), tileFormat);
        assertEquals(wmts1.getStyles(), styles);
        assertEquals(wmts1.getDimensions(), dims);
        assertEquals(wmts1.getLayers(), layers);
        assertEquals(wmts1.getURL(), new URL(url));
        assertEquals(wmts1.getName(), name);
        assertEquals(wmts1.getDataProviderId(), dataProvider);
        assertEquals(wmts1.getDescription(), description);
        assertEquals(wmts1.getProperties(), properties);
    }

    public static void validateBoundingBox(final EmpBoundingBox ebb,
                                           final double north,
                                           final double east,
                                           final double south,
                                           final double west) {
        assertEquals(ebb.north(), north, Epsilon);
        assertEquals(ebb.east(), east, Epsilon);
        assertEquals(ebb.south(), south, Epsilon);
        assertEquals(ebb.west(), west, Epsilon);
    }
}