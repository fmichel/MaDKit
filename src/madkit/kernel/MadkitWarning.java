/*
 * Copyright or © or Copr. Fabien Michel, Olivier Gutknecht, Jacques Ferber (1997)

fmichel@lirmm.fr
olg@no-distance.net
ferber@lirmm.fr

This software is a computer program whose purpose is to 
provide a lightweight Java library for designing and simulating Multi-Agent Systems (MAS).

This software is governed by the CeCILL-C license under French law and
abiding by the rules of distribution of free software.  You can  use, 
modify and/ or redistribute the software under the terms of the CeCILL-C
license as circulated by CEA, CNRS and INRIA at the following URL
"http://www.cecill.info". 

As a counterpart to the access to the source code and  rights to copy,
modify and redistribute granted by the license, users are provided only
with a limited warranty  and the software's author,  the holder of the
economic rights,  and the successive licensors  have only  limited
liability. 

In this respect, the user's attention is drawn to the risks associated
with loading,  using,  modifying and/or developing or reproducing the
software by the user in light of its specific status of free software,
that may mean  that it is complicated to manipulate,  and  that  also
therefore means  that it is reserved for developers  and  experienced
professionals having in-depth computer knowledge. Users are therefore
encouraged to load and test the software's suitability as regards their
requirements in conditions enabling the security of their systems and/or 
data to be ensured and,  more generally, to use and operate it in the 
same conditions as regards security. 

The fact that you are presently reading this means that you have had
knowledge of the CeCILL-C license and that you accept its terms.
 */

package madkit.kernel;

import madkit.i18n.I18nUtilities;
import madkit.kernel.AbstractAgent.ReturnCode;

/**
 * @author Fabien Michel
 * @version 0.91
 * @since MaDKit 5.0
 * 
 */
class MadkitWarning extends Exception {

	private static final long serialVersionUID = -5977801961418382065L;
	protected final ReturnCode code;

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
	private static final long serialVersionUID = -1096664441558015062L;
	private final String community;
	private final String group;
	private final String role;

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

final class SelfKillError extends ThreadDeath {

	/**
	 * @return the timeOut
	 */
	public int getTimeOut() {
		return timeOut;
	}

	private static final long serialVersionUID = -6883135491234461609L;
	private final int timeOut;

	public SelfKillError(int timeOut) {
		this.timeOut = timeOut;
	}

}