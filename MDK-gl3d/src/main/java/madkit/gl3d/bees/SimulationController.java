package madkit.gl3d.bees;

import java.util.Objects;
import java.util.concurrent.ConcurrentLinkedQueue;

/** Headless lifecycle controller with bounded, deterministic command application. */
public final class SimulationController {
    public enum State { STOPPED, RUNNING, PAUSED }
    private static final double SIMULATION_STEP_SECONDS = 1.0 / 60.0;

    private final ConcurrentLinkedQueue<Command> commands = new ConcurrentLinkedQueue<>();
    private BeeColonyModel model;
    private State state = State.STOPPED;
    private double speed = 1.0;
    private boolean stepRequested;
    private long lastTickDurationNanos;

    public SimulationController(BeeColonyModel model) {
        this.model = Objects.requireNonNull(model, "model");
    }

    public synchronized State state() { return state; }
    public synchronized double speed() { return speed; }
    public synchronized BeeColonyModel model() { return model; }
    public synchronized double simulatedTimeSeconds() {
        return model.tick() * SIMULATION_STEP_SECONDS;
    }
    public synchronized double lastTickDurationMicros() {
        return lastTickDurationNanos / 1_000.0;
    }

    public void start() { commands.offer(new Start()); }
    public void pause() { commands.offer(new Pause()); }
    public void resume() { commands.offer(new Resume()); }
    public void stop() { commands.offer(new Stop()); }
    public void step() { commands.offer(new Step()); }
    public synchronized void reset(BeeColonyModel replacement) {
        apply(new Reset(Objects.requireNonNull(replacement, "replacement")));
    }
    public void speed(double value) {
        if (!Double.isFinite(value) || value <= 0 || value > 100) throw new IllegalArgumentException("speed must be in (0, 100]");
        commands.offer(new Speed(value));
    }

    /** Applies queued commands and advances at most one model tick. Call once per scheduler tick. */
    public synchronized void update() {
        Command command;
        while ((command = commands.poll()) != null) apply(command);
        if (state == State.RUNNING || stepRequested) {
            long startNanos = System.nanoTime();
            model.advance();
            lastTickDurationNanos = System.nanoTime() - startNanos;
            stepRequested = false;
        }
    }

    private void apply(Command command) {
        switch (command) {
            case Start ignored -> { if (state == State.STOPPED || state == State.PAUSED) state = State.RUNNING; }
            case Pause ignored -> { if (state == State.RUNNING) state = State.PAUSED; }
            case Resume ignored -> { if (state == State.PAUSED) state = State.RUNNING; }
            case Stop ignored -> { state = State.STOPPED; stepRequested = false; }
            case Step ignored -> { if (state == State.PAUSED) stepRequested = true; }
            case Reset reset -> { model = reset.model; state = State.STOPPED; stepRequested = false; lastTickDurationNanos = 0; }
            case Speed speedCommand -> speed = speedCommand.value;
        }
    }

    private sealed interface Command permits Start, Pause, Resume, Stop, Step, Reset, Speed { }
    private record Start() implements Command { }
    private record Pause() implements Command { }
    private record Resume() implements Command { }
    private record Stop() implements Command { }
    private record Step() implements Command { }
    private record Reset(BeeColonyModel model) implements Command { }
    private record Speed(double value) implements Command { }
}