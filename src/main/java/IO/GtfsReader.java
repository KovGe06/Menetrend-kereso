package IO;

import Model.Connection;
import Model.Route;
import Model.Stop;

import java.awt.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class GtfsReader {
    private List<Connection> connections = Collections.synchronizedList(new ArrayList<>());
    private final Map<String, Route> routesMap = new HashMap<>();
    private final Map<String, Stop> stopsMap = new HashMap<>();
    private final Map<String, List<String>> calendarDatesMap = new HashMap<>();
    private final List<String> trips = Collections.synchronizedList(new ArrayList<>());
    private final Map<String, List<String>> stopTimesMap = new HashMap<>();

    public List<Connection> getConnections() {
        return connections;
    }

    public Map<String, Stop> getStopsMap() {
        return stopsMap;
    }

    public List<String> getStopNames() {
        synchronized (stopsMap) {
            return stopsMap.values().stream()
                    .map(Stop::getStopName)
                    .distinct()
                    .collect(Collectors.toList());
        }
    }

    public void loadData(String path) throws IOException, InterruptedException {
        connections.clear();
        routesMap.clear();
        stopsMap.clear();
        calendarDatesMap.clear();
        trips.clear();
        stopTimesMap.clear();
        Thread routeThread = createFileLoaderThread(path, "routes.txt", this::loadRoute);
        Thread stopsThread = createFileLoaderThread(path, "stops.txt", this::loadStops);
        Thread calendarDatesThread = createFileLoaderThread(path, "calendar_dates.txt", this::loadCalendarDates);
        Thread tripsThread = createFileLoaderThread(path, "trips.txt", this::loadTrips);
        Thread stopTimesThread = createFileLoaderThread(path, "stop_times.txt", this::loadStopTimes);

        // Indítsuk el a szálakat
        routeThread.start();
        stopsThread.start();
        calendarDatesThread.start();
        tripsThread.start();
        stopTimesThread.start();

        // Várjunk meg mindent
        routeThread.join();
        stopsThread.join();
        calendarDatesThread.join();
        tripsThread.join();
        stopTimesThread.join();

        // Miután minden adat betöltődött, generáljuk a kapcsolódásokat
        generateConnections();
        generateWalkingConnections();
    }

    private Thread createFileLoaderThread(String path, String fileName, FileLoader loader) {
        return new Thread(() -> {
            try {
                loader.load(path, fileName);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    private boolean checkFileExist(Path directoryPath, Path filePath) {
        if (!Files.exists(directoryPath) || !Files.isDirectory(directoryPath)) {
            System.err.println("A megadott mappa nem létezik: " + directoryPath);
            return true;
        }

        if (!Files.exists(filePath)) {
            System.err.println("A fájl nem található a megadott mappában: " + filePath);
            return true;
        }

        return false;
    }

    private void loadRoute(String path, String fileName) throws IOException {
        Path directoryPath = Paths.get(path);
        Path filePath = directoryPath.resolve(fileName);
        if (checkFileExist(directoryPath, filePath)) {
            return;
        }
        try (BufferedReader br = new BufferedReader(new FileReader(filePath.toFile()))) {
            br.lines().skip(1).forEach(line -> {
                String[] tokens = line.split(",");
                String routeId = tokens[1].trim();
                int routeType = Integer.parseInt(tokens[4]);
                String routeName = tokens[2].trim();
                int index = 5;
                while (index < tokens.length && !tokens[index].endsWith("\"")) {
                    index++;
                }
                Color color = index == tokens.length ? Color.decode('#' + tokens[6]) : Color.decode('#' + tokens[index + 1]);
                synchronized (routesMap) {
                    routesMap.put(routeId, new Route(routeName, routeType, color));
                }
            });
        }
    }

    private void loadStops(String path, String fileName) throws IOException {
        Path directoryPath = Paths.get(path);
        Path filePath = directoryPath.resolve(fileName);
        if (checkFileExist(directoryPath, filePath)) {
            return;
        }
        try (BufferedReader br = new BufferedReader(new FileReader(filePath.toFile()))) {
            br.lines().skip(1).forEach(line -> {
                String[] tokens = line.split(",");
                String stopId = tokens[0].trim();
                String stopName = "";
                double latitude = 0;
                double longitude = 0;
                if (tokens[1].startsWith("\"")) {
                    stopName = tokens[1] + "," + tokens[2];
                    stopName = stopName.substring(1, stopName.length() - 1);
                    latitude = Double.parseDouble(tokens[3].trim());
                    longitude = Double.parseDouble(tokens[4].trim());
                } else {
                    stopName = tokens[1].trim();
                    latitude = Double.parseDouble(tokens[2].trim());
                    longitude = Double.parseDouble(tokens[3].trim());
                }
                synchronized (stopsMap) {
                    stopsMap.put(stopId, new Stop(stopName, latitude, longitude));
                }
            });
        }
    }

    private void loadCalendarDates(String path, String fileName) {
        Path directoryPath = Paths.get(path);
        Path filePath = directoryPath.resolve(fileName);
        if (checkFileExist(directoryPath, filePath)) {
            return;
        }
        try (BufferedReader br = new BufferedReader(new FileReader(filePath.toFile()))) {
            br.lines().forEach(line -> {
                String[] tokens = line.split(",");
                if (tokens[2].equals("1")) {
                    synchronized (calendarDatesMap) {
                        calendarDatesMap
                                .computeIfAbsent(tokens[0], k -> Collections.synchronizedList(new ArrayList<>()))
                                .add(tokens[1]);
                    }
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadTrips(String path, String fileName) throws IOException {
        Path directoryPath = Paths.get(path);
        Path filePath = directoryPath.resolve(fileName);
        if (checkFileExist(directoryPath, filePath)) {
            return;
        }
        try (BufferedReader br = new BufferedReader(new FileReader(filePath.toFile()))) {
            synchronized (trips) {
                trips.addAll(br.lines().skip(1).collect(Collectors.toList()));
            }
        }
    }

    private void loadStopTimes(String path, String fileName) throws IOException {
        Path directoryPath = Paths.get(path);
        Path filePath = directoryPath.resolve(fileName);
        if (checkFileExist(directoryPath, filePath)) {
            return;
        }
        try (BufferedReader br = new BufferedReader(new FileReader(filePath.toFile()))) {
            br.lines().skip(1).forEach(line -> {
                String[] tokens = line.split(",");
                synchronized (stopTimesMap) {
                    stopTimesMap
                            .computeIfAbsent(tokens[0], k -> Collections.synchronizedList(new ArrayList<>()))
                            .add(line);
                }
            });
        }
    }

    private void generateConnections() {
        trips.forEach(trip -> {
            String[] tripTokens = trip.split(",");
            List<String> stopTimes = stopTimesMap.get(tripTokens[1]);
            if (stopTimes != null) {
                stopTimes.sort(Comparator.comparingInt(stopTime -> Integer.parseInt(stopTime.split(",")[4])));
                for (int i = 0; i < stopTimes.size() - 1; i++) {
                    String[] currentStopTokens = stopTimes.get(i).split(",");
                    String[] nextStopTokens = stopTimes.get(i + 1).split(",");
                    List<String> calendarDates = calendarDatesMap.get(tripTokens[2]);
                    synchronized (connections) {
                        connections.add(new Connection(
                                routesMap.get(tripTokens[0]),
                                stopsMap.get(currentStopTokens[1]),
                                stopsMap.get(nextStopTokens[1]),
                                parseTime(currentStopTokens[2]),
                                parseTime(nextStopTokens[3]),
                                calendarDates
                        ));
                    }
                }
            }
        });
    }

    private void generateWalkingConnections() {
        stopsMap.values().forEach(stop1 ->
                stopsMap.values().forEach(stop2 -> {
                    if (!stop1.equals(stop2)) {
                        double timeMinutes = calculateDistance(stop1, stop2);
                        if (timeMinutes <= 15) {
                            int departureTime = 0;
                            int arrivalTime = (int) Math.round(timeMinutes * 60);
                            synchronized (connections) {
                                connections.add(new Connection(
                                        new Route("walk", 21, Color.darkGray),
                                        stop1,
                                        stop2,
                                        departureTime,
                                        arrivalTime,
                                        null
                                ));
                                connections.add(new Connection(
                                        new Route("walk", 21, Color.darkGray),
                                        stop2,
                                        stop1,
                                        departureTime,
                                        arrivalTime,
                                        null
                                ));
                            }
                        }
                    }
                }));
    }

    private double calculateDistance(Stop src, Stop target) {
        double lat1 = src.getLatitude();
        double lon1 = src.getLongitude();
        double lat2 = target.getLatitude();
        double lon2 = target.getLongitude();
        double deltaLat = Math.abs(lat1 - lat2) * 110.574;
        double deltaLon = Math.abs(lon1 - lon2) * 111.320 * Math.cos(Math.toRadians((lat1 + lat2) / 2));
        return (Math.sqrt(deltaLat * deltaLat + deltaLon * deltaLon) / 3.5) * 60+1;
    }

    private int parseTime(String time) {
        String[] parts = time.split(":");
        return Integer.parseInt(parts[0]) * 3600 + Integer.parseInt(parts[1]) * 60 + Integer.parseInt(parts[2]);
    }

    // Interface a fájlok betöltésére
    @FunctionalInterface
    interface FileLoader {
        void load(String path, String fileName) throws IOException;
    }
}
