package mil.emp3.api.interfaces;

/**
 * A client wanting to export EMP object to a string of a specific format must provide an implementation of this interface
 * in order to be notified when the export process is complete or has failed.
 */

public interface IEmpExportToStringCallback {
    /**
     * This method is called by the exporter after a successful export.
     * @param stringFmt    This parameter contains the converted object or objects in string format.
     */
    void exportSuccess(String stringFmt);

    /**
     * This method is called by the exporter to indicate when an error occurs. Once the call is made the
     * export process terminates.
     * @param Ex    The exception that occurred.
     */
    void exportFailed(Exception Ex);
}
