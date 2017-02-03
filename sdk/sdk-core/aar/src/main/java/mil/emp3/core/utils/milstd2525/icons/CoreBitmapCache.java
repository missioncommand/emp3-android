package mil.emp3.core.utils.milstd2525.icons;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.SparseArray;

import armyc2.c2sd.renderer.MilStdIconRenderer;
import armyc2.c2sd.renderer.utilities.ImageInfo;
import armyc2.c2sd.renderer.utilities.SymbolUtilities;
import mil.emp3.api.interfaces.IMap;
import mil.emp3.core.utils.milstd2525.EmpImageInfo;
import mil.emp3.mapengine.interfaces.IEmpImageInfo;

/**
 * Implements common methods of BitmapCache. Current subclasses are BitmapCache and AdaptiveBitmapCache
 */
public abstract class CoreBitmapCache implements IBitmapCache {
    private final String TAG;

    /**
     * The image to use when the renderer cannot render an image.
     */
    protected Bitmap defaultImage = BitmapFactory.decodeResource(Resources.getSystem(), android.R.drawable.ic_dialog_alert); // Warning triangle

    protected final SparseArray<String> emptyArray = new SparseArray<>();    // may be used in a cache key
    protected final String emptyArrayString = emptyArray.toString();

    protected MilStdIconRenderer oIconRenderer = null;

    protected final EmpImageInfo oDefaultEmpImageInfo;

    protected CoreBitmapCache(String TAG, String sCacheDir ) {
        this.TAG = CoreBitmapCache.class.getSimpleName() + "." + TAG;
        this.oIconRenderer = MilStdIconRenderer.getInstance();
        oIconRenderer.init(sCacheDir);
        String sKey = "X";
        android.graphics.Rect oRect = new android.graphics.Rect(0, 0, this.defaultImage.getWidth() - 1, this.defaultImage.getHeight() - 1);
        this.oDefaultEmpImageInfo = new EmpImageInfo(sKey, new android.graphics.Point(this.defaultImage.getWidth() / 2, this.defaultImage.getHeight() / 2),
                oRect, oRect, this.defaultImage);
    }

    protected String makeKey(String sSymbolCode, SparseArray oModifiers, SparseArray oAttr) {
        StringBuilder keyBuilder = new StringBuilder();
        keyBuilder.append(sSymbolCode);
        keyBuilder.append((oModifiers == null) ? emptyArrayString : oModifiers.toString());
        keyBuilder.append((oAttr == null) ? emptyArrayString : oAttr.toString());

        return keyBuilder.toString();
    }

    protected void put(String sKey, EmpImageInfo oEmpImageInfo) {
        throw new UnsupportedOperationException("You must override this");
    }

    @Override
    public boolean setMidDistanceThreshold(IMap clientMap, double midDistanceThreshold) {
        throw new UnsupportedOperationException("You must override this");
    }

    @Override
    public boolean getAlgorithmStatus() {
        throw new UnsupportedOperationException("You must override this");
    }

    @Override
    public void setAlgorithmStatus(boolean enable) {
        throw new UnsupportedOperationException("You must override this");
    }

    protected int cacheSize() {
        throw new UnsupportedOperationException("You must override this");
    }

    protected IEmpImageInfo createImageInfo(String sSymbolCode, SparseArray oModifiers, SparseArray oAttr) {
        ImageInfo oImageInfo;
        EmpImageInfo oEmpImageInfo = null;

        if ((oAttr != null) || (oModifiers != null)) {
            if (!this.oIconRenderer.CanRender(sSymbolCode, ((oModifiers == null) ? this.emptyArray : oModifiers), ((oAttr == null) ? this.emptyArray : oAttr))) {
                Log.e(TAG, "The render can not render the icon.");
                return this.oDefaultEmpImageInfo;
            }

            // TODO The MilStd icon renderer has a bug which Spinelli is working on. For some type of icons it adds a modifier to the SparseArray we pass in. We will drop this code when it is corrected in the renderer.
            if ((oModifiers == null) && (this.emptyArray.size() > 0)) {
                //Log.d(TAG, sSymbolCode + " empty array not empty.");
                this.emptyArray.clear();
            }

            try {
                oImageInfo = this.oIconRenderer.RenderIcon(sSymbolCode, ((oModifiers == null) ? this.emptyArray : oModifiers), ((oAttr == null) ? this.emptyArray : oAttr));
            } catch (OutOfMemoryError Ex) {
                Log.e(TAG, "The render failed to render the icon. Lower the Mid Distance Threshold. BitMaps: " + cacheSize() + ". Mem remaining: " + Runtime.getRuntime().freeMemory() + ".");
                // TODO >>> By the time this happens very little memory is left. So this recovery does not work.
                // TODO >>> We need to come up with a better way to limit the number of Bitmaps we allocate.
                return this.oDefaultEmpImageInfo;
            }

            // TODO The MilStd icon renderer has a bug which Spinelli is working on. For some type of icons it adds a modifier to the SparseArray we pass in. We will drop this code when it is corrected in the renderer.
            if ((oModifiers == null) && (this.emptyArray.size() > 0)) {
                //Log.d(TAG, sSymbolCode + " empty array not empty.");
                //String sKey = this.makeKey(sSymbolCode, oModifiers, oAttr);
                this.emptyArray.clear();
            }
            if (oImageInfo != null) {
                String sKey = this.makeKey(sSymbolCode, oModifiers, oAttr);
                oEmpImageInfo = new EmpImageInfo(sKey, oImageInfo.getCenterPoint(), oImageInfo.getImageBounds(), oImageInfo.getSymbolBounds(), oImageInfo.getImage());
                this.put(sKey, oEmpImageInfo);
            } else {
                Log.e(TAG, "Renderer failed to generate icon for " + sSymbolCode + " Modifiers: " + oModifiers.toString() + " Attributes: " + oAttr.toString());
                return this.oDefaultEmpImageInfo;
            }
        } else {
            // Both the attributes and the modifiers are null.
            oEmpImageInfo = this.createFarImageIcon(armyc2.c2sd.renderer.utilities.SymbolUtilities.getAffiliation(sSymbolCode));
            oEmpImageInfo.setFarImageIcon();
            this.put(oEmpImageInfo.getImageKey(), oEmpImageInfo);
        }

        if(null != oEmpImageInfo) {
            return oEmpImageInfo;
        } else {
            return this.oDefaultEmpImageInfo;
        }
    }

    private EmpImageInfo createFarImageIcon(char cAffiliation) {
        DisplayMetrics oMetrics = Resources.getSystem().getDisplayMetrics();
        Bitmap.Config eConfig = Bitmap.Config.ARGB_8888;
        String sSymbolCode = "S" + cAffiliation + "P*------*****";
        armyc2.c2sd.renderer.utilities.Color oColor;
        Bitmap oBitmap;
        EmpImageInfo oImageInfo = null;
        int iColor;
        int iColorBlack = 0xFF000000;
        int iWidthHeight = (int) (oMetrics.densityDpi * 0.0625); // 1/8 in
        int iLastPixel = iWidthHeight - 1;
        android.graphics.Point point = new android.graphics.Point(iWidthHeight / 2, iWidthHeight / 2);
        android.graphics.Rect imageBounds = new android.graphics.Rect(0, 0, iLastPixel, iLastPixel);
        String sKey = this.makeKey(sSymbolCode, null, null);

        oColor = SymbolUtilities.getFillColorOfAffiliation(sSymbolCode);
        iColor = oColor.getAlpha();
        iColor = (iColor << 8) + oColor.getRed();
        iColor = (iColor << 8) + oColor.getGreen();
        iColor = (iColor << 8) + oColor.getBlue();
        oBitmap = Bitmap.createBitmap(oMetrics, iWidthHeight, iWidthHeight, eConfig);
        Canvas oCanvas = new Canvas(oBitmap);
        Paint oPaint = new Paint();

        oPaint.setColor(iColorBlack);
        oCanvas.drawCircle(iWidthHeight / 2, iWidthHeight / 2, iWidthHeight / 2, oPaint);
        oPaint.setColor(iColor);
        oCanvas.drawCircle(iWidthHeight / 2, iWidthHeight / 2, (float) (iWidthHeight / 2.5), oPaint);

        oImageInfo = new EmpImageInfo(sKey, point, imageBounds, imageBounds, oBitmap);
        this.put(sKey, oImageInfo);

        return oImageInfo;
    }
}
