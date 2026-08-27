package madkit.gl3d;

import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

import org.testng.annotations.Test;

public class GLWindowTest {
    @Test
    public void nonPositiveDimensionsAreRejectedBeforeNativeInitialization() {
        // Given / When / Then
        assertThatIllegalArgumentException().isThrownBy(() -> new GLWindow("test", 0, 100, false, () -> { }));
        assertThatIllegalArgumentException().isThrownBy(() -> new GLWindow("test", 100, -1, false, () -> { }));
    }
}
