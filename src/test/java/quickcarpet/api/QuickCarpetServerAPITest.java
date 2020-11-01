package quickcarpet.api;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

class QuickCarpetServerAPITest {
    @Test
    void getInstance() {
        assertThrows(IllegalStateException.class, QuickCarpetServerAPI::getInstance);
    }
}