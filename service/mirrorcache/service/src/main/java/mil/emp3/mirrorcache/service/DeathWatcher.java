package mil.emp3.mirrorcache.service;

import android.os.IBinder;
import android.os.RemoteException;
import android.util.ArrayMap;
import android.util.Log;

import java.util.Map;

/**
 * This class allows a service to be notified via callback when
 * a remote client's process terminates unexpectedly.
 */
public class DeathWatcher {
    static final private String TAG = DeathWatcher.class.getSimpleName();

    final private Map<String, IBinder.DeathRecipient> mCallbacks;

    public DeathWatcher() {
        this.mCallbacks = new ArrayMap<>();
    }

    public boolean registerVictim(Victim victim, DeathCallback callback) {
        Log.d(TAG, "registerVictim [" + victim + "]");
        synchronized (mCallbacks) {
            try {
                if (!mCallbacks.containsKey(victim.packageName)) {

                    final DeathCallbackFacilitator callbackFacilitator = new DeathCallbackFacilitator(victim, callback);
                    mCallbacks.put(victim.packageName, callbackFacilitator);

                    victim.binder.linkToDeath(callbackFacilitator, 0);
                }
                return true;

            } catch (RemoteException e) {
                Log.e(TAG, "ERROR: " + e.getMessage(), e);
                return false;
            }
        }
    }

    /**
     *  To be called only from thread-safe functions.
     */
    private void clientDeath(String packageName) {
        Log.d(TAG, "clientDeath [" + packageName + "]");

        try {
            mCallbacks.remove(packageName);
        } catch (Exception e) {
            Log.e(TAG, "ERROR: " + e.getMessage(), e);
        }
    }


    // -+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+- //
    // -+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+- //


    /**
     * This internal class adds additional housekeeping tasks to DeathCallback
     * instances when a clientDeath has been detected.
     */
    private class DeathCallbackFacilitator implements IBinder.DeathRecipient {
        final private Victim victim;
        final private DeathCallback callback;

        private DeathCallbackFacilitator(Victim victim, DeathCallback callback) {
            this.victim   = victim;
            this.callback = callback;
        }

        @Override
        public void binderDied() {
            synchronized (mCallbacks) {
                victim.binder.unlinkToDeath(this, 0);
                clientDeath(victim.packageName);
                callback.binderDied();
            }
        }
    }


    // -+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+- //
    // -+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+- //


    /**
     * Interface used to notify via callback that a client process was terminated.
     */
    interface DeathCallback extends IBinder.DeathRecipient {
        @Override void binderDied();
    }


    // -+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+- //
    // -+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+- //


    /**
     * This class encapsulates the details of a remote client.
     */
    static class Victim {
        final private String packageName;
        final private IBinder binder;

        public Victim(String packageName, IBinder token) {
            this.packageName = packageName;
            this.binder = token;
        }

        @Override
        public String toString() {
            return "Victim{" +
                    "binder=" + binder +
                    ", packageName='" + packageName + '\'' +
                    '}';
        }
    }
}
