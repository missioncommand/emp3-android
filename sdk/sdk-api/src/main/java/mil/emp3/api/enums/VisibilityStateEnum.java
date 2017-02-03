package mil.emp3.api.enums;

/**
 *
 * Visibility states of an object; the visibility
 * state of an object is specific to a map. Same object can have different
 * visibility states on different maps.
 */
public enum VisibilityStateEnum {
    /**
     * Object is visible on the map; the visibility of any object contained within the object will depend on its own
     * visibility state on the same map.
     */
    VISIBLE,

    /**
     * Visibility state of an object in context to a specific parent on a specific map.
     * It indicates that the object is marked visible under the parent in question but
     * is hidden due to the parents ancestors (that are on the same map) visibility 
     * setting. The object would not be visible if it had no other parents on the 
     * same map. If the object had other parents on the same map the objects 
     * visibility on the map would depend on its visibility state with those other parents.
     */
    VISIBLE_ANCESTOR_HIDDEN,

    /**
     * Not visible in relation to parent.
     * In the case of Map.getVisible(target) then HIDDEN means it is not visible to user on map engine at all.
     * HIDDEN in relation to a specific parent does not guarantee that it is not visible to the user under a different parent
     */
    HIDDEN
}
