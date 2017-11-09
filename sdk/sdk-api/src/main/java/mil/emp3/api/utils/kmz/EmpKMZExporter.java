package mil.emp3.api.utils.kmz;

import android.os.Environment;

import java.io.File;
import mil.emp3.api.interfaces.IEmpExportToTypeCallBack;
import mil.emp3.api.interfaces.IFeature;
import mil.emp3.api.interfaces.IMap;
import mil.emp3.api.interfaces.IOverlay;
import mil.emp3.api.utils.FileUtility;

/**
 * Exports Map, Overlay, or Feature to a KMZ file.
 * @author Jenifer Cochran
 */

public final class EmpKMZExporter
{
    /**
     * Private to prevent from calling this class
     * in a non-static fashion
     */
    private EmpKMZExporter()
    {

    }

    private final static File   DefaultExportDirectory = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + File.separator + "KMZExport");
    private final static String KMZFileExtension       = ".kmz";

    /**
     * This exports the map's overlays, and features displayed on the map
     * to a KMZ file given the directory where the KMZ file should be stored as
     * well as the desired location of the KMZ file.
     *
     * @param map The map that contains the overlays and feature data to be exported.
     *
     * @param extendedData Whether or not extended data should be exported.
     *
     * @param callback The callback which will provide the KMZ file created when the thread is finished or report a failure
     *
     * @param outputKmzLocation  The output location of the KMZ File (i.e. path/to/directory/kmz_file_export.kmz).
     *
     * @param temporaryDirectoryLocation The temporary directory location( it is highly recommended to use
     *                                   Context.getExternalFilesDir() as the temporary location).
     *                                   The contents of this directory will be removed after export.
     *
     */
    public static void exportToKMZ(final IMap                           map,
                                   final boolean                        extendedData,
                                   final IEmpExportToTypeCallBack<File> callback,
                                   final File                           outputKmzLocation,
                                   final File                           temporaryDirectoryLocation)
    {

        if ((map                        == null) ||
            (temporaryDirectoryLocation == null) ||
            (callback                   == null) ||
            (outputKmzLocation          == null))
        {
            throw new IllegalArgumentException("Parameters can't be null.");
        }

        if(!temporaryDirectoryLocation.exists())
        {
            throw new IllegalArgumentException("The temporaryDirectoryLocation must exist.");
        }

        if(!temporaryDirectoryLocation.isDirectory())
        {
            throw new IllegalArgumentException("The temporaryDirectoryLocation must be a path do a directory.");
        }

        if(outputKmzLocation.exists())
        {
            throw new IllegalArgumentException(String.format("The outputKmzLocation File cannot already exist. %s", outputKmzLocation.getAbsolutePath()));
        }

        if(outputKmzLocation.isDirectory())
        {
            throw new IllegalArgumentException("The outputKmzLocation File must be a file location. (i.e. path/to/directory/kmz_file_export.kmz)");
        }

        if(!outputKmzLocation.getName().toLowerCase().endsWith(KMZFileExtension))
        {
            throw new IllegalArgumentException(String.format("The ouputKmzLocation File name must have a file extension of %s", KMZFileExtension));
        }

        final KMZExportThread kmzExportThread = new KMZExportThread(map,
                                                                    extendedData,
                                                                    callback,
                                                                    outputKmzLocation,
                                                                    temporaryDirectoryLocation);
        kmzExportThread.run();
    }


    /**
     * This exports the map's overlays, and features displayed on the map
     * to a KMZ file given the directory where the KMZ file should be stored as
     * well as the desired name of the KMZ file.
     *
     * @param map The map that contains the overlays and feature data to be exported.
     *
     * @param extendedData Whether or not extended data should be exported.
     *
     * @param callback The callback which will provide the KMZ file created when the thread is finished or report a failure
     *
     * @param temporaryDirectoryLocation The temporary directory location( it is highly recommended to use
     *                                   Context.getExternalFilesDir() as the temporary location).
     *                                   The contents of this directory will be removed after export.
     * @param kmzFileName  The name of the exported KMZ File Name with .kmz extension (i.e. kmz_file_export.kmz or kmz_file_export).
     */
    public static void exportToKMZ(final IMap                           map,
                                   final boolean                        extendedData,
                                   final IEmpExportToTypeCallBack<File> callback,
                                   final String                         temporaryDirectoryLocation,
                                   final String                         kmzFileName)
    {
        if((kmzFileName                == null) ||
           (temporaryDirectoryLocation == null))
        {
            throw new IllegalArgumentException("Parameters can't be null.");
        }

        if(kmzFileName.isEmpty())
        {
            throw new IllegalArgumentException("The kmzFileName cannot be an empty string.");
        }

        FileUtility.createOutputDirectory(DefaultExportDirectory.getAbsolutePath());

        exportToKMZ(map,
                    extendedData,
                    callback,
                    new File(DefaultExportDirectory, !kmzFileName.toLowerCase().endsWith(KMZFileExtension) ? kmzFileName + KMZFileExtension
                                                                                                           : kmzFileName),
                    new File(temporaryDirectoryLocation));
    }

    /**
     * This exports the overlay specified that is displayed on the map
     * to a KMZ file given the directory where the KMZ file should be stored as
     * well as the desired location of the KMZ file.
     *
     * @param map The map that contains the overlay to be exported.
     *
     * @param overlay The overlay to be exported.
     *
     * @param extendedData Whether or not extended data should be exported
     *
     * @param callback The callback which will provide the KMZ file created when the thread is finished or report a failure
     *
     * @param outputKmzLocation  The output location of the KMZ File (i.e. path/to/directory/kmz_file_export.kmz).
     *
     * @param temporaryDirectoryLocation The temporary directory location( it is highly recommended to use
     *                                   Context.getExternalFilesDir() as the temporary location).
     *                                   The contents of this directory will be removed after export.
     *
     */
    public static void exportToKMZ(final IMap                           map,
                                   final IOverlay                       overlay,
                                   final boolean                        extendedData,
                                   final IEmpExportToTypeCallBack<File> callback,
                                   final File                           outputKmzLocation,
                                   final File                           temporaryDirectoryLocation)
    {
        if ((map                        == null) ||
            (overlay                    == null) ||
            (temporaryDirectoryLocation == null) ||
            (callback                   == null) ||
            (outputKmzLocation          == null))
        {
            throw new IllegalArgumentException("Parameters can't be null.");
        }

        if(!temporaryDirectoryLocation.exists())
        {
            throw new IllegalArgumentException("The temporaryDirectoryLocation must exist.");
        }

        if(!temporaryDirectoryLocation.isDirectory())
        {
            throw new IllegalArgumentException("The temporaryDirectoryLocation must be a path do a directory.");
        }

        if(outputKmzLocation.exists())
        {
            throw new IllegalArgumentException(String.format("The outputKmzLocation File cannot already exist. %s", outputKmzLocation.getAbsolutePath()));
        }

        if(outputKmzLocation.isDirectory())
        {
            throw new IllegalArgumentException("The outputKmzLocation File must be a file location. (i.e. path/to/directory/kmz_file_export.kmz)");
        }

        if(!outputKmzLocation.getName().toLowerCase().endsWith(KMZFileExtension))
        {
            throw new IllegalArgumentException(String.format("The ouputKmzLocation File name must have a file extension of %s", KMZFileExtension));
        }

        final KMZExportThread kmzExportThread = new KMZExportThread(map,
                                                                    overlay,
                                                                    extendedData,
                                                                    callback,
                                                                    outputKmzLocation,
                                                                    temporaryDirectoryLocation);
        kmzExportThread.run();
    }

    /**
     * This exports the overlay specified that is displayed on the map
     * to a KMZ file given the directory where the KMZ file should be stored as
     * well as the desired name of the KMZ file.
     *
     * @param map The map that contains the overlay to be exported.
     *
     * @param overlay The overlay to be exported.
     *
     * @param extendedData Whether or not extended data should be exported
     *
     * @param callback The callback which will provide the KMZ file created when the thread is finished or report a failure
     *
     * @param temporaryDirectoryLocation The temporary directory location( it is highly recommended to use
     *                                   Context.getExternalFilesDir() as the temporary location).
     *                                   The contents of this directory will be removed after export.
     * @param kmzFileName The name of the exported KMZ File Name (i.e. kmz_file_export.kmz or kmz_file_export).
     */
    public static void exportToKMZ(final IMap                           map,
                                   final IOverlay                       overlay,
                                   final boolean                        extendedData,
                                   final IEmpExportToTypeCallBack<File> callback,
                                   final String                         temporaryDirectoryLocation,
                                   final String                         kmzFileName)
    {

        if((kmzFileName                == null) ||
           (temporaryDirectoryLocation == null))
        {
            throw new IllegalArgumentException("Parameters can't be null.");
        }

        if(kmzFileName.isEmpty())
        {
            throw new IllegalArgumentException("The kmzFileName cannot be an empty string.");
        }

        FileUtility.createOutputDirectory(DefaultExportDirectory.getAbsolutePath());

        exportToKMZ(map,
                    overlay,
                    extendedData,
                    callback,
                    new File(DefaultExportDirectory, !kmzFileName.toLowerCase().endsWith(KMZFileExtension) ? kmzFileName + KMZFileExtension
                                                                                                           : kmzFileName),
                    new File(temporaryDirectoryLocation));
    }

    /**
     * This exports the feature specified that is displayed on the map
     * to a KMZ file given the directory where the KMZ file should be stored as
     * well as the desired location of the KMZ file.
     *
     * @param map The map that contains the overlay to be exported.
     *
     * @param feature The feature to be exported.
     *
     * @param extendedData Whether or not extended data should be exported
     *
     * @param callback The callback which will provide the KMZ file created when the thread is finished or report a failure
     *
     * @param outputKmzLocation  The output location of the KMZ File (i.e. path/to/directory/kmz_file_export.kmz).
     *
     * @param temporaryDirectoryLocation The temporary directory location( it is highly recommended to use
     *                                   Context.getExternalFilesDir() as the temporary location).
     *                                   The contents of this directory will be removed after export.
     *
     */
    public static void exportToKMZ(final IMap                           map,
                                   final IFeature                       feature,
                                   final boolean                        extendedData,
                                   final IEmpExportToTypeCallBack<File> callback,
                                   final File                           outputKmzLocation,
                                   final File                           temporaryDirectoryLocation)
    {

        if ((map                        == null) ||
            (feature                    == null) ||
            (temporaryDirectoryLocation == null) ||
            (callback                   == null) ||
            (outputKmzLocation)         == null)
        {
            throw new IllegalArgumentException("Parameters can't be null.");
        }

        if(!temporaryDirectoryLocation.exists())
        {
            throw new IllegalArgumentException("The temporaryDirectoryLocation must exist.");
        }

        if(!temporaryDirectoryLocation.isDirectory())
        {
            throw new IllegalArgumentException("The temporaryDirectoryLocation must be a path do a directory.");
        }

        if(outputKmzLocation.exists())
        {
            throw new IllegalArgumentException(String.format("The outputKmzLocation File cannot already exist. %s", outputKmzLocation.getAbsolutePath()));
        }

        if(outputKmzLocation.isDirectory())
        {
            throw new IllegalArgumentException("The outputKmzLocation File must be a file location. (i.e. path/to/directory/kmz_file_export.kmz)");
        }

        if(!outputKmzLocation.getName().toLowerCase().endsWith(KMZFileExtension))
        {
            throw new IllegalArgumentException(String.format("The ouputKmzLocation File name must have a file extension of %s", KMZFileExtension));
        }

        final KMZExportThread kmzExportThread = new KMZExportThread(map,
                                                                    feature,
                                                                    extendedData,
                                                                    callback,
                                                                    outputKmzLocation,
                                                                    temporaryDirectoryLocation);
        kmzExportThread.run();
    }

    /**
     * This exports the feature specified that is displayed on the map
     * to a KMZ file given the directory where the KMZ file should be stored as
     * well as the desired name of the KMZ file.
     *
     * @param map The map that contains the overlay to be exported.
     *
     * @param feature The feature to be exported.
     *
     * @param extendedData Whether or not extended data should be exported
     *
     * @param callback The callback which will provide the KMZ file created when the thread is finished or report a failure
     *
     * @param temporaryDirectoryLocation The temporary directory location( it is highly recommended to use
     *                                   Context.getExternalFilesDir() as the temporary location).
     *                                   The contents of this directory will be removed after export.
     *
     * @param kmzFileName the name of the exported KMZ File Name (i.e. kmz_file_export.kmz or kmz_file_export).
     */
    public static void exportToKMZ(final IMap                           map,
                                   final IFeature                       feature,
                                   final boolean                        extendedData,
                                   final IEmpExportToTypeCallBack<File> callback,
                                   final String                         temporaryDirectoryLocation,
                                   final String                         kmzFileName)
    {

        if((kmzFileName                == null) ||
           (temporaryDirectoryLocation == null))
        {
            throw new IllegalArgumentException("Parameters can't be null.");
        }

        if(kmzFileName.isEmpty())
        {
            throw new IllegalArgumentException("The kmzFileName cannot be an empty string.");
        }

        FileUtility.createOutputDirectory(DefaultExportDirectory.getAbsolutePath());

        exportToKMZ(map,
                    feature,
                    extendedData,
                    callback,
                    new File(DefaultExportDirectory, !kmzFileName.toLowerCase().endsWith(KMZFileExtension) ? kmzFileName + KMZFileExtension
                                                                                                           : kmzFileName),
                    new File(temporaryDirectoryLocation));
    }
}
