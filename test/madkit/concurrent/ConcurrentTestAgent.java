/*
 * Copyright 2014 Fabien Michel
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
package madkit.concurrent;

import static madkit.kernel.JunitMadkit.COMMUNITY;
import static madkit.kernel.JunitMadkit.GROUP;
import static madkit.kernel.JunitMadkit.ROLE;
import static madkit.kernel.JunitMadkit.testFails;
import madkit.kernel.Agent;
import madkit.kernel.AgentAddress;


public class ConcurrentTestAgent extends Agent {

	private boolean	playing = false;

	@Override
	protected void live() {
//		setLogLevel(Level.INFO);
//		getLogger().setWarningLogLevel(Level.OFF);
		while (true) {
			if (Math.random() < .5) {
				if (createGroup(COMMUNITY, GROUP) == ReturnCode.SUCCESS) {
					final ReturnCode requestRole = requestRole(COMMUNITY, GROUP, ROLE);
					if (requestRole != ReturnCode.SUCCESS) {
						System.err.println(" req failed "+requestRole+ " "+this+" "+getAgentAddressIn(COMMUNITY, GROUP, ROLE));
						failed(requestRole);
						return;
					}
					playing  = true;
				} 
				else {
					requestRole(COMMUNITY, GROUP, ROLE);
				}
			}
			else {
				final AgentAddress agentAddressIn = getAgentAddressIn(COMMUNITY, GROUP, ROLE);
				if(playing){
//				if(agentAddressIn != null){ //TODO
					if (Math.random() < .5) {
//						System.err.println(getAgentAddressIn(COMMUNITY, GROUP, ROLE));
						final ReturnCode leaveRole = leaveRole(COMMUNITY, GROUP, ROLE);
						if (leaveRole != ReturnCode.SUCCESS) {
							System.err.println(this.toString()+checkAgentAddress(agentAddressIn));
							System.err.println(agentAddressIn+" leave role failed "+leaveRole+ " "+this+" "+getAgentAddressIn(COMMUNITY, GROUP, ROLE));
							failed(leaveRole);
//							System.exit(0);
							return;
						}
						playing  = false;
					} else {
//						System.err.println(getAgentAddressIn(COMMUNITY, GROUP, ROLE));
						final ReturnCode leaveGroup = leaveGroup(COMMUNITY, GROUP);
						if (leaveGroup != ReturnCode.SUCCESS){
							System.err.println(checkAgentAddress(agentAddressIn));
							System.err.println(agentAddressIn+" leave group failed "+leaveGroup+ " "+this+" "+getAgentAddressIn(COMMUNITY, GROUP, ROLE));
							failed(leaveGroup);
//							System.exit(0);
							return;
						}
						playing  = false;
					}
				}
			}
		}
	}

	/**
	 * @param code 
	 * 
	 */
	private void failed(ReturnCode code) {
		final AgentAddress agentAddressIn = getAgentAddressIn(COMMUNITY, GROUP, ROLE);
		testFails(new Exception(code.toString() + (agentAddressIn == null ? "not having the role !" : agentAddressIn.toString())));
	}
}

class ConcurrentTestAgentBis extends ConcurrentTestAgent{
	@Override
	protected void live() {
		while (true) {
			String groupName = GROUP+((int) (Math.random()*10));
			if (Math.random() < .5) {
				if (createGroup(COMMUNITY, groupName) == ReturnCode.SUCCESS) {
					if (requestRole(COMMUNITY, groupName, ROLE) != ReturnCode.SUCCESS) {
						testFails(new Exception());
						return;
					}
				} 
				else {
					requestRole(COMMUNITY, groupName, ROLE);
				}
			}
			else{
				if (Math.random() < .5) {
					leaveRole(COMMUNITY, groupName, ROLE);
				} else {
					leaveGroup(COMMUNITY, groupName);
				}
			}
		}
	}
	
}