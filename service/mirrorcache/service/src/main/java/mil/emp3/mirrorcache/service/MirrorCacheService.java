package mil.emp3.mirrorcache.service;


import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.DeadObjectException;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.widget.Toast;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import mil.emp3.mirrorcache.api.IMirrorCache;
import mil.emp3.mirrorcache.api.IMirrorCacheListener;
import mil.emp3.mirrorcache.api.MirrorCacheParcelable;

import static mil.emp3.mirrorcache.api.MirrorCache.MAGIC_STRING;

public class MirrorCacheService extends Service {
    private static final String TAG = MirrorCacheService.class.getSimpleName();

    public static final int SERVICE_ID = -2;

    private static final int PID = android.os.Process.myPid();

    private final Map<Integer, IMirrorCacheListener> listeners = new HashMap<>();

    // data store
    private final Map<String, MirrorCacheParcelable> masterCache = new HashMap<>();

    // key generator
    private static long nextKey = Long.MIN_VALUE + 1;

    private Timer timer;

    // to be notified when remote clients disconnect unexpectedly
    private volatile DeathWatcher deathWatcher;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate");

        timer = new Timer("StatusTimer");
        timer.schedule(
            new TimerTask() {
                @Override
                public void run() {
                    try {

                        /*Log.v(TAG, "    keyToGuid.size(): " + keyToGuid.size());
                        for (Map.Entry<Long, String> entry : keyToGuid.entrySet()) {
                            Log.v(TAG, "        [" + entry.getKey() + "] " + entry.getValue());
                        }*/

                        Log.v(TAG, "    " + listeners.size() + " listeners registered:");
                        for (Map.Entry<Integer, IMirrorCacheListener> entry : listeners.entrySet()) {
                            Log.v(TAG, "        [" + entry.getKey() + "] " + entry.getValue().getPackageName());
                        }
                        Log.v(TAG, "    " + masterCache.size() + " items in cache.");

                        /*synchronized (masterCache) {
                            for (Map.Entry<Long, MirrorCacheParcelable> entry : masterCache.entrySet()) {
                                Log.v(TAG, "        [" + entry.getKey() + "] " + entry.getValue());
                            }
                        }*/
                        Log.v(TAG, " ");

                    } catch (Throwable t) {
                        Log.e(TAG, "ERROR: " + t.getMessage(), t);
                    }
                }
            }, 1000L, 5 * 1000L); // every 5 seconds
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand");
        Toast.makeText(this, "Starting service...", Toast.LENGTH_SHORT).show();

        final PendingIntent pendingIntent = PendingIntent.getActivity(
                this, 0, new Intent(this, MirrorCacheServiceActivity.class), 0);

        final Notification notification = new Notification.Builder(this)
                .setContentTitle("MirrorCache Service")
                .setContentText("Running...")
                .setSmallIcon(R.drawable.icon)
                .setContentIntent(pendingIntent)
                .build();

        startForeground(1, notification);

        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "onBind");

        deathWatcher = new DeathWatcher();
        return mBinder;
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy");

        timer.cancel();
        timer = null;

        super.onDestroy();
    }

    /**
     * Mirror Cache Interface Implementation
     */
    private final IMirrorCache.Stub mBinder = new IMirrorCache.Stub() {

        public synchronized void register(IBinder clientDeathListener, String packageName, final int pid, IMirrorCacheListener listener) {
            Log.d(TAG, "register [pid=" + pid + "]");

            deathWatcher.registerVictim(new DeathWatcher.Victim(packageName, clientDeathListener),
                                        new DeathWatcher.DeathCallback() {
                                            @Override
                                            public void binderDied() {
                                                unregister(pid);
                                            }
                                        });
            listeners.put(pid, listener);
        }

        public synchronized void unregister(int pid) {
            Log.d(TAG, "unregister [pid=" + pid + "]");
            listeners.remove(pid);
        }

        public synchronized void nuke() throws RemoteException {
            Log.d(TAG, "nuke");
            stopSelf();
            System.exit(1);
        }

        public synchronized void mirror(int pid, IMirrorCacheListener l) throws RemoteException {

            long t0 = System.currentTimeMillis();

            Log.d(TAG, "mirror " + masterCache.size() + " objects to pid " + pid + " listener " + l);


            if (l == null) {
                Log.d(TAG, "callback is not registered to this process");
                return;
            }

            // implement guids too
            // walk the tree and build out remove client cache via callbacks
            for (MirrorCacheParcelable o : masterCache.values()) {
//                Log.d(TAG, "mirror key " + o.mirrorKey);
//                Log.d(TAG, "before onUpdate...");
                l.onUpdate(SERVICE_ID, o, MAGIC_STRING); // always force local client cache to inflate payload even if its in teh same namespace
//                Log.d(TAG, "after onUpdate.");
            }

            /* perform all the parent child mappiongs */
//            for (MirrorCacheParcelable o : masterCache.values()) {
//                Log.d(TAG, "map children of key " + o.mirrorKey);
//                for (Long childKey : o.contentsKeys)  {
//                    l.onMap(o.mirrorKey, childKey);
//                }
//            }


            long t1 = System.currentTimeMillis();

            Log.i(TAG, "mirrored " + masterCache.size() + " objects to process " + pid + " in " + (t1 - t0) / 1000f + " seconds");
            Log.i(TAG, "mirrored at " + (float) masterCache.size() / ((float) (t1 - t0)) + " objects per millisecond ");
            Log.i(TAG, "mirrored at " + (float) masterCache.size() / ((float) (t1 - t0)) * 1000f + " objects per second ");
            Log.i(TAG, ((float) (t1 - t0)) / ((float) masterCache.size()) + " millisecond latency");

        }

        @Override
        public synchronized void update(int sourcepid, MirrorCacheParcelable o, String parentKey) throws RemoteException {
            // update the master cache with this entry
            masterCache.put(o.mirrorKey, o);

            // update potential parent child mapping
            if (!parentKey.equals(MAGIC_STRING)) {
                MirrorCacheParcelable parent =  masterCache.get(parentKey);
                if (parent != null) {
                    parent.contentsKeys.add(o.mirrorKey);
                }
            }


            // disseminate to connected clients
            update.disseminate(sourcepid, o, parentKey, o.mirrorKey);
        }

        @Override
        public synchronized void delete(int xpid, String key) throws RemoteException {
            masterCache.remove(key);
            delete.disseminate(xpid, null, MAGIC_STRING, key);
        }

        @Override
        public synchronized void map(int sourcepid, String parentKey, String childKey) throws RemoteException {
//            Log.d(TAG, "map");

            MirrorCacheParcelable p = masterCache.get(parentKey);
            if (p == null) {
                Log.w(TAG, "p == null");
                return;
            }
            p.contentsKeys.add(childKey);

            map.disseminate(sourcepid, null, parentKey, childKey);
        }

        @Override
        public synchronized void unmap(int sourcepid, String parentKey, String childKey) throws RemoteException {
//            Log.d(TAG, "unmap");

            MirrorCacheParcelable p = masterCache.get(parentKey);
            if (p == null) {
                Log.w(TAG, "p == null");
                return;
            }
            p.contentsKeys.remove(childKey);

            unmap.disseminate(sourcepid, null, parentKey, childKey);
        }
    };




    private abstract class Disseminator {

        public final void disseminate(int sourcePid, MirrorCacheParcelable p, String parentKey, String childKey) throws RemoteException {

            // Synchronously call back all listening client connection
            for (Iterator<Map.Entry<Integer, IMirrorCacheListener>> iter = listeners.entrySet().iterator(); iter.hasNext(); ) {
                final Map.Entry<Integer, IMirrorCacheListener> entry = iter.next();

                final IMirrorCacheListener listener = entry.getValue();
                if (entry.getValue() == null) {
                    Log.w(TAG, "entry.getValue() == null");
                    continue; // ya never know
                }

                final int destPid = entry.getKey();

                // apply the shunt
                if (sourcePid == destPid) {
//                    Log.d(TAG, "shunt pid " + destPid);
                    continue;
                }

                try {
                    applyFunctionCall(listener, sourcePid, p, parentKey, childKey);

                } catch (DeadObjectException e) {
                    Log.d(TAG, "removing listener from callback list " + destPid);
                    iter.remove();
                }
            }
        }

        protected abstract void applyFunctionCall(IMirrorCacheListener l, int sourcepid, MirrorCacheParcelable p, String parentKey, String childKey) throws DeadObjectException, RemoteException;
    }

    private final Disseminator update = new Disseminator() {
        protected void applyFunctionCall(IMirrorCacheListener l, int sourcepid, MirrorCacheParcelable p, String parentKey, String childKey) throws DeadObjectException, RemoteException {
            l.onUpdate(sourcepid, p, parentKey);
        }
    };

    private final Disseminator delete = new Disseminator() {
        protected void applyFunctionCall(IMirrorCacheListener l, int sourcepid, MirrorCacheParcelable p, String parentKey, String childKey) throws DeadObjectException, RemoteException {
            l.onDelete(sourcepid, childKey);
        }
    };

    private final Disseminator map = new Disseminator() {
        protected void applyFunctionCall(IMirrorCacheListener l, int sourcepid, MirrorCacheParcelable p, String parentKey, String childKey) throws DeadObjectException, RemoteException {
            l.onMap(parentKey, childKey);
        }
    };

    private final Disseminator unmap = new Disseminator() {
        protected void applyFunctionCall(IMirrorCacheListener l, int sourcepid, MirrorCacheParcelable p, String parentKey, String childKey) throws DeadObjectException, RemoteException {
            l.onUnmap(parentKey, childKey);
        }
    };
}
