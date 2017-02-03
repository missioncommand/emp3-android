package mil.emp3.wwzoom;

import android.content.res.Resources;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.cmapi.primitives.IGeoAltitudeMode;

import java.util.ArrayList;

import mil.emp3.api.WMS;
import mil.emp3.api.enums.WMSVersionEnum;
import mil.emp3.api.events.MapStateChangeEvent;
import mil.emp3.api.events.MapUserInteractionEvent;
import mil.emp3.api.exceptions.EMP_Exception;
import mil.emp3.api.interfaces.ICamera;
import mil.emp3.api.interfaces.IMap;
import mil.emp3.api.listeners.IMapInteractionEventListener;
import mil.emp3.api.listeners.IMapStateChangeEventListener;

public class CustomActivity extends AppCompatActivity {

    private final static String TAG = CustomActivity.class.getSimpleName();
    private WMS wmsService = null;
    private WMS oldWMSService = null;
    private IMap map = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "Setting custom activity");
        setContentView(R.layout.activity_custom);

        Button cancelButton = (Button) findViewById(R.id.CancelButton);
        if (cancelButton != null) {
            cancelButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    finish();
                }
            });
        } else {
            Log.e(TAG, "Cancel Button not found");
        }
        map = (IMap) findViewById(R.id.map);
        map.addMapStateChangeEventListener(new IMapStateChangeEventListener() {
            @Override
            public void onEvent(MapStateChangeEvent mapStateChangeEvent) {
                Log.d(TAG, "mapStateChangeEvent " + mapStateChangeEvent.getNewState());
            }
        });
        map.addMapInteractionEventListener(new IMapInteractionEventListener() {
            @Override
            public void onEvent(MapUserInteractionEvent mapUserInteractionEvent) {
                Log.d(TAG, "mapUserInteractionEvent " + mapUserInteractionEvent.getPoint().x);
            }
        });

        final mil.emp3.api.Camera oCamera = new mil.emp3.api.Camera();
        oCamera.setName("Main Cam");
        oCamera.setAltitudeMode(IGeoAltitudeMode.AltitudeMode.ABSOLUTE);
        oCamera.setAltitude(2e5);
        oCamera.setHeading(0.0);
        oCamera.setLatitude(31.0);
        oCamera.setLongitude(-106.0);
        oCamera.setRoll(0.0);
        oCamera.setTilt(0.0);
        try {
            map.setCamera(oCamera);
        } catch (EMP_Exception empe) {
            empe.printStackTrace();
        }
        Button zoomOutButton = (Button) findViewById(R.id.ZoomOut);
        if (zoomOutButton != null) {
            zoomOutButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ICamera camera = map.getCamera();
                    double initAltitude = camera.getAltitude();
                    if (initAltitude <= 1e8 / 1.2) {
                        initAltitude *= 1.2;
                        camera.setAltitude(initAltitude);
                        camera.apply();
                    } else {
                        Toast.makeText(CustomActivity.this, "Can't zoom out any more, altitude " + initAltitude, Toast.LENGTH_LONG).show();
                    }
                }
            });
        } else {
            Log.e(TAG, "Zoom out button not found");
        }
        Button zoomInButton = (Button) findViewById(R.id.ZoomIn);
        if (zoomInButton != null) {
            zoomInButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ICamera camera = map.getCamera();
                    double initAltitude = camera.getAltitude();
                    if (initAltitude >= 12000) {
                        initAltitude /= 1.2;
                        camera.setAltitude(initAltitude);
                        camera.apply();
                    } else {
                        Toast.makeText(CustomActivity.this, "Can't zoom in any more, altitude " + initAltitude, Toast.LENGTH_LONG).show();
                    }
                }
            });
        } else {
            Log.e(TAG, "Zoom in button not found");
        }
        Button panLeft = (Button) findViewById(R.id.PanLeft);
        if (panLeft != null) {
            panLeft.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        double dPan = oCamera.getHeading();

                        dPan -= 5.0;
                        if (dPan < 0.0) {
                            dPan += 360.0;
                        }

                        oCamera.setHeading(dPan);
                        oCamera.apply();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        } else {
            Log.e(TAG, "Pan left button not found");
        }
        Button panRight = (Button) findViewById(R.id.PanRight);
        if (panRight != null) {
            panRight.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        double dPan = oCamera.getHeading();

                        dPan += 5.0;
                        if (dPan >= 360.0) {
                            dPan -= 360.0;
                        }

                        oCamera.setHeading(dPan);
                        oCamera.apply();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        } else {
            Log.e(TAG, "Pan right button not found");
        }
        Button tiltUp = (Button) findViewById(R.id.TiltUp);
        if (tiltUp != null) {
            tiltUp.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        double dTilt = oCamera.getTilt();

                        if (dTilt < 175.0) {
                            dTilt += 5;
                        }
                        oCamera.setTilt(dTilt);
                        oCamera.apply();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        } else {
            Log.e(TAG, "Tilt up button not found");
        }
        Button tiltDown = (Button) findViewById(R.id.TiltDown);
        if (tiltDown != null) {
            tiltDown.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        double dTilt = oCamera.getTilt();

                        if (dTilt >= 5.0) {
                            dTilt -= 5;
                        }
                        oCamera.setTilt(dTilt);
                        oCamera.apply();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        } else {
            Log.e(TAG, "Tilt down button not found");
        }

        Button okButton = (Button) findViewById(R.id.OKButton);
        if (okButton != null)

        {
            okButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    EditText urlText = (EditText) findViewById(R.id.UrlText);
                    EditText versionText = (EditText) findViewById(R.id.VersionText);
                    EditText tileFormatText = (EditText) findViewById(R.id.TileFormatText);
                    EditText transparentText = (EditText) findViewById(R.id.TransparentText);
                    EditText layerText = (EditText) findViewById(R.id.LayerText);

                    try {
                        String url = urlText.getText().toString();
                        String version = versionText.getText().toString();
                        String tileFormat = tileFormatText.getText().toString();
                        boolean transparent = (transparentText.getText().toString()).equals("false");
                        String layer = layerText.getText().toString();
                        ArrayList<String> layers = new ArrayList<>();
                        layers.add(layer);
                        Resources res = getBaseContext().getResources();
                        wmsService = new WMS(url,
                                version.equals("1.1.1") ? WMSVersionEnum.VERSION_1_1 :
                                        WMSVersionEnum.VERSION_1_3,
                                tileFormat.equals("null") ? null : tileFormat,  // tile format
                                transparent,
                                layers);
                        if (wmsService != null) {
                            if (wmsService != oldWMSService) {
                                if (oldWMSService != null)
                                    map.removeMapService(oldWMSService);
                                else
                                    Log.i(TAG, "No previous WMS service");
                                map.addMapService(wmsService);
                                oldWMSService = wmsService;
                            } else {
                                Log.i(TAG, "Layer unchanged");
                            }
                        } else {
                            Log.i(TAG, "Got null WMS service");
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }
}
