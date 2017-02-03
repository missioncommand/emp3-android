package mil.emp3.mirrorcache.api;


import android.os.Parcel;
import android.os.Parcelable;

import java.nio.ByteBuffer;
import java.util.ArrayList;


/**
 *  Base class for mirrorable elements
 */
public class MirrorCacheParcelable implements Parcelable {
    private static final String TAG = MirrorCacheParcelable.class.getSimpleName();


    // Mirrorable implementation

    // Mirror Cache Global UID for dynamic discovery of shared resources
    public String GUID=null;

    // Mirror cache master key, generally obtained by MirrorCache.genKey();
    public long mirrorKey;


    // Mirrorable implementation
    // mapping relationships, currently only parents can contain children
    public ArrayList<Long> contentsKeys = new ArrayList<>();

    // wrapped object
    public String payloadClassName;
    public IMirrorable payloadObject;
    public java.nio.ByteBuffer payloadByteBuffer;
    public int payloadLength;


    // every MirrorCacheParcelable is valid by default;
    // it needs to be invalidated to force payload serialization
    public boolean payloadValid = true;


    public MirrorCacheParcelable() {
    }

    public MirrorCacheParcelable(Parcel in) {
        readFromParcel(in);
//        Log.d(TAG, "construct from parcel " + key);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
//        android.util.Log.d(TAG, "write to parcel " + mirrorKey);

        // MirrorableBase map values: key, contents array...
        dest.writeLong(mirrorKey);
//        android.util.Log.d(TAG, "write: wrote key " + mirrorKey);

        // write the child keys
        if (contentsKeys == null) dest.writeInt(0);
        else {
            dest.writeInt(contentsKeys.size());

            for (long l : contentsKeys) {
                dest.writeLong(l);
            }
        }

        // server code
        if (payloadValid) {
            dest.writeString(payloadClassName);
//            android.util.Log.d(TAG, "write: write the payload class name");
            dest.writeInt(payloadLength);
//            android.util.Log.d(TAG, "write: write the previously serialized payload to the parcel");
            dest.writeByteArray(payloadByteBuffer.array());
        }

        // Client Code - serialize the payload
        else {
            if (this.payloadObject == null) {
                dest.writeString(null);
//                android.util.Log.d(TAG, "write: write NULL no payload class name");
            }
            else {
//                android.util.Log.d(TAG, "write: write the payload class name " + this.payloadObject.getClass().getName());
                dest.writeString(this.payloadObject.getClass().getName());
            }

//            android.util.Log.d(TAG, "write: serialize payload");
            payloadLength = payloadObject.length();
            dest.writeInt(payloadLength);
//            android.util.Log.d(TAG, "write: write payload length " + payloadLength);

            payloadByteBuffer = ByteBuffer.allocate(payloadObject.length());
            payloadByteBuffer.position(0);
            payloadObject.writeToByteArray(payloadByteBuffer);

//            android.util.Log.d(TAG, "write: write the payload to the parcel");
            dest.writeByteArray(payloadByteBuffer.array());
        }



    }

    public void readFromParcel(Parcel in) {

        // MirrorableBase map values: key, contents array...
        this.mirrorKey = in.readLong();
//        android.util.Log.d(TAG, "read : mirrorKey " + mirrorKey );

        // serialize the child keys
        int numChidlren = in.readInt();
        contentsKeys = new ArrayList<Long>();
        for (int i=0; i<numChidlren; i++) {
            contentsKeys.add(in.readLong());
        }

        this.payloadClassName = in.readString();
//        android.util.Log.d(TAG, "read : payloadClassName " + payloadClassName );

        this.payloadLength = in.readInt();
//        android.util.Log.d(TAG, "read : payloadLength " + payloadLength );

        payloadByteBuffer = ByteBuffer.allocate(payloadLength);
        in.readByteArray(payloadByteBuffer.array());
//        android.util.Log.d(TAG, "read : payloadByteBuffer" );



        /* DO NOT inflate payload */
        // this allows the server to not look into payload data unnecesarily

//        android.util.Log.d(TAG, "read from parcel " + mirrorKey);
    }

    @Override
    public IMirrorable clone() throws CloneNotSupportedException {
        return (IMirrorable) super.clone();
    }


    @SuppressWarnings("unused")
    public static final Parcelable.Creator<MirrorCacheParcelable> CREATOR = new Parcelable.Creator<MirrorCacheParcelable>() {
        @Override
        public MirrorCacheParcelable createFromParcel(Parcel in) {
            return new MirrorCacheParcelable(in);
        }

        @Override
        public MirrorCacheParcelable[] newArray(int size) {
            return new MirrorCacheParcelable[size];
        }
    };




    /*
     * Update notification section
     */
    private ArrayList<IMirrorableListener> listeners = new ArrayList<>();

    public void onUpdate() {
        for (IMirrorableListener ml : listeners) {
            ml.onUpdate(this.payloadObject);
        }
    }
    public void onDelete() {
        for (IMirrorableListener ml : listeners) {
            ml.onDelete(this.payloadObject);
        }
    }
    public void addListener(IMirrorableListener ml) {
        listeners.add(ml);
    }
    public void removeListener(IMirrorableListener ml) {
        listeners.remove(ml);
    }
    public void removeListeners() {
        listeners.clear();
    }
}
