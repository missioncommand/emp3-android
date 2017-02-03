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
    
    private final ErrorDetail eErrorDeatil;
    
    public EMP_Exception(EMP_Exception.ErrorDetail detailEnum, String errorMessage) {
        super(errorMessage);
        this.eErrorDeatil = detailEnum;
    }
    
    public EMP_Exception.ErrorDetail getErrorDeatil() {
        return this.eErrorDeatil;
    }
}
