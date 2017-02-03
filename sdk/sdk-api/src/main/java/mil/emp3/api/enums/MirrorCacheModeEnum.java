package mil.emp3.api.enums;

/**
 * This class enumerates the different operational modes of MirrorCache.
 */
public enum MirrorCacheModeEnum {

    /**
     * Indicates that no MirrorCache message exchanging should occur.
     */
    DISABLED,

    /**
     * Indicates that local map changes should be synchronized remotely.
     */
    EGRESS,

    /**
     * Indicates that remote map changes should be synchronized locally.
     */
    INGRESS,

    /**
     * Indicates that both local and remote map changes should be synchronized.
     */
    BIDIRECTIONAL,
    ;
}
