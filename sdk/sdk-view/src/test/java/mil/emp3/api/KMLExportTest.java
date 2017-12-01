package mil.emp3.api;

import org.cmapi.primitives.GeoFillStyle;
import org.cmapi.primitives.GeoIconStyle;
import org.cmapi.primitives.GeoLabelStyle;
import org.cmapi.primitives.GeoPosition;
import org.cmapi.primitives.GeoStrokeStyle;
import org.cmapi.primitives.IGeoAltitudeMode;
import org.cmapi.primitives.IGeoPosition;
import org.cmapi.primitives.IGeoRenderable;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import mil.emp3.api.abstracts.Feature;
import mil.emp3.api.exceptions.EMP_Exception;
import mil.emp3.api.interfaces.IEmpExportToStringCallback;
import mil.emp3.api.interfaces.IMap;
import mil.emp3.api.interfaces.IOverlay;
import mil.emp3.api.utils.ComparisonUtils;
import mil.emp3.api.utils.kml.EmpKMLExporter;

import static org.junit.Assert.assertEquals;

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
        this.remoteMap.clearContainer();
        final Overlay overlay       = (Overlay) addOverlayToMap(this.remoteMap);
        final Point feature         = addPoint(overlay);
        final boolean[] resultFound = {false};
        final String[]  kmlReturn   = new String[1];

        EmpKMLExporter.exportToString(this.remoteMap,
                                      feature,
                                      false,
                                      new IEmpExportToStringCallback()
                                      {
                                        @Override
                                        public void exportSuccess(final String kmlString)
                                        {
                                            kmlReturn[0] = kmlString;
                                            resultFound[0] = true;
                                        }

                                        @Override
                                        public void exportFailed(Exception Ex)
                                        {
                                            resultFound[0] = true;
                                            Assert.fail(Ex.getMessage());
                                        }
                                      });

        while(!resultFound[0]) {
            try {
                Thread.sleep(50);
            } catch (InterruptedException ie) {

            }
        }

        Assert.assertTrue(kmlReturn[0] != null);

        try (final InputStream stream = new ByteArrayInputStream(kmlReturn[0].getBytes()))
        {
            overlay.clearContainer();
            final KML kmlFeature = new KML(stream);
            overlay.addFeature(kmlFeature, true);
            final KML kmlOnMap = (KML) overlay.getFeatures().get(0);
            final Point importedPoint = (Point) kmlOnMap.getFeatureList().get(0);
            ComparisonUtils.comparePoint(importedPoint, feature);
        }
    }

    @Test
    public void exportOverlay() throws Exception {
        this.remoteMap.clearContainer();
        final Overlay overlay       = (Overlay) addOverlayToMap(this.remoteMap);
        final Point feature         = addPoint(overlay);
        final boolean[] resultFound = {false};
        final String[]  kmlReturn   = new String[1];

        EmpKMLExporter.exportToString(this.remoteMap,
                                      overlay,
                                      true,
                                      new IEmpExportToStringCallback()
                                      {
                                            @Override
                                            public void exportSuccess(final String kmlString)
                                            {
                                                kmlReturn[0] = kmlString;
                                                resultFound[0] = true;
                                            }

                                            @Override
                                            public void exportFailed(Exception Ex)
                                            {
                                                resultFound[0] = true;
                                                Assert.fail(Ex.getMessage());
                                            }
                                      });
        while(!resultFound[0]) {
            try {
                Thread.sleep(50);
            } catch (InterruptedException ie) {

            }
        }

        Assert.assertTrue(kmlReturn[0] != null);

        try (final InputStream stream = new ByteArrayInputStream(kmlReturn[0].getBytes())) {
            overlay.clearContainer();
            final KML kmlFeature = new KML(stream);
            overlay.addFeature(kmlFeature, true);
            final KML kmlOnMap = (KML) overlay.getFeatures().get(0);
            final Point importedPoint = (Point) kmlOnMap.getFeatureList().get(0);
            ComparisonUtils.comparePoint(importedPoint, feature);
        }
    }

    @Test
    public void exportMap() throws Exception {
        this.remoteMap.clearContainer();
        final Overlay overlay         = (Overlay) addOverlayToMap(this.remoteMap);
        final Point feature           = addPoint(overlay);
        final boolean[] resultFound   = {false};
        final String[]  kmlReturn     = new String[1];

        EmpKMLExporter.exportToString(this.remoteMap,
                                      true,
                                      new IEmpExportToStringCallback()
                                      {
                                        @Override
                                        public void exportSuccess(final String kmlString)
                                        {
                                            kmlReturn[0] = kmlString;
                                            resultFound[0] = true;
                                        }

                                        @Override
                                        public void exportFailed(Exception Ex)
                                        {
                                            resultFound[0] = true;
                                            Assert.fail(Ex.getMessage());
                                        }
                                      });
        while(!resultFound[0]) {
            try {
                Thread.sleep(50);
            } catch (InterruptedException ie) {

            }
        }

        Assert.assertTrue(kmlReturn[0] != null);

        try (final InputStream stream = new ByteArrayInputStream(kmlReturn[0].getBytes())) {
            overlay.clearContainer();
            final KML kmlFeature = new KML(stream);
            overlay.addFeature(kmlFeature, true);
            final KML kmlOnMap = (KML) overlay.getFeatures().get(0);
            final Point importedPoint = (Point) kmlOnMap.getFeatureList().get(0);
            ComparisonUtils.comparePoint(importedPoint, feature);
        }
    }

    @Test
    public void exportPolygon() throws Exception
    {
        this.remoteMap.clearContainer();
        final Overlay overlay       = (Overlay) addOverlayToMap(this.remoteMap);
        final Polygon feature       = addPolygon(overlay);
        final boolean[] resultFound = {false};
        final String[]  kmlReturn   = new String[1];

        EmpKMLExporter.exportToString(this.remoteMap,
                                      feature,
                                      true,
                                      new IEmpExportToStringCallback()
                                      {
                                        @Override
                                        public void exportSuccess(final String kmlString)
                                        {
                                            kmlReturn[0] = kmlString;
                                            resultFound[0] = true;
                                        }

                                        @Override
                                        public void exportFailed(Exception Ex)
                                        {
                                            resultFound[0] = true;
                                            Assert.fail(Ex.getMessage());
                                        }
                                      });

        while(!resultFound[0]) {
            try {
                Thread.sleep(50);
            } catch (InterruptedException ie) {

            }
        }

        Assert.assertTrue(kmlReturn[0] != null);

        try (final InputStream stream = new ByteArrayInputStream(kmlReturn[0].getBytes()))
        {
            overlay.clearContainer();
            final KML kmlFeature = new KML(stream);
            overlay.addFeature(kmlFeature, true);
            final KML kmlOnMap = (KML) overlay.getFeatures().get(0);
            final Polygon importedPolygon = (Polygon) kmlOnMap.getFeatureList().get(0);
            ComparisonUtils.comparePolygon(importedPolygon, feature);
        }

        this.remoteMap.clearContainer();
    }

    @Test
    public void exportCircle() throws Exception
    {
        this.remoteMap.clearContainer();
        final Overlay overlay       = (Overlay) addOverlayToMap(this.remoteMap);
        final Circle feature        = addCircle(overlay);
        final boolean[] resultFound = {false};
        final String[]  kmlReturn   = new String[1];

        EmpKMLExporter.exportToString(this.remoteMap,
                                      feature,
                                      true,
                                      new IEmpExportToStringCallback()
                                      {
                                        @Override
                                        public void exportSuccess(final String kmlString)
                                        {
                                            kmlReturn[0] = kmlString;
                                            resultFound[0] = true;
                                        }

                                        @Override
                                        public void exportFailed(Exception Ex)
                                        {
                                            resultFound[0] = true;
                                            Assert.fail(Ex.getMessage());
                                        }
                                      });

        while(!resultFound[0]) {
            try {
                Thread.sleep(50);
            } catch (InterruptedException ie) {

            }
        }
        Assert.assertTrue(kmlReturn[0] != null);

        try (final InputStream stream = new ByteArrayInputStream(kmlReturn[0].getBytes()))
        {
            overlay.clearContainer();
            final KML kmlFeature = new KML(stream);
            overlay.addFeature(kmlFeature, true);
            final KML kmlOnMap = (KML) overlay.getFeatures().get(0);
            //Circles do not exist in KML so they are represented by paths
            final Polygon importedCircle = (Polygon) kmlOnMap.getFeatureList().get(0);
            ComparisonUtils.compareFeatureToPolygon(feature, importedCircle);
        }
    }

    @Test
    public void exportEllipse() throws Exception
    {
        this.remoteMap.clearContainer();
        final Overlay overlay       = (Overlay) addOverlayToMap(this.remoteMap);
        final Ellipse feature       = addEllipse(overlay);
        final boolean[] resultFound = {false};
        final String[]  kmlReturn   = new String[1];

        EmpKMLExporter.exportToString(this.remoteMap,
                                      feature,
                                      true,
                                      new IEmpExportToStringCallback()
                                      {
                                        @Override
                                        public void exportSuccess(final String kmlString)
                                        {
                                            kmlReturn[0] = kmlString;
                                            resultFound[0] = true;
                                        }

                                        @Override
                                        public void exportFailed(Exception Ex)
                                        {
                                            resultFound[0] = true;
                                            Assert.fail(Ex.getMessage());
                                        }
                                      });

        while(!resultFound[0]) {
            try {
                Thread.sleep(50);
            } catch (InterruptedException ie) {

            }
        }

        Assert.assertTrue(kmlReturn[0] != null);

        try (final InputStream stream = new ByteArrayInputStream(kmlReturn[0].getBytes()))
        {
            overlay.clearContainer();
            final KML kmlFeature = new KML(stream);
            overlay.addFeature(kmlFeature, true);
            final KML kmlOnMap = (KML) overlay.getFeatures().get(0);
            //KML does not support Ellipses so they get represented by a polygon
            final Path importedEllipse = (Path) kmlOnMap.getFeatureList().get(0);
            ComparisonUtils.compareFeatureToPath(feature, importedEllipse);
        }
    }

    @Test
    public void exportSquare() throws Exception
    {
        this.remoteMap.clearContainer();
        final Overlay overlay       = (Overlay) addOverlayToMap(this.remoteMap);
        final Square feature        = addSquare(overlay);
        final boolean[] resultFound = {false};
        final String[]  kmlReturn   = new String[1];

        EmpKMLExporter.exportToString(this.remoteMap,
                                      feature,
                                      true,
                                      new IEmpExportToStringCallback()
                                      {
                                        @Override
                                        public void exportSuccess(final String kmlString)
                                        {
                                            kmlReturn[0] = kmlString;
                                            resultFound[0] = true;
                                        }

                                        @Override
                                        public void exportFailed(Exception Ex)
                                        {
                                            resultFound[0] = true;
                                            Assert.fail(Ex.getMessage());
                                        }
                                      });

        while(!resultFound[0]) {
            try {
                Thread.sleep(50);
            } catch (InterruptedException ie) {

            }
        }

        Assert.assertTrue(kmlReturn[0] != null);

        try (final InputStream stream = new ByteArrayInputStream(kmlReturn[0].getBytes()))
        {
            overlay.clearContainer();
            final KML kmlFeature = new KML(stream);
            overlay.addFeature(kmlFeature, true);
            final KML kmlOnMap = (KML) overlay.getFeatures().get(0);
            //KML doesn't support squares so it gets represented by a polygon
            final Polygon importedSquare = (Polygon) kmlOnMap.getFeatureList().get(0);
            ComparisonUtils.compareFeatureToPolygon(feature, importedSquare);

        }
    }

    @Test
    public void exportRectangle() throws Exception
    {
        this.remoteMap.clearContainer();
        final Overlay overlay       = (Overlay) addOverlayToMap(this.remoteMap);
        final Rectangle feature     = addRectangle(overlay);
        final boolean[] resultFound = {false};
        final String[]  kmlReturn   = new String[1];

        EmpKMLExporter.exportToString(this.remoteMap,
                                      feature,
                                      true,
                                      new IEmpExportToStringCallback()
                                      {
                                        @Override
                                        public void exportSuccess(final String kmlString)
                                        {
                                            kmlReturn[0] = kmlString;
                                            resultFound[0] = true;
                                        }

                                        @Override
                                        public void exportFailed(Exception Ex)
                                        {
                                            resultFound[0] = true;
                                            Assert.fail(Ex.getMessage());
                                        }
                                      });

        while(!resultFound[0]) {
            try {
                Thread.sleep(50);
            } catch (InterruptedException ie) {

            }
        }

        Assert.assertTrue(kmlReturn[0] != null);

        try (final InputStream stream = new ByteArrayInputStream(kmlReturn[0].getBytes()))
        {
            overlay.clearContainer();
            final KML kmlFeature = new KML(stream);
            overlay.addFeature(kmlFeature, true);
            final KML kmlOnMap = (KML) overlay.getFeatures().get(0);
            //KML does not support rectangles so they get represented by a polygon
            final Path importedRectangle = (Path) kmlOnMap.getFeatureList().get(0);
            ComparisonUtils.compareFeatureToPath(feature, importedRectangle);
        }
    }

    @Test
    public void exportText() throws Exception
    {
        this.remoteMap.clearContainer();
        final Overlay overlay       = (Overlay) addOverlayToMap(this.remoteMap);
        final Text feature          = addText(overlay);
        final boolean[] resultFound = {false};
        final String[]  kmlReturn   = new String[1];

        EmpKMLExporter.exportToString(this.remoteMap,
                                      feature,
                                      true,
                                      new IEmpExportToStringCallback()
                                      {
                                        @Override
                                        public void exportSuccess(final String kmlString)
                                        {
                                            kmlReturn[0] = kmlString;
                                            resultFound[0] = true;
                                        }

                                        @Override
                                        public void exportFailed(Exception Ex)
                                        {
                                            resultFound[0] = true;
                                            Assert.fail(Ex.getMessage());
                                        }
                                      });

        while(!resultFound[0]) {
            try {
                Thread.sleep(50);
            } catch (InterruptedException ie) {

            }
        }

        Assert.assertTrue(kmlReturn[0] != null);

        try (final InputStream stream = new ByteArrayInputStream(kmlReturn[0].getBytes()))
        {
            overlay.clearContainer();
            final KML kmlFeature = new KML(stream);
            overlay.addFeature(kmlFeature, true);
            final KML kmlOnMap = (KML) overlay.getFeatures().get(0);
            final Point importedText = (Point) kmlOnMap.getFeatureList().get(0);
            ComparisonUtils.compareTextToPoint(feature, importedText);
            assertEquals(feature.getGeoId().toString(), importedText.getDataProviderId());
        }
    }

    @Test
    public void exportPath() throws Exception
    {
        this.remoteMap.clearContainer();
        final Overlay overlay       = (Overlay) addOverlayToMap(this.remoteMap);
        final Path feature          = addPath(overlay);
        final boolean[] resultFound = {false};
        final String[]  kmlReturn   = new String[1];

        EmpKMLExporter.exportToString(this.remoteMap,
                                      feature,
                                      true,
                                      new IEmpExportToStringCallback()
                                      {
                                        @Override
                                        public void exportSuccess(final String kmlString)
                                        {
                                            kmlReturn[0] = kmlString;
                                            resultFound[0] = true;
                                        }

                                        @Override
                                        public void exportFailed(Exception Ex)
                                        {
                                            resultFound[0] = true;
                                            Assert.fail(Ex.getMessage());
                                        }
                                      });

        while(!resultFound[0]) {
            try {
                Thread.sleep(50);
            } catch (InterruptedException ie) {

            }
        }

        Assert.assertTrue(kmlReturn[0] != null);

        try (final InputStream stream = new ByteArrayInputStream(kmlReturn[0].getBytes()))
        {
            overlay.clearContainer();
            final KML kmlFeature = new KML(stream);
            overlay.addFeature(kmlFeature, true);
            final KML kmlOnMap = (KML) overlay.getFeatures().get(0);
            final Path importedPath = (Path) kmlOnMap.getFeatureList().get(0);
            ComparisonUtils.comparePath(importedPath, feature);
            assertEquals(feature.getGeoId().toString(), importedPath.getDataProviderId());
        }
    }

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
        addMPFeatureStyles(oPolygon);
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
        feature.setAltitudeMode(IGeoAltitudeMode.AltitudeMode.CLAMP_TO_GROUND);
        feature.setBuffer(25.0);
        feature.setPathType(IGeoRenderable.PathType.LINEAR);
        feature.setTessellate(false);
        feature.setReadOnly(false);
    }

    private static void addMPFeatureStyles(final Feature feature) throws Exception{
        feature.getPositions().clear();
        for (int i = 0; i < 6; i++) {
            feature.getPositions().add(getRandomLocation());
        }
        feature.setName("Test Feature");
        //feature.setAzimuth(50);
        feature.setExtrude(true);
        feature.setDescription("This is a test feature");
        feature.setFillStyle(new GeoFillStyle());
        feature.setLabelStyle(new GeoLabelStyle());
        feature.setStrokeStyle(new GeoStrokeStyle());
        feature.setAltitudeMode(IGeoAltitudeMode.AltitudeMode.CLAMP_TO_GROUND);
        feature.setBuffer(25.0);
        feature.setPathType(IGeoRenderable.PathType.LINEAR);
        feature.setTessellate(false);
        feature.setReadOnly(false);
    }


}
