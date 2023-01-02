package hu.krisz768.bettertuke.models;

public class BusAttributes {
    private String platenumber;
    private String type;
    private int propulsion;
    private int articulated;
    private boolean lowfloor;
    private int doors;
    private boolean airconditioner;
    private boolean wifi;
    private boolean usb;

    public BusAttributes(String platenumber, String type, int propulsion, int articulated, int lowfloor, int doors, int airconditioner, int wifi, int usb) {
        this.platenumber = platenumber;
        this.type = type;
        this.propulsion = propulsion;
        this.articulated = articulated;
        this.lowfloor = lowfloor==1;
        this.doors = doors;
        this.airconditioner = airconditioner==1;
        this.wifi = wifi==1;
        this.usb = usb==1;
    }

    public String getPlatenumber() {
        return platenumber;
    }

    public String getType() {
        return type;
    }

    public int getPropulsion() {
        return propulsion;
    }

    public int getArticulated() {
        return articulated;
    }

    public boolean isLowfloor() {
        return lowfloor;
    }

    public int getDoors() {
        return doors;
    }

    public boolean isAirconditioner() {
        return airconditioner;
    }

    public boolean isWifi() {
        return wifi;
    }

    public boolean isUsb() {
        return usb;
    }
}
