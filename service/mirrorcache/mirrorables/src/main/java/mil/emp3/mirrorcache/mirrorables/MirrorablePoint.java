package mil.emp3.mirrorcache.mirrorables;

import org.cmapi.primitives.GeoPoint;
import org.cmapi.primitives.GeoPosition;
import org.cmapi.primitives.IGeoPoint;
import org.cmapi.primitives.IGeoPosition;

import java.nio.ByteBuffer;
import java.util.UUID;

import mil.emp3.api.Point;
import mil.emp3.mirrorcache.api.IMirrorable;

public class MirrorablePoint extends Point implements IMirrorable {
    private static final String TAG = MirrorablePoint.class.getName();

    static final private int FLAG_NULL     = 0;
    static final private int FLAG_NOT_NULL = 1;

    private Long mirrorKey; // this is never used by app dev

    public MirrorablePoint() {
        super(new GeoPoint());
    }
    public MirrorablePoint(IGeoPoint p) {
        super(p);
    }

    @Override
    public void readFromByteArray(ByteBuffer in) {

        //geoId
        byte[] bytes = new byte[in.getInt()];
        in.get(bytes);
        setGeoId(UUID.fromString(new String(bytes)));

        // iconUri
        if (FLAG_NOT_NULL == in.getInt()) {
            bytes = new byte[in.getInt()];
            in.get(bytes);
            setIconURI(new String(bytes));
        }

        // position
        if (FLAG_NOT_NULL == in.getInt()) {

            final IGeoPosition position = new GeoPosition();
            position.setLatitude(in.getDouble());
            position.setLongitude(in.getDouble());
            position.setAltitude(in.getDouble());

            setPosition(position);
        }
    }

    @Override
    public void writeToByteArray(ByteBuffer out) {

        // geoId
        out.putInt(getGeoId().toString().length());
        out.put(getGeoId().toString().getBytes());

        // iconUri
        if (getIconURI() != null) {
            out.putInt(FLAG_NOT_NULL);
            out.putInt(getIconURI().length());
            out.put(getIconURI().getBytes());

        } else {
            out.putInt(FLAG_NULL);
        }

        // position
        if (getPosition() != null) {
            out.putInt(FLAG_NOT_NULL);
            out.putDouble(getPosition().getLatitude());
            out.putDouble(getPosition().getLongitude());
            out.putDouble(getPosition().getAltitude());

        } else {
            out.putInt(FLAG_NULL);
        }
    }

    @Override
    public int length() {
        return
            /* geoId    */ Integer.SIZE + getGeoId().toString().length() +
            /* iconUri  */ Integer.SIZE + (getIconURI() != null ? Integer.SIZE + getIconURI().length() : 0) +
            /* position */ Integer.SIZE + (getPosition() != null ? ((Double.SIZE / 8) * 3) : 0);
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
