package mil.emp3.api.events;

import mil.emp3.api.interfaces.IContainer;
import mil.emp3.api.interfaces.IFeature;
import mil.emp3.api.interfaces.IMap;
import mil.emp3.api.interfaces.IOverlay;
import mil.emp3.api.enums.ContainerEventEnum;
import org.cmapi.primitives.IGeoBase;

/**
 * This class implements an abstract Container event. It provides access to all event properties.
 */
public abstract class ContainerEvent extends Event<ContainerEventEnum, IContainer> {
    /**
     * This constructor is used by the EMP core to create the event.
     * @param eEvent {@link ContainerEventEnum} This parameter indicates the type of container event.
     * @param oTarget this parameter indicates the actual container in which the events has occurred.
     */
    protected ContainerEvent(ContainerEventEnum eEvent, IContainer oTarget) {
        super(eEvent, oTarget);
    }

    /**
     * This method retrieves a list of the children that the events refers to. If
     * The event is an ADD, the list contains the children that were added. If its a REMOVE
     * event the list of children that were removed.
     * @return 
     */
    public abstract java.util.List<IGeoBase> getAffectedChildren();

    /**
     * This method is a convenience function that indicate if the target is an IMap
     * @return True if the target object implements the IMap interface. False otherwise.
     */
    public boolean targetIsMap() {
        return (this.getTarget() instanceof IMap);
    }

    /**
     * This method is a convenience function that indicate if the target is an IOverlay
     * @return True if the target object implements the IOverlay interface. False otherwise.
     */
    public boolean targetIsOverlay() {
        return (this.getTarget() instanceof IOverlay);
    }

    /**
     * This method is a convenience function that indicate if the target is an IFeature
     * @return True if the target object implements the IFeature interface. False otherwise.
     */
    public boolean targetIsFeature() {
        return (this.getTarget() instanceof IFeature);
    }

    /**
     * This method retrieves the target as an IMap object.
     * @return An IMap object if the target implements the IMap interface. null otherwise.
     */
    public IMap getTargetMap() {
        if (this.targetIsMap()) {
            return (IMap) this.getTarget();
        }
        return null;
    }

    /**
     * This method retrieves the target as an IOverlay object.
     * @return An IOverlay object if the target implements the IOverlay interface. null otherwise.
     */
    public IOverlay getTargetOverlay() {
        if (this.targetIsOverlay()) {
            return (IOverlay) this.getTarget();
        }
        return null;
    }

    /**
     * This method retrieves the target as an IFeature object.
     * @return An IFeature object if the target implements the IFeature interface. null otherwise.
     */
    public IFeature getTargetFeature() {
        if (this.targetIsFeature()) {
            return (IFeature) this.getTarget();
        }
        return null;
    }
}
