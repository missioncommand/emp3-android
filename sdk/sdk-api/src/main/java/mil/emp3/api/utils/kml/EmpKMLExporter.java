package mil.emp3.api.utils.kml;

import android.util.Xml;

import java.io.IOException;
import java.security.InvalidParameterException;

import mil.emp3.api.interfaces.IFeature;
import mil.emp3.api.interfaces.IMap;
import mil.emp3.api.interfaces.IOverlay;
import mil.emp3.api.interfaces.core.ICoreManager;
import mil.emp3.api.utils.ManagerFactory;

/**
 * This class implements the KML export capability for EMP.
 */

public class EmpKMLExporter {
    private static String TAG = EmpKMLExporter.class.getSimpleName();
    static final private ICoreManager coreManager = ManagerFactory.getInstance().getCoreManager();

    /**
     * A client wanting to export EMP object to KML must provide an implementation of this interface
     * in order to be notified when the export process is complete or has failed.
     */
    public interface IEmpKMLExportCallback {
        /**
         * This method is called by the exporter after a successful export.
         * @param kmlString    This parameter contains the KML string.
         */
        void kmlExportSuccess(final String kmlString);

        /**
         * This method is called by the exporter to indicate when an error occurs. Once the call is made the
         * export process terminates.
         * @param Ex    The exception that occurred.
         */
        void kmlExportFailed(Exception Ex);
    }

    private EmpKMLExporter() {

    }

    public static void exportToString(IMap map, IEmpKMLExportCallback callback) {

        if ((null == map) || (null == callback)) {
            throw new InvalidParameterException("Parameters can't be null.");
        }

        KMLExportThread exporter = new KMLExportThread(map, callback);
        exporter.start();
    }

    public static void exportToString(IOverlay overlay, IEmpKMLExportCallback callback) {

        if ((null == overlay) || (null == callback)) {
            throw new InvalidParameterException("Parameters can't be null.");
        }

        KMLExportThread exporter = new KMLExportThread(overlay, callback);
        exporter.start();
    }

    public static void exportToString(IFeature feature, IEmpKMLExportCallback callback) {

        if ((null == feature) || (null == callback)) {
            throw new InvalidParameterException("Parameters can't be null.");
        }

        KMLExportThread exporter = new KMLExportThread(feature, callback);
        exporter.start();
    }
}
