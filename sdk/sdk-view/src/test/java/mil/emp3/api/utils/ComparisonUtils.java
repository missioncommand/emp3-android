package mil.emp3.api.utils;

import android.graphics.Bitmap;

import org.cmapi.primitives.GeoCircle;
import org.cmapi.primitives.GeoEllipse;
import org.cmapi.primitives.GeoIconStyle;
import org.cmapi.primitives.GeoPoint;
import org.cmapi.primitives.GeoPolygon;
import org.cmapi.primitives.GeoPosition;
import org.cmapi.primitives.GeoRectangle;
import org.cmapi.primitives.GeoSquare;
import org.cmapi.primitives.GeoText;
import org.cmapi.primitives.IGeoAltitudeMode;
import org.cmapi.primitives.IGeoCircle;
import org.cmapi.primitives.IGeoColor;
import org.cmapi.primitives.IGeoEllipse;
import org.cmapi.primitives.IGeoFillStyle;
import org.cmapi.primitives.IGeoIconStyle;
import org.cmapi.primitives.IGeoLabelStyle;
import org.cmapi.primitives.IGeoPoint;
import org.cmapi.primitives.IGeoPolygon;
import org.cmapi.primitives.IGeoPosition;
import org.cmapi.primitives.IGeoRectangle;
import org.cmapi.primitives.IGeoRenderable;
import org.cmapi.primitives.IGeoSquare;
import org.cmapi.primitives.IGeoStrokeStyle;
import org.cmapi.primitives.IGeoText;

import java.util.Date;
import java.util.HashMap;
import java.util.List;

import mil.emp3.api.Circle;
import mil.emp3.api.Ellipse;
import mil.emp3.api.Path;
import mil.emp3.api.Point;
import mil.emp3.api.Polygon;
import mil.emp3.api.Rectangle;
import mil.emp3.api.Square;
import mil.emp3.api.Text;
import mil.emp3.api.abstracts.Feature;
import mil.emp3.api.enums.FeatureTypeEnum;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;


/**
 * Created by matt.miller@rgi-corp.local on 10/27/17.
 */
public class ComparisonUtils {
    public static final double EPSILON8 = 1e-8;
    public static final double EPSILON3 = 1e-3;
    public static final double EPSILON2 = 1e-2;

    private static void compareDoublesWithLog(final double value1, final double value2, final double marginOfError, final String tag) {
        if (Math.abs(value1 - value2) < marginOfError) {
            return;
        } else {
            System.out.println(tag+": "+value1 + " is too different in value from " + value2 + " when comparing to an accuracy of " + marginOfError);
        }
    }

    private static void compareGeoPosition(final IGeoPosition p1, final IGeoPosition p2, final double epsilon) {
        if (p1 == null && p2 == null) {
            return;
        }
        compareDoublesWithLog(p1.getLatitude(), p2.getLatitude(), epsilon, "Latitude");
        compareDoublesWithLog(p1.getLongitude(), p2.getLongitude(), epsilon, "Longitude");
        compareDoublesWithLog(p1.getAltitude(), p2.getAltitude(), epsilon, "Altitude");
    }

    private static void compareGeoPosition(final IGeoPosition p1, final IGeoPosition p2) {
        compareGeoPosition(p1, p2, EPSILON8);
    }

    private static void compareGeoPositionArray(final List<IGeoPosition> p1, final List<IGeoPosition> p2) {
        final double size = p1.size();
        if (size != p2.size()) {
            fail("Two Geoposition Lists are of unequal length");
        }
        for (int i = 0; i < size; i++) {
            compareGeoPosition(p1.get(i), p2.get(i));
        }
    }

    private static void compareGeoColor(final IGeoColor c1, final IGeoColor c2) {
        compareDoublesWithLog(c1.getAlpha(), c2.getAlpha(), EPSILON8, "Color Alpha");
        assertEquals(c1.getRed(), c2.getRed());
        assertEquals(c1.getBlue(), c2.getBlue());
        assertEquals(c1.getGreen(), c2.getGreen());
    }

    private static void compareFillStyle(final IGeoFillStyle f1, final IGeoFillStyle f2) {
        if (f1 == null && f2 == null) {
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
        assertEquals(s1.getStrokeWidth(), s2.getStrokeWidth(), EPSILON8);
    }

    private static void compareIconStyle(final IGeoIconStyle i1, final IGeoIconStyle i2) {
        compareDoublesWithLog(i1.getOffSetX(), i2.getOffSetX(), EPSILON8, "Icon OffsetX Value");
        compareDoublesWithLog(i1.getOffSetY(), i2.getOffSetY(), EPSILON8, "Icon Offset Y Value");
        compareDoublesWithLog(i1.getSize(), i2.getSize(), EPSILON8, "Icon Size");
    }

    private static void compareLabelStyle(final IGeoLabelStyle l1, final IGeoLabelStyle l2) {
        compareGeoColor(l1.getColor(), l2.getColor());
        assertEquals(l1.getFontFamily(), l2.getFontFamily());
        assertEquals(l1.getJustification(), l2.getJustification());
        compareGeoColor(l1.getOutlineColor(), l2.getOutlineColor());
        compareDoublesWithLog(l1.getSize(), l2.getSize(), EPSILON8, "Label Size");
        assertEquals(l1.getTypeface(), l2.getTypeface());
    }

    private static void compareGeoPoint(final IGeoPoint point1, final IGeoPoint point2) {
        compareGeoRenderable(point1, point2);

    }

    private static void compareGeoPolygon(final IGeoPolygon polygon1, final IGeoPolygon polygon2) {
        compareGeoRenderable(polygon1, polygon2);
    }

    private static void compareGeoRectangle(final IGeoRectangle rectangle1, final IGeoRectangle rectangle2) {
        compareDoublesWithLog(rectangle1.getHeight(), rectangle2.getHeight(), EPSILON8, "GeoRectangle Height");
        compareDoublesWithLog(rectangle1.getWidth(), rectangle2.getWidth(), EPSILON8, "GeoRectangle Width");
        compareGeoRenderable(rectangle1, rectangle2);
    }

    private static void compareGeoSquare(final IGeoSquare square1, final IGeoSquare square2) {
        compareDoublesWithLog(square1.getWidth(), square2.getWidth(), EPSILON8, "GeoSquare Width");
        compareGeoRenderable(square1, square2);
    }

    private static void compareGeoText(final IGeoText text1, final IGeoText text2) {
        compareGeoRenderable(text1,text2);
    }

    private static void compareGeoEllipse(final IGeoEllipse ellipse1, final IGeoEllipse ellipse2) {
        compareDoublesWithLog(ellipse1.getSemiMajor(), ellipse2.getSemiMajor(), EPSILON8, "GeoEllipse SemiMajor");
        compareDoublesWithLog(ellipse1.getSemiMinor(), ellipse2.getSemiMinor(), EPSILON8, "GeoEllipse SemiMinor");
        compareGeoRenderable(ellipse1, ellipse2);
    }

    private static void compareGeoRenderable(final IGeoRenderable geoRenderable1, final IGeoRenderable geoRenderable2) {
        assertEquals(geoRenderable1.getTimeStamp(), geoRenderable2.getTimeStamp());
        assertEquals(geoRenderable1.getTimeSpans(), geoRenderable2.getTimeSpans());
        assertEquals(geoRenderable1.getTessellate(), geoRenderable2.getTessellate());
        compareStrokeStyle(geoRenderable1.getStrokeStyle(), geoRenderable2.getStrokeStyle());
        assertEquals(geoRenderable1.getReadOnly(), geoRenderable2.getReadOnly());
        assertEquals(geoRenderable1.getProperties(), geoRenderable2.getProperties());
        compareGeoPositionArray(geoRenderable1.getPositions(), geoRenderable2.getPositions());
        assertEquals(geoRenderable1.getPathType(), geoRenderable2.getPathType());
        assertEquals(geoRenderable1.getName(), geoRenderable2.getName());
        compareLabelStyle(geoRenderable1.getLabelStyle(), geoRenderable2.getLabelStyle());
        compareFillStyle(geoRenderable1.getFillStyle(), geoRenderable2.getFillStyle());
        assertEquals(geoRenderable1.getExtrude(), geoRenderable2.getExtrude());
        assertEquals(geoRenderable1.getDescription(), geoRenderable2.getDescription());
        assertEquals(geoRenderable1.getDataProviderId(), geoRenderable2.getDataProviderId());
        assertEquals(geoRenderable1.getChildren(), geoRenderable2.getChildren());
        assertEquals(geoRenderable1.getBuffer(), geoRenderable2.getBuffer(), EPSILON8);
        compareDoublesWithLog(geoRenderable1.getAzimuth(), geoRenderable2.getAzimuth(), EPSILON8, "GeoRenderable Azimuth");
        assertEquals(geoRenderable1.getAltitudeMode(), geoRenderable2.getAltitudeMode());
    }

    private static void compareGeoCircle(final IGeoCircle circle1, final IGeoCircle circle2) {
        compareDoublesWithLog(circle1.getRadius(), circle2.getRadius(), EPSILON8, "GeoCircle Radius");
        compareGeoRenderable(circle1, circle2);
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
        compareDoublesWithLog(point.getIconScale(), iconScale, EPSILON8, "Point Icon Scale");
        compareDoublesWithLog(point.getResourceId(), resourceId, EPSILON8, "Point Resource ID");
        compareIconStyle(point.getIconStyle(), iconStyle);
        assertEquals(point.getIconURI(), iconURI);
        compareGeoPoint(point.getRenderable(), geoPoint);
        validateFeature(point,
                        fte,
                        childFeatures,
                        parentOverlays,
                        parentFeatures,
                        positions,
                        date,
                        timeSpans,
                        altitudeMode,
                        strokeStyle,
                        fillStyle,
                        labelStyle,
                        extrude,
                        tessellate,
                        buffer,
                        azimuth,
                        geoPosition,
                        readOnly,
                        parents,
                        hasChildren,
                        children,
                        name,
                        dataProvider,
                        description,
                        properties);
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
        validateFeature(poly,
                        fte,
                        childFeatures,
                        parentOverlays,
                        parentFeatures,
                        positions,
                        date,
                        timeSpans,
                        altitudeMode,
                        strokeStyle,
                        fillStyle,
                        labelStyle,
                        extrude,
                        tessellate,
                        buffer,
                        azimuth,
                        geoPosition,
                        readOnly,
                        parents,
                        hasChildren,
                        children,
                        name,
                        dataProvider,
                        description,
                        properties);
    }

    public static void validateEllipse(final Ellipse ell,
                                       final GeoEllipse geoEllipse,
                                       final double majorRadius,
                                       final double minorRadius,
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
        compareGeoEllipse(ell.getRenderable(), geoEllipse);
        compareDoublesWithLog(ell.getSemiMajor(), majorRadius, EPSILON8, "Ellipse SemiMajor");
        compareDoublesWithLog(ell.getSemiMinor(), minorRadius, EPSILON8, "Ellipse SemiMinor");
        assertEquals(ell.getFeatureType(), fte);
        assertEquals(ell.getChildFeatures(), childFeatures);
        assertEquals(ell.getParentOverlays(), parentOverlays);
        assertEquals(ell.getParentFeatures(), parentFeatures);
        compareGeoPositionArray(ell.getPositions(), positions);
        assertEquals(ell.getTimeStamp(), date);
        assertEquals(ell.getTimeSpans(), timeSpans);
        assertEquals(ell.getAltitudeMode(), altitudeMode);
        compareStrokeStyle(ell.getStrokeStyle(), strokeStyle);
        compareFillStyle(ell.getFillStyle(), fillStyle);
        compareLabelStyle(ell.getLabelStyle(), labelStyle);
        assertEquals(ell.getExtrude(), extrude);
        assertEquals(ell.getTessellate(), tessellate);
        compareDoublesWithLog(ell.getBuffer(), buffer, EPSILON8, "Ellipse Buffer");
        compareDoublesWithLog(ell.getAzimuth(), azimuth, EPSILON8, "Ellipse Azimuth");
        compareGeoPosition(ell.getPosition(), geoPosition);
        assertEquals(ell.getReadOnly(), readOnly);
        assertEquals(ell.getParents(), parents);
        assertEquals(ell.hasChildren(), hasChildren);
        assertEquals(ell.getChildren(), children);
        assertEquals(ell.getName(), name);
        assertEquals(ell.getDataProviderId(), dataProvider);
        assertEquals(ell.getDescription(), description);
        assertEquals(ell.getProperties(), properties);

    }
    public static void validateCircle(final Circle circ,
                                      final GeoCircle geoCircle,
                                      final double radius,
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
        compareGeoCircle(circ.getRenderable(), geoCircle);
        compareDoublesWithLog(circ.getRadius(), radius, EPSILON8, "Circle Radius");
        assertEquals(circ.getFeatureType(), fte);
        assertEquals(circ.getChildFeatures(), childFeatures);
        assertEquals(circ.getParentOverlays(), parentOverlays);
        assertEquals(circ.getParentFeatures(), parentFeatures);
        compareGeoPositionArray(circ.getPositions(), positions);
        assertEquals(circ.getTimeStamp(), date);
        assertEquals(circ.getTimeSpans(), timeSpans);
        assertEquals(circ.getAltitudeMode(), altitudeMode);
        compareStrokeStyle(circ.getStrokeStyle(), strokeStyle);
        compareFillStyle(circ.getFillStyle(), fillStyle);
        compareLabelStyle(circ.getLabelStyle(), labelStyle);
        assertEquals(circ.getExtrude(), extrude);
        assertEquals(circ.getTessellate(), tessellate);
        compareDoublesWithLog(circ.getBuffer(), buffer, EPSILON8, "Circle Buffer");
        compareDoublesWithLog(circ.getAzimuth(), azimuth, EPSILON8, "Circle Azimuth");
        compareGeoPosition(circ.getPosition(), geoPosition);
        assertEquals(circ.getReadOnly(), readOnly);
        assertEquals(circ.getParents(), parents);
        assertEquals(circ.hasChildren(), hasChildren);
        assertEquals(circ.getChildren(), children);
        assertEquals(circ.getName(), name);
        assertEquals(circ.getDataProviderId(), dataProvider);
        assertEquals(circ.getDescription(), description);
        assertEquals(circ.getProperties(), properties);
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
        compareDoublesWithLog(rect.getWidth(), width, EPSILON8, "Rectangle Width");
        compareDoublesWithLog(rect.getHeight(), height, EPSILON8, "Rectangle Height");
        validateFeature(rect,
                        fte,
                        childFeatures,
                        parentOverlays,
                        parentFeatures,
                        positions,
                        date,
                        timeSpans,
                        altitudeMode,
                        strokeStyle,
                        fillStyle,
                        labelStyle,
                        extrude,
                        tessellate,
                        buffer,
                        azimuth,
                        geoPosition,
                        readOnly,
                        parents,
                        hasChildren,
                        children,
                        name,
                        dataProvider,
                        description,
                        properties);
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
        compareDoublesWithLog(square.getWidth(), width, EPSILON8, "Square Width");
        validateFeature(square,
                        fte,
                        childFeatures,
                        parentOverlays,
                        parentFeatures,
                        positions,
                        date,
                        timeSpans,
                        altitudeMode,
                        strokeStyle,
                        fillStyle,
                        labelStyle,
                        extrude,
                        tessellate,
                        buffer,
                        azimuth,
                        geoPosition,
                        readOnly,
                        parents,
                        hasChildren,
                        children,
                        name,
                        dataProvider,
                        description,
                        properties);
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
        compareDoublesWithLog(text.getRotationAngle(), rotationAngle, EPSILON8, "Text Rotation Angle");
        validateFeature(text,
                        fte,
                        childFeatures,
                        parentOverlays,
                        parentFeatures,
                        positions,
                        date,
                        timeSpans,
                        altitudeMode,
                        strokeStyle,
                        fillStyle,
                        labelStyle,
                        extrude,
                        tessellate,
                        buffer,
                        azimuth,
                        geoPosition,
                        readOnly,
                        parents,
                        hasChildren,
                        children,
                        name,
                        dataProvider,
                        description,
                        properties);
    }

    public static void validateFeature(final Feature feature,
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
        assertEquals(feature.getFeatureType(), fte);
        assertEquals(feature.getChildFeatures(), childFeatures);
        assertEquals(feature.getParentOverlays(), parentOverlays);
        assertEquals(feature.getParentFeatures(), parentFeatures);
        compareGeoPositionArray(feature.getPositions(), positions);
        assertEquals(feature.getTimeStamp(), date);
        assertEquals(feature.getTimeSpans(), timeSpans);
        assertEquals(feature.getAltitudeMode(), altitudeMode);
        compareStrokeStyle(feature.getStrokeStyle(), strokeStyle);
        compareFillStyle(feature.getFillStyle(), fillStyle);
        compareLabelStyle(feature.getLabelStyle(), labelStyle);
        assertEquals(feature.getExtrude(), extrude);
        assertEquals(feature.getTessellate(), tessellate);
        compareDoublesWithLog(feature.getBuffer(), buffer, EPSILON8, "Feature Buffer");
        compareDoublesWithLog(feature.getAzimuth(), azimuth, EPSILON8, "Feature Azimuth");
        compareGeoPosition(feature.getPosition(), geoPosition);
        assertEquals(feature.getReadOnly(), readOnly);
        assertEquals(feature.getParents(), parents);
        assertEquals(feature.hasChildren(), hasChildren);
        assertEquals(feature.getChildren(), children);
        assertEquals(feature.getName(), name);
        assertEquals(feature.getDataProviderId(), dataProvider);
        assertEquals(feature.getDescription(), description);
        assertEquals(feature.getProperties(), properties);

    }

    public static void validateBoundingBox(final EmpBoundingBox ebb,
                                           final double north,
                                           final double east,
                                           final double south,
                                           final double west) {
        compareDoublesWithLog(ebb.north(), north, EPSILON8, "Bounding Box North");
        compareDoublesWithLog(ebb.east(), east, EPSILON8, "Bounding Box East");
        compareDoublesWithLog(ebb.south(), south, EPSILON8, "Bounding Box South");
        compareDoublesWithLog(ebb.west(), west, EPSILON8, "Bounding Box West");
    }

    public static void comparePoint(final Point point1, final Point point2) {
        assertEquals(point1.getIconURI(), point2.getIconURI());
//        assertEquals(point1.getIconScale(), point2.getIconScale(), EPSILON8);
        compareIconStyle(point1.getIconStyle(), point2.getIconStyle());
        assertEquals(point1.getResourceId(), point2.getResourceId());
        compareGeoPosition(point1.getPosition(), point2.getPosition());
    }

    public static void comparePolygon(final Polygon polygon1, final Polygon polygon2) {
        assertEquals(polygon1.getPatternFillImage(), polygon2.getPatternFillImage());
        compareFeature(polygon1, polygon2);

    }

    public static void compareCircle(final Circle circle1, final Circle circle2) {
        compareDoublesWithLog(circle1.getRadius(), circle2.getRadius(), EPSILON8, "Circle Radius");
        compareFeature(circle1, circle2);
    }

    public static void compareEllipse(final Ellipse ellipse1, final Ellipse ellipse2) {
        compareDoublesWithLog(ellipse1.getSemiMinor(), ellipse2.getSemiMinor(), EPSILON8, "Ellipse SemiMinor");
        compareDoublesWithLog(ellipse1.getSemiMajor(), ellipse2.getSemiMajor(), EPSILON8, "Ellipse SemiMajor");
        compareFeature(ellipse1, ellipse2);
    }

    public static void compareRectangle(final Rectangle rectangle1, final Rectangle rectangle2) {
        compareDoublesWithLog(rectangle1.getWidth(), rectangle2.getWidth(), EPSILON8, "Rectangle Width");
        compareDoublesWithLog(rectangle1.getHeight(), rectangle2.getHeight(), EPSILON8, "Rectangle Height");
        compareFeature(rectangle1, rectangle2);
    }

    public static void compareSquare(final Square square1, final Square square2) {
        compareDoublesWithLog(square1.getWidth(), square2.getWidth(), EPSILON8, "Square Width");
        compareFeature(square1, square2);
    }

    public static void compareText(final Text text1, final Text text2) {
        compareDoublesWithLog(text1.getRotationAngle(), text2.getRotationAngle(), EPSILON8, "Text Rotation Angle");
        assertEquals(text1.getText(), text2.getText());
        compareFeature(text1, text2);
    }

    public static void comparePath(final Path path1, final Path path2) {
//        compareFeature(path1, path2);
        compareGeoPosition(path1.getPosition(), path2.getPosition());
        assertEquals(path1.getAzimuth(), path2.getAzimuth(), EPSILON8);
        assertEquals(path1.getDescription(), path2.getDescription());
        assertEquals(path1.getName(), path2.getName());
        compareStrokeStyle(path1.getStrokeStyle(), path2.getStrokeStyle());
        compareLabelStyle(path1.getLabelStyle(), path2.getLabelStyle());
        assertEquals(path1.getFeatureType(), path2.getFeatureType());
        assertEquals(path1.getReadOnly(), path2.getReadOnly());
    }

    public static void compareFeature(final Feature feature1, final Feature feature2) {
        compareGeoPosition(feature1.getPosition(), feature2.getPosition());
        assertEquals(feature1.getAzimuth(), feature2.getAzimuth(), EPSILON8);
        assertEquals(feature1.getDescription(), feature2.getDescription());
        compareFillStyle(feature1.getFillStyle(), feature2.getFillStyle());
        assertEquals(feature1.getName(), feature2.getName());
        compareStrokeStyle(feature1.getStrokeStyle(), feature2.getStrokeStyle());
//        assertEquals(feature1.getPathType(), feature2.getPathType());
//        assertEquals(feature1.getExtrude(), feature2.getExtrude());
        compareLabelStyle(feature1.getLabelStyle(), feature2.getLabelStyle());
//        assertEquals(feature1.getAltitudeMode(), feature2.getAltitudeMode());
//        assertEquals(feature1.getBuffer(), feature2.getBuffer(), EPSILON8);
//        assertEquals(feature1.getTessellate(), feature2.getTessellate());
        assertEquals(feature1.getFeatureType(), feature2.getFeatureType());
        assertEquals(feature1.getReadOnly(), feature2.getReadOnly());
    }

    public static void compareTextToPoint(final Text text, final Point point) {
        compareGeoPosition(text.getPosition(), point.getPosition());
        assertEquals(text.getDescription(), point.getDescription());
        assertEquals(text.getName(), point.getName());
        compareFillStyle(text.getFillStyle(), point.getFillStyle());
        compareStrokeStyle(text.getStrokeStyle(), point.getStrokeStyle());
        compareLabelStyle(text.getLabelStyle(), point.getLabelStyle());
        assertEquals(text.getReadOnly(), point.getReadOnly());
    }

    public static void compareCircleToPolygon(final Circle circle, final Polygon polygon) {
        compareGeoPosition(polygon.getPosition(), circle.getPosition(), EPSILON3);
        compareDoublesWithLog(polygon.getAzimuth(), circle.getAzimuth(), EPSILON8, "Polygon vs. Circle Azimuth");
        assertEquals(polygon.getDescription(), "<b>" + circle.getName() + "</b><br/>\n" + circle.getDescription());
        compareFillStyle(polygon.getFillStyle(), circle.getFillStyle());
        compareStrokeStyle(polygon.getStrokeStyle(), circle.getStrokeStyle());
        compareLabelStyle(polygon.getLabelStyle(), circle.getLabelStyle());
        assertEquals(polygon.getReadOnly(), circle.getReadOnly());
    }

    public static void compareSquareToPolygon(final Square square, final Polygon polygon) {
        compareGeoPosition(polygon.getPosition(), square.getPosition(), EPSILON3);
        compareDoublesWithLog(polygon.getAzimuth(), square.getAzimuth(), EPSILON8, "Polygon vs. Square Azimuth");
        assertEquals(polygon.getDescription(), "<b>" + square.getName() + "</b><br/>\n" + square.getDescription());
        compareFillStyle(polygon.getFillStyle(), square.getFillStyle());
        compareStrokeStyle(polygon.getStrokeStyle(), square.getStrokeStyle());
        compareLabelStyle(polygon.getLabelStyle(), square.getLabelStyle());
        assertEquals(polygon.getReadOnly(), square.getReadOnly());
    }

    public static void compareEllipseToPolygon(final Ellipse ellipse, final Polygon polygon) {
        compareGeoPosition(polygon.getPosition(), ellipse.getPosition(), EPSILON3);
        compareDoublesWithLog(polygon.getAzimuth(), ellipse.getAzimuth(), EPSILON8, "Polygon vs. Ellipse Azimuth");
        assertEquals(polygon.getDescription(), "<b>" + ellipse.getName() + "</b><br/>\n" + ellipse.getDescription());
        compareFillStyle(polygon.getFillStyle(), ellipse.getFillStyle());
        compareStrokeStyle(polygon.getStrokeStyle(), ellipse.getStrokeStyle());
        compareLabelStyle(polygon.getLabelStyle(), ellipse.getLabelStyle());
        assertEquals(polygon.getReadOnly(), ellipse.getReadOnly());
    }

    public static void compareRectangleToPolygon(final Rectangle rectangle, final Polygon polygon) {
        compareGeoPosition(polygon.getPosition(), rectangle.getPosition(), EPSILON2);
        compareDoublesWithLog(polygon.getAzimuth(), rectangle.getAzimuth(), EPSILON8, "Polygon vs. Rectangle Azimuth");
        assertEquals(polygon.getDescription(), "<b>" + rectangle.getName() + "</b><br/>\n" + rectangle.getDescription());
        compareFillStyle(polygon.getFillStyle(), rectangle.getFillStyle());
        compareStrokeStyle(polygon.getStrokeStyle(), rectangle.getStrokeStyle());
        compareLabelStyle(polygon.getLabelStyle(), rectangle.getLabelStyle());
        assertEquals(polygon.getReadOnly(), rectangle.getReadOnly());
    }

    public static void compareFeatureToPolygon(final Feature feature, final Polygon polygon) {
        compareGeoPosition(polygon.getPosition(), feature.getPosition(), EPSILON2);
        compareDoublesWithLog(polygon.getAzimuth(), feature.getAzimuth(), EPSILON8, "Polygon vs. Feature Azimuth");
        assertEquals(polygon.getDescription(), "<b>" + feature.getName() + "</b><br/>\n" + feature.getDescription());
        compareFillStyle(polygon.getFillStyle(), feature.getFillStyle());
        compareStrokeStyle(polygon.getStrokeStyle(), feature.getStrokeStyle());
        compareLabelStyle(polygon.getLabelStyle(), feature.getLabelStyle());
        assertEquals(polygon.getReadOnly(), feature.getReadOnly());
    }

    /* Separate method for GeoJson because SEC Renderer translates differently
     */

    public static void compareFeatureToGeoJsonPolygon(final Feature feature, final Polygon polygon) {
        compareGeoPosition(polygon.getPosition(), feature.getPosition(), EPSILON2);
        compareDoublesWithLog(polygon.getAzimuth(), feature.getAzimuth(), EPSILON8, "Polygon vs. Feature JSON Azimuth");
    }

    public static void compareFeatureToPath(final Feature feature, final Path path) {
        compareGeoPosition(path.getPosition(), feature.getPosition(), EPSILON2);
        compareDoublesWithLog(path.getAzimuth(), feature.getAzimuth(), EPSILON8, "Path vs. Feature Azimuth");
        assertEquals(path.getDescription(), "<b>" + feature.getName() + "</b><br/>\n" + feature.getDescription());
        compareFillStyle(path.getFillStyle(), feature.getFillStyle());
        compareStrokeStyle(path.getStrokeStyle(), feature.getStrokeStyle());
        compareLabelStyle(path.getLabelStyle(), feature.getLabelStyle());
        assertEquals(path.getReadOnly(), feature.getReadOnly());
    }

}
