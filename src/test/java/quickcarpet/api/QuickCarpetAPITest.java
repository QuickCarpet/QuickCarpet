package quickcarpet.api;

import org.junit.jupiter.api.Test;
import quickcarpet.QuickCarpet;

import static org.junit.jupiter.api.Assertions.assertEquals;

class QuickCarpetAPITest {

    @Test
    void getInstance() {
        assertEquals(QuickCarpet.getInstance(), QuickCarpetAPI.getInstance());
    }
}