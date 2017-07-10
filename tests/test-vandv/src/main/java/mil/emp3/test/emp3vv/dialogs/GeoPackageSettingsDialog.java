package mil.emp3.test.emp3vv.dialogs;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import mil.emp3.api.Camera;
import mil.emp3.api.exceptions.EMP_Exception;
import mil.emp3.api.interfaces.ICamera;
import mil.emp3.api.interfaces.IMap;
import mil.emp3.test.emp3vv.R;
import mil.emp3.test.emp3vv.common.Emp3TesterDialogBase;


public class GeoPackageSettingsDialog extends Emp3TesterDialogBase {

    private EditText name;
    private String geoPackageFileName;
    private double cameraLatitude;
    private double cameraLongitude;
    private double cameraAltitude;
    private boolean relocateCamera = false;

    public GeoPackageSettingsDialog() {

    }

    public static GeoPackageSettingsDialog newInstance(String title, IGeoPackageSettingsDialogListener listener,
                                                IMap map) {
        GeoPackageSettingsDialog geoPackageSettingsDialog = new GeoPackageSettingsDialog();
        Bundle args = new Bundle();
        args.putString("title", title);
        geoPackageSettingsDialog.setArguments(args);
        geoPackageSettingsDialog.init(map, listener);
        return geoPackageSettingsDialog;
    }

    public static GeoPackageSettingsDialog newInstanceForOptItem(String title, IGeoPackageSettingsDialogListener listener,
                                                       IMap map) {
        GeoPackageSettingsDialog geoPackageSettingsDialog = new GeoPackageSettingsDialog();
        Bundle args = new Bundle();
        args.putString("title", title);
        geoPackageSettingsDialog.setArguments(args);
        geoPackageSettingsDialog.initForOptItem(map, listener);
        return geoPackageSettingsDialog;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.geopackage_settings_dialog, container);
        setDialogPosition();
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        name = (EditText) view.findViewById(R.id.NameText);
        geoPackageFileName = ((EditText) view.findViewById(R.id.GeoPackageFileName)).getText().toString();
        relocateCamera = false;
        String cameraInput = ((EditText) view.findViewById(R.id.CameraLatitude)).getText().toString();
        if (cameraInput != null && !cameraInput.isEmpty()) {
            relocateCamera = true;
            cameraLatitude = Double.parseDouble(cameraInput);
            cameraInput = ((EditText) view.findViewById(R.id.CameraLongitude)).getText().toString();
            cameraLongitude = Double.parseDouble(cameraInput);
            cameraInput = ((EditText) view.findViewById(R.id.CameraAltitude)).getText().toString();
            cameraAltitude = Double.parseDouble(cameraInput);
        }

        Button doneButton = (Button) view.findViewById(R.id.done);
        doneButton.setOnClickListener(v -> GeoPackageSettingsDialog.this.dismiss());

        Button applyButton = (Button) view.findViewById(R.id.apply);
        applyButton.setOnClickListener(v -> {
            if (null != GeoPackageSettingsDialog.this.listener) {
                if (relocateCamera) {
                    boolean oldCamera = true;
                    ICamera camera = map.getCamera();
                    if (camera == null) {
                        camera = new Camera();
                        oldCamera = false;
                    }
                    camera.setLatitude(cameraLatitude);
                    camera.setLongitude(cameraLongitude);
                    camera.setAltitude(cameraAltitude);
                    if (oldCamera) {
                        camera.apply(true);
                    } else {
                        try {
                            map.setCamera(camera, true);
                        } catch (EMP_Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
                ((IGeoPackageSettingsDialogListener) GeoPackageSettingsDialog.this.listener).
                        addGeoPackageService(map,
                                name.getText().toString(),
                                geoPackageFileName);
            }
        });
    }

    public interface IGeoPackageSettingsDialogListener extends IEmp3TesterDialogBaseListener {
        void addGeoPackageService(IMap map, String name, String layer);
    }
}
