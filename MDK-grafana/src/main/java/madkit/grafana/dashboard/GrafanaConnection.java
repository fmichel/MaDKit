package madkit.grafana.dashboard;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Base64;
import java.util.logging.Logger;

/**
 * HTTP client wrapper for the Grafana REST API.
 * <p>
 * Thread-safe: the underlying {@link HttpClient} is thread-safe.
 */
public class GrafanaConnection {

    private static final Logger LOGGER = Logger.getLogger(GrafanaConnection.class.getName());

    private final String baseUrl;
    private final String authHeader;
    private final HttpClient httpClient;

    /**
     * Creates a new Grafana connection.
     *
     * @param baseUrl  the Grafana base URL (e.g., "http://localhost:3000")
     * @param username the admin username
     * @param password the admin password
     */
    public GrafanaConnection(String baseUrl, String username, String password) {
        this.baseUrl = baseUrl;
        this.authHeader = "Basic " + Base64.getEncoder()
                .encodeToString((username + ":" + password).getBytes());
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .build();
    }

    /**
     * Posts a dashboard JSON model to the Grafana API.
     *
     * @param dashboardJson the dashboard JSON string (the "dashboard" field content)
     * @return the dashboard URL path (e.g., "/d/uid/slug"), or {@code null} on failure
     * @throws IOException          if the HTTP request fails
     * @throws InterruptedException if the current thread is interrupted
     */
    public String postDashboard(String dashboardJson) throws IOException, InterruptedException {
        String payload = "{\"dashboard\":" + dashboardJson + ",\"overwrite\":true}";
        var request = HttpRequest.newBuilder(URI.create(baseUrl + "/api/dashboards/db"))
                .header("Authorization", authHeader)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(payload))
                .timeout(Duration.ofSeconds(10))
                .build();
        var response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() >= 300) {
            LOGGER.warning(() -> "Dashboard POST failed (HTTP " + response.statusCode() + "): " + response.body());
            return null;
        }
        LOGGER.fine(() -> "Dashboard posted successfully: " + response.body());
        return extractUrlFromResponse(response.body());
    }

    /**
     * Extracts the dashboard URL from a Grafana API response body.
     *
     * @param responseBody the JSON response body
     * @return the URL path, or {@code null} if not found
     */
    String extractUrlFromResponse(String responseBody) {
        int urlIdx = responseBody.indexOf("\"url\"");
        if (urlIdx < 0) return null;
        int start = responseBody.indexOf('"', urlIdx + 5) + 1;
        int end = responseBody.indexOf('"', start);
        return (start > 0 && end > start) ? responseBody.substring(start, end) : null;
    }

    /**
     * Returns the base URL of the Grafana instance.
     *
     * @return the base URL
     */
    public String getBaseUrl() {
        return baseUrl;
    }
}
