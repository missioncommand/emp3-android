package mil.emp3.api.shadows;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Rect;
import android.util.SparseArray;

import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

import armyc2.c2sd.renderer.MilStdIconRenderer;
import armyc2.c2sd.renderer.utilities.ImageInfo;

/**
 * @author Matt Miller & Jenifer Cochran
 */
@Implements(MilStdIconRenderer.class)
public class ShadowKMLExportThread
{

    @Implementation
    public static MilStdIconRenderer getInstance()
    {
        return new MilStdIconRenderer();
    }

    @Implementation
    public static ImageInfo RenderIcon(String              symbolID,
                                       SparseArray<String> modifiers,
                                       SparseArray<String> attributes)
    {
        int[] colors = new int[]{
                                    Color.parseColor("#ff0000"),
                                    Color.parseColor("#00ff00"),
                                    Color.parseColor("#0000ff"),
                                    Color.parseColor("#990000"),
                                    Color.parseColor("#009900"),
                                    Color.parseColor("#000099")
                                };
        //create bitmap
        Bitmap image = Bitmap.createBitmap(colors, 3, 2, Bitmap.Config.ARGB_8888);
        //create location
        Point centerPoint = new Point();
        centerPoint.set(0,0);
        //create bounds
        Rect bounds = new Rect();
        bounds.set(-1, 1, 1, -1);

        return new ImageInfo(image, centerPoint, bounds);
    }
}
