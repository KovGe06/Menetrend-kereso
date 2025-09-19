import Model.Route;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.awt.Color;

class RouteTest {

    private Route route;

    @BeforeEach
    void setUp() {
        route = new Route("Route", 1, Color.RED);
    }

    @Test
    void testConstructor() {
        assertEquals("Route", route.getRouteName());
        assertEquals(1, route.getRouteType());
        assertEquals(Color.RED, route.getRouteColor());
    }

    @Test
    void testgetRouteName() {
        assertEquals("Route", route.getRouteName());
    }

    @Test
    void testgetRouteType() {
        assertEquals(1, route.getRouteType());
    }

    @Test
    void testgetRouteColor() {
        assertEquals(Color.RED, route.getRouteColor());
    }

    @Test
    void testToJson() {
        String expectedJson = "{\"routeName\":\"Route\",\"routeType\":1,\"routeColor\":\"#ff0000\"}";
        String actualJson = route.toJson().toString();
        assertEquals(expectedJson, actualJson);
    }
}
