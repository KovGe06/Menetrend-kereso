package Search;

import Model.Connection;
import Model.Stop;

import java.util.*;

public class BackwardSearch extends Search {

    public BackwardSearch(List<Connection> connections) {
        super(connections);
    }


    @Override
    protected PriorityQueue<Connection> createQueue() {
        return new PriorityQueue<>(Comparator.comparingInt(Connection::getDepartureTime).reversed().thenComparing(c -> c.getRoute().getRouteName()));
    }

    @Override
    public boolean ConnectionSelector(Connection connection, int startTime){
        return startTime>=connection.getDepartureTime();
    }

    @Override
    protected void addConnectionMap(Connection connection, Map<Stop, List<Connection>> connectionsMap){
        connection.setPreviousConnection(null);
        connectionsMap.computeIfAbsent(connection.getToStop(), k -> new ArrayList<>()).add(connection);
    }

    @Override
    protected void initializeQueue(PriorityQueue<Connection> queue, Map<Stop, List<Connection>> connectionsMap,Stop startStop, Stop endStop, int endTime, Map<Stop, Integer> bestDepartureTimes) {
        for (Connection connection : connectionsMap.getOrDefault(endStop, Collections.emptyList())) {
            updateTimeConnection(connection,endTime,bestDepartureTimes);
            if (connection.getArrivalTime() <= endTime) {
                if(!bestDepartureTimes.containsKey(connection.getFromStop()) || bestDepartureTimes.get(connection.getFromStop())<connection.getDepartureTime()) {
                    bestDepartureTimes.put(connection.getFromStop(), connection.getDepartureTime());
                    queue.add(connection);
                }
            }
        }
    }

    @Override
    protected void updateTimeConnection(Connection connection, int newArrivalTime, Map<Stop, Integer> bestDepartureTime) {
        if (connection.getRoute().getRouteType()==21) {
            if (!bestDepartureTime.containsKey(connection.getFromStop()) || connection.getDepartureTime() > bestDepartureTime.get(connection.getFromStop())){
                int travelTime=connection.getTravelTime();
                connection.setDepartureTime(newArrivalTime-travelTime);
                connection.setArrivalTime((newArrivalTime));
            }
        }
    }

    @Override
    protected void processConnection(PriorityQueue<Connection> queue, Map<Stop, List<Connection>> connectionsMap, Connection currentConnection, Map<Stop, Integer> bestTimes) {
        for (Connection previousConnection : connectionsMap.getOrDefault(currentConnection.getFromStop(), Collections.emptyList())) {
            updateTimeConnection(previousConnection,currentConnection.getDepartureTime(),bestTimes);
            if(previousConnection.getRoute().getRouteType()==21 && currentConnection.getRoute().getRouteType()==21){
                continue;
            }
            int currentConnectionArrivalTime = transferTime(currentConnection,previousConnection);
            if (previousConnection.getArrivalTime() <= currentConnectionArrivalTime) {
                if (!bestTimes.containsKey(previousConnection.getFromStop()) || previousConnection.getDepartureTime() > bestTimes.get(previousConnection.getFromStop())) {
                    bestTimes.put(previousConnection.getFromStop(), previousConnection.getDepartureTime());
                    previousConnection.setPreviousConnection(currentConnection);
                    queue.add(previousConnection);
                }
            }
        }
    }

    @Override
    protected boolean resultRoute( Connection currentConnection,List<List<Connection>>routes, Stop startStop,Stop endStop,Map<Stop, List<Connection>> connectionsMap, Map<Stop, Integer> bestTimes) {
        if (currentConnection.getFromStop().equals(startStop)) {
            List<Connection> route = new ArrayList<>();
            Connection step = currentConnection;

            if(step.getRoute().getRouteType()==21){
                route.add(currentConnection);
                connectionsMap.computeIfAbsent(step.getToStop(), k -> new ArrayList<>()).remove(step);
                step=step.getPreviousConnection();
            }
            currentConnection=step;
            while (step != null) {
                route.add(step);
                if(step.getRoute().equals(currentConnection.getRoute())){
                    connectionsMap.computeIfAbsent(step.getToStop(), k -> new ArrayList<>()).remove(step);
                }
                step = step.getPreviousConnection();
            }
            routes.add(route);
            return true;
        }
        return false;
    }

    @Override
    protected void sortRoutes(List<List<Connection>> routes) {
        routes.sort(Comparator.comparingInt(route -> -route.get(0).getDepartureTime()));
    }

    @Override
    public int transferTime(Connection currentConnection, Connection previousConnection) {
        String currentLineName = currentConnection.getRoute().getRouteName();
        String previousLineName = previousConnection.getRoute().getRouteName();
        if (!currentLineName.equals(previousLineName)&&(previousConnection.getRoute().getRouteType()!=21)) {
            return currentConnection.getDepartureTime()-120 ;
        }
        return currentConnection.getDepartureTime();
    }
}
