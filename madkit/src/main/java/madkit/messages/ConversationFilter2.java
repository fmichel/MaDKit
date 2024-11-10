
package madkit.messages;

import java.util.function.Predicate;

import madkit.kernel.ConversationID;
import madkit.kernel.Message;


/**
 * A filter that selects messages based on their conversation ID.
 * 
 * @author Fabien Michel
 * @since MaDKit 5.0.4
 * @version 0.9
 *
 */
public class ConversationFilter2 implements Predicate<Message> {
	
	
	private final ConversationID conversationID;

	/**
	 * a new filter that acts according to the conversationID of a message.
	 * 
	 * @param origin the message's ID which will be used to check
	 * the acceptability of message.
	 */
	public ConversationFilter2(final Message origin) {
		conversationID = origin.getConversationID();
	}

	@Override
	public boolean test(final Message m) {
		return conversationID.equals(m.getConversationID());
	}

}
