package mil.emp3.api.utils;

import org.cmapi.primitives.IGeoColor;

import mil.emp3.api.enums.MirrorCacheModeEnum;
import mil.emp3.api.enums.Property;
import mil.emp3.api.interfaces.IEmpPropertyList;

/**
 * This class implements a list of key, value pairs where the key is a string and the value is an Object.
 */
// java.utils.Properties only supports string values.
public class EmpPropertyList extends java.util.HashMap<String, Object> implements IEmpPropertyList {

    @Override
    public boolean genExceptionIfNotPresent(String key) {
        if (!this.containsKey(key)) {
            throw new IllegalArgumentException("Property " + key + " is required.");
        }
        return true;
    }

    @Override
    public String getStringValue(String key) {
        if (this.containsKey(key)) {
            if (!(this.get(key) instanceof String)) {
                throw new IllegalArgumentException("Property " + key + " value must be a string.");
            }
            return (String) this.get(key);
        }
        return null;
    }

    @Override
    public IGeoColor getColorValue(String key) {
        if (this.containsKey(key)) {
            if (!(this.get(key) instanceof IGeoColor)) {
                throw new IllegalArgumentException("Property " + key + " value must be an IGeoColor object.");
            }
            return (IGeoColor) this.get(key);
        }
        return null;
    }

    @Override
    public double getDoubleValue(String key) {
        if (this.containsKey(key)) {
            if (!(this.get(key) instanceof Double)) {
                throw new IllegalArgumentException("Property " + key + " value must be a double.");
            }
            return (double) this.get(key);
        }
        return Double.NaN;
    }

    @Override
    public mil.emp3.api.enums.MirrorCacheModeEnum getMirrorCacheModeEnum(String key) {
        if (this.containsKey(key)) {
            if (!(this.get(key) instanceof mil.emp3.api.enums.MirrorCacheModeEnum)) {
                throw new IllegalArgumentException("Property " + key + " value must be a mil.emp3.api.enums.MirrorCacheModeEnum value.");
            }
            return (mil.emp3.api.enums.MirrorCacheModeEnum) this.get(key);
        }
        return null;
    }

    @Override
    public android.content.Context getContext(String key) {
        if (this.containsKey(key)) {
            if (!(this.get(key) instanceof android.content.Context)) {
                throw new IllegalArgumentException("Property " + key + " value must be a android.content.Context value.");
            }
            return (android.content.Context) this.get(key);
        }
        return null;
    }
}
