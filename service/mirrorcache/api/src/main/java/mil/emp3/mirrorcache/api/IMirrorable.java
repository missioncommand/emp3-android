package mil.emp3.mirrorcache.api;

import java.nio.ByteBuffer;

public interface IMirrorable {

    void readFromByteArray(ByteBuffer in);
    void writeToByteArray(ByteBuffer out);
    int length();
    Long getMirrorKey();
    void setMirrorKey(Long k);
}
