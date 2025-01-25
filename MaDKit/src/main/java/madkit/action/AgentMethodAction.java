/*******************************************************************************
 * MaDKit - Multi-agent systems Development Kit 
 * 
 * Copyright (c) 1998-2025 Fabien Michel, Olivier Gutknecht, Jacques Ferber...
 * 
 * This software is a computer program whose purpose is to
 * provide a lightweight Java API for developing and simulating 
 * Multi-Agent Systems (MAS) using an organizational perspective.
 *
 * This software is governed by the CeCILL-C license under French law and
 * abiding by the rules of distribution of free software.You can use,
 * modify and/ or redistribute the software under the terms of the CeCILL-C
 * license as circulated by CEA, CNRS and INRIA at the following URL
 * "http://www.cecill.info".
 *
 * As a counterpart to the access to the source code and rights to copy,
 * modify and redistribute granted by the license, users are provided only
 * with a limited warranty and the software's author, the holder of the
 * economic rights, and the successive licensors have only limited
 * liability.
 *
 * In this respect, the user's attention is drawn to the risks associated
 * with loading, using, modifying and/or developing or reproducing the
 * software by the user in light of its specific status of free software,
 * that may mean that it is complicated to manipulate, and that also
 * therefore means that it is reserved for developers and experienced
 * professionals having in-depth computer knowledge. Users are therefore
 * encouraged to load and test the software's suitability as regards their
 * requirements in conditions enabling the security of their systems and/or
 * data to be ensured and, more generally, to use and operate it in the
 * same conditions as regards security.
 *
 * The fact that you are presently reading this means that you have had
 * knowledge of the CeCILL-C license and that you accept its terms.
 *******************************************************************************/
package madkit.action;

import madkit.kernel.Agent;

/**
 * An Action that directly calls the
 * {@link Agent#handleRequestActionMessage(RequestActionMessage)} method of an agent when
 * triggered.
 * <p>
 * This action is useful when you want to trigger an agent's method from a graphical user
 * interface, for example.
 * <p>
 * Using this, developers should be aware that the agent's method will be called in the
 * caller thread, and not in the agent's thread. This can lead to concurrency issues if
 * the agent's method is not thread-safe.
 * <p>
 * Moreover, from an agent-oriented programming perspective, this could be seen as a bad
 * practice, as it breaks the encapsulation of the agent's behavior. So, it could be a
 * better solution to use the {@link RequestAgentMessageAction} that does not directly
 * call the agent's method, but only sends a {@link RequestActionMessage} to the agent,
 * which will be processed only when the agent decides to do so.
 * <p>
 * Still, there are cases where using a direct call to the agent's method does not impact
 * the control flow of the agent, and it can be a useful tool to interact with the agent
 * from the outside, especially in a graphical user interface.
 * 
 * @see RequestAgentMessageAction RequestAgentMessage
 * 
 */
public class AgentMethodAction extends ActionWithData {

	/**
	 * Creates a new AgentMethodAction that will call the given method of the given agent when
	 * triggered.
	 * 
	 * @param agent      the agent that will receive the request
	 * @param methodName the name of the method to call
	 * @param params     the parameters to pass to the method
	 */
	public AgentMethodAction(Agent agent, String methodName, Object... params) {
		super(methodName, _ -> {
			agent.handleRequestActionMessage(new RequestActionMessage(methodName, params));
		});

	}
}
