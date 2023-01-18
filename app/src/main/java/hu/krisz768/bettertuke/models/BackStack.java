package hu.krisz768.bettertuke.models;

import com.google.android.gms.maps.model.LatLng;

import hu.krisz768.bettertuke.Database.BusLine;

public class BackStack {
    private final Integer CurrentPlace;
    private final Integer CurrentStop;
    private final Integer CurrentBusTrack;
    private final ScheduleBackStack scheduleBackStack;
    private final boolean IsBackButtonCollapse;
    private final LatLng SelectedPlace;

    private final BusLine busLine;

    public BackStack(Integer currentPlace, Integer currentStop, Integer currentBusTrack, BusLine busLine, ScheduleBackStack scheduleBackStack, boolean IsBackButtonCollapse, LatLng SelectedPlace) {
        CurrentPlace = currentPlace;
        CurrentStop = currentStop;
        CurrentBusTrack = currentBusTrack;
        this.busLine = busLine;
        this.scheduleBackStack = scheduleBackStack;
        this.IsBackButtonCollapse = IsBackButtonCollapse;
        this.SelectedPlace = SelectedPlace;
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

    public BusLine getBusLine() {
        return busLine;
    }

    public ScheduleBackStack getScheduleBackStack() {
        return scheduleBackStack;
    }

    public boolean isBackButtonCollapse() {
        return IsBackButtonCollapse;
    }

    public LatLng getSelectedPlace() {
        return SelectedPlace;
    }
}
