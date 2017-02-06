/**
 * All interfaces in this package must be implemented by a map engine wrapper. EMP Core modules are dependent on these interfaces.
 *
 * EMP is loosely split into three layers, namely API, Core and Map Engine. The API and Core together insulate the application
 * from specific Map Engine implementations. IMapInstance insulates the EMP Core from specific map engine API. In the current implementation
 * EMP supports NASA World Wind map engine. If you want to support a map engine other than NASA World Wind then you will need to
 * implement IMpaInstance interface along with other interfaces in this package. EMP also allows for multiple instances of same or different
 * map engines to be present and active in a single application. Implementors of IMapInstance must allow for multiple instances of one or more
 * map engines to operate side-by-side in a single instance of an application. Instances of map engine can either be compiled into the application
 * or can be loaded from a self contained android application.
 */
package mil.emp3.mapengine.interfaces;