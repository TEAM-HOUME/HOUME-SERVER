package or.sopt.houme.global.logging;

import net.logstash.logback.marker.LogstashMarker;
import net.logstash.logback.marker.Markers;

import java.util.LinkedHashMap;
import java.util.Map;

public final class LogMarkers {

    private LogMarkers() {
    }

    public static LogstashMarker fields(Object... keyValues) {
        if (keyValues.length % 2 != 0) {
            throw new IllegalArgumentException("Structured log fields must be key-value pairs.");
        }

        Map<String, Object> fields = new LinkedHashMap<>();
        for (int i = 0; i < keyValues.length; i += 2) {
            fields.put(String.valueOf(keyValues[i]), keyValues[i + 1]);
        }
        return Markers.appendEntries(fields);
    }
}
