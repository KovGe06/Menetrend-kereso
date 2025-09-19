package IO;

import Model.Connection;
import Model.Route;
import Model.Stop;

import javax.json.*;
import java.awt.*;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class JsonReader {

    public static List<Connection> loadConnectionsFromFile(String filePath) {
        List<Connection> connections = new ArrayList<>();

        try (FileReader reader = new FileReader(filePath)) {
            javax.json.JsonReader jsonReader = Json.createReader(reader);
            JsonObject jsonObject = jsonReader.readObject();

            JsonArray connectionsArray = jsonObject.getJsonArray("connections");

            for (JsonObject connJson : connectionsArray.getValuesAs(JsonObject.class)) {

                // Kiolvassa a Route adatokat
                JsonObject routeJson = connJson.getJsonObject("route");
                String routeName = routeJson.getString("routeName");
                int routeType = routeJson.getInt("routeType");
                String routeColor = routeJson.getString("routeColor");
                Route route = new Route(routeName, routeType, Color.decode(routeColor));

                // Kiolvassa a FromStop adatokat
                JsonObject fromStopJson = connJson.getJsonObject("fromStop");
                String fromStopName = fromStopJson.getString("stopName");
                double fromStopLat = fromStopJson.getJsonNumber("latitude").doubleValue();
                double fromStopLon = fromStopJson.getJsonNumber("longitude").doubleValue();
                Stop fromStop = new Stop(fromStopName, fromStopLat, fromStopLon);

                // Kiolvassa a ToStop adatokat
                JsonObject toStopJson = connJson.getJsonObject("toStop");
                String toStopName = toStopJson.getString("stopName");
                double toStopLat = toStopJson.getJsonNumber("latitude").doubleValue();
                double toStopLon = toStopJson.getJsonNumber("longitude").doubleValue();
                Stop toStop = new Stop(toStopName, toStopLat, toStopLon);

                int departureTime = connJson.getInt("departureTime");
                int arrivalTime = connJson.getInt("arrivalTime");

                JsonArray calendarDatesJson = connJson.getJsonArray("calendarDates");
                List<String> calendarDates = new ArrayList<>();
                for (JsonValue date : calendarDatesJson) {
                    calendarDates.add(date.toString().replace("\"", ""));
                }

                // Hozzáadjuk az új Connection-t a listához
                Connection connection = new Connection(route, fromStop, toStop, departureTime, arrivalTime, calendarDates);
                connections.add(connection);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("Beolvastam összesen "+connections.size()+" kapcsolatot.");
        return connections;
    }
}
