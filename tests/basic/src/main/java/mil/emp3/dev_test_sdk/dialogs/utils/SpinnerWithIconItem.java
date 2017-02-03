package mil.emp3.dev_test_sdk.dialogs.utils;

/**
 *
 */
public abstract class SpinnerWithIconItem {
    private final int sItemImageId;

    public SpinnerWithIconItem(int iImageId) {
        this.sItemImageId = iImageId;
    }

    public abstract String getText();

    public int getImage() {
        return this.sItemImageId;
    }
}
