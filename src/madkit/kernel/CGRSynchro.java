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
package madkit.kernel;

import madkit.message.ObjectMessage;

/**
 * @author Fabien Michel
 * @version 0.9
 * @since MaDKit 5.0
 *
 */
class CGRSynchro extends ObjectMessage<AgentAddress> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1125125814563126121L;

	public enum Code {
		CREATE_GROUP,
		REQUEST_ROLE,
		LEAVE_ROLE,
		LEAVE_GROUP
//		LEAVE_ORG		
		}
	
	final private Code code;

	/**
	 * @param code
	 * @param aa
	 */
	public CGRSynchro(final Code code, final AgentAddress aa) {
		super(aa);
		this.code = code;
	}

	/**
	 * @return the code
	 */
	public Code getCode() {
		return code;
	}

	@Override
	public String toString() {
		return super.toString()+"\n\t"+getCode()+" on "+getContent();
	}


}
