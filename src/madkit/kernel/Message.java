/*
 * Copyright 1997-2012 Fabien Michel, Olivier Gutknecht, Jacques Ferber
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

import java.util.concurrent.atomic.AtomicInteger;


/** 
 * The generic MaDKit message class. Create Subclasses to adapt it to
 * your needs. This class is quite lightweight, it just defines sender
 * and receivers (expressed with {@link AgentAddress} class). 
 * 
 * @version 5
 * @author Fabien Michel
 * @author Olivier Gutknecht
 * @see AgentAddress
 * @see AbstractAgent#sendMessage(AgentAddress, Message)
 * @see AbstractAgent#sendMessage(String, String, String, Message)
 * 
*/

//TODO 
//* If security is an issue for the application, implementing the <code>SecuredMessage</code> interface should be considered as,
//* although the default fields of a message could not be altered by receivers (read only or copy),
//* other messages (subclasses of Message) and their content could be intercepted via the hooking mechanism of the kernel.
//* Thus, additional content could be altered by receivers if not read only.
//* Implementing the <code>SecuredMessage</code> interface 
//* (and Overriding the default <code>clone</code> method of <code>Message</code>; Do not forget to call super.clone())
//* will tell the kernel
//* to do a copy of the message for each receiver, ensuring the data security of the original object/content used to build a message.
//* @see SecuredMessage

public class Message implements Cloneable,java.io.Serializable{//TODO message already sent warning !!!

	/**
	 * 
	 */
	private static final long serialVersionUID = 6519763450927614564L;
	private AgentAddress receiver, sender;
	private int conversationID;
	private static AtomicInteger cID = new AtomicInteger();//TODO if many many ??

	
	public Message(){//TODO id when sending ?
		conversationID = cID.getAndIncrement();
	}
	/**
	 * @param a
	 */
	final void setReceiver(AgentAddress a) {
		receiver = a;
	}

	/**
	 * @param agentAddressOf
	 */
	final void setSender(final AgentAddress agentAddressOf) {
		sender = agentAddressOf;
	}

	/**
	 * 
	 * @return the receiver
	 */
	public AgentAddress getReceiver() {
		return receiver;
	}

	/**
	 * Returns the agent address corresponding to the agent that sends this message.
	 * 
	 * @return the message's sender or <code>null</code> if the message has not been sent by an agent.
	 * @see AbstractAgent#sendMessage(AgentAddress, Message)
	 * @see AbstractAgent#sendMessage(String, String, String, Message)
	 */
	public AgentAddress getSender() {
		return sender;
	}
	
	@Override
	public String toString() {
		String s = getClass().getSimpleName()+"."+getConversationID();
		if(sender == null || receiver == null)
			return s;
		return s + ": "+sender+" -> "+receiver;
		}

	/**
	 * Returns a shadow copy of the message.
	 * Message subclasses requiring
	 * deep copies of their object fields should override this method. Especially,
	 * message cloning is used by {@link AbstractAgent#broadcastMessage(String, String, String, Message)}
	 * and the like to set different receivers for each cloned message.
	 * 
	 * @return a shadow copy of the message.
	 */
	@Override
	protected Message clone() {//TODO logging and warning and how clone
		try	{
			return (Message) super.clone();
		}
		catch (CloneNotSupportedException e) { 
			throw new InternalError(); 
		}
	}
	
	/**
	 * @param iD the iD to set
	 */
	final void setID(final int iD) {
			conversationID = iD;
	}

	/**
	 * returns the conversation ID of this message.
	 * 
	 * @return the ID of the conversation to which this message belongs to.
	 */
	final public int getConversationID() {
		return conversationID;
	}
	

}
