package mil.emp3.api.utils.kml;

import mil.emp3.api.interfaces.IEmpExportToStringCallback;
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
     * Private to prevent from calling this class
     * in a non-static fashion
     */
    private EmpKMLExporter() {

    }

    /**
     * This exports the map's overlays, and features displayed on the map to a String,
     * the contents of which is a proper KML file.  The callback function needs to choose
     * where to write the string to.
     * @param map the map that contains the overlays and feature data to be exported.
     * @param extendedData whether or not extended data should be exported.
     * @param callback the callback which will provide the KML file created when the thread is finished or report a failure
     */
    public static void exportToString(final IMap map, final boolean extendedData, final IEmpExportToStringCallback callback) {

        if ((null == map) || (null == callback)) {
            throw new IllegalArgumentException("Parameters can't be null.");
        }

        final KMLExportThread exporter = new KMLExportThread(map, extendedData, callback);
        exporter.start();
    }

    /**
     * This exports a specific overlay  displayed on the map to a String,
     * the contents of which is a proper KML file.  The callback function needs to choose
     * where to write the string to.
     * @param map the map that contains the overlays and feature data to be exported.
     * @param overlay the specific overlay to be stored into the KML file
     * @param extendedData whether or not extended data should be exported.
     * @param callback the callback which will provide the KML file created when the thread is finished or report a failure
     */
    public static void exportToString(final IMap map, final IOverlay overlay, final boolean extendedData, final IEmpExportToStringCallback callback) {

        if ((null == overlay) || (null == callback)) {
            throw new IllegalArgumentException("Parameters can't be null.");
        }

        final KMLExportThread exporter = new KMLExportThread(map, overlay, extendedData, callback);
        exporter.start();
    }

    /**
     * This exports a specific feature displayed on the map to a String,
     * the contents of which is a proper KML file.  The callback function needs to choose
     * where to write the string to.
     * @param map the map that contains the overlays and feature data to be exported.
     * @param feature the specific feature to be stored into the KML file
     * @param extendedData whether or not extended data should be exported.
     * @param callback the callback which will provide the KML file created when the thread is finished or report a failure
     */
    public static void exportToString(final IMap map, final IFeature feature, final boolean extendedData, final IEmpExportToStringCallback callback) {

        if ((null == feature) || (null == callback)) {
            throw new IllegalArgumentException("Parameters can't be null.");
        }

        final KMLExportThread exporter = new KMLExportThread(map, feature, extendedData, callback);
        exporter.start();
    }
}
