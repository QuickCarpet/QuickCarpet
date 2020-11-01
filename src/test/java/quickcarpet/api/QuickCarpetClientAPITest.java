package quickcarpet.api;

import org.junit.jupiter.api.Test;
import quickcarpet.QuickCarpetClient;

import static org.junit.jupiter.api.Assertions.assertEquals;

class QuickCarpetClientAPITest {
    @Test
    void getInstance() {
        assertEquals(QuickCarpetClient.getInstance(), QuickCarpetClientAPI.getInstance());
    }
}