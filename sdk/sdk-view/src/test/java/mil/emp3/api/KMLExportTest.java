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

import java.util.Random;

import mil.emp3.api.abstracts.Feature;
import mil.emp3.api.exceptions.EMP_Exception;
import mil.emp3.api.interfaces.IEmpExportToStringCallback;
import mil.emp3.api.interfaces.IMap;
import mil.emp3.api.interfaces.IOverlay;
import mil.emp3.api.utils.kml.EmpKMLExporter;

import static mil.emp3.api.utils.ComparisonUtils.compareCircleToPolygon;
import static mil.emp3.api.utils.ComparisonUtils.compareEllipseToPolygon;
import static mil.emp3.api.utils.ComparisonUtils.comparePath;
import static mil.emp3.api.utils.ComparisonUtils.comparePoint;
import static mil.emp3.api.utils.ComparisonUtils.comparePolygon;
import static mil.emp3.api.utils.ComparisonUtils.compareRectangleToPolygon;
import static mil.emp3.api.utils.ComparisonUtils.compareSquareToPolygon;
import static mil.emp3.api.utils.ComparisonUtils.compareTextToPoint;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Created by matt.miller@rgi-corp.local on 11/15/17.
 */

public class KMLExportTest extends TestBaseSingleMap{
    private final static String TAG = KMLExportTest.class.getName();
    private static int count = 0;

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

        EmpKMLExporter.exportToString(this.remoteMap,
                                      feature,
                                      false,
                                      new IEmpExportToStringCallback()
                                      {
                                        @Override
                                        public void exportSuccess(final String kmlString)
                                        {
                                            try 
                                            {
                                                final KML kmlFeature = new KML(kmlString);
                                                final Point importedPoint = (Point) kmlFeature.getFeatureList().get(0);
                                                comparePoint(importedPoint, feature);
                                                resultFound[0] = true;
                                            } catch (final Exception e){
                                                resultFound[0] = true;
                                                fail(e.getMessage());
                                            }
                                            resultFound[0] = true;
                                        }

                                        @Override
                                        public void exportFailed(Exception ex)
                                        {
                                            resultFound[0] = true;
                                            fail(ex.getMessage());
                                        }
                                      });

        int i = 0;
        while(!resultFound[0] && ++i <= 10) {
            try {
                Thread.sleep(50);
            } catch (InterruptedException ie) {

            }
        }
        if (i >= 10) {
            fail("No result in half second for exportPoint");
        }
    }

    @Test
    public void exportOverlay() throws Exception {
        this.remoteMap.clearContainer();
        final Overlay overlay       = (Overlay) addOverlayToMap(this.remoteMap);
        final Point feature         = addPoint(overlay);
        final boolean[] resultFound = {false};

        EmpKMLExporter.exportToString(this.remoteMap,
                                      overlay,
                                      true,
                                      new IEmpExportToStringCallback()
                                      {
                                          public void exportSuccess(final String kmlString)
                                          {
                                              try 
                                              {
                                                  final KML kmlFeature = new KML(kmlString);
                                                  final Point importedPoint = (Point) kmlFeature.getFeatureList().get(0);
                                                  comparePoint(importedPoint, feature);
                                                  resultFound[0] = true;
                                              } catch (final Exception e){
                                                  resultFound[0] = true;
                                                  fail(e.getMessage());
                                              }
                                          }

                                            @Override
                                            public void exportFailed(Exception ex)
                                            {
                                                resultFound[0] = true;
                                                fail(ex.getMessage());
                                            }
                                      });
        int i = 0;
        while(!resultFound[0] && ++i <= 10) {
            try {
                Thread.sleep(50);
            } catch (InterruptedException ie) {

            }
        }
        if (i >= 10) {
            fail("No result in half second for exportOverlay");
        }
    }

    @Test
    public void exportMap() throws Exception {
        this.remoteMap.clearContainer();
        final Overlay overlay         = (Overlay) addOverlayToMap(this.remoteMap);
        final Point feature           = addPoint(overlay);
        final boolean[] resultFound   = {false};

        EmpKMLExporter.exportToString(this.remoteMap,
                                      true,
                                      new IEmpExportToStringCallback()
                                      {
                                        @Override
                                        public void exportSuccess(final String kmlString)
                                        {
                                            try 
                                            {
                                                final KML kmlFeature = new KML(kmlString);
                                                final Point importedPoint = (Point) kmlFeature.getFeatureList().get(0);
                                                comparePoint(importedPoint, feature);
                                                resultFound[0] = true;
                                            } catch (final Exception e){
                                                resultFound[0] = true;
                                                fail(e.getMessage());
                                            }
                                        }

                                        @Override
                                        public void exportFailed(Exception ex)
                                        {
                                            resultFound[0] = true;
                                            fail(ex.getMessage());
                                        }
                                      });

        int i = 0;
        while(!resultFound[0] && ++i <= 10) {
            try {
                Thread.sleep(50);
            } catch (InterruptedException ie) {

            }
        }
        if (i >= 10) {
            fail("No result in half second for exportMap");
        }
    }

    @Test
    public void exportPolygon() throws Exception
    {
        this.remoteMap.clearContainer();
        final Overlay overlay       = (Overlay) addOverlayToMap(this.remoteMap);
        final Polygon feature       = addPolygon(overlay);
        final boolean[] resultFound = {false};

        EmpKMLExporter.exportToString(this.remoteMap,
                                      feature,
                                      true,
                                      new IEmpExportToStringCallback()
                                      {
                                        @Override
                                        public void exportSuccess(final String kmlString)
                                        {
                                            try 
                                            {
                                                final KML kmlFeature = new KML(kmlString);
                                                final Polygon importedPolygon = (Polygon) kmlFeature.getFeatureList().get(0);
                                                comparePolygon(importedPolygon, feature);
                                                resultFound[0] = true;
                                            } catch (final Exception e){
                                                resultFound[0] = true;
                                                fail(e.getMessage());
                                            }
                                        }

                                        @Override
                                        public void exportFailed(Exception ex)
                                        {
                                            resultFound[0] = true;
                                            fail(ex.getMessage());
                                        }
                                      });

        int i = 0;
        while(!resultFound[0] && ++i <= 10) {
            try {
                Thread.sleep(50);
            } catch (InterruptedException ie) {

            }
        }
        if (i >= 10) {
            fail("No result in half second for exportPolygon");
        }
    }

    @Test
    public void exportCircle() throws Exception
    {
        this.remoteMap.clearContainer();
        final Overlay overlay       = (Overlay) addOverlayToMap(this.remoteMap);
        final Circle feature        = addCircle(overlay);
        final boolean[] resultFound = {false};

        EmpKMLExporter.exportToString(this.remoteMap,
                                      feature,
                                      true,
                                      new IEmpExportToStringCallback()
                                      {
                                        @Override
                                        public void exportSuccess(final String kmlString)
                                        {
                                            try 
                                            {
                                                final KML kmlFeature = new KML(kmlString);
                                                final Polygon parsedPolygon = (Polygon) kmlFeature.getFeatureList().get(0);
                                                compareCircleToPolygon(feature, parsedPolygon);
                                                resultFound[0] = true;
                                            } catch (final Exception e){
                                                resultFound[0] = true;
                                                fail(e.getMessage());

                                            }
                                        }

                                        @Override
                                        public void exportFailed(final Exception ex)
                                        {
                                            resultFound[0] = true;
                                            fail(ex.getMessage());
                                        }
                                      });

        int i = 0;
        while(!resultFound[0] && ++i <= 10) {
            try {
                Thread.sleep(50);
            } catch (InterruptedException ie) {

            }
        }
        if (i >= 10) {
            fail("No result in half second for exportCircle");
        }
    }

    @Test
    public void exportEllipse() throws Exception
    {
        this.remoteMap.clearContainer();
        final Overlay overlay       = (Overlay) addOverlayToMap(this.remoteMap);
        final Ellipse feature       = addEllipse(overlay);
        final boolean[] resultFound = {false};

        EmpKMLExporter.exportToString(this.remoteMap,
                                      feature,
                                      true,
                                      new IEmpExportToStringCallback()
                                      {
                                        @Override
                                        public void exportSuccess(final String kmlString)
                                        {
                                            try 
                                            {
                                                final KML kmlFeature = new KML(kmlString);
                                                final Polygon parsedPolygon = (Polygon) kmlFeature.getFeatureList().get(0);
                                                compareEllipseToPolygon(feature, parsedPolygon);
                                                resultFound[0] = true;
                                            } catch (final Exception e){
                                                resultFound[0] = true;
                                                fail(e.getMessage());
                                            }
                                        }
                                        @Override
                                        public void exportFailed(Exception ex)
                                        {
                                            resultFound[0] = true;
                                            fail(ex.getMessage());
                                        }
                                      });

        int i = 0;
        while(!resultFound[0] && ++i <= 10) {
            try {
                Thread.sleep(50);
            } catch (InterruptedException ie) {

            }
        }
        if (i >= 10) {
            fail("No result in half second for exportEllipse");
        }
    }

    @Test
    public void exportSquare() throws Exception
    {
        this.remoteMap.clearContainer();
        final Overlay overlay       = (Overlay) addOverlayToMap(this.remoteMap);
        final Square feature        = addSquare(overlay);
        final boolean[] resultFound = {false};

        EmpKMLExporter.exportToString(this.remoteMap,
                                      feature,
                                      true,
                                      new IEmpExportToStringCallback()
                                      {
                                        @Override
                                        public void exportSuccess(final String kmlString)
                                        {
                                            try 
                                            {
                                                final KML kmlFeature = new KML(kmlString);
                                                final Polygon parsedPolygon = (Polygon) kmlFeature.getFeatureList().get(0);
                                                compareSquareToPolygon(feature, parsedPolygon);
                                                resultFound[0] = true;
                                            } catch (final Exception e){
                                                resultFound[0] = true;
                                                fail(e.getMessage());
                                            }
                                        }

                                        @Override
                                        public void exportFailed(Exception ex)
                                        {
                                            resultFound[0] = true;
                                            fail(ex.getMessage());
                                        }
                                      });

        int i = 0;
        while(!resultFound[0] && ++i <= 10) {
            try {
                Thread.sleep(50);
            } catch (InterruptedException ie) {

            }
        }
        if (i >= 10) {
            fail("No result in half second for exportSquare");
        }
    }

    @Test
    public void exportRectangle() throws Exception
    {
        this.remoteMap.clearContainer();
        final Overlay overlay       = (Overlay) addOverlayToMap(this.remoteMap);
        final Rectangle feature     = addRectangle(overlay);
        final boolean[] resultFound = {false};

        EmpKMLExporter.exportToString(this.remoteMap,
                                      feature,
                                      true,
                                      new IEmpExportToStringCallback()
                                      {
                                        @Override
                                        public void exportSuccess(final String kmlString)
                                        {
                                            try 
                                            {
                                                final KML kmlFeature = new KML(kmlString);
                                                final Polygon parsedPolygon = (Polygon) kmlFeature.getFeatureList().get(0);
                                                compareRectangleToPolygon(feature, parsedPolygon);
                                                resultFound[0] = true;
                                            } catch (Exception e) {
                                                resultFound[0] = true;
                                                fail(e.getMessage());
                                            }
                                        }

                                        @Override
                                        public void exportFailed(Exception ex)
                                        {
                                            resultFound[0] = true;
                                            fail(ex.getMessage());
                                        }
                                      });

        int i = 0;
        while(!resultFound[0] && ++i <= 10) {
            try {
                Thread.sleep(50);
            } catch (InterruptedException ie) {

            }
        }
        if (i >= 10) {
            fail("No result in half second for exportRectangle");
        }
    }

    @Test
    public void exportText() throws Exception
    {
        this.remoteMap.clearContainer();
        final Overlay overlay       = (Overlay) addOverlayToMap(this.remoteMap);
        final Text feature          = addText(overlay);
        final boolean[] resultFound = {false};

        EmpKMLExporter.exportToString(this.remoteMap,
                                      feature,
                                      true,
                                      new IEmpExportToStringCallback()
                                      {
                                        @Override
                                        public void exportSuccess(final String kmlString)
                                        {
                                            try 
                                            {
                                                final KML kmlFeature = new KML(kmlString);
                                                //Ellipse do not exist in KML so they are represented by paths
                                                final Point importedText = (Point) kmlFeature.getFeatureList().get(0);
                                                compareTextToPoint(feature, importedText);
                                                resultFound[0] = true;
                                            } catch (final Exception e){
                                                resultFound[0] = true;
                                                fail(e.getMessage());
                                            }
                                        }

                                        @Override
                                        public void exportFailed(Exception ex)
                                        {
                                            resultFound[0] = true;
                                            fail(ex.getMessage());
                                        }
                                      });

        int i = 0;
        while(!resultFound[0] && ++i <= 10) {
            try {
                Thread.sleep(50);
            } catch (InterruptedException ie) {

            }
        }
        if (i >= 10) {
            fail("No result in half second for exportText");
        }
    }

    @Test
    public void exportPath() throws Exception
    {
        this.remoteMap.clearContainer();
        final Overlay overlay       = (Overlay) addOverlayToMap(this.remoteMap);
        final Path feature          = addPath(overlay);
        final boolean[] resultFound = {false};

        EmpKMLExporter.exportToString(this.remoteMap,
                                      feature,
                                      true,
                                      new IEmpExportToStringCallback()
                                      {
                                        @Override
                                        public void exportSuccess(final String kmlString)
                                        {
                                            try 
                                            {
                                                final KML kmlFeature = new KML(kmlString);
                                                final Path importedPath = (Path) kmlFeature.getFeatureList().get(0);
                                                comparePath(feature, importedPath);
                                                resultFound[0] = true;
                                            } catch (final Exception e){
                                                resultFound[0] = true;
                                                fail(e.getMessage());
                                            }
                                        }

                                        @Override
                                        public void exportFailed(Exception ex)
                                        {
                                            resultFound[0] = true;
                                            fail(ex.getMessage());
                                        }
                                      });

        int i = 0;
        while(!resultFound[0] && ++i <= 10) {
            try {
                Thread.sleep(50);
            } catch (InterruptedException ie) {

            }
        }
        if (i >= 10) {
            fail("No result in half second for exportPath");
        }
    }

    static IOverlay addOverlayToMap(final IMap map) throws EMP_Exception
    {
        final Overlay overlay = new Overlay();
        overlay.setName("Test Overlay " + count++);
        map.addOverlay(overlay, true);
        return overlay;
    }

    static Point addPoint(final IOverlay overlay) throws Exception
    {
        final Point oPoint = new Point();
        oPoint.setIconScale(10);
        oPoint.setIconURI("https://127.0.0.1");
        oPoint.setIconStyle(new GeoIconStyle());
        addFeatureStyles(oPoint);
        overlay.addFeature(oPoint, true);
        return oPoint;
    }

    static Text addText(final IOverlay overlay) throws Exception
    {
        final Text oText = new Text();
        oText.setRotationAngle(40);
        oText.setText("Test String");
        addFeatureStyles(oText);
        overlay.addFeature(oText, true);
        return oText;
    }

    static Circle addCircle(final IOverlay overlay) throws Exception
    {
        final Circle oCircle = new Circle();
        oCircle.setName("Test Circle");
        oCircle.setRadius(15.5);
        addFeatureStyles(oCircle);
        overlay.addFeature(oCircle, true);
        return oCircle;
    }

    static Ellipse addEllipse(final IOverlay overlay) throws Exception
    {
        final Ellipse oEllipse = new Ellipse();
        oEllipse.setSemiMinor(15.5);
        oEllipse.setSemiMajor(14.5);
        addFeatureStyles(oEllipse);
        overlay.addFeature(oEllipse, true);
        return oEllipse;
    }

    static Square addSquare(final IOverlay overlay) throws Exception
    {
        final Square oSquare = new Square();
        oSquare.setWidth(15.5);
        addFeatureStyles(oSquare);
        overlay.addFeature(oSquare, true);
        return oSquare;
    }

    static Rectangle addRectangle(final IOverlay overlay) throws Exception
    {
        final Rectangle oRectangle = new Rectangle();
        oRectangle.setWidth(15.5);
        oRectangle.setHeight(14.5);
        addFeatureStyles(oRectangle);
        overlay.addFeature(oRectangle, true);
        return oRectangle;
    }

    static Polygon addPolygon(final IOverlay overlay) throws Exception
    {
        final Polygon oPolygon = new Polygon();
        addMPFeatureStyles(oPolygon);
        overlay.addFeature(oPolygon, true);
        return oPolygon;
    }

    static Path addPath(final IOverlay overlay) throws Exception
    {
        final Path oPath = new Path();
        addFeatureStyles(oPath);
        overlay.addFeature(oPath, true);
        return oPath;
    }

    static GeoPosition getRandomLocation() throws EMP_Exception {
        final GeoPosition location = new GeoPosition();
        location.setLatitude(getRandomValueBetween(-90.0,90.0));
        location.setLongitude(getRandomValueBetween(-180.0, 180.0));
        return location;
    }

    static double getRandomValueBetween(final double low, final double high)
    {
        return low + (high - low) * new Random().nextDouble();
    }

    static void addFeatureStyles(final Feature feature) throws Exception{
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

    static void addMPFeatureStyles(final Feature feature) throws Exception{
        feature.getPositions().clear();
        for (int i = 0; i < 4; i++) {
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
