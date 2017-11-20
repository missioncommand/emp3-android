package mil.emp3.api;

import org.cmapi.primitives.GeoFillStyle;
import org.cmapi.primitives.GeoIconStyle;
import org.cmapi.primitives.GeoLabelStyle;
import org.cmapi.primitives.GeoPosition;
import org.cmapi.primitives.GeoStrokeStyle;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Random;

import mil.emp3.api.exceptions.EMP_Exception;
import mil.emp3.api.interfaces.IEmpExportToStringCallback;
import mil.emp3.api.interfaces.IMap;
import mil.emp3.api.interfaces.IOverlay;
import mil.emp3.api.utils.ComparisonUtils;
import mil.emp3.api.utils.kml.EmpKMLExporter;

/**
 * Created by matt.miller@rgi-corp.local on 11/15/17.
 */

public class KMLExportTest extends TestBaseSingleMap{
    private final static String TAG = KMLExportTest.class.getName();

    @Before
    public void setup() throws Exception{
        super.init();
        super.setupSingleMap(TAG);
    }

    @Test
    public void exportPoint() throws Exception{
        final Overlay overlay = (Overlay) addOverlayToMap(this.remoteMap);
        final Point feature = addPoint(overlay);
        final boolean[] resultFound = new boolean[1];
        resultFound[0] = false;


        EmpKMLExporter.exportToString(this.remoteMap, feature, true, new IEmpExportToStringCallback() {

            @Override
            public void exportSuccess(final String kmlString) {
                try {
                    overlay.clearContainer();
                    final InputStream stream = new ByteArrayInputStream(kmlString.getBytes() );
                    final KML kmlFeature = new KML(stream);
                    overlay.addFeature(kmlFeature, true);
                    final KML kmlOnMap = (KML) overlay.getFeatures().get(0);
                    final Point importedPoint = (Point) kmlOnMap.getFeatureList().get(0);
                    try {
                        ComparisonUtils.comparePoint(importedPoint, feature);
                        resultFound[0] = true;
                        return;
                    } catch (final Exception ex) {
                        System.err.println(importedPoint + " is not equal to " + feature);
                    }
                    resultFound[0] = true;
                    Assert.fail();
                } catch (final Exception e) {
                    resultFound[0] = true;
                    Assert.fail();
                }
                resultFound[0] = true;
            }

            @Override
            public void exportFailed(Exception Ex) {
                resultFound[0] = true;
                Assert.fail();
            }
        });
        while(!resultFound[0]){
            if(resultFound[0]) {
                return;
            }
        }
    }

    @Test
    public void exportPolygon() throws Exception{
        final Overlay overlay = (Overlay) addOverlayToMap(this.remoteMap);
        final Polygon feature = addPolygon(overlay);
        final boolean[] resultFound = new boolean[1];
        resultFound[0] = false;


        EmpKMLExporter.exportToString(this.remoteMap, feature, true, new IEmpExportToStringCallback() {

            @Override
            public void exportSuccess(final String kmlString) {
                try {
                    overlay.clearContainer();
                    final InputStream stream = new ByteArrayInputStream(kmlString.getBytes() );
                    final KML kmlFeature = new KML(stream);
                    overlay.addFeature(kmlFeature, true);
                    final KML kmlOnMap = (KML) overlay.getFeatures().get(0);
                    final Polygon importedPoint = (Polygon) kmlOnMap.getFeatureList().get(0);
                    try {
                        ComparisonUtils.comparePolygon(importedPoint, feature);
                        resultFound[0] = true;
                        return;
                    } catch (final Exception ex) {
                        System.err.println(importedPoint + " is not equal to " + feature);
                    }
                    resultFound[0] = true;
                    Assert.fail();
                } catch (final Exception e) {
                    resultFound[0] = true;
                    Assert.fail();
                }
                resultFound[0] = true;
                Assert.fail();
            }

            @Override
            public void exportFailed(Exception Ex) {
                resultFound[0] = true;
                Assert.fail();
            }
        });
        while(!resultFound[0]){}
    }

    @Test
    public void exportCircle() throws Exception{
        final Overlay overlay = (Overlay) addOverlayToMap(this.remoteMap);
        final Circle feature = addCircle(overlay);
        final boolean[] resultFound = new boolean[1];
        resultFound[0] = false;


        EmpKMLExporter.exportToString(this.remoteMap, feature, true, new IEmpExportToStringCallback() {

            @Override
            public void exportSuccess(final String kmlString) {
                try {
                    overlay.clearContainer();
                    final InputStream stream = new ByteArrayInputStream(kmlString.getBytes() );
                    final KML kmlFeature = new KML(stream);
                    overlay.addFeature(kmlFeature, true);
                    final KML kmlOnMap = (KML) overlay.getFeatures().get(0);
                    final Circle importedCircle = (Circle) kmlOnMap.getFeatureList().get(0);
                    try {
                        ComparisonUtils.compareCircle(importedCircle, feature);
                        resultFound[0] = true;
                        return;
                    } catch (final Exception ex) {
                        System.err.println(importedCircle + " is not equal to " + feature);
                    }
                    resultFound[0] = true;
                    Assert.fail();
                } catch (final Exception e) {
                    resultFound[0] = true;
                    Assert.fail();
                }
                resultFound[0] = true;
                Assert.fail();
            }

            @Override
            public void exportFailed(Exception Ex) {
                resultFound[0] = true;
                Assert.fail();
            }
        });
        while(!resultFound[0]){}
    }

    private static IOverlay addOverlayToMap(final IMap map) throws EMP_Exception
    {
        final Overlay overlay = new Overlay();
        //add overlay to map
        overlay.setName("Test Overlay");
        map.addOverlay(overlay, true);
        return overlay;
    }

    private static Point addPoint(final IOverlay overlay) throws EMP_Exception
    {
        final Point oPoint = getRandomPoint();
        oPoint.setName("Test Point");
        //oPoint.setIconScale(10);
        oPoint.setIconURI("https://127.0.0.1");
        oPoint.setIconStyle(new GeoIconStyle());
        //oPoint.setAzimuth(50);
        oPoint.setExtrude(true);
        oPoint.setDescription("This is a test point");
        oPoint.setFillStyle(new GeoFillStyle());
        oPoint.setLabelStyle(new GeoLabelStyle());
        oPoint.setStrokeStyle(new GeoStrokeStyle());
        overlay.addFeature(oPoint, true);
        return oPoint;
    }

    private static Circle addCircle(final IOverlay overlay) throws EMP_Exception
    {
        final Circle oCircle = getRandomCircle();
        oCircle.setName("Test Circle");
        //oPoint.setIconScale(10);
        oCircle.setRadius(15.5);
        //oPoint.setAzimuth(50);
        oCircle.setExtrude(true);
        oCircle.setDescription("This is a test Circle");
        oCircle.setFillStyle(new GeoFillStyle());
        oCircle.setLabelStyle(new GeoLabelStyle());
        oCircle.setStrokeStyle(new GeoStrokeStyle());
        overlay.addFeature(oCircle, true);
        return oCircle;
    }

    private static Polygon addPolygon(final IOverlay overlay) throws EMP_Exception
    {
        final Polygon oPolygon = getRandomPolygon();
        oPolygon.setName("Test Polygon");//
//        oPolygon.setAzimuth(50);
        oPolygon.setExtrude(true);
        oPolygon.setDescription("This is a test polygon");
//        oPolygon.setFillStyle(new GeoFillStyle());
        oPolygon.setLabelStyle(new GeoLabelStyle());
        oPolygon.setStrokeStyle(new GeoStrokeStyle());
        overlay.addFeature(oPolygon, true);
        return oPolygon;
    }

    private static GeoPosition getRandomLocation() throws EMP_Exception {
        final GeoPosition location = new GeoPosition();
        location.setLatitude(getRandomValueBetween(-90.0,90.0));
        location.setLongitude(getRandomValueBetween(-180.0, 180.0));
        return location;
    }

    private static Point getRandomPoint() throws EMP_Exception
    {
        final Point oPoint = new Point();
        oPoint.setPosition(getRandomLocation());
        return oPoint;
    }

    private static Circle getRandomCircle() throws EMP_Exception
    {
        final Circle oCircle = new Circle();
        oCircle.setPosition(getRandomLocation());
        return oCircle;
    }

    private static Polygon getRandomPolygon() throws EMP_Exception
    {
        final Polygon oPolygon = new Polygon();
        oPolygon.setPosition(getRandomLocation());
        return oPolygon;
    }

    private static double getRandomValueBetween(final double low, final double high)
    {
        return low + (high - low) * new Random().nextDouble();
    }

}
