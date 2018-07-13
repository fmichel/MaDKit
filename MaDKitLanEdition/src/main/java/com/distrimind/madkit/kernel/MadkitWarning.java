/*
 * MadKitLanEdition (created by Jason MAHDJOUB (jason.mahdjoub@distri-mind.fr)) Copyright (c)
 * 2015 is a fork of MadKit and MadKitGroupExtension. 
 * 
 * Copyright or © or Copr. Jason Mahdjoub, Fabien Michel, Olivier Gutknecht, Jacques Ferber (1997)
 * 
 * jason.mahdjoub@distri-mind.fr
 * fmichel@lirmm.fr
 * olg@no-distance.net
 * ferber@lirmm.fr
 * 
 * This software is a computer program whose purpose is to
 * provide a lightweight Java library for designing and simulating Multi-Agent Systems (MAS).
 * This software is governed by the CeCILL-C license under French law and
 * abiding by the rules of distribution of free software.  You can  use,
 * modify and/ or redistribute the software under the terms of the CeCILL-C
 * license as circulated by CEA, CNRS and INRIA at the following URL
 * "http://www.cecill.info".
 * As a counterpart to the access to the source code and  rights to copy,
 * modify and redistribute granted by the license, users are provided only
 * with a limited warranty  and the software's author,  the holder of the
 * economic rights,  and the successive licensors  have only  limited
 * liability.
 * 
 * In this respect, the user's attention is drawn to the risks associated
 * with loading,  using,  modifying and/or developing or reproducing the
 * software by the user in light of its specific status of free software,
 * that may mean  that it is complicated to manipulate,  and  that  also
 * therefore means  that it is reserved for developers  and  experienced
 * professionals having in-depth computer knowledge. Users are therefore
 * encouraged to load and test the software's suitability as regards their
 * requirements in conditions enabling the security of their systems and/or
 * data to be ensured and,  more generally, to use and operate it in the
 * same conditions as regards security.
 * The fact that you are presently reading this means that you have had
 * knowledge of the CeCILL-C license and that you accept its terms.
 */

package com.distrimind.madkit.kernel;

import com.distrimind.madkit.i18n.I18nUtilities;
import com.distrimind.madkit.kernel.AbstractAgent.ReturnCode;

/**
 * @author Fabien Michel
 * @author Jason Mahdjoub
 * @version 0.92
 * @since MaDKitLanEdition 1.0
 * 
 */
class MadkitWarning extends Exception {

	/**
	 * 
	 */
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
	final private String community, role;
	private final AbstractGroup group;

	/*public OrganizationWarning(ReturnCode code, String _community) {
		super(code);
		this.community = _community;
		this.group = null;
		this.role = null;
	}

	public OrganizationWarning(ReturnCode code, Role _cgr) {
		this(code, _cgr.getGroup(), _cgr.getRole());
	}*/

	public OrganizationWarning(ReturnCode code, Group _group, String _role) {
		super(code);
		this.community = _group.getCommunity();
		this.group = _group;
		this.role = _role;
	}

	public OrganizationWarning(ReturnCode code, AbstractGroup _group, String _role) {
		super(code);
		this.community = null;
		this.group = _group;
		this.role = _role;
	}

	public OrganizationWarning(ReturnCode code, Group _group) {
		this(code, _group, null);
	}

	@Override
	public String getMessage() {
		String msg = code + " : ";
		switch (code) {
		case NOT_GROUP:
		case NOT_IN_GROUP:
		case ALREADY_GROUP:
		case ACCESS_DENIED:
			return msg + I18nUtilities.getCGRString(group);
		case NOT_COMMUNITY:
			return msg + I18nUtilities.getCGRString(community);
		case ROLE_NOT_HANDLED:
		case NOT_ROLE:
		case ROLE_ALREADY_HANDLED:
		case IGNORED:
			return msg + I18nUtilities.getCGRString(group, role);
		default:
			System.err.println("\n\n************** " + code + " result not handled ");
			new Exception().printStackTrace();
			return null;
		}
	}
}
