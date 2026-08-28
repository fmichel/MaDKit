package madkit.gl3d.bees;

import static org.assertj.core.api.Assertions.assertThat;

import org.testng.annotations.Test;

public class ColonySnapshotExchangeTest {
    @Test
    public void latestSnapshotWinsWithoutQueueGrowth() {
        // Given
        ColonySnapshotExchange exchange = new ColonySnapshotExchange();
        BeeColonyModel model = new BeeColonyModel(3, 2, 1);
        ColonySnapshot first = model.colonySnapshot();
        model.advance();
        ColonySnapshot second = model.colonySnapshot();

        // When
        exchange.publish(first);
        exchange.publish(second);

        // Then
        assertThat(exchange.latest()).isSameAs(second);
        assertThat(exchange.latestOrEmpty().tick()).isEqualTo(1);
    }
}
