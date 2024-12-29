/*
This software is a computer program whose purpose is to 
provide a lightweight Java library for designing and simulating Multi-Agent Systems (MAS).

This software is governed by the [CeCILL|CeCILL-B|CeCILL-C] license under French law and
abiding by the rules of distribution of free software.  You can  use, 
modify and/ or redistribute the software under the terms of the [CeCILL|CeCILL-B|CeCILL-C]
license as circulated by CEA, CNRS and INRIA at the following URL
"http://www.cecill.info". 

As a counterpart to the access to the source code and  rights to copy,
modify and redistribute granted by the license, users are provided only
with a limited warranty  and the software's author,  the holder of the
economic rights,  and the successive licensors  have only  limited
liability. 

In this respect, the user's attention is drawn to the risks associated
with loading,  using,  modifying and/or developing or reproducing the
software by the user in light of its specific status of free software,
that may mean  that it is complicated to manipulate,  and  that  also
therefore means  that it is reserved for developers  and  experienced
professionals having in-depth computer knowledge. Users are therefore
encouraged to load and test the software's suitability as regards their
requirements in conditions enabling the security of their systems and/or 
data to be ensured and,  more generally, to use and operate it in the 
same conditions as regards security. 

The fact that you are presently reading this means that you have had
knowledge of the CeCILL-C license and that you accept its terms.
 */

/**
 * 
 * Contains {@link madkit.kernel.Scheduler} and {@link madkit.kernel.Activator}
 * subclasses for simulation authoring.
 * <p>
 * Contains specialized Scheduler agents such as:
 * <ul>
 * <li>{@link madkit.simulation.scheduler.TickBasedScheduler}: A scheduler that
 * manages agents based on a tick-based simulation scheme.
 * <li>{@link madkit.simulation.scheduler.DateBasedDiscreteEventScheduler}: A scheduler that
 * manages agents based on a date-based representation of time. It is especially
 * useful for discrete-event simulation.
 * </ul>
 * <p>
 * This package also includes various types of activators such as:
 * </p>
 * <ul>
 * <li><code>MethodActivator</code>: Invokes a specific method on a group of
 * agents.</li>
 * <li><code>DateBasedDiscreteEventActivator</code>: Activates agents based on
 * discrete-event simulation schemes.</li>
 * <li><code>DiscreteEventAgentsActivator</code>: Manages agents using a
 * priority queue based on their next event date.</li>
 * </ul>
 *
 * These activators are used to control the behavior and scheduling of agents in
 * a simulation.
 *
 * @see madkit.simulation.scheduler.MethodActivator
 * @see madkit.simulation.scheduler.DateBasedDiscreteEventActivator
 * @see madkit.simulation.scheduler.DiscreteEventAgentsActivator
 *
 * @since MaDKit 5.3
 * @author Fabien Michel
 * @version 1.0
 * 
 */

package madkit.simulation.scheduler;
