package mil.emp3.api.enums;

/**
 * This class enumerates all the configuration property keys used by properties parameter of different API calls. Each enumerated
 * value requires a specific value type.
 */
public enum Property {
    /**
     * This enumerated value defines the map's fully qualified class name for a new map. The key value pair requires
     * a string value.
     */
    ENGINE_CLASSNAME ("engine.className"),
    /**
     * This enumerated value defines the map's fully qualified android APK name where the class indicated
     * in ENGINE_CLASSNAME is implemented. The key value pair requires a string value.
     */
    ENGINE_APKNAME   ("engine.apkName")  ,
    /**
     * This enumerated value defines the stoke color used to select features. The key value pair requires an IGeoColor
     * value. The default color is yellow.
     */
    DEFAULT_SELECT_STROKE_COLOR ("default.stroke.color"),
    /**
     * This enumerated value defines the scale factor to use when an icon type feature is selected. The key value pair
     * requires a double value. The default value is 1.2.
     */
    DEFAULT_SELECT_ICON_SCALE   ("default.icon.scale"),
    /**
     * This enumerated value defines the debug setting. The key value pair requires a boolean value and the default is
     * false. Setting this value to true will generate additional log statements.
     */
    DEBUG                       ("debug"),
    /**
     * This enumerated value defines the proxy url to be used when a feature or map service indicates
     * useProxy set to true. The default value is null. The key value pair requires a string value.
     */
    PROXY_URL                   ("proxy.url"),
    /**
     * This enumerated value defines if the mirror cache is to be used and in what mode. The default
     * value is {@link mil.emp3.api.enums.MirrorCacheModeEnum#DISABLED}. This property requires a
     * {@link mil.emp3.api.enums.MirrorCacheModeEnum} value.
     */
    MIRROR_CACHE_MODE           ("mirror.cache.mode"),
    ;

    final private String value;
    Property(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    static public Property fromValue(String v) {
        for (Property c : Property.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }
}
