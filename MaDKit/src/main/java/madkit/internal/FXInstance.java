package madkit.internal;

import java.awt.GraphicsEnvironment;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Level;
import java.util.logging.Logger;

import javafx.application.Platform;
import madkit.gui.FXExecutor;

public class FXInstance {

	static {
		headlessMode = GraphicsEnvironment.isHeadless();
	}

	/** Indicates whether the JavaFX application has started. */
	private static boolean isStarted = false;
	/** Indicates whether the application is running in headless mode. */
	private static boolean headlessMode;

	private FXInstance() {
		throw new IllegalStateException("Utility class");
	}

	/**
	 * Starts the JavaFX application if it is not already started and not in
	 * headless mode.
	 * 
	 * @param logger the logger to use
	 */
	public static synchronized void startFX(Logger logger) {
		if (!(FXInstance.isStarted() || FXInstance.isHeadlessMode())) {
			Platform.setImplicitExit(false);
			CountDownLatch latch = new CountDownLatch(1);
			Platform.startup(() -> {
				FXInstance.setStarted(true);
				latch.countDown();
				logger.log(Level.INFO, () -> "FX Platform Started!");
			});
			try {
				latch.await();
			} catch (InterruptedException e) {
				logger.log(Level.WARNING, "FX start interrupted!", e);
				Thread.currentThread().interrupt();
			}
		}
	}

	/**
	 * If set to {@code true}, the application will run in headless mode and will
	 * not start the JavaFX application. It will inhibit the JavaFX initialization
	 * and calls to {@link FXExecutor#runLater(Runnable)} and
	 * {@link FXExecutor#runAndWait(Runnable)} This is useful for running the
	 * application in environments that do not support JavaFX.
	 *
	 * @param inhibit {@code true} to set headless mode
	 */
	public static void setHeadlessMode(boolean inhibit) {
		headlessMode = inhibit;
	}

	/**
	 * @return the isStarted
	 */
	public static boolean isStarted() {
		return isStarted;
	}

	/**
	 * @param isStarted the isStarted to set
	 */
	public static void setStarted(boolean isStarted) {
		FXInstance.isStarted = isStarted;
	}

	/**
	 * @return the headlessMode
	 */
	public static boolean isHeadlessMode() {
		return headlessMode;
	}

}
