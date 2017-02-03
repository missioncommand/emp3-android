package mil.emp3.echo;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

import org.cmapi.primitives.IGeoMilSymbol;
import org.cmapi.primitives.IGeoPosition;

import java.util.Map;

import mil.emp3.api.interfaces.ICamera;
import mil.emp3.mirrorcache.api.IMirrorCacheStateChangeListener;
import mil.emp3.mirrorcache.api.IMirrorable;
import mil.emp3.mirrorcache.api.MirrorCache;

public class EchoActivity extends Activity {
    private static final String TAG = EchoActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");

        setContentView(R.layout.activity_echo);

        MirrorCache.getInstance().onCreate(this, "mil.emp3.mirrorcache.service", "mil.emp3.mirrorcache.service.MirrorCacheService");
        MirrorCache.getInstance().addStateChangeListener(new IMirrorCacheStateChangeListener() {
            @Override
            public void onMirrorred() {
                Log.d(TAG, "onMirrorred");
            }

            @Override
            public void onDelete(IMirrorable o) {
                Log.d(TAG, "onDelete");
            }

            @Override
            public void onUpdate(IMirrorable o) {
                Log.d(TAG, "onUpdate");

                if (o instanceof ICamera) {
                    Log.d(TAG, "[ Echo: ICamera ]");

                    final ICamera camera = (ICamera) o;
                    Log.d(TAG, "\tgeoId: " + camera.getGeoId());
                    Log.d(TAG, "\tlatitude: " + camera.getLatitude());
                    Log.d(TAG, "\tlongitude: " + camera.getLongitude());
                    Log.d(TAG, "\taltitude: " + camera.getAltitude());
                    Log.d(TAG, "\theading: " + camera.getHeading());
                    Log.d(TAG, "\ttilt: " + camera.getTilt());
                    Log.d(TAG, "\troll: " + camera.getRoll());

                } else if (o instanceof IGeoMilSymbol) {
                    Log.d(TAG, "[ Echo: IGeoMilSymbol ]");

                    final IGeoMilSymbol symbol = (IGeoMilSymbol) o;
                    Log.d(TAG, "\tgeoId: " + symbol.getGeoId());

                    Log.d(TAG, "\tname: " + symbol.getName());
                    Log.d(TAG, "\tsymbolCode: " + symbol.getSymbolCode());
                    Log.d(TAG, "\tsymbolStandard: " + symbol.getSymbolStandard().toString());

                    Log.d(TAG, "\tpositions (" + symbol.getPositions().size() + "): ");
                    for (IGeoPosition position : symbol.getPositions()) {
                        Log.d(TAG, "\t\tlat=" + position.getLatitude() + ", long=" + position.getLongitude());
                    }

                    Log.d(TAG, "\tmodifiers (" + symbol.getModifiers().size() + "): ");
                    for (Map.Entry entry : symbol.getModifiers().entrySet()) {
                        Log.d(TAG, "\t\t" + entry.getKey() + " = " + entry.getValue());
                    }
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "onDestroy");

        MirrorCache.getInstance().onDestroy();

        super.onDestroy();
    }

}
