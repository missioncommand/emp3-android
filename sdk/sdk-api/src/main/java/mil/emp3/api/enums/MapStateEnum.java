package mil.emp3.api.enums;

/**
 * This enumerated class defines the different states a map can be in.
 */
public enum MapStateEnum {
  /**
   * This state indicates that the map instance has NOT been initialized and therefore inoperable. It is the initial state of all map instances.
   */
  MAP_NEW,
  /**
   * This state indicates that the map instance is initializing.
   */
  INIT_IN_PROGRESS,
  /**
   * This state indicates that the map is currently being swapped.
   */
  MAP_SWAP_IN_PROGRESS,
  /**
   * This state indicates that the map instance is ready to process request.
   */
  MAP_READY,
  /**
   * This state indicates that the map instances is shutting down.
   */
  SHUTDOWN_IN_PROGRESS,
  /**
   * This state indicates that the map instance has been shutdown.
   */
  SHUTDOWN
}
