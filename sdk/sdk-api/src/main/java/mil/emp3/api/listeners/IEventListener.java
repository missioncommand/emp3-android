package mil.emp3.api.listeners;


import mil.emp3.api.interfaces.IEvent;

/**
 * This interface defines the base of all event listener interfaces.
 * @param <T> An object the is derived from {@link IEvent}.<br/>
 * <br/>
 * See the
 * <a href="package-summary.html#package.description">
 *    package overview
 * </a> for more information.
 */
public interface IEventListener<T extends IEvent> {

  /** 
   * This method must be implemented by an object class to handle T type events.
   * @param event An event class that implements the {@link IEvent} interface.
   */
  void onEvent(T event);
}
