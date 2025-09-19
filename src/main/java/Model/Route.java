package Model;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import java.awt.*;

public class Route {
    private final String routeName;
    private final int routeType;
    private final Color routeColor;

    public Route(String routeName, int routeType, Color routeColor) {
        this.routeName = routeName;
        this.routeType = routeType;
        this.routeColor = routeColor;
    }
    //Getterek
    public String getRouteName() {return routeName;}
    public int getRouteType() {return routeType;}
    public Color getRouteColor() {return routeColor;}

    public JsonObject toJson() {
        JsonObjectBuilder routeBuilder = Json.createObjectBuilder();
        routeBuilder.add("routeName", routeName);
        routeBuilder.add("routeType", routeType);
        routeBuilder.add("routeColor", String.format("#%02x%02x%02x", routeColor.getRed(), routeColor.getGreen(), routeColor.getBlue()));
        return routeBuilder.build();
    }

}
