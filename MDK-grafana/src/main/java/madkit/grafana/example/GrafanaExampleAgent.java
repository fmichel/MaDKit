package madkit.grafana.example;

import madkit.grafana.dashboard.GrafanaConnection;
import madkit.grafana.dashboard.GrafanaDashboard;
import madkit.grafana.docker.GrafanaInfrastructure;
import madkit.grafana.metrics.SimuMetrics;
import madkit.kernel.Agent;

/**
 * A self-contained example demonstrating the MDK-grafana library.
 * <p>
 * This agent performs the full Grafana metrics lifecycle without requiring any
 * simulation framework:
 * <ol>
 *   <li>Starts the Grafana + InfluxDB Docker infrastructure in {@link #onActivation()}</li>
 *   <li>Creates a dashboard with Time Series and Stat panels (programmatic API)</li>
 *   <li>Records 100 data points with 500 ms pauses in {@link #onLive()} so the user
 *       can watch the dashboard update in real time</li>
 *   <li>Cleans up metrics in {@link #onEnd()}</li>
 * </ol>
 * <p>
 * Run with: {@code executeThisAgent()}
 *
 * @see GrafanaInfrastructure
 * @see GrafanaDashboard
 * @see SimuMetrics
 */
public class GrafanaExampleAgent extends Agent {

	/** The running Grafana + InfluxDB Docker infrastructure. */
	private GrafanaInfrastructure infrastructure;

	/** The metrics facade used to record data points. */
	private SimuMetrics metrics;

	/** The HTTP connection to the Grafana REST API. */
	private GrafanaConnection connection;

	/**
	 * Starts the Docker infrastructure, initializes metrics, creates a dashboard
	 * with four panels, and opens the browser.
	 */
	@Override
	protected void onActivation() {
		getLogger().info("Starting Grafana infrastructure...");
		infrastructure = GrafanaInfrastructure.startDefault();
		metrics = GrafanaInfrastructure.createMetrics(infrastructure);
		connection = GrafanaInfrastructure.createDefaultConnection();
		createDashboard();
		getLogger().info("Dashboard ready — open http://localhost:3000");
	}

	/**
	 * Generates 100 iterations of sinusoidal data with random noise and records
	 * three measurements (cpu, memory, requests) per iteration.
	 * <p>
	 * Each iteration pauses 500 ms so that data appears gradually in Grafana,
	 * giving the user time to observe the real-time updates.
	 */
	@Override
	protected void onLive() {
		for (int i = 0; i < 100 && isAlive(); i++) {
			int iteration = i;
			double cpuValue = computeCpuValue(i);
			double memoryValue = computeMemoryValue(i);
			double requestCount = computeRequestCount(i);

			metrics.record("cpu", cpuValue);
			metrics.record("memory", memoryValue);
			metrics.record("requests", requestCount);

			getLogger().fine(() -> String.format("Iteration %d: cpu=%.1f mem=%.1f req=%.0f",
					iteration, cpuValue, memoryValue, requestCount));
			pause(500);
		}
		getLogger().info("Data generation complete.");
	}

	/**
	 * Closes the metrics facade, flushing any remaining buffered data points
	 * to InfluxDB and CSV writers.
	 */
	@Override
	protected void onEnd() {
		if (metrics != null) {
			metrics.close();
		}
		getLogger().info("Metrics closed. Dashboard remains at http://localhost:3000");
	}

	/**
	 * Creates a Grafana dashboard with four panels using the programmatic API:
	 * two Time Series panels (CPU and Memory) and two Stat panels (current values).
	 * <p>
	 * If the connection is unavailable, logs a warning and returns gracefully.
	 */
	private void createDashboard() {
		if (connection == null) {
			return;
		}
		try {
			var dashboard = new GrafanaDashboard("MDK-grafana Example", connection);
			dashboard.addTimeSeriesPanel("CPU Usage", "cpu", "value");
			dashboard.addTimeSeriesPanel("Memory Usage", "memory", "value");
			dashboard.addStatPanel("Current CPU", "cpu", "value");
			dashboard.addStatPanel("Current Memory", "memory", "value");
			dashboard.save();
			dashboard.openInBrowser();
		} catch (Exception e) {
			getLogger().warning(() -> "Dashboard creation failed: " + e.getMessage());
		}
	}

	/**
	 * Computes a synthetic CPU value using a sinusoidal wave with random noise.
	 * <p>
	 * The formula produces values roughly in the range [0, 80]:
	 * {@code 30 + 40 * sin(i * 0.1) + random * 10}.
	 *
	 * @param iteration the current loop iteration index
	 * @return the computed CPU value
	 */
	private double computeCpuValue(int iteration) {
		return 30 + 40 * Math.sin(iteration * 0.1) + Math.random() * 10;
	}

	/**
	 * Computes a synthetic memory value using a cosine wave with random noise.
	 * <p>
	 * The formula produces values roughly in the range [25, 75]:
	 * {@code 50 + 20 * cos(i * 0.15) + random * 5}.
	 *
	 * @param iteration the current loop iteration index
	 * @return the computed memory value
	 */
	private double computeMemoryValue(int iteration) {
		return 50 + 20 * Math.cos(iteration * 0.15) + Math.random() * 5;
	}

	/**
	 * Computes a synthetic request count using a sinusoidal wave with random noise.
	 * <p>
	 * The formula produces non-negative values roughly in the range [0, 170]:
	 * {@code max(0, 100 + 50 * sin(i * 0.2) + random * 20)}.
	 *
	 * @param iteration the current loop iteration index
	 * @return the computed request count (always non-negative)
	 */
	private double computeRequestCount(int iteration) {
		return Math.max(0, 100 + 50 * Math.sin(iteration * 0.2) + Math.random() * 20);
	}

	/**
	 * Entry point — launches this example agent.
	 *
	 * @param args command-line arguments (unused)
	 */
	public static void main(String[] args) {
		executeThisAgent();
	}
}
