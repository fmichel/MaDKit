package madkit.gl3d.cuda;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;

import java.util.concurrent.atomic.AtomicInteger;

import org.testng.annotations.Test;

public class InteropBufferTest {
    @Test
    public void mapClosesExactlyOnceAndPublishesDevicePointer() {
        // Given
        AtomicInteger closeCount = new AtomicInteger();
        InteropBuffer buffer = new InteropBuffer(7, ignored -> new InteropBuffer.Mapping() {
            public long devicePointer() { return 42; }
            public void close() { closeCount.incrementAndGet(); }
        });

        // When
        InteropBuffer.Mapping mapping = buffer.map();

        // Then
        assertThat(mapping.devicePointer()).isEqualTo(42);
        mapping.close();
        mapping.close();
        assertThat(closeCount).hasValue(1);
    }

    @Test
    public void secondMapIsRejectedUntilFirstMappingIsClosed() {
        // Given
        InteropBuffer buffer = new InteropBuffer(3, ignored -> new InteropBuffer.Mapping() {
            public long devicePointer() { return 1; }
            public void close() { }
        });
        InteropBuffer.Mapping mapping = buffer.map();

        // When / Then
        assertThatIllegalStateException().isThrownBy(buffer::map);
        mapping.close();
        buffer.close();
        assertThatIllegalStateException().isThrownBy(buffer::map);
    }
}
