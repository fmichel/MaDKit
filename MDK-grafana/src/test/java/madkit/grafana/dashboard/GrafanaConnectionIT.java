package madkit.grafana.dashboard;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Integration test — requires a running Grafana instance.
 */
@Test(groups = "integration")
public class GrafanaConnectionIT {

    private GrafanaConnection connection;

    @BeforeClass
    public void setUp() {
        connection = new GrafanaConnection("http://localhost:3000", "admin", "admin");
    }

    @Test
    public void givenRunningGrafana_whenPostDashboard_thenNoException() {
        // Given
        String minimalJson = """
                {
                  "title": "IT Test Dashboard",
                  "panels": [],
                  "schemaVersion": 39
                }
                """;

        // When / Then
        assertThatCode(() -> connection.postDashboard(minimalJson))
                .as("posting a dashboard should not throw")
                .doesNotThrowAnyException();
    }

    @Test
    public void givenRunningGrafana_whenPostDashboard_thenUrlIsReturned() throws Exception {
        // Given
        String json = """
                {
                  "uid": "it-test-dashboard",
                  "title": "IT Test Dashboard",
                  "panels": [],
                  "schemaVersion": 39
                }
                """;

        // When
        String url = connection.postDashboard(json);

        // Then
        assertThat(url)
                .as("Grafana should return the dashboard URL")
                .isNotNull()
                .contains("/d/it-test-dashboard/");
    }
}
