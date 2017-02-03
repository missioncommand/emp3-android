package mil.emp3.api.view.utils;

import android.content.res.TypedArray;

import mil.emp3.api.enums.MirrorCacheModeEnum;

/**
 * This static class provides methods to retrieve specific typed values from a type array.
 */
public class TypeArrayParser {
    public static String getStringAttribute(TypedArray typeArray, int attributeId) {
        String retValue = null;
        CharSequence csValue = typeArray.getText(attributeId);

        if (csValue != null) {
            retValue = csValue.toString();
        }

        return retValue;
    }

    public static MirrorCacheModeEnum getMirrorCacheModeAttribute(TypedArray typeArray, int attributeId) {
        MirrorCacheModeEnum retValue = null;

        CharSequence csValue = typeArray.getText(attributeId);

        if (csValue != null) {
            try {
                retValue = MirrorCacheModeEnum.valueOf(csValue.toString());
            } catch (Exception ex) {
                throw new IllegalArgumentException("Invalid MirrorCacheModeEnum value in styled attribute.");
            }
        }

        return retValue;
    }
}
