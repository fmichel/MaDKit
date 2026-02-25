package madkit.grafana.dashboard;

import static org.assertj.core.api.Assertions.assertThat;

import org.testng.annotations.Test;

public class GrafanaConnectionTest {

    @Test
    public void givenConnection_whenGetBaseUrl_thenReturnsConfiguredUrl() {
        // Given
        var conn = new GrafanaConnection("http://my-grafana:3000", "admin", "admin");

        // When
        String url = conn.getBaseUrl();

        // Then
        assertThat(url).isEqualTo("http://my-grafana:3000");
    }

    @Test
    public void givenValidResponseJson_whenExtractUrl_thenReturnsUrlPath() {
        // Given
        var conn = new GrafanaConnection("http://localhost:3000", "admin", "admin");
        String json = "{\"id\":1,\"uid\":\"abc\",\"url\":\"/d/abc/my-dashboard\",\"status\":\"success\"}";

        // When
        String url = conn.extractUrlFromResponse(json);

        // Then
        assertThat(url).isEqualTo("/d/abc/my-dashboard");
    }

    @Test
    public void givenResponseWithoutUrl_whenExtractUrl_thenReturnsNull() {
        // Given
        var conn = new GrafanaConnection("http://localhost:3000", "admin", "admin");
        String json = "{\"message\":\"error\"}";

        // When
        String url = conn.extractUrlFromResponse(json);

        // Then
        assertThat(url).isNull();
    }
}
