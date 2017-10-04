package mil.emp3.api.utils;

import java.io.File;

/**
 * Created by jenifer.cochran@rgi-corp.local on 10/4/17.
 */

public final class FileUtility
{
    private FileUtility()
    {

    }

    public static void deleteFolder(File folder)
    {
        File[] files = folder.listFiles();
        if (files != null)
        {
            for (File file : files)
            {
                if (file.isDirectory())
                {
                    deleteFolder(file);
                } else
                {
                    file.delete();
                }
            }
        }
        folder.delete();
    }

}
