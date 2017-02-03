package mil.emp3.api.enums;

/**
 * Configuration property keys used by properties parameter of various API calls. Each enumerated
 * value requires a specific value type.
 */
public enum Property {
    /**
     * Fully qualified class name for a map engine. Property value must be of String type
     */
    ENGINE_CLASSNAME ("engine.className"),
    /**
     * Fully qualified android APK name that contains an implementation of engine.className. Property value must be of String type.
     */
    ENGINE_APKNAME   ("engine.apkName")  ,
    /**
     * Stroke color used to highlight selected feature(s) (Features selected using selectFeature method of IMap). The property value must be of
     * type IGeoColor. The default color is yellow.
     */
    DEFAULT_SELECT_STROKE_COLOR ("default.stroke.color"),
    /**
     * Scale factor to use when an icon type feature is selected. Property value must be of type double. The default value is 1.2.
     */
    DEFAULT_SELECT_ICON_SCALE   ("default.icon.scale"),
    /**
     * Debug setting for the API and implementation code. Property value must be of type boolean. The default is
     * false. Setting this value to true will generate additional log statements.
     */
    DEBUG                       ("debug"),
    /**
     * Proxy url to be used when a feature or map service set useProxy to true. The default value is null. Property value must be of type string.
     */
    PROXY_URL                   ("proxy.url"),
    /**
     * MirrorCache mode to be used. The default value is {@link mil.emp3.api.enums.MirrorCacheModeEnum#DISABLED}. This property requires a
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
