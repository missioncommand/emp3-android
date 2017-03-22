package mil.emp3.test.emp3vv.navItems.basic_capability_test;

import android.app.Activity;

import mil.emp3.api.Rectangle;
import mil.emp3.api.Square;
import mil.emp3.api.interfaces.IMap;

public class SquareCapabilityTest extends SinglePointBasicShapeCapabilityTest<Square> {
    private static String TAG = SquareCapabilityTest.class.getSimpleName();

    private int squareWidth = 5000;


    public SquareCapabilityTest(Activity activity, IMap map1, IMap map2) {
        super(activity, map1, map2, new Square());

        this.testFeature.setWidth(this.squareWidth);
    }

    protected double getFeatureWidth() {
        return this.testFeature.getWidth();
    }

    protected void setFeatureWidth(double value) {
        this.testFeature.setWidth(value);
    }

    @Override
    public String[] getSupportedUserActions() {
        String[] actions = {"Fill Color", "Width", "Azimuth"};
        return actions;
    }

    @Override
    public String[] getMoreActions() {
        String[] actions = {"Stroke Color", "Stroke Width", "Position"};
        return actions;
    }
}