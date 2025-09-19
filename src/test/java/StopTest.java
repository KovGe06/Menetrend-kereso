import Model.Stop;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class StopTest {

    private Stop stop;

    @BeforeEach
    void setUp() {
        stop = new Stop("Station", 10.5, 20.5);
    }

    @Test
    void testConstructor() {
        assertEquals("Station", stop.getStopName());
        assertEquals(10.5, stop.getLatitude());
        assertEquals(20.5, stop.getLongitude() );
    }

    @Test
    void testgetStopName() {
        assertEquals("Station", stop.getStopName());
    }

    @Test
    void testgetLatitude() {
        assertEquals(10.5, stop.getLatitude());
    }

    @Test
    void testgetLongitude() {
        assertEquals(20.5, stop.getLongitude());
    }

    @Test
    void testToJson() {
        String expectedJson = "{\"stopName\":\"Station\",\"latitude\":10.5,\"longitude\":20.5}";
        String actualJson = stop.toJson().toString();
        assertEquals(expectedJson, actualJson);
    }
}
