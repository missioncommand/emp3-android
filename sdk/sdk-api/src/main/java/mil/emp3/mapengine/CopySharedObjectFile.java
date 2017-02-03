package mil.emp3.mapengine;

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
 * This code is derived from: https://github.com/singwhatiwanna/dynamic-load-apk/blob/master/README-en.md
 * It is used to copy shared object (so) libraries from the APK containing the map engine to the host application.
 *
 */

public class CopySharedObjectFile {
    private final String TAG = CopySharedObjectFile.class.getSimpleName();

    private final String mSoFileName;
    private final ZipFile mZipFile;
    private final ZipEntry mZipEntry;
    private final Context mContext;
    private final String mNativeLibDir;
    private final long mLastModityTime;

    public CopySharedObjectFile(Context context, ZipFile zipFile, ZipEntry zipEntry, long lastModify, String engineName) {
        mZipFile = zipFile;
        mContext = context;
        mZipEntry = zipEntry;
        mSoFileName = parseSoFileName(zipEntry.getName());
        mLastModityTime = lastModify;
        mNativeLibDir = mContext.getDir(engineName, Context.MODE_PRIVATE).getAbsolutePath();
    }

    public static String getNativeLibDir(Context context, String engineName) {
        return context.getDir(engineName, Context.MODE_PRIVATE).getAbsolutePath();
    }
    public static void setSoLastModifiedTime(Context cxt, String soName, long time) {
        SharedPreferences prefs = cxt.getSharedPreferences("engine_shared_objects",
                Context.MODE_PRIVATE | Context.MODE_MULTI_PROCESS);
        prefs.edit().putLong(soName, time).apply();
    }

    public static long getSoLastModifiedTime(Context cxt, String soName) {
        SharedPreferences prefs = cxt.getSharedPreferences("engine_shared_objects",
                Context.MODE_PRIVATE | Context.MODE_MULTI_PROCESS);
        return prefs.getLong(soName, 0);
    }

    private final String parseSoFileName(String zipEntryName) {
        return zipEntryName.substring(zipEntryName.lastIndexOf("/") + 1);
    }

    private void writeSoFile2LibDir() throws IOException {
        InputStream is = null;
        FileOutputStream fos = null;
        is = mZipFile.getInputStream(mZipEntry);
        fos = new FileOutputStream(new File(mNativeLibDir, mSoFileName));
        copy(is, fos);
        // mZipFile.close();
    }

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

    private int getAvailableSize(InputStream is) throws IOException {
        if (is == null)
            return 0;
        int available = is.available();
        return available <= 0 ? 1024 : available;
    }

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
