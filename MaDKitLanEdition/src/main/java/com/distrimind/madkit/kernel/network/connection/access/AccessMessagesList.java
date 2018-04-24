package com.distrimind.madkit.kernel.network.connection.access;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

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

	private void readObject(final ObjectInputStream in) throws IOException, ClassNotFoundException
	{
		readAndCheckObject(in);
	}
	private void writeObject(final ObjectOutputStream oos) throws IOException
	{
		writeAndCheckObject(oos);
	}
	@Override
	public void readAndCheckObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
		throw new MessageSerializationException(Integrity.FAIL_AND_CANDIDATE_TO_BAN);
	}
	@Override
	public void writeAndCheckObject(ObjectOutputStream oos) throws IOException {
		oos.defaultWriteObject();
		
	}
}
