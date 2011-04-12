/*
 * Copyright 1997-2011 Fabien Michel, Olivier Gutknecht, Jacques Ferber
 * 
 * This file is part of MadKit.
 * 
 * MadKit is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * MadKit is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with MadKit. If not, see <http://www.gnu.org/licenses/>.
 */
package madkit.kernel;

import madkit.messages.ObjectMessage;

/**
 * The brand new version of KernelMessage.
 * For now its purpose is to allow agents to send to the kernel agent
 * some madkit commands such launchAgent.
 * 
 * @author Fabien Michel
 * @author Olivier Gutknecht
 * @version 5.0
 * @since MadKit 1.0
 *
 */
public class KernelMessage extends ObjectMessage<Object[]> {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -8752564850733075719L;

	private OperationCode code;

	/**
	 * @param code
	 * @param commandOptions
	 */
	public KernelMessage(OperationCode code, Object... commandOptions) {
		super(commandOptions);
		this.code = code;
	}

	/**
	 * @return the code
	 */
	public OperationCode getCode() {
		return code;
	}
	
	public enum OperationCode {
		SHUTDOWN_NOW,
		
		LAUNCH_AGENT,
		
		LAUNCH_NETWORK,
		
		STOP_NETWORK,
		
//		KILL_AGENT,
	}
	
	@Override
	public String toString() {
		String content="";
		for (Object o : getContent()) {
			content+=o+",";
		}
		return super.toString()+" : "+content;
	}

}
