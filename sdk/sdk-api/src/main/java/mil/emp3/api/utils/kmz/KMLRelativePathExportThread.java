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
 * Exports the KML file in a way that it uses relative pathing
 * to reference images stored locally. For instance, Single Point
 * Military Symbology cannot be queried on the server therefore a graphic
 * is saved locally and the pathing in the KML will refer to the local
 * storage instead of pointing to a server.
 *
 * @author Jenifer Cochran
 */

public class KMLRelativePathExportThread extends KMLExportThread
{

    private static final String TAG = "KMLRelativePathExport";
    private static final String ImageDirectory = "Image";
    private static final String ImageExtension = ".PNG";
    private final String outputDirectory;

    private static final HashMap<String, Integer> imageNameHash = new HashMap<>();
    private static int uniqueImageCount = 0;


    /***
     * This exports the map's overlays, and features displayed on the map
     * to a KML file.
     *
     * @param map the map that contains the overlays and feature data to be exported.
     * @param extendedData whether or not extended data should be exported.
     * @param callback the callback which will provide the KML file created when the
     *                 thread is finished or report a failure
     * @param outputDirectory the upper level directory of where to store the locally
     *                        referenced image files (i.e. storage/0/emulated/Pictures
     *                        then all the image files will be created storage/0/emulated/Pictures/Image
     *                        and the KML images will be referenced Image/my_graphic.png in the KML)
     */
    protected KMLRelativePathExportThread(IMap                       map,
                                          boolean                    extendedData,
                                          IEmpExportToStringCallback callback,
                                          String                     outputDirectory)
    {
        super(map, extendedData, callback);

        if(!FileUtility.directoryExists(outputDirectory))
        {
            throw new IllegalArgumentException("The directory %s does not exist. Must pass a valid and existing directory.");
        }

        this.outputDirectory = outputDirectory;
        createImageDirectory(outputDirectory);
    }


    /***
     * This exports the overlay specified that is displayed on the map
     * to a KML file.
     *
     * @param map the map that contains the overlay to be exported.
     * @param overlay the overlay to be exported.
     * @param extendedData whether or not extended data should be exported.
     * @param callback the callback which will provide the KML file created when the
     *                 thread is finished or report a failure
     * @param outputDirectory the upper level directory of where to store the locally
     *                        referenced image files (i.e. storage/0/emulated/Pictures
     *                        then all the image files will be created storage/0/emulated/Pictures/Image
     *                        and the KML images will be referenced Image/my_graphic.png in the KML)
     */
    protected KMLRelativePathExportThread(IMap                       map,
                                          IOverlay                   overlay,
                                          boolean                    extendedData,
                                          IEmpExportToStringCallback callback,
                                          String                     outputDirectory)
    {
        super(map, overlay, extendedData, callback);

        if(!FileUtility.directoryExists(outputDirectory))
        {
            throw new IllegalArgumentException("The directory %s does not exist. Must pass a valid and existing directory.");
        }

        this.outputDirectory = outputDirectory;
        createImageDirectory(outputDirectory);
    }


    /***
     * This exports the feature specified that is displayed on the map
     * to a KML file.
     *
     * @param map the map that contains the feature to be exported.
     * @param feature the feature to be exported.
     * @param extendedData whether or not extended data should be exported.
     * @param callback the callback which will provide the KML file created when the
     *                 thread is finished or report a failure
     * @param outputDirectory the upper level directory of where to store the locally
     *                        referenced image files (i.e. storage/0/emulated/Pictures
     *                        then all the image files will be created storage/0/emulated/Pictures/Image
     *                        and the KML images will be referenced Image/my_graphic.png in the KML)
     */
    protected KMLRelativePathExportThread(IMap                       map,
                                          IFeature                   feature,
                                          boolean                    extendedData,
                                          IEmpExportToStringCallback callback,
                                          String                     outputDirectory)
    {
        super(map, feature, extendedData, callback);

        if(!FileUtility.directoryExists(outputDirectory))
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
        MilStdIconRenderer mir      = MilStdIconRenderer.getInstance();
        ImageInfo          icon     = mir.RenderIcon(feature.getSymbolCode(),
                                                     feature.getUnitModifiers(MilStdLabelSettingEnum.ALL_LABELS),
                                                     attributes);
        String             keyValue = MilStdUtilities.getMilStdSinglePointParams(feature,
                                                                                 eLabelSetting,
                                                                                 labelSet,
                                                                                 attributes);
        String             fileName = KMLRelativePathExportThread.getImageFileName(keyValue);

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
        //if we already have this stored, reference the image with the same
        //file name NOTE: file names are in the form <#>.PNG
        if(imageNameHash.containsKey(militarySymbologyParams))
        {
            fileName = imageNameHash.get(militarySymbologyParams) + ImageExtension;
        }
        else
        {
            //otherwise we have not created this image before and
            //add it to our set
            fileName = uniqueImageCount + ImageExtension;
            imageNameHash.put(militarySymbologyParams, uniqueImageCount);
            uniqueImageCount++;
        }

        return fileName;
    }

    /**
     * Creates the Image Directory containing the locally referenced
     * image files
     *
     * @param outputDirectory where the image directory should be created
     */
    private static void createImageDirectory(String outputDirectory)
    {
        File imageDirectory = new File(outputDirectory + File.separator + ImageDirectory);
        if(imageDirectory.exists())
        {
            FileUtility.deleteFolder(imageDirectory);
        }
        imageDirectory.mkdir();
    }

    /***
     * Stores the Image locally on disk
     *
     * @param image the image to be stored
     * @param fileName the name of the image file
     * @throws IOException
     */
    private void storeImage(Bitmap image, String fileName) throws IOException
    {
        File pictureFile = getOutputMediaFile(this.outputDirectory, fileName);

        //If the image is already in the desired location,
        //no need to write it again since this indicates that
        //the image is referencing the same one
        //NOTE: this method assumes that if a fileName is the same
        //as another one in the directory, then the image is also
        //the same
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
            //the 90 is the quality, which will be ignored since we are
            //doing a PNG format which is lossless format
            //Reference: https://developer.android.com/reference/android/graphics/Bitmap.html
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

    /***
     * Creates the Image file at the appropriate location as well as checking
     * storage permissions.
     *
     * @param outputDirectory the base directory
     * @param fileName the name of the image file
     * @return a file with its location at the base directory in the Image folder location
     * @throws IOException thrown if unable to write to external storage
     */
    private  File getOutputMediaFile(String outputDirectory, String fileName) throws IOException
    {
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.
        if(!FileUtility.isExternalStorageWritable())
        {
            throw new IOException("Unable to write to external storage");
        }

        return new File(outputDirectory + File.separator + ImageDirectory + File.separator + fileName);
    }
}
