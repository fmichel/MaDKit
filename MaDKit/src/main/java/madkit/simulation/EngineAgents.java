/*******************************************************************************
 * Copyright (c) 2022, MaDKit Team
 *
 * This software is a computer program whose purpose is to
 * provide a lightweight Java API for developing and simulating 
 * Multi-Agent Systems (MAS) using an organizational perspective.
 *
 * This software is governed by the CeCILL-C license under French law and
 * abiding by the rules of distribution of free software.  You can  use,
 * modify and/ or redistribute the software under the terms of the CeCILL-C
 * license as circulated by CEA, CNRS and INRIA at the following URL
 * "http://www.cecill.info".
 *
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
 *
 * The fact that you are presently reading this means that you have had
 * knowledge of the CeCILL-C license and that you accept its terms.
 *******************************************************************************/
package madkit.simulation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import madkit.kernel.Scheduler;
import madkit.simulation.scheduler.TickBasedScheduler;

/**
 * 
 * Annotation to specify the engine agents of a simulation.
 * <p>
 * The annotation can be used on a class that extends {@link SimulationEngine}.
 * The annotation specifies the scheduler, environment, model, and viewers of the
 * simulation. The annotation has the following attributes:
 * <ul>
 * <li>{@code scheduler} (optional): the scheduler class of the simulation. The
 * default value is {@link TickBasedScheduler}.
 * <li>{@code environment} (optional): the environment class of the simulation.
 * The default value is {@link Environment}.
 * <li>{@code model} (optional): the model class of the simulation. The default
 * value is {@link SimulationModel}.
 * <li>{@code viewers} (optional): the viewer classes of the simulation. The
 * default value is an empty array.
 * </ul>
 * 
 * 
 * @see SimulationEngine
 * @see SimulationModel
 * @see Environment
 * @see Scheduler
 * 
 * @since MaDKit 6.0
 */
@Inherited
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface EngineAgents {

	/**
	 * The scheduler class of the simulation.
	 * 
	 * @return the scheduler class.
	 */
	Class<? extends Scheduler<?>> scheduler() default TickBasedScheduler.class;

	/**
	 * The environment class of the simulation.
	 * 
	 * @return the environment class.
	 */
	Class<? extends Environment> environment() default Environment.class;


	/**
	 * The model class of the simulation.
	 * 
	 * @return the model class.
	 */
	Class<? extends SimulationModel> model() default SimulationModel.class;

	/**
	 * The viewers classes of the simulation.
	 * 
	 * @return the viewers classes.
	 */
	Class<? extends SimuAgent>[] viewers() default {};

}
