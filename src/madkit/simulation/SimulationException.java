/*
 * Copyright 1997-2012 Fabien Michel, Olivier Gutknecht, Jacques Ferber
 * 
 * This file is part of MaDKit.
 * 
 * MaDKit is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 * 
 * MaDKit is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with MaDKit. If not, see <http://www.gnu.org/licenses/>.
 */
package madkit.simulation;

import madkit.kernel.Activator;
import madkit.kernel.Probe;
import madkit.kernel.Scheduler;
import madkit.kernel.Watcher;

/**
 * This unchecked exception could be used by activators and probes to
 * indicate and propagate the exception thrown by an agent
 * during the use of an {@link Activator} or {@link Probe}.
 * Doing so, this exception will interrupt the life cycle of the related
 * {@link Scheduler} or {@link Watcher} if not caught before, so displaying
 * the corresponding stack trace.
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
