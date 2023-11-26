package hu.krisz768.bettertuke.Database;

import android.content.Context;

import java.io.Serializable;

public class BusLine implements Serializable {
    private final int LineId;
    private final int DepartureHour;
    private final int DepartureMinute;
    private final LineInfoTravelTime[] Stops;
    private final LineInfoRoute[] Route;
    private final LineInfoRouteInfo RouteInfo;
    private String Date;

    private BusLine CTrip;

    public LineInfoRouteInfo getRouteInfo() {
        return RouteInfo;
    }

    public int getLineId() {
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

    public BusLine(int lineId, int departureHour, int departureMinute, LineInfoTravelTime[] stops, LineInfoRoute[] route, LineInfoRouteInfo routeInfo, BusLine cTrip) {
        LineId = lineId;
        DepartureHour = departureHour;
        DepartureMinute = departureMinute;
        Stops = stops;
        Route = route;
        RouteInfo = routeInfo;
        CTrip = cTrip;
    }

    public static BusLine BusLinesByLineId(int Id, boolean GetGTFS, Context ctx) {
        DatabaseManager Dm = new DatabaseManager(ctx);

        return Dm.GetBusLineById(Id, GetGTFS);
    }

    public String getDate() {
        return Date;
    }

    public void setDate(String date) {
        Date = date;
    }
}
