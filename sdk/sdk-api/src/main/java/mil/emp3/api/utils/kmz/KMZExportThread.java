package mil.emp3.api.utils.kmz;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import mil.emp3.api.interfaces.IEmpExportToStringCallback;
import mil.emp3.api.interfaces.IEmpExportToTypeCallBack;
import mil.emp3.api.interfaces.IFeature;
import mil.emp3.api.interfaces.IMap;
import mil.emp3.api.interfaces.IOverlay;
import mil.emp3.api.utils.FileUtility;
import mil.emp3.api.utils.ZipUtility;

/**
 * This exports the map's overlays or features on a separate thread to a
 * KMZ file.
 *
 * @author Jenifer Cochran
 */

public final class KMZExportThread extends Thread
{
    private final File                           temporaryDirectory;
    private final IEmpExportToTypeCallBack<File> callback;
    private final File                           kmzOutputLocation;
    private final KMLRelativePathExportThread    kmlRelativePathExportThread;

    private final static String DefaultKMLFileName     = "kml_export.kml";

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
     * @param kmzOutputDirectory  The name of the exported KMZ File Name (i.e. kmz_file_export.kmz or kmz_file_export).
     *
     * @param temporaryDirectory The temporary directory location( it is highly recommended to use
     *                           Context.getExternalFilesDir() as the temporary location).
     *                           The contents of this directory will be removed after export.
     *
     */
    protected KMZExportThread(final IMap                           map,
                              final boolean                        extendedData,
                              final IEmpExportToTypeCallBack<File> callback,
                              final File                           kmzOutputDirectory,
                              final File                           temporaryDirectory)
    {
        this.callback           = callback;
        this.temporaryDirectory = new File(temporaryDirectory.getAbsolutePath());
        this.kmzOutputLocation  = new File(kmzOutputDirectory.getAbsolutePath());

        if(FileUtility.isChildDirectory(this.temporaryDirectory, kmzOutputDirectory))
        {
            throw new IllegalArgumentException(String.format("The temporary directory cannot be a parent directory of %s.  Must select a different temporary directory.",
                                                             kmzOutputDirectory.getAbsolutePath()));
        }

        FileUtility.createOutputDirectory(temporaryDirectory.getAbsolutePath());

        this.kmlRelativePathExportThread = new KMLRelativePathExportThread(map,
                                                                           extendedData,
                                                                           new IEmpExportToStringCallback(){
                                                                                                               @Override
                                                                                                               public void exportSuccess(String stringFmt)
                                                                                                               {
                                                                                                                   KMZExportThread.createKMZFile(stringFmt,
                                                                                                                                                 KMZExportThread.this.temporaryDirectory,
                                                                                                                                                 KMZExportThread.this.kmzOutputLocation,
                                                                                                                                                 KMZExportThread.this.callback);
                                                                                                               }
                                                                                                               @Override
                                                                                                               public void exportFailed(Exception Ex)
                                                                                                               {
                                                                                                                   KMZExportThread.this.callback.exportFailed(Ex);
                                                                                                               }
                                                                                                           },
                                                                           temporaryDirectory.getAbsolutePath());
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
     * @param kmzOutputLocation The name of the exported KMZ File Name (i.e. kmz_file_export.kmz or kmz_file_export).
     *
     * @param temporaryDirectory The temporary directory location( it is highly recommended to use
     *                           Context.getExternalFilesDir() as the temporary location).
     *                           The contents of this directory will be removed after export.
     *
     */
    protected KMZExportThread(final IMap                           map,
                              final IOverlay                       overlay,
                              final boolean                        extendedData,
                              final IEmpExportToTypeCallBack<File> callback,
                              final File                           kmzOutputLocation,
                              final File                           temporaryDirectory)
    {
        this.callback           = callback;
        this.temporaryDirectory = new File(temporaryDirectory.getAbsolutePath());
        this.kmzOutputLocation  = new File(kmzOutputLocation.getAbsolutePath());

        if(FileUtility.isChildDirectory(this.temporaryDirectory, this.kmzOutputLocation))
        {
            throw new IllegalArgumentException(String.format("The temporary directory cannot be a parent directory of %s.  Must select a different temporary directory.",
                                                             this.kmzOutputLocation.getAbsolutePath()));
        }

        FileUtility.createOutputDirectory(temporaryDirectory.getAbsolutePath());

        this.kmlRelativePathExportThread = new KMLRelativePathExportThread(map,
                                                                           overlay,
                                                                           extendedData,
                                                                           new IEmpExportToStringCallback(){
                                                                                                               @Override
                                                                                                               public void exportSuccess(String stringFmt)
                                                                                                               {
                                                                                                                   KMZExportThread.createKMZFile(stringFmt,
                                                                                                                                                 KMZExportThread.this.temporaryDirectory,
                                                                                                                                                 KMZExportThread.this.kmzOutputLocation,
                                                                                                                                                 KMZExportThread.this.callback);
                                                                                                               }

                                                                                                               @Override
                                                                                                               public void exportFailed(Exception Ex)
                                                                                                               {
                                                                                                                   KMZExportThread.this.callback.exportFailed(Ex);
                                                                                                               }
                                                                                                           },
                                                                           temporaryDirectory.getAbsolutePath());

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
     * @param temporaryDirectory The temporary directory location( it is highly recommended to use
     *                           Context.getExternalFilesDir() as the temporary location).
     *                           The contents of this directory will be removed after export.
     *
     * @param kmzOutputLocation The name of the exported KMZ File Name (i.e. kmz_file_export.kmz or kmz_file_export).
     */
    protected KMZExportThread(final IMap                           map,
                              final IFeature                       feature,
                              final boolean                        extendedData,
                              final IEmpExportToTypeCallBack<File> callback,
                              final File                           kmzOutputLocation,
                              final File                           temporaryDirectory)

    {
        this.callback           = callback;
        this.temporaryDirectory = new File(temporaryDirectory.getAbsolutePath());
        this.kmzOutputLocation  = new File(kmzOutputLocation.getAbsolutePath());

        if(FileUtility.isChildDirectory(this.temporaryDirectory, this.kmzOutputLocation))
        {
            throw new IllegalArgumentException(String.format("The temporary directory cannot be a parent directory of %s.  Must select a different temporary directory.",
                                                             this.kmzOutputLocation.getAbsolutePath()));
        }

        FileUtility.createOutputDirectory(temporaryDirectory.getAbsolutePath());

        this.kmlRelativePathExportThread = new KMLRelativePathExportThread(map,
                                                                           feature,
                                                                           extendedData,
                                                                           new IEmpExportToStringCallback(){
                                                                                                               @Override
                                                                                                               public void exportSuccess(String stringFmt)
                                                                                                               {
                                                                                                                   KMZExportThread.createKMZFile(stringFmt,
                                                                                                                                                 KMZExportThread.this.temporaryDirectory,
                                                                                                                                                 KMZExportThread.this.kmzOutputLocation,
                                                                                                                                                 KMZExportThread.this.callback);
                                                                                                               }

                                                                                                               @Override
                                                                                                               public void exportFailed(Exception Ex)
                                                                                                               {
                                                                                                                   KMZExportThread.this.callback.exportFailed(Ex);
                                                                                                               }
                                                                                                           },
                                                                           temporaryDirectory.getAbsolutePath());

    }



    @Override
    public void run()
    {
        super.run();
        this.kmlRelativePathExportThread.run();
    }

    /***
     * Creates the kmz file and reports the kmz File to the callback.
     *
     * @param kmlStringData The kml data in the form of a string
     *
     * @param temporaryDirectory The temporary directory location
     *
     * @param kmzFile The kmz file location output
     *
     * @param callback The callback to report the kmz file to
     *
     */
    private static void createKMZFile(final String                         kmlStringData,
                                      final File                           temporaryDirectory,
                                      final File                           kmzFile,
                                      final IEmpExportToTypeCallBack<File> callback)
    {
        //Create the kml file at the given temporary directory
        //where the related image files are also located
        final File kmlFile = new File(temporaryDirectory.getAbsolutePath() + File.separator + DefaultKMLFileName);

        try(final FileOutputStream out = new FileOutputStream(kmlFile.getAbsoluteFile()))
        {
            //write the kml to the temporary directory
            final byte[] byteArray = kmlStringData.getBytes();
            out.write(byteArray, 0, byteArray.length);
            out.flush();
            //zip up directory containing the kml file
            //as well as the Image directory containing
            //the referenced images
            ZipUtility.zip(temporaryDirectory, kmzFile);
            callback.exportSuccess(kmzFile);
        }
        catch (IOException e)
        {
            callback.exportFailed(e);
        }
        finally
        {
            //clean up the folders and files that are no longer needed
            //from the temporary directory
            if(kmlFile.exists())
            {
                kmlFile.delete();
            }
            if(temporaryDirectory.exists())
            {
                FileUtility.deleteFolder(temporaryDirectory);
            }
        }
    }
}
