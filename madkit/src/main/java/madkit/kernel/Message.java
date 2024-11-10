
package madkit.kernel;

/**
 * The generic MaDKit message class. Create Subclasses to adapt it to your
 * needs. This class is quite lightweight, it just defines sender and receivers
 * (expressed with {@link AgentAddress} class).
 * 
 * @version 5
 * @author Fabien Michel
 * @author Olivier Gutknecht
 * @see AgentAddress
 * @see Agent#send(Message, AgentAddress)
 * @see Agent#send(Message, String, String, String)
 */

// TODO
// * If security is an issue for the application, implementing the <code>SecuredMessage</code> interface should be
// considered as,
// * although the default fields of a message could not be altered by receivers (read only or copy),
// * other messages (subclasses of Message) and their content could be intercepted via the hooking mechanism of the
// kernel.
// * Thus, additional content could be altered by receivers if not read only.
// * Implementing the <code>SecuredMessage</code> interface
// * (and Overriding the default <code>clone</code> method of <code>Message</code>; Do not forget to call super.clone())
// * will tell the kernel
// * to do a copy of the message for each receiver, ensuring the data security of the original object/content used to
// build a message.
// * @see SecuredMessage

public class Message implements Cloneable, java.io.Serializable {// TODO message already sent warning !!!

	private static final long serialVersionUID = -7343412576480540415L;
	private AgentAddress receiver;
	private AgentAddress sender;
	private ConversationID conversationID = new ConversationID();

	/**
	 * @param atargetedRole
	 */
	final void setReceiver(AgentAddress aa) {
		receiver = aa;
	}

	/**
	 * @param aa
	 */
	final void setSender(final AgentAddress aa) {
		sender = aa;
	}

	/**
	 * Returns the agent address corresponding to the agent that receive this
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
	 * message cloning is used by
	 * {@link Agent#broadcast(String, String, String, Message)} and the like to set
	 * different receivers for each cloned message.
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
	 * @param iD the iD to set
	 */
	final void getIDFrom(final Message from) {
		conversationID = from.conversationID;
	}

	/**
	 * returns the conversation ID for this message. When a message is created, it
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
