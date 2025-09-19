package Model;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import java.util.List;

public class Connection {
    private final Route route;
    private final Stop fromStop;
    private final Stop toStop;
    private int departureTime;
    private int arrivalTime;
    private final List<String> calendarDates;
    private Connection previousConnection;


    public Connection(Route route, Stop fromStop, Stop toStop,int departureTime,int arrivalTime,List<String> calendarDates) {
        this.route = route;
        this.fromStop = fromStop;
        this.toStop = toStop;
        this.departureTime = departureTime;
        this.arrivalTime = arrivalTime;
        this.calendarDates = calendarDates;
    }

    //getterek
    public Route getRoute() {return route;}
    public Stop getFromStop() {return fromStop;}
    public Stop getToStop() {return toStop;}
    public int getDepartureTime() {return departureTime;}
    public int getArrivalTime() {return arrivalTime;}
    public List<String> getCalendarDates() {return calendarDates;}
    public Connection getPreviousConnection() {return previousConnection;}
    public int getTravelTime() {return arrivalTime - departureTime;}
    //Setterek
    public void setDepartureTime(int departureTime) {this.departureTime=departureTime;}
    public void setArrivalTime(int arrivalTime) {this.arrivalTime = arrivalTime;}
    public void setPreviousConnection(Connection previousConnection) {
        this.previousConnection = previousConnection;
    }

    @Override
    public String toString () {
        return route.getRouteName() + "  " + fromStop.getStopName() + " --> " + toStop.getStopName() + "  " + formatTime(departureTime) + "  " + formatTime(arrivalTime) + "  " + formatTime(getTravelTime()) + "\n";
    }
    public JsonObject toJson() {
        JsonObjectBuilder connectionBuilder = Json.createObjectBuilder();
        connectionBuilder.add("route", route.toJson());
        connectionBuilder.add("fromStop", fromStop.toJson());
        connectionBuilder.add("toStop", toStop.toJson());
        connectionBuilder.add("departureTime", departureTime);
        connectionBuilder.add("arrivalTime", arrivalTime);

        JsonArrayBuilder calendarDatesBuilder = Json.createArrayBuilder();
        if(calendarDates != null) {
            for (String date : calendarDates) {
                calendarDatesBuilder.add(date);
            }
        }
        connectionBuilder.add("calendarDates", calendarDatesBuilder);

        return connectionBuilder.build();
    }

    public String formatTime(int time) {
        if(time<86400&&time>=0) {
            int hours = time / 3600;
            int minutes = (time%3600)/ 60;
            int seconds = time % 60 ;
            return String.format("%02d:%02d:%02d", hours, minutes, seconds);
        }if(time>=86400){
            time-=86400;
            int hours = time / 3600;
            int minutes = (time % 3600) / 60;
            int seconds = time % 60;
            return String.format("%02d:%02d:%02d", hours, minutes, seconds);
        }
        time += 86400;
        int hours = time / 3600;
        int minutes = (time % 3600) / 60;
        int seconds = time %60;
        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }
}
