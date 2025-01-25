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

package madkit.kernel;

/**
 * The generic MaDKit message class. Create Subclasses to adapt it to your
 * needs. This class is quite lightweight, it just defines sender and receivers
 * (expressed with {@link AgentAddress} class).
 *
 * @version 5
 *
 *
 * @see AgentAddress
 * @see Agent#send(Message, AgentAddress)
 * @see Agent#send(Message, String, String, String)
 */
public class Message implements Cloneable, java.io.Serializable {

	private static final long serialVersionUID = -7343412576480540415L;
	private AgentAddress receiver;
	private AgentAddress sender;
	private ConversationID conversationID = new ConversationID();

	/**
	 * Sets the receiver of the message.
	 *
	 * @param aa the agent address of the receiver
	 */
	final void setReceiver(AgentAddress aa) {
		receiver = aa;
	}

	/**
	 * Sets the sender of the message.
	 *
	 * @param aa the agent address of the sender
	 */
	final void setSender(final AgentAddress aa) {
		sender = aa;
	}

	/**
	 * Returns the agent address corresponding to the agent that receives this
	 * message.
	 *
	 * @return the receiver
	 */
	public AgentAddress getReceiver() {
		return receiver;
	}

	/**
	 * Returns the agent address corresponding to the agent that sends this message.
	 *
	 * @return the message's sender or <code>null</code> if the message has not been
	 *         sent by an agent, but by any other kind of object.
	 * @see Agent#send(Message, AgentAddress)
	 * @see Agent#send(Message, String, String, String)
	 */
	public AgentAddress getSender() {
		return sender;
	}

	/**
	 * Returns the role of the agent that sent this message.
	 *
	 * @return the sender's role or <code>null</code> if the message has not been
	 *         sent by an agent, but by any other kind of object.
	 * @see Agent#send(Message, AgentAddress)
	 * @see Agent#send(Message, String, String, String)
	 */
	public String getSenderRole() {
		return sender != null ? sender.getRole() : null;
	}

	/**
	 * Returns a string representation of the message.
	 *
	 * @return a string representation of the message
	 */
	@Override
	public String toString() {
		String s = getClass().getSimpleName() + "." + getConversationID();
		if (sender == null || receiver == null)
			return s;
		return s + ": " + sender + " -> " + receiver;
	}

	/**
	 * Returns a shallow copy of the message. Message subclasses requiring deep
	 * copies of their object fields should override this method. Especially,
	 * message cloning is used by {@link Agent#broadcast(Message, java.util.List)}
	 * and the like to set different receivers for each cloned message.
	 *
	 * @return a shallow copy of the message.
	 */
	@Override
	protected Message clone() {
		try {
			return (Message) super.clone();
		} catch (CloneNotSupportedException e) {
			throw new InternalError();
		}
	}

	/**
	 * Copies the conversation ID from another message.
	 *
	 * @param from the message from which to copy the conversation ID
	 */
	final void setIDFrom(Message from) {
		conversationID = from.conversationID;
	}

	/**
	 * Returns the conversation ID for this message. When a message is created, it
	 * is given an ID that will be used to tag all the messages that will be created
	 * for answering this message using {@link Agent#reply(Message, Message)} like
	 * methods. Especially, if the answer is again used for replying, the ID will be
	 * used again to tag this new answer, and so on.
	 *
	 * @return the ID of the conversation to which this message belongs to.
	 */
	public ConversationID getConversationID() {
		return conversationID;
	}

}
