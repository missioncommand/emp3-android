package mil.emp3.api.enums;

/**
 * The font size modifier values that should applied to text during rendering of {@link mil.emp3.api.Text} and Tactical Graphic {@link mil.emp3.api.MilStdSymbol} feature.
 */
public enum FontSizeModifierEnum {
    /**
     * Text font size is reduced by 40% of the indicated size.
     */
    SMALLEST,
    /**
     * Text font size is reduced by 20% of the indicated size.
     */
    SMALLER,
    /**
     * Text is rendered in its indicated size. No size change is applied.
     */
    NORMAL,
    /**
     * Text font size is increased by 20% of its indicated value.
     */
    LARGER,
    /**
     * Text font size is increased by 40% of its indicated value.
     */
    LARGEST;

    public float getScaleValue() {
        switch (this) {
            case SMALLEST:
                return (float) 0.6;
            case SMALLER:
                return (float) 0.8;
            case NORMAL:
                return (float) 1.0;
            case LARGER:
                return (float) 1.2;
            case LARGEST:
                return (float) 1.4;
        }
        return (float) 1.0;
    }
}
