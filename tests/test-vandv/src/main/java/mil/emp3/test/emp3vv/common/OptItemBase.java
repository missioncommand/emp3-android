package mil.emp3.test.emp3vv.common;

import android.app.Activity;

import mil.emp3.api.interfaces.IMap;

public abstract class OptItemBase implements Runnable {

    private final String TAG;
    protected final Activity activity;
    protected OnTestStatusUpdateListener statusUpdateListener;
    protected final IMap maps[] = new IMap[2];

    public OptItemBase(Activity activity, IMap map1, IMap map2, String tag, boolean doSetup) {

        maps[0] = map1;
        maps[1] = map2;

        this.activity = activity;
        TAG = tag;

        if(activity instanceof OnTestStatusUpdateListener) statusUpdateListener = (OnTestStatusUpdateListener) activity;

        if(doSetup) {
            setUp();
        }
    }

    private void setUp() {

    }
}
