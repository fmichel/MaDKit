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
package networking;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.net.SocketException;
import java.util.Enumeration;

import org.testng.annotations.Test;

import madkit.action.KernelAction;
import madkit.kernel.Agent;
import madkit.kernel.MadkitConcurrentTestCase;
import madkit.kernel.MadkitTestInstance;
import madkit.messaging.ForEverReplierAgent;

/**
 * The Class MadkitNetworkTest.
 */
@Test(enabled = false)
public class MadkitNetworkTest extends MadkitConcurrentTestCase {

	private static final int MADKIT_PORT = 4444;
	private static final int TIMEOUT = 1000;

//	@Test
	public void givenMadkitWhenNetworkOptionActivatedThenNetworkAgentIsLaunched() throws InterruptedException {
		MadkitTestInstance m = MadkitTestInstance.getNetworkInstance();
		m.assertNetworkStatus(true);
		MadkitTestInstance.cleanUpInstances();
		m.assertNetworkStatus(false);
	}

//	@Test
	public void givenNetworkRunning_whenStopped_thenPortClosed() throws Exception {
		runNetworkTest(() -> {
			// Given: a MaDKit network instance is running
			MadkitTestInstance m = MadkitTestInstance.getNetworkInstance();

			InetAddress currentIp = findInetAddress();
			assertThat(currentIp).as("A usable IPv4 address must be found on the host").isNotNull();

			// When: the network port is listening and then we stop all instances
			assertThat(isPortOpen(currentIp.getHostAddress(), MADKIT_PORT))
					.as("MaDKit network port %d should be listening before stop", MADKIT_PORT).isTrue();

			m.assertNetworkStatus(true);
			m.doAction(KernelAction.STOP_NETWORK);
			m.waitForNetworkToStop();
			m.assertNetworkStatus(false);

//			m.assertNetworkStatus(false);
			// Then: the port should be closed and the network agent should no longer be present
			assertThat(isPortOpen(currentIp.getHostAddress(), MADKIT_PORT))
					.as("MaDKit network port %d should be closed after stop", MADKIT_PORT).isFalse();
			resume();
			MadkitTestInstance.cleanUpInstances();
		});
	}

//	@Test
	public void givenMadkitWhenNetworkOptionActivatedThenPortShouldBeListing() throws IOException {
		// Given: MaDKit kernel with network enabled
		MadkitTestInstance m = MadkitTestInstance.getNetworkInstance();
		String currentIp = findInetAddress().getHostAddress();
		MadkitTestInstance.launchCustomNetworkInstance(ForEverReplierAgent.class);

		Agent na = m.getNetworkAgent();
		System.err.println(na);

		// When: Attempting to connect to MaDKit's network port
		boolean isPortListening = isPortOpen(currentIp, MADKIT_PORT);

		// Then: Verify the port is listening
		assertThat(isPortListening).as("MaDKit network port %d should be listening", MADKIT_PORT).isTrue();
		MadkitTestInstance.cleanUpInstances();
	}

//	@Test
	public void givenForeignInstance_whenClose_thenQuit() {
		runTest(new Agent() {
			@Override
			protected void onActivation() {
				MadkitTestInstance foreignMK = MadkitTestInstance.getNetworkInstance();
				foreignMK.doAction(KernelAction.EXIT);
				resume();
			}
		});
	}

	private boolean isPortOpen(String host, int port) {
		try (Socket socket = new Socket()) {
			socket.connect(new InetSocketAddress(host, port), TIMEOUT);
			return true;
		} catch (IOException e) {
			return false;
		}
	}

	private static InetAddress findInetAddress() {
		try {
			Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces();
			// find:
			while (en.hasMoreElements()) {
				NetworkInterface ni = en.nextElement();
				if (!ni.isLoopback()) {
					final Enumeration<InetAddress> e = ni.getInetAddresses();
					while (e.hasMoreElements()) {
						InetAddress ia = e.nextElement();
						if (!ia.isLoopbackAddress() && ia instanceof Inet4Address) {
							return ia;
						}
					}
				}
			}
		} catch (SocketException e1) {
			e1.printStackTrace();
		}
		return null;
	}

}
