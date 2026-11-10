package hu.krisz768.bettertuke.Database;

import android.content.Context;

import java.io.Serializable;

import hu.krisz768.bettertuke.NewGTFS.NewGTFSDatabase;

public class BusLine implements Serializable {
    private final String LineId;
    private final int DepartureHour;
    private final int DepartureMinute;
    private final LineInfoTravelTime[] Stops;
    private final LineInfoRoute[] Route;
    private final LineInfoRouteInfo RouteInfo;
    private String Date;
    private final BusLine CTrip;

    public LineInfoRouteInfo getRouteInfo() {
        return RouteInfo;
    }

    public String getLineId() {
        return LineId;
    }

    public int getDepartureHour() {
        return DepartureHour;
    }

    public int getDepartureMinute() {
        return DepartureMinute;
    }

    public LineInfoTravelTime[] getStops() {
        return Stops;
    }

    public LineInfoRoute[] getRoute() {
        return Route;
    }

    public BusLine getCTrip() {
        return CTrip;
    }

    public BusLine(String lineId, int departureHour, int departureMinute, LineInfoTravelTime[] stops, LineInfoRoute[] route, LineInfoRouteInfo routeInfo, BusLine cTrip) {
        LineId = lineId;
        DepartureHour = departureHour;
        DepartureMinute = departureMinute;
        Stops = stops;
        Route = route;
        RouteInfo = routeInfo;
        CTrip = cTrip;
    }

    public static BusLine BusLinesByLineId(String Id, boolean GetGTFS, java.util.Date date, Context ctx) {
        NewGTFSDatabase NDm = new NewGTFSDatabase(ctx);

        return NDm.GetBusLineById(Id, GetGTFS, date);
    }

    public String getDate() {
        return Date;
    }

    public void setDate(String date) {
        Date = date;
    }
}
