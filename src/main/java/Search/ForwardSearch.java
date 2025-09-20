package Search;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

import Model.Connection;
import Model.Stop;

public class ForwardSearch extends Search {

    public ForwardSearch(List<Connection> connections) {
        super(connections);
    }

    @Override
    protected PriorityQueue<Connection> createQueue() {
        return new PriorityQueue<>(Comparator.comparingInt(Connection::getArrivalTime).thenComparing(c -> c.getRoute().getRouteName()));
    }

    @Override
    public boolean ConnectionSelector(Connection connection, int startTime){
        return startTime<=connection.getDepartureTime();
    }

    @Override
    protected void addConnectionMap(Connection connection, Map<Stop, List<Connection>> connectionsMap){
        connection.setPreviousConnection(null);
        connectionsMap.computeIfAbsent(connection.getFromStop(), k -> new ArrayList<>()).add(connection);
    }

    @Override
    protected void initializeQueue(PriorityQueue<Connection> queue, Map<Stop, List<Connection>> connectionsMap, Stop startStop , Stop endStop, int startTime, Map<Stop, Integer> bestArrivalTimes) {
        for (Connection connection : connectionsMap.getOrDefault(startStop, Collections.emptyList())) {
            updateTimeConnection(connection,startTime,bestArrivalTimes);
            if (connection.getDepartureTime() >= startTime) {
                if (!bestArrivalTimes.containsKey(connection.getToStop()) || connection.getArrivalTime() < bestArrivalTimes.get(connection.getToStop())) {
                    bestArrivalTimes.put(connection.getToStop(), connection.getArrivalTime());
                    queue.add(connection);
                }
            }
        }
    }

    @Override
    public void updateTimeConnection(Connection connection,int newDepartureTime, Map<Stop, Integer> bestArrivalTimes) {
        if (connection.getRoute().getRouteType()==21) {
            if (!bestArrivalTimes.containsKey(connection.getToStop()) || connection.getArrivalTime() < bestArrivalTimes.get(connection.getToStop())){
                int travelTime=connection.getTravelTime();
                connection.setDepartureTime(newDepartureTime);
                connection.setArrivalTime((newDepartureTime+travelTime));
            }
        }
    }

    @Override
    protected void processConnection(PriorityQueue<Connection> queue, Map<Stop, List<Connection>> connectionsMap, Connection currentConnection, Map<Stop, Integer> bestArrivalTimes) {
        for (Connection nextConnection : connectionsMap.getOrDefault(currentConnection.getToStop(), Collections.emptyList())) {
            updateTimeConnection(nextConnection,currentConnection.getArrivalTime(),bestArrivalTimes);
            if(nextConnection.getRoute().getRouteType()==21 && currentConnection.getRoute().getRouteType()==21){
                continue;
            }
            int currentConnectionDepartureTime=transferTime(currentConnection,nextConnection);
            if ((nextConnection.getDepartureTime() >= currentConnectionDepartureTime)) {
                // Csak akkor folytatjuk, ha új, gyorsabb útvonalat találtunk
                if (!bestArrivalTimes.containsKey(nextConnection.getToStop()) || nextConnection.getArrivalTime() < bestArrivalTimes.get(nextConnection.getToStop())) {
                    bestArrivalTimes.put(nextConnection.getToStop(), nextConnection.getArrivalTime());
                    nextConnection.setPreviousConnection(currentConnection);
                    queue.add(nextConnection);
                }
            }
        }
    }

    @Override
    protected boolean resultRoute(Connection currentConnection,List<List<Connection>>routes, Stop startStop,Stop endStop,Map<Stop, List<Connection>> connectionsMap, Map<Stop, Integer> bestArrivalTimes) {
        if(currentConnection.getToStop().equals(endStop)){
            Connection step = currentConnection;
            List<Connection>route = new ArrayList<>();

            if(step.getRoute().getRouteType()==21){
                route.add(currentConnection);
                connectionsMap.computeIfAbsent(step.getFromStop(), k -> new ArrayList<>()).remove(step);
                step=step.getPreviousConnection();
            }
            currentConnection=step;
            while (step != null) {
                route.add(step);
                if(step.getRoute().equals(currentConnection.getRoute())){
                    connectionsMap.computeIfAbsent(step.getFromStop(), k -> new ArrayList<>()).remove(step);
                }
                step = step.getPreviousConnection();
            }
            Collections.reverse(route);
            routes.add(route);
            return true;
        }
        return false;
    }

    @Override
    protected void sortRoutes(List<List<Connection>> routes) {
        routes.sort((route1, route2) -> {
            // Az utolsó connection toArrivalTime-ját hasonlítjuk össze
            int arrivalTime1 = route1.get(route1.size() - 1).getArrivalTime();
            int arrivalTime2 = route2.get(route2.size() - 1).getArrivalTime();
            return Integer.compare(arrivalTime1, arrivalTime2);
        });
    }

    @Override
    public int transferTime(Connection currentConnection, Connection nextConnection) {
        String currentLineName = currentConnection.getRoute().getRouteName();
        String nextLineName = nextConnection.getRoute().getRouteName();
        if (!currentLineName.equals(nextLineName)&&(nextConnection.getRoute().getRouteType()!=21)) {
            return currentConnection.getArrivalTime()+120 ;
        }
        return currentConnection.getArrivalTime();
    }
}
