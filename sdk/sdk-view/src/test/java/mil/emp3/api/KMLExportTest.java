package mil.emp3.api;

import org.cmapi.primitives.GeoFillStyle;
import org.cmapi.primitives.GeoIconStyle;
import org.cmapi.primitives.GeoLabelStyle;
import org.cmapi.primitives.GeoPosition;
import org.cmapi.primitives.GeoStrokeStyle;
import org.cmapi.primitives.IGeoAltitudeMode;
import org.cmapi.primitives.IGeoRenderable;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Random;

import mil.emp3.api.abstracts.Feature;
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
    public void exportPoint() throws Exception
    {
        final Overlay overlay       = (Overlay) addOverlayToMap(this.remoteMap);
        final Point feature         = addPoint(overlay);
        final boolean[] resultFound = {false};
        final String[]  kmlFile     = new String[1];

        EmpKMLExporter.exportToString(this.remoteMap,
                                      feature,
                                     true,
                                      new IEmpExportToStringCallback(){

                                                                          @Override
                                                                          public void exportSuccess(final String kmlString)
                                                                          {
                                                                             System.out.println("Success");
                                                                             kmlFile[0] = kmlString;
                                                                             resultFound[0] = true;
                                                                          }

                                                                          @Override
                                                                          public void exportFailed(Exception Ex)
                                                                          {
                                                                              System.out.println("Failed");
                                                                              resultFound[0] = true;
                                                                              Assert.fail(Ex.getMessage());
                                                                          }
                                                                      });

        while(!resultFound[0])
        {

        }

        System.out.println("Ended");
        Assert.assertTrue(kmlFile[0] != null || kmlFile[0] != "");

        try (final InputStream stream = new ByteArrayInputStream(kmlFile[0].getBytes()))
        {
            System.out.println("I made it!!!");
            overlay.clearContainer();
            final KML kmlFeature = new KML(stream);
            overlay.addFeature(kmlFeature, true);
            final KML kmlOnMap = (KML) overlay.getFeatures().get(0);
            final Point importedPoint = (Point) kmlOnMap.getFeatureList().get(0);
            ComparisonUtils.comparePoint(importedPoint, feature);
        }

    }
//
//    @Test
//    public void exportOverlay() throws Exception{
//        final Overlay overlay = (Overlay) addOverlayToMap(this.remoteMap);
//        final Point feature = addPoint(overlay);
//        final String[] resultFound = new String[1];
//        resultFound[0] = "";
//
//
//        EmpKMLExporter.exportToString(this.remoteMap, overlay, true, new IEmpExportToStringCallback() {
//
//            @Override
//            public void exportSuccess(final String kmlString) {
//                try {
//                    overlay.clearContainer();
//                    final InputStream stream = new ByteArrayInputStream(kmlString.getBytes() );
//                    final KML kmlFeature = new KML(stream);
//                    overlay.addFeature(kmlFeature, true);
//                    final KML kmlOnMap = (KML) overlay.getFeatures().get(0);
//                    final Point importedPoint = (Point) kmlOnMap.getFeatureList().get(0);
//                    ComparisonUtils.comparePoint(importedPoint, feature);
//                    resultFound[0] = SUCCESS;
//                } catch (final Exception Ex) {
//                    resultFound[0] = FAILED;
//                    Assert.fail(Ex.getMessage());
//                }
//            }
//
//            @Override
//            public void exportFailed(Exception Ex) {
//                resultFound[0] = FAILED;
//                Assert.fail(Ex.getMessage());
//            }
//        });
//        while(resultFound[0].equals("")){}
//        assertEquals(resultFound[0], SUCCESS);    }
//
//    @Test
//    public void exportMap() throws Exception{
//        final Overlay overlay = (Overlay) addOverlayToMap(this.remoteMap);
//        final Point feature = addPoint(overlay);
//        final String[] resultFound = new String[1];
//        resultFound[0] = "";
//
//        EmpKMLExporter.exportToString(this.remoteMap, true, new IEmpExportToStringCallback() {
//
//            @Override
//            public void exportSuccess(final String kmlString) {
//                try {
//                    overlay.clearContainer();
//                    final InputStream stream = new ByteArrayInputStream(kmlString.getBytes() );
//                    final KML kmlFeature = new KML(stream);
//                    overlay.addFeature(kmlFeature, true);
//                    final KML kmlOnMap = (KML) overlay.getFeatures().get(0);
//                    final Point importedPoint = (Point) kmlOnMap.getFeatureList().get(0);
//                    ComparisonUtils.comparePoint(importedPoint, feature);
//                    resultFound[0] = SUCCESS;
//                } catch (final Exception Ex) {
//                    resultFound[0] = FAILED;
//                    Assert.fail(Ex.getMessage());
//                }
//            }
//
//            @Override
//            public void exportFailed(Exception Ex) {
//                resultFound[0] = FAILED;
//                Assert.fail(Ex.getMessage());
//            }
//        });
//        while(resultFound[0].equals("")){}
//        assertEquals(resultFound[0], SUCCESS);    }
//
//    @Test
//    public void exportPolygon() throws Exception{
//        final Overlay overlay = (Overlay) addOverlayToMap(this.remoteMap);
//        final Polygon feature = addPolygon(overlay);
//        final String[] resultFound = new String[1];
//        resultFound[0] = "";
//
//        EmpKMLExporter.exportToString(this.remoteMap, feature, true, new IEmpExportToStringCallback() {
//
//            @Override
//            public void exportSuccess(final String kmlString) {
//                try {
//                    overlay.clearContainer();
//                    final InputStream stream = new ByteArrayInputStream(kmlString.getBytes() );
//                    final KML kmlFeature = new KML(stream);
//                    overlay.addFeature(kmlFeature, true);
//                    final KML kmlOnMap = (KML) overlay.getFeatures().get(0);
//                    final Polygon importedPoint = (Polygon) kmlOnMap.getFeatureList().get(0);
//                    ComparisonUtils.comparePolygon(importedPoint, feature);
//                    resultFound[0] = SUCCESS;
//                } catch (final Exception Ex) {
//                    resultFound[0] = FAILED;
//                    Assert.fail(Ex.getMessage());
//                }
//            }
//
//            @Override
//            public void exportFailed(Exception Ex) {
//                resultFound[0] = FAILED;
//                Assert.fail(Ex.getMessage());
//            }
//        });
//        while(resultFound[0].equals("")){}
//        assertEquals(resultFound[0], SUCCESS);    }
//
//    @Test
//    public void exportCircle() throws Exception{
//        final Overlay overlay = (Overlay) addOverlayToMap(this.remoteMap);
//        final Circle feature = addCircle(overlay);
//        final String[] resultFound = new String[1];
//        resultFound[0] = "";
//
//        EmpKMLExporter.exportToString(this.remoteMap, feature, true, new IEmpExportToStringCallback() {
//
//            @Override
//            public void exportSuccess(final String kmlString) {
//                try {
//                    overlay.clearContainer();
//                    final InputStream stream = new ByteArrayInputStream(kmlString.getBytes() );
//                    final KML kmlFeature = new KML(stream);
//                    overlay.addFeature(kmlFeature, true);
//                    final KML kmlOnMap = (KML) overlay.getFeatures().get(0);
//                    final Circle importedCircle = (Circle) kmlOnMap.getFeatureList().get(0);
//                    ComparisonUtils.compareCircle(importedCircle, feature);
//                    resultFound[0] = SUCCESS;
//                } catch (final Exception Ex) {
//                    resultFound[0] = FAILED;
//                    Assert.fail(Ex.getMessage());
//                }
//            }
//
//            @Override
//            public void exportFailed(Exception Ex) {
//                resultFound[0] = FAILED;
//                Assert.fail(Ex.getMessage());
//            }
//        });
//        while(resultFound[0].equals("")){}
//        assertEquals(resultFound[0], SUCCESS);    }
//
//    @Test
//    public void exportEllipse() throws Exception{
//        final Overlay overlay = (Overlay) addOverlayToMap(this.remoteMap);
//        final Ellipse feature = addEllipse(overlay);
//        final String[] resultFound = new String[1];
//        resultFound[0] = "";
//
//        EmpKMLExporter.exportToString(this.remoteMap, feature, true, new IEmpExportToStringCallback() {
//
//            @Override
//            public void exportSuccess(final String kmlString) {
//                try {
//                    overlay.clearContainer();
//                    final InputStream stream = new ByteArrayInputStream(kmlString.getBytes() );
//                    final KML kmlFeature = new KML(stream);
//                    overlay.addFeature(kmlFeature, true);
//                    final KML kmlOnMap = (KML) overlay.getFeatures().get(0);
//                    final Ellipse importedEllipse = (Ellipse) kmlOnMap.getFeatureList().get(0);
//                    ComparisonUtils.compareEllipse(importedEllipse, feature);
//                    resultFound[0] = SUCCESS;
//                } catch (final Exception Ex) {
//                    resultFound[0] = FAILED;
//                    Assert.fail(Ex.getMessage());
//                }
//            }
//
//            @Override
//            public void exportFailed(Exception Ex) {
//                resultFound[0] = FAILED;
//                Assert.fail(Ex.getMessage());
//            }
//        });
//        while(resultFound[0].equals("")){}
//        assertEquals(resultFound[0], SUCCESS);    }
//
//    @Test
//    public void exportSquare() throws Exception{
//        final Overlay overlay = (Overlay) addOverlayToMap(this.remoteMap);
//        final Square feature = addSquare(overlay);
//        final String[] resultFound = new String[1];
//        resultFound[0] = "";
//
//        EmpKMLExporter.exportToString(this.remoteMap, feature, true, new IEmpExportToStringCallback() {
//
//            @Override
//            public void exportSuccess(final String kmlString) {
//                try {
//                    overlay.clearContainer();
//                    final InputStream stream = new ByteArrayInputStream(kmlString.getBytes() );
//                    final KML kmlFeature = new KML(stream);
//                    overlay.addFeature(kmlFeature, true);
//                    final KML kmlOnMap = (KML) overlay.getFeatures().get(0);
//                    final Square importedSquare = (Square) kmlOnMap.getFeatureList().get(0);
//                    ComparisonUtils.compareSquare(importedSquare, feature);
//                    resultFound[0] = SUCCESS;
//                } catch (final Exception Ex) {
//                    resultFound[0] = FAILED;
//                }
//            }
//
//            @Override
//            public void exportFailed(Exception Ex) {
//                resultFound[0] = FAILED;
//                Assert.fail(Ex.getMessage());
//            }
//        });
//        while(resultFound[0].equals("")){}
//        assertEquals(resultFound[0], SUCCESS);    }
//
//    @Test
//    public void exportRectangle() throws Exception{
//        final Overlay overlay = (Overlay) addOverlayToMap(this.remoteMap);
//        final Rectangle feature = addRectangle(overlay);
//        final String[] resultFound = new String[1];
//        resultFound[0] = "";
//
//        EmpKMLExporter.exportToString(this.remoteMap, feature, true, new IEmpExportToStringCallback() {
//
//            @Override
//            public void exportSuccess(final String kmlString) {
//                try {
//                    overlay.clearContainer();
//                    final InputStream stream = new ByteArrayInputStream(kmlString.getBytes() );
//                    final KML kmlFeature = new KML(stream);
//                    overlay.addFeature(kmlFeature, true);
//                    final KML kmlOnMap = (KML) overlay.getFeatures().get(0);
//                    final Rectangle importedRectangle = (Rectangle) kmlOnMap.getFeatureList().get(0);
//                    ComparisonUtils.compareRectangle(importedRectangle, feature);
//                    resultFound[0] = SUCCESS;
//                } catch (final Exception Ex) {
//                    resultFound[0] = FAILED;
//                    Assert.fail(Ex.getMessage());
//                }
//            }
//
//            @Override
//            public void exportFailed(Exception Ex) {
//                resultFound[0] = FAILED;
//                Assert.fail(Ex.getMessage());
//            }
//        });
//        while(resultFound[0].equals("")){}
//        assertEquals(resultFound[0], SUCCESS);    }
//
//    @Test
//    public void exportText() throws Exception{
//        final Overlay overlay = (Overlay) addOverlayToMap(this.remoteMap);
//        final Text feature = addText(overlay);
//        final String[] resultFound = new String[1];
//        resultFound[0] = "";
//
//        EmpKMLExporter.exportToString(this.remoteMap, feature, true, new IEmpExportToStringCallback() {
//
//            @Override
//            public void exportSuccess(final String kmlString) {
//                try {
//                    overlay.clearContainer();
//                    final InputStream stream = new ByteArrayInputStream(kmlString.getBytes() );
//                    final KML kmlFeature = new KML(stream);
//                    overlay.addFeature(kmlFeature, true);
//                    final KML kmlOnMap = (KML) overlay.getFeatures().get(0);
//                    final Text importedText = (Text) kmlOnMap.getFeatureList().get(0);
//                    ComparisonUtils.compareText(importedText, feature);
//                    resultFound[0] = SUCCESS;
//                } catch (final Exception Ex) {
//                    resultFound[0] = FAILED;
//                    Assert.fail(Ex.getMessage());
//                }
//            }
//
//            @Override
//            public void exportFailed(Exception Ex) {
//                resultFound[0] = FAILED;
//                Assert.fail(Ex.getMessage());
//            }
//        });
//        while(resultFound[0].equals("")){}
//        assertEquals(resultFound[0], SUCCESS);    }
//
//    @Test
//    public void exportPath() throws Exception{
//        final Overlay overlay = (Overlay) addOverlayToMap(this.remoteMap);
//        final Path feature = addPath(overlay);
//        final String[] resultFound = new String[1];
//        resultFound[0] = "";
//
//        EmpKMLExporter.exportToString(this.remoteMap, feature, true, new IEmpExportToStringCallback() {
//
//            @Override
//            public void exportSuccess(final String kmlString) {
//                try {
//                    overlay.clearContainer();
//                    final InputStream stream = new ByteArrayInputStream(kmlString.getBytes() );
//                    final KML kmlFeature = new KML(stream);
//                    overlay.addFeature(kmlFeature, true);
//                    final KML kmlOnMap = (KML) overlay.getFeatures().get(0);
//                    final Path importedPath = (Path) kmlOnMap.getFeatureList().get(0);
//                    ComparisonUtils.comparePath(importedPath, feature);
//                    resultFound[0] = SUCCESS;
//                } catch (final Exception Ex) {
//                    resultFound[0] = FAILED;
//                    Assert.fail(Ex.getMessage());
//                }
//            }
//
//            @Override
//            public void exportFailed(Exception Ex) {
//                resultFound[0] = FAILED;
//                Assert.fail(Ex.getMessage());
//            }
//        });
//        while(resultFound[0].equals("")){}
//        assertEquals(resultFound[0], SUCCESS);
// }
//

    private static IOverlay addOverlayToMap(final IMap map) throws EMP_Exception
    {
        final Overlay overlay = new Overlay();
        overlay.setName("Test Overlay");
        map.addOverlay(overlay, true);
        return overlay;
    }

    private static Point addPoint(final IOverlay overlay) throws Exception
    {
        final Point oPoint = new Point();
        oPoint.setIconScale(10);
        oPoint.setIconURI("https://127.0.0.1");
        oPoint.setIconStyle(new GeoIconStyle());
        addFeatureStyles(oPoint);
        overlay.addFeature(oPoint, true);
        return oPoint;
    }

    private static Text addText(final IOverlay overlay) throws Exception
    {
        final Text oText = new Text();
        oText.setRotationAngle(40);
        oText.setText("Test String");
        addFeatureStyles(oText);
        overlay.addFeature(oText, true);
        return oText;
    }

    private static Circle addCircle(final IOverlay overlay) throws Exception
    {
        final Circle oCircle = new Circle();
        oCircle.setName("Test Circle");
        oCircle.setRadius(15.5);
        addFeatureStyles(oCircle);
        overlay.addFeature(oCircle, true);
        return oCircle;
    }

    private static Ellipse addEllipse(final IOverlay overlay) throws Exception
    {
        final Ellipse oEllipse = new Ellipse();
        oEllipse.setSemiMinor(15.5);
        oEllipse.setSemiMajor(14.5);
        addFeatureStyles(oEllipse);
        overlay.addFeature(oEllipse, true);
        return oEllipse;
    }

    private static Square addSquare(final IOverlay overlay) throws Exception
    {
        final Square oSquare = new Square();
        oSquare.setWidth(15.5);
        addFeatureStyles(oSquare);
        overlay.addFeature(oSquare, true);
        return oSquare;
    }

    private static Rectangle addRectangle(final IOverlay overlay) throws Exception
    {
        final Rectangle oRectangle = new Rectangle();
        oRectangle.setWidth(15.5);
        oRectangle.setHeight(14.5);
        addFeatureStyles(oRectangle);
        overlay.addFeature(oRectangle, true);
        return oRectangle;
    }

    private static Polygon addPolygon(final IOverlay overlay) throws Exception
    {
        final Polygon oPolygon = new Polygon();
        addFeatureStyles(oPolygon);
        overlay.addFeature(oPolygon, true);
        return oPolygon;
    }

    private static Path addPath(final IOverlay overlay) throws Exception
    {
        final Path oPath = new Path();
        addFeatureStyles(oPath);
        overlay.addFeature(oPath, true);
        return oPath;
    }

    private static GeoPosition getRandomLocation() throws EMP_Exception {
        final GeoPosition location = new GeoPosition();
        location.setLatitude(getRandomValueBetween(-90.0,90.0));
        location.setLongitude(getRandomValueBetween(-180.0, 180.0));
        return location;
    }

    private static double getRandomValueBetween(final double low, final double high)
    {
        return low + (high - low) * new Random().nextDouble();
    }

    private static void addFeatureStyles(final Feature feature) throws Exception{
        feature.setPosition(getRandomLocation());
        feature.setName("Test Feature");
        //feature.setAzimuth(50);
        feature.setExtrude(true);
        feature.setDescription("This is a test feature");
        feature.setFillStyle(new GeoFillStyle());
        feature.setLabelStyle(new GeoLabelStyle());
        feature.setStrokeStyle(new GeoStrokeStyle());
        feature.setAltitudeMode(IGeoAltitudeMode.AltitudeMode.RELATIVE_TO_GROUND);
        feature.setBuffer(25.0);
        feature.setPathType(IGeoRenderable.PathType.LINEAR);
        feature.setTessellate(false);
        feature.setReadOnly(false);
    }

}
