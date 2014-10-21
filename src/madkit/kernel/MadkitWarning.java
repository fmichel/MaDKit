/*
 * Copyright 1997-2014 Fabien Michel, Olivier Gutknecht, Jacques Ferber
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

import madkit.i18n.I18nUtilities;
import madkit.kernel.AbstractAgent.ReturnCode;

/**
 * @author Fabien Michel
 * @version 0.9
 * @since MaDKit 5.0
 * 
 */
class MadkitWarning extends Exception {

	/**
	 * 
	 */
	private static final long	serialVersionUID	= -5977801961418382065L;
	protected final ReturnCode	code;

	MadkitWarning(String message, ReturnCode code) {
		super(message);
		this.code = code;
	}

	MadkitWarning(ReturnCode code) {
		this.code = code;
	}

	@Override
	public String getMessage() {
		String msg = super.getMessage();
		return code.name() + ": " + (msg == null ? "" : msg + " ") + code.toString();
	}

}

final class OrganizationWarning extends MadkitWarning {

	/**
	 * 
	 */
	private static final long	serialVersionUID	= -1096664441558015062L;
	final private String			community, group, role;

	public OrganizationWarning(ReturnCode code, String community, String group, String role) {
		super(code);
		this.community = community;
		this.group = group;
		this.role = role;
	}

	@Override
	public String getMessage() {
		String msg = code + " : ";
		switch (code) {
		case NOT_GROUP:
		case NOT_IN_GROUP:
		case ALREADY_GROUP:
		case ACCESS_DENIED:
			return msg + I18nUtilities.getCGRString(community, group, null);
		case NOT_COMMUNITY:
			return msg + I18nUtilities.getCGRString(community, null, null);
		case ROLE_NOT_HANDLED:
		case NOT_ROLE:
		case ROLE_ALREADY_HANDLED:
		case IGNORED:
			return msg + I18nUtilities.getCGRString(community, group, role);
		default:
			System.err.println("\n\n************** " + code.name() + " result not handled ");
			new Exception().printStackTrace();
			return null;
		}
	}
}

class KilledException extends Error {// TODO should be runtime exception

	private static final long	serialVersionUID	= -8695603147644262321L;

	public KilledException(String msg) {
		super(msg);
	}

	@Override
	public synchronized Throwable fillInStackTrace() {
		return null;
	}
}

class SelfKillException extends KilledException {

	private static final long	serialVersionUID	= -6883135491234461609L;

	public SelfKillException(String msg) {
		super(msg);
	}

}