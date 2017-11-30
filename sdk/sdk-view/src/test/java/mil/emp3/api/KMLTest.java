package mil.emp3.api;

import android.graphics.Color;
import android.webkit.URLUtil;

import org.cmapi.primitives.GeoDocument;
import org.cmapi.primitives.IGeoDocument;
import org.junit.Before;
import org.junit.Test;
import org.powermock.core.classloader.annotations.PrepareForTest;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import mil.emp3.api.interfaces.IFeature;
import mil.emp3.api.interfaces.IImageLayer;

import static org.junit.Assert.assertEquals;

/**
 * Created by matt.miller@rgi-corp.local on 10/23/17.
 */
@PrepareForTest({Color.class, URLUtil.class})
public class KMLTest extends TestBaseSingleMap {
    private final static String TAG = KMLTest.class.getSimpleName();
    private final URL url = this.getClass().getClassLoader().getResource("kml_samples.kml");
    @Before
    public void setUp() throws Exception {
        setupSingleMap(TAG);
    }

    @Test
    public void kmlStringConstructor() throws Exception {
        KML testKML;
        final FileReader fileReader = new FileReader(url.getPath());
        final BufferedReader bufferedReader = new BufferedReader(fileReader);
        String fileLine = bufferedReader.readLine();
        String KMLString = "";
        while (fileLine != null) {
            KMLString += fileLine;
            fileLine = bufferedReader.readLine();
        }
        testKML = new KML(KMLString);
    }

    @Test
    public void urlConstructor() throws Exception {
        //KML URL Constructor
        KML testKML = new KML(url);
    }

    @Test(expected = IllegalArgumentException.class)
    public void urlDocumentBaseBothNullConstructor() throws Exception {
        //KML URL and DocumentBase Constructor
        KML testKML = new KML(null, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void urlDocumentBaseNullDocumentBaseConstructor() throws Exception {
        KML testKML = new KML(url, null);
    }

    @Test
    public void urlDocumentBaseConstructor() throws Exception {
        KML testKML = new KML(url, "HI");
    }

    @Test(expected = IllegalArgumentException.class)
    public void nullInputStreamConstructor() throws Exception {
        InputStream inputStream = null;
        KML testKML = new KML(inputStream);
    }

    @Test
    public void validInputStreamConstructor() throws Exception {
        InputStream inputStream = url.openStream();
        KML testKML = new KML(inputStream);
    }

    @Test(expected = IllegalArgumentException.class)
    public void nullStringConstructor() throws Exception {
        KML testKML = new KML("");
    }

    @Test
    public void cloneConstructor() throws Exception {
        KML testKML = new KML(url);
        testKML = new KML(testKML);
    }

    @Test(expected=IllegalArgumentException.class)
    public void invalidGeoDocumentConstructor() throws Exception {
        GeoDocument gd = new GeoDocument();
        KML testKML = new KML(gd);
        testKML = new KML(gd);
    }

    @Test
    public void validGeoDocumentConstructor() throws Exception {
        GeoDocument gd = new GeoDocument();
        gd.setDocumentURI(url.toString());
        KML testKML = new KML(gd);
    }

    @org.junit.Test
    public void getFeatureList() throws Exception
    {
        final KML testKML = new KML(url);
        List<IFeature> result = testKML.getFeatureList();
        assertEquals(result.size(), 19);
    }

    @org.junit.Test
    public void getImageLayerList() throws Exception
    {
        final KML testKML = new KML(url);
        List<IImageLayer> result = testKML.getImageLayerList();
        assertEquals(result.size(), 1);

    }

    @org.junit.Test
    public void findKMLId() throws Exception
    {
        final KML testKML = new KML(url);
        assertEquals(testKML.findKMLId("false_key"), new ArrayList<>());
    }

    @org.junit.Test
    public void testDocumentURI() throws Exception
    {
        final KML testKML = new KML(url);
        testKML.setDocumentURI("test_URI");
        assertEquals(testKML.getDocumentURI(),"test_URI");
    }

    @org.junit.Test
    public void testDocumentMIMEType() throws Exception
    {
        final KML testKML = new KML(url);
        testKML.setDocumentMIMEType("test_type");
        assertEquals(testKML.getDocumentMIMEType(), "test_type");
    }

    @org.junit.Test
    public void testProperties() throws Exception
    {
        final KML testKML = new KML(url);
        HashMap<String, String> properties = testKML.getProperties();
        String KML_STRING_PROPERTY = "emp3 kml string";
        Boolean result = testKML.containsProperty(KML_STRING_PROPERTY);
        assertEquals(result, properties.containsKey(KML_STRING_PROPERTY));
        result = testKML.containsProperty("false_key");
        assertEquals(result, properties.containsKey("false_key"));
    }

    @org.junit.Test
    public void getGeoDocument() throws Exception
    {
        final GeoDocument gd = new GeoDocument();
        gd.setDocumentURI(url.toString());
        final KML testKML = new KML(gd);
        IGeoDocument result = testKML.getGeoDocument();
        assertEquals(result, gd);
    }

    @org.junit.Test
    public void exportToKML() throws Exception
    {
        final KML testKML = new KML(url);
        assertEquals(testKML.exportToKML(),"");
    }

    @Test
    public void geoDocumentConstructorToString() throws Exception {
        final GeoDocument gd = new GeoDocument();
        gd.setDocumentURI(url.toString());
        KML kmlTest = new KML(gd);
        final String expectedString = "KML \n" +
                                      "Image Layer Count 1\n" +
                                      "Feature Count 19 Point at\n" +
                                      "\tlatitude: 37.42228990140251\n" +
                                      "\tlongitude: -122.0822035425683\n" +
                                      "\taltitude: 0.0";
        assertEquals(kmlTest.toString(), expectedString);
    }

    @Test
    public void urlConstructorToString() throws Exception {
        KML kmlTest = new KML(url);
        final String expectedString = "KML KML Samples\n" +
                                      "Image Layer Count 1\n" +
                                      "Feature Count 19 Point at\n" +
                                      "\tlatitude: 37.42228990140251\n" +
                                      "\tlongitude: -122.0822035425683\n" +
                                      "\taltitude: 0.0";
        assertEquals(kmlTest.toString(), expectedString);
    }
}