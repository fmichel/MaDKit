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
package madkit.message;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import madkit.kernel.AgentAddress;

/**
 * This class describes an ACL message. It provides accessors for all
 * message parameters defined in the FIPA 97 specification part 2.
 * Note that the :receiver and :sender are automatically mapped to the
 * MaDKit AgentAddress.
 * 
 * @author Ol. Gutknecht, J. Ferber
 * @version 1.1
 * @since MaDKit 1.0
 */

public class ACLMessage extends ActMessage // NO_UCD
{

	/**
	 * 
	 */
	private static final long			serialVersionUID			= -6750319187360080985L;
	/** constant identifying the FIPA performative **/
	public static final int				ACCEPT_PROPOSAL			= 0;
	/** constant identifying the FIPA performative **/
	public static final int				AGREE							= 1;
	/** constant identifying the FIPA performative **/
	public static final int				CANCEL						= 2;
	/** constant identifying the FIPA performative **/
	public static final int				CFP							= 3;
	/** constant identifying the FIPA performative **/
	public static final int				CONFIRM						= 4;
	/** constant identifying the FIPA performative **/
	public static final int				DISCONFIRM					= 5;
	/** constant identifying the FIPA performative **/
	public static final int				FAILURE						= 6;
	/** constant identifying the FIPA performative **/
	public static final int				INFORM						= 7;
	/** constant identifying the FIPA performative **/
	public static final int				INFORM_IF					= 8;
	/** constant identifying the FIPA performative **/
	public static final int				INFORM_REF					= 9;
	/** constant identifying the FIPA performative **/
	public static final int				NOT_UNDERSTOOD				= 10;
	/** constant identifying the FIPA performative **/
	public static final int				PROPOSE						= 11;
	/** constant identifying the FIPA performative **/
	public static final int				QUERY_IF						= 12;
	/** constant identifying the FIPA performative **/
	public static final int				QUERY_REF					= 13;
	/** constant identifying the FIPA performative **/
	public static final int				REFUSE						= 14;
	/** constant identifying the FIPA performative **/
	public static final int				REJECT_PROPOSAL			= 15;
	/** constant identifying the FIPA performative **/
	public static final int				REQUEST						= 16;
	/** constant identifying the FIPA performative **/
	public static final int				REQUEST_WHEN				= 17;
	/** constant identifying the FIPA performative **/
	public static final int				REQUEST_WHENEVER			= 18;
	/** constant identifying the FIPA performative **/
	public static final int				SUBSCRIBE					= 19;
	/** constant identifying the FIPA performative **/
	public static final int				PROXY							= 20;
	/** constant identifying the FIPA performative **/
	public static final int				PROPAGATE					= 21;
	/** constant identifying an unknown performative **/
	public static final int				UNKNOWN						= -1;

	public static final String			ACCEPT_PROPOSAL_STRING	= "ACCEPT-PROPOSAL";
	public static final String			AGREE_STRING				= "AGREE";
	public static final String			CANCEL_STRING				= "CANCEL";
	public static final String			CFP_STRING					= "CFP";
	public static final String			CONFIRM_STRING				= "CONFIRMP";
	public static final String			DISCONFIRM_STRING			= "DISCONFIRM";
	public static final String			FAILURE_STRING				= "FAILURE";
	public static final String			INFORM_STRING				= "INFORM";
	public static final String			INFORM_IF_STRING			= "INFORM-IF";
	public static final String			INFORM_REF_STRING			= "INFORM-REF";
	public static final String			NOT_UNDERSTOOD_STRING	= "NOT-UNDERSTOOD";
	public static final String			PROPOSE_STRING				= "PROPOSE";
	public static final String			QUERY_IF_STRING			= "QUERY-IF";
	public static final String			QUERY_REF_STRING			= "QUERY-REF";
	public static final String			REFUSE_STRING				= "REFUSE";
	public static final String			REJECT_PROPOSAL_STRING	= "REJECT-PROPOSAL";
	public static final String			REQUEST_STRING				= "REQUEST";
	public static final String			REQUEST_WHEN_STRING		= "REQUEST-WHEN";
	public static final String			REQUEST_WHENEVER_STRING	= "REQUEST-WHENEVER";
	public static final String			SUBSCRIBE_STRING			= "SUBSCRIBE";
	public static final String			PROXY_STRING				= "PROXY";
	public static final String			PROPAGATE_STRING			= "PROPAGATE";

	private static final String		SENDER_KEY					= ":sender";
	private static final String		RECEIVER_KEY				= ":receiver";
	private static final String		CONTENT_KEY					= ":content";
	private static final String		REPLY_WITH_KEY				= ":reply-with";
	private static final String		IN_REPLY_TO_KEY			= ":in-reply-to";
	private static final String		REPLY_BY_KEY				= ":reply-by";
	private static final String		LANGUAGE_KEY				= ":language";
	private static final String		ENCODING_KEY				= ":encoding";
	private static final String		ONTOLOGY_KEY				= ":ontology";
	private static final String		PROTOCOL_KEY				= ":protocol";
	private static final String		CONVERSATION_ID_KEY		= ":conversation-id";
	private static final String		ENVELOPE_KEY				= ":envelope";

	public static List<String>			performatives				= new ArrayList<String>(
																						22);
	static { // initialization of the Vector of performatives
		performatives.add("ACCEPT-PROPOSAL");
		performatives.add("AGREE");
		performatives.add("CANCEL");
		performatives.add("CFP");
		performatives.add("CONFIRM");
		performatives.add("DISCONFIRM");
		performatives.add("FAILURE");
		performatives.add("INFORM");
		performatives.add("INFORM-IF");
		performatives.add("INFORM-REF");
		performatives.add("NOT-UNDERSTOOD");
		performatives.add("PROPOSE");
		performatives.add("QUERY-IF");
		performatives.add("QUERY-REF");
		performatives.add("REFUSE");
		performatives.add("REJECT-PROPOSAL");
		performatives.add("REQUEST");
		performatives.add("REQUEST-WHEN");
		performatives.add("REQUEST-WHENEVER");
		performatives.add("SUBSCRIBE");
		performatives.add("PROXY");
		performatives.add("PROPAGATE");
	}

	/**
	 * @serial
	 */
	private ArrayList<AgentAddress>	dests							= new ArrayList<AgentAddress>();

	/**
	 * @serial
	 */
	private ArrayList<AgentAddress>	reply_to						= new ArrayList<AgentAddress>();

	/** Default constructor for ACLMessage class */
	public ACLMessage() {
		super(NOT_UNDERSTOOD_STRING);
	}

	/** Constructor for ACLMessage class */
	public ACLMessage(String actType) {
		super(actType.toUpperCase());
	}

	/** Constructor for ACLMessage class */
	public ACLMessage(String actType, String cont) {
		super(actType.toUpperCase(), cont);
	}

	public ACLMessage(int perf, String cont) {
		super(performatives.get(perf), cont);
	}

	public String getAct() {
		return action;
	}

	public void setContent(String s) {
		setField("content", s);
	}

	public String getPerformative() {
		return getAct();
	}

	public void setPerformative(String s) {
		this.action = s;
	}

	/**
	 * Adds a value to <code>:receiver</code> slot. <em><b>Warning:</b>
     no checks are made to validate the slot value.</em>
	 * 
	 * @param r The value to add to the slot value set.
	 */
	public void addReceiver(AgentAddress r) {
		if (r != null)
			dests.add(r);
	}

	/**
	 * Return the list of receivers..
	 */
	public List<AgentAddress> getReceivers() {
		return dests;
	}

	/**
	 * Removes a value from <code>:receiver</code> slot. <em><b>Warning:</b> no checks are made to validate the slot
     value.</em>
	 * 
	 * @param r The value to remove from the slot value set.
	 * @return true if the AgentAddress has been found and removed, false otherwise
	 */
	public boolean removeReceiver(AgentAddress r) {
		if (r != null)
			return dests.remove(r);
		return false;
	}

	/**
	 * Removes all values from <code>:receiver</code> slot. <em><b>Warning:</b> no checks are made to validate the slot
     value.</em>
	 */
	public void clearAllReceiver() {
		dests.clear();
	}

	/**
	 * Adds a value to <code>:reply-to</code> slot. <em><b>Warning:</b>
     no checks are made to validate the slot value.</em>
	 * 
	 * @param dest The value to add to the slot value set.
	 */
	public void addReplyTo(AgentAddress dest) {
		if (dest != null)
			reply_to.add(dest);
	}

	/**
	 * Removes a value from <code>:reply_to</code> slot. <em><b>Warning:</b> no checks are made to validate the slot
     value.</em>
	 * 
	 * @param dest The value to remove from the slot value set.
	 * @return true if the AgentAddress has been found and removed, false otherwise
	 */
	public boolean removeReplyTo(AgentAddress dest) {
		if (dest != null)
			return reply_to.remove(dest);
		return false;
	}

	/**
	 * Removes all values from <code>:reply_to</code> slot. <em><b>Warning:</b> no checks are made to validate the slot
     value.</em>
	 */
	public void clearAllReplyTo() {
		reply_to.clear();
	}

	public String getEnvelope() {
		return (String) getFieldValue(ENVELOPE_KEY);
	}

	public void setEnvelope(String s) {
		setField(ENVELOPE_KEY, s);
	}

	public String getConversationIDentifier() {
		return (String) getFieldValue(CONVERSATION_ID_KEY);
	}

	public void setConversationID(String s) {
		setField(CONVERSATION_ID_KEY, s);
	}

	public String getProtocol() {
		return (String) getFieldValue(PROTOCOL_KEY);
	}

	public void setProtocol(String s) {
		setField(PROTOCOL_KEY, s);
	}

	public String getReplyWith() {
		return (String) getFieldValue(REPLY_WITH_KEY);
	}

	public void setReplyWith(String s) {
		setField(REPLY_WITH_KEY, s);
	}

	public String getReplyBy() {
		return (String) getFieldValue(REPLY_BY_KEY);
	}

	public void setReplyBy(String s) {
		setField(REPLY_BY_KEY, s);
	}

	public void setReplyBy(Date s) {
		setField(REPLY_BY_KEY, s.toString());
	}

	public String getInReplyTo() {
		return (String) getFieldValue(IN_REPLY_TO_KEY);
	}

	public void setInReplyTo(String s) {
		setField(IN_REPLY_TO_KEY, s);
	}

	public String getLanguage() {
		return (String) getFieldValue(LANGUAGE_KEY);
	}

	public void setLanguage(String s) {
		setField(LANGUAGE_KEY, s);
	}

	public String getEncoding() {
		return (String) getFieldValue(ENCODING_KEY);
	}

	public void setEncoding(String s) {
		setField(ENCODING_KEY, s);
	}

	public String getOntology() {
		return (String) getFieldValue(ONTOLOGY_KEY);
	}

	public void setOntology(String s) {
		setField(ONTOLOGY_KEY, s);
	}

	public String toString() {
		StringBuffer buffer = new StringBuffer();

		buffer.append("(" + getAct() + " ");
		if (getSender() != null)
			buffer.append(SENDER_KEY + " " + getSender());
		if (getReceiver() != null)
			buffer.append(" " + RECEIVER_KEY + " " + getReceiver());
		if (content != null)
			if (content.length() > 0)
				buffer.append(" " + CONTENT_KEY + " " + content + "\n");

		buffer.append(")");
		return new String(buffer);
	}

	/**
	 * create a new ACLMessage that is a reply to this message.
	 * In particular, it sets the following parameters of the new message:
	 * receiver, language, ontology, protocol, conversation-id,
	 * in-reply-to, reply-with.
	 * The programmer needs to set the communicative-act and the content.
	 * Of course, if he wishes to do that, he can reset any of the fields.
	 * 
	 * @return the ACLMessage to send as a reply
	 */
	public ACLMessage createReply() {
		ACLMessage m = new ACLMessage();
		if (reply_to.isEmpty()) {
			m.addReceiver(getSender());
		}
		else
			for (Iterator<AgentAddress> it = reply_to.iterator(); it.hasNext();) {
				m.addReceiver(it.next());
			}
		return m;
	}

}
