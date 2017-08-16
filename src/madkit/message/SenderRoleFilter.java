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
package madkit.message;

import madkit.kernel.AgentAddress;
import madkit.kernel.Message;


/**
 * A filter that accepts message based on the sender's role.
 * 
 * @author Fabien Michel
 * @since MaDKit 5.0.4
 * @version 0.9
 *
 */
public class SenderRoleFilter implements MessageFilter {
	
	
	private final String community, group, role;

	/**
	 * a new filter that acts according to the sender's CGR location.
	 * 
	 * @param community 
	 * @param group 
	 * @param role the role that the sender must have
	 */
	public SenderRoleFilter(final String community, final String group, final String role) {
		this.community = community;
		this.group = group;
		this.role = role;
	}

	@Override
	public boolean accept(final Message m) {
		final AgentAddress sender = m.getSender();
		return sender.getCommunity().equals(community) && sender.getGroup().equals(group) && sender.getRole().equals(role);
	}

}
