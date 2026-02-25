package madkit.grafana;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Path;

import org.testng.annotations.Test;

public class GrafanaConfigTest {

	@Test
	public void givenDefaults_whenCreated_thenAllFieldsArePopulated() {
		// Given / When
		var config = GrafanaConfig.defaults();

		// Then
		assertThat(config.grafanaUrl()).isEqualTo("http://localhost:3000");
		assertThat(config.grafanaUser()).isEqualTo("admin");
		assertThat(config.grafanaPassword()).isEqualTo("admin");
		assertThat(config.influxUrl()).isEqualTo("http://localhost:8086");
		assertThat(config.influxOrg()).isEqualTo("madkit");
		assertThat(config.influxBucket()).isEqualTo("madkit");
		assertThat(config.influxToken()).isEqualTo("madkit-dev-token");
		assertThat(config.workDir()).isEqualTo(Path.of("grafana-workdir"));
	}

	@Test
	public void givenCustomConfig_whenCreated_thenFieldsAreCustom() {
		// Given / When
		var config = new GrafanaConfig("http://remote:3000", "user", "pass", "http://remote:8086", "org", "bucket",
				"token", Path.of("/tmp/workdir"));

		// Then
		assertThat(config.grafanaUrl()).isEqualTo("http://remote:3000");
		assertThat(config.influxToken()).isEqualTo("token");
	}
}
