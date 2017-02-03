package mil.emp3.test.emp3vv.utils;

import android.util.Log;

import org.cmapi.primitives.IGeoBase;

import java.util.ArrayList;
import java.util.List;

import mil.emp3.api.interfaces.IContainer;
import mil.emp3.api.interfaces.IFeature;
import mil.emp3.api.interfaces.IMap;
import mil.emp3.api.interfaces.IOverlay;

public class MapNamesUtility {
    private static String TAG = MapNamesUtility.class.getSimpleName();

    public static boolean isNameInUse(IMap map, String name) {
        return getNames(map, true, true, true).contains(name);
    }

    public static List<String> getNames(IMap map, boolean includeMap, boolean includeOverlays, boolean includeFeatures ) {

        List<String> names = new ArrayList<>();

        if(includeMap) {
            if((map.getName() != null) && (map.getName().trim().length() != 0)) {
                names.add(map.getName());
            }
        }

        if(includeOverlays) {
            List<IOverlay> overlays = map.getAllOverlays();
            if (null != overlays) {
                for (IOverlay overlay : overlays) {
                    if((overlay.getName() != null) && (overlay.getName().trim().length() != 0)) {
                        names.add(overlay.getName());
                    }
                }
            }
        }

        if(includeFeatures) {
            List<IFeature> features = map.getAllFeatures();
            if (null != features) {
                for (IFeature feature : features) {
                    if((feature.getName() != null) && (feature.getName().trim().length() != 0)) {
                        names.add(feature.getName());
                    }
                }
            }
        }

        return names;
    }

    public static List<String> getParentNames(IContainer container) {
        Log.d(TAG, "getParentName " + container.getName());
        List<String> names = new ArrayList<>();
        List<IContainer> parents = container.getParents();
        Log.d(TAG, "parents count " + parents.size());
        if(null != parents) {
            for (IContainer c : parents) {
                names.add(c.getName());
            }
        }
        return names;
    }

    public static List<String> getChildrenNames(IContainer container) {
        Log.d(TAG, "getChildrenName " + container.getName());
        List<String> names = new ArrayList<>();
        List<IGeoBase> children = container.getChildren();
        Log.d(TAG, "children count " + children.size());
        if(null != children) {
            for (IGeoBase c : children) {
                    names.add(c.getName());
            }
        }
        return names;
    }

    public static IContainer getContainer(IMap map, String name) {

        if((null == map) || (null == name)) {
            throw new IllegalArgumentException("map and name must be non-null");
        }
        if((map.getName() != null) && (map.getName().equals(name))) {
                return map;
        }
        List<IOverlay> overlays = map.getAllOverlays();
        if (null != overlays) {
            for (IOverlay overlay : overlays) {
                if((overlay.getName() != null) && (overlay.getName().equals(name))) {
                        return overlay;
                }
            }
        }

        List<IFeature> features = map.getAllFeatures();
        if (null != features) {
            for (IFeature feature : features) {
                if((feature.getName() != null) && (feature.getName().equals(name))) {
                    return feature;
                }
            }
        }
        return null;
    }

    public static List<String> getDescendantsNames(IContainer container) {
        List<String> names = new ArrayList<>();
        List<IContainer> descendants = new ArrayList<>();
        if(container instanceof IMap) {
            IMap map = (IMap) container;
            descendants.addAll(map.getAllOverlays());
            descendants.addAll(map.getAllFeatures());
        } else if(container instanceof IOverlay) {
            IOverlay overlay = (IOverlay) container;
            descendants.addAll(overlay.getOverlays());
            descendants.addAll(overlay.getFeatures());
        } else if(container instanceof IFeature) {
            IFeature feature = (IFeature) container;
            descendants.addAll(feature.getChildFeatures());
        }

        for (IContainer c : descendants) {
            if((c.getName() != null) && (0 != c.getName().trim().length())) {
                names.add(c.getName());
            }
        }
        return names;
    }
}
