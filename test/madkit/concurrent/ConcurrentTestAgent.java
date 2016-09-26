/*
 * Copyright or © or Copr. Fabien Michel, Olivier Gutknecht, Jacques Ferber (1997)

fmichel@lirmm.fr
olg@no-distance.net
ferber@lirmm.fr

This software is a computer program whose purpose is to 
provide a lightweight Java library for designing and simulating Multi-Agent Systems (MAS).

This software is governed by the CeCILL-C license under French law and
abiding by the rules of distribution of free software.  You can  use, 
modify and/ or redistribute the software under the terms of the CeCILL-C
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
package madkit.concurrent;

import static madkit.kernel.JunitMadkit.COMMUNITY;
import static madkit.kernel.JunitMadkit.GROUP;
import static madkit.kernel.JunitMadkit.ROLE;
import static madkit.kernel.JunitMadkit.testFails;

import madkit.kernel.Agent;
import madkit.kernel.AgentAddress;

/**
* @author Fabien Michel
*/
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