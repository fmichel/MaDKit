
package madkit.simulation;

import madkit.kernel.Scheduler;
import madkit.kernel.Activator;
import madkit.kernel.Probe;
import madkit.kernel.Watcher;

/**
 * This unchecked exception could be used by activators and probes to indicate
 * and propagate the exception thrown by an agent during the use of an
 * {@link Activator} or {@link Probe}. Doing so, this exception will interrupt
 * the life cycle of the related {@link Scheduler} or {@link Watcher} if
 * not caught before, so displaying the corresponding stack trace.
 * 
 * @author Fabien Michel
 * @since MaDKit 5.0.0.15
 * @version 0.9
 * 
 */
public class SimulationException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2815963785558410975L;

	public SimulationException(String message, Throwable cause) {
		super(message, cause);
	}

}
