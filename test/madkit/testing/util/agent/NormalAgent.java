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
package madkit.testing.util.agent;

import static madkit.kernel.AbstractAgent.ReturnCode.SUCCESS;
import static madkit.kernel.JunitMadkit.COMMUNITY;
import static madkit.kernel.JunitMadkit.GROUP;
import static madkit.kernel.JunitMadkit.ROLE;
import static org.junit.Assert.assertEquals;

import java.util.logging.Level;

import madkit.kernel.Agent;

/**
 * @author Fabien Michel
 * @since MaDKit 5.0.0.12
 * @version 0.91
 * 
 */
public class NormalAgent extends Agent {

	public NormalAgent() {
		setName(getLogger().getName());
	}

	@Override
	protected void activate() {
		createGroupIfAbsent(COMMUNITY, GROUP, true);
		assertEquals(SUCCESS, requestRole(COMMUNITY, GROUP, ROLE));
		getLogger().setLevel(Level.ALL);
	}
	
	@Override
	protected void live() {//need the override !
		getLogger().setLevel(Level.ALL);
		System.err.println(Thread.currentThread().getThreadGroup().isDestroyed());
	}
	
	public static void main(String[] args) {
		executeThisAgent("--kernelLogLevel","ALL","--agentLogLevel","ALL","--createLogFiles","--debug");
	}
	
}
