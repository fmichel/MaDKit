/*
 * Copyright 1997-2011 Fabien Michel, Olivier Gutknecht, Jacques Ferber
 * 
 * This file is part of MadKit.
 * 
 * MadKit is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * MadKit is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with MadKit. If not, see <http://www.gnu.org/licenses/>.
 */
package madkit.kernel;

import java.util.concurrent.atomic.AtomicInteger;


/** 
 * The generic MadKit message class. Create Subclasses to adapt it to
 * your needs. This class is quite lightweight, it just defines sender
 * and receivers (expressed with AgentAddress class). 
 * 
 * @version 5
 * @author Fabien Michel since v.3
 * @author Olivier Gutknecht
 * @see AgentAddress
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

public class Message implements Cloneable,java.io.Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 6519763450927614564L;
	private AgentAddress receiver, sender;
	private int conversationID;
	private static AtomicInteger cID = new AtomicInteger();//TODO if many many ??

	
	public Message(){
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
	 * @return the receiver
	 */
	public AgentAddress getReceiver() {
		return receiver;
	}

	/**
	 * @return the sender
	 */
	public AgentAddress getSender() {
		return sender;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		if(sender == null || receiver == null)
			return getClass().getSimpleName();
		return getClass().getSimpleName()+" from "+sender+" to "+receiver;
		}

	/**
	 * @return a copy of the message.
	 */
	@Override
	public Message clone() {//TODO logging and warning and how clone
		try	{
			return (Message) super.clone();
		}
		catch (CloneNotSupportedException e) { 
			e.printStackTrace();
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
	 * @return the iD
	 */
	final public int getID() {
		return conversationID;
	}
	

}
