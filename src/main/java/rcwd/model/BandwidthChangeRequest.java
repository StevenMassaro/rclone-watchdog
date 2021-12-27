package rcwd.model;

import lombok.Getter;

import java.util.concurrent.TimeUnit;

@Getter
public class BandwidthChangeRequest {
    private final TimeUnit unit;
    private final double duration;

    public BandwidthChangeRequest(TimeUnit unit, double duration) {
        this.unit = unit;
        this.duration = duration;
    }
}
