
package madkit.gui.fx;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.FutureTask;
import java.util.logging.Level;
import java.util.logging.Logger;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;
import madkit.kernel.AgentInterruptedException;

/**
 * Manages the JavaFX application lifecycle and provides utility methods for
 * running tasks on the JavaFX application thread. This class is responsible for
 * initializing and managing the JavaFX environment for the application.
 *
 * @since MaDKit 5.1.1
 * @version 6.0
 *
 * @see javafx.application.Application
 * @see javafx.application.Platform
 * @see javafx.stage.Stage
 */
public class FXManager extends Application {

	/** The singleton instance of the FXManager. */
	private static FXManager fxApplicationInstance;
	/** A latch used to wait for the JavaFX application to initialize. */
	public static final CountDownLatch latch = new CountDownLatch(1);
	/** Indicates whether the JavaFX application has started. */
	private static boolean isStarted = false;
	/** Indicates whether the application is running in headless mode. */
	private static boolean headlessMode;
	/** The logger for the FXManager. */
	private static final Logger FX_ROOT_LOGGER = Logger.getLogger("[FX Manager] ");

	/**
	 * Waits for the JavaFX application to initialize.
	 *
	 * @return the singleton instance of the FXManager
	 */
	static FXManager waitForFxInitialization() {
		try {
			latch.await();
		} catch (InterruptedException e) {
			FX_ROOT_LOGGER.log(Level.WARNING, " () -> Interrupted!", e);
			Thread.currentThread().interrupt();
		}
		return fxApplicationInstance;
	}

	/**
	 * Sets the singleton instance of the FXManager.
	 *
	 * @param fxa the FXManager instance
	 */
	public static void setFxApplicationInstance(FXManager fxa) {
		fxApplicationInstance = fxa;
		latch.countDown();
	}

	/**
	 * Constructs a new FXManager and sets the singleton instance.
	 */
	public FXManager() {
		setFxApplicationInstance(this);
	}

	/**
	 * Starts the JavaFX application and sets the primary stage.
	 *
	 * @param primaryStage the primary stage for the JavaFX application
	 * @throws Exception if an error occurs during startup
	 */
	@Override
	public void start(Stage primaryStage) throws Exception {
		primaryStage.setTitle("MDK desktop to be done...");
	}

	/**
	 * Starts the JavaFX application if it is not already started and not in
	 * headless mode.
	 */
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

	/**
	 * Runs the specified {@link Runnable} on the JavaFX application thread at some
	 * unspecified time in the future.
	 *
	 * @param action the {@link Runnable} to run
	 */
	public static void runLater(Runnable action) {
		if (isStarted) {
			Platform.runLater(action);
		}
	}

	/**
	 * Sets whether the application is running in headless mode.
	 *
	 * @param b {@code true} to set headless mode, {@code false} otherwise
	 */
	public static void setHeadlessMode(boolean b) {
		headlessMode = b;
	}

	/**
	 * Returns whether the application is running in headless mode.
	 *
	 * @return {@code true} if the application is in headless mode, {@code false}
	 *         otherwise
	 */
	public static boolean isHeadlessMode() {
		return headlessMode;
	}
}
