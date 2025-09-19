import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import Model.*;
import Search.*;

import java.awt.*;
import java.time.LocalDateTime;
import java.util.*;
import java.util.List;

class TestSearch {

    private List<Connection> connections;
    private Stop startStop;
    private Stop endStop;
    private LocalDateTime towardDateTime;
    private LocalDateTime backwardDateTime;

    @BeforeEach
    void setUp() {
        // Létrehozunk néhány mintát a Stop-ok és Connection-ök számára
        startStop = new Stop("Start Station", 47.4979, 19.0402);
        endStop = new Stop("End Station", 47.5079, 19.0502);

        // Teszt adatokat hozunk létre
        connections = new ArrayList<>();

        // Létrehozunk néhány kapcsolatot (példa)
        Route route = new Route("Route A", 1, Color.RED);
        Connection connection1 = new Connection(route, startStop, endStop, 3600, 3660, Collections.singletonList("20241125"));
        Connection connection2 = new Connection(route, endStop, startStop, 3660, 3720, Collections.singletonList("20241125"));

        connections.add(connection1);
        connections.add(connection2);

        // Létrehozunk egy konkrét időpontot
        towardDateTime = LocalDateTime.of(2024, 11, 25, 1, 0);
        backwardDateTime = LocalDateTime.of(2024, 11, 25, 10, 0);
    }

    @Test
    void testForwardSearch() {
        Search forwardSearch = new ForwardSearch(connections);
        List<List<Connection>> routes = forwardSearch.findShortestRoutes(startStop, endStop, towardDateTime);

        assertNotNull(routes);
        assertFalse(routes.isEmpty());

        // Ellenőrizzük, hogy az első útvonal tényleg a várt irányba megy
        List<Connection> route = routes.get(0);
        assertEquals(startStop, route.get(0).getFromStop());
        assertEquals(endStop, route.get(route.size() - 1).getToStop());
    }

    @Test
    void testBackwardSearch() {
        Search backwardSearch = new BackwardSearch(connections);
        List<List<Connection>> routes = backwardSearch.findShortestRoutes(endStop, startStop, backwardDateTime);

        assertNotNull(routes);
        assertFalse(routes.isEmpty());

        // Ellenőrizzük, hogy az első útvonal tényleg a várt irányba megy
        List<Connection> route = routes.get(0);
        assertEquals(startStop, route.get(0).getToStop());
        assertEquals(endStop, route.get(route.size() - 1).getFromStop());
    }

    @Test
    void testSearchWithNoValidRoute() {
        // Az időpontot úgy állítjuk be, hogy nincs érvényes kapcsolat
        LocalDateTime invalidTime = LocalDateTime.of(2024, 11, 25, 23, 59);
        Search forwardSearch = new ForwardSearch(connections);
        List<List<Connection>> routes = forwardSearch.findShortestRoutes(startStop, endStop, invalidTime);

        assertNotNull(routes);
        assertTrue(routes.isEmpty());
    }

    @Test
    void testConnectionSelector() {
        Connection connection = connections.get(0);
        int startTime = 3600; // 01:00:00
        boolean isSelectable = connection.getDepartureTime() >= startTime;

        Search forwardSearch = new ForwardSearch(connections);
        boolean result = ((ForwardSearch) forwardSearch).ConnectionSelector(connection, startTime);

        assertEquals(isSelectable, result);
        //Teszteljük a ConnectionSelector metódust (backward search)
        isSelectable = connection.getDepartureTime() <= startTime;
        Search backwardSearch = new BackwardSearch(connections);
        result = ((BackwardSearch) backwardSearch).ConnectionSelector(connection, startTime);
        assertEquals(isSelectable, result);
    }

    @Test
    void testForwardTransferTime() {
        Connection connection1 = connections.get(0);
        Connection connection2 = connections.get(1);

        Search forwardSearch = new ForwardSearch(connections);
        int transferTime = ((ForwardSearch) forwardSearch).transferTime(connection1, connection2);
        assertTrue(transferTime >= 60);
    }

    @Test
    void testBackwardSearchWithValidRoute() {
        Search backwardSearch = new BackwardSearch(connections);
        List<List<Connection>> routes = backwardSearch.findShortestRoutes(endStop, startStop, backwardDateTime);

        assertNotNull(routes);
        assertFalse(routes.isEmpty());
    }

    @Test
    void testBackwardSearchWithNoValidRoute() {
        LocalDateTime invalidTime = LocalDateTime.of(2024, 11, 25, 0, 59);
        Search backwardSearch = new BackwardSearch(connections);
        List<List<Connection>> routes = backwardSearch.findShortestRoutes(startStop, endStop, invalidTime);

        assertNotNull(routes);
        assertTrue(routes.isEmpty());
    }

    @Test
    void testBackwardTransferTime() {
        Connection connection1 = connections.get(0);
        Connection connection2 = connections.get(1);

        Search backwardSearch = new BackwardSearch(connections);
        int transferTime = ((BackwardSearch) backwardSearch).transferTime(connection1, connection2);

        // Ellenőrizzük, hogy a transfer time megfelelő-e
        assertTrue(transferTime >= 60, "Transfer time should be at least 60 seconds for different routes in backward search");
    }
}
