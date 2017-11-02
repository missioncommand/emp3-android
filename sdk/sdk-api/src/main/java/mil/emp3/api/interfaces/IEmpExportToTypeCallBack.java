package mil.emp3.api.interfaces;

/**
 * A callback interface to return a specified exported value
 * @author jenifer cochran
 */
public interface IEmpExportToTypeCallBack<T>
{
    /**
     * This method is called by the exporter after a successful export.
     * @param exportObject    This parameter contains the exported object.
     */
    void exportSuccess(T exportObject);

    /**
     * This method is called by the exporter to indicate when an error occurs. Once the call is made the
     * export process terminates.
     * @param Ex    The exception that occurred.
     */
    void exportFailed(Exception Ex);
}
