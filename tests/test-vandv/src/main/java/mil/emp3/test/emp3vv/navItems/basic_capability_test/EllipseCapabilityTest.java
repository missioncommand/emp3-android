package mil.emp3.test.emp3vv.navItems.basic_capability_test;

import android.app.Activity;

import mil.emp3.api.Circle;
import mil.emp3.api.Ellipse;
import mil.emp3.api.interfaces.IMap;

public class EllipseCapabilityTest extends SinglePointBasicShapeCapabilityTest<Ellipse> {
    private static String TAG = EllipseCapabilityTest.class.getSimpleName();

    private double semiMinor = 5000;
    private double semiMajor = 10000;

    public EllipseCapabilityTest(Activity activity, IMap map1, IMap map2) {
        super(activity, map1, map2, new Ellipse());

        this.testFeature.setSemiMajor(this.semiMajor);
        this.testFeature.setSemiMinor(this.semiMinor);
        this.testFeature.setAzimuth(0);
    }

    protected double getFeatureSemiMajor() {
        return this.testFeature.getSemiMajor();
    }

    protected void setFeatureSemiMajor(double value){
        this.testFeature.setSemiMajor(value);
    }

    protected double getFeatureSemiMinor() {
        return this.testFeature.getSemiMinor();
    }

    protected void setFeatureSemiMinor(double value){
        this.testFeature.setSemiMinor(value);
    }

    @Override
    public String[] getSupportedUserActions() {
        String[] actions = {"Fill Color", "Radii", "Azimuth"};
        return actions;
    }

    @Override
    public String[] getMoreActions() {
        String[] actions = {"Stroke Color", "Stroke Width", "Position"};
        return actions;
    }
}
