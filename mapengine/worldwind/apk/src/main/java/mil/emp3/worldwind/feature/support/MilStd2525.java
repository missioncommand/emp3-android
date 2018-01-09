/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package mil.emp3.worldwind.feature.support;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Looper;
import android.util.SparseArray;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.UUID;

import armyc2.c2sd.renderer.utilities.ImageInfo;
import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.geom.Offset;
import gov.nasa.worldwind.render.ImageSource;
import gov.nasa.worldwind.shape.PlacemarkAttributes;
import gov.nasa.worldwind.util.Logger;
import mil.emp3.mapengine.interfaces.IEmpImageInfo;
import mil.emp3.mapengine.interfaces.IMilStdRenderer;
import mil.emp3.worldwind.feature.MilStd2525SinglePoint;

/**
 * This utility class generates PlacemarkAttributes bundles with MIL-STD-2525 symbols. It was copied from worldwind-examples and modified as
 * we have our own wrapper and initialization of MilStd Renderer. The renderImage method was modified to use EMP provided MilStd wrapper.
 */
public class MilStd2525 {

    /**
     * The image to use when the renderer cannot render an image.
     */
    private static Bitmap defaultImage = BitmapFactory.decodeResource(Resources.getSystem(), android.R.drawable.ic_dialog_alert); // Warning triangle

    /**
     * The actual rendering engine for the MIL-STD-2525 graphics.
     */
    // private static MilStdIconRenderer renderer = MilStdIconRenderer.getInstance();
    private static IMilStdRenderer renderer;

    /**
     * The handler used to schedule runnable to be executed on the main thread.
     */
    private static Handler mainLoopHandler = new Handler(Looper.getMainLooper());

    /**
     * A cache of PlacemarkAttribute bundles containing MIL-STD-2525 symbols. Using a cache is essential for memory
     * management: we want to share the bitmap textures for identical symbols.  The cache maintains weak references to
     * the attribute bundles so that the garbage collector can reclaim the memory when a Placemark releases an attribute
     * bundle, for instance when it changes its level-of-detail.
     */
    private static HashMap<String, WeakReference<PlacemarkAttributes>> symbolCache = new HashMap<>();

    private static SparseArray<String> emptyArray = new SparseArray<>();    // may be used in a cache key

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
        if (milStdPlacemark.getLastLevelOfDetail() == MilStd2525LevelOfDetailSelector.MEDIUM_LEVEL_OF_DETAIL) {
            geoId += "ATTR";
        } else {
            modifiers = milStdPlacemark.getSymbolModifiers();
        }
        // Look for an attribute bundle in our cache and determine if the cached reference is valid
        WeakReference<PlacemarkAttributes> reference = symbolCache.get(geoId);
        PlacemarkAttributes placemarkAttributes = (reference == null ? null : reference.get());

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
            // Add a weak reference to the attribute bundle to our cache
            symbolCache.put(geoId, new WeakReference<>(placemarkAttributes));

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


        // Look for an attribute bundle in our cache and determine if the cached reference is valid
        WeakReference<PlacemarkAttributes> reference = symbolCache.get(simpleCode);
        PlacemarkAttributes placemarkAttributes = (reference == null ? null : reference.get());

        // Create the attributes if they haven't been created yet or if they've been released
        if (placemarkAttributes == null) {

            // Create the attributes bundle and add it to the cache.
            // The actual bitmap will be lazily (re)created using a factory.
            placemarkAttributes = MilStd2525.createPlacemarkAttributes(simpleCode,null, null);
            if (placemarkAttributes == null) {
                throw new IllegalArgumentException("Cannot generate a symbol for: " + simpleCode);
            }
            // Add a weak reference to the attribute bundle to our cache
            symbolCache.put(simpleCode, new WeakReference<>(placemarkAttributes));

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
        symbolCache.remove(geoId);
        symbolCache.remove(geoId + "ATTR");
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
        SymbolBitmapFactory factory = new SymbolBitmapFactory(symbolCode, modifiers, attributes, placemarkAttributes);
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


    /**
     * This ImageSource.BitmapFactory implementation creates MIL-STD-2525 bitmaps for use with MilStd2525Placemark.
     */
    protected static class SymbolBitmapFactory implements ImageSource.BitmapFactory {

        private final String symbolCode;

        private final SparseArray<String> modifiers;

        private final SparseArray<String> attributes;

        private final PlacemarkAttributes placemarkAttributes;

        /**
         * Constructs a SymbolBitmapFactory instance capable of creating a bitmap with the given code, modifiers and
         * attributes. The createBitmap() method will return a new instance of a bitmap and will also update the
         * associated placemarkAttributes bundle's imageOffset property based on the size of the new bitmap.
         *
         * @param symbolCode          SIDC code
         * @param modifiers           Unit modifiers to be copied; null is permitted
         * @param attributes          Rendering attributes to be copied; null is permitted
         * @param placemarkAttributes Placemark attribute bundle associated with this factory
         */
        public SymbolBitmapFactory(String symbolCode, SparseArray<String> modifiers, SparseArray<String> attributes, PlacemarkAttributes placemarkAttributes) {
            // Capture the values needed to (re)create the symbol bitmap
            this.symbolCode = symbolCode;
            this.modifiers = modifiers;
            this.attributes = attributes;
            // The MilStd2525.symbolCache maintains a WeakReference to the placemark attributes. The finalizer is able to
            // resolve the circular dependency between the PlacemarkAttributes->ImageSource->Factory->PlacemarkAttributes
            // and garbage collect the attributes a Placemark releases its attribute bundle (e.g., when switching
            // between levels-of-detail)
            this.placemarkAttributes = placemarkAttributes;
        }

        /**
         * Returns the MIL-STD-2525 bitmap and updates the PlacemarkAttributes associated with this factory instance.
         *
         * @return a new bitmap rendered from the parameters given in the constructor; may be null
         */
        @Override
        public Bitmap createBitmap() {
            // Create the symbol's bitmap
            ImageInfo imageInfo = MilStd2525.renderImage(this.symbolCode, this.modifiers, this.attributes);
            if (imageInfo == null) {
                Logger.logMessage(Logger.ERROR, "MilStd2525", "createBitmap", "Failed to render image for " + this.symbolCode);
                // TODO: File JIRA issue - must return a valid bitmap, else the ImageRetriever repeatedly attempts to create the bitmap.
                return defaultImage;
            }

            // Apply the computed image offset after the renderer has created the image. This is essential for proper
            // placement as the offset may change depending on the level of detail, for instance, the absence or
            // presence of text modifiers.
            Point centerPoint = imageInfo.getCenterPoint(); // The center of the core symbol
            Rect bounds = imageInfo.getImageBounds();       // The extents of the image, including text modifiers
            final Offset placemarkOffset = new Offset(
                WorldWind.OFFSET_PIXELS, centerPoint.x, // x offset
                WorldWind.OFFSET_PIXELS, bounds.height() - centerPoint.y); // y offset converted to lower-left origin

            // Apply the placemark offset to the attributes on the main thread. This is necessary to synchronize write
            // access to placemarkAttributes from the thread that invokes this BitmapFactory and read access from the
            // main thread.
            mainLoopHandler.post(new Runnable() {
                @Override
                public void run() {
                    placemarkAttributes.setImageOffset(placemarkOffset);
                }
            });

            // Return the bitmap
            return imageInfo.getImage();
        }

    }
}
