package mil.emp3.api.utils.kml;

import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import mil.emp3.api.interfaces.IEmpExportToStringCallback;
import mil.emp3.api.interfaces.IEmpExportToTypeCallBack;
import mil.emp3.api.interfaces.IFeature;
import mil.emp3.api.interfaces.IMap;
import mil.emp3.api.interfaces.IOverlay;
import mil.emp3.api.interfaces.core.ICoreManager;
import mil.emp3.api.utils.ManagerFactory;
import mil.emp3.api.utils.kmz.KMZExporterThread;

/**
 * This class implements the KML export capability for EMP.
 */

public class EmpKMLExporter {
    private static String TAG = EmpKMLExporter.class.getSimpleName();
    static final private ICoreManager coreManager = ManagerFactory.getInstance().getCoreManager();

    private EmpKMLExporter() {

    }

    public static void exportToString(IMap map, boolean extendedData, IEmpExportToStringCallback callback) {

        if ((null == map) || (null == callback)) {
            throw new IllegalArgumentException("Parameters can't be null.");
        }

        KMZExporterThread kmzExporterThread = new KMZExporterThread(map, extendedData,new IEmpExportToTypeCallBack<File>() {

            @Override
            public void exportSuccess(File kmlString)
            {
                kmlString.exists();
            }

            @Override
            public void exportFailed(Exception Ex)
            {

            }
        }, Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + File.separator + "KMZExport");
        kmzExporterThread.run();
        KMLExportThread exporter = new KMLExportThread(map, extendedData, callback);
        exporter.start();
    }

    public static void exportToString(IMap map, IOverlay overlay, boolean extendedData, IEmpExportToStringCallback callback) {

        if ((null == overlay) || (null == callback)) {
            throw new IllegalArgumentException("Parameters can't be null.");
        }

        KMLExportThread exporter = new KMLExportThread(map, overlay, extendedData, callback);
        exporter.start();
    }

    public static void exportToString(IMap map, IFeature feature, boolean extendedData, IEmpExportToStringCallback callback) {

        if ((null == feature) || (null == callback)) {
            throw new IllegalArgumentException("Parameters can't be null.");
        }

        KMLExportThread exporter = new KMLExportThread(map, feature, extendedData, callback);
        exporter.start();
    }
}
