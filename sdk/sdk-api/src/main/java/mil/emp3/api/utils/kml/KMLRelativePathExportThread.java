package mil.emp3.api.utils.kml;

import android.graphics.Bitmap;
import android.os.Environment;
import android.util.Log;
import android.util.SparseArray;

import org.cmapi.primitives.IGeoMilSymbol;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Set;

import armyc2.c2sd.renderer.MilStdIconRenderer;
import armyc2.c2sd.renderer.utilities.ImageInfo;
import mil.emp3.api.MilStdSymbol;
import mil.emp3.api.enums.MilStdLabelSettingEnum;
import mil.emp3.api.interfaces.IEmpExportToStringCallback;
import mil.emp3.api.interfaces.IFeature;
import mil.emp3.api.interfaces.IMap;
import mil.emp3.api.interfaces.IOverlay;

/**
 * Created by jenifer.Cochran on 9/28/2017.
 */

class KMLRelativePathExportThread extends KMLExportThread
{

    private static final String TAG = "KMZExporterThread";
    private final String outputDirectory;

    protected KMLRelativePathExportThread(IMap                       map,
                                          boolean                    extendedData,
                                          IEmpExportToStringCallback callback,
                                          String                     outputDirectory)
    {
        super(map, extendedData, callback);
        if(!directoryExists(outputDirectory))
        {
            throw new IllegalArgumentException("The directory %s does not exist. Must pass a valid ");
        }
        this.outputDirectory = outputDirectory;
    }

    protected KMLRelativePathExportThread(IMap                       map,
                                          IOverlay                   overlay,
                                          boolean                    extendedData,
                                          IEmpExportToStringCallback callback,
                                          String                     outputDirectory)
    {
        super(map, overlay, extendedData, callback);
        if(!directoryExists(outputDirectory))
        {
            throw new IllegalArgumentException("The directory %s does not exist. Must pass a valid ");
        }
        this.outputDirectory = outputDirectory;
    }

    protected KMLRelativePathExportThread(IMap map, IFeature feature, boolean extendedData, IEmpExportToStringCallback callback, String outputDirectory)
    {
        super(map, feature, extendedData, callback);
        if(!directoryExists(outputDirectory))
        {
            throw new IllegalArgumentException("The directory %s does not exist. Must pass a valid ");
        }
        this.outputDirectory = outputDirectory;
    }

    @Override
    protected String getMilStdSinglePointIconURL(MilStdSymbol                feature,
                                                 MilStdLabelSettingEnum      eLabelSetting,
                                                 Set<IGeoMilSymbol.Modifier> labelSet,
                                                 SparseArray<String>         attributes)
    {
        MilStdIconRenderer mir = MilStdIconRenderer.getInstance();
        ImageInfo icon = mir.RenderIcon(feature.getSymbolCode(), feature.getUnitModifiers(MilStdLabelSettingEnum.ALL_LABELS),attributes);
//        icon.getImage().
        return super.getMilStdSinglePointIconURL(feature, eLabelSetting, labelSet, attributes);
    }

    private void storeImage(Bitmap image) throws IOException
    {
        File pictureFile = getOutputMediaFile(this.outputDirectory, "asdfasdf.PNG");
        if (pictureFile == null)
        {
            Log.d(TAG, "Error creating media file, check storage permissions: ");// e.getMessage());
            return;
        }
        try {
            FileOutputStream fos = new FileOutputStream(pictureFile);
            image.compress(Bitmap.CompressFormat.PNG, 90, fos);
            fos.close();
        } catch (FileNotFoundException e) {
            Log.d(TAG, "File not found: " + e.getMessage());
        } catch (IOException e) {
            Log.d(TAG, "Error accessing file: " + e.getMessage());
        }
    }
    /** Create a File for saving an image or video */
    private  File getOutputMediaFile(String outputDirectory, String fileName) throws IOException
    {
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.
        if(!isExternalStorageWritable())
        {
            throw new IOException("Unable to write to external storage");
        }

        return new File(outputDirectory + File.separator + fileName);
    }

    public static boolean directoryExists(String directoryPath)
    {
        File dir = new File(directoryPath);
        return dir.exists() && dir.isDirectory();
    }

    /* Checks if external storage is available for read and write */
    public static boolean isExternalStorageWritable()
    {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state);
    }

}
