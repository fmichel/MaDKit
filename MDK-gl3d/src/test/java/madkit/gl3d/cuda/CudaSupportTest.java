package madkit.gl3d.cuda;

import static org.assertj.core.api.Assertions.assertThat;

import org.testng.annotations.Test;

public class CudaSupportTest {
    @Test
    public void capabilityCheckIsSafeWithoutNativeCuda() {
        // Given
        String previous = System.getProperty("madkit.gl3d.cuda.disabled");
        try {
            // When
            System.setProperty("madkit.gl3d.cuda.disabled", "true");

            // Then
            assertThat(CudaSupport.isAvailable()).isFalse();
            assertThat(CudaSupport.availabilityDescription()).contains("disabled");
        } finally {
            if (previous == null) {
                System.clearProperty("madkit.gl3d.cuda.disabled");
            } else {
                System.setProperty("madkit.gl3d.cuda.disabled", previous);
            }
        }
    }
}
