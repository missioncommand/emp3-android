package mil.emp3.api;

import org.cmapi.primitives.GeoBounds;
import org.cmapi.primitives.GeoDocument;
import org.cmapi.primitives.GeoPosition;
import org.cmapi.primitives.GeoRenderable;
import org.cmapi.primitives.IGeoBounds;
import org.cmapi.primitives.IGeoDocument;
import org.cmapi.primitives.IGeoPosition;
import org.cmapi.primitives.IGeoRenderable;
import org.cmapi.primitives.IGeoStrokeStyle;
import org.xmlpull.v1.XmlSerializer;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

import mil.emp3.api.abstracts.Feature;
import mil.emp3.api.enums.FeatureTypeEnum;
import mil.emp3.api.exceptions.EMP_Exception;
import mil.emp3.api.interfaces.IFeature;
import mil.emp3.api.interfaces.IGeoJSON;
import mil.emp3.json.geoJson.GeoJsonParser;

public class GeoJSON extends Feature<IGeoRenderable> implements IGeoJSON {

    final static int BUFFERSIZE = 1024;
    private List<IFeature> featureList = new ArrayList<>();
    private final IGeoDocument geoDocument;

    public GeoJSON(String input) throws EMP_Exception {
        super(new GeoRenderable(), FeatureTypeEnum.GEOJSON);
        featureList.addAll(GeoJsonParser.parse(input));
        geoDocument = new GeoDocument();
        geoDocument.setDocumentURI(input);
        geoDocument.setDocumentMIMEType("application/vnd.geo+json");
        IGeoBounds bBox = new GeoBounds();
    }

    public GeoJSON(InputStream stream) throws EMP_Exception {
        super(new GeoRenderable(), FeatureTypeEnum.GEOJSON);
        String input = streamToString(stream);
        featureList.addAll(GeoJsonParser.parse(input));
        geoDocument = new GeoDocument();
        geoDocument.setDocumentURI(input);
        geoDocument.setDocumentMIMEType("application/vnd.geo+json");
    }

    private String streamToString(InputStream stream) {
        final char[] buffer = new char[BUFFERSIZE];
        final StringBuilder out = new StringBuilder();
        try (Reader in = new InputStreamReader(stream, "UTF-8")) {
            while (true) {
                int rsz = in.read(buffer, 0, buffer.length);
                if (rsz < 0)
                    break;
                out.append(buffer, 0, rsz);
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
        return out.toString();
    }

    @Override
    public List<IFeature> getFeatureList() {
        return featureList;
    }

    @Override
    public void setDocumentURI(String documentURI) {
        geoDocument.setDocumentURI(documentURI);
    }

    @Override
    public String getDocumentURI() {
        return geoDocument.getDocumentURI();
    }

    @Override
    public void setDocumentMIMEType(String documentMIMEType) {
        geoDocument.setDocumentMIMEType(documentMIMEType);
    }

    @Override
    public String getDocumentMIMEType() {
        return geoDocument.getDocumentMIMEType();
    }
}
