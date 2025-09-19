package Search;

import Model.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

public abstract class Search {
    protected List<Connection> connections;

    public Search(List<Connection> connections) {
        this.connections = connections;
    }

    public List<List<Connection>> findShortestRoutes(Stop startStop, Stop endStop, LocalDateTime dateTime) {
        int referenceTime = dateTime.getHour() * 3600 + dateTime.getMinute() * 60 + dateTime.getSecond();
        List<List<Connection>> routes = new ArrayList<>();
        Map<Stop, List<Connection>> connectionsMap = new HashMap<>();
        Map<Stop, Integer> bestTimes = new HashMap<>();
        PriorityQueue<Connection> queue = createQueue();

        availableConnections(connectionsMap, dateTime);

        for (int i = 0; i < 5; i++)
         {
            queue.clear();
            bestTimes.clear();

            initializeQueue(queue, connectionsMap, startStop, endStop, referenceTime, bestTimes);

            while (!queue.isEmpty()) {
                Connection currentConnection = queue.remove();

                processConnection(queue, connectionsMap, currentConnection, bestTimes);

                if(resultRoute(currentConnection,routes,startStop,endStop,connectionsMap,bestTimes)){
                    queue.clear();
                }
            }

        }
        sortRoutes(routes);
        return routes;
    }

    protected void availableConnections(Map<Stop, List<Connection>> connectionsMap,LocalDateTime startDateTime) {
        LocalDate localDate=startDateTime.toLocalDate();
        int startTime=startDateTime.toLocalTime().getHour()*3600+startDateTime.toLocalTime().getMinute()*60+startDateTime.toLocalTime().getSecond();
        String currentDate=String.valueOf(localDate.getYear())+localDate.getMonthValue()+localDate.getDayOfMonth();
        localDate=localDate.minusDays(1);
        String lastDay=String.valueOf(localDate.getYear())+localDate.getMonthValue()+String.valueOf(localDate.getDayOfMonth());
        localDate=localDate.plusDays(2);
        String nextDay=String.valueOf(localDate.getYear())+localDate.getMonthValue()+String.valueOf(localDate.getDayOfMonth());
        for (Connection connection : connections) {
            List<String> calendarDates = connection.getCalendarDates();
            if (calendarDates != null) {
                for (String calendarDate : calendarDates) {
                    if (currentDate.equals(calendarDate) && ConnectionSelector(connection,startTime)) {
                        addConnectionMap(connection,connectionsMap);
                    }
                    if (nextDay.equals(calendarDate)) {
                        Connection updatedConnection = new Connection(
                                connection.getRoute(),
                                connection.getFromStop(),
                                connection.getToStop(),
                                connection.getDepartureTime()+86400,
                                connection.getArrivalTime()+86400,
                                calendarDates
                        );
                        addConnectionMap(updatedConnection,connectionsMap);
                    }
                    if ((lastDay.equals(calendarDate))) {
                        Connection updatedConnection = new Connection(
                                connection.getRoute(),
                                connection.getFromStop(),
                                connection.getToStop(),
                                connection.getDepartureTime()-86400,
                                connection.getArrivalTime()-86400,
                                calendarDates
                        );
                        addConnectionMap(updatedConnection,connectionsMap);
                    }

                }
            }else{
                addConnectionMap(connection,connectionsMap);
            }
        }
    }

    // Absztrakt metódusok a konkrét keresési logika implementálásához
    protected abstract PriorityQueue<Connection> createQueue();

    protected abstract void addConnectionMap(Connection connection, Map<Stop, List<Connection>> connectionsMap);

    protected abstract void initializeQueue(PriorityQueue<Connection> queue, Map<Stop, List<Connection>> connectionsMap, Stop startStop, Stop endStop, int referenceTime, Map<Stop, Integer> bestTimes);

    protected abstract void updateTimeConnection(Connection connection, int newTime, Map<Stop, Integer> bestDepartureTime);

    protected abstract void processConnection(PriorityQueue<Connection> queue, Map<Stop, List<Connection>> connectionsMap, Connection currentConnection, Map<Stop, Integer> bestTimes);

    protected abstract boolean ConnectionSelector(Connection connection,int startTime);

    protected abstract boolean resultRoute(Connection currentConnection,List<List<Connection>>routes, Stop startStop,Stop endStop,Map<Stop, List<Connection>> connectionsMap, Map<Stop, Integer> bestTimes);

    protected abstract void sortRoutes(List<List<Connection>> routes);

    protected abstract int transferTime(Connection currentConnection, Connection nextConnection);
}
