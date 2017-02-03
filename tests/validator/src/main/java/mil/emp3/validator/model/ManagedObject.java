package mil.emp3.validator.model;

import java.util.UUID;

abstract public class ManagedObject<T> {

    final T object;
    final String id;

    protected ManagedObject(T object) {
        this.object = object;
        this.id     = UUID.randomUUID().toString();
    }

    public T get() {
        return object;
    }

    public String getId() {
        return id;
    }
}
