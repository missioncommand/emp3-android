package mil.emp3.api.enums;

public enum WMTSVersionEnum {
    /**
     * This enumerated value indicates WMTS version 1.0.0
     */
    VERSION_1_0_0 ("1.0.0");

    private final String version;

    WMTSVersionEnum (String v) {
        version = v;
    }

    @Override
    public String toString() {
        return this.version;
    }
}
