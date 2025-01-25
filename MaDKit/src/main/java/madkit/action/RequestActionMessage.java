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
import madkit.messages.StringMessage;

/**
 * A message used to model a request made to an agent to perform an action that
 * corresponds to an internal method call. The message contains the name of the method to
 * call and the parameters to pass to the method.
 * <p>
 * It is especially used in {@link RequestAgentMessageAction} and
 * {@link AgentMethodAction} which are mainly designed to be used in a GUI to request an
 * agent to perform an action.
 * <p>
 * Upon reception of such a message, an agent can use
 * {@link Agent#handleRequestActionMessage(RequestActionMessage)} to automatically perform
 * the underlying method call.
 * 
 * @see RequestAgentMessageAction AgentMethodAction
 */
public class RequestActionMessage extends StringMessage {

	private static final long serialVersionUID = -7541550743747428543L;
	private Object[] parameters;

	/**
	 * Builds a new request message with the name of the method to call and the parameters to
	 * pass to the method.
	 * 
	 * @param methodName the name of the method to call
	 * @param parameters the parameters to pass to the method
	 */
	public RequestActionMessage(String methodName, Object... parameters) {
		super(methodName);
		this.parameters = parameters;
	}

	/**
	 * Gets the parameters to pass to the method.
	 * 
	 * @return the parameters
	 */
	public Object[] getParameters() {
		return parameters;
	}
}
