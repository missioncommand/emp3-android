package mil.emp3.core.utils.milstd2525;

import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.Rect;

import java.lang.ref.WeakReference;

import mil.emp3.mapengine.interfaces.IEmpImageInfo;

/**
 *
 */
public class EmpImageInfo implements IEmpImageInfo {
    private final String sImageKey;
    private final android.graphics.Point oPoint;
    private final android.graphics.Rect oImageBounds;
    private final android.graphics.Rect oSymbolBounds;
    private WeakReference<Bitmap> oBitmapReference;

    // Following members were created for us in Bitmap Cache Management. Currently they are used in AdaptiveBitmapCache
    private boolean farImageIcon;                         // This is true if Bitmap is for Far Image Icon. This is related to Mid/Far Thresholds
    private int imageSize;                                // This is size of the image set at create time.

    public EmpImageInfo(String sKey,
            android.graphics.Point point,
            android.graphics.Rect imageBounds,
            android.graphics.Rect symbolBounds,
            android.graphics.Bitmap bitmap
    ) {
        this.sImageKey = sKey;
        this.oPoint = point;
        this.oImageBounds = imageBounds;
        this.oSymbolBounds = symbolBounds;
        this.oBitmapReference = new WeakReference<Bitmap>(bitmap);
        this.farImageIcon = false;
        this.imageSize = bitmap.getAllocationByteCount();
    }

    @Override
    public Point getCenterPoint() {
        return this.oPoint;
    }

    @Override
    public Bitmap getImage() {
        return this.oBitmapReference.get();
    }

    @Override
    public Rect getImageBounds() {
        return this.oImageBounds;
    }

    @Override
    public Rect getSymbolBounds() {
        return this.oSymbolBounds;
    }

    @Override
    public String getImageKey() {
        return this.sImageKey;
    }

    @Override
    public boolean isFarImageIcon() {
        return farImageIcon;
    }

    @Override
    public void setFarImageIcon() {
        farImageIcon = true;
    }

    @Override
    public long getImageSize() {return imageSize; }
    public int getResourceId() {
        return 0;
    }
}
