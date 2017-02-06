/**
 * <p>
 * OVERVIEW:
 * </p>
 * <p>
 * The Extensible Mapping Platform 3 (EMP3) is a US Government Open Source project providing a framework for adding map capabilities to android applications.
 * The EMP project is currently managed by US Army Tactical Mission Command (TMC) in partnership with DI2E, and developed by CECOM Software Engineering Center
 * Tactical Solutions Directorate (SEC). EMP is intended to be used and contributed to by any US Government organization that falls within the ITAR restrictions placed on the software hosted
 * on the DI2E Dev Tools site. EMP evolved from the US Army Common Operating Environment (COE) Command Post Computing Environment (CP CE) initiative. The CP CE
 * initiative was recently extended to include the mounted computing environment and the acronym was accordingly updated to CP/M CE.
 * The term CP/M CE is still used in many areas and there is a CP/M CE targeted build that is produced from EMP to meet the CPCE map and API
 * requirements. The great news about EMP is that it is not limited to the CPCE specific build and feature set. EMP is designed to be customized and
 * extended to meet the needs and schedule of any organization.
 * </p>
 *
 * <p>
 * ARCHITECTURE:
 * </p>
 * <p>
 * EMP3 follows the layered architecture pattern with Application being the top most layer and a map engine (e.g. NASA World Wind) being the bottom most
 * layer. Following is a brief description of each of those layers.
 * </p>
 * <p>
 * <b>Developer Facing MAP API</b>: This is the Application hook into the EMP3 framework and it doesn't change regardless of which map engine is used in the
 * bottom most layer. The {@link mil.emp3.api.interfaces.IMap} class is the outermost container and has the capabilities to
 * add {@link mil.emp3.api.interfaces.IOverlay} objects which in turn contain {@link mil.emp3.api.interfaces.IFeature} objects. Out of the box EMP3 supports
 * rendering of Military Standard Symbols, various standard shapes and Text on the map. In addition to adding and deleting objects to the displayed map
 * applications can listen for events that represent user actions on the screen, application actions and view changes caused by camera settings. Capability to
 * edit existing features and freehand drawing is also supported. This is not an exhaustive list of features and you are encouraged to look at the mil.emp3.api
 * for further details. Application gets an handle to the IMap by creating either the mil.emp3.api.MapView or mil.emp3.api.MapFragment. These objects can be
 * created by either including them in the layout file or programmatically. An application can create multiple MapView and MapFragment objects allowing for
 * multiple maps within single Android application. Moreover each of these MapView/MapFragment can be linked to a different map engine if required.
 * </p>
 * <p>
 * <b>Map Core</b>: Loosely speaking this is the implementation of the Developer Facing MAP API and as such it stores all the artifacts Map related artifacts,
 * listeners and editors. It manages mapping between IMap and map engine, invokes application installed listeners, maintains visibility of features
 * on the map based on overlay/feature hierarchy, uses 2525 renderer for rendering Military Standard symbols etc. Once of it major functions is to
 * normalize functional discrepancies across map engines from various vendors.
 * </p>
 * <p>
 * <b>2525 Renderer</b>: This is a stand alone open source component that is used by EMP to render Military Standard Symbols.
 * </p>
 * <p>
 * <b>Map Engine Interface</b>: This interface isolates the 'Map Core' layer from map engine implementation. Developers wishing to support a new map engine
 * must implement this interface.
 * </p>
 * <p>
 * <b>Map engine implementation</b>: This layer consists of two components, namely, 'Map Engine Glue Code' which is implementation of 'Map Engine Interface' and
 * 'Map Engine SDK' which is the vendor supplied map engine.
 * </p>
 * <p>
 * <b>EMP3 Capability Tester</b>: This is an application that uses MapView and Developer Facing MAP API to validate and verify the capabilities claimed by EMP3 package.
 * It by no means is complete at this stage.
 * </p>
 * <table style="width:100%">
 * <tr>
 * <th>Layer</th>
 * <th>Gradle Project</th>
 * <th>Java Package</th>
 * </tr>
 * <tr>
 * <td>Developer Facing MAP API</td>
 * <td>emp3-android-sdk</td>
 * <td>mil.emp3.api</td>
 * </tr>
 * <tr>
 * <td>Map Core</td>
 * <td>emp3-android-sdk-core-aar</td>
 * <td>mil.emp3.core</td>
 * </tr>
 * <tr>
 * <td>Map Engine Interface</td>
 * <td>emp3-android-sdk</td>
 * <td>mil.emp3.mapengine</td>
 * </tr>
 * <tr>
 * <td>Map engine implementation</td>
 * <td>map-engine-worldwind-apk</td>
 * <td>mil.emp3.worldwind</td>
 * </tr>
 * <tr>
 * <td>Test Application</td>
 * <td>test-vandv</td>
 * <td>mil.emp3.test.emp3vv</td>
 * </tr>
 * </table>
 * <p>
 * How to build an application with EMP3 artifacts? There are a number ways, depending on your environment, to either build with compile time dependency
 * run time dependency on the EMP3 components. The gradle lines below show the simplest approach to get your first application up and running.
 * <pre>
 * compile ("mil.army.sec.smartClient:emp3-android-sdk-view:SDK_VERSION@aar")  { transitive = true }
 * compile (group: 'mil.army.sec.smartClient', name: 'emp3-android-sdk-core', version: "$SDK_VERSION", ext: 'aar') { transitive = true }
 * compile ("mil.army.sec.smartClient:emp3-android-worldwind:SDK_VERSION@aar") { transitive = true }
 * </pre>
 * </p>
 */
package mil.emp3;