package mil.emp3.api.interfaces;

import org.cmapi.primitives.IGeoColor;

/**
 * This class defines the interface to an EmpPropertyList object.
 */
public interface IEmpPropertyList extends java.util.Map<String, Object> {
    /**
     * This method is used internally to generate an exception if the key is not present on the list.
     * @param key
     * @return
     */
    boolean genExceptionIfNotPresent(String key);

    /**
     * This method return the string value of the entry identified by the key provided. An exception
     * is generated if the value contained in the list is not a String.
     * @param key
     * @return
     */
    String getStringValue(String key);

    /**
     * This method return the IGeoColor object from the value of the entry identified by the key provided. An exception
     * is generated if the value contained in the list is not a IGeoColor object.
     * @param key
     * @return
     */
    IGeoColor getColorValue(String key);

    /**
     * This method return the double value from the value of the entry identified by the key provided. An exception
     * is generated if the value contained in the list is not a double.
     * @param key
     * @return
     */
    double getDoubleValue(String key);

    /**
     * Gives the mirror cache mode enumeration
     * @param key
     * @return enum
     */
    mil.emp3.api.enums.MirrorCacheModeEnum getMirrorCacheModeEnum(String key);

    /**
     * Gets the Android context
     * @param key
     * @return context
     */
    android.content.Context getContext(String key);
}
