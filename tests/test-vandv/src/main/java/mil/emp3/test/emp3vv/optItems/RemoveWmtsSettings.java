package mil.emp3.test.emp3vv.optItems;


import android.app.Activity;

import mil.emp3.api.interfaces.IMap;

public class RemoveWmtsSettings extends WmsSettings {
    public RemoveWmtsSettings(Activity activity, IMap map1, IMap map2){
        super(activity, map1, map2);
        setActionSet(false);
    }
}