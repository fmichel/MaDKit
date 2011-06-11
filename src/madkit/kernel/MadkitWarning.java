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

import java.util.Arrays;

import madkit.i18n.I18nUtilities;
import madkit.kernel.AbstractAgent.ReturnCode;

/**
 * @author Fabien Michel
 * @version 0.9
 * @since MadKit 5.0
 *
 */
class MadkitWarning extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5977801961418382065L;
	protected final ReturnCode code;

	MadkitWarning(String message, ReturnCode code){
		super(message);
		this.code = code;
	}

	MadkitWarning(ReturnCode code){
		this.code = code;
	}
	
	@Override
	public String getMessage() {
		String msg = super.getMessage();
		return code.name()+": "+(msg == null ? "":msg+" ")+code.toString();
	}

	final ReturnCode getCode(){
		return code;
	}

	/* (non-Javadoc)
	 * @see java.lang.Throwable#toString()
	 */
//	@Override
//	public String toString() {
//		final String s = getClass().getSimpleName();
//		final String message = getLocalizedMessage();
//		return (message != null) ? (s + ": " + message) : s;
//	}
}

final class OrganizationWarning extends MadkitWarning{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -1096664441558015062L;
	final private String community,group,role;

	public OrganizationWarning(ReturnCode code, String community,String group, String role) {
		super(code);
		this.community = community;
		this.group = group;
			this.role = role;
	}
	
	@Override
	public String getMessage() {
		String msg = code+" : ";
		switch (code) {
		case NOT_GROUP:
		case NOT_IN_GROUP:
		case ALREADY_GROUP:
		case ACCESS_DENIED:
			return msg+I18nUtilities.getCGRString(community,group, null);
		case NOT_COMMUNITY:
			return msg+I18nUtilities.getCGRString(community, null, null);
		case ROLE_NOT_HANDLED:
		case NOT_ROLE:
		case ROLE_ALREADY_HANDLED:
			return msg+I18nUtilities.getCGRString(community, group, role);
		default:
			System.err.println("\n\n************** "+code.name()+" result not handled ");
			new Exception().printStackTrace();
			return null;
		}
	}
}

final class KernelException extends RuntimeException {

	private static final long serialVersionUID = -4549760236073524549L;

	KernelException(AbstractAgent agent ){
		super( (agent == null ? "" : agent.toString()) + " Cannot use this method as this agent has not been launched or is already ended or killed");
		setStackTrace(Arrays.asList(getStackTrace()).subList(2, getStackTrace().length-1).toArray(new StackTraceElement[0]));
	}
}


final class KilledException extends RuntimeException {
	/**
	 * 
	 */
	private static final long serialVersionUID = -8695603147644262321L;

	KilledException(String msg){
		super(msg);
	}

	KilledException(Throwable cause){
		super(cause);
	}

	//	@Override
	//	public synchronized Throwable fillInStackTrace() {
	//		return null;
	//	}
}//TODO get the cause of the kill