package mil.emp3.api.enums;

/**
 * Type of usability state of the Map. Map must be in MAP_READY state before applications can use the API to update the map or issue any actions.
 */
public enum MapStateEnum {
  /**
   * Map instance has NOT been initialized and therefore inoperable. It is the initial state of all map instances.
   */
  MAP_NEW,
  /**
   * Map instance is initializing.
   */
  INIT_IN_PROGRESS,
  /**
   * Map instance is currently being swapped.
   */
  MAP_SWAP_IN_PROGRESS,
  /**
   * Map instance is ready to process request.
   */
  MAP_READY,
  /**
   * Map instances is shutting down.
   */
  SHUTDOWN_IN_PROGRESS,
  /**
   * Map instance was shutdown.
   */
  SHUTDOWN
}
