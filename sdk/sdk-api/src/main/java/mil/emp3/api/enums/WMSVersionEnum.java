package mil.emp3.api.enums;

/**
 *
 * This class enumerates the WMS service versions.
 */
public enum WMSVersionEnum {
    /**
     * This enumerated value indicates WMS version 1.1
     */
    VERSION_1_1,
    /**
     * This enumerated value indicates WMS version 1.1.1
     */
    VERSION_1_1_1,
    /**
     * This enumerated value indicates WMS version 1.3
     */
    VERSION_1_3;

    @Override
    public String toString() {
        switch (this) {
            case VERSION_1_1:
                return "1.1";
            case VERSION_1_1_1:
                return "1.1.1";
            case VERSION_1_3:
                return "1.3";
        }
        
        return "1.3";
    }
}
