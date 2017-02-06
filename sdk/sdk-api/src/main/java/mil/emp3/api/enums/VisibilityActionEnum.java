package mil.emp3.api.enums;

import mil.emp3.api.interfaces.IEventEnum;

/**
 * Types of visibility actions that can be applied to an object.
 */
public enum VisibilityActionEnum implements IEventEnum {
    /**
     * Visibility state of the object and all its descendants is set to HIDDEN
     */
    HIDE_ALL,

    /**
     * Visibility state of the object and all its descendants is set to VISIBLE.
     * <p>
     * Internal:
     * If the object has any parents that are HIDDEN, the parents must be toggled to VISIBLE all the way up the hierarchy.
     * This should behave as defined in the TOGGLE_ON action
     * </p>
     */
    SHOW_ALL,

    /**
     * Visibility state of the objects is set to VISIBLE.
     * <p>
     * Internal:
     * For each descendant if the visibility state is VISIBLE_ANCESTOR_HIDDEN set state to VISIBLE, else if child state is HIDDEN leave as HIDDEN.
     * If the object has any parents that are HIDDEN, the parents must be toggled to VISIBLE all the way up the hierarchy.
     * This should behave as defined in the TOGGLE_ON action
     * </p>
    */
    TOGGLE_ON,

    /**
     *  Visibility state of the object is set to HIDDEN.
     *  <p>
     * Internal:
     * For each descendant if the visibility state is VISIBLE set state to VISIBLE_ANCESTOR_HIDDEN, else if child state is HIDDEN leave as HIDDEN.
     * </p>
    */
    TOGGLE_OFF
}
