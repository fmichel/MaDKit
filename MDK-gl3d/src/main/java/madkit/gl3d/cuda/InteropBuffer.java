package madkit.gl3d.cuda;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Coordinates ownership of a shared OpenGL buffer during a CUDA frame.
 *
 * <p>The actual JCuda registration is deliberately injected through
 * {@link Mapper}; this keeps the GL module usable on non-NVIDIA machines and
 * makes map/unmap ordering testable without a native driver.</p>
 */
public final class InteropBuffer implements AutoCloseable {
    @FunctionalInterface
    public interface Mapper {
        Mapping map(long glBufferId);
    }

    public interface Mapping extends AutoCloseable {
        long devicePointer();
        @Override
        void close();
    }

    private final long glBufferId;
    private final Mapper mapper;
    private final AtomicBoolean closed = new AtomicBoolean();
    private boolean mapped;

    public InteropBuffer(long glBufferId, Mapper mapper) {
        if (glBufferId <= 0) {
            throw new IllegalArgumentException("OpenGL buffer id must be positive");
        }
        this.glBufferId = glBufferId;
        this.mapper = Objects.requireNonNull(mapper, "mapper");
    }

    public long glBufferId() {
        return glBufferId;
    }

    /** Maps the buffer once; the returned mapping must be closed before another map. */
    public synchronized Mapping map() {
        ensureOpen();
        if (mapped) {
            throw new IllegalStateException("Interop buffer is already mapped");
        }
        Mapping result = Objects.requireNonNull(mapper.map(glBufferId), "mapper returned null");
        mapped = true;
        return new Mapping() {
            private boolean released;

            @Override
            public long devicePointer() {
                if (released) {
                    throw new IllegalStateException("Mapping has been released");
                }
                return result.devicePointer();
            }

            @Override
            public void close() {
                synchronized (InteropBuffer.this) {
                    if (!released) {
                        released = true;
                        try {
                            result.close();
                        } finally {
                            mapped = false;
                        }
                    }
                }
            }
        };
    }

    @Override
    public synchronized void close() {
        if (mapped) {
            throw new IllegalStateException("Cannot close a mapped interop buffer");
        }
        closed.set(true);
    }

    private void ensureOpen() {
        if (closed.get()) {
            throw new IllegalStateException("Interop buffer is closed");
        }
    }
}
