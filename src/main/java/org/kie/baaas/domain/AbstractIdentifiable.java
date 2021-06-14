package org.kie.baaas.domain;

import org.optaplanner.core.api.domain.lookup.PlanningId;

abstract class AbstractIdentifiable {

    @PlanningId
    private Long id;

    AbstractIdentifiable() {
    }

    AbstractIdentifiable(long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }
}
