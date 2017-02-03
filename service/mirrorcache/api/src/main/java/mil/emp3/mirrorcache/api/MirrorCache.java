package mil.emp3.mirrorcache.api;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Binder;
import android.os.IBinder;
import android.os.Looper;
import android.os.RemoteException;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Operate on a HashMap cache synchronously across android dalvik bounds.
 * This implementation provides a synchronous and blocking object
 * cache implemntation that mirrors a hashmap in each dalvik instance via AIDL.
 */
public class MirrorCache {
    private static final String TAG = MirrorCache.class.getSimpleName();

    static final private MirrorCache instance = new MirrorCache();

    enum MirrorCacheState {

        /**
         * Initial mirror cache connection and mirroring state.
         * This state occurs at startup or upon IORemoteException.
         */
        STATE_0_NULL,

        /**
         * Mirror cache connection and mirroring state.
         * In this state the service bind is called periodically untill the desirec service is obtainable..
         * This state occurs at startup or upon IORemoteException when the state machine restarts the connection establishment process.
         */
        STATE_1_INIT_WAIT_TO_BIND,

        /**
         * Mirror cache connection and mirroring state.
         * In this state the we wait untill the Android UI thread onServiceConnect is called.
         */
        STATE_2_BOUND_TO_SERVICE_WAIT_TO_CONNECT,

        /**
         * Mirror cache connection and mirroring state.
         * In this state the we wait untill the cache in the android service is fully mirrorred locally.
         */
        STATE_3_CONNECTED_TO_SERVICE_WAIT_TO_MIRROR,

        /**
         * Mirror cache connection and mirroring state.
         * In this state the we are operational.
         */
        STATE_4_MIRRORED,
        ;

    }

    // time to sleep in milliseconds when attempting to bring up IPC

    /**
     * The amount of time to sleep when polling for either
     * bind connection within connection establishment state machine or
     * when calling vi API signature and still waiting for service cache to be mirrorred locally.
     * Eager polls 10 times a second.
     */
    public static final int POLL_INTERVAL_EAGER = 100;

    /**
     * The amount of time to sleep when polling for either
     * bind connection within connection establishment state machine or
     * when calling vi API signature and still waiting for service cache to be mirrorred locally.
     * Laid back polls once a second.
     */
    public static final int POLL_INTERVAL_LAID_BACK = 1000;

    /**
     * The amount of time to sleep when polling for either
     * bind connection within connection establishment state machine or
     * when calling vi API signature and still waiting for service cache to be mirrorred locally
     * Lazy back polls every 10 seconds.
     */
    public static final int POLL_INTERVAL_LAZY = 10000;

    /**
     * Reserved hash map key.
     * This is used by specific application developer extensions to MirrorCache
     * to establish a custom hierarchy of data.
     * This key acts as the custom hierarchy root node.
     * This is the only key that is hardcoded and not automatically generated for the developer by {@link IMirrorCache#genKey()}
     */
    public static final long ROOT_KEY = Long.MIN_VALUE;

    /**
     * represents flag for invoking shunt optimization
     */
    private static final int SHUNT_PID = -1;

    private static final int PID = android.os.Process.myPid();

    private int pollInterval = POLL_INTERVAL_LAID_BACK;

    private Context appContext;
    private String mcServicePackageName;
    private String mcServiceClassName;
    private Intent intent;

    private volatile boolean mirrored = false;

    // mirrored cache synced with MirrorCacheService masterCache
    protected static final HashMap<Long, MirrorCacheParcelable> mirrorCache = new HashMap<Long, MirrorCacheParcelable>();

    /**
     * Quick cross reference to convert keys to GUIDs
     */
    protected static final HashMap<Long, String> keyToGuid = new HashMap<Long, String>();

    /**
     * Quick cross reference to convert GUIDs to keys
     */
    protected static final HashMap<String, Long> guidToKey = new HashMap<String, Long>();


    // list of state change listeners this is traversed in MirrorCache.setState()
    private static final ArrayList<IMirrorCacheStateChangeListener> mcStateListeners= new ArrayList<IMirrorCacheStateChangeListener>();

    private static WaitToBindThread waitToBind = null;

    private static IMirrorCache mirrorCacheService = null;

    private MirrorCacheState state = MirrorCacheState.STATE_0_NULL;

    /**
     * Used to prevent the state from prematurely changing
     * while waiting to bind to the service.
     */
    private static ReentrantLock bindLock = new ReentrantLock();

    /**
     * Android Binder service connection
     */
    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.d(TAG, "onServiceDisconnected thread " + Thread.currentThread().getId());

            changeState(MirrorCacheState.STATE_0_NULL);
            changeState(MirrorCacheState.STATE_1_INIT_WAIT_TO_BIND);
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d(TAG, "onServiceConnected thread " + Thread.currentThread().getId());

            try {
                bindLock.lock();

                mirrorCacheService = IMirrorCache.Stub.asInterface(service);

                // tell the MirrorCache service to distribute updates to this listener
                try {
                    mirrorCacheService.register(new Binder(), appContext.getPackageName(), PID, mirrorCacheListener);

                } catch (RemoteException e) {
                    Log.e(TAG, "ERROR: " + e.getMessage(), e);
                    changeState(MirrorCacheState.STATE_0_NULL);
                    changeState(MirrorCacheState.STATE_1_INIT_WAIT_TO_BIND);

                } finally {
                    changeState(MirrorCacheState.STATE_3_CONNECTED_TO_SERVICE_WAIT_TO_MIRROR);
                }

            } finally {
                bindLock.unlock();
            }
        }
    };

    private final IMirrorCacheListener.Stub mirrorCacheListener = new IMirrorCacheListener.Stub() {

        @Override
        public String getPackageName() {
            return appContext.getPackageName();
        }
        @Override
        public void onUpdate(int pid, MirrorCacheParcelable in, long pk, String guid) {
            //Log.d(TAG, "onUpdate key " + in.mirrorKey + " parent " + pk + " " + ((guid==null)?"null":guid));

            MirrorCacheParcelable p = in;

            //if (pid == MirrorCacheService.SERVICE_ID) {
            //Log.d(TAG, "update via server mirror");
            //}
            //else if (pid == SHUNT_PID){
            //Log.d(TAG, "update via self shunt");
            //}
            //else {
            //Log.d(TAG, "update via client pid " + pid);
            //}

            if (pid != SHUNT_PID) {

                // 'in' is MirrorcacheParcelable instantiated by AIDL from pool and populated with payload bytes
                // get locally cached MirrorcacheParcelable
                p = mirrorCache.get(in.mirrorKey);
                if (p == null) {
                    //Log.d(TAG, "add this newly deserialized object to the local cache");
                    p = in;
                }
                //Log.d(TAG, "inflate over the existing entry to not to peturb the listenners");
                in.payloadByteBuffer.position(0);
//                    Log.d(TAG, "reset byte buffer");

                // FIXME this assumes the key will never be overwritten with a payload of another class type
                if (p.payloadObject == null) {
                    p.payloadObject = MirrorableLoader.newMirrorable(in.payloadClassName);
                }

                p.payloadObject.readFromByteArray(in.payloadByteBuffer);
                p.payloadObject.setMirrorKey(in.mirrorKey);
            }


            mirrorCache.put(p.mirrorKey, p);
            if (pk != -1) {
                onMap(pk, p.mirrorKey);
            }
            if (guid != null) {
                guidToKey.put(guid, p.mirrorKey);
                keyToGuid.put(p.mirrorKey, guid);
            }

            // notify listenners
//                Log.d(TAG, "onUpdate notify parcelable listeners");
            if (pid != SHUNT_PID) {
                p.onUpdate(); //notify individual instance listeners
                MirrorCache.this.onUpdate(p.payloadObject);
            }
        }

        @Override
        public void onDelete(int pid, long key) {
//            Log.d(TAG, "onDelete key " + key);
            final MirrorCacheParcelable p = mirrorCache.remove(key);
            if (p != null) {

                if (pid != SHUNT_PID) {
                    p.onDelete(); //notify individual instance listeners
                    MirrorCache.this.onDelete(p.payloadObject);
                }

                p.payloadObject = null;
            }
        }

        @Override
        public void onNoop() {
        }

        @Override
        public void onMap(long parentKey, long childKey) {
            MirrorCacheParcelable p = mirrorCache.get(parentKey);
            if (p == null) {
                Log.e(TAG, "mirror cache out of sync");
                return;
            }
            // make sure this operates on the long value and not the object
            Log.d(TAG, "map " + parentKey + " to " + childKey);
            if (p.contentsKeys.contains(childKey) == false) {
                p.contentsKeys.add(childKey);
            }
        }

        @Override
        public void onUnmap(long parentKey, long childKey) {
            MirrorCacheParcelable p = mirrorCache.get(parentKey);
            if (p == null) {
                Log.e(TAG, "mirror cache out of sync");
                return;
            }
            // make sure this operates on the long value and not the object
            if (p.contentsKeys.contains(childKey) == false) {
                p.contentsKeys.remove(childKey);
            }
        }
    };

    private MirrorCache() { }
    static public MirrorCache getInstance() {
        return instance;
    }

    /**
     * Call this method once from the Application onCreate method
     * This forces the developer to pass the Application context to eliminate
     * Android Context scope weirdities.
     *
     * @param c pass the context of the calling Android application component (i.e., service, activity)
     * @param packageName java package namespace where the service class can be located
     * @param className the name of the specific implmenting mirror cache service
     * @param pollInterval time to sleep in milliseconds when attempting to bring up IPC or use {@link MirrorCache#POLL_INTERVAL_LAID_BACK}, {@link MirrorCache#POLL_INTERVAL_LAZY},{@link MirrorCache#POLL_INTERVAL_EAGER},
     */
    public void onCreate(Context c, String packageName, String className, int pollInterval) {
        Log.d(TAG, "onCreate (" + packageName + " " + className + ")");

        /* start state transition initialization sequence */
        if (state == MirrorCacheState.STATE_0_NULL) {
            Log.d(TAG, "Starting the state machine.");
            isCurrentThreadUIThread();
            /*
             * sometimes, at least 5 years ago in Gingerbread and before, the Activity Context was
             * fleeting but the Application Context is stubborn
             */
            appContext = c.getApplicationContext();
            mcServicePackageName = packageName;
            mcServiceClassName = className;
            intent = new Intent();
            intent.setClassName(mcServicePackageName, mcServiceClassName);
            changeState(MirrorCacheState.STATE_1_INIT_WAIT_TO_BIND);

        } else {
            Log.d(TAG, "Object already in use and state machine underway at state " + state);
        }
    }

    public void onCreate(Context c) {
        this.onCreate(c, "mil.emp3.mirrorcache.service", "mil.emp3.mirrorcache.service.MirrorCacheService", POLL_INTERVAL_LAID_BACK);
    }

    /**
     * Call this method once from the Application onCreate method
     * This forces the developer to pass the Application context to eliminate
     * Android Context scope weirdities.
     *
     * @param c pass the context of the calling Android application component (i.e., service, activity)
     * @param packageName java package namespace where the service class can be located
     * @param className the name of the specific implmenting mirror cache service
     */
    public void onCreate(Context c, String packageName, String className) {
        onCreate(c, packageName, className, POLL_INTERVAL_LAID_BACK);
    }

    /**
     * Call this method from the application lifecycle's peer method onDestroy()
     */
    public void onDestroy() {
        Log.d(TAG, "onDestroy");

        // clean up here
        changeState(MirrorCacheState.STATE_0_NULL);

        // clear all static members, otherwise listeners that are hanging around will cause an exception
        mirrorCache.clear();
        keyToGuid.clear();
        guidToKey.clear();
        mcStateListeners.clear();

        // waitToBind is handled by setting state to STATE_0_NULL
    }

    /**
     * Get all the mirrorable objects stored in the mirror cache
     * @return array list of mirrorable objects
     */
    public ArrayList<IMirrorable> values() {
        waitForMirror();

        final ArrayList<IMirrorable> al = new ArrayList<IMirrorable>();
        for (MirrorCacheParcelable p : mirrorCache.values()) {
            al.add(p.payloadObject);
        }
        return al;
    }

    /**
     * Test method for excercising IPC bounds without actually transferring any objects.
     * This is meant to establish the IPC and function call overhead>
     * It is made deprecated since the end developer should not use it.
     */
    @Deprecated
    public void noop() {
        waitForMirror();

        try {
            mirrorCacheService.noop(PID);
        } catch (RemoteException e) {
            Log.e(TAG, "ERROR: " + e.getMessage(), e);

            changeState(MirrorCacheState.STATE_0_NULL);
            changeState(MirrorCacheState.STATE_1_INIT_WAIT_TO_BIND);
            noop();
        }
    }

    /**
     * DO NOT PRESS this shiny candy like button
     *
     * Tell the Mirror Cache service to blow itself away.
     * This represents a total Mirrorcache fault condition.
     */
    @Deprecated
    public void nuke() {
        try {
            mirrorCacheService.nuke();
        } catch (RemoteException e) {
        }
    }

    /**
     * Obtain the mirrorable object denoted by the unique id string
     * @param guid globally unique ID for obtaining the desired object
     * @return the desired object identified by guid
     */
    public IMirrorable get(String guid) {
        waitForMirror();

        Long key = guidToKey.get(guid);
        if (key == null) return null;

        MirrorCacheParcelable mb = mirrorCache.get(key);
        if (mb == null) return null;

        // no need to inflate object unless its actually needed
        // this may slow down first render loop
        //if (mb.payloadObject == null) mb.inflate();
        return mb.payloadObject;
    }

    /**
     * Obtain the mirrorable object denoted by the mirror cache provided key
     * @param key globally unique mirror cache generated key for obtaining the desired object
     * @return the desired object identified by key
     */
    public IMirrorable get(long key) {
        waitForMirror();

        MirrorCacheParcelable mb = mirrorCache.get(key);
        if (mb == null) return null;

        // no need to inflate object unless its actually needed
        // this may slow down first render loop
        //if (mb.payloadObject == null) mb.inflate();
        return mb.payloadObject;
    }

    /**
     * Obtain a list of mirrorable objects that have been mapped as children
     * @param o the parent mirrorable object
     * @return a list of child mirrorable objects
     */
    public ArrayList<IMirrorable> getChildren(IMirrorable o) {
        waitForMirror();

        if (o == null) return null;
        Long mirrorKey = o.getMirrorKey();
        if (mirrorKey == null) return null;
        MirrorCacheParcelable p = mirrorCache.get(mirrorKey);
        if (p == null) return null;
        if (p.contentsKeys == null) return null;
        //if (p.contentsKeys.size() == 0) return null;

        ArrayList<IMirrorable> al = new ArrayList<IMirrorable>();
        for (Long nextKey : p.contentsKeys) {
            //Log.d(TAG, "load children from key " + nextKey);
            MirrorCacheParcelable mcp = mirrorCache.get(nextKey);
            if (mcp != null) {
                IMirrorable m = mcp.payloadObject;
                if (m != null) {
                    //Log.d(TAG, "add children from Mirrorable " + m.getMirrorKey());
                    al.add(m);
                }
            }
        }
        return al;
    }

    /**
     * Obtain a list of mirror cache generated object keys that represent the children to the parent mirrorable object
     * @param o the parent mirrorable object
     * @return a list of mirror cache generated object keys representing the children
     */
    public ArrayList<Long> getChildKeys(IMirrorable o) {
        waitForMirror();

        if (o == null) return null;

        Long mirrorKey = o.getMirrorKey();
        if (mirrorKey == null) return null;

        MirrorCacheParcelable p = mirrorCache.get(mirrorKey);
        if (p == null) return null;

        return p.contentsKeys;
    }

    /**
     * non atomic update has potential for race conditions when establishing new keys, guids and parent child mapping
     * @see MirrorCache#update(IMirrorable, IMirrorable, String)
     *
     */
    public void update(IMirrorable m) {
        update(m, null, null);
    }

    /**
     * Atomic update
     *
     * @param m mirrorable object to be updated
     * @param pm updated objects parent
     * @param guid updated objects globally unique id
     */
    public void update(IMirrorable m, IMirrorable pm, String guid) {
        waitForMirror();

        Log.d(TAG, "update");
        Log.d(TAG, "guid: " + guid);

        if (m == null) {
            Log.e(TAG, "attempting to cache null Mirrorable object");
            return;
        }
        try {
            if (m.getMirrorKey() == null) {
//            Log.e(TAG, "update with assigned key...");
                m.setMirrorKey(mirrorCacheService.genKey());  // This is the auto key gen capability
//            Log.e(TAG, "update with server generated key " + m.getMirrorKey());
            } else {
//            Log.e(TAG, "update with existing key " + m.getMirrorKey());
            }

            long key = m.getMirrorKey();

            MirrorCacheParcelable p = mirrorCache.get(key);
            if (p == null) {
                p = new MirrorCacheParcelable();
            }
            p.mirrorKey = key;
            p.payloadObject = m;

            long pk = -1;
            if (pm != null) pk = pm.getMirrorKey();
            p.payloadValid = false; // invalidate byte buffer to force parcelable to serialize it
            mirrorCacheService.update(PID, p, pk, guid);
            // apply the shunt, purposely bypass MirrorCacheService callback
            mirrorCacheListener.onUpdate(SHUNT_PID, p, pk, guid);

        } catch (RemoteException e) {
            Log.e(TAG, "ERROR: " + e.getMessage(), e);

            changeState(MirrorCacheState.STATE_0_NULL);
            changeState(MirrorCacheState.STATE_1_INIT_WAIT_TO_BIND);
            update(m, pm, guid);
        }
    }

    /**
     * Establish the mirrorred parent child mirrorable object relationship.
     * @see MirrorCache#unmap(IMirrorable, IMirrorable)
     *
     * @param pm parent mirrorable object
     * @param cm child mirrorable object
     */
    public void map(IMirrorable pm, IMirrorable cm) {
        waitForMirror();

//        Log.d(TAG, "map");
        long pk = pm.getMirrorKey();
        long ck = cm.getMirrorKey();
        MirrorCacheParcelable pp = mirrorCache.get(pk);
        MirrorCacheParcelable cp = mirrorCache.get(ck);

        if ((pp == null) || (cp == null)) {
            Log.e(TAG, "map() cache is effed");
        }

        try {
            mirrorCacheService.map(PID, pk, ck);
            // apply the shunt, purposely bypass MirrorCacheService callback
            mirrorCacheListener.onMap(pk, ck);
        } catch (RemoteException e) {
            e.printStackTrace();
            changeState(MirrorCacheState.STATE_0_NULL);
            changeState(MirrorCacheState.STATE_1_INIT_WAIT_TO_BIND);
            map(pm, cm);
        }
    }

    /**
     * remove the previously established mirrorred parent child mirrorable object relationship
     * @param pm parent mirrorable object
     * @param cm child mirrorable object
     */
    public void unmap(IMirrorable pm, IMirrorable cm) {
        waitForMirror();

//        Log.d(TAG, "map");
        long pk = pm.getMirrorKey();
        long ck = cm.getMirrorKey();
        MirrorCacheParcelable pp = mirrorCache.get(pk);
        MirrorCacheParcelable cp = mirrorCache.get(ck);

        if ((pp == null) || (cp == null)) {
            Log.e(TAG, "map() cache is effed");
        }

        try {
            mirrorCacheService.unmap(PID, pk, ck);
            // apply the shunt, purposely bypass MirrorCacheService callback
            mirrorCacheListener.onUnmap(pk, ck);
        } catch (RemoteException e) {
            e.printStackTrace();
            changeState(MirrorCacheState.STATE_0_NULL);
            changeState(MirrorCacheState.STATE_1_INIT_WAIT_TO_BIND);
            unmap(pm, cm);
        }
    }

    /**
     * Remove the mirrorable object from the mirror cache
     * @param key the mirror cache generated key representing the object to be removed
     */
    private void doDelete(long key) {
        waitForMirror();

        try {
            mirrorCacheService.delete(PID, key);
            mirrorCacheListener.onDelete(PID, key);

        } catch (RemoteException e) {
            Log.e(TAG, "ERROR: " + e.getMessage(), e);
            changeState(MirrorCacheState.STATE_0_NULL);
            changeState(MirrorCacheState.STATE_1_INIT_WAIT_TO_BIND);

            doDelete(key);
        }
    }

    /**
     * Remove the mirrorable object from the mirror cache
     *
     * @param guid Globally unique id representing the mirrorable object to be deleted
     */
    public void delete(String guid) {
        waitForMirror();

        final Long key = guidToKey.get(guid);
        if (key != null) {
            doDelete(key);
        }
    }

    /**
     * Debugging method to write the contents of the mirror cache to logcat.
     * Marked as Deprecated since it is not meant for end developers
     */
    @Deprecated
    public void dump() {
        waitForMirror();

        Log.d(TAG, "dump");
        for (Long key : mirrorCache.keySet()) {
            Log.d(TAG, "dump key " + key);

            MirrorCacheParcelable mb = null;
            mb = mirrorCache.get(key);
            Log.d(TAG, "mb key " + mb.mirrorKey);

            IMirrorable m = null;
            m = get(key);
            Log.d(TAG, "dump val " + mb.payloadObject);
        }
    }

    /**
     * Add a listenning callback to the desired mirrorable object.
     * This callback is called everytime a mirrorable object is updated.
     * @see MirrorCache#removeListener(IMirrorable, IMirrorableListener)
     *
     * @param m mirroable object to listen to for updates
     * @param l listenning callback to be added
     */
    public void addListener(IMirrorable m, IMirrorableListener l) {
        if (m == null || m.getMirrorKey() == null) {
            Log.e(TAG, "m == null || m.getMirrorKey() == null");
            return;
        }
        mirrorCache.get(m.getMirrorKey()).addListener(l);
    }

    /**
     * Remove the listener from the mirroable object
     * @see MirrorCache#addListener(IMirrorable, IMirrorableListener)
     *
     * @param m object being listenned to for updates
     * @param l litsening callback to be removed
     */
    public void removeListener(IMirrorable m, IMirrorableListener l) {
        mirrorCache.get(m.getMirrorKey()).removeListener(l);
    }

    /**
     * Register the callback for mirror cache state changes
     * @see MirrorCache#removeStateChangeListener(IMirrorCacheStateChangeListener)
     *
     * @param l implementation of state change listener to add
     */
    public void addStateChangeListener(IMirrorCacheStateChangeListener l) {
        Log.d(TAG, "addStateChangeListener");
        mcStateListeners.add(l);
    }

    /**
     * Remove the callback for mirror cache state changes
     * @see MirrorCache#addStateChangeListener(IMirrorCacheStateChangeListener)
     *
     * @param l implementation of state change listener to delete
     */
    public void removeStateChangeListener(IMirrorCacheStateChangeListener l) {
        Log.d(TAG, "removeStateChangeListener");
        mcStateListeners.remove(l);
    }


    /*******************************************************************************
     * IPC AIDL/Binder connection Maintenance Section
     *******************************************************************************/

    /**
     * This method simply blocks the calling thread until
     * the IPC is up and the cache has been completely
     * copied to the local mirror
     */
    private void waitForMirror() {
        //while (mirrored.get() == false) {
        while (state != MirrorCacheState.STATE_4_MIRRORED && state != MirrorCacheState.STATE_0_NULL) {
            Log.d(TAG, "[" + PID + "] [" + state + "] waiting to finish mirroring..");
            try {
                Thread.sleep(pollInterval);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * State machine for mirror cache connection management and mirroring subsystem.
     * This implementation is meant to be a thurough and robust mechanism to capture
     * every possible state transition during th einitialization sequence.
     *
     * Currently only expected state transitions have been implemnted.
     * Unexpected transitions result in a logcat entry.
     * Non transitions (same state) are ignored.
     *
     * @param newState change from current state to this state
     */
    private void changeState(MirrorCacheState newState) {
        Log.d(TAG, "change state from " + state + " to " + newState + " in thread " + Thread.currentThread().getId());

        switch (state) {
            case STATE_0_NULL: {
                switch (newState) {
                    case STATE_0_NULL: {
                        break;
                    }

                    case STATE_1_INIT_WAIT_TO_BIND: {

                        /* non UI thread worker to establish/start android service */
                        if (waitToBind == null) waitToBind = new WaitToBindThread(this);
                        waitToBind.start();

                        setState(newState);
                        break;
                    }

                    default: {
                        Log.e(TAG, "state transition fault");
                        setState(MirrorCacheState.STATE_0_NULL);
                        setState(MirrorCacheState.STATE_1_INIT_WAIT_TO_BIND);
                    }
                }
                break;
            }

            case STATE_1_INIT_WAIT_TO_BIND: {
                switch (newState) {
                    case STATE_0_NULL: {
                        waitToBind.finish(); // kill the waitToBind thread
                        waitToBind = null;

                        // reset the local cache
                        mirrored = false;

                        setState(newState);
                        break;
                    }

                    case STATE_2_BOUND_TO_SERVICE_WAIT_TO_CONNECT: {
                        waitToBind.finish();
                        waitToBind = null;
                        /* waiting to connect already occurring on UI thread in background */
                        setState(newState);
                        break;
                    }

                    default: {
                        Log.e(TAG, "state transition fault");
                        setState(MirrorCacheState.STATE_0_NULL);
                        setState(MirrorCacheState.STATE_1_INIT_WAIT_TO_BIND);
                    }
                }
                break;
            }

            case STATE_2_BOUND_TO_SERVICE_WAIT_TO_CONNECT: {
                switch (newState) {
                    case STATE_0_NULL: {
                        appContext.unbindService(mConnection); // unbind the service

                        mirrored = false; // reset the local cache

                        setState(newState);
                        break;
                    }

                    case STATE_3_CONNECTED_TO_SERVICE_WAIT_TO_MIRROR: {

                    /* non UI thread worker to perform background mirroring */
                        new Thread() {
                            @Override
                            public void run() {
                                try {
                                    Log.d(TAG, guidToKey.entrySet().size() + " GUID entries");
                                    Log.d(TAG, "freeing all references in the mirror cache");
                                    // remnove all listening references too!
                                    for (MirrorCacheParcelable m : mirrorCache.values()) {
                                        m.removeListeners();
                                    }
                                    mirrorCache.clear();
                                    guidToKey.clear();
                                    keyToGuid.clear();
                                    Log.d(TAG, guidToKey.entrySet().size() + " GUID entries");
                                    Log.d(TAG, "calling mirror method");
                                    MirrorCache.mirrorCacheService.mirror(MirrorCache.PID, MirrorCache.this.mirrorCacheListener);
                                    Log.d(TAG, "mirroring complete");

                                } catch (RemoteException e) {
                                    Log.e(TAG, "ERROR: " + e.getMessage(), e);

                                    MirrorCache.this.changeState(MirrorCacheState.STATE_0_NULL);
                                    MirrorCache.this.changeState(MirrorCacheState.STATE_1_INIT_WAIT_TO_BIND);
                                }
                                MirrorCache.this.changeState(MirrorCacheState.STATE_4_MIRRORED);
                            }
                        }.start();

                        setState(newState);
                        break;
                    }

                    default: {
                        Log.e(TAG, "state transition fault");
                        setState(MirrorCacheState.STATE_0_NULL);
                        setState(MirrorCacheState.STATE_1_INIT_WAIT_TO_BIND);
                    }
                }
                break;
            }

            case STATE_3_CONNECTED_TO_SERVICE_WAIT_TO_MIRROR: {
                switch (newState) {
                    case STATE_0_NULL: {
                        // unregister the listener
                        if (mirrorCacheService != null) {
                            try {
                                mirrorCacheService.unregister(PID);
                            } catch (RemoteException e) {
                                e.printStackTrace();
                            }
                        }
                        // unbind the service
                        appContext.unbindService(mConnection);

                        // reset the local cache
                        mirrored = false;

                        setState(newState);
                        break;
                    }

                    // cant kill this background thread WTF so we hack here
                    case STATE_3_CONNECTED_TO_SERVICE_WAIT_TO_MIRROR: {
                        break;
                    }

                    case STATE_4_MIRRORED: {
                        mirrored = true;
                        setState(newState);
                        break;
                    }

                    default: {
                        Log.e(TAG, "state transition fault");
                        setState(MirrorCacheState.STATE_0_NULL);
                        setState(MirrorCacheState.STATE_1_INIT_WAIT_TO_BIND);
                    }
                }
                break;
            }

            case STATE_4_MIRRORED: {
                switch (newState) {
                    case STATE_0_NULL: {
                        // unregister the listener
                        if (mirrorCacheService != null) {
                            try {
                                mirrorCacheService.unregister(PID);
                            } catch (RemoteException e) {
                                Log.e(TAG, "ERROR: " + e.getMessage(), e);
                            }
                        }
                        // unbind the service
                        appContext.unbindService(mConnection);

                        // not mirrored
                        mirrored = false;

                        setState(newState);
                        break;
                    }

                    case STATE_4_MIRRORED: {
                        break;
                    }

                    default: {
                        Log.e(TAG, "state transition fault");
                        setState(MirrorCacheState.STATE_0_NULL);
                        setState(MirrorCacheState.STATE_1_INIT_WAIT_TO_BIND);
                    }
                }
                break;
            }

            default: {
                Log.e(TAG, "state transition fault");
                setState(MirrorCacheState.STATE_0_NULL);
                setState(MirrorCacheState.STATE_1_INIT_WAIT_TO_BIND);
            }
        }
    }

    /**
     * make sure to inform listener when state changes
     * @param newState
     */
    private void setState(MirrorCacheState newState) {
        Log.d(TAG, "setState (" + newState + ")");

        state = newState;
        onStateChanged();
    }

    /**
     * Disseminate state change notifications
     */
    private void onStateChanged() {
        Log.d(TAG, "onStateChanged");

        if (state == MirrorCacheState.STATE_4_MIRRORED) {
            if (mcStateListeners != null) {
                for (IMirrorCacheStateChangeListener l : mcStateListeners) {
                    if (l != null) {
                        l.onMirrorred();
                    }
                }
            }
        }
    }

    private void onUpdate(IMirrorable im) {
        Log.d(TAG, "onUpdate");

        if (mcStateListeners != null) {
            for (IMirrorCacheStateChangeListener l : mcStateListeners) {
                if (l != null) {
                    l.onUpdate(im);
                }
            }
        }
    }

    private void onDelete(IMirrorable im) {
        Log.d(TAG, "onDelete");

        if (mcStateListeners != null) {
            for (IMirrorCacheStateChangeListener l : mcStateListeners) {
                if (l != null) {
                    l.onDelete(im);
                }
            }
        }
    }

    private void isCurrentThreadUIThread() {
        Log.d(TAG, "_isCurrentThreadUIThread: " + (Thread.currentThread().getId() == Looper.getMainLooper().getThread().getId()));
    }

    /**
     * Waiting to bind thread
     * this implements a safely interruptable thread that can be killed on demand
     */
    private static class WaitToBindThread extends Thread {
        private final String TAG = WaitToBindThread.class.getSimpleName() + " " + WaitToBindThread.this.getId();
        private volatile boolean finished;

        final private MirrorCache mirrorCache;
        private WaitToBindThread(MirrorCache mirrorCache) {
            this.mirrorCache = mirrorCache;
        }

        private void finish() {
            Log.d(TAG, "Force stopping wait to bind thread. From thread " + Thread.currentThread().getId());
            finished = true;
            this.interrupt();
        }

        @Override
        public void run() {
            Log.d(TAG, "Waiting to bind Thread starting... " + Thread.currentThread().getId());

            finished = false;
            while (true) {
                // Please leave these sync blocks
                //synchronized (state) {
                if (finished) break;

                Log.d(TAG, "Waiting to bind service... " + Thread.currentThread().getId());

                if (mirrorCache.intent != null) {
                    boolean bindResult;

                    //if (inBindCall) {
                        //Log.w(TAG, "BAILING OUT. already in bind call in a left over waitToBind thread!!!");
                        //finish();
                        //break;
                    //}

                    try {
                        bindLock.lock();

                        //inBindCall = true;
                        Log.d(TAG, "Binding service..");
                        bindResult = mirrorCache.appContext.bindService(mirrorCache.intent, mirrorCache.mConnection, 0);//Context.BIND_AUTO_CREATE);
                        //inBindCall = false;

                        Log.d(TAG, "bindResult: " + bindResult);
                        if (bindResult) {
                            Log.d(TAG, "Bound service. " + Thread.currentThread().getId());
                            mirrorCache.changeState(MirrorCacheState.STATE_2_BOUND_TO_SERVICE_WAIT_TO_CONNECT);
                            finish();

                        } else {
                            Log.e(TAG, "ERROR: Unable to bind to service");
                        }

                    } finally {
                        bindLock.unlock();
                    }

                } else {
                    Log.w(TAG, "onCreate not yet called; intent not initialized.");
                }
                //}

                // Please leave these sync blocks
                //synchronized (state) {
                if (finished) break;
                //}

                // live to die another day
                try {
                    Log.d(TAG, "Waiting " + mirrorCache.pollInterval + " ms to bind service..." + Thread.currentThread().getId());
                    sleep(mirrorCache.pollInterval);
                } catch (InterruptedException e) {
                    Log.d(TAG, "INTERRUPTED: Waiting to bind thread interrupted..." + Thread.currentThread().getId());
                    // DO NOT synchronize this just overwrite ASAP
                    finished = true;
                }
                Log.d(TAG, "Waiting to bind thread exiting." + Thread.currentThread().getId());
            }
        }
    }
}
