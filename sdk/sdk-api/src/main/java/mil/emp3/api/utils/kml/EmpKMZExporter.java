package mil.emp3.api.utils.kml;

import android.os.Environment;

import java.io.File;

import mil.emp3.api.interfaces.IEmpExportToStringCallback;
import mil.emp3.api.interfaces.IFeature;
import mil.emp3.api.interfaces.IMap;
import mil.emp3.api.interfaces.IOverlay;
import mil.emp3.api.interfaces.core.ICoreManager;
import mil.emp3.api.utils.ManagerFactory;

/**
 * This class implements the KML export capability for EMP.
 */

public class EmpKMZExporter {
    private static String TAG = EmpKMLExporter.class.getSimpleName();
    static final private ICoreManager coreManager = ManagerFactory.getInstance().getCoreManager();

    private EmpKMZExporter() {

    }
    public static boolean deleteDir(File dir) {
        if (dir.isDirectory()) {
            String[] children = dir.list();
            for (int i=0; i<children.length; i++) {
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
        }
        return dir.delete();
    }
    public static void exportToString(IMap map, boolean extendedData, IEmpExportToStringCallback callback) {

        if ((null == map) || (null == callback)) {
            throw new IllegalArgumentException("Parameters can't be null.");
        }
        File dir = new File(Environment.DIRECTORY_PICTURES + "/kmz");
        deleteDir(dir);
        dir.mkdir();
        dir = new File(Environment.DIRECTORY_PICTURES + "/kmz/images/");
        dir.mkdir();

        KMZExportThread exporter = new KMZExportThread(map, extendedData, callback);
        exporter.start();
    }

    public static void exportToString(IMap map, IOverlay overlay, boolean extendedData, IEmpExportToStringCallback callback) {

        if ((null == overlay) || (null == callback)) {
            throw new IllegalArgumentException("Parameters can't be null.");
        }
        File dir = new File(Environment.DIRECTORY_PICTURES + "/kmz");
        deleteDir(dir);
        dir.mkdir();
        dir = new File(Environment.DIRECTORY_PICTURES + "/kmz/images/");
        dir.mkdir();

        KMZExportThread exporter = new KMZExportThread(map, overlay, extendedData, callback);
        exporter.start();
    }

    public static void exportToString(IMap map, IFeature feature, boolean extendedData, IEmpExportToStringCallback callback) {

        if ((null == feature) || (null == callback)) {
            throw new IllegalArgumentException("Parameters can't be null.");
        }
        File dir = new File(Environment.DIRECTORY_PICTURES + "/kmz");
        deleteDir(dir);
        dir.mkdir();
        dir = new File(Environment.DIRECTORY_PICTURES + "/kmz/images/");
        dir.mkdir();

        KMZExportThread exporter = new KMZExportThread(map, feature, extendedData, callback);
        exporter.start();
    }
}
