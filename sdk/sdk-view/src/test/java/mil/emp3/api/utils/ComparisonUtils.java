package mil.emp3.api.utils;

import android.graphics.Bitmap;

import org.cmapi.primitives.GeoEllipse;
import org.cmapi.primitives.GeoIconStyle;
import org.cmapi.primitives.GeoPoint;
import org.cmapi.primitives.GeoPolygon;
import org.cmapi.primitives.GeoPosition;
import org.cmapi.primitives.IGeoAltitudeMode;
import org.cmapi.primitives.IGeoColor;
import org.cmapi.primitives.IGeoEllipse;
import org.cmapi.primitives.IGeoFillStyle;
import org.cmapi.primitives.IGeoIconStyle;
import org.cmapi.primitives.IGeoLabelStyle;
import org.cmapi.primitives.IGeoPoint;
import org.cmapi.primitives.IGeoPolygon;
import org.cmapi.primitives.IGeoPosition;
import org.cmapi.primitives.IGeoStrokeStyle;

import java.util.Date;
import java.util.HashMap;
import java.util.List;

import mil.emp3.api.Ellipse;
import mil.emp3.api.Point;
import mil.emp3.api.Polygon;
import mil.emp3.api.enums.FeatureTypeEnum;

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

    private static void compareGeoEllipse(final IGeoEllipse r1, final IGeoEllipse r2) {
        assertEquals(r1.getSemiMajor(), r2.getSemiMajor(), Epsilon);
        assertEquals(r1.getSemiMinor(), r2.getSemiMinor(), Epsilon);
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
        assertEquals(ell.getSemiMajor(), majorRadius, Epsilon);
        assertEquals(ell.getSemiMinor(), minorRadius, Epsilon);
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
        assertEquals(ell.getBuffer(), buffer, Epsilon);
        assertEquals(ell.getAzimuth(), azimuth, Epsilon);
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