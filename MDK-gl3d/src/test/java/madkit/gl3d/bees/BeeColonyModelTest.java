package madkit.gl3d.bees;

import static org.assertj.core.api.Assertions.assertThat;

import org.testng.annotations.Test;

public class BeeColonyModelTest {
    @Test
    public void sameSeedProducesSameSnapshots() {
        // Given
        BeeColonyModel first = new BeeColonyModel(42, 12, 4);
        BeeColonyModel second = new BeeColonyModel(42, 12, 4);

        // When
        for (int i = 0; i < 50; i++) {
            first.advance();
            second.advance();
        }

        // Then
        assertThat(first.tick()).isEqualTo(50);
        assertThat(first.snapshot()).isEqualTo(second.snapshot());
        assertThat(first.foodStore()).isEqualTo(second.foodStore());
    }

    @Test
    public void beesEventuallyLeaveHiveAndForage() {
        // Given
        BeeColonyModel model = new BeeColonyModel(7, 1, 1);

        // When
        for (int i = 0; i < 100; i++) model.advance();

        // Then
        assertThat(model.snapshot().get(0).state()).isNotEqualTo(BeeState.IN_HIVE);
    }

    @Test
    public void emptyFlowerFieldStillAdvancesTime() {
        // Given
        BeeColonyModel model = new BeeColonyModel(1, 3, 0);

        // When
        model.advance();

        // Then
        assertThat(model.tick()).isEqualTo(1);
    }

    @Test
    public void snapshotPublishesDeterministicVelocity() {
        // Given
        BeeColonyModel first = new BeeColonyModel(7, 1, 1);
        BeeColonyModel second = new BeeColonyModel(7, 1, 1);

        // When
        first.advance();
        second.advance();

        // Then
        assertThat(first.snapshot().get(0).velocity()).isEqualTo(second.snapshot().get(0).velocity());
        assertThat(first.snapshot().get(0).velocity()).isNotEqualTo(new Vec3(0, 0, 0));
    }

    @Test
    public void flowerlessSnapshotPublishesZeroVelocity() {
        // Given
        BeeColonyModel model = new BeeColonyModel(1, 1, 0);

        // When
        model.advance();

        // Then
        assertThat(model.snapshot().get(0).velocity()).isEqualTo(new Vec3(0, 0, 0));
    }
}