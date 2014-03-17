/*
 * Copyright 2014 Fabien Michel
 * 
 * This file is part of MaDKit.
 * 
 * MaDKit is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * MaDKit is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with MaDKit. If not, see <http://www.gnu.org/licenses/>.
 */
package madkit.message;

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
public class ConversationFilter implements MessageFilter {
	
	
	final private ConversationID conversationID;

	/**
	 * a new filter that acts according to the conversationID of a message.
	 * 
	 * @param origin the message's ID which will be used to check
	 * the acceptability of message.
	 */
	public ConversationFilter(final Message origin) {
		conversationID = origin.getConversationID();
	}

	@Override
	public boolean accept(final Message m) {
		return conversationID.equals(m.getConversationID());
	}

}
