package mil.emp3.api.utils;

import com.google.maps.android.kml.KmlStyle;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import mil.emp3.api.Overlay;
import mil.emp3.api.interfaces.IContainer;
import mil.emp3.api.interfaces.IFeature;
import mil.emp3.api.interfaces.IOverlay;

/**
 * This class implements an entry for the hierarchy of EMP.
 */

public class EmpObjectHierarchyEntry {
    private final List<EmpObjectHierarchyEntry> childrenList = new ArrayList<>();
    private final IContainer empObject;
    private final EmpObjectHierarchyEntry parentEntry;
    private final Map<String, String> styleMap;
    private final Map<String, KmlStyle> styles;

    public EmpObjectHierarchyEntry(EmpObjectHierarchyEntry parent, IContainer object,
                                   Map<String, String> styleMap, Map<String, KmlStyle> styles) {
        empObject = object;
        parentEntry = parent;
        this.styleMap = styleMap;
        this.styles = styles;

        if (null != parentEntry) {
            parentEntry.addChild(this);
        }
    }

    public IContainer getEmpObject() {
        return this.empObject;
    }

    public EmpObjectHierarchyEntry getParent() {
        return this.parentEntry;
    }

    public List<EmpObjectHierarchyEntry> getChildren() {
        return this.childrenList;
    }

    public void  addChild(EmpObjectHierarchyEntry child) {
        this.childrenList.add(child);
    }

    public boolean isOverlayEntry() {
        return (this.empObject instanceof IOverlay);
    }

    public boolean isFeatureEntry() {
        return (this.empObject instanceof IFeature);
    }

    public Map<String, String> getStyleMap() {return this.styleMap;}

    public Map<String, KmlStyle> getStyles() { return  this.styles;}
}
