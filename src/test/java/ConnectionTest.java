import Model.*;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.awt.*;
import java.util.List;
import java.util.Arrays;

class ConnectionTest {

    private Connection connection;

    @BeforeEach
    void setUp() {
        Route route = new Route("R1",3, Color.BLACK);
        Stop fromStop = new Stop("Station A",10,10);
        Stop toStop = new Stop("Station B",10,20);
        List<String> calendarDates = Arrays.asList("2024-11-25", "2024-11-26");

        connection = new Connection(route, fromStop, toStop, 3600, 7200, calendarDates);
    }

    @Test
    void testConstructorAndGetters() {
        assertEquals("R1", connection.getRoute().getRouteName());
        assertEquals("Station A", connection.getFromStop().getStopName());
        assertEquals("Station B", connection.getToStop().getStopName());
        assertEquals(3600, connection.getDepartureTime());
        assertEquals(7200, connection.getArrivalTime());
        assertTrue(connection.getCalendarDates().contains("2024-11-25"));
    }

    @Test
    void testTravelTime() {
        int expectedTravelTime = 7200 - 3600;
        assertEquals(expectedTravelTime, connection.getTravelTime());
    }

    @Test
    void testToString() {
        String expectedString = "R1  Station A --> Station B  01:00:00  02:00:00  01:00:00\n";
        assertEquals(expectedString, connection.toString());
    }

    @Test
    void testSetDepartureTime() {
        connection.setDepartureTime(4500);
        assertEquals(4500, connection.getDepartureTime());
    }

    @Test
    void testSetArrivalTime() {
        connection.setArrivalTime(8100);
        assertEquals(8100, connection.getArrivalTime());
    }
}
