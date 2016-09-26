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

class RequestRoleSecure extends ObjectMessage<Object>{

	/**
	 * 
	 */
	private static final long	serialVersionUID	= 1661974372588706717L;
	final private AgentAddress	requester;
	final private String	roleName;

	public RequestRoleSecure(AgentAddress requester, String roleName, Object key) {
		super(key);
		this.requester = requester;
		this.roleName = roleName;
	}

	/**
	 * @return the requester
	 */
	AgentAddress getRequester() {
		return requester;
	}

	/**
	 * @return the roleName
	 */
	public String getRoleName() {
		return roleName;
	}
}