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

import java.util.Enumeration;

/**
 * This class describes a KQML message. It provides accessors for all
 * reserved fields defined in the KQML Specification.
 * Note that the :receiver and :sender are automatically mapped to the
 * MaDKit AgentAddress.
 * 
 * @author Olivier Gutknecht
 * @version MaDKit 1.0
 */

public class KQMLMessage extends ActMessage {

	/**
	 * 
	 */
	private static final long	serialVersionUID	= -3534390128191295863L;

	/** Constructor for KQMLMessage class */
	public KQMLMessage(String performative) {
		super(performative);
	}

	public String getPerformative() {
		return action;
	}

	public String getForce() {
		return (String) getFieldValue("force");
	}

	public void setForce(String s) {
		fields.put("force", s);
	}

	public String getReplyWith() {
		return (String) getFieldValue("reply-with");
	}

	public void setReplyWith(String s) {
		fields.put("reply-with", s);
	}

	public String getInReplyTo() {
		return (String) getFieldValue("in-reply-to");
	}

	public void setInReplyTo(String s) {
		fields.put("in-reply-to", s);
	}

	public String getLanguage() {
		return (String) getFieldValue("language");
	}

	public void setLanguage(String s) {
		fields.put("language", s);
	}

	public String getOntology() {
		return (String) getFieldValue("ontology");
	}

	public void setOntology(String s) {
		fields.put("ontology", s);
	}

	public String toString() {
		StringBuffer buffer = new StringBuffer();

		buffer.append("(" + getPerformative());
		for (Enumeration<String> e = fields.keys(); e.hasMoreElements();) {
			Object field = e.nextElement();
			buffer.append(" :" + field + " ");
			buffer.append(getFieldValue((String) field));
		}
		if (getSender() != null)
			buffer.append(" :sender \"" + getSender() + "\"");
		if (getReceiver() != null)
			buffer.append(" :receiver \"" + getReceiver() + "\"");

		buffer.append(")");
		return new String(buffer);
	}

}
