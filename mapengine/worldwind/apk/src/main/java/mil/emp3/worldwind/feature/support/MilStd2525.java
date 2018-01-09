/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package mil.emp3.worldwind.feature.support;

import android.util.SparseArray;

import java.util.HashMap;

import armyc2.c2sd.renderer.utilities.ImageInfo;
import gov.nasa.worldwind.render.ImageSource;
import gov.nasa.worldwind.shape.PlacemarkAttributes;
import mil.emp3.mapengine.interfaces.IEmpImageInfo;
import mil.emp3.mapengine.interfaces.IMilStdRenderer;
import mil.emp3.worldwind.feature.MilStd2525SinglePoint;

/**
 * This utility class generates PlacemarkAttributes bundles with MIL-STD-2525 symbols. It was copied from worldwind-examples and modified as
 * we have our own wrapper and initialization of MilStd Renderer. The renderImage method was modified to use EMP provided MilStd wrapper.
 */
public class MilStd2525 {

    /**
     * The actual rendering engine for the MIL-STD-2525 graphics.
     */
    // private static MilStdIconRenderer renderer = MilStdIconRenderer.getInstance();
    private static IMilStdRenderer renderer;

    /**
     * A cache of PlacemarkAttribute bundles containing MIL-STD-2525 symbols. Using a cache is essential for memory
     * management: we want to share the bitmap textures for identical symbols.  The cache maintains weak references to
     * the attribute bundles so that the garbage collector can reclaim the memory when a Placemark releases an attribute
     * bundle, for instance when it changes its level-of-detail.
     */
    private static HashMap<String, PlacemarkAttributes> simpleDotPlacemarks = new HashMap<>();

    private final static double MINIMUM_IMAGE_SCALE = 0.25;

    public static void setRenderer(IMilStdRenderer renderer) {
        MilStd2525.renderer = renderer;
    }

    /**
     * Gets a PlacemarkAttributes bundle for the supplied symbol specification. The attribute bundle is retrieved from a
     * cache. If the symbol is not found in the cache, an attribute bundle is created and added to the cache before it
     * is returned.
     *
     * @param milStdPlacemark
     *
     * @return Either a new or a cached PlacemarkAttributes bundle containing the specified symbol embedded in the
     * bundle's imageSource property.
     */
    public static PlacemarkAttributes getPlacemarkAttributes(MilStd2525SinglePoint milStdPlacemark) {


        SparseArray<String> modifiers = null;
        String geoId = milStdPlacemark.getFeature().getGeoId().toString();
        PlacemarkAttributes placemarkAttributes = null;
        if (milStdPlacemark.getLastLevelOfDetail() == MilStd2525LevelOfDetailSelector.MEDIUM_LEVEL_OF_DETAIL) {
            placemarkAttributes = milStdPlacemark.getMidPlacemarkAttributes();
        } else {
            modifiers = milStdPlacemark.getSymbolModifiers();
            placemarkAttributes = milStdPlacemark.getHighPlacemarkAttributes();
        }

        // Create the attributes if they haven't been created yet or if they've been released
        if (placemarkAttributes == null || milStdPlacemark.isDirty()) {

            // Create the attributes bundle and add it to the cache.
            // The actual bitmap will be lazily (re)created using a factory.
            placemarkAttributes = MilStd2525.createPlacemarkAttributes(milStdPlacemark.getSymbolCode(),
                    modifiers,
                    milStdPlacemark.getSymbolAttributes());
            if (placemarkAttributes == null) {
                throw new IllegalArgumentException("Cannot generate a symbol for: " + geoId);
            }

            if (modifiers == null) {
                milStdPlacemark.setMidPlacemarkAttributes(placemarkAttributes);
            } else {
                milStdPlacemark.setHighPlacemarkAttributes(placemarkAttributes);
            }

            // Perform some initialization of the bundle conducive to eye distance scaling
            placemarkAttributes.setMinimumImageScale(MINIMUM_IMAGE_SCALE);
        }

        return placemarkAttributes;
    }

    /**
     * Gets a PlacemarkAttributes bundle for the supplied symbol specification. The attribute bundle is retrieved from a
     * cache. If the symbol is not found in the cache, an attribute bundle is created and added to the cache before it
     * is returned.
     *
     * @param simpleCode
     *
     * @return Either a new or a cached PlacemarkAttributes bundle containing the specified symbol embedded in the
     * bundle's imageSource property.
     */
    public static PlacemarkAttributes getPlacemarkAttributes(String simpleCode) {


        PlacemarkAttributes placemarkAttributes = simpleDotPlacemarks.get(simpleCode);

        // Create the attributes if they haven't been created yet or if they've been released
        if (placemarkAttributes == null) {

            // Create the attributes bundle and add it to the cache.
            // The actual bitmap will be lazily (re)created using a factory.
            placemarkAttributes = MilStd2525.createPlacemarkAttributes(simpleCode,null, null);
            if (placemarkAttributes == null) {
                throw new IllegalArgumentException("Cannot generate a symbol for: " + simpleCode);
            }
            // Add a weak reference to the attribute bundle to our cache
            simpleDotPlacemarks.put(simpleCode, placemarkAttributes);

            // Perform some initialization of the bundle conducive to eye distance scaling
            placemarkAttributes.setMinimumImageScale(MINIMUM_IMAGE_SCALE);
        }

        return placemarkAttributes;
    }
    /** Called from mapInstance to remove placemarks when the corresponding
     * feature is removed
     * @param geoId
     */

    public static void removePlacemarkAttributes(final String geoId) {
    }

    /**
     * Creates a placemark attributes bundle containing a MIL-STD-2525 symbol using the specified modifiers and
     * attributes.  The ImageSource bitmap is lazily created via an ImageSource.Bitmap factory. The call to the
     * factory's createBitmap method made when Placemark comes into view; it's also used to recreate the bitmap if the
     * resource was evicted from the World Wind render resource cache.
     *
     * @param symbolCode The 15-character SIDC (symbol identification coding scheme) code.
     * @param modifiers  The ModifierUnit (unit) or ModifierTG (tactical graphic) modifiers collection. May be null.
     * @param attributes The MilStdAttributes attributes collection. May be null.
     *
     * @return A new PlacemarkAttributes bundle representing the Mbtw IL-STD-2525 symbol.
     */
    public static PlacemarkAttributes createPlacemarkAttributes(String symbolCode, SparseArray<String> modifiers, SparseArray<String> attributes) {
        PlacemarkAttributes placemarkAttributes = new PlacemarkAttributes();

        // Create a BitmapFactory instance with the values needed to create and recreate the symbol's bitmap
        MilStd2525SinglePoint.SymbolBitmapFactory factory = new MilStd2525SinglePoint.SymbolBitmapFactory(symbolCode, modifiers, attributes, placemarkAttributes);
        placemarkAttributes.setImageSource(ImageSource.fromBitmapFactory(factory));

        return placemarkAttributes;
    }

    /**
     * Creates an MIL-STD-2525 symbol from the specified symbol code, modifiers and attributes.
     *
     * @param symbolCode The MIL-STD-2525 symbol code.
     * @param modifiers  The MIL-STD-2525 modifiers. If null, a default (empty) modifier list will be used.
     * @param attributes The MIL-STD-2525 attributes. If null, a default (empty) attribute list will be used.
     *
     * @return An ImageInfo object containing the symbol's bitmap and meta data; may be null
     */

    public static ImageInfo renderImage(String symbolCode, SparseArray<String> modifiers, SparseArray<String> attributes) {
        if(null == renderer) {
            throw new IllegalStateException("MilStd2525 renderer is not set");
        }

        IEmpImageInfo empImageInfo = renderer.getMilStdIcon(symbolCode, modifiers, attributes);
        if(null != empImageInfo) {
            ImageInfo imageInfo = new ImageInfo(empImageInfo.getImage(), empImageInfo.getCenterPoint(), empImageInfo.getSymbolBounds());
            return imageInfo;
        } else {
            return null;
        }
    }
}
