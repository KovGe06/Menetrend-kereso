package Model;


import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

public class Stop {
    String stopName;
    double latitude;
    double longitude;

    public Stop(String StopName, double latitude, double longitude) {
        this.stopName = StopName;
        this.latitude = latitude;
        this.longitude = longitude;
    }
    //Getterek
    public String getStopName() {return stopName;}
    public double getLatitude() {return latitude;}
    public double getLongitude() {return longitude;}

    public JsonObject toJson() {
        JsonObjectBuilder stopBuilder = Json.createObjectBuilder();
        stopBuilder.add("stopName", stopName);
        stopBuilder.add("latitude", latitude);
        stopBuilder.add("longitude", longitude);
        return stopBuilder.build();
    }


}