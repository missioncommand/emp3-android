package mil.emp3.api.interfaces;

/**
 * This interface defines the screen capture callback methods. A call to IMap.getScreenCapture must
 * provide an implementation of this interface. The methods defined are called to provide the outcome
 * of the screen capture operation.
 */

public interface IScreenCaptureCallback {
    /**
     * This method is called when the screen capture has been performed and available in the capture object.
     * @param capture The object that implements the ICapture interface.
     */
    void captureSuccess(ICapture capture);

    /**
     * This method is called when the screen capture fails.
     * @param Ex The exception that caused the screen capture to fail.
     */
    void captureFailed(Exception Ex);
}
