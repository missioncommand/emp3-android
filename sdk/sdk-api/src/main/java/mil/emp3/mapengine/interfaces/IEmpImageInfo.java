package mil.emp3.mapengine.interfaces;

/**
 * An object that holds properties of the Image object created by Mil Std Renderer.
 */
public interface IEmpImageInfo {
    /**
     * Gets the center of the image
     * @return android point
     */
    android.graphics.Point getCenterPoint();

    /**
     * Gets the bitmap of the image
     * @return android bitmap
     */
    android.graphics.Bitmap getImage();

    /**
     * This method gets the images's viewing area.
     * @return android rectangle object
     */
    android.graphics.Rect getImageBounds();

    /**
     * This method gets the symbol's viewing area.
     * @return android rectangle object
     */
    android.graphics.Rect getSymbolBounds();

    /**
     * Gets the resource id of the image as a string.
     * @return string
     */
    String getImageKey();

    /**
     * Returns true if the Image is created for a Far Image Icon. This is related to MidDistance/FarDistance Thresholds.
     * Far Image icons are stored in a separate cache.
     * @return
     */
    boolean isFarImageIcon();

    /**
     * Marks this image as Far Image Icon. This is related to MidDistance/FarDistance Thresholds.
     * Far Image icons are stored in a separate cache.
     */
    void setFarImageIcon();

    /**
     * Gets the size of the image that was stored when Image was created. This is used to calculate size of the cache when
     * WeakReference BitMap is already make by the GC.
     * @return
     */
    long getImageSize();
    /**
     * Gets the resource id of the image.
     * @return integer
     */
    int getResourceId();
}
