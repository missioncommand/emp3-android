/**
 * Please read documentation in gradle project emp3-android-sdk, java package mil.emp3.core.
 * <p>
 * Only classes of interest, in this project, for an application developer are MapView and MapFragment. Following snippets from a layout file and
 * java code show the most basic way of starting a map. It presumes that you have built the map engine AAR and included it in your build script as
 * a compile time dependency.
 * </p>
 * <ul>
 * <li>map_engine_name is the name of the class that implements IMapInstance interface.</li>
 * <li>map_name is the name of the map instance, this is required and must be unique</li>
 * </ul>
 * <pre>
 *    &lt;mil.emp3.api.MapView xmlns:android="http://schemas.android.com/apk/res/android"
 *        xmlns:app="http://schemas.android.com/apk/res-auto"
 *        android:id="@+id/map"
 *        app:map_engine_name="mil.emp3.worldwind.MapInstance"
 *        app:map_name="map1"
 *        android:layout_width="fill_parent"
 *        android:layout_height="wrap_content"
 *        android:layout_weight="1"/&gt;
 * </pre>
 * <pre>
 *      map = (IMap) findViewById(R.id.map);
 *      try {
 *          map.addMapStateChangeEventListener(new IMapStateChangeEventListener() {
 *          public void onEvent(MapStateChangeEvent mapStateChangeEvent) {
 *              Log.d(TAG, &quot;mapStateChangeEvent map&quot; + mapStateChangeEvent.getNewState());
 *              // If new map state is MAP_READY then you can begin to use the Map.
 *          }
 *          });
 *      } catch (EMP_Exception e) {
 *          e.printStackTrace();
 *      }
 * </pre>
 * </p>
 */
package mil.emp3.api;