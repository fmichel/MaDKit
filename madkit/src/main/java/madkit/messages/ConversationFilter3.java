
package madkit.messages;

import java.util.function.BiPredicate;

import madkit.kernel.Message;


/**
 * A filter that selects messages based on their conversation ID.
 * 
 * @author Fabien Michel
 * @since MaDKit 5.0.4
 * @version 0.9
 *
 */
public class ConversationFilter3 implements BiPredicate<Message,Message> {
	
	
	@Override
	public boolean test(Message t, Message u) {
		return t.getConversationID().equals(u.getConversationID());
	}

}
