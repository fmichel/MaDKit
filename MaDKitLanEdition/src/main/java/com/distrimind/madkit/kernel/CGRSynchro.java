/*
 * MadKitLanEdition (created by Jason MAHDJOUB (jason.mahdjoub@distri-mind.fr)) Copyright (c)
 * 2015 is a fork of MadKit and MadKitGroupExtension. 
 * 
 * Copyright or © or Copr. Jason Mahdjoub, Fabien Michel, Olivier Gutknecht, Jacques Ferber (1997)
 * 
 * jason.mahdjoub@distri-mind.fr
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
package com.distrimind.madkit.kernel;

import com.distrimind.madkit.message.ObjectMessage;

/**
 * @author Fabien Michel
 * @author Jason Mahdjoub
 * @version 0.91
 * @since MaDKitLanEdition 1.0
 *
 */
public class CGRSynchro extends ObjectMessage<AgentAddress> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1125125814563126121L;

	public enum Code {
		CREATE_GROUP, REQUEST_ROLE, LEAVE_ROLE, LEAVE_GROUP
		// LEAVE_ORG
	}

	final private Code code;
	final private boolean manual;

	/**
	 * @param code
	 * @param aa
	 */
	CGRSynchro(final Code code, final AgentAddress aa, boolean manual_operation) {
		super(aa);
		if (code == null)
			throw new NullPointerException("code");
		if (aa == null)
			throw new NullPointerException("aa");

		this.code = code;
		manual = manual_operation;
	}

	@Override
	public String toString() {
		return "CGRSynchro[code=" + code + ", manual=" + manual + ", agentAddress=" + getContent() + "]";
	}

	/**
	 * @return the code
	 */
	public Code getCode() {
		return code;
	}

	public boolean isManualOperation() {
		return manual;
	}

}

class RequestRoleSecure extends ObjectMessage<Object> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1661974372588706717L;
	final private Class<? extends AbstractAgent> requesterClass;
	final private AgentAddress requester;
	final private String roleName;

	public RequestRoleSecure(Class<? extends AbstractAgent> requesterClass, AgentAddress requester, String roleName,
			Object key) {
		super(key);
		this.requesterClass = requesterClass;
		this.requester = requester;
		this.roleName = roleName;
	}

	/**
	 * @return the requester
	 */
	AgentAddress getRequester() {
		return requester;
	}

	/**
	 * @return the roleName
	 */
	public String getRoleName() {
		return roleName;
	}

	Class<? extends AbstractAgent> getRequesterClass() {
		return requesterClass;
	}
}