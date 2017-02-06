package com.google.maps.android.kml;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents a KML Document or Folder.
 *
 * NOTE: This file has been modified from its initial content to account for Common Map Geospatial Notation.
 */
public class KmlContainer {

    private final Map<String, String> mProperties;

    private final Map<KmlPlacemark, Object> mPlacemarks;

    private final List<KmlContainer> mContainers;

    private final Map<KmlGroundOverlay, Object> mGroundOverlays;

    private final Map<String, String> mStyleMap;

    private Map<String, KmlStyle> mStyles;

    private String mContainerId;

    private boolean bDocument;

    /*package*/
    protected KmlContainer(Map<String, String> properties, Map<String, KmlStyle> styles,
            Map<KmlPlacemark, Object> placemarks, Map<String, String> styleMaps,
            List<KmlContainer> containers, Map<KmlGroundOverlay, Object> groundOverlay,
            boolean isDocument, String Id) {
    //KmlContainer(HashMap<String, String> properties, HashMap<String, KmlStyle> styles,
    //        HashMap<KmlPlacemark, Object> placemarks, HashMap<String, String> styleMaps,
    //        ArrayList<KmlContainer> containers, String Id) {
        mProperties = properties;
        mPlacemarks = placemarks;
        mStyles = styles;
        mStyleMap = styleMaps;
        mContainers = containers;
        mGroundOverlays = groundOverlay;
        mContainerId = Id;
        bDocument = isDocument;
    }

    public boolean isDocument() {
        return this.bDocument;
    }

    /**
     * @return Map of Kml Styles, with key values representing style name (ie, color) and
     * value representing style value (ie #FFFFFF)
     */
    /* package */
    public Map<String, KmlStyle> getStyles() {
        return mStyles;
    }

    /**
     * @param placemarks Placemark for the container to contain
     * @param object     Corresponding GoogleMap map object of the basic_placemark (if it has been
     *                   added
     *                   to the map)
     */
    /* package */
    void setPlacemark(KmlPlacemark placemarks, Object object) {
        mPlacemarks.put(placemarks, object);
    }

    /**
     * @return A map of strings representing a style map, null if no style maps exist
     */
    /* package */
    public Map<String, String> getStyleMap() {
        return mStyleMap;
    }

    /**
     * Gets all of the ground overlays which were set in the container
     *
     * @return A set of ground overlays
     */
    /* package */
    public Map<KmlGroundOverlay, Object> getGroundOverlayHashMap() {
        return mGroundOverlays;
    }

    /**
     * Gets the Container ID if it is specified
     *
     * @return Container ID or null if not set
     */
    public String getContainerId() {
        return mContainerId;
    }

    /**
     * Gets a style based on an ID
     */
    public KmlStyle getStyle(String styleID) {
        return mStyles.get(styleID);
    }

    /**
     * @return HashMap of containers
     */
    /*package*/
    public Map<KmlPlacemark, Object> getPlacemarksHashMap() {
        return mPlacemarks;
    }

    /**
     * Gets the value of a property based on the given key
     *
     * @param propertyName property key to find
     * @return value of property found, null if key doesn't exist
     */
    public String getProperty(String propertyName) {
        return mProperties.get(propertyName);
    }

    /**
     * Gets whether the container has any properties
     *
     * @return true if there are properties, false otherwise
     */
    public boolean hasProperties() {
        return mProperties.size() > 0;
    }

    /**
     * Gets whether the given key exists in the properties
     *
     * @param keyValue property key to find
     * @return true if key was found, false otherwise
     */
    public boolean hasProperty(String keyValue) {
        return mProperties.containsKey(keyValue);
    }

    /**
     * Gets whether the container has containers
     *
     * @return true if there are containers, false otherwise
     */
    public boolean hasContainers() {
        return mContainers.size() > 0;
    }

    /**
     * Gets an iterable of nested KmlContainers
     *
     * @return iterable of KmlContainers
     */
    public Iterable<KmlContainer> getContainers() {
        return mContainers;
    }

    public List<KmlContainer> getContainerList() {
        return this.mContainers;
    }

    /**
     * Gets an iterable of the properties hashmap entries
     *
     * @return iterable of the properties hashmap entries
     */
    public Iterable<String> getProperties() {
        return mProperties.keySet();
    }

    /**
     * Gets an iterable of KmlPlacemarks
     *
     * @return iterable of KmlPlacemarks
     */
    public Iterable<KmlPlacemark> getPlacemarks() {
        return mPlacemarks.keySet();
    }

    public Map<KmlPlacemark, Object> getPlacemarkList() {
        return mPlacemarks;
    }

    /**
     * Gets whether the container has any placemarks
     *
     * @return true if there are placemarks, false otherwise
     */
    public boolean hasPlacemarks() {
        return mPlacemarks.size() > 0;
    }

    /**
     * Gets an iterable of KmlGroundOverlay objects
     *
     * @return iterable of KmlGroundOverlay objects
     */
    //public Iterable<KmlGroundOverlay> getGroundOverlays() {
    //    return mGroundOverlays.keySet();
    //}

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Container").append("{");
        sb.append("\n properties=").append(mProperties);
        sb.append(",\n placemarks=").append(mPlacemarks);
        sb.append(",\n containers=").append(mContainers);
        sb.append(",\n ground overlays=").append(mGroundOverlays);
        sb.append(",\n style maps=").append(mStyleMap);
        sb.append(",\n styles=").append(mStyles);
        sb.append("\n}\n");
        return sb.toString();
    }
}
