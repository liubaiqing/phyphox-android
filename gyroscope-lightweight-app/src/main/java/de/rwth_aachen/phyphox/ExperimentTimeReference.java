package de.rwth_aachen.phyphox;

import android.os.SystemClock;

import java.util.ArrayList;
import java.util.List;

public class ExperimentTimeReference {

    public static class TimeMapping {
        public Event event;
        public double experimentTime;
        public long systemTime;

        public TimeMapping(Event event, double experimentTime, long systemTime) {
            this.event = event;
            this.experimentTime = experimentTime;
            this.systemTime = systemTime;
        }
    }

    public enum Event {
        start, stop
    }

    public List<TimeMapping> timeMappings = new ArrayList<>();
    private long startTime;

    public ExperimentTimeReference() {
        startTime = SystemClock.elapsedRealtimeNanos();
        timeMappings.add(new TimeMapping(Event.start, 0.0, startTime));
    }

    public double getExperimentTime() {
        long now = SystemClock.elapsedRealtimeNanos();
        return (now - startTime) / 1e9;
    }

    public double getExperimentTimeFromEvent(long eventTime) {
        return (eventTime - startTime) / 1e9;
    }
}
