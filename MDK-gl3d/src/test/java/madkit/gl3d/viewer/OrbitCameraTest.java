package madkit.gl3d.viewer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.testng.annotations.Test;

public class OrbitCameraTest {
    @Test
    public void zoomAndFieldOfViewRemainClamped() {
        // Given
        OrbitCamera camera = new OrbitCamera();

        // When
        camera.zoom(-1_000);
        camera.fieldOfView(1);

        // Then
        assertThat(camera.distance()).isEqualTo(1);
        assertThat(camera.fieldOfView()).isEqualTo(10);
    }

    @Test
    public void cameraProducesFiniteMatricesAfterOrbitAndPan() {
        // Given
        OrbitCamera camera = new OrbitCamera();

        // When
        camera.orbit(3, 2);
        camera.pan(5, -2);
        Matrix4f view = camera.viewMatrix();
        Matrix4f projection = camera.projectionMatrix(16f / 9f);

        // Then
        assertThat(view.isFinite()).isTrue();
        assertThat(projection.isFinite()).isTrue();
        assertThat(camera.target()).isNotEqualTo(new Vector3f());
    }

    @Test
    public void projectionRejectsInvalidAspectRatio() {
        // Given
        OrbitCamera camera = new OrbitCamera();

        // When / Then
        assertThatIllegalArgumentException().isThrownBy(() -> camera.projectionMatrix(0));
    }
}
