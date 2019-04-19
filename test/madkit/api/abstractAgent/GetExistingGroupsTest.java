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
package madkit.api.abstractAgent;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

import madkit.agr.LocalCommunity;
import madkit.kernel.AbstractAgent;
import madkit.kernel.JunitMadkit;

/**
 * @author Fabien Michel
 * @since MaDKit 5.0.0.20
 * @version 0.9
 * 
 */
@SuppressWarnings("all")
public class GetExistingGroupsTest extends JunitMadkit {

    @Test
    public void onlyLocal() {
	launchTestV2(new AbstractAgent() {
	    protected void activate() {
		System.err.println(getExistingGroups(LocalCommunity.NAME));
		assertEquals(4, getExistingGroups(LocalCommunity.NAME).size());
		assertEquals(LocalCommunity.Groups.GUI, getExistingGroups(LocalCommunity.NAME).first());
	    }
	});
    }

    @Test
    public void notFound(){
	launchTestV2(new AbstractAgent() {
	    protected void activate() {
		assertNull(getExistingGroups(dontExist()));
	    }
	});
    }

    @Test
    public void createNewAndLeave() {
	launchTestV2(new AbstractAgent() {
	    protected void activate() {
		createGroup("aa", "g");
		assertEquals(1, getExistingGroups("aa").size());
		assertEquals("g", getExistingGroups("aa").first());
		createGroup("aa", "g2");
		assertEquals(2, getExistingGroups("aa").size());
		leaveGroup("aa", "g");
		assertEquals(1, getExistingGroups("aa").size());
		assertEquals("g2", getExistingGroups("aa").first());
		leaveGroup("aa", "g2");
		assertEquals(1, getExistingCommunities().size());
		assertEquals(LocalCommunity.NAME, getExistingCommunities().first());
	    }
	});
    }

}
