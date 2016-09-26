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
package madkit.message.hook;

import madkit.message.ObjectMessage;

/**
 * This message could be used to request a kernel's hook
 * on agent actions so that the sender will be 
 * kept informed when an agent performed particular action.
 * 
 * @author Fabien Michel
 * @since MaDKit 5.0.0.14
 * @version 0.91
 * @see AgentActionEvent
 * 
 */
public class HookMessage extends ObjectMessage<HookMessage.AgentActionEvent> {
	

	/**
	 * 
	 */
	private static final long	serialVersionUID	= 3008390114345525272L;

	/**
	 * This message should be used to request or release a hook on
	 * an agent action. The message should be sent to the kernel 
	 * (which is the manager of the SYSTEM group in the local
	 * community), here is
	 * an example :
	 * <pre><code>
	 * sendMessage(
	 * 	LocalCommunity.NAME,
	 * 	LocalCommunity.Groups.SYSTEM, 
	 * 	Organization.GROUP_MANAGER_ROLE,
	 * 	new HookMessage(AgentActionEvent.REQUEST_ROLE));
	 * </code></pre>
	 * In this example, the sender will be informed by the 
	 * kernel of all successful requestRole operation 
	 * made by the agents. This information will be transmitted 
	 * using a subclass of HookMessage depending on the nature of the event.
	 * That is, {@link OrganizationEvent}, {@link MessageEvent} or
	 * {@link AgentLifeEvent} messages will be sent by the kernel
	 * according to the type of the hook which has been requested.
	 * <p>
	 * To give up the hook, just send to the kernel another message built with
	 * the same action and it will remove the sender from the subscriber list.
	 * 
	 * @param hookType the action event type to monitor
	 * @see AgentActionEvent
	 */
	public HookMessage(HookMessage.AgentActionEvent hookType) {
		super(hookType);
	}
	
	/**
	 * Enumeration representing agent actions that could be monitored using hooks.
	 *
	 */
	public enum AgentActionEvent{
		CREATE_GROUP,
		REQUEST_ROLE,
		LEAVE_GROUP,
		LEAVE_ROLE,
		SEND_MESSAGE,
		BROADCAST_MESSAGE,
		AGENT_STARTED,
		AGENT_TERMINATED
	}
}
