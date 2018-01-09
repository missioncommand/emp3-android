package mil.emp3.worldwind.feature;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Looper;
import android.util.SparseArray;

import org.cmapi.primitives.IGeoPosition;

import armyc2.c2sd.renderer.utilities.ImageInfo;
import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.geom.Offset;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.render.ImageSource;
import gov.nasa.worldwind.render.RenderContext;
import gov.nasa.worldwind.shape.Placemark;
import gov.nasa.worldwind.shape.PlacemarkAttributes;
import gov.nasa.worldwind.util.Logger;
import mil.emp3.api.MilStdSymbol;
import mil.emp3.mapengine.interfaces.IMilStdRenderer;
import mil.emp3.worldwind.MapInstance;
import mil.emp3.worldwind.feature.support.MilStd2525;
import mil.emp3.worldwind.feature.support.MilStd2525LevelOfDetailSelector;

/**
 * This class implements the milstd 2525 single point icons in the world wind map engine.
 */
public class MilStd2525SinglePoint extends FeatureRenderableMapping<MilStdSymbol> {
    private static final String TAG = MilStd2525SinglePoint.class.getSimpleName();

    public class EMPPlacemark extends Placemark {
        public final MilStd2525SinglePoint featureMapper;

        protected EMPPlacemark(MilStd2525SinglePoint mapper, Position position) {
            super(position);
            this.featureMapper = mapper;
        }
    }
    private final IMilStdRenderer oRenderer;
    private String sSymbolCode;
    private SparseArray oModifiers = null;
    private SparseArray oAttributes = null;
    private int iLastLevelOfDetail = -1;
    private final Placemark placemark;
    private PlacemarkAttributes highPlacemarkAttributes = null;
    private PlacemarkAttributes midPlacemarkAttributes = null;

    public MilStd2525SinglePoint(MapInstance mapInstance, IMilStdRenderer iconRenderer, Position position, MilStdSymbol symbol) {
        super(symbol, mapInstance);

        this.placemark = new EMPPlacemark(this, position);
        this.sSymbolCode = symbol.getSymbolCode();
        this.oRenderer = iconRenderer;
        this.setSymbolAttributes();
        this.setSymbolModifiers();
        placemark.setPickDelegate(symbol);
        switch (symbol.getAltitudeMode()) {
            case RELATIVE_TO_GROUND:
                placemark.setAltitudeMode(WorldWind.RELATIVE_TO_GROUND);
                break;
            case ABSOLUTE:
                placemark.setAltitudeMode(WorldWind.ABSOLUTE);
                break;
            case CLAMP_TO_GROUND:
                placemark.setAltitudeMode(WorldWind.CLAMP_TO_GROUND);
                break;
        }
        placemark.setLevelOfDetailSelector(MilStd2525LevelOfDetailSelector.getInstance());

        this.setRenderable(placemark);
    }

    @Override
    public void render(RenderContext renderContext) {
        this.placemark.render(renderContext);
    }

    private void setSymbolModifiers() {
        this.oModifiers = this.oRenderer.getUnitModifiers(this.getMapInstance(), this.getFeature());
    }

    private void setSymbolAttributes() {
        this.oAttributes = this.oRenderer.getAttributes(this.getMapInstance(), this.getFeature(), this.isSelected());
        this.getSymbol().setSymbolAttributes(this.oAttributes);
    }

    public SparseArray getSymbolModifiers() {
        return this.oModifiers;
    }

    public SparseArray getSymbolAttributes() {
        return this.oAttributes;
    }

    public void resetDirty() {
        this.setDirty(false);
    }

    public void setHighPlacemarkAttributes(final PlacemarkAttributes pma) {
        highPlacemarkAttributes = pma;
    }

    public void setMidPlacemarkAttributes(final PlacemarkAttributes pma) {
        midPlacemarkAttributes = pma;
    }

    public PlacemarkAttributes getHighPlacemarkAttributes() {
        return highPlacemarkAttributes;
    }

    public PlacemarkAttributes getMidPlacemarkAttributes() {
        return midPlacemarkAttributes;
    }

    /**
     * This method updates the feature and set it dirty if it requires the renderer to be called.
     * @param symbol
     */
    public void updateSymbol(MilStdSymbol symbol) {
        if (this.getFeature() != symbol) {
            this.setFeature(symbol);
            this.setDirty(false);
        }

        // Without the following position updates via a call to Feature.apply will not work.
        // Should we comparing old position vs new position?
        IGeoPosition oPos = this.getFeature().getPosition();
        this.placemark.getPosition().set(oPos.getLatitude(), oPos.getLongitude(), oPos.getAltitude());

        if (this.sSymbolCode != symbol.getSymbolCode()) {
            // if the symbol code has changed it is dirty.
            this.setDirty(true);
            this.sSymbolCode = symbol.getSymbolCode();
        }

        SparseArray oMod = this.oRenderer.getUnitModifiers(this.getMapInstance(), this.getFeature());
        SparseArray oAttr = this.oRenderer.getAttributes(this.getMapInstance(), this.getFeature(), isSelected());

        if (!this.oModifiers.toString().equals(oMod.toString())) {
            // The modifiers have changed so we mark it dirty.
            this.setDirty(true);
            this.oModifiers = oMod;
        }

        if (!this.oAttributes.toString().equals(oAttr.toString())) {
            // The attributes has changed, mark dirty.
            this.setDirty(true);
            this.oAttributes = oAttr;
        }
    }

    public String getSymbolCode() {
        return this.getFeature().getSymbolCode();
    }

    public MilStdSymbol getSymbol() {
        return this.getFeature();
    }

    public int getLastLevelOfDetail() {
        return this.iLastLevelOfDetail;
    }

    public void setLastLevelOfDetail(int iLevel) {
        this.iLastLevelOfDetail = iLevel;
    }

    public double getIconScale() {
        double scale = this.getFeature().getIconScale();

        if (this.isSelected()) {
            scale = scale * oRenderer.getSelectedIconScale(this.getMapInstance());
        }

        return scale;
    }

    /**
     * Update attributes based on selected flag. This will get picked up on the next render call.
     * @param selected
     */
    @Override
    public void setSelected(boolean selected) {
        super.setSelected(selected);
        setSymbolAttributes();
        setSymbolModifiers();
    }

    /**
     * This ImageSource.BitmapFactory implementation creates MIL-STD-2525 bitmaps for use with MilStd2525Placemark.
     */
    public static class SymbolBitmapFactory implements ImageSource.BitmapFactory {

        private final String symbolCode;

        private final SparseArray<String> modifiers;

        private final SparseArray<String> attributes;

        private final PlacemarkAttributes placemarkAttributes;

        /**
         * The image to use when the renderer cannot render an image.
         */
        private static Bitmap defaultImage = BitmapFactory.decodeResource(Resources.getSystem(), android.R.drawable.ic_dialog_alert); // Warning triangle

        /**
         * The handler used to schedule runnable to be executed on the main thread.
         */
        private static Handler mainLoopHandler = new Handler(Looper.getMainLooper());

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
