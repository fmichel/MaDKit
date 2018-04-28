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

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import com.distrimind.madkit.exceptions.MessageSerializationException;
import com.distrimind.madkit.kernel.network.SystemMessage.Integrity;
import com.distrimind.madkit.message.ObjectMessage;
import com.distrimind.madkit.util.SerializationTools;
import com.distrimind.madkit.util.SerializableAndSizable;

/**
 * @author Fabien Michel
 * @author Jason Mahdjoub
 * @version 0.91
 * @since MaDKitLanEdition 1.0
 *
 */
public class CGRSynchro extends ObjectMessage<AgentAddress> implements SerializableAndSizable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1125125814563126121L;

	public enum Code {
		CREATE_GROUP, REQUEST_ROLE, LEAVE_ROLE, LEAVE_GROUP
		// LEAVE_ORG
	}

	private Code code;
	private boolean manual;

	@Override
	public int getInternalSerializedSize() {
		return super.getInternalSerializedSizeImpl(0)+code.name().length()*2+3;
	}	
	
	@Override
	protected void readAndCheckObject(final ObjectInputStream in) throws IOException, ClassNotFoundException
	{
		super.readAndCheckObjectImpl(in, 0);
		code=Code.valueOf(SerializationTools.readString(in, 1000, false));
		if (code==null)
			throw new MessageSerializationException(Integrity.FAIL);
		manual=in.readBoolean();
		
	}
	@Override
	protected void writeAndCheckObject(final ObjectOutputStream oos) throws IOException{
		super.writeAndCheckObjectImpl(oos, 0);
		SerializationTools.writeString(oos, code.name(), 1000, false);
		oos.writeBoolean(manual);
	}
	
	
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

class RequestRoleSecure extends ObjectMessage<SerializableAndSizable> implements SerializableAndSizable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1661974372588706717L;
	private Class<? extends AbstractAgent> requesterClass;
	private AgentAddress requester;
	private String roleName;

	@Override
	public int getInternalSerializedSize() {
		return super.getInternalSerializedSizeImpl()+requesterClass.getName().length()*2+2+requester.getInternalSerializedSize()+roleName.length()*2+2;
	}	
	
	@SuppressWarnings("unchecked")
	@Override
	protected void readAndCheckObject(final ObjectInputStream in) throws IOException, ClassNotFoundException
	{
		super.readAndCheckObjectImpl(in);
		String clazz=SerializationTools.readString(in, Short.MAX_VALUE, false);
		
		Class<?> c=Class.forName(clazz, false, MadkitClassLoader.getSystemClassLoader());
		if (c==null)
			throw new MessageSerializationException(Integrity.FAIL);
		if (!AbstractAgent.class.isAssignableFrom(c))
			throw new MessageSerializationException(Integrity.FAIL_AND_CANDIDATE_TO_BAN);
		c=Class.forName(clazz, true, MadkitClassLoader.getSystemClassLoader());
		requesterClass=(Class<? extends AbstractAgent>)c;
		
		Object o=in.readObject();
		if (!(o instanceof AgentAddress))
			throw new MessageSerializationException(Integrity.FAIL_AND_CANDIDATE_TO_BAN);
		requester=(AgentAddress)o;
		roleName=SerializationTools.readString(in, Group.MAX_ROLE_NAME_LENGTH, false);
		
	}
	@Override
	protected void writeAndCheckObject(final ObjectOutputStream oos) throws IOException{
		super.writeAndCheckObjectImpl(oos);
		SerializationTools.writeString(oos, requesterClass.getName(), Short.MAX_VALUE, false);
		oos.writeObject(requester);
		SerializationTools.writeString(oos, roleName, Group.MAX_ROLE_NAME_LENGTH, false);
	}
	
	
	public RequestRoleSecure(Class<? extends AbstractAgent> requesterClass, AgentAddress requester, String roleName,
			SerializableAndSizable key) {
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