package mil.emp3.mapengine.interfaces;

/**
 *
 */
public interface IEmpImageInfo {
    android.graphics.Point getCenterPoint();
    android.graphics.Bitmap getImage();
    android.graphics.Rect getImageBounds();
    android.graphics.Rect getSymbolBounds();
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
    int getResourceId();
}
