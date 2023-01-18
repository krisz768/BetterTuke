package hu.krisz768.bettertuke.models;

public class BusAttributes {
    private final String plateNumber;
    private final String type;
    private final int propulsion;
    private final int articulated;
    private final boolean lowFloor;
    private final int doors;
    private final boolean airConditioner;
    private final boolean wifi;
    private final boolean usb;

    public BusAttributes(String plateNumber, String type, int propulsion, int articulated, int lowFloor, int doors, int airConditioner, int wifi, int usb) {
        this.plateNumber = plateNumber;
        this.type = type;
        this.propulsion = propulsion;
        this.articulated = articulated;
        this.lowFloor = lowFloor ==1;
        this.doors = doors;
        this.airConditioner = airConditioner ==1;
        this.wifi = wifi==1;
        this.usb = usb==1;
    }

    public String getPlateNumber() {
        String edited_plateNumber="";
        if(plateNumber.length()==6)
            edited_plateNumber= plateNumber.substring(0,3)+"-"+ plateNumber.substring(3,6);
        else if(plateNumber.length()==7)
            edited_plateNumber= plateNumber.substring(0,4)+"-"+ plateNumber.substring(4,7);
        return edited_plateNumber;
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

    public boolean isLowFloor() {
        return lowFloor;
    }

    public int getDoors() {
        return doors;
    }

    public boolean isAirConditioner() {
        return airConditioner;
    }

    public boolean isWifi() {
        return wifi;
    }

    public boolean isUsb() {
        return usb;
    }
}
