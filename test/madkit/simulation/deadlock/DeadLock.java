/*
 * Copyright 1997-2012 Fabien Michel, Olivier Gutknecht, Jacques Ferber
 * 
 * This file is part of MaDKit.
 * 
 * MaDKit is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * MaDKit is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with MaDKit. If not, see <http://www.gnu.org/licenses/>.
 */
package madkit.simulation.deadlock;

import static madkit.kernel.JunitMadkit.COMMUNITY;
import static madkit.kernel.JunitMadkit.GROUP;
import static madkit.kernel.JunitMadkit.ROLE;
import madkit.kernel.AbstractAgent;
import madkit.kernel.Madkit.LevelOption;
import madkit.kernel.Scheduler;
import madkit.simulation.activator.GenericBehaviorActivator;
import madkit.testing.util.agent.SimulatedAgent;


/**
 * @author Fabien Michel
 * @since MaDKit 5.0.0.15
 * @version 0.9
 * 
 */
public class DeadLock extends Scheduler {
	
	/**
	 * 
	 */
	private static final long	serialVersionUID	= 1L;
	private GenericBehaviorActivator<AbstractAgent> simulatedAgents;
	
	@Override
	protected void activate() {
		launchAgent(new PerceptionAgent());
		simulatedAgents = new GenericBehaviorActivator<AbstractAgent>(
				COMMUNITY, 
				GROUP, 
				ROLE, 
				"doIt"){
			@Override 
			protected void adding(AbstractAgent theAgent) {
				if(theAgent.hashCode() < 10){
					launchAgent(new SimulatedAgent());
				}
			}
		};
		addActivator(simulatedAgents);
		launchAgent(new SimulatedAgent());
	}
	
	@Override
	public void doSimulationStep() {
		super.doSimulationStep();
		if(getGVT() > 5){
			killAgent(simulatedAgents.getCurrentAgentsList().get(0));
		}
	}
	
	public static void main(String[] args) {
		String[] myArgs = {LevelOption.kernelLogLevel.toString(),"ALL"};
		AbstractAgent.executeThisAgent(myArgs);
	}

}
