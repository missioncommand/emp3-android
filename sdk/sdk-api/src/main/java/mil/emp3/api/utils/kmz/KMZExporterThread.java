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

public final class KMZExporterThread extends Thread
{
    private final File                           temporaryDirectory;
    private final IEmpExportToTypeCallBack<File> callback;
    private final String                         kmzFileName;
    private final KMLRelativePathExportThread    kmlRelativePathExportThread;

    private final static String DefaultKMLFileName     = "kml_export.kml";
    private final static String KMZFileExtension       = ".kmz";
    private final static File   DefaultExportDirectory = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + File.separator + "KMZExport");


    public KMZExporterThread(final IMap                           map,
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
        createOutputDirectory(DefaultExportDirectory.getAbsolutePath());
        createOutputDirectory(temporaryDirectory);

        this.kmlRelativePathExportThread = new KMLRelativePathExportThread(map,
                                                                           extendedData,
                                                                           new IEmpExportToStringCallback()
                                                                           {
                                                                               @Override
                                                                               public void exportSuccess(String stringFmt)
                                                                               {
                                                                                   createKMZFile(stringFmt,
                                                                                                 KMZExporterThread.this.temporaryDirectory,
                                                                                                 KMZExporterThread.this.kmzFileName,
                                                                                                 KMZExporterThread.this.callback);
                                                                               }

                                                                               @Override
                                                                               public void exportFailed(Exception Ex)
                                                                               {
                                                                                   KMZExporterThread.this.callback.exportFailed(Ex);
                                                                               }
                                                                           },
                                                                           temporaryDirectory);
    }

    protected KMZExporterThread(final IMap                           map,
                                final IOverlay                       overlay,
                                final boolean                        extendedData,
                                final IEmpExportToTypeCallBack<File> callback,
                                final String temporaryDirectory,
                                final String                         kmzFileName)
    {
        this.callback           = callback;
        this.temporaryDirectory = new File(temporaryDirectory);
        this.kmzFileName        = kmzFileName.toLowerCase()
                                             .endsWith(KMZFileExtension) ? kmzFileName
                                                                         : kmzFileName + KMZFileExtension;
        createOutputDirectory(DefaultExportDirectory.getAbsolutePath());
        createOutputDirectory(temporaryDirectory);

        this.kmlRelativePathExportThread = new KMLRelativePathExportThread(map,
                                                                           overlay,
                                                                           extendedData,
                                                                           new IEmpExportToStringCallback(){
                                                                                                               @Override
                                                                                                               public void exportSuccess(String stringFmt)
                                                                                                               {
                                                                                                                   createKMZFile(stringFmt,
                                                                                                                                 KMZExporterThread.this.temporaryDirectory,
                                                                                                                                 KMZExporterThread.this.kmzFileName,
                                                                                                                                 KMZExporterThread.this.callback);
                                                                                                               }

                                                                                                               @Override
                                                                                                               public void exportFailed(Exception Ex)
                                                                                                               {
                                                                                                                   KMZExporterThread.this.callback.exportFailed(Ex);
                                                                                                               }
                                                                                                           },
                                                                           temporaryDirectory);

    }

    protected KMZExporterThread(final IMap                           map,
                                final IFeature                       feature,
                                final boolean                        extendedData,
                                final IEmpExportToTypeCallBack<File> callback,
                                final String temporaryDirectory,
                                final String                         kmzFileName)
    {
        this.callback           = callback;
        this.temporaryDirectory = new File(temporaryDirectory);
        this.kmzFileName        = kmzFileName.toLowerCase()
                                             .endsWith(KMZFileExtension) ? kmzFileName
                                                                         : kmzFileName + KMZFileExtension;

        createOutputDirectory(DefaultExportDirectory.getAbsolutePath());
        createOutputDirectory(temporaryDirectory);

        this.kmlRelativePathExportThread = new KMLRelativePathExportThread(map,
                                                                           feature,
                                                                           extendedData,
                                                                           new IEmpExportToStringCallback(){
                                                                                                               @Override
                                                                                                               public void exportSuccess(String stringFmt)
                                                                                                               {
                                                                                                                   createKMZFile(stringFmt,
                                                                                                                                 KMZExporterThread.this.temporaryDirectory,
                                                                                                                                 KMZExporterThread.this.kmzFileName,
                                                                                                                                 KMZExporterThread.this.callback);
                                                                                                               }

                                                                                                               @Override
                                                                                                               public void exportFailed(Exception Ex)
                                                                                                               {
                                                                                                                   KMZExporterThread.this.callback.exportFailed(Ex);
                                                                                                               }
                                                                                                           },
                                                                           temporaryDirectory);

    }

    private static void createOutputDirectory(String outputDirectory)
    {
        File directory = new File(outputDirectory);
        directory.mkdirs();
        if(!directory.isDirectory())
        {
            throw new IllegalArgumentException(String.format("The temporaryDirectory must be a path to a Directory. %s is not a directory.",
                                               outputDirectory));
        }
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
        //create the zip file given
        File zipFile = new File(DefaultExportDirectory +
                                File.separator +
                                kmzFileName);

        //Zip files up and rename the extension to .kmz
        //Create the kml file at the given kmz directory
        File kmlFile = new File(temporaryDirectory.getAbsolutePath() + File.separator + DefaultKMLFileName);

        try(FileOutputStream out = new FileOutputStream(kmlFile.getAbsoluteFile()))
        {

            //write the kml to the temporary directory
            byte[] byteArray = kmlString.getBytes();
            out.write(byteArray, 0, byteArray.length);
            out.flush();

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
