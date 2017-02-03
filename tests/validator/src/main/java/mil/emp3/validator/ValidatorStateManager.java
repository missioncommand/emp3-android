package mil.emp3.validator;

import org.cmapi.primitives.IGeoAltitudeMode;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import mil.emp3.api.Camera;
import mil.emp3.api.MapFragment;
import mil.emp3.api.Overlay;
import mil.emp3.api.WMS;
import mil.emp3.api.enums.WMSVersionEnum;
import mil.emp3.validator.model.ManagedCamera;
import mil.emp3.validator.model.ManagedMapFragment;
import mil.emp3.validator.model.ManagedOverlay;
import mil.emp3.validator.model.ManagedWms;

public class ValidatorStateManager {

    static final ValidatorStateManager instance = new ValidatorStateManager();

    final private Map<String, ManagedMapFragment> maps;
    final private Map<String, ManagedOverlay> overlays;
    final private Map<String, ManagedCamera> cameras;
    final private Map<String, ManagedWms> wmses;

    private ValidatorStateManager() {
        maps     = new HashMap<>();
        overlays = new HashMap<>();
        cameras  = new HashMap<>();
        wmses    = new HashMap<>();
    }

    static public ValidatorStateManager getInstance() {
        return instance;
    }


    public ManagedMapFragment createMapFragment() {
        final ManagedMapFragment managedMapFragment = new ManagedMapFragment(new MapFragment());
        maps.put(managedMapFragment.getId(), managedMapFragment);

        return managedMapFragment;
    }

    public ManagedOverlay createOverlay(String name) {
        final ManagedOverlay managedOverlay = new ManagedOverlay(new Overlay());
        managedOverlay.get().setName(name);

        overlays.put(managedOverlay.getId(), managedOverlay);

        return managedOverlay;
    }

    public ManagedCamera createCamera(String name, double latitude, double longitude, double altitude, double roll, double tilt, double heading, String altitudeMode) {
        final ManagedCamera managedCamera = new ManagedCamera(new Camera());
        managedCamera.get().setName(name);
        managedCamera.get().setLatitude(latitude);
        managedCamera.get().setLongitude(longitude);
        managedCamera.get().setAltitude(altitude);
        managedCamera.get().setRoll(roll);
        managedCamera.get().setTilt(tilt);
        managedCamera.get().setHeading(heading);
        managedCamera.get().setAltitudeMode(IGeoAltitudeMode.AltitudeMode.valueOf(altitudeMode));

        cameras.put(managedCamera.getId(), managedCamera);

        return managedCamera;
    }

    public ManagedWms createWms(String name, String url, List<String> layers, String wmsVersion, String tileFormat) {
        final WMS wms;
        try {
            wms = new WMS(url, WMSVersionEnum.valueOf(wmsVersion), tileFormat, true, layers);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
        wms.setName(name);

        final ManagedWms managedWms = new ManagedWms(wms);

        return managedWms;
    }

    public ManagedMapFragment getMap(String id) {
        return maps.get(id);
    }
    public ManagedOverlay getOverlay(String id) {
        return overlays.get(id);
    }
    public ManagedCamera getCamera(String id) {
        return cameras.get(id);
    }
    public ManagedWms getWms(String id) {
        return wmses.get(id);
    }

    public ManagedMapFragment getMap() {
        return maps.values().iterator().next();
    }
}
