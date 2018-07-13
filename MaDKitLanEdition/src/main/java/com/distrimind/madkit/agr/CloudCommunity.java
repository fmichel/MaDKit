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

/**
 * Defines the default groups and roles used for networking.
 * 
 * @author Fabien Michel
 * @author Jason Mahdjoub
 * @since MaDKitGroupEdition 1.0
 * @version 1.0
 * 
 */
public class CloudCommunity implements Organization {// TODO check groups protection

	public static final String NAME = "~~Cloud";

	/**
	 * Default groups in the Cloud community.
	 * 
	 * @since MaDKitGroupEdition 1.0
	 */
	public static final class Groups {
		// public static final Group NETWORK_AGENTS=new Group(false, null, true, NAME,
		// "~~network agents");
		/*
		 * public static final Group DISTANT_KERNEL_AGENTS =
		 * PRINCIPAL_GROUP.getSubGroup(false, null, false, "~~distant kernel agents");
		 * public static final Group LOCAL_NETWORKS_AGENTS =
		 * PRINCIPAL_GROUP.getSubGroup(false, null, false, "~~local networks agents");
		 * 
		 * public static Group getLocalLanGroup(InetAddress inet_address) { return
		 * LOCAL_NETWORKS_AGENTS.getSubGroup(false, null, false,
		 * "~~LocalLan:"+inet_address.toString()); }
		 * 
		 * public static Group getNioAgentGroup(InetAddress inet_address, )
		 * 
		 * public static final Group LOCAL_NETWORK_AGENTS =
		 * LOCAL_NETWORK_AGENTS.getSubGroup(false, null, false, "~~local lan agent");
		 * 
		 * public static final Group SOCKET_AGENTS =
		 * DISTANT_KERNEL_AGENTS.getSubGroup(false, null, false, "~~socket agents");
		 * public static final Group DIRECT_SOCKET_AGENTS =
		 * SOCKET_AGENTS.getSubGroup(false, null, false, "~~direct socket agents");
		 * public static final Group INDIRECT_SOCKET_AGENTS =
		 * SOCKET_AGENTS.getSubGroup(false, null, false, "~~indirect socket agents");
		 * public static final Group NIO_AGENTS = SOCKET_AGENTS.getSubGroup(false, null,
		 * false, "~~nio agents");
		 * 
		 * 
		 * 
		 * static final String ROLE_NIO_AGENT="~~NIOAgent";
		 * 
		 * static final Group LAN_GROUP=PRINCIPAL_GROUP.getSubGroup(true,
		 * "~~LAN_GROUP"); static final Group
		 * LAN_NOTIFICATIONS_GROUP=LAN_GROUP.getSubGroup(true, "~~LAN_NOTIFICATIONS");
		 * static final String
		 * LAN_IDENTIFIER_NOTIFICATIONS_ROLE="~~LAN_IDENTIFIER_NOTIFICATIONS"; static
		 * final String LAN_GROUP_NOTIFICATIONS_ROLE="~~LAN_GROUP_NOTIFICATIONS"; static
		 * final String LAN_NOTIFIER="~~LAN_NOTIFIER";
		 * 
		 * static final Group LAN_ROUTER_GROUP=LAN_GROUP.getSubGroup(true,
		 * "~~LAN_ROUTER"); //static final Group
		 * LAN_ROUTER_GROUP_FOR_DISTANT_KERNEL=LAN_GROUP.getSubGroup(true,
		 * "~~AgentsForDistantKernel"); /*static final Group
		 * LAN_RECEPTION_GROUP=LAN_GROUP.getSubGroup(true, "~~LAN_RECEPTION"); static
		 * final Group LAN_EMISSION_GROUP=LAN_GROUP.getSubGroup(true,
		 * "~~LAN_RECEPTION"); static final String
		 * LAN_EMISSION_ROLE="~~LAN_EMISSION_ROLE"; static final String
		 * LAN_RECEPTION_ROLE="~~LAN_RECEPTION_ROLE";
		 */

		/*
		 * Identify the agent responsible of the discovery of the local infrastructure
		 * (UPNP IGD routers, network interfaces).
		 */
		/*
		 * static final String LAN_LOCAL_INFRASTRUCTURE_DISCOVER_ROLE=
		 * "~~LAN_LOCAL_INFRASTRUCTURE_DISCOVER_ROLE"; //static final String
		 * LAN_IGD_NEEDER_ROLE="~~LAN_IGD_NEEDER_ROLE"; static final String
		 * LAN_ROUTER_ROLE="~~LAN_ROUTER_ROLE"; static final String
		 * LAN_KERNEL_ROUTER_ROLE="~~LAN_ROUTER_ROLE"; static final String
		 * LAN_TRANSMITER_ROLE="~~LAN_TRANSMITTER_ROLE";
		 * 
		 * 
		 * 
		 * static final String
		 * SCHEDULER_NAME_FOR_AGENTS_FAKE_THREAD="~~MKLE_AGENTS_FAKE_THREAD_SCHEDULER";
		 */

	}

	/**
	 * Default roles in the Cloud community.
	 * 
	 * @since MaDKitGroupEdition 1.0
	 */
	public static final class Roles {

		// public static final String NET_AGENT="~~NET_AGENT";

		/*
		 * Role taken by distant kernel
		 */
		// public static final String DISTANT_KERNEL_ROLE="~~DISTANT_KERNEL";

		/*
		 * Role taken by socket agents
		 */
		// public static final String
		// SOCKET_AGENT_ROLE=LocalCommunity.Roles.SOCKET_AGENT_ROLE;

	}
}
