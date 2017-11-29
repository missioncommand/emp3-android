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
    public static final double Epsilon = 1e-8;

    private static void compareGeoPosition(final IGeoPosition p1, final IGeoPosition p2) {
        if (p1 == null && p2 == null) {
            return;
        }
        assertEquals(p1.getLatitude(), p2.getLatitude(), Epsilon);
        assertEquals(p1.getLongitude(), p2.getLongitude(), Epsilon);
        assertEquals(p1.getAltitude(), p2.getAltitude(), Epsilon);
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
        assertEquals(c1.getAlpha(), c2.getAlpha(), Epsilon);
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

    private static void compareGeoPoint(final IGeoPoint point1, final IGeoPoint point2) {
        compareGeoRenderable(point1, point2);

    }

    private static void compareGeoPolygon(final IGeoPolygon polygon1, final IGeoPolygon polygon2) {
        compareGeoRenderable(polygon1, polygon2);
    }

    private static void compareGeoRectangle(final IGeoRectangle rectangle1, final IGeoRectangle rectangle2) {
        assertEquals(rectangle1.getHeight(), rectangle2.getHeight(), Epsilon);
        assertEquals(rectangle1.getWidth(), rectangle2.getWidth(), Epsilon);
        compareGeoRenderable(rectangle1, rectangle2);
    }

    private static void compareGeoSquare(final IGeoSquare square1, final IGeoSquare square2) {
        assertEquals(square1.getWidth(), square2.getWidth(), Epsilon);
        compareGeoRenderable(square1, square2);
    }

    private static void compareGeoText(final IGeoText text1, final IGeoText text2) {
        compareGeoRenderable(text1,text2);
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
        assertEquals(geoRenderable1.getBuffer(), geoRenderable2.getBuffer(), Epsilon);
        assertEquals(geoRenderable1.getAzimuth(), geoRenderable2.getAzimuth(), Epsilon);
        assertEquals(geoRenderable1.getAltitudeMode(), geoRenderable2.getAltitudeMode());
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
        assertEquals(square.getWidth(), width, Epsilon);
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
        assertEquals(text.getRotationAngle(), rotationAngle, Epsilon);
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
        assertEquals(feature.getBuffer(), buffer, Epsilon);
        assertEquals(feature.getAzimuth(), azimuth, Epsilon);
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
        assertEquals(ebb.north(), north, Epsilon);
        assertEquals(ebb.east(), east, Epsilon);
        assertEquals(ebb.south(), south, Epsilon);
        assertEquals(ebb.west(), west, Epsilon);
    }

    public static void comparePoint(final Point point1, final Point point2) {
        assertEquals(point1.getIconURI(), point2.getIconURI());
//        assertEquals(point1.getIconScale(), point2.getIconScale(), Epsilon);
        compareIconStyle(point1.getIconStyle(), point2.getIconStyle());
        assertEquals(point1.getResourceId(), point2.getResourceId());
        compareFeature(point1, point2);
    }

    public static void comparePolygon(final Polygon polygon1, final Polygon polygon2) {
        assertEquals(polygon1.getPatternFillImage(), polygon2.getPatternFillImage());
        compareFeature(polygon1, polygon2);

    }

    public static void compareCircle(final Circle circle1, final Circle circle2) {
        assertEquals(circle1.getRadius(), circle2.getRadius(), Epsilon);
        compareFeature(circle1, circle2);
    }

    public static void compareEllipse(final Ellipse ellipse1, final Ellipse ellipse2) {
        assertEquals(ellipse1.getSemiMinor(), ellipse2.getSemiMinor(), Epsilon);
        assertEquals(ellipse1.getSemiMajor(), ellipse2.getSemiMajor(), Epsilon);
        compareFeature(ellipse1, ellipse2);
    }

    public static void compareRectangle(final Rectangle rectangle1, final Rectangle rectangle2) {
        assertEquals(rectangle1.getWidth(), rectangle2.getWidth(), Epsilon);
        assertEquals(rectangle1.getHeight(), rectangle2.getHeight(), Epsilon);
        compareFeature(rectangle1, rectangle2);
    }

    public static void compareSquare(final Square square1, final Square square2) {
        assertEquals(square1.getWidth(), square2.getWidth(), Epsilon);
        compareFeature(square1, square2);
    }

    public static void compareText(final Text text1, final Text text2) {
        assertEquals(text1.getRotationAngle(), text2.getRotationAngle(), Epsilon);
        assertEquals(text1.getText(), text2.getText());
        compareFeature(text1, text2);
    }

    public static void comparePath(final Path path1, final Path path2) {
        compareFeature(path1, path2);
    }

    public static void compareFeature(final Feature feature1, final Feature feature2) {
        compareGeoPosition(feature1.getPosition(), feature2.getPosition());
        assertEquals(feature1.getAzimuth(), feature2.getAzimuth(), Epsilon);
        assertEquals(feature1.getDescription(), feature2.getDescription());
        compareFillStyle(feature1.getFillStyle(), feature2.getFillStyle());
        assertEquals(feature1.getName(), feature2.getName());
        compareStrokeStyle(feature1.getStrokeStyle(), feature2.getStrokeStyle());
//        assertEquals(feature1.getPathType(), feature2.getPathType());
        assertEquals(feature1.getAzimuth(), feature2.getAzimuth(), Epsilon);
//        assertEquals(feature1.getExtrude(), feature2.getExtrude());
        compareLabelStyle(feature1.getLabelStyle(), feature2.getLabelStyle());
//        assertEquals(feature1.getAltitudeMode(), feature2.getAltitudeMode());
//        assertEquals(feature1.getBuffer(), feature2.getBuffer(), Epsilon);
//        assertEquals(feature1.getTessellate(), feature2.getTessellate());
        assertEquals(feature1.getFeatureType(), feature2.getFeatureType());
        assertEquals(feature1.getReadOnly(), feature2.getReadOnly());
    }
}
