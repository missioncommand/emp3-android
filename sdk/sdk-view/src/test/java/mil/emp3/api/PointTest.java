package mil.emp3.api;

import android.test.mock.MockContext;
import android.util.Log;
import android.webkit.URLUtil;

import org.bouncycastle.asn1.ASN1Sequence;
import org.cmapi.primitives.GeoFillStyle;
import org.cmapi.primitives.GeoIconStyle;
import org.cmapi.primitives.GeoLabelStyle;
import org.cmapi.primitives.GeoPoint;
import org.cmapi.primitives.GeoPosition;
import org.cmapi.primitives.GeoStrokeStyle;
import org.cmapi.primitives.IGeoAltitudeMode;
import org.cmapi.primitives.IGeoColor;
import org.cmapi.primitives.IGeoFillStyle;
import org.cmapi.primitives.IGeoIconStyle;
import org.cmapi.primitives.IGeoLabelStyle;
import org.cmapi.primitives.IGeoPoint;
import org.cmapi.primitives.IGeoPosition;
import org.cmapi.primitives.IGeoStrokeStyle;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;

import java.io.File;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.powermock.api.mockito.PowerMockito.when;
import org.junit.*;

import mil.emp3.api.enums.FeatureTypeEnum;
import sec.geo.kml.KmlOptions;

/**
 * Created by Matt.Miller on 10/16/2017.
 */
@PrepareForTest({URLUtil.class})
public class PointTest extends TestBase {

    public static final String TAG = PointTest.class.getSimpleName();
    private static final double Epsilon = 1e-8;
    Point p1;
    private GeoPoint gp;

    @Before
    public void setUp() throws Exception {
        PowerMockito.mockStatic(URLUtil.class);
        when(URLUtil.isValidUrl(any(String.class))).thenReturn(true);
        p1 = new Point();
    }

    private void compareGeoPosition(final IGeoPosition p1, final IGeoPosition p2) {
        if(p1 == null && p2 == null) {
            return;
        }
        assertEquals(p1.getLatitude(), p2.getLatitude(), Epsilon);
        assertEquals(p1.getLongitude(), p2.getLongitude(), Epsilon);
        assertEquals(p1.getAltitude(), p2.getAltitude(), Epsilon);
    }

    private void compareGeoPositionArray(final List<IGeoPosition> p1, final List<IGeoPosition> p2) {
        final double size = p1.size();
        if(size != p2.size()){
            fail();
        }
        for(int i = 0; i < size; i++) {
            compareGeoPosition(p1.get(i), p2.get(i));
        }
    }

    private void compareGeoColor(final IGeoColor c1, final IGeoColor c2) {
        assertEquals(c1.getAlpha(), c2.getAlpha(), Epsilon);
        assertEquals(c1.getRed(), c2.getRed());
        assertEquals(c1.getBlue(), c2.getBlue());
        assertEquals(c1.getGreen(), c2.getGreen());
    }

    private void compareFillStyle(final IGeoFillStyle f1, final IGeoFillStyle f2) {
        assertEquals(f1.getDescription(), f2.getDescription());
        compareGeoColor(f1.getFillColor(), f2.getFillColor());
        assertEquals(f1.getFillPattern(), f2.getFillPattern());
    }

    private void compareStrokeStyle(final IGeoStrokeStyle s1, final IGeoStrokeStyle s2) {
        assertEquals(s1.getStipplingFactor(), s2.getStipplingFactor());
        assertEquals(s1.getStipplingPattern(), s2.getStipplingPattern());
        compareGeoColor(s1.getStrokeColor(), s2.getStrokeColor());
        assertEquals(s1.getStrokeWidth(), s2.getStrokeWidth(), Epsilon);
    }

    private void compareIconStyle(final IGeoIconStyle i1, final IGeoIconStyle i2) {
        assertEquals(i1.getOffSetX(), i2.getOffSetX(), Epsilon);
        assertEquals(i1.getOffSetY(), i2.getOffSetY(), Epsilon);
        assertEquals(i1.getSize(), i2.getSize(), Epsilon);
    }

    private void compareLabelStyle(final IGeoLabelStyle l1, final IGeoLabelStyle l2) {
        compareGeoColor(l1.getColor(), l2.getColor());
        assertEquals(l1.getFontFamily(), l2.getFontFamily());
        assertEquals(l1.getJustification(), l2.getJustification());
        compareGeoColor(l1.getOutlineColor(), l2.getOutlineColor());
        assertEquals(l1.getSize(), l2.getSize(), Epsilon);
        assertEquals(l1.getTypeface(), l2.getTypeface());
    }

    private void compareGeoPoint(final IGeoPoint p1, final IGeoPoint p2) {
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

    private void validatePoint(final Point point,
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
                               final UUID geoId,
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



    @Test
    public void defaultConstructor() {
        p1 = new Point();
        validatePoint(p1,
                1.0,
                0,
                new GeoIconStyle(),
                null,
                new GeoPoint(),
                FeatureTypeEnum.GEO_POINT,
                Collections.EMPTY_LIST,
                Collections.EMPTY_LIST,
                Collections.EMPTY_LIST,
                Collections.EMPTY_LIST,
                null,
                Collections.EMPTY_LIST,
                null,
                new GeoStrokeStyle(),
                new GeoFillStyle(),
                new GeoLabelStyle(),
                false,
                true,
                0.0,
                0.0,
                null,
                false,
                Collections.EMPTY_LIST,
                false,
                Collections.EMPTY_LIST,
                "",
                new UUID(0,0),
                null,
                "",
                new HashMap());
    }

    @Test
    public void stringConstructor() {
        final String sampleURL = "127.0.0.1";
        p1 = new Point(sampleURL);
        final GeoPoint gp = new GeoPoint();
        gp.setIconURI(sampleURL);
        validatePoint(p1,
                1.0,
                0,
                new GeoIconStyle(),
                sampleURL,
                gp,
                FeatureTypeEnum.GEO_POINT,
                Collections.EMPTY_LIST,
                Collections.EMPTY_LIST,
                Collections.EMPTY_LIST,
                Collections.EMPTY_LIST,
                null,
                Collections.EMPTY_LIST,
                null,
                new GeoStrokeStyle(),
                new GeoFillStyle(),
                new GeoLabelStyle(),
                false,
                true,
                0.0,
                0.0,
                null,
                false,
                Collections.EMPTY_LIST,
                false,
                Collections.EMPTY_LIST,
                "",
                new UUID(0,0),
                null,
                "",
                new HashMap());
    }

    @Test
    public void geoPointConstructor() {
        gp = new GeoPoint();
        p1 = new Point(gp);
        validatePoint(p1,
                1.0,
                0,
                new GeoIconStyle(),
                null,
                gp,
                FeatureTypeEnum.GEO_POINT,
                Collections.EMPTY_LIST,
                Collections.EMPTY_LIST,
                Collections.EMPTY_LIST,
                Collections.EMPTY_LIST,
                null,
                Collections.EMPTY_LIST,
                null,
                new GeoStrokeStyle(),
                new GeoFillStyle(),
                new GeoLabelStyle(),
                false,
                true,
                0.0,
                0.0,
                null,
                false,
                Collections.EMPTY_LIST,
                false,
                Collections.EMPTY_LIST,
                "",
                new UUID(0,0),
                null,
                "",
                new HashMap());
    }

    @Test
    public void latLongConstructor() {
        p1 = new Point(50,50);
        final GeoPosition geoPos = new GeoPosition();
        geoPos.setLatitude(50);
        geoPos.setLongitude(50);
        final GeoPoint geoPoint = new GeoPoint();
        geoPoint.setPositions(Collections.singletonList(geoPos));
        validatePoint(p1,
                1.0,
                0,
                new GeoIconStyle(),
                null,
                geoPoint,
                FeatureTypeEnum.GEO_POINT,
                Collections.EMPTY_LIST,
                Collections.EMPTY_LIST,
                Collections.EMPTY_LIST,
                Collections.singletonList(geoPos),
                null,
                Collections.EMPTY_LIST,
                null,
                new GeoStrokeStyle(),
                new GeoFillStyle(),
                new GeoLabelStyle(),
                false,
                true,
                0.0,
                0.0,
                geoPos,
                false,
                Collections.EMPTY_LIST,
                false,
                Collections.EMPTY_LIST,
                "",
                new UUID(0,0),
                null,
                "",
                new HashMap());
    }

    @Test
    public void testToString() {
        p1 = new Point(50,50);
        final String expectedResult = "Point at\n" +
                                "\tlatitude: 50.0\n" +
                                "\tlongitude: 50.0\n" +
                                "\taltitude: 0.0";
        assertEquals(p1.toString(), expectedResult);
    }

    @Test(expected = IllegalArgumentException.class)
    public void invalidURLConstructor() {
        when(URLUtil.isValidUrl(any(String.class))).thenReturn(false);
        p1 = new Point("xyz");
    }

    @Test(expected = IllegalArgumentException.class)
    public void nanLatConstructor() {
        p1 = new Point(Double.NaN, 50);

    }

    @Test(expected = IllegalArgumentException.class)
    public void nanLongConstructor() {
        p1 = new Point(50, Double.NaN);
    }

    @Test(expected = IllegalArgumentException.class)
    public void invalidLatConstructor() {
        p1 = new Point(-100, 50);
    }

    @Test(expected = IllegalArgumentException.class)
    public void invalidLongConstructor() {
        p1 = new Point(50, -300);
    }

    @Test
    public void testIconStyle() throws Exception {
        final GeoIconStyle style = new GeoIconStyle();
        p1.setIconStyle(style);
        assertEquals(style, p1.getIconStyle());
    }

    @Test
    public void testIconURISuccess() throws Exception {
        final String sampleURL = "127.0.0.1";
        p1.setIconURI(sampleURL);
        assertEquals(p1.getIconURI(), sampleURL);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testIconURIInvalidURL() {
        when(URLUtil.isValidUrl(any(String.class))).thenReturn(false);
        p1.setIconURI("xyz");
    }


    @Test(expected = IllegalArgumentException.class)
    public void testIconScaleNegavie() {
        p1.setIconScale(-5);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testIconScaleZero() {
        p1.setIconScale(0);
    }

    @Test
    public void testSetIconScaleSuccess() {
        final int scale = 5;
        p1.setIconScale(scale);
        assertEquals(p1.getIconScale(), scale, Epsilon);
    }

    @Test
    public void testResourceId() {
        final int resourceID = 5;
        p1.setResourceId(resourceID);
        assertEquals(p1.getResourceId(), resourceID, Epsilon);
    }
}