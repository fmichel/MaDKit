package madkit.gui.fx;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.FutureTask;
import java.util.logging.Level;
import java.util.logging.Logger;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;
import madkit.kernel.AgentInterruptedException;

public class FXManager extends Application {

	private static FXManager fxApplicationInstance;
	public static final CountDownLatch latch = new CountDownLatch(1);
	private static boolean isStarted = false;
	private static boolean headlessMode;
	private static final Logger FX_ROOT_LOGGER = Logger.getLogger("[FX Manager] ");

	static FXManager waitForFxInitialization() {
		try {
			latch.await();
		} catch (InterruptedException e) {
			FX_ROOT_LOGGER.log(Level.WARNING, " () -> Interrupted!", e);
			Thread.currentThread().interrupt();
		}
		return fxApplicationInstance;
	}

	public static void setFxApplicationInstance(FXManager fxa) {
		fxApplicationInstance = fxa;
		latch.countDown();
	}

	public FXManager() {
		setFxApplicationInstance(this);
	}

	@Override
	public void start(Stage primaryStage) throws Exception {
		primaryStage.setTitle("MDK desktop to be done...");
	}

	public static synchronized void startFX() {
		if (!(isStarted || headlessMode)) {
			FX_ROOT_LOGGER.log(Level.FINEST, () -> "FX Started!");
			Platform.setImplicitExit(true);
			Thread thread = new Thread(() -> Application.launch(FXManager.class));
			isStarted = true;
			thread.setDaemon(true);
			thread.start();
			FXManager.waitForFxInitialization();
		}
	}

	/**
	 * Runs the specified {@link Runnable} on the JavaFX application thread and
	 * waits for completion.
	 *
	 * @param action the {@link Runnable} to run
	 * @throws NullPointerException if {@code action} is {@code null}
	 */
	public static void runAndWait(Runnable action) {
		if (isStarted) {
			try {
				if (Platform.isFxApplicationThread()) {
					action.run();
				} else {
					FutureTask<Object> futureTask = new FutureTask<>(action, null);
					Platform.runLater(futureTask);
					futureTask.get();
				}
			} catch (InterruptedException e) {
				throw new AgentInterruptedException();
			} catch (Exception e) {
				FX_ROOT_LOGGER.log(Level.WARNING, "FX problem...", e);
			}
		}
	}

	public static void runLater(Runnable action) {
		if (isStarted) {
			Platform.runLater(action);
		}
	}

	/**
	 * @param b
	 */
	public static void setHeadlessMode(boolean b) {
		headlessMode = b;
	}

	/**
	 * @return the headlessMode
	 */
	public static boolean isHeadlessMode() {
		return headlessMode;
	}

}