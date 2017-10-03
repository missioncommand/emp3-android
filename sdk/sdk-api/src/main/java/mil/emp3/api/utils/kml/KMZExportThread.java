package mil.emp3.api.utils.kml;

import android.os.Environment;
import android.util.Log;
import android.util.Xml;

import org.xmlpull.v1.XmlSerializer;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import mil.emp3.api.interfaces.IEmpExportToStringCallback;
import mil.emp3.api.interfaces.IFeature;
import mil.emp3.api.interfaces.IMap;
import mil.emp3.api.interfaces.IOverlay;

import static mil.emp3.api.utils.kml.ZipUtils.callZip;

/**
 * Created by Matt.Miller on 9/27/2017.
 */

public class KMZExportThread extends KMLExportThread {
    private final static String TAG = KMZExportThread.class.getSimpleName();

    int i = 0;
    protected KMZExportThread(IMap map, boolean extendedData, IEmpExportToStringCallback callback) {
        super(map, extendedData, callback);
    }

    protected KMZExportThread(IMap map, IOverlay overlay, boolean extendedData, IEmpExportToStringCallback callback) {
        super(map, overlay, extendedData, callback);
    }

    protected KMZExportThread(IMap map, IFeature feature, boolean extendedData, IEmpExportToStringCallback callback) {
        super(map, feature, extendedData, callback);
    }

    private void serializeHRef(String URI, XmlSerializer xmlSerializer) throws IOException {
            if ((null != URI) && !URI.isEmpty()) {
                xmlSerializer.startTag(null, "href");
                URL url = new URL(URI);
                HttpURLConnection huc = (HttpURLConnection) url.openConnection();
                huc.setRequestMethod("HEAD");
                int responseCode = huc.getResponseCode();
                if (responseCode == 200) {
                    url = new URL(URI);
                    InputStream in = new BufferedInputStream(url.openStream());
                    ByteArrayOutputStream out = new ByteArrayOutputStream();
                    byte[] buf = new byte[1024];
                    int n = 0;
                    while (-1 != (n = in.read(buf))) {
                        out.write(buf, 0, n);
                    }
                    out.close();
                    in.close();
                    byte[] response = out.toByteArray();
                    FileOutputStream fos = new FileOutputStream(Environment.DIRECTORY_PICTURES + "/kmz/files/image" + String.valueOf(i));
                    fos.write(response);
                    fos.close();
                } else {
                    InputStream input = null;
                    OutputStream output = null;
                    try {
                        input = new FileInputStream(URI);
                        output = new FileOutputStream(Environment.DIRECTORY_PICTURES + "/kmz/files/image" + String.valueOf(i));
                        byte[] buf = new byte[1024];
                        int bytesRead;
                        while ((bytesRead = input.read(buf)) > 0) {
                            output.write(buf, 0, bytesRead);
                        }
                    } finally {
                        input.close();
                        output.close();
                    }
                }

            }
            xmlSerializer.text(Environment.DIRECTORY_PICTURES + "/kmz/files/image" + String.valueOf(i));
            i += 1;
            xmlSerializer.endTag(null, "href");
    }

    @Override
    public void run() {
        try {
            String kmlString = "";

            XmlSerializer xmlSerializer = Xml.newSerializer();

            if (null != this.overlay) {
                kmlString = export(this.overlay, xmlSerializer);
            } else if (null != this.feature) {
                kmlString = export(this.feature, xmlSerializer);
            } else if (null != this.map) {
                kmlString = export(this.map, xmlSerializer);
            }
            try {
                this.callback.exportSuccess(kmlString);
            } catch (Exception Ex) {
                Log.e(TAG, "Exception raised in export callback.", Ex);
            }
        } catch (Exception Ex) {
            this.callback.exportFailed(Ex);
        }
        String sourceFolder = Environment.DIRECTORY_PICTURES + "/kmz";
        String outputFolder = "EMP_Export.kmz";callZip(sourceFolder, outputFolder);
        callZip(sourceFolder, outputFolder);
    }
}
