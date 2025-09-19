package Main;

import IO.GtfsReader;
import UI.UI;

import java.io.IOException;
import java.nio.file.Paths;


public class Main {
    public static void main(String[] args) throws IOException, InterruptedException {
        GtfsReader gtfsReader = new GtfsReader();
        String path = Paths.get("src", "main", "resources").toString();
        gtfsReader.loadData(path);
        System.out.println("GTFS data is ready");
        UI ui =new UI(gtfsReader);
    }
}
