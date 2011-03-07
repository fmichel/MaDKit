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

/**
 * @author Fabien Michel
 * @version 0.9
 * @since MadKit 5.0
 *
 */
class CGRSynchroMessage extends NetworkMessage<AgentAddress> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3196237045985776131L;
	public static final int CREATE_GROUP = 1;
	public static final int REQUEST_ROLE = 2;
	public static final int LEAVE_ROLE = 3;
	public static final int LEAVE_GROUP= 4;
	public static final int LEAVE_ORG= 5;

	final private int operation;// = CREATE_GROUP;

	/**
	 * @param code
	 * @param operation
	 * @param aa
	 */
	public CGRSynchroMessage(final int operation, final AgentAddress aa) {
		super(aa);
		this.operation = operation;
	}

//	/**
//	 * @param operation the operation to set
//	 */
//	public void setOperation(int operation) {
//		this.operation = operation;
//	}

	/**
	 * @return the operation
	 */
	public int getOperation() {
		return operation;
	}

	@Override
	public String toString() {
		switch (operation) {
		case CGRSynchroMessage.CREATE_GROUP:
			return super.toString()+" CREATE_GROUP";
		case CGRSynchroMessage.LEAVE_GROUP:
			return super.toString()+" LEAVE_GROUP ";
		case CGRSynchroMessage.LEAVE_ROLE:
			return super.toString()+" LEAVE_ROLE ";
		case CGRSynchroMessage.REQUEST_ROLE:
			return super.toString()+" REQUEST_ROLE ";
		case CGRSynchroMessage.LEAVE_ORG:
			return super.toString()+" LEAVE_ORG ";
		default:
			return super.toString()+" PROBLEM !!!!!! ";
		}
	}


}
