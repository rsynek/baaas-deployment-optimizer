package org.kie.baaas.domain;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

@JsonIdentityInfo(generator= ObjectIdGenerators.PropertyGenerator.class, property="id")
public class Resource extends AbstractIdentifiable {

    private double safeCapacityRatio;

    public Resource() {
        // Required by Jackson.
    }

    public Resource(long id, double safeCapacityRatio) {
        super(id);
        if (safeCapacityRatio < 0.0 || safeCapacityRatio > 1.0) {
            throw new IllegalArgumentException("Safe capacity ratio (" + safeCapacityRatio + ") must be between 0.0 and 1.0.");
        }
        this.safeCapacityRatio = safeCapacityRatio;
    }

    public double getSafeCapacityRatio() {
        return safeCapacityRatio;
    }
}
