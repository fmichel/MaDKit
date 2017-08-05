/*
 * Copyright or © or Copr. Fabien Michel, Olivier Gutknecht, Jacques Ferber (1997)
 * 
 * fmichel@lirmm.fr
 * olg@no-distance.net
 * ferber@lirmm.fr
 * 
 * This software is a computer program whose purpose is to
 * provide a lightweight Java library for designing and simulating Multi-Agent Systems (MAS).
 * This software is governed by the CeCILL-C license under French law and
 * abiding by the rules of distribution of free software.  You can  use,
 * modify and/ or redistribute the software under the terms of the CeCILL-C
 * license as circulated by CEA, CNRS and INRIA at the following URL
 * "http://www.cecill.info".
 * As a counterpart to the access to the source code and  rights to copy,
 * modify and redistribute granted by the license, users are provided only
 * with a limited warranty  and the software's author,  the holder of the
 * economic rights,  and the successive licensors  have only  limited
 * liability.
 * 
 * In this respect, the user's attention is drawn to the risks associated
 * with loading,  using,  modifying and/or developing or reproducing the
 * software by the user in light of its specific status of free software,
 * that may mean  that it is complicated to manipulate,  and  that  also
 * therefore means  that it is reserved for developers  and  experienced
 * professionals having in-depth computer knowledge. Users are therefore
 * encouraged to load and test the software's suitability as regards their
 * requirements in conditions enabling the security of their systems and/or
 * data to be ensured and,  more generally, to use and operate it in the
 * same conditions as regards security.
 * The fact that you are presently reading this means that you have had
 * knowledge of the CeCILL-C license and that you accept its terms.
 */
package com.distrimind.madkit.simulation;

import com.distrimind.madkit.kernel.Activator;
import com.distrimind.madkit.kernel.Probe;
import com.distrimind.madkit.kernel.Scheduler;
import com.distrimind.madkit.kernel.Watcher;

/**
 * This unchecked exception could be used by activators and probes to indicate
 * and propagate the exception thrown by an agent during the use of an
 * {@link Activator} or {@link Probe}. Doing so, this exception will interrupt
 * the life cycle of the related {@link Scheduler} or {@link Watcher} if not
 * caught before, so displaying the corresponding stack trace.
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
