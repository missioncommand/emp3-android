package mil.emp3.test.emp3vv.navItems.basic_capability_test;

import android.app.Activity;

import mil.emp3.api.Circle;
import mil.emp3.api.interfaces.IMap;

public class CircleCapabilityTest extends SinglePointBasicShapeCapabilityTest<Circle> {
    private static String TAG = RectangleCapabilityTest.class.getSimpleName();

    private double circleRadius = 5000;

    public CircleCapabilityTest(Activity activity, IMap map1, IMap map2) {
        super(activity, map1, map2, new Circle());

        this.testFeature.setRadius(this.circleRadius);
    }

    protected double getFeatureRadius() {
        return this.testFeature.getRadius();
    }

    protected void setFeatureRadius(double value) {
        this.testFeature.setRadius(value);
    }


    @Override
    public String[] getSupportedUserActions() {
        String[] actions = {"Fill Color", "Radius", "Stroke Color", "Stroke Width"};
        return actions;
    }

    @Override
    public String[] getMoreActions() {
        String[] actions = {"Position"};
        return actions;
    }
}
