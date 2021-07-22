package org.kie.baaas.optimizer.domain;

public class Resource extends AbstractIdentifiable {

    private int index;
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

    public Resource(long id, int index, double safeCapacityRatio) {
        this(id, safeCapacityRatio);
        this.index = index;
    }

    public int getIndex() {
        return index;
    }

    public double getSafeCapacityRatio() {
        return safeCapacityRatio;
    }
}
