package mil.emp3.api.events;

import mil.emp3.api.interfaces.IContainer;
import mil.emp3.api.interfaces.IMap;
import mil.emp3.api.enums.VisibilityActionEnum;

/**
 * This event is generated when an IOverlay or IFeature object's visibility is changed on a specific map.
 * To register for this event you must instantiate an object that implements the IVisibilityEventListener
 * interface and register for the event on an IMap, IOverlay or IFeature.
 */
public abstract class VisibilityEvent extends Event<VisibilityActionEnum, IContainer> {
    protected VisibilityEvent(VisibilityActionEnum eEvent, IContainer oTarget) {
        super(eEvent, oTarget);
    }

    /**
     * This method retrieves the parent container the target visibility was changed under.
     * @return 
     * {@link IContainer} - The parent container if the visibility of the target was set via
     * a call to {@link IMap#setVisibility(mil.emp3.api.interfaces.IContainer, mil.emp3.api.interfaces.IContainer, mil.emp3.api.enums.VisibilityActionEnum)} or
     * {@link IMap#setVisibility(java.util.UUID, java.util.UUID, mil.emp3.api.enums.VisibilityActionEnum)}.
     * <br/>null - otherwise.
     */
    public abstract IContainer getParent();

    /**
     * This method returns the interface of the map the visibility event was triggered on.
     * @return IMap Interface object.
     */
    public abstract IMap getMapEventOccurredOn();
}
