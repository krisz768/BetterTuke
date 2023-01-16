package hu.krisz768.bettertuke.models;

import hu.krisz768.bettertuke.Database.BusJaratok;

public class BackStack {
    private Integer CurrentPlace = -1;
    private Integer CurrentStop = -1;
    private Integer CurrentBusTrack = -1;
    private ScheduleBackStack scheduleBackStack;

    private BusJaratok busJarat;

    public BackStack(Integer currentPlace, Integer currentStop, Integer currentBusTrack, BusJaratok busJarat, ScheduleBackStack scheduleBackStack) {
        CurrentPlace = currentPlace;
        CurrentStop = currentStop;
        CurrentBusTrack = currentBusTrack;
        this.busJarat = busJarat;
        this.scheduleBackStack = scheduleBackStack;
    }

    public Integer getCurrentPlace() {
        return CurrentPlace;
    }

    public Integer getCurrentStop() {
        return CurrentStop;
    }

    public Integer getCurrentBusTrack() {
        return CurrentBusTrack;
    }

    public BusJaratok getBusJarat() {
        return busJarat;
    }

    public ScheduleBackStack getScheduleBackStack() {
        return scheduleBackStack;
    }
}
