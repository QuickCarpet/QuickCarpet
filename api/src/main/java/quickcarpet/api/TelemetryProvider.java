package quickcarpet.api;

import com.google.gson.JsonObject;

public interface TelemetryProvider {
    JsonObject getTelemetryData();
}
