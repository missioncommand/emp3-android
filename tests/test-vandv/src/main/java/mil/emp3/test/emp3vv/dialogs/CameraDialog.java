package mil.emp3.test.emp3vv.dialogs;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import mil.emp3.api.events.CameraEvent;
import mil.emp3.api.exceptions.EMP_Exception;
import mil.emp3.api.interfaces.ICamera;
import mil.emp3.api.interfaces.IMap;
import mil.emp3.api.listeners.EventListenerHandle;
import mil.emp3.api.listeners.ICameraEventListener;
import mil.emp3.test.emp3vv.R;
import mil.emp3.test.emp3vv.common.Emp3TesterDialogBase;
import mil.emp3.test.emp3vv.dialogs.utils.CameraComponents;
import mil.emp3.test.emp3vv.dialogs.utils.ErrorDialog;

public class CameraDialog extends Emp3TesterDialogBase implements ICameraEventListener {

    private static String TAG = CameraDialog.class.getSimpleName();

    ICamera startPosition;
    private CameraComponents cameraComponents;
    EventListenerHandle cameraEventListenerHandle;

    public CameraDialog() {
        // Empty constructor is required for DialogFragment
        // Make sure not to add arguments to the constructor
        // Use `newInstance` instead as shown below
    }

    public static CameraDialog newInstance(String title, ICameraDialogListener listener, ICamera startPosition, IMap map) {

        if (null == startPosition) {
            throw new IllegalArgumentException("startPosition must be non-null");
        }
        CameraDialog frag = new CameraDialog();
        Bundle args = new Bundle();
        args.putString("title", title);
        frag.setArguments(args);
        frag.init(map, listener);
        frag.setStartPosition(startPosition);
        return frag;
    }

    public static CameraDialog newInstanceForOptItem(String title, ICameraDialogListener listener, ICamera startPosition, IMap map) {

        if (null == startPosition) {
            throw new IllegalArgumentException("startPosition must be non-null");
        }
        CameraDialog frag = new CameraDialog();
        Bundle args = new Bundle();
        args.putString("title", title);
        frag.setArguments(args);
        frag.initForOptItem(map, listener);
        frag.setStartPosition(startPosition);
        return frag;
    }

    public void setStartPosition(ICamera startPosition) {
        this.startPosition = startPosition;
    }
    public ICamera getCamera() {
        if(null != cameraComponents) {
            return cameraComponents.getCamera();
        } else {
            return null;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.camera_dialog, container);
        setDialogPosition();
        return v;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        cameraComponents = new CameraComponents();
        cameraComponents.onViewCreated(view, savedInstanceState, startPosition);

        Button doneButton = (Button) view.findViewById(R.id.done);
        doneButton.setOnClickListener(v -> {
            CameraDialog.this.dismiss();
            map.removeEventListener(cameraEventListenerHandle);
        });

        Button applyButton = (Button) view.findViewById(R.id.apply);
        applyButton.setOnClickListener(v -> {
            String isValid = cameraComponents.validate();
            if(isValid.equals(CameraComponents.isValid)) {
                if (null != CameraDialog.this.listener) {
                    ((ICameraDialogListener) CameraDialog.this.listener).cameraSet(CameraDialog.this);
                }
            } else {
                ErrorDialog.showError(getContext(), isValid);
            }
        });

        try {
            cameraEventListenerHandle = map.addCameraEventListener(this);
        } catch (EMP_Exception e) {
            Log.e(TAG, "adding Camera Event Listener", e);
        }
    }

    public interface ICameraDialogListener extends IEmp3TesterDialogBaseListener {
        void cameraSet(CameraDialog cameraDialog);
    }

    @Override
    public void onEvent(final CameraEvent event) {
        getActivity().runOnUiThread(() -> cameraComponents.cameraMoved(event.getCamera()));

    }
}

