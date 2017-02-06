package mil.emp3.api.enums;

/**
 * Types of MirrorCache operational modes.
 */
public enum MirrorCacheModeEnum {

    /**
     * MirrorCache message exchanging is disabled.
     */
    DISABLED,

    /**
     * Local map changes should be published to MirrorCache service.
     */
    EGRESS,

    /**
     * Map should register with MirrorCache service to receive updates made to remote map(s)
     */
    INGRESS,

    /**
     * Local map changes should be published to MirrorCache service.
     * Map should register with MirrorCache service to receive updates made to remote map(s)
     */
    BIDIRECTIONAL,
    ;
}
