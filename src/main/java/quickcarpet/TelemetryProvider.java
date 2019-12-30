package quickcarpet;

import com.google.gson.JsonObject;

public interface TelemetryProvider {
    JsonObject getTelemetryData();
}
