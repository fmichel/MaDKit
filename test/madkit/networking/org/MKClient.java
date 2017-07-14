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
package madkit.networking.org;

import static madkit.kernel.AbstractAgent.ReturnCode.SUCCESS;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import madkit.agr.LocalCommunity;
import madkit.agr.LocalCommunity.Groups;
import madkit.kernel.Agent;
import madkit.kernel.AgentAddress;
import madkit.kernel.Message;

/**
* @author Fabien Michel
*/
public class MKClient extends Agent {

	String communityName = LocalCommunity.NAME;
	String groupName = Groups.NETWORK;
	String roleName = "net agent";
	private AgentAddress other;

	@Override
	public void activate() {
		getLogger().info("\n\n-----------Client launched ------------\n\n");
		assertEquals(SUCCESS, createGroup("other", "other", true, null));
		assertEquals(SUCCESS, requestRole("other", "other", "client", null));

		pause(1000);
		// ///////////////////////// REQUEST ROLE ////////////////////////
		other = getAgentWithRole("other", "other", "other");
		assertNotNull(other);
		ReturnCode code = createGroup("public", "system", true, null);
		assertEquals(SUCCESS, code);
		code = requestRole("public", "system", "site", null);
		assertEquals(SUCCESS, code);
		code = requestRole("public", "system", "site2", null);
		assertEquals(SUCCESS, code);

		launchServerSideTest();

		// 2
		code = leaveRole("public", "system", "site2");
		assertEquals(SUCCESS, code);
		launchServerSideTest();

		// 3
		assertEquals(SUCCESS, leaveRole("public", "system", "site"));
		launchServerSideTest();

		// 4
		assertEquals(SUCCESS, createGroup("public", "system", true, null));
		assertEquals(SUCCESS, requestRole("public", "system", "site", null));
		launchServerSideTest();

		// 5
		assertEquals(SUCCESS, leaveGroup("public", "system"));
		// assertTrue(isConnected());
		launchServerSideTest();

		// 6
		assertEquals(SUCCESS, createGroup("public", "system", true, null));
		assertEquals(SUCCESS, requestRole("public", "system", "site", null));
		launchServerSideTest();

		// 7
		assertEquals(SUCCESS, leaveGroup("public", "system"));
		assertNotNull(getAgentWithRole("public", "system", "site"));
		// assertTrue(isConnected());
		launchServerSideTest();

		pause(3000);
	}

	/**
	 * 
	 */
	private void launchServerSideTest() {
		sendMessage(other, new Message());
		pause(1000);
	}

}
