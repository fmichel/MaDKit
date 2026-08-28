package madkit.gl3d.bees;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

/** Latest-value snapshot exchange; a slow renderer never creates an unbounded queue. */
public final class ColonySnapshotExchange {
    private final AtomicReference<ColonySnapshot> latest = new AtomicReference<>();

    public void publish(ColonySnapshot snapshot) {
        latest.set(Objects.requireNonNull(snapshot, "snapshot"));
    }

    public ColonySnapshot latest() {
        return latest.get();
    }

    public ColonySnapshot latestOrEmpty() {
        ColonySnapshot snapshot = latest.get();
        return snapshot == null ? new ColonySnapshot(0, java.util.List.of(), java.util.List.of(), 0) : snapshot;
    }
}
