
package madkit.messages;

import java.util.function.Predicate;

import madkit.kernel.ConversationID;
import madkit.kernel.Message;

/**
 * A filter that selects messages based on their conversation ID.
 * 
 * @author Fabien Michel
 * @since MaDKit 5.0.4
 * @version 6.0
 *
 */
public class ConversationFilter implements Predicate<Message> {

	private final ConversationID conversationID;

	/**
	 * a filter that acts according to the conversationID of a message.
	 * 
	 * @param origin the message's ID which will be used to check the acceptability
	 *               of message.
	 */
	public ConversationFilter(Message origin) {
		conversationID = origin.getConversationID();
	}

	/**
	 * A filter that acts according to the conversationID of a message.
	 * 
	 * @param m the message to be tested
	 */
	@Override
	public boolean test(Message m) {
		return conversationID.equals(m.getConversationID());
	}

}
