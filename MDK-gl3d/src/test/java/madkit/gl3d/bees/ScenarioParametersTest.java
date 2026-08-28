package madkit.gl3d.bees;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.testng.annotations.Test;

public class ScenarioParametersTest {
    @Test
    public void parameterEditsClampAtSupportedBounds() {
        // Given
        ScenarioParameters parameters = ScenarioParameters.defaults();

        // When
        ScenarioParameters edited = parameters.withBeeCount(-1).withFlowerCount(20_000);

        // Then
        assertThat(edited.beeCount()).isZero();
        assertThat(edited.flowerCount()).isEqualTo(ScenarioParameters.MAX_FLOWER_COUNT);
    }

    @Test
    public void invalidInitialParametersAreRejected() {
        // Given / When / Then
        assertThatThrownBy(() -> new ScenarioParameters(1, -1, 1))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new ScenarioParameters(1, 1, ScenarioParameters.MAX_FLOWER_COUNT + 1))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
