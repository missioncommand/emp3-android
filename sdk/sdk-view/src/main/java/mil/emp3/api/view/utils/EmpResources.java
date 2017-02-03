package mil.emp3.api.view.utils;

import android.util.Log;

import org.cmapi.primitives.GeoIconStyle;
import org.cmapi.primitives.IGeoIconStyle;
import org.cmapi.primitives.IGeoLabelStyle;
import org.cmapi.primitives.IGeoStrokeStyle;

import mil.emp3.api.interfaces.core.IStorageManager;
import mil.emp3.api.interfaces.core.storage.IClientMapToMapInstance;
import mil.emp3.api.utils.ManagerFactory;
import mil.emp3.mapengine.interfaces.IEmpImageInfo;
import mil.emp3.mapengine.interfaces.IEmpResources;
import mil.emp3.mapengine.interfaces.IMapInstance;
import mil.emp3.view.R;

/**
 * This class implements the access to EMP core resources.
 */
public class EmpResources implements IEmpResources {
    private static final String TAG = EmpResources.class.getSimpleName();

    private static EmpResources instance = null;
    private final android.content.res.Resources contextResources;
    private java.util.concurrent.ConcurrentHashMap<Integer, EmpResourceImageInfo> resourceCache = new java.util.concurrent.ConcurrentHashMap<>();
    private IGeoIconStyle defaultIconStyle = null;
    final private IStorageManager storageManager = ManagerFactory.getInstance().getStorageManager();

    public static IEmpResources getInstance(android.content.res.Resources resources) {
        if (EmpResources.instance == null) {
            synchronized(EmpResources.class) {
                if (EmpResources.instance == null) {
                    EmpResources.instance = new EmpResources(resources);
                }
            }
        }

        return EmpResources.instance;
    }

    private EmpResources(android.content.res.Resources resources) {
        this.contextResources = resources;
    }

    private EmpResourceImageInfo getImageInfo(int resId) {
        EmpResourceImageInfo imageInfo = null;

        if (this.resourceCache.containsKey(resId)) {
            imageInfo = this.resourceCache.get(resId);
        } else {
            try {
                imageInfo = new EmpResourceImageInfo(this.contextResources, resId);
                this.resourceCache.put(resId, imageInfo);
            } catch (Exception e) {
                Log.e(TAG, "Resource (" + resId + ") not found.");
            }
        }

        return imageInfo;
    }

    @Override
    public IEmpImageInfo getDefaultIconImageInfo() {
        EmpResourceImageInfo imageInfo = this.getImageInfo(R.drawable.emp_default_icon);
        return imageInfo;
    }

    public IGeoIconStyle getDefaultIconStyle() {
        if (this.defaultIconStyle == null) {
            synchronized (EmpResources.class) {
                if (this.defaultIconStyle == null) {
                    EmpResourceImageInfo imageInfo = this.getImageInfo(R.drawable.emp_default_icon);

                    this.defaultIconStyle = new GeoIconStyle();
                    this.defaultIconStyle.setOffSetX(imageInfo.getImageBounds().width() / 2);
                    this.defaultIconStyle.setOffSetY(0);
                }
            }
        }

        return this.defaultIconStyle;
    }

    @Override
    public IEmpImageInfo getAndroidResourceIconImageInfo(int resId) {
        return this.getImageInfo(resId);
    }

    @Override
    public IGeoStrokeStyle getSelectedStrokeStyle(IMapInstance mapInstance) {
        return storageManager.getSelectedStrokeStyle(mapInstance);
    }

    @Override
    public IGeoLabelStyle getSelectedLabelStyle(IMapInstance mapInstance) {
        return storageManager.getSelectedLabelStyle(mapInstance);
    }

    @Override
    public double getSelectedIconScale(IMapInstance mapInstance) {
        return storageManager.getSelectedIconScale(mapInstance);
    }
}
