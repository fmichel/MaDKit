/*
 * Copyright 2012 Fabien Michel
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
package com.distrimind.madkit.message.hook;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import com.distrimind.madkit.exceptions.MessageSerializationException;
import com.distrimind.madkit.kernel.AgentAddress;
import com.distrimind.madkit.kernel.network.SystemMessage.Integrity;
import com.distrimind.madkit.util.NetworkMessage;
import com.distrimind.madkit.util.SerializationTools;

/**
 * A message which is sent to agents that have requested a hook on
 * {@link HookMessage.AgentActionEvent#CREATE_GROUP},
 * {@link HookMessage.AgentActionEvent#REQUEST_ROLE},
 * {@link HookMessage.AgentActionEvent#LEAVE_GROUP}, or
 * {@link HookMessage.AgentActionEvent#LEAVE_ROLE}
 * 
 * @author Fabien Michel
 * @since MadKit 5.0.0.19
 * @version 0.9
 * 
 */
@SuppressWarnings("ExternalizableWithoutPublicNoArgConstructor")
public class OrganizationEvent extends CGREvent implements NetworkMessage {


	private AgentAddress source;
	
	@SuppressWarnings("unused")
	private OrganizationEvent()
	{
		super(null);
	}
	
	public OrganizationEvent(AgentActionEvent agentAction, AgentAddress source) {
		super(agentAction);
		this.source = source;
	}

	public AgentAddress getSourceAgent() {
		return source;
	}

	@Override
	public int getInternalSerializedSize() {
		
		return super.getInternalSerializedSizeImpl()+source.getInternalSerializedSize();
	}
	@Override
	public void readExternal(final ObjectInput in) throws IOException, ClassNotFoundException
	{
		super.readExternal(in, 1000);
		Object o=SerializationTools.readExternalizableAndSizable(in, false);
		if (!(o instanceof AgentAddress))
			throw new MessageSerializationException(Integrity.FAIL_AND_CANDIDATE_TO_BAN);
		source=(AgentAddress)o;
		
	}
	@Override
	public void writeExternal(final ObjectOutput oos) throws IOException{
		super.writeExternal(oos, 1000);
		SerializationTools.writeExternalizableAndSizable(oos, source, false);
	}
}
