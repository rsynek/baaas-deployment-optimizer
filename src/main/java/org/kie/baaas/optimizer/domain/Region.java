package org.kie.baaas.optimizer.domain;

public class Region extends AbstractIdentifiable {

    private String name;

    public Region() {
    }

    public Region(long id, String name) {
        super(id);
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return "Region{" +
                "id='" + getId() + '\'' +
                "name='" + name + '\'' +
                '}';
    }
}
