package mil.emp3.api.utils.kmz;

import android.graphics.Bitmap;
import android.os.Environment;
import android.util.Log;
import android.util.SparseArray;

import org.cmapi.primitives.IGeoMilSymbol;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Set;

import armyc2.c2sd.renderer.MilStdIconRenderer;
import armyc2.c2sd.renderer.utilities.ImageInfo;
import mil.emp3.api.MilStdSymbol;
import mil.emp3.api.enums.MilStdLabelSettingEnum;
import mil.emp3.api.interfaces.IEmpExportToStringCallback;
import mil.emp3.api.interfaces.IFeature;
import mil.emp3.api.interfaces.IMap;
import mil.emp3.api.interfaces.IOverlay;
import mil.emp3.api.utils.FileUtility;
import mil.emp3.api.utils.MilStdUtilities;
import mil.emp3.api.utils.kml.KMLExportThread;

/**
 * Created by jenifer.Cochran on 9/28/2017.
 */

public class KMLRelativePathExportThread extends KMLExportThread
{

    private static final String TAG = "KMZExporterThread";
    private static final String ImageDirectory = "Image";
    private static final String ImageExtension = ".PNG";
    private final String outputDirectory;

    private static final HashMap<String, Integer> imageNameHash = new HashMap<>();
    private static int uniqueImageCount = 0;

    protected KMLRelativePathExportThread(IMap                       map,
                                          boolean                    extendedData,
                                          IEmpExportToStringCallback callback,
                                          String                     outputDirectory)
    {
        super(map, extendedData, callback);

        if(!directoryExists(outputDirectory))
        {
            throw new IllegalArgumentException("The directory %s does not exist. Must pass a valid and existing directory.");
        }

        this.outputDirectory = outputDirectory;
        createImageDirectory(outputDirectory);
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
            throw new IllegalArgumentException("The directory %s does not exist. Must pass a valid and existing directory.");
        }

        this.outputDirectory = outputDirectory;
        createImageDirectory(outputDirectory);
    }

    protected KMLRelativePathExportThread(IMap                       map,
                                          IFeature                   feature,
                                          boolean                    extendedData,
                                          IEmpExportToStringCallback callback,
                                          String                     outputDirectory)
    {
        super(map, feature, extendedData, callback);

        if(!directoryExists(outputDirectory))
        {
            throw new IllegalArgumentException("The directory %s does not exist. Must pass a valid and existing directory.");
        }

        this.outputDirectory = outputDirectory;
        createImageDirectory(outputDirectory);
    }

    @Override
    protected String getMilStdSinglePointIconURL(MilStdSymbol                feature,
                                                 MilStdLabelSettingEnum      eLabelSetting,
                                                 Set<IGeoMilSymbol.Modifier> labelSet,
                                                 SparseArray<String>         attributes) throws IOException
    {
        MilStdIconRenderer mir = MilStdIconRenderer.getInstance();
        ImageInfo icon = mir.RenderIcon(feature.getSymbolCode(), feature.getUnitModifiers(MilStdLabelSettingEnum.ALL_LABELS),attributes);
        String keyValue = MilStdUtilities.getMilStdSinglePointParams(feature, eLabelSetting, labelSet, attributes);
        String fileName = KMLRelativePathExportThread.getImageFileName(keyValue);

        storeImage(icon.getImage(), fileName);

        return ImageDirectory + File.separator + fileName;
    }

    /**
     * Gets an image file Name that if another image with the same
     * image was created, it will reference the file name of a previous image
     * (this will let us not duplicate image files when they are the same)
     *
     * @param militarySymbologyParams the parameters that make up the military symbology
     * @return the name of the image file
     */
    private static String getImageFileName(String militarySymbologyParams)
    {
        String fileName = "";
        if(imageNameHash.containsKey(militarySymbologyParams))
        {
            fileName = imageNameHash.get(militarySymbologyParams) + ImageExtension;
        }
        else
        {
            fileName = uniqueImageCount + ImageExtension;
            imageNameHash.put(militarySymbologyParams, uniqueImageCount);
            uniqueImageCount++;
        }

        return fileName;
    }



    private static void createImageDirectory(String outputDirectory)
    {
        File imageDirectory = new File(outputDirectory + File.separator + ImageDirectory);
        if(imageDirectory.exists())
        {
            FileUtility.deleteFolder(imageDirectory);
        }
        imageDirectory.mkdir();
    }

    private void storeImage(Bitmap image, String fileName) throws IOException
    {
        File pictureFile = getOutputMediaFile(this.outputDirectory, fileName);

        if(pictureFile.exists())
        {
            return;
        }

        if (pictureFile == null)
        {
            Log.d(TAG, "Error creating media file, check storage permissions: ");
            return;
        }

        try(FileOutputStream fos = new FileOutputStream(pictureFile))
        {
            image.compress(Bitmap.CompressFormat.PNG, 90, fos);
        }
        catch (FileNotFoundException e)
        {
            Log.d(TAG, "File not found: " + e.getMessage());
        }
        catch (IOException e)
        {
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

        return new File(outputDirectory + File.separator + ImageDirectory + File.separator + fileName);
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
