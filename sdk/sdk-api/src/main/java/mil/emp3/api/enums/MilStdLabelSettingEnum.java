package mil.emp3.api.enums;

/**
 * Types of MilStd label settings. use setMilStdLabels method on {@link mil.emp3.api.abstracts.Map} set the label settings
 */
public enum MilStdLabelSettingEnum {
    /**
     * Map engine shall display required MilStd labels only.
     * (V, L, S, AA, AB, AC)
     */
    REQUIRED_LABELS,
    /**
     * Map engine shall display the common MilStd labels in addition to the required labels.
     * (H, M, T, T1, CN)
     */
    COMMON_LABELS,
    /**
     * Map engine shall display all MilStd labels in addition to the common labels.
     * (C, F, G, H1, H2, J, K, N, P, W, W1, X, Y, Z)
     */
    ALL_LABELS
}
