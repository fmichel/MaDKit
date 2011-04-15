/*
 * Copyright 1997-2011 Fabien Michel, Olivier Gutknecht, Jacques Ferber
 * 
 * This file is part of MadKit.
 * 
 * MadKit is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * MadKit is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with MadKit. If not, see <http://www.gnu.org/licenses/>.
 */
package madkit.kernel;

import madkit.simulation.GenericBehaviorActivator;

/**
 * This class defines a tool for scheduling mechanism.
 * An activator is configured according to a community, a group and a role.
 * It could be used to activate a group of agents on a particular behavior (a method of the agent's class)
 * 
 * @author Fabien Michel
 * @author Olivier Gutknecht 
 * @since MadKit 2.0
 * @see Scheduler
 * @see GenericBehaviorActivator 
 * @version 5.0
 * 
 */
public class Activator<A extends AbstractAgent> extends Overlooker<A>{

	/**
	 * Builds a new Activator on the given CGR location of the
	 * artificial society. Once created, it has to be added by a {@link Scheduler} 
	 * agent using the {@link Scheduler#addActivator(Activator)} method.
	 * @param communityName
	 * @param groupName
	 * @param roleName
	 * @see Scheduler
	 */
	public Activator(String communityName, String groupName, String roleName) {
		super(communityName, groupName, roleName);
	}

	/**
	 * Called by a scheduler when this activator is activated.
	 * @see Scheduler#doSimulationStep()
	 */
	public void execute() {
	}

}
