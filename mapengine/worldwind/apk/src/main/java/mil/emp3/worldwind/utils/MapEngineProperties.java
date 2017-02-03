package mil.emp3.worldwind.utils;

import mil.emp3.mapengine.interfaces.IMapEngineProperties;

/**
 * Created by ish.rivera on 6/15/2016.
 */
public class MapEngineProperties implements IMapEngineProperties {
    @Override
    public String getName() {
        return "NASA Worldwind Map Engine";
    }

    @Override
    public String getVersion() {
        return "0.2.5";
    }

    @Override
    public String getHelp() {
        return "";
    }
}
