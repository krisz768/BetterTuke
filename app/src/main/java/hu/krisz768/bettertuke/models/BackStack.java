package hu.krisz768.bettertuke.models;

import com.google.android.gms.maps.model.LatLng;

import hu.krisz768.bettertuke.Database.BusLine;

public class BackStack {
    private final Integer CurrentPlace;
    private final String CurrentStop;
    private final String CurrentBusTrack;
    private final ScheduleBackStack scheduleBackStack;
    private final boolean IsBackButtonCollapse;
    private final LatLng SelectedPlace;
    private final BusLine busLine;
    private final IncomBusBackStack IncomBusMode;
    private final String ActiveBusType;
    private final SwitchBackStack switchBackStack;

    public BackStack(Integer currentPlace, String currentStop, String currentBusTrack, BusLine busLine, ScheduleBackStack scheduleBackStack, boolean IsBackButtonCollapse, LatLng SelectedPlace, IncomBusBackStack IncomBusMode, String activeBusType, SwitchBackStack switchBackStack) {
        CurrentPlace = currentPlace;
        CurrentStop = currentStop;
        CurrentBusTrack = currentBusTrack;
        this.busLine = busLine;
        this.scheduleBackStack = scheduleBackStack;
        this.IsBackButtonCollapse = IsBackButtonCollapse;
        this.SelectedPlace = SelectedPlace;
        this.IncomBusMode = IncomBusMode;
        ActiveBusType = activeBusType;
        this.switchBackStack = switchBackStack;
    }

    public Integer getCurrentPlace() {
        return CurrentPlace;
    }

    public String getCurrentStop() {
        return CurrentStop;
    }

    public String getCurrentBusTrack() {
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

    public IncomBusBackStack getIncomBusMode() {
        return IncomBusMode;
    }

    public String getActiveBusType() {
        return ActiveBusType;
    }

    public SwitchBackStack getSwitchBackStack() {
        return switchBackStack;
    }
}
