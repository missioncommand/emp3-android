package mil.emp3.api.utils.kmz;

import java.io.File;
import mil.emp3.api.interfaces.IEmpExportToTypeCallBack;
import mil.emp3.api.interfaces.IFeature;
import mil.emp3.api.interfaces.IMap;
import mil.emp3.api.interfaces.IOverlay;

/**
 * Exports Map, Overlay, or Feature to a KMZ file.
 * @author Jenifer Cochran
 */

public final class EmpKMZExporter
{
    private static final String KMZExportDirectory = "KMZExport";

    /**
     * Private to prevent from calling this class
     * in a non-static fashion
     */
    private EmpKMZExporter()
    {

    }

    /**
     * This exports the map's overlays, and features displayed on the map
     * to a KMZ file given the directory where the KMZ file should be stored as
     * well as the desired name of the KMZ file.
     * @param map the map that contains the overlays and feature data to be exported.
     * @param extendedData whether or not extended data should be exported.
     * @param callBack the callback which will provide the KMZ file created when the thread is finished or report a failure
     * @param KMZDirectoryName the directory location where the KMZ file should be created. (i.e. /storage/emulated/0/Pictures/).
     * @param KMZFileName  the name of the exported KMZ File Name (i.e. kmz_file_export.kmz or kmz_file_export).
     */
    public static void exportToKMZ(final IMap                           map,
                                   final boolean                        extendedData,
                                   final IEmpExportToTypeCallBack<File> callBack,
                                   final String                         KMZDirectoryName,
                                   final String                         KMZFileName)
    {

        if ((null == map)              ||
            (null == KMZDirectoryName) ||
            (null == callBack)         ||
            (null == KMZFileName))
        {
            throw new IllegalArgumentException("Parameters can't be null.");
        }

        if(KMZDirectoryName.isEmpty())
        {
            throw new IllegalArgumentException("The KMZDirectoryName cannot be an empty string.");
        }

        if(KMZFileName.isEmpty())
        {
            throw new IllegalArgumentException("The KMZFileName cannot be an empty string.");
        }

        KMZExporterThread kmzExporterThread = new KMZExporterThread(map,
                                                                    extendedData,
                                                                    callBack,
                                                                    KMZDirectoryName,
                                                                    KMZFileName);
        kmzExporterThread.run();
        kmzExporterThread.start();
    }

    /**
     * This exports the overlay specified that is displayed on the map
     * to a KMZ file given the directory where the KMZ file should be stored as
     * well as the desired name of the KMZ file.
     * @param map the map that contains the overlay to be exported.
     * @param overlay the overlay to be exported.
     * @param extendedData whether or not extended data should be exported
     * @param callBack the callback which will provide the KMZ file created when the thread is finished or report a failure
     * @param KMZDirectoryName the directory location where the KMZ file should be created. (i.e. /storage/emulated/0/Pictures/).
     * @param KMZFileName the name of the exported KMZ File Name (i.e. kmz_file_export.kmz or kmz_file_export).
     */
    public static void exportToKMZ(final IMap                           map,
                                   final IOverlay                       overlay,
                                   final boolean                        extendedData,
                                   final IEmpExportToTypeCallBack<File> callBack,
                                   final String                         KMZDirectoryName,
                                   final String                         KMZFileName)
    {

        if ((null == map)              ||
            (null == overlay)          ||
            (null == KMZDirectoryName) ||
            (null == callBack)         ||
            (null == KMZFileName))
        {
            throw new IllegalArgumentException("Parameters can't be null.");
        }

        if(KMZDirectoryName.isEmpty())
        {
            throw new IllegalArgumentException("The KMZDirectoryName cannot be an empty string.");
        }

        if(KMZFileName.isEmpty())
        {
            throw new IllegalArgumentException("The KMZFileName cannot be an empty string.");
        }
   
        KMZExporterThread kmzExporterThread = new KMZExporterThread(map,
                                                                    overlay,
                                                                    extendedData,
                                                                    callBack,
                                                                    KMZDirectoryName,
                                                                    KMZFileName);
        kmzExporterThread.run();
        kmzExporterThread.start();
    }

    /**
     * This exports the feature specified that is displayed on the map
     * to a KMZ file given the directory where the KMZ file should be stored as
     * well as the desired name of the KMZ file.
     * @param map the map that contains the overlay to be exported.
     * @param feature the feature to be exported.
     * @param extendedData whether or not extended data should be exported
     * @param callBack the callback which will provide the KMZ file created when the thread is finished or report a failure
     * @param KMZDirectoryName the directory location where the KMZ file should be created. (i.e. /storage/emulated/0/Pictures/).
     * @param KMZFileName the name of the exported KMZ File Name (i.e. kmz_file_export.kmz or kmz_file_export).
     */
    public static void exportToKMZ(final IMap                           map,
                                   final IFeature                       feature,
                                   final boolean                        extendedData,
                                   final IEmpExportToTypeCallBack<File> callBack,
                                   final String                         KMZDirectoryName,
                                   final String                         KMZFileName)
    {

        if ((null == map)              ||
            (null == feature)          ||
            (null == KMZDirectoryName) ||
            (null == callBack)         ||
            (null == KMZFileName))
        {
            throw new IllegalArgumentException("Parameters can't be null.");
        }

        if(KMZDirectoryName.isEmpty())
        {
            throw new IllegalArgumentException("The KMZDirectoryName cannot be an empty string.");
        }

        if(KMZFileName.isEmpty())
        {
            throw new IllegalArgumentException("The KMZFileName cannot be an empty string.");
        }

        KMZExporterThread kmzExporterThread = new KMZExporterThread(map,
                                                                    feature,
                                                                    extendedData,
                                                                    callBack,
                                                                    KMZDirectoryName,
                                                                    KMZFileName);
        kmzExporterThread.run();
        kmzExporterThread.start();
    }
}
