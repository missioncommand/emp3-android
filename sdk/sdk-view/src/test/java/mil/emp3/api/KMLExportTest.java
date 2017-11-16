package mil.emp3.api;

import android.os.Environment;
import android.util.Log;

import org.junit.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import mil.emp3.api.interfaces.IEmpExportToStringCallback;
import mil.emp3.api.utils.kml.EmpKMLExporter;

/**
 * Created by matt.miller@rgi-corp.local on 11/15/17.
 */

public class KMLExportTest {

    @Test
    public void exportPoint() {
        EmpKMLExporter.exportToString(this.map, this.oCurrentSelectedFeature, true, new IEmpExportToStringCallback() {

            @Override
            public void exportSuccess(String kmlString) {
                //Log.i(TAG, kmlString);
                FileOutputStream out = null;
                File sd = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
                File dest = new File(sd, "featureexport.kml");
                if (dest.exists()) {
                    dest.delete();
                }
                try {
                    out = new FileOutputStream(dest);
                    byte[] byteArray = kmlString.getBytes();
                    out.write(byteArray, 0, byteArray.length);
                    out.flush();
                    MainActivity.this.makeToast("Export complete");
                } catch (Exception e) {
                    Log.e(TAG, "Failed to save kml file.", e);
                    MainActivity.this.makeToast("Export failed");
                } finally {
                    try {
                        if (out != null) {
                            out.close();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void exportFailed(Exception Ex) {
                Log.e(TAG, "Map export to KML failed.", Ex);
                MainActivity.this.makeToast("Export failed");
            }
        });
    }

}
