package mil.emp3.api.interfaces;

/**
 * This class defines the interface to a Capture object.
 */
public interface ICapture {
    /**
     * This method retrieves the bitmap object that contains the screen shot.
     * @return android.graphics.Bitmap if successful or null if not.
     */
    public android.graphics.Bitmap screenshot();
}
