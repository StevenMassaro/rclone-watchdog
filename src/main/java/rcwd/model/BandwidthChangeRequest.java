package rcwd.model;

import java.util.concurrent.TimeUnit;

public class BandwidthChangeRequest {
    private final TimeUnit unit;
    private final double duration;

    public BandwidthChangeRequest(TimeUnit unit, double duration) {
        this.unit = unit;
        this.duration = duration;
    }

    public TimeUnit getUnit() {
        return unit;
    }

    public double getDuration() {
        return duration;
    }
}
