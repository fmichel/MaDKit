package madkit.grafana.metrics;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Writes simulation metrics to InfluxDB via the v2 write API using line protocol.
 * <p>
 * Thread-safe: the underlying {@link HttpClient} is thread-safe, and this class holds no
 * mutable state beyond the client.
 */
public class InfluxDbWriter implements MetricsWriter {

	private static final Logger LOGGER = Logger.getLogger(InfluxDbWriter.class.getName());

	/** The full InfluxDB v2 write API URL including org, bucket, and precision parameters. */
	private final String writeUrl;
	/** The InfluxDB authentication token. */
	private final String token;
	/** The HTTP client used for write requests. */
	private final HttpClient httpClient;

	/**
	 * Creates an InfluxDB writer.
	 *
	 * @param influxUrl the base URL (e.g., "http://localhost:8086")
	 * @param org       the InfluxDB organization
	 * @param bucket    the InfluxDB bucket
	 * @param token     the authentication token
	 */
	public InfluxDbWriter(String influxUrl, String org, String bucket, String token) {
		this.writeUrl = influxUrl + "/api/v2/write?org=" + org + "&bucket=" + bucket + "&precision=ns";
		this.token = token;
		this.httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();
	}

	@Override
	public void writeBatch(List<DataPoint> points) {
		if (points.isEmpty()) {
			return;
		}
		String body = points.stream().map(DataPoint::toLineProtocol).collect(Collectors.joining("\n"));
		try {
			var request = HttpRequest.newBuilder(URI.create(writeUrl)).header("Authorization", "Token " + token)
					.header("Content-Type", "text/plain; charset=utf-8").POST(HttpRequest.BodyPublishers.ofString(body))
					.timeout(Duration.ofSeconds(10)).build();
			var response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
			if (response.statusCode() >= 300) {
				LOGGER.warning(() -> "InfluxDB write failed (HTTP " + response.statusCode() + "): " + response.body());
			}
		} catch (IOException | InterruptedException e) {
			LOGGER.warning(() -> "InfluxDB write error: " + e.getMessage());
		}
	}

	@Override
	public void flush() {
		// HTTP writes are immediate — no buffering
	}

	@Override
	public void close() {
		// HttpClient does not require explicit close in Java 21+
	}
}
