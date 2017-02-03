package mil.emp3.mirrorcache.mirrorables;

import org.cmapi.primitives.GeoCamera;
import org.cmapi.primitives.IGeoCamera;

import java.nio.ByteBuffer;

import mil.emp3.api.Camera;
import mil.emp3.mirrorcache.api.IMirrorable;

public class MirrorableCamera extends Camera implements IMirrorable {
    private static final String TAG = MirrorableCamera.class.getName();

    private Long mirrorKey; // this is never used by app dev

    public MirrorableCamera() {
        super(new GeoCamera());
    }
    public MirrorableCamera(IGeoCamera c) {
        super(c);
    }

    @Override
    public void readFromByteArray(ByteBuffer in) {
        setLatitude(in.getDouble());
        setLongitude(in.getDouble());
        setAltitude(in.getDouble());
        setHeading(in.getDouble());
        setTilt(in.getDouble());
        setRoll(in.getDouble());
    }

    @Override
    public void writeToByteArray(ByteBuffer out) {
        out.putDouble(getLatitude());
        out.putDouble(getLongitude());
        out.putDouble(getAltitude());
        out.putDouble(getHeading());
        out.putDouble(getTilt());
        out.putDouble(getRoll());
    }

    @Override
    public int length() {
        return
                (Double.SIZE / 8) +
                (Double.SIZE / 8) +
                (Double.SIZE / 8) +
                (Double.SIZE / 8) +
                (Double.SIZE / 8) +
                (Double.SIZE / 8);
    }

    @Override
    public Long getMirrorKey() {
        return mirrorKey;
    }

    @Override
    public void setMirrorKey(Long mirrorKey) {
        this.mirrorKey = mirrorKey;
    }
}

