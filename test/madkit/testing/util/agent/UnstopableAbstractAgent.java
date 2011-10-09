/*
 * Copyright 1997-2011 Fabien Michel, Olivier Gutknecht, Jacques Ferber
 * 
 * This file is part of MadKit.
 * 
 * MadKit is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 * 
 * MadKit is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with MadKit. If not, see <http://www.gnu.org/licenses/>.
 */
package madkit.testing.util.agent;

import madkit.kernel.AbstractAgent;
import madkit.kernel.Agent;
import madkit.kernel.AgentAddress;
import madkit.kernel.Madkit;
import madkit.kernel.Madkit.LevelOption;

/**
 * @author Fabien Michel
 * @since MadKit 5.0.0.9
 * @version 0.9
 * 
 */
public class UnstopableAbstractAgent extends AbstractAgent
{
	protected void activate() {
		int i = 0;
		while(i<990000000){
			i++;
			if(i%10000000 == 0)
				if(logger != null)
					logger.info("activate "+getState()+" "+i);
		}
	}
	
	/**
	 * 
	 */
	protected void end() {
		int i = 0;
		while(true){
			i++;
			if(i%10000000 == 0){
				if(logger != null)
					logger.info("end "+getState()+" "+i);
			}
		}
	}


	public static void main(String[] args) {
		String[] argss = {LevelOption.agentLogLevel.toString(),"ALL",LevelOption.kernelLogLevel.toString(),"ALL","--launchAgents",UnstopableAbstractAgent.class.getName(),",true"};
		Madkit.main(argss);		
	}

}