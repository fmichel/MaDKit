/*
 * Copyright or © or Copr. Fabien Michel, Olivier Gutknecht, Jacques Ferber (1997)

fmichel@lirmm.fr
olg@no-distance.net
ferber@lirmm.fr

This software is a computer program whose purpose is to 
provide a lightweight Java library for designing and simulating Multi-Agent Systems (MAS).

This software is governed by the CeCILL-C license under French law and
abiding by the rules of distribution of free software.  You can  use, 
modify and/ or redistribute the software under the terms of the CeCILL-C
license as circulated by CEA, CNRS and INRIA at the following URL
"http://www.cecill.info". 

As a counterpart to the access to the source code and  rights to copy,
modify and redistribute granted by the license, users are provided only
with a limited warranty  and the software's author,  the holder of the
economic rights,  and the successive licensors  have only  limited
liability. 

In this respect, the user's attention is drawn to the risks associated
with loading,  using,  modifying and/or developing or reproducing the
software by the user in light of its specific status of free software,
that may mean  that it is complicated to manipulate,  and  that  also
therefore means  that it is reserved for developers  and  experienced
professionals having in-depth computer knowledge. Users are therefore
encouraged to load and test the software's suitability as regards their
requirements in conditions enabling the security of their systems and/or 
data to be ensured and,  more generally, to use and operate it in the 
same conditions as regards security. 

The fact that you are presently reading this means that you have had
knowledge of the CeCILL-C license and that you accept its terms.
 */
package madkit.kernel;



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
	private static final long	serialVersionUID	= -7343412576480540415L;
	/**
	 * 
	 */
	private AgentAddress receiver, sender;
	private ConversationID conversationID	= new ConversationID();
	
//	public Message(){//TODO id when sending ?
//	}
	/**
	 * @param atargetedRole
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
	 * Returns the agent address corresponding to the agent that receive this message.
	 * 
	 * @return the receiver
	 */
	public AgentAddress getReceiver() {
		return receiver;
	}

	/**
	 * Returns the agent address corresponding to the agent that sends this message.
	 * 
	 * @return the message's sender or <code>null</code> if 
	 * the message has not been sent by an agent, but by any other kind of object.
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
	final void getIDFrom(final Message from) {
			conversationID = from.conversationID;
	}

	/**
	 * returns the conversation ID for this message. 
	 * When a message is created, it is given an ID that will
	 * be used to tag all the messages that will be created 
	 * for answering this message using 
	 * {@link AbstractAgent#sendReply(Message, Message)} like methods.
	 * Especially, if the answer is again used for replying, the ID 
	 * will be used again to tag this new answer, and so on.
	 * 
	 * @return the ID of the conversation to which this message belongs to.
	 */
	public ConversationID getConversationID() {
		return conversationID;
	}
	

}
