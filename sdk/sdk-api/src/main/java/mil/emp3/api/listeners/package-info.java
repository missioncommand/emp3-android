/**
 * Applications register event listeners on a variety of EMP objects like the Containers, Camera, editors etc.
 * EMP invokes the onEvent() method of these listeners when the appropriate event occurs. The event can be triggered
 * as a result of user interaction on the screen or application invoking a specific API. When an application
 * registers a listener it receives a handle object, this handle object should be used to unregister the listener.<br/>
 *<br/>
 * CAUTION:<br/>
 * &nbsp;&nbsp;&nbsp;1. Application should not execute any blocking call in the listener.<br/>
 * &nbsp;&nbsp;&nbsp;2. Application should perform minimal work within the onEvent method.<br/>
 * &nbsp;&nbsp;&nbsp;3. If an application installs multiple listeners for the same event, there is no guarantee of the order in which they will be invoked.<br/>
 * &nbsp;&nbsp;&nbsp;4. If a listener is triggered as a result of application interaction then it may be called in-line or out-of-band.<br/>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;4.1 Listener may be called after the EMP method is complete or may be called before the EMP method is complete.<br/>
 *<br/>
 */
package mil.emp3.api.listeners;
