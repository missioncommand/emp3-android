package mil.emp3.api.utils.kml;

import android.util.Log;

import com.google.maps.android.kml.KmlContainer;
import com.google.maps.android.kml.KmlGeometry;
import com.google.maps.android.kml.KmlGroundOverlay;
import com.google.maps.android.kml.KmlLineString;
import com.google.maps.android.kml.KmlMultiGeometry;
import com.google.maps.android.kml.KmlParser;
import com.google.maps.android.kml.KmlPlacemark;
import com.google.maps.android.kml.KmlPoint;
import com.google.maps.android.kml.KmlPolygon;
import com.google.maps.android.kml.KmlStyle;

import org.cmapi.primitives.GeoFillStyle;
import org.cmapi.primitives.IGeoAltitudeMode;
import org.cmapi.primitives.IGeoColor;
import org.cmapi.primitives.IGeoFillStyle;
import org.cmapi.primitives.IGeoStrokeStyle;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import mil.emp3.api.ImageLayer;
import mil.emp3.api.Overlay;
import mil.emp3.api.Path;
import mil.emp3.api.Point;
import mil.emp3.api.Polygon;
import mil.emp3.api.interfaces.IContainer;
import mil.emp3.api.interfaces.IFeature;
import mil.emp3.api.interfaces.IImageLayer;
import mil.emp3.api.interfaces.IOverlay;
import mil.emp3.api.utils.EmpObjectHierarchyEntry;

/**
 * This class implements the EMP V3 KML parser. This initial implementation only parses KML documents.
 * Parsing of KML fragments is NOT supported.
 */

public class EmpKMLParser {
    static private String TAG = EmpKMLParser.class.getSimpleName();

    private EmpObjectHierarchyEntry rootEmpEntry;

    // This visibleFeatureList contains all the EMP features the KML translates into that are visible.
    private final List<IFeature> visibleFeatureList = new ArrayList<>();
    // This invisibleFeatureList contains all the EMP features the KML translates into that are not visible.
    private final List<IFeature> invisibleFeatureList = new ArrayList<>();

    // This list contains all the ground overlays defined in the KML document.
    private final List<IImageLayer> imageLayerList = new ArrayList<>();

    private String docId = null;
    private String documentName = null;
    private String documentDescription = null;
    private String documentBase = null;

    /**
     * This Constructor parses a KML string.
     * @param kmlString A valid KML string.
     * @throws XmlPullParserException This exception is raised if the KML fails to parse.
     * @throws IOException This exception is raised if it fails to read the string.
     */
    public EmpKMLParser(String kmlString) throws XmlPullParserException, IOException {
        XmlPullParser xmlPullParser;

        java.io.StringReader reader = new java.io.StringReader(kmlString);
        XmlPullParserFactory factory = XmlPullParserFactory.newInstance();

        factory.setNamespaceAware(true);
        xmlPullParser = factory.newPullParser();
        xmlPullParser.setInput(reader);

        this.parseKML(xmlPullParser);
    }

    /**
     * This constructor parses the KML from an input stream.
     * @param stream The input stream to read from.
     * @throws XmlPullParserException This exception is raised if the KML fails to parse.
     * @throws IOException This exception is raised if it fails to read the input stream.
     */
    public EmpKMLParser(InputStream stream) throws XmlPullParserException, IOException {
        this(stream, null);
    }

    /**
     * This constructor parses the KML from an input stream.
     * @param stream The input stream to read from.
     * @param documentBase A valid file system path (e.g directory where KMZ file was exploded).
     * @throws XmlPullParserException This exception is raised if the KML fails to parse.
     * @throws IOException This exception is raised if it fails to read the input stream.
     */
    public EmpKMLParser(InputStream stream, String documentBase) throws XmlPullParserException, IOException {
        XmlPullParser xmlPullParser;

        XmlPullParserFactory factory = XmlPullParserFactory.newInstance();

        factory.setNamespaceAware(true);
        xmlPullParser = factory.newPullParser();
        xmlPullParser.setInput(stream, null);

        this.documentBase = documentBase;
        this.parseKML(xmlPullParser);
    }

    private void parseKML(XmlPullParser xmlPullParser) throws XmlPullParserException, IOException{

        KmlParser kmlParser = new KmlParser(xmlPullParser);
        kmlParser.parseKml();

        if (!kmlParser.getContainers().isEmpty()) {
            this.rootEmpEntry = this.processContainer(null, kmlParser.getContainers().get(0));
        }
        //this.processPlacemarks(kmlParser.getPlacemarks(), kmlParser.getStyleMaps(), kmlParser.getStyles());
        //this.processGroundOverlays(kmlParser.getGroundOverlays(), kmlParser.getStyleMaps(), kmlParser.getStyles());

        if (null != this.rootEmpEntry) {
            IContainer rootContainer = this.rootEmpEntry.getEmpObject();
            this.docId = rootContainer.getDataProviderId();
            this.documentName = rootContainer.getName();
            this.documentDescription = rootContainer.getDescription();
        }
    }

    /**
     * This method returns the list of visible features generated from the KML document.
     * @return The list of visible features.
     */
    public List<IFeature> getVisibleFeatures() {
        return this.visibleFeatureList;
    }

    /**
     * This method returns the list of features generated from the KML document that are mark not visible.
     * @return The list of not visible features.
     */
    public List<IFeature> getInvisibleFeatures() {
        return this.invisibleFeatureList;
    }

    /**
     * This method returns the list of image layers generated from the ground overlays in the KML document.
     * @return The list of image layers.
     */
    public List<IImageLayer> getImageLayers() {
        return this.imageLayerList;
    }

    private void processGroundOverlays(Map<KmlGroundOverlay, Object> groundOverlayMap) {
        for (KmlGroundOverlay groundOverlay: groundOverlayMap.keySet()) {
            try {
                IImageLayer imageLayer = new ImageLayer(groundOverlay.getImageUrl(), groundOverlay.getLatLngBox());
                this.imageLayerList.add(imageLayer);
            } catch (MalformedURLException e) {
                Log.e(TAG, "Failed to create ImageLayer.", e);
            }
        }
    }

    private EmpObjectHierarchyEntry createEMPOverlay(EmpObjectHierarchyEntry parentEntry, KmlContainer kmlContainer) {
        IOverlay overlay = new Overlay();
        EmpObjectHierarchyEntry objectEntry = new EmpObjectHierarchyEntry(parentEntry, overlay,
                kmlContainer.getStyleMap(), kmlContainer.getStyles());

        if ((null != kmlContainer.getContainerId()) && !kmlContainer.getContainerId().isEmpty()) {
            overlay.setDataProviderId(kmlContainer.getContainerId());
        }

        if (null != kmlContainer.getProperty("name")) {
            overlay.setName(kmlContainer.getProperty("name"));
        } else {
            overlay.setName("untitled KML " + (kmlContainer.isDocument()? "document": "layer"));
        }

        if (null != kmlContainer.getProperty("description")) {
            overlay.setDescription(kmlContainer.getProperty("description"));
        }

        return objectEntry;
    }

    private EmpObjectHierarchyEntry processContainer(EmpObjectHierarchyEntry parentEntry,
            KmlContainer kmlContainer) {
        EmpObjectHierarchyEntry containerEntry;

        if ((null == parentEntry) || parentEntry.isOverlayEntry()) {
            containerEntry = createEMPOverlay(parentEntry, kmlContainer);
        } else {
            containerEntry = parentEntry;
        }

        for (KmlContainer childKMLContainer: kmlContainer.getContainerList()) {
            processContainer(containerEntry, childKMLContainer);
        }

        for (KmlPlacemark kmlPlacemark: kmlContainer.getPlacemarkList().keySet()) {
            this.createEMPFeatures(containerEntry, kmlPlacemark);
        }

        this.processGroundOverlays(kmlContainer.getGroundOverlayHashMap());

        return containerEntry;
    }

    private void createEMPFeature(EmpObjectHierarchyEntry parentEntry,
            String placemarkId,
            KmlGeometry geometry,
            KmlStyle kmlStyle,
            Map<String, String> properties) {
        IFeature newFeature = null;
        String name = (properties.containsKey("name")? properties.get("name"): null);

        if (null == geometry) {
            return;
        }
        switch (geometry.getGeometryType()) {
            case "Point": {
                Point newPoint = new Point();
                KmlPoint kmlPoint = (KmlPoint) geometry;

                if (null == name) {
                    name = "KML Point";
                }

                if ((null != kmlStyle) && (null != kmlStyle.getIconUrl())) {
                    if(null == documentBase) {
                        newPoint.setIconURI(kmlStyle.getIconUrl());
                    } else {
                        // documentBase is set by the KMLSProvider when a KMZ file is exploded and stored on local file system.
                        // If the IconUrl is already a well formed URL then don't touch it. Otherwise it is probably a relative
                        // path from the KMZ file, so create a file URL for the path.
                        try {
                            new URL(kmlStyle.getIconUrl());   // This is done to check validity of URL, don't remove, we process the exception.
                            newPoint.setIconURI(kmlStyle.getIconUrl());
                        } catch (MalformedURLException e) {
                            try {
                                String fullPath = documentBase + File.separator + kmlStyle.getIconUrl().toString();
                                File file = new File(fullPath);
                                newPoint.setIconURI(file.toURI().toURL().toString());
                            } catch (MalformedURLException em) {
                                Log.e(TAG, "createEMPFeature ", e);
                            }
                        }

                    }
                }

                newPoint.setPosition(kmlPoint.getGeometryObject());
                newFeature = newPoint;
                break;
            }
            case "LineString": {
                IGeoColor color;
                Path line = new Path();
                KmlLineString kmlLine = (KmlLineString) geometry;

                if (null == name) {
                    name = "KML LineString";
                }

                if (null != kmlStyle) {
                    IGeoStrokeStyle strokeStyle = line.getStrokeStyle();
                    color = strokeStyle.getStrokeColor();
                    IGeoColor kmlColor = kmlStyle.getStrokeColor();
                    if (kmlColor != null) {
                        color.setAlpha(kmlColor.getAlpha());
                        color.setRed(kmlColor.getRed());
                        color.setGreen(kmlColor.getGreen());
                        color.setBlue(kmlColor.getBlue());
                    }
                    strokeStyle.setStrokeWidth(kmlStyle.getStrokeWidth());
                }

                line.getPositions().addAll(kmlLine.getGeometryObject());
                newFeature = line;
                break;
            }
            case "Polygon": {
                IGeoColor color;
                Polygon newPolygon = new Polygon();
                KmlPolygon kmlPolygon = (KmlPolygon) geometry;

                if (null == name) {
                    name = "KML Polygon";
                }

                if ((null != kmlStyle) && kmlStyle.hasFill() && (null != kmlStyle.getFillColor())) {
                    IGeoFillStyle fillStyle = ((null == newPolygon.getFillStyle())? new GeoFillStyle(): newPolygon.getFillStyle());
                    color = fillStyle.getFillColor();
                    color.setAlpha(kmlStyle.getFillColor().getAlpha());
                    color.setRed(kmlStyle.getFillColor().getRed());
                    color.setGreen(kmlStyle.getFillColor().getGreen());
                    color.setBlue(kmlStyle.getFillColor().getBlue());
                    newPolygon.setFillStyle(fillStyle);
                } else {
                    newPolygon.setFillStyle(null);
                }

                if (null != kmlStyle) {
                    IGeoStrokeStyle strokeStyle = newPolygon.getStrokeStyle();
                    color = strokeStyle.getStrokeColor();
                    IGeoColor kmlColor = kmlStyle.getStrokeColor();
                    if (kmlColor != null) {
                        color.setAlpha(kmlColor.getAlpha());
                        color.setRed(kmlColor.getRed());
                        color.setGreen(kmlColor.getGreen());
                        color.setBlue(kmlColor.getBlue());
                    }
                    strokeStyle.setStrokeWidth(kmlStyle.getStrokeWidth());
                }

                newPolygon.getPositions().addAll(kmlPolygon.getOuterBoundaryCoordinates());
                newFeature = newPolygon;
                break;
            }
            case "MultiGeometry": {
                KmlMultiGeometry multiGeometry = (KmlMultiGeometry) geometry;

                for (KmlGeometry childGeometry: multiGeometry.getGeometryObject()) {
                    this.createEMPFeature(parentEntry, placemarkId, childGeometry, kmlStyle, properties);
                }
                break;
            }
        }

        if (null != newFeature) {
            newFeature.setName(name);

            if (properties.containsKey("description")) {
                newFeature.setDescription(properties.get("description"));
            }

            if (properties.containsKey("extrude") && (properties.get("extrude").equals("1") || properties.get("extrude").toLowerCase().equals("false"))) {
                newFeature.setExtrude(true);
            }

            if (properties.containsKey("altitudeMode")) {
                switch (properties.get("altitudeMode").toLowerCase()) {
                    case "absolute":
                        newFeature.setAltitudeMode(IGeoAltitudeMode.AltitudeMode.ABSOLUTE);
                        break;
                    default:
                    case "clamptoground":
                        newFeature.setAltitudeMode(IGeoAltitudeMode.AltitudeMode.CLAMP_TO_GROUND);
                        break;
                    case "relativetoground":
                        newFeature.setAltitudeMode(IGeoAltitudeMode.AltitudeMode.RELATIVE_TO_GROUND);
                        break;
                }
            } else {
                newFeature.setAltitudeMode(IGeoAltitudeMode.AltitudeMode.CLAMP_TO_GROUND);
            }

            if (properties.containsKey("visibility") && (!properties.get("visibility").equals("1") && !properties.get("visibility").toLowerCase().equals("true"))) {
                this.invisibleFeatureList.add(newFeature);
            } else {
                this.visibleFeatureList.add(newFeature);
            }

            if (null != placemarkId) {
                newFeature.setDataProviderId(placemarkId);
            }
        }
    }

    private void createEMPFeatures(EmpObjectHierarchyEntry parentEntry,
            KmlPlacemark kmlPlacemark) {

        KmlGeometry geometry = kmlPlacemark.getGeometry();
        KmlStyle kmlStyle = kmlPlacemark.getInlineStyle();

        EmpObjectHierarchyEntry containerEntry = parentEntry;
        while (null == kmlStyle && containerEntry != null) {
            Map<String, String> styleMap = containerEntry.getStyleMap();
            Map<String, KmlStyle> styles = containerEntry.getStyles();
            String styleRef = kmlPlacemark.getStyleId();
            kmlStyle = styles.get(styleRef);
            if (null == kmlStyle && styleMap.containsKey(styleRef)) {
                String styleId = styleMap.get(styleRef);
                if (styles.containsKey(styleId)) {
                    kmlStyle = styles.get(styleId);
                }
            }
            containerEntry = containerEntry.getParent();
        }

        this.createEMPFeature(parentEntry, kmlPlacemark.getPlacemarkId(), geometry, kmlStyle, kmlPlacemark.getProperties());
    }

    public String getDocumentId() {
        return this.docId;
    }

    public String getDocumentName() {
        return this.documentName;
    }

    public String getDocumentDescription() {
        return this.documentDescription;
    }
}
