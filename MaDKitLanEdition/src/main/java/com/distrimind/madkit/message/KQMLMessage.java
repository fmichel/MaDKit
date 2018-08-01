/*
 * Copyright or © or Copr. Fabien Michel, Olivier Gutknecht, Jacques Ferber (1997)
 * 
 * fmichel@lirmm.fr
 * olg@no-distance.net
 * ferber@lirmm.fr
 * 
 * This software is a computer program whose purpose is to
 * provide a lightweight Java library for designing and simulating Multi-Agent Systems (MAS).
 * This software is governed by the CeCILL-C license under French law and
 * abiding by the rules of distribution of free software.  You can  use,
 * modify and/ or redistribute the software under the terms of the CeCILL-C
 * license as circulated by CEA, CNRS and INRIA at the following URL
 * "http://www.cecill.info".
 * As a counterpart to the access to the source code and  rights to copy,
 * modify and redistribute granted by the license, users are provided only
 * with a limited warranty  and the software's author,  the holder of the
 * economic rights,  and the successive licensors  have only  limited
 * liability.
 * 
 * In this respect, the user's attention is drawn to the risks associated
 * with loading,  using,  modifying and/or developing or reproducing the
 * software by the user in light of its specific status of free software,
 * that may mean  that it is complicated to manipulate,  and  that  also
 * therefore means  that it is reserved for developers  and  experienced
 * professionals having in-depth computer knowledge. Users are therefore
 * encouraged to load and test the software's suitability as regards their
 * requirements in conditions enabling the security of their systems and/or
 * data to be ensured and,  more generally, to use and operate it in the
 * same conditions as regards security.
 * The fact that you are presently reading this means that you have had
 * knowledge of the CeCILL-C license and that you accept its terms.
 */
package com.distrimind.madkit.message;

import java.util.Enumeration;

/**
 * This class describes a KQML message. It provides accessors for all reserved
 * fields defined in the KQML Specification. Note that the :receiver and :sender
 * are automatically mapped to the MaDKit AgentAddress.
 * 
 * @author Olivier Gutknecht
 * @version MaDKit 1.0
 */
@SuppressWarnings("ExternalizableWithoutPublicNoArgConstructor")
public class KQMLMessage extends ActMessage {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3534390128191295863L;
	protected KQMLMessage()
	{

	}

	/** Constructor for KQMLMessage class
	 * @param performative the performative parameter
	 *  */
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
		StringBuilder buffer = new StringBuilder();

		buffer.append("(").append(getPerformative());
		for (Enumeration<String> e = fields.keys(); e.hasMoreElements();) {
			Object field = e.nextElement();
			buffer.append(" :").append(field).append(" ");
			buffer.append(getFieldValue((String) field));
		}
		if (getSender() != null)
			buffer.append(" :sender \"").append(getSender()).append("\"");
		if (getReceiver() != null)
			buffer.append(" :receiver \"").append(getReceiver()).append("\"");

		buffer.append(")");
		return new String(buffer);
	}

}
