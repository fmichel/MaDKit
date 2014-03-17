/*
 * Copyright 2014 Fabien Michel, Olivier Gutknecht, Jacques Ferber
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
package madkit.kernel;

import java.io.Serializable;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 
 * This class represents the conversation ID to which a message belongs.
 * 
 * When a message is created, it is given an ID that will
 * be used to tag all the messages that will be created
 * for answering this message using {@link AbstractAgent#sendReply(Message, Message)} like methods.
 * Especially, if the answer is again used for replying, the ID
 * will be used again to tag this new answer, and so on.
 * 
 * 
 * @author Fabien Michel
 * @since MadKit 5.0.4
 */
final public class ConversationID implements Serializable{

	/**
	 * 
	 */
	private static final long	serialVersionUID	= 4280603137316237711L;
	final static private AtomicInteger ID_COUNTER = new AtomicInteger();//TODO if many many ??
	final private int id ;
	private KernelAddress origin;

	ConversationID() {
		id = ID_COUNTER.getAndIncrement();
	}
	
	@Override
	public String toString() {
		return id+(origin == null ? "" : "-"+origin);
	}
	
	@Override
	public boolean equals(Object obj) {
		if(this == obj)
			return true;
		if(origin != null){//obj necessarily comes from the network or is different, so origin should have been set priorly if there is a chance of equality
			final ConversationID ci = (ConversationID) obj;//no check is intentional
			return this.id == ci.id && origin.equals(ci.origin);
		}
		return false;
	}

	void setOrigin(KernelAddress origin) {
		if (this.origin == null) {
			this.origin = origin;
		}
	}

	@Override
	public int hashCode() {
		return id;
	}
}