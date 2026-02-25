open module madkit.grafana {
	requires madkit.base;
	requires java.net.http;
	requires java.logging;
	requires java.desktop;

	exports madkit.grafana;
	exports madkit.grafana.metrics;
	exports madkit.grafana.dashboard;
	exports madkit.grafana.docker;
	exports madkit.grafana.example;
}