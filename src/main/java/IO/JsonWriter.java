package IO;
import Model.Connection;
import java.io.*;
import java.util.List;
import javax.json.*;

public class JsonWriter {

    public static void writeConnectionsToFile(List<Connection> connections, String filePath) {
        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(filePath)))) {
            JsonObjectBuilder jsonBuilder = Json.createObjectBuilder();
            JsonArrayBuilder connectionsArrayBuilder = Json.createArrayBuilder();
            int maxConnections = 10000; // maximum 10,000 connection-t ír ki mert túl hosszú idő lenne többet
            int processedConnections = 0;

            final int totalConnections = connections.size();

            for (Connection connection : connections) {
                if (processedConnections >= maxConnections) {
                    break;
                }

                JsonObject connectionJson = connection.toJson(); // Átalakítjuk a Connection objektumot JSON objektummá
                connectionsArrayBuilder.add(connectionJson);
                processedConnections++;

            }

            // Kiírás
            JsonObject finalJson = jsonBuilder.add("connections", connectionsArrayBuilder.build()).build();
            writer.write(finalJson.toString());
            writer.flush();
            System.out.println("Kiírtam összesen " + processedConnections + " kapcsolatot.");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
