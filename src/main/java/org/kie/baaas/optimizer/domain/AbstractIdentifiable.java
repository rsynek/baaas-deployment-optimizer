package org.kie.baaas.optimizer.domain;

import org.optaplanner.core.api.domain.lookup.PlanningId;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

@JsonIdentityInfo(generator= ObjectIdGenerators.PropertyGenerator.class, property="id")
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
