package mil.emp3.api.view.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.graphics.Rect;

import java.lang.ref.WeakReference;

import mil.emp3.mapengine.interfaces.IEmpImageInfo;

/**
 * This is the base class for all EMP resource icons
 */
public class EmpResourceImageInfo implements IEmpImageInfo {
    private Rect oBounds;
    private WeakReference<Bitmap> bitmapRef = null;
    private final BitmapFactory.Options oOptions = new BitmapFactory.Options();

    private final android.content.res.Resources contextResources;
    private final String sImageKey;
    private final int resourceId;

    protected EmpResourceImageInfo(android.content.res.Resources resources, int resId) {
        this.contextResources = resources;
        this.resourceId = resId;
        this.sImageKey = resId + "";
    }

    private void setValues() {
        if ((this.bitmapRef == null) || (this.bitmapRef.get() == null)) {
            synchronized (this) {
                if ((this.bitmapRef == null) || (this.bitmapRef.get() == null)) {
                    this.bitmapRef = new WeakReference(BitmapFactory.decodeResource(this.contextResources, this.resourceId, this.oOptions));
                    this.oBounds = new Rect(0, 0, this.oOptions.outWidth, this.oOptions.outHeight);
                }
            }
        }
    }

    @Override
    public Point getCenterPoint() {
        this.setValues();
        return null;
    }

    @Override
    public Bitmap getImage() {
        this.setValues();
        return this.bitmapRef.get();
    }

    @Override
    public Rect getImageBounds() {
        this.setValues();
        return this.oBounds;
    }

    @Override
    public Rect getSymbolBounds() {
        this.setValues();
        return this.oBounds;
    }

    @Override
    public String getImageKey() {
        this.setValues();
        return this.sImageKey;
    }

    @Override
    public boolean isFarImageIcon() {
        return false;
    }

    @Override
    public void setFarImageIcon() {

    }

    @Override
    public long getImageSize() {
        this.setValues();
        return this.bitmapRef.get().getByteCount();
    }

    @Override
    public int getResourceId() {
        return this.resourceId;
    }
}
