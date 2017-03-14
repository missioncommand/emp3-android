package mil.emp3.api.enums;

/**
 * This class enumerates the map grid line types that can be set on a map.
 */

public enum MapGridTypeEnum {
    /**
     * This enumerated value indicates that the map will display no grid lines. It is the default value.
     */
    NONE,
    /**
     * This enumerated value indicates that the map will display MGRS grid lines.
     */
    MGRS,
    /**
     * This enumerated value indicates that the map will display UTM grid lines.
     */
    UTM,
    /**
     * This enumerated value indicates that the map will display Degree, Minute and seconds grid lines.
     */
    DMS,
    /**
     * This enumerated value indicates that the map will display decimal degrees grid lines.
     */
    DD
}
