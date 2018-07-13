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
package com.distrimind.madkit.agr;

import com.distrimind.madkit.kernel.AbstractAgent;
import com.distrimind.madkit.kernel.AgentAddress;
import com.distrimind.madkit.kernel.AgentNetworkID;
import com.distrimind.madkit.kernel.Gatekeeper;
import com.distrimind.madkit.kernel.Group;
import com.distrimind.madkit.kernel.KernelAddress;
import com.distrimind.madkit.kernel.NetworkAgent;
import com.distrimind.madkit.kernel.Task;
import com.distrimind.madkit.kernel.network.KernelAddressInterfaced;

/**
 * @author Fabien Michel
 * @author Jason Mahdjoub
 * @since MaDKitGroupEdition 1.0
 * @version 1.0
 * 
 */
public class LocalCommunity implements Organization {// TODO check groups protection

	public static final String NAME = "~~local";

	/**
	 * 
	 * @author Jason Mahdjoub
	 * @since MaDKitGroupEdition 1.0
	 *
	 */
	public static final class Groups {

		public static final Group SYSTEM_ROOT = new Group(false, new Gatekeeper() {

			@Override
			public boolean allowAgentToCreateSubGroup(Group _parent_group, Group _sub_group,
					final Class<? extends AbstractAgent> requesterClass, AgentNetworkID _agentNetworkID,
					Object _memberCard) {

				return _sub_group.equals(SYSTEM) || _sub_group.equals(KERNELS) || _sub_group.equals(NETWORK)
						|| _sub_group.equals(GUI);
			}

			@Override
			public boolean allowAgentToTakeRole(Group _group, String _roleName,
					final Class<? extends AbstractAgent> requesterClass, AgentNetworkID _agentNetworkID,
					Object _memberCard) {
				return false;
			}
		}, true, NAME, "~~root");

		public static final Group SYSTEM = LocalCommunity.Groups.SYSTEM_ROOT.getSubGroup(false, new Gatekeeper() {

			@Override
			public boolean allowAgentToCreateSubGroup(Group _parent_group, Group _sub_group,
					final Class<? extends AbstractAgent> requesterClass, AgentNetworkID _agentNetworkID,
					Object _memberCard) {
				return requesterClass.getCanonicalName().equals("com.distrimind.madkit.kernel.MadkitKernel");
			}

			@Override
			public boolean allowAgentToTakeRole(Group _group, String _roleName,
					final Class<? extends AbstractAgent> requesterClass, AgentNetworkID _agentNetworkID,
					Object _memberCard) {
				return requesterClass.getCanonicalName().equals("com.distrimind.madkit.kernel.MadkitKernel");
			}
		}, true, "~~system");

		public static final Group KERNELS = LocalCommunity.Groups.SYSTEM_ROOT.getSubGroup(true, new Gatekeeper() {

			@Override
			public boolean allowAgentToTakeRole(Group _group, String _roleName,
					final Class<? extends AbstractAgent> requesterClass, AgentNetworkID _agentNetworkID,
					Object _memberCard) {
				return requesterClass.getCanonicalName().equals("com.distrimind.madkit.kernel.MadkitKernel");
			}

			@Override
			public boolean allowAgentToCreateSubGroup(Group _parent_group, Group _sub_group,
					final Class<? extends AbstractAgent> requesterClass, AgentNetworkID _agentNetworkID,
					Object _memberCard) {
				return requesterClass.getCanonicalName().equals("com.distrimind.madkit.kernel.MadkitKernel");
			}
		}, true, "~~kernels");

		public static final Group GUI = SYSTEM_ROOT.getSubGroup(false, null, true, "~~gui");

		public static final Group TASK_AGENTS = Task.TASK_AGENTS;

		// Network groups

		public static final Group NETWORK = SYSTEM_ROOT.getSubGroup(false, null, true, "~~network");
		public static final Group LOCAL_NETWORKS = NETWORK.getSubGroup(false, new Gatekeeper() {

			@Override
			public boolean allowAgentToCreateSubGroup(Group _parent_group, Group _sub_group,
					final Class<? extends AbstractAgent> requesterClass, AgentNetworkID _agentNetworkID,
					Object _memberCard) {
				return requesterClass.getCanonicalName()
						.equals("com.distrimind.madkit.kernel.network.LocalNetworkAgent")
						|| requesterClass.getCanonicalName().equals("com.distrimind.madkit.kernel.network.NIOAgent");
			}

			@Override
			public boolean allowAgentToTakeRole(Group _group, String _roleName,
					final Class<? extends AbstractAgent> requesterClass, AgentNetworkID _agentNetworkID,
					Object _memberCard) {
				// TODO Auto-generated method stub
				return true;
			}
		}, true, "~~local_networks");
		public static final Group NETWORK_INTERFACES = NETWORK.getSubGroup(false, new Gatekeeper() {

			@Override
			public boolean allowAgentToCreateSubGroup(Group _parent_group, Group _sub_group,
					final Class<? extends AbstractAgent> requesterClass, AgentNetworkID _agentNetworkID,
					Object _memberCard) {
				return requesterClass.getCanonicalName()
						.equals("com.distrimind.madkit.kernel.network.LocalNetworkAffectationAgent");
			}

			@Override
			public boolean allowAgentToTakeRole(Group _group, String _roleName,
					final Class<? extends AbstractAgent> requesterClass, AgentNetworkID _agentNetworkID,
					Object _memberCard) {
				return true;
			}
		}, true, "~~network_interfaces");
		// public static final Group AGENTS_SOCKET_GROUPS=NETWORK.getSubGroup(false,
		// null, true, "~~agents_sockets");
		public static final Group DISTANT_KERNEL_AGENTS_GROUPS = NETWORK.getSubGroup(false, new Gatekeeper() {

			@Override
			public boolean allowAgentToCreateSubGroup(Group _parent_group, Group _sub_group,
					final Class<? extends AbstractAgent> requesterClass, AgentNetworkID _agentNetworkID,
					Object _memberCard) {
				return requesterClass == NetworkAgent.class || requesterClass.getCanonicalName()
						.equals("com.distrimind.madkit.kernel.network.DistantKernelAgent");
			}

			@Override
			public boolean allowAgentToTakeRole(Group _group, String _roleName,
					final Class<? extends AbstractAgent> requesterClass, AgentNetworkID _agentNetworkID,
					Object _memberCard) {
				return requesterClass.getCanonicalName()
						.equals("com.distrimind.madkit.kernel.network.DistantKernelAgent")
						|| requesterClass.getCanonicalName().equals("com.distrimind.madkit.kernel.network.AgentSocket")
						|| requesterClass.getCanonicalName()
								.equals("com.distrimind.madkit.kernel.network.IndirectAgentSocket")
						|| requesterClass == NetworkAgent.class;

			}
		}, true, "~~distant_kernel_agents");

		public static final Group AGENTS_SOCKET_GROUPS = NETWORK.getSubGroup(false, new Gatekeeper() {

			@Override
			public boolean allowAgentToCreateSubGroup(Group _parent_group, Group _sub_group,
					final Class<? extends AbstractAgent> requesterClass, AgentNetworkID _agentNetworkID,
					Object _memberCard) {
				return requesterClass.getCanonicalName().equals("com.distrimind.madkit.kernel.network.AgentSocket")
						|| requesterClass.getCanonicalName()
								.equals("com.distrimind.madkit.kernel.network.IndirectAgentSocket");
			}

			@Override
			public boolean allowAgentToTakeRole(Group _group, String _roleName,
					final Class<? extends AbstractAgent> requesterClass, AgentNetworkID _agentNetworkID,
					Object _memberCard) {
				return requesterClass.getCanonicalName()
						.equals("com.distrimind.madkit.kernel.network.DistantKernelAgent")
						|| requesterClass.getCanonicalName().equals("com.distrimind.madkit.kernel.network.AgentSocket")
						|| requesterClass.getCanonicalName()
								.equals("com.distrimind.madkit.kernel.network.IndirectAgentSocket");

			}
		}, true, "~~agent_socket_groups");

		/*
		 * public static Group getAgentSocketGroup(Group distant_agent_kernel_group, int
		 * agent_socket_hash_code) { return
		 * distant_agent_kernel_group.getSubGroup(Integer.toString(
		 * agent_socket_hash_code)); }
		 */

		private static final Gatekeeper distantKernelAgentSubGatekeeper = new Gatekeeper() {

			@Override
			public boolean allowAgentToCreateSubGroup(Group _parent_group, Group _sub_group,
					final Class<? extends AbstractAgent> requesterClass, AgentNetworkID _agentNetworkID,
					Object _memberCard) {
				return requesterClass.getCanonicalName()
						.equals("com.distrimind.madkit.kernel.network.DistantKernelAgent")
						|| requesterClass.getCanonicalName().equals("com.distrimind.madkit.kernel.network.AgentSocket")
						|| requesterClass.getCanonicalName()
								.equals("com.distrimind.madkit.kernel.network.IndirectAgentSocket");
			}

			@Override
			public boolean allowAgentToTakeRole(Group _group, String _roleName,
					final Class<? extends AbstractAgent> requesterClass, AgentNetworkID _agentNetworkID,
					Object _memberCard) {
				return requesterClass.getCanonicalName()
						.equals("com.distrimind.madkit.kernel.network.DistantKernelAgent")
						|| requesterClass.getCanonicalName().equals("com.distrimind.madkit.kernel.network.AgentSocket")
						|| requesterClass.getCanonicalName()
								.equals("com.distrimind.madkit.kernel.network.IndirectAgentSocket");
			}
		};

		public static Group getAgentSocketGroup(long agentID) {
			return AGENTS_SOCKET_GROUPS.getSubGroup(false, distantKernelAgentSubGatekeeper, "agentSocket" + agentID);
		}

		/*
		 * public static Group getDistantKernelAgentGroup(KernelAddress ka) { return
		 * DISTANT_KERNEL_AGENTS_GROUPS.getSubGroup(false,
		 * distantKernelAgentSubGatekeeper, ka.toString()); }
		 */

		public static Group getDistantKernelAgentGroup(AgentAddress aa) {
			return getDistantKernelAgentGroup(aa.getAgentNetworkID());
		}

		public static Group getDistantKernelAgentGroup(AgentNetworkID nid) {
			return DISTANT_KERNEL_AGENTS_GROUPS.getSubGroup(false, distantKernelAgentSubGatekeeper, nid.toString());
		}

		public static Group getOriginalDistantKernelAgentGroup(KernelAddress ka) {
			String subGroup = "~~orginal_kernel_addresses";
			if (ka instanceof KernelAddressInterfaced)
				return DISTANT_KERNEL_AGENTS_GROUPS.getSubGroup(false, distantKernelAgentSubGatekeeper, subGroup,
						((KernelAddressInterfaced) ka).getOriginalKernelAddress().toString());
			else
				return DISTANT_KERNEL_AGENTS_GROUPS.getSubGroup(false, distantKernelAgentSubGatekeeper, subGroup,
						ka.toString());
		}

		/*
		 * private static String getHexString(byte[] bytes) { StringBuilder sb = new
		 * StringBuilder(bytes.length*2); for (byte b : bytes) { sb.append(
		 * String.format("%x", new Byte(b)) ); } return sb.toString(); }
		 */

		/*
		 * public static Group getLocalLanGroup(InterfaceAddress interface_address) {
		 * return LOCAL_NETWORKS.getSubGroup(false, null, false,
		 * "~~LocalLan:"+interface_address.toString()); }
		 * 
		 * 
		 * public static Group getNetworkInterfaceGroup(NetworkInterface
		 * network_interface) throws SocketException { return
		 * NETWORK_INTERFACES.getSubGroup(false, null, false,
		 * "~~NI:"+getHexString(network_interface.getHardwareAddress())); }
		 */

	}

	/**
	 * Default roles within a MaDKit organization.
	 * 
	 * @since MaDKitGroupEdition 1.0
	 */
	public static final class Roles {

		/**
		 * The value of this constant is {@value}.
		 */
		public static final String KERNEL = "~~kernel";
		public static final String UPDATER = "~~updater";
		public static final String EMMITER = "~~emmiter";
		public static final String SECURITY = "~~security";
		public static final String GUI = "~~gui";

		/**
		 * This role is given to internal MadKit agents that manage task execution
		 */
		public static final String TASK_MANAGER_ROLE = Task.TASK_MANAGER_ROLE;

		/**
		 * This role is automatically given to agents that launch tasks
		 */
		public static final String TASK_ASKER_ROLE = Task.TASK_ASKER_ROLE;

		// network roles

		/**
		 * Represents the management of the local network
		 */
		public static final String LOCAL_NETWORK_AFFECTATION_ROLE = "~~LOCAL_NETWORK_AFFECTATION";

		/**
		 * Represents the the local network agents
		 */
		public static final String LOCAL_NETWORK_ROLE = "~~LOCAL_NETWORK";

		/**
		 * Represents the affectation of NioAgents to network interfaces
		 */
		public static final String NIO_AFFECTATION_ROLE = "~~NIO_AFFECTATION";

		/**
		 * Role that explore local network
		 */
		public static final String LOCAL_NETWORK_EXPLORER_ROLE = "~~LOCAL_NETWORK_EXPLORER";

		/**
		 * Role taken by NIO agents
		 */
		public static final String NIO_ROLE = "~~nio";

		/**
		 * Role taken by socket agents
		 */
		public static final String SOCKET_AGENT_ROLE = "~~SOCKET_AGENT_KERNEL";

		/**
		 * Role taken by socket agents
		 */
		public static final String MASTER_SOCKET_AGENT_ROLE = "~~MASTER_SOCKET_AGENT_KERNEL";

		/**
		 * Role taken by transfer agents
		 */
		public static final String TRANSFER_AGENT_ROLE = "~~TRANSFER_AGENT_ROLE";

		/**
		 * Role taken by socket agents
		 */
		public static final String DISTANT_KERNEL_AGENT_ROLE = "~~DISTANT_KERNEL_AGENT_ROLE";

		/**
		 * Role taken by network agents
		 */
		public static final String NET_AGENT = "~~net agent";

		/**
		 * Role taken by multicast listener agents
		 */
		public static final String MULTICAST_LISTENER_ROLE = "~~MULTICAST_LISTENER_ROLE";

	}

	public static class BlackBoards {
		public static final String NETWORK_BLACKBOARD = "~~NETWORK_BLACKBOARD";
	}
}
