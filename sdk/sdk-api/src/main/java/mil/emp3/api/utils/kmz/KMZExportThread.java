package mil.emp3.api.utils.kmz;

import android.os.Environment;

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
    private final String                         kmzFileName;
    private final KMLRelativePathExportThread    kmlRelativePathExportThread;

    private final static String DefaultKMLFileName     = "kml_export.kml";
    private final static String KMZFileExtension       = ".kmz";
    private final static File   DefaultExportDirectory = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + File.separator + "KMZExport");


    /**
     * This exports the map's overlays, and features displayed on the map
     * to a KMZ file given the directory where the KMZ file should be stored as
     * well as the desired name of the KMZ file.
     *
     * @param map the map that contains the overlays and feature data to be exported.
     * @param extendedData whether or not extended data should be exported.
     * @param callback the callback which will provide the KMZ file created when the thread is finished or report a failure
     * @param temporaryDirectory the temporary directory location( it is highly recommended to use
     *                                   Context.getExternalFilesDir() as the temporary location).
     *                                   The contents of this directory will be removed after export.
     * @param kmzFileName  the name of the exported KMZ File Name (i.e. kmz_file_export.kmz or kmz_file_export).
     */
    protected KMZExportThread(final IMap                           map,
                              final boolean                        extendedData,
                              final IEmpExportToTypeCallBack<File> callback,
                              final String                         temporaryDirectory,
                              final String                         kmzFileName)
    {
        this.callback           = callback;
        this.temporaryDirectory = new File(temporaryDirectory);
        this.kmzFileName        = kmzFileName.toLowerCase()
                                             .endsWith(KMZFileExtension) ? kmzFileName
                                                                         : kmzFileName + KMZFileExtension;
        FileUtility.createOutputDirectory(DefaultExportDirectory.getAbsolutePath());
        FileUtility.createOutputDirectory(temporaryDirectory);

        this.kmlRelativePathExportThread = new KMLRelativePathExportThread(map,
                                                                           extendedData,
                                                                           new IEmpExportToStringCallback(){
                                                                                                               @Override
                                                                                                               public void exportSuccess(String stringFmt)
                                                                                                               {
                                                                                                                   KMZExportThread.createKMZFile(stringFmt,
                                                                                                                                                 KMZExportThread.this.temporaryDirectory,
                                                                                                                                                 KMZExportThread.this.kmzFileName,
                                                                                                                                                 KMZExportThread.this.callback);
                                                                                                               }
                                                                                                               @Override
                                                                                                               public void exportFailed(Exception Ex)
                                                                                                               {
                                                                                                                   KMZExportThread.this.callback.exportFailed(Ex);
                                                                                                               }
                                                                                                           },
                                                                           temporaryDirectory);
    }

    /**
     * This exports the overlay specified that is displayed on the map
     * to a KMZ file given the directory where the KMZ file should be stored as
     * well as the desired name of the KMZ file.
     *
     * @param map the map that contains the overlay to be exported.
     * @param overlay the overlay to be exported.
     * @param extendedData whether or not extended data should be exported
     * @param callback the callback which will provide the KMZ file created when the thread is finished or report a failure
     * @param temporaryDirectory the temporary directory location( it is highly recommended to use
     *                                   Context.getExternalFilesDir() as the temporary location).
     *                                   The contents of this directory will be removed after export.
     * @param kmzFileName the name of the exported KMZ File Name (i.e. kmz_file_export.kmz or kmz_file_export).
     */
    protected KMZExportThread(final IMap                           map,
                              final IOverlay                       overlay,
                              final boolean                        extendedData,
                              final IEmpExportToTypeCallBack<File> callback,
                              final String                         temporaryDirectory,
                              final String                         kmzFileName)
    {
        this.callback           = callback;
        this.temporaryDirectory = new File(temporaryDirectory);
        this.kmzFileName        = kmzFileName.toLowerCase()
                                             .endsWith(KMZFileExtension) ? kmzFileName
                                                                         : kmzFileName + KMZFileExtension;
        FileUtility.createOutputDirectory(DefaultExportDirectory.getAbsolutePath());
        FileUtility.createOutputDirectory(temporaryDirectory);

        this.kmlRelativePathExportThread = new KMLRelativePathExportThread(map,
                                                                           overlay,
                                                                           extendedData,
                                                                           new IEmpExportToStringCallback(){
                                                                                                               @Override
                                                                                                               public void exportSuccess(String stringFmt)
                                                                                                               {
                                                                                                                   KMZExportThread.createKMZFile(stringFmt,
                                                                                                                                                 KMZExportThread.this.temporaryDirectory,
                                                                                                                                                 KMZExportThread.this.kmzFileName,
                                                                                                                                                 KMZExportThread.this.callback);
                                                                                                               }

                                                                                                               @Override
                                                                                                               public void exportFailed(Exception Ex)
                                                                                                               {
                                                                                                                   KMZExportThread.this.callback.exportFailed(Ex);
                                                                                                               }
                                                                                                           },
                                                                           temporaryDirectory);

    }

    /**
     * This exports the feature specified that is displayed on the map
     * to a KMZ file given the directory where the KMZ file should be stored as
     * well as the desired name of the KMZ file.
     *
     * @param map the map that contains the overlay to be exported.
     * @param feature the feature to be exported.
     * @param extendedData whether or not extended data should be exported
     * @param callback the callback which will provide the KMZ file created when the thread is finished or report a failure
     * @param temporaryDirectory the temporary directory location( it is highly recommended to use
     *                                   Context.getExternalFilesDir() as the temporary location).
     *                                   The contents of this directory will be removed after export.
     * @param kmzFileName the name of the exported KMZ File Name (i.e. kmz_file_export.kmz or kmz_file_export).
     */
    protected KMZExportThread(final IMap                           map,
                              final IFeature                       feature,
                              final boolean                        extendedData,
                              final IEmpExportToTypeCallBack<File> callback,
                              final String                         temporaryDirectory,
                              final String                         kmzFileName)
    {
        this.callback           = callback;
        this.temporaryDirectory = new File(temporaryDirectory);
        this.kmzFileName        = kmzFileName.toLowerCase()
                                             .endsWith(KMZFileExtension) ? kmzFileName
                                                                         : kmzFileName + KMZFileExtension;

        FileUtility.createOutputDirectory(DefaultExportDirectory.getAbsolutePath());
        FileUtility.createOutputDirectory(temporaryDirectory);

        this.kmlRelativePathExportThread = new KMLRelativePathExportThread(map,
                                                                           feature,
                                                                           extendedData,
                                                                           new IEmpExportToStringCallback(){
                                                                                                               @Override
                                                                                                               public void exportSuccess(String stringFmt)
                                                                                                               {
                                                                                                                   KMZExportThread.createKMZFile(stringFmt,
                                                                                                                                                 KMZExportThread.this.temporaryDirectory,
                                                                                                                                                 KMZExportThread.this.kmzFileName,
                                                                                                                                                 KMZExportThread.this.callback);
                                                                                                               }

                                                                                                               @Override
                                                                                                               public void exportFailed(Exception Ex)
                                                                                                               {
                                                                                                                   KMZExportThread.this.callback.exportFailed(Ex);
                                                                                                               }
                                                                                                           },
                                                                           temporaryDirectory);

    }



    @Override
    public void run()
    {
        super.run();
        this.kmlRelativePathExportThread.run();
        this.kmlRelativePathExportThread.start();
    }

    private static void createKMZFile(final String                         kmlString,
                                      final File                           temporaryDirectory,
                                      final String                         kmzFileName,
                                      final IEmpExportToTypeCallBack<File> callback)
    {
        //create the zip file with the given filename passed
        File zipFile = new File(DefaultExportDirectory +
                                File.separator +
                                kmzFileName);

        //Create the kml file at the given temporary directory
        //where the related image files are also located
        File kmlFile = new File(temporaryDirectory.getAbsolutePath() + File.separator + DefaultKMLFileName);

        try(FileOutputStream out = new FileOutputStream(kmlFile.getAbsoluteFile()))
        {

            //write the kml to the temporary directory
            byte[] byteArray = kmlString.getBytes();
            out.write(byteArray, 0, byteArray.length);
            out.flush();
            //zip up directory containing the kml file
            //as well as the Image directory containing
            //the referenced images
            ZipUtility.zip(temporaryDirectory, zipFile);
            callback.exportSuccess(zipFile);
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
