package mil.emp3.test.emp3vv.navItems.basic_capability_test;

import android.app.Activity;

import mil.emp3.api.Rectangle;
import mil.emp3.api.interfaces.IMap;

/**
 * This class defines the capability test for the Geo Rectangle feature.
 */

public class RectangleCapabilityTest extends SinglePointBasicShapeCapabilityTest<Rectangle> {
    private static String TAG = RectangleCapabilityTest.class.getSimpleName();

    private int rectWidth = 5000;
    private int rectHeight = 10000;


    public RectangleCapabilityTest(Activity activity, IMap map1, IMap map2) {
        super(activity, map1, map2, new Rectangle());

        this.testFeature.setHeight(this.rectHeight);
        this.testFeature.setWidth(this.rectWidth);
    }

    protected double getFeatureWidth() {
        return this.testFeature.getWidth();
    }

    protected void setFeatureWidth(double value) {
        this.testFeature.setWidth(value);
    }

    protected double getFeatureHeight() {
        return this.testFeature.getHeight();
    }

    protected void setFeatureHeight(double value) {
        this.testFeature.setHeight(value);
    }

    @Override
    public String[] getSupportedUserActions() {
        String[] actions = {"Fill Color", "Width", "Height", "Azimuth"};
        return actions;
    }

    @Override
    public String[] getMoreActions() {
        String[] actions = {"Stroke Color", "Stroke Width", "Position"};
        return actions;
    }
}
