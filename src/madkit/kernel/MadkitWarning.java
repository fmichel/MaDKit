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
	private static final long serialVersionUID = -1769706904251720012L;
	private final ReturnCode code;
	private final String cgrLocalization;
	/**
	 * @return the cgrLocalization
	 */
	final String getCgrLocalization() {
		return cgrLocalization;
	}

	/**
	 * @param message
	 */
	MadkitWarning(ReturnCode result, String message) {
		super(message);
//		cgrLocalization = cgrPb == null ? "" : cgrPb;
		cgrLocalization = message;
		code = result;
	}

	final ReturnCode getCode(){
		return code;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Throwable#toString()
	 */
	@Override
	public String toString() {
        final String s = getClass().getSimpleName();
        final String message = getLocalizedMessage();
        return (message != null) ? (s + ": " + message) : s;
	}
}

final class CreateGroupWarning extends MadkitWarning {
	/**
	 * 
	 */
	private static final long serialVersionUID = 3413823353563538173L;
	final static String noCreation = Utils.getI18N("notCreated");
	CreateGroupWarning(ReturnCode code, String printCGR) {
		super(code,printCGR + noCreation +" **");	
	}
}

final class NotAvailableActionWarning extends MadkitWarning {
	/**
	 * 
	 */
	private static final long serialVersionUID = -198059762126069336L;

	NotAvailableActionWarning(ReturnCode code, String printCGR) {
		super(code,printCGR);	
	}
}

final class sendMessageWarning extends MadkitWarning {
	/**
	 * 
	 */
	private static final long serialVersionUID = -3877584720927435349L;
	final static String baseMessage = Utils.getI18N("cantSend");
	final static String notInGroupMsg = baseMessage+" "+Utils.getI18N("notInTargetedGroup");
	final static String roleNotHandled = baseMessage+" "+Utils.getI18N("notHandled");
	final static String nullArgMsg = baseMessage+" "+Utils.getI18N("nullAA");
	final static String invAAMsg = baseMessage+" "+Utils.getI18N("invAA");

	sendMessageWarning(ReturnCode code, String message) {
		super(code,buildMessage(code,message));
	}
	sendMessageWarning(ReturnCode code) {
		super(code,buildMessage(code,""));
	}
	static String buildMessage(ReturnCode code, String message){
		switch (code) {
		case NOT_IN_GROUP:
			return notInGroupMsg+message;
		case ROLE_NOT_HANDLED:
			return roleNotHandled+message;
		case INVALID_ARG:
			return baseMessage+message;
		case INVALID_AA:
			return invAAMsg;
		case NOT_COMMUNITY:
		case NOT_GROUP:
		case NOT_ROLE:
			return baseMessage+message+Utils.getI18N("notExist");
		default:
			return " something is wrong : "+code;
		}
	}
}

final class broadcastMessageWarning extends MadkitWarning {
	/**
	 * 
	 */
	private static final long serialVersionUID = -6824429451036897060L;

	broadcastMessageWarning(ReturnCode code, String message) {
		super(code,message);	
	}
}

final class RequestRoleWarning extends MadkitWarning {
	/**
	 * 
	 */
	private static final long serialVersionUID = -4736663895867738174L;

	RequestRoleWarning(ReturnCode code, String printCGR) {
		super(code,printCGR);	
	}
}

final class LeaveRoleWarning extends MadkitWarning {
	/**
	 * 
	 */
	private static final long serialVersionUID = 365477089361530831L;

	LeaveRoleWarning(ReturnCode code, String printCGR) {
		super(code,printCGR+code.getMessage());	
	}
}

final class LeaveGroupWarning extends MadkitWarning {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1941486014374980711L;

	LeaveGroupWarning(ReturnCode code, String printCGR) {
		super(code,printCGR);	
	}
}

final class getAgentWithRoleWarning extends MadkitWarning {
	/**
	 * 
	 */
	private static final long serialVersionUID = -703865110702187520L;

	getAgentWithRoleWarning(ReturnCode code, String printCGR) {
		super(code,printCGR);	
	}
}

final class getAgentsWithRoleWarning extends MadkitWarning {
	/**
	 * 
	 */
	private static final long serialVersionUID = 426725091307658380L;

	getAgentsWithRoleWarning(ReturnCode code, String printCGR) {
		super(code,printCGR);	
	}
}

final class LaunchAgentWarning extends MadkitWarning {
	/**
	 * 
	 */
	private static final long serialVersionUID = -1466401740783355792L;

	LaunchAgentWarning(ReturnCode code, String printCGR) {
		super(code,printCGR);	
	}
}
final class killedAgentWarning extends MadkitWarning {
	/**
	 * 
	 */
	private static final long serialVersionUID = -6472468632387543451L;

	killedAgentWarning(ReturnCode code, String printCGR) {
		super(code,printCGR);	
	}
}

final class KilledException extends RuntimeException {
	/**
	 * 
	 */
	private static final long serialVersionUID = -8695603147644262321L;

	KilledException(){}
	
	KilledException(Throwable cause){
		super(cause);
	}
}//TODO get the cause of the kill