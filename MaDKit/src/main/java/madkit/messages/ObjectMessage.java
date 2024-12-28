package madkit.messages;

import madkit.kernel.Message;

/**
 * This parameterizable class could be used to convey any Java Object between
 * MaDKit agents.
 * 
 * @author Fabien Michel
 * @since MaDKit 5.0.0.1
 * @param <T> the type of the object to be conveyed
 * @version 0.9
 */
public class ObjectMessage<T> extends Message {

	private static final long serialVersionUID = 2061462024105569662L;
	private final T content;

	/**
	 * Builds a message with the specified content
	 * 
	 * @param content the object to be conveyed
	 */
	public ObjectMessage(final T content) {
		this.content = content;
	}

	/**
	 * Gets the content of this message
	 * 
	 * @return the object of type T contained in the message
	 */
	public T getContent() {
		return content;
	}

	/**
	 * Returns a string representation of the message.
	 *
	 * @return a string representation of the message
	 * @see madkit.kernel.Message#toString()
	 */
	@Override
	public String toString() {
		String s = super.toString();
		s += " " + (getClass().getSimpleName() + getConversationID());
		return s + " {" + content + "}";
	}
}
