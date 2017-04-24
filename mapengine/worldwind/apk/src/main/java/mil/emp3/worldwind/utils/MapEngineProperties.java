package mil.emp3.worldwind.utils;

import mil.emp3.mapengine.interfaces.IMapEngineProperties;

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
