package mil.emp3.validator.fragment;

import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import java.util.HashMap;
import java.util.Map;

import mil.emp3.api.Camera;
import mil.emp3.api.enums.Property;
import mil.emp3.api.events.MapStateChangeEvent;
import mil.emp3.api.interfaces.ICamera;
import mil.emp3.api.interfaces.IEmpPropertyList;
import mil.emp3.api.interfaces.IMap;
import mil.emp3.api.listeners.EventListenerHandle;
import mil.emp3.api.listeners.IMapStateChangeEventListener;
import mil.emp3.api.utils.EmpPropertyList;
import mil.emp3.validator.R;
import mil.emp3.validator.ValidatorStateManager;
import mil.emp3.validator.model.ManagedMapFragment;

public class MapValidatorFragment extends Fragment {
    static final private String TAG = MapValidatorFragment.class.getSimpleName();

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView");

        final View view = inflater.inflate(R.layout.fragment_validator_create_map, container, false);

        final Button createButton = (Button) view.findViewById(R.id.button_create);
        createButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick");

                final ManagedMapFragment mapFragment = ValidatorStateManager.getInstance().createMapFragment();
                getFragmentManager().beginTransaction().replace(R.id.content_fragment, mapFragment.get(), "map_fragment").commit();

                // TODO create 'loading' screen
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        final String name = ((EditText) getView().findViewById(R.id.textView_name)).getText().toString();
                        final String mapEngineClass = ((EditText) getView().findViewById(R.id.textView_map_engine_class)).getText().toString();
                        final String mapEngineAppId = ((EditText) getView().findViewById(R.id.textView_map_engine_appId)).getText().toString();

                        Log.d(TAG, "name: " + name);
                        Log.d(TAG, "mapEngineClass: " + mapEngineClass);
                        Log.d(TAG, "mapEngineAppId: " + mapEngineAppId);

                        final ICamera camera = new Camera();
                        camera.setAltitude(1e7);

                        try {
                            final IMap map = (IMap) getFragmentManager().findFragmentByTag("map_fragment");
                            IEmpPropertyList propList = new EmpPropertyList();

                            propList.put(Property.ENGINE_CLASSNAME.getValue(), mapEngineClass);
                            propList.put(Property.ENGINE_APKNAME.getValue(), mapEngineAppId);
                            map.setName(name);
                            map.swapMapEngine(propList);
                            map.setCamera(camera, false);

                            //TODO handle the handle
                            final EventListenerHandle handle = map.addMapStateChangeEventListener(new IMapStateChangeEventListener() {
                                @Override
                                public void onEvent(MapStateChangeEvent e) {
                                    Log.d(TAG, "onEvent: " + e.getNewState());

                                    getFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                                    getFragmentManager().beginTransaction().replace(R.id.sideNav_fragment, new SideNavFragment()).commit();
                                }
                            });
                        } catch (Exception e) {
                            throw new IllegalStateException(e);
                        }
                    }
                });
            }
        });

        return view;
    }
}
