package mil.emp3.api;

import android.content.Context;

import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import mil.emp3.api.abstracts.MapService;
import mil.emp3.api.enums.KMLSStatusEnum;
import mil.emp3.api.exceptions.EMP_Exception;
import mil.emp3.api.interfaces.IKMLS;
import mil.emp3.api.interfaces.IMap;

public class KMLS extends MapService implements IKMLS {
    private Map<IMap, KMLSStatusEnum> KMLSStatusMap = new HashMap<>();
    private Context context;

    public KMLS(Context context, String serviceURL) throws MalformedURLException {
        super(serviceURL);
        if(null == context) {
            throw new IllegalArgumentException("context must be specified");
        }
        this.context = context;
    }

    public KMLS(UUID geoId, String serviceURL) throws MalformedURLException {
        super(serviceURL);
        if(null == geoId) {
            throw new IllegalArgumentException("geoId must be non-null");
        }
        setGeoId(geoId);
    }

    @Override
    public KMLSStatusEnum getStatus(IMap mapClient) throws EMP_Exception {
        if(null == mapClient) {
            throw new IllegalArgumentException("mapClient must be non-null");
        }
        KMLSStatusEnum status = KMLSStatusMap.get(mapClient);
        if(null == status) {
            throw new EMP_Exception(EMP_Exception.ErrorDetail.INVALID_MAP, "Service wasn't added to the map");
        }
        return status;
    }

    @Override
    public Context getContext() {
        return context;
    }
}
