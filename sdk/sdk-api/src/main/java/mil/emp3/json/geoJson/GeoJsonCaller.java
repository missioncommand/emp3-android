package mil.emp3.json.geoJson;

import java.util.List;

import mil.emp3.api.interfaces.IEmpExportToStringCallback;
import mil.emp3.api.interfaces.IFeature;
import mil.emp3.api.interfaces.IMap;
import mil.emp3.api.interfaces.IOverlay;
import mil.emp3.api.interfaces.core.ICoreManager;
import mil.emp3.api.utils.ManagerFactory;

public class GeoJsonCaller {

    private static String TAG = GeoJsonCaller.class.getSimpleName();
    static final private ICoreManager coreManager = ManagerFactory.getInstance().getCoreManager();

    private GeoJsonCaller() {

    }

    public static void exportToString(IMap map, boolean extendedData, IEmpExportToStringCallback callback) {

        if ((null == map) || (null == callback)) {
            throw new IllegalArgumentException("Parameters can't be null.");
        }

        GeoJsonExporter exporter = new GeoJsonExporter(map, extendedData, callback);
        exporter.start();
    }

    public static void exportToString(IMap map, IOverlay overlay, boolean extendedData, IEmpExportToStringCallback callback) {

        if ((null == overlay) || (null == callback)) {
            throw new IllegalArgumentException("Parameters can't be null.");
        }

        GeoJsonExporter exporter = new GeoJsonExporter(map, overlay, extendedData, callback);
        exporter.start();
    }

    public static void exportToString(IMap map, IFeature feature, boolean extendedData, IEmpExportToStringCallback callback) {

        if ((null == feature) || (null == callback)) {
            throw new IllegalArgumentException("Parameters can't be null.");
        }

        GeoJsonExporter exporter = new GeoJsonExporter(map, feature, extendedData, callback);
        exporter.start();
    }

    public static void exportToString(IMap map, List<IFeature> featureList, boolean extendedData, IEmpExportToStringCallback callback) {

        if ((null == featureList) || (null == callback)) {
            throw new IllegalArgumentException("Parameters can't be null.");
        }

        GeoJsonExporter exporter = new GeoJsonExporter(map, featureList, extendedData, callback);
        exporter.start();
    }

}
