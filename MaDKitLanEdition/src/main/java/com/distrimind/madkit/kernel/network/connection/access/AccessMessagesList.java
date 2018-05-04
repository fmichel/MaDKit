package com.distrimind.madkit.kernel.network.connection.access;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import com.distrimind.madkit.exceptions.MessageSerializationException;

public class AccessMessagesList extends AccessMessage {
	/**
	 * 
	 */
	private static final long serialVersionUID = 7355804164639261970L;
	private AccessMessage[] messages;
	
	AccessMessagesList(AccessMessage ...messages)
	{
		this.messages=messages;
	}
	public AccessMessage[] getMessages()
	{
		return messages;
	}
	

	@Override
	public boolean checkDifferedMessages() {
		return false;
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		throw new MessageSerializationException(Integrity.FAIL_AND_CANDIDATE_TO_BAN);
	}
	@Override
	public void writeExternal(ObjectOutput oos) throws IOException {
		throw new IOException();
	}
}
