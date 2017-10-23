package mil.emp3.api;

import android.graphics.Color;

import org.cmapi.primitives.GeoDocument;
import org.junit.Before;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.net.URL;

import static org.junit.Assert.*;

/**
 * Created by matt.miller@rgi-corp.local on 10/23/17.
 */
@PrepareForTest({Color.class})
public class KMLTest extends TestBaseSingleMap {
    private final static String TAG = KMLTest.class.getSimpleName();

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
        GeoDocument gd = new GeoDocument();
        final URL url = this.getClass().getClassLoader().getResource("kml_samples.kml");
        final FileReader fileReader = new FileReader(url.getPath());
        final BufferedReader bufferedReader = new BufferedReader(fileReader);
        String fileLine = bufferedReader.readLine();
        String KMLString = "";
        while(fileLine != null){
            KMLString += fileLine;
            fileLine = bufferedReader.readLine();
        }


        //KML String Constructor
        try {
            testKML = new KML("");
            fail();
        } catch (final IllegalArgumentException e) {
            assertEquals(e.getMessage(), "KML string can not be null nor empty.");
        }
        try {
            testKML = new KML(KMLString);
        } catch (final IllegalArgumentException e) {
            fail();
        }

        //KML URL Constructor
        try {
            testKML = new KML(url);
        } catch (final Exception e) {
            fail();
        }

        //KML URL and DocumentBase Constructor
        try {
            testKML = new KML(null, null);
        } catch (final Exception e) {
            
        }


        //GeoDocument Constructor
        try {
           testKML = new KML(gd);
        } catch (final IllegalArgumentException e) {
            assertEquals(e.getMessage(), "GeoRenderable does not contain a KML string or URL property.");
        }

        try {
            testKML = new KML(gd);
        } catch (final Exception e) {
            assertEquals(e.getMessage(), "Hello");
        }

        gd.setDocumentURI(url.toString());
        try {
            testKML = new KML(gd);
        } catch (final Exception e) {
            assertEquals(e.getMessage(), "HI");
        }
    }

    @org.junit.Test
    public void getFeatureList() throws Exception
    {
    }

    @org.junit.Test
    public void getImageLayerList() throws Exception
    {
    }

    @org.junit.Test
    public void findKMLId() throws Exception
    {
    }

    @org.junit.Test
    public void testDocumentURI() throws Exception
    {
    }

    @org.junit.Test
    public void testDocumentMIMEType() throws Exception
    {
    }

    @org.junit.Test
    public void testProperties() throws Exception
    {
    }

    @org.junit.Test
    public void getGeoDocument() throws Exception
    {
    }

    @org.junit.Test
    public void exportToKML() throws Exception
    {
    }

    @org.junit.Test
    public void testToString() throws Exception
    {
    }
}