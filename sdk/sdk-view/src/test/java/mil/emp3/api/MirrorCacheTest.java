package mil.emp3.api;

import junit.framework.TestCase;
import junit.framework.Assert;
/*
import org.mockito.internal.matchers.Contains;

import java.net.URI;

import mil.emp3.api.exceptions.EMP_Exception;
import mil.emp3.mirrorcache.MirrorCacheClient;
import mil.emp3.mirrorcache.MirrorCacheException;

import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mock;
*/

public class MirrorCacheTest extends TestCase {

    // Keep JUnit happy, throw it a bone

    public void testMirrorZero() {
        assert(0 == 0);
    }

    /*
    MirrorCache is not currently supported


    public void testThrowsWhenInvalidProductId() throws EMP_Exception {
        final MirrorCacheClient client = mock(MirrorCacheClient.class);
        MirrorCache mc = new MirrorCache(client);
        mc.connect();

        try {
            mc.subscribe("invalid-id");
            fail();
        } catch (IllegalArgumentException expected) {
            assertThat(expected.getMessage(), new Contains("Invalid productId"));
        }

        try {
            mc.unsubscribe("invalid-id");
            fail();
        } catch (IllegalArgumentException expected) {
            assertThat(expected.getMessage(), new Contains("Invalid productId"));
        }
    }

    public void testThrowsWhenInvalidOverlayName() throws EMP_Exception {
        final MirrorCacheClient client = mock(MirrorCacheClient.class);
        MirrorCache mc = new MirrorCache(client);
        mc.connect();

        Overlay overlay = mock(Overlay.class);

        when(overlay.getName()).thenReturn(" ");
        try {
            mc.addProduct(overlay);
            fail();
        } catch (IllegalArgumentException expected) {
            assertThat(expected.getMessage(), new Contains("Invalid overlayName"));
        }

        when(overlay.getName()).thenReturn(null);
        try {
            mc.addProduct(overlay);
            fail();
        } catch (IllegalArgumentException expected) {
            assertThat(expected.getMessage(), new Contains("Invalid overlayName"));
        }
    }

    public void testInitOnceWhenMultipleConnects() throws EMP_Exception, MirrorCacheException {
        final MirrorCacheClient client = mock(MirrorCacheClient.class);
        final MirrorCache mc = new MirrorCache(client);

        mc.connect();
        mc.connect();
        mc.connect();

        verify(client, times(1)).init();
        verify(client, times(1)).connect();
    }

    public void testShutdownOnceWhenMultipleDisconnects() throws EMP_Exception, MirrorCacheException {
        final MirrorCacheClient client = mock(MirrorCacheClient.class);
        final MirrorCache mc = new MirrorCache(client);

        mc.connect();
        mc.disconnect();
        mc.disconnect();
        mc.disconnect();

        verify(client, times(1)).disconnect();
        verify(client, times(1)).shutdown();
    }

    public void testThrowsWhenNotConnected() throws EMP_Exception {
        final MirrorCacheClient client = mock(MirrorCacheClient.class);
        final MirrorCache mc = new MirrorCache(client);

        try {
            mc.subscribe("product:a");
            fail();
        } catch (IllegalStateException expected) { }

        try {
            mc.unsubscribe("product:a");
            fail();
        } catch (IllegalStateException expected) { }

        try {
            mc.addProduct(mock(Overlay.class));
            fail();
        } catch (IllegalStateException expected) { }

        try {
            mc.removeProduct(mock(Overlay.class), false);
            fail();
        } catch (IllegalStateException expected) { }

        try {
            mc.getProductIds();
            fail();
        } catch (IllegalStateException expected) { }
    }

    public void testThrowsWhenNullArgument() throws EMP_Exception, MirrorCacheException {
        try {
            new MirrorCache((URI) null);
            fail();
        } catch (NullPointerException expected) { }

        try {
            new MirrorCache((MirrorCacheClient) null);
            fail();
        } catch (NullPointerException expected) { }


        final MirrorCacheClient client = mock(MirrorCacheClient.class);
        MirrorCache mc = new MirrorCache(client);
        mc.connect();

        try {
            mc.addProduct(null);
            fail();
        } catch (NullPointerException expected) { }

        try {
            mc.removeProduct(null, false);
            fail();
        } catch (NullPointerException expected) { }

        try {
            mc.subscribe(null);
            fail();
        } catch (NullPointerException expected) { }

        try {
            mc.unsubscribe(null);
            fail();
        } catch (NullPointerException expected) { }
    }
    */
}