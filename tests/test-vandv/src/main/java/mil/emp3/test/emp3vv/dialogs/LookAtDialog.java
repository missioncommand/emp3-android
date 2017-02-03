package mil.emp3.test.emp3vv.dialogs;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import mil.emp3.api.events.LookAtEvent;
import mil.emp3.api.exceptions.EMP_Exception;
import mil.emp3.api.interfaces.ILookAt;
import mil.emp3.api.interfaces.IMap;
import mil.emp3.api.listeners.EventListenerHandle;
import mil.emp3.api.listeners.ILookAtEventListener;
import mil.emp3.test.emp3vv.R;
import mil.emp3.test.emp3vv.common.Emp3TesterDialogBase;
import mil.emp3.test.emp3vv.dialogs.utils.CameraComponents;

public class LookAtDialog extends Emp3TesterDialogBase implements ILookAtEventListener {
    private static String TAG = LookAtDialog.class.getSimpleName();

    ILookAt startPosition;
    private CameraComponents lookAtComponents;
    private EventListenerHandle lookAtEventListenerHandle;
    ILookAt myLookAt;

    public LookAtDialog() {
        // Empty constructor is required for DialogFragment
        // Make sure not to add arguments to the constructor
        // Use `newInstance` instead as shown below
    }

    public static LookAtDialog newInstance(String title, ILookAtDialogListener listener, ILookAt startPosition, IMap map) {

        if (null == startPosition) {
            throw new IllegalArgumentException("startPosition must be non-null");
        }
        LookAtDialog frag = new LookAtDialog();
        Bundle args = new Bundle();
        args.putString("title", title);
        frag.setArguments(args);
        frag.init(map, listener);
        frag.setStartPosition(startPosition);
        return frag;
    }

    public static LookAtDialog newInstanceForOptItem(String title, ILookAtDialogListener listener, ILookAt startPosition, IMap map) {

        if (null == startPosition) {
            throw new IllegalArgumentException("startPosition must be non-null");
        }
        LookAtDialog frag = new LookAtDialog();
        Bundle args = new Bundle();
        args.putString("title", title);
        frag.setArguments(args);
        frag.initForOptItem(map, listener);
        frag.setStartPosition(startPosition);
        return frag;
    }

    public void setStartPosition(ILookAt startPosition) {
        this.startPosition = startPosition;
    }
    public ILookAt getLookAt() {
        if(null != lookAtComponents) {
            return lookAtComponents.getLookAt();
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
        lookAtComponents = new CameraComponents();
        lookAtComponents.onViewCreated(view, savedInstanceState, startPosition);

        Button doneButton = (Button) view.findViewById(R.id.done);
        doneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myLookAt.removeEventListener(lookAtEventListenerHandle);
                LookAtDialog.this.dismiss();
            }
        });

        Button applyButton = (Button) view.findViewById(R.id.apply);
        applyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != LookAtDialog.this.listener) {
                    ((ILookAtDialogListener)LookAtDialog.this.listener).lookAtSet(LookAtDialog.this);
                }
            }
        });

        try {
            Log.d(TAG, "onViewCreated LookAt " + map.getLookAt().getGeoId().toString());
            myLookAt = map.getLookAt();
            lookAtEventListenerHandle = myLookAt.addLookAtEventListener(this);
        } catch (EMP_Exception e) {
            Log.e(TAG, "addLookAtEventListener ", e);
        }
    }

    public interface ILookAtDialogListener extends IEmp3TesterDialogBaseListener {
        void lookAtSet(LookAtDialog lookAtDialog);
    }

    @Override
    public void onEvent(final LookAtEvent event) {
        if((null != getActivity()) && (null != lookAtComponents)) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    lookAtComponents.lookAtMoved(event.getLookAt());
                }
            });
        } else {
            Log.e(TAG, "onEvent shouldn't be invoked");
        }
    }
}
