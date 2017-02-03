package mil.emp3.api.enums;

/**
 * This class enumerates the different MilStd label settings.
 */
public enum MilStdLabelSettingEnum {
    /**
     * This value indicates that the map engine shall display required MilStd labels only.
     * V, L, S, AA, AB, AC
     */
    REQUIRED_LABELS,
    /**
     * This value indicates that the map engine shall display the common MilStd labels.
     * In addition to the required labels.
     * H, M, T, T1, CN
     */
    COMMON_LABELS,
    /**
     * This value indicates that the map engine shall display all MilStd labels.
     * In addition to the common labels.
     * C, F, G, H1, H2, J, K, N, P, W, W1, X, Y, Z
     */
    ALL_LABELS
}
