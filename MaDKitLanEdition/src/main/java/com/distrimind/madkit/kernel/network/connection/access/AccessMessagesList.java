package com.distrimind.madkit.kernel.network.connection.access;

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
	public Integrity checkDataIntegrity() {
		return Integrity.FAIL_AND_CANDIDATE_TO_BAN;
	}

	@Override
	public boolean checkDifferedMessages() {
		return false;
	}

}
