package mil.emp3.api.exceptions;

/**
 * This exception class is the base of the EMP V3 exceptions.
 */
public class EMP_Exception extends Exception {
    public enum ErrorDetail {
        INVALID_PARAMETER,
        INVALID_MAP,
        INVALID_PARENT,
        INVALID_CHILD,
        NOT_SUPPORTED,
        OTHER
    }

    private final ErrorDetail errorDetail;

    public EMP_Exception(EMP_Exception.ErrorDetail errorDetail, String errorMessage) {
        super(errorMessage);
        this.errorDetail = errorDetail;
    }

    public EMP_Exception(EMP_Exception.ErrorDetail errorDetail, String errorMessage, Throwable t) {
        super(errorMessage, t);
        this.errorDetail = errorDetail;
    }

    public EMP_Exception.ErrorDetail getErrorDeatil() {
        return this.errorDetail;
    }
}
