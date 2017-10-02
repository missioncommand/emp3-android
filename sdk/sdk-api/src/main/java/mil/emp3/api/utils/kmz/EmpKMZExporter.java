package mil.emp3.api.utils.kmz;

import android.os.Environment;

import java.io.File;
import mil.emp3.api.interfaces.IEmpExportToTypeCallBack;
import mil.emp3.api.interfaces.IFeature;
import mil.emp3.api.interfaces.IMap;
import mil.emp3.api.interfaces.IOverlay;

/**
 * Created by jenifer.cochran@rgi-corp.local on 10/2/17.
 */

public class EmpKMZExporter
{
    private static final String KMZExportDirectory = "KMZExport";
    private static final String DefaultExportDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + File.separator + "KMZExport";

    private EmpKMZExporter()
    {

    }

    public static void exportToKMZ(final IMap                           map,
                                   final boolean                        extendedData,
                                   final IEmpExportToTypeCallBack<File> callBack,
                                   final String                         KMZdirectoryName)
    {

        if ((null == map) || (null == KMZdirectoryName))
        {
            throw new IllegalArgumentException("Parameters can't be null.");
        }

        KMZExporterThread kmzExporterThread = new KMZExporterThread(map,
                                                                    extendedData,
                                                                    callBack,
                                                       DefaultExportDirectory + File.separator + KMZdirectoryName);
        kmzExporterThread.run();
        kmzExporterThread.start();
    }

    public static void exportToKMZ(final IMap                           map,
                                   final IOverlay                       overlay,
                                   final boolean                        extendedData,
                                   final IEmpExportToTypeCallBack<File> callBack,
                                   final String                         KMZdirectoryName)
    {

        if ((null == overlay) || (null == callBack))
        {
            throw new IllegalArgumentException("Parameters can't be null.");
        }

        KMZExporterThread kmzExporterThread = new KMZExporterThread(map,
                                                                    overlay,
                                                                    extendedData,
                                                                    callBack,
                                                       DefaultExportDirectory + File.separator + KMZdirectoryName);
        kmzExporterThread.run();
        kmzExporterThread.start();
    }

    public static void exportToKMZ(final IMap                           map,
                                   final IFeature                       feature,
                                   final boolean                        extendedData,
                                   final IEmpExportToTypeCallBack<File> callBack,
                                   final String                         KMZdirectoryName)
    {

        if ((null == feature) || (null == callBack))
        {
            throw new IllegalArgumentException("Parameters can't be null.");
        }

        KMZExporterThread kmzExporterThread = new KMZExporterThread(map,
                                                                    feature,
                                                                    extendedData,
                                                                    callBack,
                                                       DefaultExportDirectory + File.separator + KMZdirectoryName);
        kmzExporterThread.run();
        kmzExporterThread.start();
    }
}
