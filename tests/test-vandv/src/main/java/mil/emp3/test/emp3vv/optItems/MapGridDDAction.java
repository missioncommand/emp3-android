package mil.emp3.test.emp3vv.optItems;

import android.app.Activity;

import mil.emp3.api.enums.MapGridTypeEnum;
import mil.emp3.api.interfaces.IMap;
import mil.emp3.test.emp3vv.common.ExecuteTest;
import mil.emp3.test.emp3vv.common.OptItemBase;

/**
 * This class implements the Map DD grid action
 */

public class MapGridDDAction extends OptItemBase {
    private static String TAG = MapGridDDAction.class.getSimpleName();

    public MapGridDDAction(Activity activity, IMap map1, IMap map2) {
        super(activity, map1, map2, TAG, true);
    }

    @Override
    public void run() {
        int iMap = ExecuteTest.getCurrentMap();

        if (iMap == -1) {
            return;
        }

        maps[iMap].setGridType(MapGridTypeEnum.DD);
    }
}
