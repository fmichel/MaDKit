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
package madkit.kernel;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import madkit.agr.LocalCommunity;
import madkit.agr.LocalCommunity.Groups;
import madkit.agr.LocalCommunity.Roles;
import madkit.network.NetworkCommunity;

/**
 * The Class MadkitTestInstance.
 */
public class MadkitTestInstance extends Madkit {

	/** The Constant helperInstances. */
	public static final List<MadkitTestInstance> helperInstances = new ArrayList<>();

	/** The kernel agent. */
	protected Agent kernelAgent;

	public MadkitTestInstance(String[] args) {
		super(args);
		Field f;
		try {
			f = Madkit.class.getDeclaredField("kernelAgent");
			f.setAccessible(true);
			kernelAgent = (KernelAgent) f.get(this);
			synchronized (helperInstances) {
				helperInstances.add(this);
			}
		} catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
			e.printStackTrace();
		}
	}

	public static MadkitTestInstance getInstance(String... args) {
		MadkitTestInstance madkitTestInstance = new MadkitTestInstance(args);
//		madkitTestInstance.waitForNetworkToBeUp();
		return madkitTestInstance;
	}

	public static MadkitTestInstance getNetworkInstance(String... args) {
		String[] args2 = { "--network" };
		if (args != null) {
			args2 = new String[args.length + 1];
			System.arraycopy(args, 0, args2, 0, args.length);
			args2[args.length] = "--network";
		}
		return getInstance(args2);
	}

	public static MadkitTestInstance launchCustomNetworkInstance(Class<? extends Agent> agentTolaunch) {
		String[] args = { "--agents", agentTolaunch.getName() };
		return getNetworkInstance(args);
	}

	public void assertNetworkStatus(boolean running) {
		if (running) {
			Agent networkAgent = getNetworkAgent();
			assertThat(networkAgent).as("Network agent should be present when network is started").isNotNull();
			Group g = kernelAgent.getOrganization().getGroup(NetworkCommunity.NAME,
					NetworkCommunity.Groups.NETWORK_AGENTS);
			if (g == null || !g.contains(networkAgent)) {
				throw new AssertionError("Network should be running");
			}
		} else {
			Agent networkAgent = getNetworkAgent();
			try {
				Group g = kernelAgent.getOrganization().getGroup(NetworkCommunity.NAME,
						NetworkCommunity.Groups.NETWORK_AGENTS);
				if (g != null && g.contains(networkAgent)) {
					throw new AssertionError("Network should be stopped");
				}
			} catch (CGRNotAvailable e) {
				e.printStackTrace();
			}
		}
	}

	public void waitForNetworkToStop() {
		try {
			Agent networkAgent = getNetworkAgent();
			if (networkAgent == null) {
				return;
			}
			Group g = kernelAgent.getOrganization().getGroup(NetworkCommunity.NAME,
					NetworkCommunity.Groups.NETWORK_AGENTS);
			if (g != null && g.contains(networkAgent)) {
				Thread.sleep(500);
			}
			waitForNetworkToStop();
		} catch (CGRNotAvailable | InterruptedException | NullPointerException e) {
			e.printStackTrace();
		}

	}

	public void waitForNetworkToBeUp() {
		try {
			Agent networkAgent = getNetworkAgent();
			Group g = kernelAgent.getOrganization().getGroup(NetworkCommunity.NAME,
					NetworkCommunity.Groups.NETWORK_AGENTS);
			if (g == null || !g.contains(networkAgent)) {
				Thread.sleep(200);
			}
			waitForNetworkToBeUp();
		} catch (CGRNotAvailable | InterruptedException e) {
			e.printStackTrace();
		}

	}

	/**
	 * @return the kernelAgent
	 */
	public Agent getKernelAgent() {
		return kernelAgent;
	}

	public Organization getOrganization() {
		return kernelAgent.getOrganization();
	}

	public Agent getNetworkAgent() {
		if (!(kernelAgent.kernel instanceof DeadKernel)) {
			AgentAddress networkAgentRoleInLocalCommunity = kernelAgent.getAgentWithRole(LocalCommunity.LOCAL,
					Groups.NETWORK, Roles.NET_AGENT);
			if (networkAgentRoleInLocalCommunity == null) {
				return null;
			}
			return networkAgentRoleInLocalCommunity.getAgent();
		}
		return null;
	}

	public static void cleanUpInstances() {
		synchronized (helperInstances) {
			for (Iterator<MadkitTestInstance> iterator = helperInstances.iterator(); iterator.hasNext();) {
				MadkitTestInstance m = iterator.next();
				System.err.println(
						"------------Cleaning -> " + m.getKernelAgent().getKernelAddress() + " ---------------------");
				m.exit();
				iterator.remove();
				Thread.yield();
			}
			try {
				Thread.sleep(200);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		System.err.println("------------Cleaning help instances done ---------------------\n\n");
	}

	// Cannot another main here !!
}
