package mil.emp3.test.emp3vv.navItems.geoJSON_test;

import android.app.Activity;

import mil.emp3.api.interfaces.IMap;
import mil.emp3.test.emp3vv.common.NavItemBase;

public class GeoJSONTest extends NavItemBase {

    public GeoJSONTest(Activity activity, IMap map1, IMap map2) {
        super(activity, map1, map2, TAG);
    }

    @Override
    protected void test0() {

    }

    @Override
    protected boolean exitTest() {
        return false;
    }

    @Override
    public boolean actOn(String userAction) {
        return false;
    }
}
