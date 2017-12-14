package mil.emp3.api;

import org.junit.Before;
import org.junit.Test;
import org.robolectric.util.Logger;

import mil.emp3.api.abstracts.Feature;
import mil.emp3.api.interfaces.IEmpExportToStringCallback;
import mil.emp3.json.geoJson.GeoJsonCaller;

import static mil.emp3.api.utils.ComparisonUtils.compareFeatureToGeoJsonPolygon;
import static mil.emp3.api.utils.ComparisonUtils.compareFeatureToPath;
import static mil.emp3.api.utils.ComparisonUtils.comparePath;
import static mil.emp3.api.utils.ComparisonUtils.comparePoint;
import static mil.emp3.api.utils.ComparisonUtils.comparePolygon;
import static org.junit.Assert.fail;

public class GeoJSONExportTest extends TestBaseSingleMap{
        private final static String TAG = mil.emp3.api.GeoJSONExportTest.class.getName();
        private static int count = 0;

        @Before
        public void setup() throws Exception{
            super.init();
            super.setupSingleMap(TAG);
        }

        @Test
        public void exportPoint() throws Exception
        {

            final Overlay overlay       = (Overlay) KMLExportTest.addOverlayToMap(this.remoteMap);
            final Point feature         = KMLExportTest.addPoint(overlay);
            final boolean[] resultFound = {false};

            GeoJsonCaller.exportToString(this.remoteMap,
                    feature,
                    false,
                    new IEmpExportToStringCallback()
                    {
                        @Override
                        public void exportSuccess(final String geoJSONStr)
                        {
                            try
                            {
                                final GeoJSON geoJSON = new GeoJSON(geoJSONStr);
                                final Point importedPoint = (Point) geoJSON.getFeatureList().get(0);
                                comparePoint(importedPoint, feature);
                            } catch (final Exception e){
                                fail(e.getMessage());
                            } finally {
                                resultFound[0] = true;
                            }
                        }

                        @Override
                        public void exportFailed(Exception ex)
                        {
                            resultFound[0] = true;
                            fail(ex.getMessage());
                        }
                    });

            while(!resultFound[0]) {
                try {
                    Thread.sleep(50);
                } catch (InterruptedException ie) {

                }
            }
        }

        @Test
        public void exportOverlay() throws Exception {
            this.remoteMap.clearContainer();
            final Overlay overlay       = (Overlay) KMLExportTest.addOverlayToMap(this.remoteMap);
            final Point feature         = KMLExportTest.addPoint(overlay);
            final boolean[] resultFound = {false};

            GeoJsonCaller.exportToString(this.remoteMap,
                    overlay,
                    true,
                    new IEmpExportToStringCallback()
                    {
                        public void exportSuccess(final String geoJSONStr)
                        {
                            try
                            {
                                final GeoJSON geoJSON = new GeoJSON(geoJSONStr);
                                final Point importedPoint = (Point) geoJSON.getFeatureList().get(0);
                                comparePoint(importedPoint, feature);
                            } catch (final Exception e){
                                fail(e.getMessage());
                            } finally {
                                resultFound[0] = true;
                            }
                        }

                        @Override
                        public void exportFailed(Exception ex)
                        {
                            resultFound[0] = true;
                            fail(ex.getMessage());
                        }
                    });
            while(!resultFound[0]) {
                try {
                    Thread.sleep(50);
                } catch (InterruptedException ie) {

                }
            }
        }

        @Test
        public void exportMap() throws Exception {
            this.remoteMap.clearContainer();
            final Overlay overlay         = (Overlay) KMLExportTest.addOverlayToMap(this.remoteMap);
            final Point feature           = KMLExportTest.addPoint(overlay);
            final boolean[] resultFound   = {false};

            GeoJsonCaller.exportToString(this.remoteMap,
                    true,
                    new IEmpExportToStringCallback()
                    {
                        @Override
                        public void exportSuccess(final String geoJSONStr)
                        {
                            try
                            {
                                final GeoJSON geoJSON = new GeoJSON(geoJSONStr);
                                final Point importedPoint = (Point) geoJSON.getFeatureList().get(0);
                                comparePoint(importedPoint, feature);
                            } catch (final Exception e){
                                fail(e.getMessage());
                            } finally {
                                resultFound[0] = true;
                            }
                        }

                        @Override
                        public void exportFailed(Exception ex)
                        {
                            resultFound[0] = true;
                            fail(ex.getMessage());
                        }
                    });

            while(!resultFound[0]) {
                try {
                    Thread.sleep(50);
                } catch (InterruptedException ie) {

                }
            }
        }

        @Test
        public void exportPolygon() throws Exception
        {
            this.remoteMap.clearContainer();
            final Overlay overlay       = (Overlay) KMLExportTest.addOverlayToMap(this.remoteMap);
            final Polygon feature       = KMLExportTest.addPolygon(overlay);
            final boolean[] resultFound = {false};

            GeoJsonCaller.exportToString(this.remoteMap,
                    feature,
                    true,
                    new IEmpExportToStringCallback()
                    {
                        @Override
                        public void exportSuccess(final String geoJSONStr)
                        {
                            try
                            {
                                final GeoJSON geoJSON = new GeoJSON(geoJSONStr);
                                final Polygon importedPolygon = (Polygon) geoJSON.getFeatureList().get(0);
                                comparePolygon(importedPolygon, feature);
                            } catch (final Exception e){
                                fail(e.getMessage());
                            } finally {
                                resultFound[0] = true;
                            }
                        }

                        @Override
                        public void exportFailed(Exception ex)
                        {
                            resultFound[0] = true;
                            fail(ex.getMessage());
                        }
                    });

            while(!resultFound[0]) {
                try {
                    Thread.sleep(50);
                } catch (InterruptedException ie) {

                }
            }
        }

        private void parseGeoJSON(String featureType, String geoJSONStr, Feature feature){
            try {
                final GeoJSON geoJSON = new GeoJSON(geoJSONStr);
                //Circles do not exist in KML so they are represented by paths
                for (int i = 0; i < geoJSON.getFeatureList().size(); i++) {
                    Object obj = geoJSON.getFeatureList().get(i);
                    if (obj instanceof Path) {
                        Logger.info(featureType + " is rendered as Path");
                        Logger.info(feature.toString());
                        Logger.info(obj.toString());
                        compareFeatureToPath(feature, (Path) obj);
                    } else if (obj instanceof Polygon) {
                        Logger.info(featureType + " is rendered as Polygon");
                        Logger.info(feature.toString());
                        Logger.info(obj.toString());
                        compareFeatureToGeoJsonPolygon(feature, (Polygon) obj);
                    } else if (obj instanceof Point && featureType.equals("Text")) {
                        // GeoJSON has no place for text, so comparison is omitted
                    } else {
                        Logger.error(featureType + " is rendered as " + obj.getClass().getSimpleName());
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                fail(e.getMessage());
            }
        }

        @Test
        public void exportCircle() throws Exception
        {
            this.remoteMap.clearContainer();
            final Overlay overlay       = (Overlay) KMLExportTest.addOverlayToMap(this.remoteMap);
            final Circle feature        = KMLExportTest.addCircle(overlay);
            final boolean[] resultFound = {false};

            GeoJsonCaller.exportToString(this.remoteMap,
                    feature,
                    true,
                    new IEmpExportToStringCallback()
                    {
                        @Override
                        public void exportSuccess(final String geoJSONStr)
                        {
                            parseGeoJSON("Circle", geoJSONStr, feature);
                            resultFound[0] = true;
                        }

                        @Override
                        public void exportFailed(final Exception ex)
                        {
                            resultFound[0] = true;
                            fail(ex.getMessage());
                        }
                    });

            while(!resultFound[0]) {
                try {
                    Thread.sleep(50);
                } catch (InterruptedException ie) {

                }
            }
        }

        @Test
        public void exportEllipse() throws Exception
        {
            this.remoteMap.clearContainer();
            final Overlay overlay       = (Overlay) KMLExportTest.addOverlayToMap(this.remoteMap);
            final Ellipse feature       = KMLExportTest.addEllipse(overlay);
            final boolean[] resultFound = {false};

            GeoJsonCaller.exportToString(this.remoteMap,
                    feature,
                    true,
                    new IEmpExportToStringCallback()
                    {
                        @Override
                        public void exportSuccess(final String geoJSONStr)
                        {
                            parseGeoJSON("Ellipse", geoJSONStr, feature);
                            resultFound[0] = true;
                        }
                        @Override
                        public void exportFailed(Exception ex)
                        {
                            resultFound[0] = true;
                            fail(ex.getMessage());
                        }
                    });

            while(!resultFound[0]) {
                try {
                    Thread.sleep(50);
                } catch (InterruptedException ie) {

                }
            }
        }

        @Test
        public void exportSquare() throws Exception
        {
            this.remoteMap.clearContainer();
            final Overlay overlay       = (Overlay) KMLExportTest.addOverlayToMap(this.remoteMap);
            final Square feature        = KMLExportTest.addSquare(overlay);
            final boolean[] resultFound = {false};

            GeoJsonCaller.exportToString(this.remoteMap,
                    feature,
                    true,
                    new IEmpExportToStringCallback()
                    {
                        @Override
                        public void exportSuccess(final String geoJSONStr)
                        {
                            parseGeoJSON("Square", geoJSONStr, feature);
                            resultFound[0] = true;
                        }

                        @Override
                        public void exportFailed(Exception ex)
                        {
                            resultFound[0] = true;
                            fail(ex.getMessage());
                        }
                    });

            while(!resultFound[0]) {
                try {
                    Thread.sleep(50);
                } catch (InterruptedException ie) {

                }
            }
        }

        @Test
        public void exportRectangle() throws Exception
        {
            this.remoteMap.clearContainer();
            final Overlay overlay       = (Overlay) KMLExportTest.addOverlayToMap(this.remoteMap);
            final Rectangle feature     = KMLExportTest.addRectangle(overlay);
            final boolean[] resultFound = {false};

            GeoJsonCaller.exportToString(this.remoteMap,
                    feature,
                    true,
                    new IEmpExportToStringCallback()
                    {
                        @Override
                        public void exportSuccess(final String geoJSONStr)
                        {
                            try {
                                parseGeoJSON("Rectangle", geoJSONStr, feature);
                            } finally {
                                resultFound[0] = true;
                            }
                        }

                        @Override
                        public void exportFailed(Exception ex)
                        {
                            resultFound[0] = true;
                            fail(ex.getMessage());
                        }
                    });

            while(!resultFound[0]) {
                try {
                    Thread.sleep(50);
                } catch (InterruptedException ie) {

                }
            }
        }

        @Test
        public void exportText() throws Exception
        {
            this.remoteMap.clearContainer();
            final Overlay overlay       = (Overlay) KMLExportTest.addOverlayToMap(this.remoteMap);
            final Text feature          = KMLExportTest.addText(overlay);
            final boolean[] resultFound = {false};

            GeoJsonCaller.exportToString(this.remoteMap,
                    feature,
                    true,
                    new IEmpExportToStringCallback()
                    {
                        @Override
                        public void exportSuccess(final String geoJSONStr)
                        {
                            try {
                                parseGeoJSON("Text", geoJSONStr, feature);
                            } finally {
                                resultFound[0] = true;
                            }
                        }

                        @Override
                        public void exportFailed(Exception ex)
                        {
                            resultFound[0] = true;
                            fail(ex.getMessage());
                        }
                    });

            while(!resultFound[0]) {
                try {
                    Thread.sleep(50);
                } catch (InterruptedException ie) {

                }
            }
        }

        @Test
        public void exportPath() throws Exception
        {
            this.remoteMap.clearContainer();
            final Overlay overlay       = (Overlay) KMLExportTest.addOverlayToMap(this.remoteMap);
            final Path feature          = KMLExportTest.addPath(overlay);
            final boolean[] resultFound = {false};

            GeoJsonCaller.exportToString(this.remoteMap,
                    feature,
                    true,
                    new IEmpExportToStringCallback()
                    {
                        @Override
                        public void exportSuccess(final String geoJSONStr)
                        {
                            try
                            {
                                final GeoJSON geoJSON = new GeoJSON(geoJSONStr);
                                final Path importedPath = (Path) geoJSON.getFeatureList().get(0);
                                comparePath(feature, importedPath);
                            } catch (final Exception e){
                                fail(e.getMessage());
                            } finally {
                                resultFound[0] = true;
                            }
                        }

                        @Override
                        public void exportFailed(Exception ex)
                        {
                            resultFound[0] = true;
                            fail(ex.getMessage());
                        }
                    });

            while(!resultFound[0]) {
                try {
                    Thread.sleep(50);
                } catch (InterruptedException ie) {

                }
            }
        }

    }
