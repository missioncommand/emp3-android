package mil.emp3.core.events;

import java.util.ArrayList;
import java.util.List;

import mil.emp3.api.interfaces.IContainer;
import mil.emp3.api.enums.ContainerEventEnum;
import mil.emp3.api.interfaces.IFeature;

import org.cmapi.primitives.IGeoBase;

/**
 * Created by ish.rivera on 2/29/2016.
 */
public class ContainerEvent extends mil.emp3.api.events.ContainerEvent{
    private final ArrayList<IGeoBase> oChildren;

    public ContainerEvent(ContainerEventEnum eEvent, IContainer oEventedObject, IGeoBase oChild) {
        super(eEvent, oEventedObject);
        this.oChildren = new ArrayList<>();
        this.oChildren.add(oChild);
    }

    public ContainerEvent(ContainerEventEnum eEvent, IContainer oEventedObject, List<? extends IGeoBase> oList) {
        super(eEvent, oEventedObject);
        this.oChildren = new ArrayList<>(oList);
    }

    @Override
    public final ArrayList<IGeoBase> getAffectedChildren() {
        return this.oChildren;
    }
}
