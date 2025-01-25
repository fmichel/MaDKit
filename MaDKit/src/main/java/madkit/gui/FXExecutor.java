/*******************************************************************************
 * MaDKit - Multi-agent systems Development Kit 
 * 
 * Copyright (c) 1998-2025 Fabien Michel, Olivier Gutknecht, Jacques Ferber...
 * 
 * This software is a computer program whose purpose is to
 * provide a lightweight Java API for developing and simulating 
 * Multi-Agent Systems (MAS) using an organizational perspective.
 *
 * This software is governed by the CeCILL-C license under French law and
 * abiding by the rules of distribution of free software.You can use,
 * modify and/ or redistribute the software under the terms of the CeCILL-C
 * license as circulated by CEA, CNRS and INRIA at the following URL
 * "http://www.cecill.info".
 *
 * As a counterpart to the access to the source code and rights to copy,
 * modify and redistribute granted by the license, users are provided only
 * with a limited warranty and the software's author, the holder of the
 * economic rights, and the successive licensors have only limited
 * liability.
 *
 * In this respect, the user's attention is drawn to the risks associated
 * with loading, using, modifying and/or developing or reproducing the
 * software by the user in light of its specific status of free software,
 * that may mean that it is complicated to manipulate, and that also
 * therefore means that it is reserved for developers and experienced
 * professionals having in-depth computer knowledge. Users are therefore
 * encouraged to load and test the software's suitability as regards their
 * requirements in conditions enabling the security of their systems and/or
 * data to be ensured and, more generally, to use and operate it in the
 * same conditions as regards security.
 *
 * The fact that you are presently reading this means that you have had
 * knowledge of the CeCILL-C license and that you accept its terms.
 *******************************************************************************/
package madkit.gui;

import java.util.concurrent.FutureTask;
import java.util.logging.Level;
import java.util.logging.Logger;

import javafx.application.Platform;
import madkit.internal.FXInstance;
import madkit.kernel.AgentInterruptedException;
import madkit.kernel.Madkit;

/**
 * Provides a wrapper around the JavaFX {@link Platform} class with additional
 * functionality for managing calls to the JavaFX Platform.
 * 
 * Since MaDKit is not a JavaFX application per se, it needs to manage the JavaFX Platform
 * lifecycle. So, the JavaFX Platform is automatically started when the environment is not
 * in headless mode. This class provides methods to run actions on the JavaFX application
 * thread. Especially, it provides a way to run actions on the JavaFX thread and wait for
 * completion. This is useful when performing rendering actions in simulation.
 *
 * @version 6.0
 *
 * @see javafx.application.Platform
 */
public class FXExecutor {

	/** The logger for the FXExecutor. */
	private static final Logger logger = Madkit.MDK_LOGGER;

	/**
	 * Constructs a new FXExecutor and sets the singleton instance.
	 */
	private FXExecutor() {
		throw new IllegalStateException("Utility class");
	}

	/**
	 * Runs the specified {@link Runnable} on the JavaFX application thread and waits for
	 * completion.
	 *
	 * @param action the {@link Runnable} to run
	 * @throws NullPointerException if {@code action} is {@code null}
	 */
	public static void runAndWait(Runnable action) {
		if (FXInstance.isStarted()) {
			try {
				if (Platform.isFxApplicationThread()) {
					action.run();
				} else {
					FutureTask<Object> futureTask = new FutureTask<>(action, null);
					Platform.runLater(futureTask);
					futureTask.get();
				}
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				throw new AgentInterruptedException();
			} catch (Exception e) {
				logger.log(Level.SEVERE, "FXExecutor exception running ");
				if (e.getCause() != null) {
					throw new RuntimeException(e.getCause());
				} else {
					throw new RuntimeException(e);
				}
			}
		} else {
			logNotStartedException();
		}
	}

	/**
	 * Runs the specified {@link Runnable} on the JavaFX application thread at some
	 * unspecified time in the future.
	 *
	 * @param action the {@link Runnable} to run
	 */
	public static void runLater(Runnable action) {
		if (FXInstance.isStarted()) {
			Platform.runLater(action);
		} else {
			logNotStartedException();
		}
	}

	private static void logNotStartedException() {
		logger.log(Level.WARNING, "FX not started: cannot run action! Launch MaDKit first!");
	}

	/**
	 * Returns whether the application is running in headless mode.
	 *
	 * @return {@code true} if the application is in headless mode, {@code false} otherwise
	 */
	public static boolean isHeadlessMode() {
		return FXInstance.isHeadlessMode();
	}

	/**
	 * Returns whether the JavaFX Platform has started.
	 * 
	 * @return {@code true} if the JavaFX Platform has started
	 */
	public static boolean isStarted() {
		return FXInstance.isStarted();
	}
}
