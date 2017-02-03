package mil.emp3.api.enums;

import mil.emp3.api.interfaces.IEventEnum;

/**
 *
 * This class enumerates the different visibility actions that can be performed on 
 * an object. It is also used to indicate the visibility action in a {@link VisibilityEvent}.
 */
public enum VisibilityActionEnum implements IEventEnum {
    /**
     * This enumerated value sets the object and all descendants visibility state to HIDDEN
     */
    HIDE_ALL,

    /*
    * This enumerated value sets the object and all descendants visibility state to VISIBLE. If the object has any parents that are HIDDEN, the parents must be toggled to VISIBLE all the way up the hierarchy. This should behave as defined in the TOGGLE_ON action
    */
    SHOW_ALL,

    /*
    * This enumerated value sets this objects visibility state to VISIBLE.  For each descendant if the visibility state is VISIBLE_ANCESTOR_HIDDEN set state to VISIBLE, else if child state is HIDDEN leave as HIDDEN.   If the object has any parents that are HIDDEN, the parents must be toggled to VISIBLE all the way up the hierarchy.  This should behave as defined in the TOGGLE_ON action
    */
    TOGGLE_ON,

    /*
    * This enumerated value sets this objects visibility state to HIDDEN. For each descendant if the visibility state is VISIBLE set state to VISIBLE_ANCESTOR_HIDDEN, else if child state is HIDDEN leave as HIDDEN.
    */
    TOGGLE_OFF
}
