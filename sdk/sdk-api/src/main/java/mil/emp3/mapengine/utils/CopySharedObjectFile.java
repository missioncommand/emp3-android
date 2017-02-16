package mil.emp3.mapengine.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * EMP3 supports loading of a map engine (implementation of IMpaInstance) from another Android Application.
 * A map engine may have native code in shared object (libraries) that needs to be made accessible. Those
 * libraries need to be copied to a location that the host application can access. CopySharedObjectFile performs this task.
 *
 * This code is derived from: https://github.com/singwhatiwanna/dynamic-load-apk/blob/master/README-en.md
 * It is used to copy shared object (so) libraries from the APK containing the map engine to the host application.
 *
 * Copyright (C) 2014 singwhatiwanna() <singwhatiwanna@gmail.com>
 *     collaborator:
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 */

public class CopySharedObjectFile {
    private final String TAG = CopySharedObjectFile.class.getSimpleName();

    private final String mSoFileName;
    private final ZipFile mZipFile;
    private final ZipEntry mZipEntry;
    private final Context mContext;
    private final String mNativeLibDir;
    private final long mLastModityTime;

    /**
     *
     * @param context
     * @param zipFile
     * @param zipEntry
     * @param lastModify
     * @param engineName
     */
    public CopySharedObjectFile(Context context, ZipFile zipFile, ZipEntry zipEntry, long lastModify, String engineName) {
        mZipFile = zipFile;
        mContext = context;
        mZipEntry = zipEntry;
        mSoFileName = parseSoFileName(zipEntry.getName());
        mLastModityTime = lastModify;
        mNativeLibDir = mContext.getDir(engineName, Context.MODE_PRIVATE).getAbsolutePath();
    }

    /**
     * Get name of a directory where shared object libraries for this engine will be copied to.
     * @param context
     * @param engineName
     * @return
     */
    public static String getNativeLibDir(Context context, String engineName) {
        return context.getDir(engineName, Context.MODE_PRIVATE).getAbsolutePath();
    }

    /**
     * If application has already run once or multiple instances of map engine are created then we don't want to
     * overwrite what was copied earlier. So time stamp of the last copy is stored away.
     * @param cxt
     * @param soName
     * @param time
     */
    public static void setSoLastModifiedTime(Context cxt, String soName, long time) {
        SharedPreferences prefs = cxt.getSharedPreferences("engine_shared_objects",
                Context.MODE_PRIVATE | Context.MODE_MULTI_PROCESS);
        prefs.edit().putLong(soName, time).apply();
    }

    /**
     * Fetches the previously stored last modified time stamp.
     * @param cxt
     * @param soName
     * @return
     */
    public static long getSoLastModifiedTime(Context cxt, String soName) {
        SharedPreferences prefs = cxt.getSharedPreferences("engine_shared_objects",
                Context.MODE_PRIVATE | Context.MODE_MULTI_PROCESS);
        return prefs.getLong(soName, 0);
    }

    /**
     * Extracts name of the shared object file from the zip entry.
     * @param zipEntryName
     * @return
     */
    private final String parseSoFileName(String zipEntryName) {
        return zipEntryName.substring(zipEntryName.lastIndexOf("/") + 1);
    }

    /**
     * Writes the shared object file to destination directory.
     * @throws IOException
     */
    private void writeSoFile2LibDir() throws IOException {
        InputStream is = null;
        FileOutputStream fos = null;
        is = mZipFile.getInputStream(mZipEntry);
        fos = new FileOutputStream(new File(mNativeLibDir, mSoFileName));
        copy(is, fos);
        // mZipFile.close();
    }

    /**
     * Copies input stream (source of shared object file from map engine APK) to output file
     * @param is
     * @param os
     * @throws IOException
     */
    public void copy(InputStream is, OutputStream os) throws IOException {
        if (is == null || os == null)
            return;
        BufferedInputStream bis = new BufferedInputStream(is);
        BufferedOutputStream bos = new BufferedOutputStream(os);
        int size = getAvailableSize(bis);
        byte[] buf = new byte[size];
        int i = 0;
        while ((i = bis.read(buf, 0, size)) != -1) {
            bos.write(buf, 0, i);
        }
        bos.flush();
        bos.close();
        bis.close();
    }

    /**
     * Fetches the size of available data from input stream.
     * @param is
     * @return
     * @throws IOException
     */
    private int getAvailableSize(InputStream is) throws IOException {
        if (is == null)
            return 0;
        int available = is.available();
        return available <= 0 ? 1024 : available;
    }

    /**
     * Copy shared object from the mapengine application to host application directory.
     */
    public void copy() {
        try {
            writeSoFile2LibDir();
            setSoLastModifiedTime(mContext, mZipEntry.getName(), mLastModityTime);
            Log.d(TAG, "copy so lib success: " + mZipEntry.getName());
        } catch (IOException e) {
            Log.e(TAG, "copy so lib failed: " + e.toString());
            e.printStackTrace();
        }

    }
}
