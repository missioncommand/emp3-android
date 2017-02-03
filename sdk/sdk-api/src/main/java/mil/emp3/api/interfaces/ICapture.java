package mil.emp3.api.interfaces;

/**
 * This class defines the interface to a Capture object.
 */
public interface ICapture {
    /**
     * This method retrieves the bitmap object that contains the screen shot.
     * @return android.graphics.Bitmap if successful or null if not.
     */
    android.graphics.Bitmap screenshotBitmap();

    /**
     * This method retrieves the screenshot as a base 64 data URL string.
     * @return A base 64 data URL string if its successful or nul if not.
     */
    String screenshotDataURL();
}
