/*******************************************************************************
 * MaDKit - Multi-agent systems Development Kit 
 * 
 * Copyright (c) 1998-2025 Fabien Michel, Olivier Gutknecht, Jacques Ferber...
 * 
 * This software is a computer program whose purpose is to
 * provide a lightweight Java API for developing and simulating 
 * Multi-Agent Systems (MAS) using an organizational perspective.
 *
 * This software is governed by the CeCILL-C license under French law and
 * abiding by the rules of distribution of free software.You can use,
 * modify and/ or redistribute the software under the terms of the CeCILL-C
 * license as circulated by CEA, CNRS and INRIA at the following URL
 * "http://www.cecill.info".
 *
 * As a counterpart to the access to the source code and rights to copy,
 * modify and redistribute granted by the license, users are provided only
 * with a limited warranty and the software's author, the holder of the
 * economic rights, and the successive licensors have only limited
 * liability.
 *
 * In this respect, the user's attention is drawn to the risks associated
 * with loading, using, modifying and/or developing or reproducing the
 * software by the user in light of its specific status of free software,
 * that may mean that it is complicated to manipulate, and that also
 * therefore means that it is reserved for developers and experienced
 * professionals having in-depth computer knowledge. Users are therefore
 * encouraged to load and test the software's suitability as regards their
 * requirements in conditions enabling the security of their systems and/or
 * data to be ensured and, more generally, to use and operate it in the
 * same conditions as regards security.
 *
 * The fact that you are presently reading this means that you have had
 * knowledge of the CeCILL-C license and that you accept its terms.
 *******************************************************************************/
package madkit.network;

import static org.assertj.core.api.Assertions.fail;

import java.util.ArrayList;
import java.util.List;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

import madkit.action.KernelAction;
import madkit.kernel.MadkitConcurrentTestCase;
import madkit.kernel.Madkit;

/**
 * The Class MadkitNetworkConcurrentTestCase.
 */
public class MadkitNetworkConcurrentTestCase extends MadkitConcurrentTestCase {

	private List<Madkit> networkedInstances = new ArrayList<>();

	@Override
	protected String[] getMadkitTestArgs() {
		return new String[] { "--network" };
	}

	@Override
	@BeforeMethod
	public void initMDK() {
		super.initMDK();
		networkedInstances.add(madkit);
	}

//	/**
//	 * Run test with a given agent
//	 * 
//	 */
//	public void runNetworkTest() {
//		launchThreadedMKNetworkInstance();
//		try {
//			await(20000);
//		} catch (TimeoutException | InterruptedException e) {
//			fail("TimeoutException / InterruptedException", e);
//		}
//	}

	@AfterMethod
	public void afterMethod() {
		// Clean up network connections after each test method
		try {
			closeNetworkConnections();
		} catch (Exception e) {
			fail("Failed to close network connections: " + e.getMessage());
		}
	}

	public void closeNetworkConnections() throws InterruptedException {
		for (Madkit m : networkedInstances) {
			m.doAction(KernelAction.STOP_NETWORK);
			m.doAction(KernelAction.EXIT);
		}
		Thread.sleep(1000); // Wait for network connections to close properly
		networkedInstances.clear();
		System.err.println("------------Cleaning help instances done ---------------------\n\n");
	}

//	public void launchThreadedMKNetworkInstance(final Level l) {
//		launchThreadedMKNetworkInstance(l, ForEverReplierAgent.class);
//	}
//
//	public void launchThreadedMKNetworkInstance() {
//		launchThreadedMKNetworkInstance(Level.ALL, ForEverReplierAgent.class);
//	}

//	public void launchThreadedMKNetworkInstance(final Level l, final Class<? extends Agent> agentClass) {
//		new Thread(new Runnable() {
//			@Override
//			public void run() {
//				launchCustomNetworkInstance(l, agentClass.toString());
//			}
//		}).start();
//	}

}
