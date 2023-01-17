package hu.krisz768.bettertuke.models;

import com.google.android.gms.maps.model.LatLng;

import hu.krisz768.bettertuke.Database.BusJaratok;

public class BackStack {
    private Integer CurrentPlace = -1;
    private Integer CurrentStop = -1;
    private Integer CurrentBusTrack = -1;
    private ScheduleBackStack scheduleBackStack;
    private boolean IsBackButtonCollapse;
    private LatLng SelectedPlace;

    private BusJaratok busJarat;

    public BackStack(Integer currentPlace, Integer currentStop, Integer currentBusTrack, BusJaratok busJarat, ScheduleBackStack scheduleBackStack, boolean IsBackButtonCollapse, LatLng SelectedPlace) {
        CurrentPlace = currentPlace;
        CurrentStop = currentStop;
        CurrentBusTrack = currentBusTrack;
        this.busJarat = busJarat;
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

    public BusJaratok getBusJarat() {
        return busJarat;
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
