/*
 * Copyright or © or Copr. Fabien Michel, Olivier Gutknecht, Jacques Ferber (1997)

fmichel@lirmm.fr
olg@no-distance.net
ferber@lirmm.fr

This software is a computer program whose purpose is to
provide a lightweight Java API for developing and simulating Multi-Agent Systems (MAS).

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

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import org.testng.annotations.Test;

import madkit.kernel.Agent.ReturnCode;
import madkit.test.agents.EmptyAgent;

/**
 * @author Fabien Michel
 *
 */
public class KernelAgentTest {

	@Test
	public void testLaunchAgent() {
		// Given the KernelAgent and an Agent are initialized
		KernelAgent kernelAgent = new KernelAgent(new Madkit());
		Agent testAgent = new EmptyAgent();
		assertFalse(testAgent.isAlive());

		// When the launchAgent method is called
		ReturnCode returnCode = kernelAgent.launchAgent(testAgent, 5);

		// Then the agent should be alive and the return code should be SUCCESS
		assertTrue(testAgent.isAlive());
		assertEquals(returnCode, ReturnCode.SUCCESS);
	}

	@Test
	public void givenAgentIsAlive_whenKillAgent_thenAgentShouldBeDead() {
        // Given the KernelAgent and an Agent are initialized
        KernelAgent kernelAgent = new KernelAgent(new Madkit());
        Agent testAgent = new EmptyAgent();
        kernelAgent.launchAgent(testAgent, 5);
        assertTrue(testAgent.isAlive());

        // When the killAgent method is called
        ReturnCode returnCode = kernelAgent.killAgent(testAgent);

        // Then the agent should be dead and the return code should be SUCCESS
        assertFalse(testAgent.isAlive());
        assertEquals(returnCode, ReturnCode.SUCCESS);
    }

	
	@Test
	public void givenAkernelAgent_whenGetKernelConfig_thenShouldReturnMadkitConfig() {
		// Given the KernelAgent is initialized with a Madkit instance
		KernelAgent kernelAgent = new KernelAgent(new Madkit());

		// When the getKernelConfig method is called
		KernelConfig config = kernelAgent.getKernelConfig();

		// Then the returned KernelConfig should be the same as the one set in Madkit
		assertNotNull(config);
	}

}
