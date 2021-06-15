package org.kie.baaas.domain;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

@JsonIdentityInfo(generator= ObjectIdGenerators.PropertyGenerator.class, property="id")
public class Service extends AbstractIdentifiable {

    public Service() {
        // Required by Jackson and OptaPlanner.
    }

    public Service(long id) {
        super(id);
    }
}
