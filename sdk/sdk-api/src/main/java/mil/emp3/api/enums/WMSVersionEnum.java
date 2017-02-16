package mil.emp3.api.enums;

/**
 * WMS service versions.
 */
public enum WMSVersionEnum {
    /**
     * WMS version 1.1.
     */
    VERSION_1_1 ("1.1"),
    /**
     * WMS version 1.1.1.
     */
    VERSION_1_1_1 ("1.1.1"),
    /**
     * WMS version 1.3.
     * Deprecated value, use 1.3.0 instead
     */
    @Deprecated
    VERSION_1_3 ("1.3"),
    /**
     * WMS version 1.3.
     */
    VERSION_1_3_0 ("1.3.0");

    private final String version;

    WMSVersionEnum (String v) {
        version = v;
    }

    @Override
    public String toString() {
        if (this.version.equals("1.3")) {
            return "1.3.0";
        } else {
            return this.version;
        }
    }
}
