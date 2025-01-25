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
package madkit.internal;

import java.awt.GraphicsEnvironment;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javafx.application.Platform;
import madkit.gui.FXExecutor;

/**
 * The Class FXInstance.
 */
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
	 * Starts the JavaFX application if it is not already started and not in headless mode.
	 * 
	 * @param logger the logger to use
	 */
	public static synchronized void startFX(Logger logger) {
		if (!(FXInstance.isStarted() || FXInstance.isHeadlessMode())) {
			Platform.setImplicitExit(false);
			CompletableFuture<Void> fxStart = CompletableFuture.runAsync(() -> {
				try {
					Platform.startup(() -> {
						logger.log(Level.INFO, () -> "FX Platform Started!");
					});
				} catch (IllegalStateException e) {
					if (e.getMessage().contains("Toolkit already initialized")) {
						logger.log(Level.INFO, () -> "FX Platform already started!");
					} else {
						logger.log(Level.WARNING, () -> "FX start error: ");
						throw e;
					}
				}
			});
			try {
				fxStart.get();
				FXInstance.setStarted(true);
			} catch (ExecutionException e) {
				logger.log(Level.WARNING, e, () -> "FX start error: " + e.getMessage());
			} catch (InterruptedException e) {
				logger.log(Level.WARNING, e, () -> "FX start interrupted: " + e.getMessage());
				Thread.currentThread().interrupt();
			}
		}
	}

	/**
	 * If set to {@code true}, the application will run in headless mode and will not start
	 * the JavaFX application. It will inhibit the JavaFX initialization and calls to
	 * {@link FXExecutor#runLater(Runnable)} and {@link FXExecutor#runAndWait(Runnable)} This
	 * is useful for running the application in environments that do not support JavaFX.
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
