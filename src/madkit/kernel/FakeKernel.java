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
package madkit.kernel;

import java.util.List;

import madkit.i18n.ErrorMessages;
import madkit.util.MadkitProperties;

/**
 * @author Fabien Michel
 * @since MaDKit 5
 * @version 1.0
 * 
 */
class FakeKernel extends MadkitKernel{

	

	///////////////////////////////////////////////////////////////////////////
	////////////////////////// Agent interface
	///////////////////////////////////////////////////////////////////////////
	
	

	//////////////////////////////////////////////////////////////
	////////////////////////// Organization interface
	//////////////////////////////////////////////////////////////

	String buildFailString(final AbstractAgent agent){
		return (agent != null ? agent.toString() : "Agent" + AbstractAgent.State.NOT_LAUNCHED)+ErrorMessages.MUST_BE_LAUNCHED;
	}
	
	@Override
	final ReturnCode createGroup(final AbstractAgent agent, final String community, final String group, final Gatekeeper gatekeeper, final boolean isDistributed) {
		throw buildKernelException(agent);
	}
	
	AgentAddress getAgentAddressIn(AbstractAgent agent, String community, String group, String role) {
		throw buildKernelException(agent);
	}
	
	@Override
	final ReturnCode requestRole(AbstractAgent agent, String community, String group, String role, Object memberCard) {
		throw buildKernelException(agent);
	}

	@Override
	final ReturnCode leaveGroup(final AbstractAgent agent, final String community, final String group) {
		throw buildKernelException(agent);
	}

	@Override
	final ReturnCode leaveRole(AbstractAgent agent, String community,String group, String role) {
		throw buildKernelException(agent);
	}

	@Override
	final AgentAddress getAgentWithRole(final AbstractAgent agent, final String community, final String group, final String role){ 
		throw buildKernelException(agent);
	}

	@Override
	final List<AgentAddress> getAgentsWithRole(AbstractAgent agent, String community, String group, String role,boolean callerIncluded){
		throw buildKernelException(agent);
	}

	@Override
	final boolean isCommunity(AbstractAgent agent,String community){
		throw buildKernelException(agent);
	}

	@Override
	final boolean isGroup(AbstractAgent agent, String community, String group){
		throw buildKernelException(agent);
	}

	@Override
	final boolean isRole(AbstractAgent agent, String community, String group, String role){
		throw buildKernelException(agent);
	}
	
	@Override
	final public boolean isKernelOnline() {
		throw buildKernelException(null);
	}
	
	//////////////////////////////////////////////////////////////
	////////////////////////// Messaging interface
	//////////////////////////////////////////////////////////////
	@Override
	final ReturnCode sendMessage(final AbstractAgent agent, final String community, final String group, final String role, final Message messageToSend, final String senderRole) {
		throw buildKernelException(agent);
	}

	@Override
	final ReturnCode sendMessage(AbstractAgent agent, AgentAddress receiver, final Message messageToSend, final String senderRole){
		throw buildKernelException(agent);
	}

	@Override
	final ReturnCode broadcastMessageWithRole(final AbstractAgent agent, final String community, final String group, final String role, final Message messageToSend, String senderRole){
		throw buildKernelException(agent);
	}

	//////////////////////////////////////////////////////////////
	////////////////////////// Launching and Killing
	//////////////////////////////////////////////////////////////

	@Override
	final ReturnCode launchAgent(final AbstractAgent agent, final AbstractAgent agent2, final int timeOutSeconds, final boolean defaultGUI){
		throw buildKernelException(agent);
	}

	@Override
	void launchAgentBucketWithRoles(AbstractAgent requester, List<AbstractAgent> bucket, int cpuCoreNb, String... CGRLocations) {
		throw buildKernelException(requester);
	}

	@Override
	final ReturnCode killAgent(final AbstractAgent agent,final AbstractAgent target, int timeOutSeconds){
		throw buildKernelException(agent);
	}

	/**
	 * @param agent
	 * @return 
	 * @throws KernelException 
	 */
	private KernelException buildKernelException(final AbstractAgent agent) {
		final KernelException ke = new KernelException(buildFailString(agent));
		ke.printStackTrace();
		return ke;
	}

	@Override
	final synchronized boolean removeOverlooker(final AbstractAgent agent, Overlooker<? extends AbstractAgent> o) {
		throw buildKernelException(agent);
	}

	@Override
	final synchronized boolean addOverlooker(AbstractAgent agent, Overlooker<? extends AbstractAgent> o) {
		throw buildKernelException(agent);
	}

	/**
	 * @return
	 */
	@Override
	final public KernelAddress getKernelAddress() {
		throw buildKernelException(null);
	}

	@Override
	final public MadkitProperties getMadkitConfig() {
		return Madkit.defaultConfig;
	}

	@Override
	final List<Message> broadcastMessageWithRoleAndWaitForReplies(AbstractAgent agent,
			String community, String group, String role, Message message,
			String senderRole, Integer timeOutMilliSeconds) {
		throw buildKernelException(agent);
	}

	@Override
	final boolean createGroupIfAbsent(AbstractAgent agent,
			String community, String group, Gatekeeper gatekeeper,
			boolean isDistributed) {
		throw buildKernelException(agent);
	}

}
