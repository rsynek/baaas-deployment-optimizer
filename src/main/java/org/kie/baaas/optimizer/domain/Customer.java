package org.kie.baaas.optimizer.domain;

public class Customer extends AbstractIdentifiable {

    private boolean exclusive;

    public Customer() {
        // Required by Jackson.
    }

    public Customer(long id, boolean exclusive) {
        super(id);
        this.exclusive = exclusive;
    }

    public boolean isExclusive() {
        return exclusive;
    }
}
