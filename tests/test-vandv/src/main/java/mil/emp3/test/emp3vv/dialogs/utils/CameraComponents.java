package mil.emp3.test.emp3vv.dialogs.utils;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import org.cmapi.primitives.IGeoAltitudeMode;

import java.util.ArrayList;
import java.util.List;

import mil.emp3.api.Camera;
import mil.emp3.api.LookAt;
import mil.emp3.api.global;
import mil.emp3.api.interfaces.ICamera;
import mil.emp3.api.interfaces.ILookAt;
import mil.emp3.test.emp3vv.R;

import static java.lang.Double.valueOf;

/**
 * processes camera_components.xml layout file. It is shared by LookAt and Camera dialogs.
 */
public class CameraComponents {
    private static String TAG = CameraComponents.class.getSimpleName();

    private EditText et_latitude;
    private EditText et_longitude;
    private EditText et_altitude;
    private EditText et_heading;
    private EditText et_tilt;
    private EditText et_roll_range;

    private Spinner altitudeModeSpinner;
    private ArrayAdapter<String> altitudeModeSpinnerAdapter;
    private List<String> altitudeModeSpinnerData;

    private boolean isLookAt = false;

    public static String isValid = "VALID";

    private void onViewCreated(View view, @Nullable Bundle savedInstanceState, double latitude, double longitude, double altitude,
                               double heading, double tilt, double roll_range, IGeoAltitudeMode.AltitudeMode altitudeMode) {
        et_latitude = (EditText) view.findViewById(R.id.latitude);
        et_longitude = (EditText) view.findViewById(R.id.longitude);
        et_altitude = (EditText) view.findViewById(R.id.altitude);
        et_heading = (EditText) view.findViewById(R.id.heading);
        et_tilt = (EditText) view.findViewById(R.id.tilt);
        et_roll_range = (EditText) view.findViewById(R.id.roll_range);

        altitudeModeSpinner = (Spinner) view.findViewById(R.id.altitude_mode);
        altitudeModeSpinnerData = new ArrayList<>();
        for(IGeoAltitudeMode.AltitudeMode mode: IGeoAltitudeMode.AltitudeMode.values()) {
            altitudeModeSpinnerData.add(mode.toString());
        }
        altitudeModeSpinnerAdapter = new ArrayAdapter(view.getContext(), android.R.layout.simple_spinner_item, altitudeModeSpinnerData);
        altitudeModeSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        altitudeModeSpinner.setAdapter(altitudeModeSpinnerAdapter);

        updateDisplay(latitude, longitude, altitude, heading, tilt, roll_range, altitudeMode);
        Log.d(TAG, "altitudeMode " + altitudeMode);
    }

    public void onViewCreated(View view, @Nullable Bundle savedInstanceState, ICamera camera) {
        onViewCreated(view, savedInstanceState, camera.getLatitude(), camera.getLongitude(), camera.getAltitude(),
                camera.getHeading(), camera.getTilt(), camera.getRoll(), camera.getAltitudeMode());
    }

    public void onViewCreated(View view, @Nullable Bundle savedInstanceState, ILookAt lookAt) {
        this.isLookAt = true;
        onViewCreated(view, savedInstanceState, lookAt.getLatitude(), lookAt.getLongitude(), lookAt.getAltitude(),
                lookAt.getHeading(), lookAt.getTilt(), lookAt.getRange(), lookAt.getAltitudeMode());

        TextView lbl_range_role = (TextView) view.findViewById(R.id.lbl_roll_range);
        lbl_range_role.setText("Range: ");
    }

    private void updateDisplay(double latitude, double longitude, double altitude,
                       double heading, double tilt, double roll_range, IGeoAltitudeMode.AltitudeMode altitudeMode) {
        et_latitude.setText(String.format("%1$6.3f", latitude));
        et_longitude.setText(String.format("%1$6.3f", longitude));
        et_altitude.setText(String.format("%1$d", (long) altitude));
        et_heading.setText(String.format("%1$6.3f", heading));
        et_tilt.setText(String.format("%1$6.3f", tilt));
        et_roll_range.setText(String.format("%1$d", (long) roll_range));
        altitudeModeSpinner.setSelection(altitudeModeSpinnerData.indexOf(altitudeMode));
    }

    public ICamera getCamera() {
        if(validate().equals(isValid)) {
            ICamera camera = new Camera();
            camera.setLatitude(valueOf(et_latitude.getText().toString()));
            camera.setLongitude(valueOf(et_longitude.getText().toString()));
            camera.setAltitude(valueOf(et_altitude.getText().toString()));
            camera.setHeading(valueOf(et_heading.getText().toString()));
            camera.setTilt(valueOf(et_tilt.getText().toString()));
            camera.setRoll(valueOf(et_roll_range.getText().toString()));
            camera.setAltitudeMode(IGeoAltitudeMode.AltitudeMode.valueOf(altitudeModeSpinnerData.get(altitudeModeSpinner.getSelectedItemPosition())));
            return camera;
        } else {
            return null;
        }
    }

    public void setCamera(ICamera camera) {
        updateDisplay(camera.getLatitude(), camera.getLongitude(), camera.getAltitude(), camera.getHeading(), camera.getTilt(),
                camera.getRoll(), camera.getAltitudeMode());
    }

    public ILookAt getLookAt() {

        if(validate().equals(isValid)) {
            ILookAt lookAt = new LookAt();
            lookAt.setLatitude(valueOf(et_latitude.getText().toString()));
            lookAt.setLongitude(valueOf(et_longitude.getText().toString()));
            lookAt.setAltitude(valueOf(et_altitude.getText().toString()));
            lookAt.setHeading(valueOf(et_heading.getText().toString()));
            lookAt.setTilt(valueOf(et_tilt.getText().toString()));
            lookAt.setRange(valueOf(et_roll_range.getText().toString()));
            lookAt.setAltitudeMode(IGeoAltitudeMode.AltitudeMode.valueOf(altitudeModeSpinnerData.get(altitudeModeSpinner.getSelectedItemPosition())));
            return lookAt;
        } else {
            return null;
        }
    }

    public void setLookAt(ILookAt lookAt) {
        updateDisplay(lookAt.getLatitude(), lookAt.getLongitude(), lookAt.getAltitude(), lookAt.getHeading(), lookAt.getTilt(),
                lookAt.getRange(), lookAt.getAltitudeMode());
    }

    public void cameraMoved(ICamera camera) {
        updateDisplay(camera.getLatitude(), camera.getLongitude(), camera.getAltitude(),
                camera.getHeading(), camera.getTilt(), camera.getRoll(), camera.getAltitudeMode());
    }

    public void lookAtMoved(ILookAt lookAt) {
        updateDisplay(lookAt.getLatitude(), lookAt.getLongitude(), lookAt.getAltitude(),
                lookAt.getHeading(), lookAt.getTilt(), lookAt.getRange(), lookAt.getAltitudeMode());
    }

    public String validate() {

        try {
            double latitude = Double.valueOf(et_latitude.getText().toString());
            if (Double.isNaN(latitude) || (latitude < global.LATITUDE_MINIMUM) || (latitude > global.LATITUDE_MAXIMUM)) {
                return "LATITUDE_MINIMUM " + global.LATITUDE_MINIMUM + " LATITUDE_MAXIMUM " + global.LATITUDE_MAXIMUM;
            }

            double longitude = Double.valueOf(et_longitude.getText().toString());
            if (Double.isNaN(longitude) || (longitude < global.LONGITUDE_MINIMUM) || (longitude > global.LONGITUDE_MAXIMUM)) {
                return "LONGITUDE_MINIMUM " + global.LONGITUDE_MINIMUM + " LONGITUDE_MAXIMUM " + global.LONGITUDE_MAXIMUM;
            }

            double heading = Double.valueOf(et_heading.getText().toString());
            if (Double.isNaN(heading) || (heading < global.HEADING_MINIMUM) || (heading > global.HEADING_MAXIMUM)) {
                return "HEADING_MINIMUM " + global.HEADING_MINIMUM + " HEADING_MAXIMUM " + global.HEADING_MAXIMUM;
            }

            double tilt = Double.valueOf(valueOf(et_tilt.getText().toString()));
            if (Double.isNaN(tilt) || (tilt < global.CAMERA_TILT_MINIMUM) || (tilt > global.CAMERA_TILT_MAXIMUM)) {
                return "CAMERA_TILT_MINIMUM " + global.CAMERA_TILT_MINIMUM + " CAMERA_TILT_MAXIMUM " + global.CAMERA_TILT_MAXIMUM;
            }

            if (!isLookAt) {
                double roll_range = Double.valueOf(et_roll_range.getText().toString());
                if (Double.isNaN(roll_range) || (roll_range < global.CAMERA_ROLL_MINIMUM) || (roll_range > global.CAMERA_ROLL_MAXIMUM)) {
                    return "CAMERA_ROLL_MINIMUM " + global.CAMERA_ROLL_MINIMUM + " CAMERA_ROLL_MAXIMUM " + global.CAMERA_ROLL_MAXIMUM;
                }
            }

            return "VALID";
        } catch(Exception e) {
            return e.getMessage();
        }
    }
}
