package mil.emp3.test.emp3vv.navItems.camera_and_look_at;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import org.cmapi.primitives.IGeoPosition;

import java.util.List;

import mil.emp3.api.Camera;
import mil.emp3.api.LookAt;
import mil.emp3.api.interfaces.ICamera;
import mil.emp3.api.interfaces.IContainer;
import mil.emp3.api.interfaces.IFeature;
import mil.emp3.api.interfaces.ILookAt;
import mil.emp3.api.interfaces.IMap;
import mil.emp3.test.emp3vv.R;
import mil.emp3.test.emp3vv.common.Emp3TesterDialogBase;
import mil.emp3.test.emp3vv.dialogs.utils.CameraComponents;
import mil.emp3.test.emp3vv.dialogs.utils.ErrorDialog;
import mil.emp3.test.emp3vv.utils.MapNamesUtility;

/**
 * This dialog will let you set/apply the camera or let you set/apply camera/lookAt. At the creation of the dialog you
 * decide which of these functionality is required. Dialog consists of two panels, left side panel is for Camera and
 * right side panel is for lookAt. Right side panel is made invisible if you are dealing with only the camera. You will see
 * empty white panel.
 *
 * Each panel has the capability to set the all the parameters of Camera and LookAt manually.
 * Each panel has three buttons:
 * Get - get current camera/lookAt
 * Set - set current camera/lookAt
 * Apply - Apply settings to the current camera or lookAt
 *
 * Each panel has a list of all features of the map. On the camera panel, selecting a feature populates lat/long from
 * feature onto camera widget. On the LookAt panel, selecting a feature populates lat/long/altitude from feature onto
 * LookAt widget.
 *
 * Get/Set for camera are straight forward, read the values from the widget and apply/set.
 * Get/Set for LookAt is a bit more than that. It presumes that camera is set to the values displayed
 * on the panel, it uses the lat/long/alt from lookAt panel as target and then calculates other parameters of LookAt.
 */
public class CameraAndLookAtDialog extends Emp3TesterDialogBase {
    private static String TAG = CameraAndLookAtDialog.class.getSimpleName();

    private boolean isLookAtTest = false;
    ListView cameraFeaturesList;
    ArrayAdapter<String> cameraFeaturesListAdapter;
    List<String> cameraFeaturesListData;

    ListView lookAtFeaturesList;
    ArrayAdapter<String> lookAtFeaturesListAdapter;
    List<String> lookAtFeaturesListData;

    CameraComponents cameraComponents;
    CameraComponents lookAtComponents;

    public interface ICameraAndLookAtDialogListener extends IEmp3TesterDialogBaseListener {
        void applyCamera(CameraAndLookAtDialog dialog);
        void setCamera(CameraAndLookAtDialog dialog);
        void applyLookAt(CameraAndLookAtDialog dialog);
        void setLookAt(CameraAndLookAtDialog dialog);
    }

    public ICamera getCamera() {
        if(null != cameraComponents) {
            return cameraComponents.getCamera();
        } else {
            return null;
        }
    }

    public ILookAt getLookAt() {
        if(null != lookAtComponents) {
            return lookAtComponents.getLookAt();
        } else {
            return null;
        }
    }

    public CameraAndLookAtDialog() {
    }

    public static CameraAndLookAtDialog newInstance(String title, IMap map, ICameraAndLookAtDialogListener listener, boolean isLookAtTest) {
        CameraAndLookAtDialog frag = new CameraAndLookAtDialog();
        Bundle args = new Bundle();
        args.putString("title", title);
        frag.setArguments(args);
        frag.init(map, listener);
        frag.isLookAtTest = isLookAtTest;
        return frag;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.camera_and_look_at_dialog, container);
        setDialogPosition();
        return v;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Setup the Camera Panel
        cameraFeaturesList = (ListView) view.findViewById(R.id.camera_features_list);
        cameraFeaturesListData = MapNamesUtility.getNames(map, false, false, true);
        cameraFeaturesListAdapter = setupSingleChoiceList("All Features", cameraFeaturesList, cameraFeaturesListData);

        cameraFeaturesList.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            /**
             * Update the latitude and longitude value in camera panel of camera components
             * @param parent
             * @param view
             * @param position
             * @param id
             */
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.d(TAG, "cameraFeaturesList onItemClick " + cameraFeaturesListAdapter.getItem(position-1));
                IContainer c = MapNamesUtility.getContainer(getMap(), cameraFeaturesListAdapter.getItem(position-1));
                if(c instanceof IFeature) {
                    IFeature f = (IFeature) c;
                    if((null != f.getPositions() && (0 != f.getPositions().size()))) {
                        IGeoPosition p = f.getPositions().get(0);
                        ICamera camera = new Camera();
                        ICamera oldCamera = cameraComponents.getCamera();
                        camera.copySettingsFrom(oldCamera);
                        camera.setLatitude(p.getLatitude());
                        camera.setLongitude(p.getLongitude());
                        cameraComponents.setCamera(camera);
                    }
                }
            }
        });

        View cameraView = view.findViewById(R.id.camera);
        cameraComponents = new CameraComponents();
        cameraComponents.onViewCreated(cameraView, savedInstanceState, getMap().getCamera());

        Button applyCameraButton = (Button) view.findViewById(R.id.apply_camera);
        applyCameraButton.setOnClickListener(v -> {
            String isValid = cameraComponents.validate();
            if(isValid.equals(CameraComponents.isValid)) {
                ((ICameraAndLookAtDialogListener)CameraAndLookAtDialog.this.listener).applyCamera(CameraAndLookAtDialog.this);
            } else {
                ErrorDialog.showError(getContext(), isValid);
            }
        });

        Button setCameraButton = (Button) view.findViewById(R.id.set_camera);
        setCameraButton.setOnClickListener(v -> ((ICameraAndLookAtDialogListener)CameraAndLookAtDialog.this.listener).setCamera(CameraAndLookAtDialog.this));

        Button getCameraButton = (Button) view.findViewById(R.id.get_camera);
        getCameraButton.setOnClickListener(v -> cameraComponents.setCamera(getMap().getCamera()));

        if(isLookAtTest) {
            // Setup LookAt panel
            lookAtFeaturesList = (ListView) view.findViewById(R.id.look_at_features_list);
            lookAtFeaturesListData = MapNamesUtility.getNames(map, false, false, true);
            lookAtFeaturesListAdapter = setupSingleChoiceList("All Features", lookAtFeaturesList, lookAtFeaturesListData);

            lookAtFeaturesList.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                /**
                 * Update the latitude and longitude value in camera panel of camera components
                 * @param parent
                 * @param view
                 * @param position
                 * @param id
                 */
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Log.d(TAG, "lookAtFeaturesList onItemClick " + lookAtFeaturesListAdapter.getItem(position-1));
                    IContainer c = MapNamesUtility.getContainer(getMap(), lookAtFeaturesListAdapter.getItem(position-1));
                    if(c instanceof IFeature) {
                        IFeature f = (IFeature) c;
                        if((null != f.getPositions() && (0 != f.getPositions().size()))) {
                            IGeoPosition p = f.getPositions().get(0);
                            ILookAt lookAt = new LookAt();
                            ILookAt currentLookAt = lookAtComponents.getLookAt();
                            lookAt.copySettingsFrom(currentLookAt);
                            lookAt.setLatitude(p.getLatitude());
                            lookAt.setLongitude(p.getLongitude());
                            lookAt.setAltitude(p.getAltitude());
                            lookAtComponents.setLookAt(lookAt);
                        }
                    }
                }
            });

            Button applyLookAtButton = (Button) view.findViewById(R.id.apply_look_at);
            applyLookAtButton.setOnClickListener(v -> ((ICameraAndLookAtDialogListener)CameraAndLookAtDialog.this.listener).applyLookAt(CameraAndLookAtDialog.this));

            Button setLookAtButton = (Button) view.findViewById(R.id.set_look_at);
            setLookAtButton.setOnClickListener(v -> ((ICameraAndLookAtDialogListener)CameraAndLookAtDialog.this.listener).setLookAt(CameraAndLookAtDialog.this));

            Button getLookAtButton = (Button) view.findViewById(R.id.get_look_at);
            getLookAtButton.setOnClickListener(v -> lookAtComponents.setLookAt(getMap().getLookAt()));

            View lookAtView = view.findViewById(R.id.look_at);
            lookAtComponents = new CameraComponents();
            lookAtComponents.onViewCreated(lookAtView, savedInstanceState, getMap().getLookAt());
        } else {
            view.findViewById(R.id.look_at).setVisibility(View.INVISIBLE);
            view.findViewById(R.id.apply_look_at).setVisibility(View.INVISIBLE);
            view.findViewById(R.id.set_look_at).setVisibility(View.INVISIBLE);
            view.findViewById(R.id.get_look_at).setVisibility(View.INVISIBLE);
            view.findViewById(R.id.lbl_look_at).setVisibility(View.INVISIBLE);
        }

        Button doneButton = (Button) view.findViewById(R.id.done);
        doneButton.setOnClickListener(v -> CameraAndLookAtDialog.this.dismiss());
    }
}
