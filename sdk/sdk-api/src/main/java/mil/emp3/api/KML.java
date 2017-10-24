package mil.emp3.api;

import org.cmapi.primitives.GeoDocument;
import org.cmapi.primitives.GeoRenderable;
import org.cmapi.primitives.IGeoDocument;
import org.cmapi.primitives.IGeoRenderable;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import mil.emp3.api.abstracts.Feature;
import mil.emp3.api.enums.FeatureTypeEnum;
import mil.emp3.api.interfaces.IFeature;
import mil.emp3.api.interfaces.IImageLayer;
import mil.emp3.api.interfaces.IKML;
import mil.emp3.api.utils.kml.EmpKMLParser;

/**
 * This class implements the KML feature. The current verson only parses KML dicuments. KML fracgments are
 * not supported.
 */

public class KML extends Feature<IGeoRenderable> implements IKML {
    private static final String KML_STRING_PROPERTY = "emp3 kml string";
    //private static final String KML_URL_PROPERTY = "emp3 kml url";

    private final IGeoDocument geoDocument;

    // This featureList contains all the EMP features the KML translates into.
    private final java.util.List<IFeature> featureList = new java.util.ArrayList<>();
    // This list contains all the ground overlays defined in the KML document.
    private final java.util.List<IImageLayer> imageLayerList = new java.util.ArrayList<>();

    /**
     * This constructor creates a KML feature from a document geo object.
     * @param document An object that implements the IGeoDocument interface.
     * @throws IllegalArgumentException This exception is raised if the geo renderable object does not contain a KML string or URL.
     * @throws XmlPullParserException This exception is raised if the KML fails to parse correctly.
     * @throws IOException This exception is raised in the event of an IO error accessing the URL.
     */
    public KML(final IGeoDocument document) throws XmlPullParserException, IOException {
        super(new GeoRenderable(), FeatureTypeEnum.KML);

        if (!document.getProperties().containsKey(KML_STRING_PROPERTY) && !((null != document.getDocumentURI()) && !document.getDocumentURI().isEmpty())) {
            throw new IllegalArgumentException("GeoRenderable does not contain a KML string or URL property.");
        }

        this.geoDocument = document;
        final EmpKMLParser empKMLParser;

        // If there is a kml string in the properties, use it.
        if (this.getProperties().containsKey(KML_STRING_PROPERTY)) {
            final String kmlString = this.getProperties().get(KML_STRING_PROPERTY);
            empKMLParser = new EmpKMLParser(kmlString);
        } else {
            // Else it MUST have a URI.
            final String kmlURL =  this.geoDocument.getDocumentURI();
            final java.net.URL url = new java.net.URL(kmlURL);

            final InputStream inputStream = url.openStream();
            empKMLParser = new EmpKMLParser(inputStream);
            inputStream.close();
        }

        processParseOutput(empKMLParser);
    }

    /**
     * This constructor take a valid KML string.
     * @param kmlString A valid KML document.
     * @throws IllegalArgumentException This exception is raised if the string is null or empty.
     * @throws XmlPullParserException This exception is raised if the KML fails to parse correctly.
     * @throws IOException This exception is raised in the event of an IO error accessing the URL.
     */
    public KML(final String kmlString) throws XmlPullParserException, IOException {
        super(new GeoRenderable(), FeatureTypeEnum.KML);

        if ((null == kmlString) || (kmlString.isEmpty())) {
            throw new IllegalArgumentException("KML string can not be null nor empty.");
        }
        final EmpKMLParser empKMLParser;
        empKMLParser = new EmpKMLParser(kmlString);

        this.geoDocument = new GeoDocument();
        this.setProperty(KML_STRING_PROPERTY, kmlString);
        this.geoDocument.setDocumentMIMEType("application/vnd.google-earth.kml+xml");

        setDocumentFields(empKMLParser);
        processParseOutput(empKMLParser);
    }

    /**
     * This constructor creates a KML feature from the KML obtained from input stream. The caller is responsible for closing the stream.
     * @param inputStream The open input stream.
     * @throws IllegalArgumentException This exception is raised if the stream is null.
     * @throws XmlPullParserException This exception is raised if the KML fails to parse correctly.
     * @throws IOException This exception is raised in the event of an IO error accessing the stream.
     */
    public KML(final InputStream inputStream) throws XmlPullParserException, IOException {
        super(new GeoRenderable(), FeatureTypeEnum.KML);

        if (null == inputStream) {
            throw new IllegalArgumentException("Input stream can not be null.");
        }
        this.geoDocument = new GeoDocument();
        final EmpKMLParser empKMLParser;
        empKMLParser = new EmpKMLParser(inputStream);

        setDocumentFields(empKMLParser);
        processParseOutput(empKMLParser);

        final String kmlString = this.exportToKML();
        this.getProperties().put(KML_STRING_PROPERTY, kmlString);
        this.geoDocument.setDocumentMIMEType("application/vnd.google-earth.kml+xml");
    }

    /**
     * This constructor creates a KML feature from the KML obtained from the URL.
     * @param url A valid URL to a KML resource.
     * @throws IllegalArgumentException This exception is raised if the url is null.
     * @throws XmlPullParserException This exception is raised if the KML fails to parse correctly.
     * @throws IOException This exception is raised in the event of an IO error accessing the URL.
     */
    public KML(final java.net.URL url) throws XmlPullParserException, IOException {
        this(url, null);
    }

    /**
     * This constructor creates a KML feature from the KML obtained from the URL.
     * @param url A valid URL to a KML resource.
     * @param documentBase A valid filesystem path (e.g. where KMZ from adding of KML Service was exploded)
     * @throws IllegalArgumentException This exception is raised if the url is null.
     * @throws XmlPullParserException This exception is raised if the KML fails to parse correctly.
     * @throws IOException This exception is raised in the event of an IO error accessing the URL.
     */
    public KML(final java.net.URL url, final String documentBase) throws XmlPullParserException, IOException {
        super(new GeoRenderable(), FeatureTypeEnum.KML);

        if (null == url) {
            throw new IllegalArgumentException("Invalid parameters. URL can not be null.");
        }
        this.geoDocument = new GeoDocument();
        InputStream inputStream = null;
        final EmpKMLParser empKMLParser;

        try {
            inputStream = url.openStream();
            if((null == documentBase) || (0 == documentBase.length())) {
                empKMLParser = new EmpKMLParser(inputStream);
            } else {
                empKMLParser = new EmpKMLParser(inputStream, documentBase);
            }

            setDocumentFields(empKMLParser);
            processParseOutput(empKMLParser);

            this.geoDocument.setDocumentURI(url.toString());
            this.geoDocument.setDocumentMIMEType("application/vnd.google-earth.kml+xml");
        } finally {
            if (null != inputStream) {
                inputStream.close();
            }
        }
    }

    private void setDocumentFields(final EmpKMLParser empKmlParser) {
        if (null != empKmlParser.getDocumentId()) {
            this.setDataProviderId(empKmlParser.getDocumentId());
        }

        if (null != empKmlParser.getDocumentName()) {
            this.setName(empKmlParser.getDocumentName());
        }

        if (null != empKmlParser.getDocumentDescription()) {
            this.setDescription(empKmlParser.getDocumentDescription());
        }
    }

    private void processParseOutput(final EmpKMLParser empKmlParser) {
        this.featureList.addAll(empKmlParser.getVisibleFeatures());
        this.featureList.addAll(empKmlParser.getInvisibleFeatures());
        this.imageLayerList.addAll(empKmlParser.getImageLayers());
    }

    @Override
    public List<IFeature> getFeatureList() {
        return this.featureList;
    }

    @Override
    public List<IImageLayer> getImageLayerList() {
        return this.imageLayerList;
    }

    @Override
    public List<IFeature> findKMLId(final String kmlId) {
        final List<IFeature> resultList = new ArrayList<>();

        for (final IFeature feature: this.featureList) {
            if ((null != feature.getDataProviderId()) && feature.getDataProviderId().equals(kmlId)) {
                resultList.add(feature);
            }
        }

        return resultList;
    }

    @Override
    public void setDocumentURI(final String documentURI) {
        this.geoDocument.setDocumentURI(documentURI);
    }

    @Override
    public String getDocumentURI() {
        return this.geoDocument.getDocumentURI();
    }

    @Override
    public void setDocumentMIMEType(final String documentMIMEType) {
        this.geoDocument.setDocumentMIMEType(documentMIMEType);
    }

    @Override
    public String getDocumentMIMEType() {
        return this.geoDocument.getDocumentMIMEType();
    }

    @Override
    public HashMap<String, String> getProperties() {
        return this.geoDocument.getProperties();
    }

    @Override
    public boolean containsProperty(final String propertyName) {
        return (this.getProperties().containsKey(propertyName));
    }

    public IGeoDocument getGeoDocument() {
        return this.geoDocument;
    }

    @Override
    public String exportToKML() {
        return "";
    }

    /**
     * Convert all features to String, Converts details of the first feature on the list.
     * @return
     */
    @Override
    public String toString() {
        String str = getFeatureType().toString() + " ";
        if(null != getName()) {
            str += getName() + "\n";
        } else {
            str += "\n";
        }

        if(null != getImageLayerList() && getImageLayerList().size() > 0) {
            str += "Image Layer Count " + getImageLayerList().size() + "\n";
        }

        if(null != getFeatureList() && getFeatureList().size() > 0) {
            str += "Feature Count " + getFeatureList().size() + " " + getFeatureList().get(0).toString();
        }
        return str;
    }
}
