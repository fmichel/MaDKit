package madkit.grafana.docker;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.logging.Logger;

/**
 * Polls an HTTP health endpoint until it returns 200 or a timeout is reached.
 */
public class HealthChecker {

    private static final Logger LOGGER = Logger.getLogger(HealthChecker.class.getName());
    /** Interval in milliseconds between successive health-check polls. */
    private static final long POLL_INTERVAL_MS = 1_000;

    /** The HTTP client used for health-check requests. */
    private final HttpClient httpClient;

    /**
     * Creates a new health checker with a default 3-second connect timeout.
     */
    public HealthChecker() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(3))
                .build();
    }

    /**
     * Checks if the given URL returns HTTP 200.
     *
     * @param healthUrl the URL to check (e.g., "http://localhost:3000/api/health")
     * @return true if the endpoint is healthy
     */
    public boolean isHealthy(String healthUrl) {
        try {
            var request = HttpRequest.newBuilder(URI.create(healthUrl))
                    .timeout(Duration.ofSeconds(3)).GET().build();
            var response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            return response.statusCode() == 200;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Blocks until the endpoint is healthy or the timeout expires.
     *
     * @param healthUrl the URL to poll
     * @param timeout   maximum wait duration
     * @throws InterruptedException  if interrupted while waiting
     * @throws IllegalStateException if the timeout expires before the endpoint becomes healthy
     */
    public void awaitHealthy(String healthUrl, Duration timeout) throws InterruptedException {
        long deadline = System.currentTimeMillis() + timeout.toMillis();
        LOGGER.fine(() -> "Waiting for health at " + healthUrl + " (timeout: " + timeout + ")");
        while (System.currentTimeMillis() < deadline) {
            if (isHealthy(healthUrl)) {
                LOGGER.fine(() -> "Health check passed for " + healthUrl);
                return;
            }
            Thread.sleep(POLL_INTERVAL_MS);
        }
        throw new IllegalStateException("Health check timed out for " + healthUrl + " after " + timeout);
    }
}
