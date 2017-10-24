package mil.emp3.api;

import android.graphics.Color;
import android.util.Log;

import org.cmapi.primitives.GeoDocument;
import org.cmapi.primitives.IGeoDocument;
import org.junit.Before;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import mil.emp3.api.interfaces.IFeature;
import mil.emp3.api.interfaces.IImageLayer;

import static org.junit.Assert.*;

/**
 * Created by matt.miller@rgi-corp.local on 10/23/17.
 */
@PrepareForTest({Color.class})
public class KMLTest extends TestBaseSingleMap {
    private final static String TAG = KMLTest.class.getSimpleName();
    private final URL url = this.getClass().getClassLoader().getResource("kml_samples.kml");
    @Before
    public void setUp() throws Exception {
        setupSingleMap(TAG);
        PowerMockito.mockStatic(Color.class);
        PowerMockito.when(Color.class, "parseColor", Mockito.any(String.class)).thenReturn(0);
    }

    @org.junit.Test
    public void testConstructors() throws Exception
    {
        KML testKML;
        final FileReader fileReader = new FileReader(url.getPath());
        final BufferedReader bufferedReader = new BufferedReader(fileReader);
        String fileLine = bufferedReader.readLine();
        String KMLString = "";
        while(fileLine != null){
            KMLString += fileLine;
            fileLine = bufferedReader.readLine();
        }

        //KML URL Constructor
        testKML = new KML(url);


        //KML URL and DocumentBase Constructor
        try {
            testKML = new KML(null, null);
        } catch (final IllegalArgumentException e) {
            assertEquals(e.getMessage(), "Invalid parameters. URL can not be null.");
        }
        testKML = new KML(url, null);
        testKML = new KML(url, "HI");

        //InputStream Constructor
        InputStream inputStream = null;
        try {
            testKML = new KML(inputStream);
        } catch (final IllegalArgumentException e) {
            assertEquals(e.getMessage(), "Input stream can not be null.");
        }
        inputStream = url.openStream();
        testKML = new KML(inputStream);

        //KML String Constructor
        try {
            testKML = new KML("");
            fail();
        } catch (final IllegalArgumentException e) {
            assertEquals(e.getMessage(), "KML string can not be null nor empty.");
        }
        testKML = new KML(KMLString);

        //GeoDocument Constructor
        GeoDocument gd = new GeoDocument();
        testKML = new KML(testKML);
        try {
           testKML = new KML(gd);
        } catch (final IllegalArgumentException e) {
            assertEquals(e.getMessage(), "GeoRenderable does not contain a KML string or URL property.");
        }

        gd.setDocumentURI(url.toString());
        testKML = new KML(gd);
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
        final KML testKML = new KML(url);
        IGeoDocument result = testKML.getGeoDocument();
        assertEquals(result, new KML(result).getGeoDocument());
    }

    @org.junit.Test
    public void exportToKML() throws Exception
    {
        //probably shouldn't do this
        final KML testKML = new KML(url);
        assertEquals(testKML.exportToKML(),"");
    }

    @org.junit.Test
    public void testToString() throws Exception
    {
        final GeoDocument gd = new GeoDocument();
        gd.setDocumentURI(url.toString());
        KML kmlTest = new KML(gd);
        Log.i(TAG, kmlTest.toString());
        kmlTest = new KML(url);
        Log.i(TAG, kmlTest.toString());
    }
}