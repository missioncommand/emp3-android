package mil.emp3.api.utils.kmz;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipOutputStream;

import mil.emp3.api.interfaces.IEmpExportToStringCallback;
import mil.emp3.api.interfaces.IEmpExportToTypeCallBack;
import mil.emp3.api.interfaces.IFeature;
import mil.emp3.api.interfaces.IMap;
import mil.emp3.api.interfaces.IOverlay;

/**
 * Created by jenifer.Cochran on 9/28/2017.
 *
 */

public class KMZExporterThread extends Thread
{
    private final File                           outputDirectory;
    private final IEmpExportToTypeCallBack<File> callback;
    private final IMap                           map;
    private final boolean                        extendedData;
    private final IOverlay                       overlay;
    private final IFeature                       feature;
    private final ExportType                     exportType;


    protected enum ExportType
    {
        Map,
        Overlay,
        Feature
    }


    public KMZExporterThread(final IMap                           map,
                             final boolean                        extendedData,
                             final IEmpExportToTypeCallBack<File> callback,
                             final String                         outputDirectory)
    {
        this.outputDirectory = new File(outputDirectory);
        createOutputDirectory(outputDirectory);

        this.callback     = callback;
        this.map          = map;
        this.extendedData = extendedData;
        this.overlay      = null;
        this.feature      = null;
        this.exportType   = ExportType.Map;
    }



    protected KMZExporterThread(IMap                           map,
                                IOverlay                       overlay,
                                boolean                        extendedData,
                                IEmpExportToTypeCallBack<File> callback,
                                String                         outputDirectory)
    {
        this.outputDirectory = new File(outputDirectory);
        createOutputDirectory(outputDirectory);

        this.callback     = callback;
        this.map          = map;
        this.extendedData = extendedData;
        this.overlay      = overlay;
        this.feature      = null;
        this.exportType   = ExportType.Overlay;

    }

    protected KMZExporterThread(IMap                           map,
                                IFeature                       feature,
                                boolean                        extendedData,
                                IEmpExportToTypeCallBack<File> callback,
                                String                         outputDirectory)
    {
        this.outputDirectory = new File(outputDirectory);
        createOutputDirectory(outputDirectory);

        this.callback     = callback;
        this.map          = map;
        this.extendedData = extendedData;
        this.overlay      = null;
        this.feature      = feature;
        this.exportType   = ExportType.Feature;
    }

    private static void createOutputDirectory(String outputDirectory)
    {
        File directory = new File(outputDirectory);
        directory.mkdirs();
        if(!directory.isDirectory())
        {
            throw new IllegalArgumentException(String.format("The outputDirectory must be a path to a Directory. %s is not a directory.", outputDirectory));
        }

    }

    @Override
    public void run()
    {
        super.run();

        KMLRelativePathExportThread kmlRelativePathExportThread = null;

        switch (this.exportType)
        {

            case Map:
                 kmlRelativePathExportThread = new KMLRelativePathExportThread(this.map,
                                                                               this.extendedData,
                                                                               new IEmpExportToStringCallback(){
                                                                                                                   @Override
                                                                                                                   public void exportSuccess(String stringFmt)
                                                                                                                   {
                                                                                                                         CreateKMZfile(stringFmt,
                                                                                                                                       KMZExporterThread.this.outputDirectory,
                                                                                                                                       KMZExporterThread.this.callback);
                                                                                                                   }

                                                                                                                   @Override
                                                                                                                   public void exportFailed(Exception Ex)
                                                                                                                   {
                                                                                                                       KMZExporterThread.this.callback.exportFailed(Ex);
                                                                                                                   }
                                                                                                               },
                                                                               this.outputDirectory.getAbsolutePath());

                break;
            case Overlay:
                 kmlRelativePathExportThread = new KMLRelativePathExportThread(this.map,
                                                                               this.overlay,
                                                                               this.extendedData,
                                                                               new IEmpExportToStringCallback()
                                                                               {
                                                                                   @Override
                                                                                   public void exportSuccess(String stringFmt)
                                                                                   {
                                                                                       CreateKMZfile(stringFmt,
                                                                                                     KMZExporterThread.this.outputDirectory,
                                                                                                     KMZExporterThread.this.callback);
                                                                                   }

                                                                                   @Override
                                                                                   public void exportFailed(Exception Ex)
                                                                                   {
                                                                                        KMZExporterThread.this.callback.exportFailed(Ex);
                                                                                   }
                                                                               },
                                                                               this.outputDirectory.getAbsolutePath());
                break;
            case Feature:
                 kmlRelativePathExportThread = new KMLRelativePathExportThread(this.map,
                                                                               this.feature,
                                                                               this.extendedData,
                                                                               new IEmpExportToStringCallback()
                                                                               {
                                                                                   @Override
                                                                                   public void exportSuccess(String stringFmt)
                                                                                   {
                                                                                       CreateKMZfile(stringFmt,
                                                                                                     KMZExporterThread.this.outputDirectory,
                                                                                                     KMZExporterThread.this.callback);
                                                                                   }

                                                                                   @Override
                                                                                   public void exportFailed(Exception Ex)
                                                                                   {
                                                                                        KMZExporterThread.this.callback.exportFailed(Ex);
                                                                                   }
                                                                               },
                                                                               this.outputDirectory.getAbsolutePath());
                break;
        }

        kmlRelativePathExportThread.run();
        kmlRelativePathExportThread.start();

    }

    private static void CreateKMZfile(final String kmlString, final File kmzDirectory, final IEmpExportToTypeCallBack<File> callback)
    {

    }


}
