/*
 * Copyright 1997-2011 Fabien Michel, Olivier Gutknecht, Jacques Ferber
 * 
 * This file is part of MadKit.
 * 
 * MadKit is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * MadKit is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with MadKit. If not, see <http://www.gnu.org/licenses/>.
 */
package madkit.kernel;

import static madkit.kernel.AbstractAgent.State.LIVING;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

import madkit.gui.GUIToolkit;
import madkit.kernel.Madkit.Roles;
import madkit.messages.ObjectMessage;

/**
 * @author Fabien Michel
 * @version 0.9
 * @since MadKit 5
 *
 */
final class NetworkAgent extends Agent {//TODO if logger != null

	/**
	 * 
	 */
	private static final long serialVersionUID = 6961163274458902519L;

	final private ConcurrentHashMap<KernelAddress, KernelConnection> peers = new ConcurrentHashMap<KernelAddress, KernelConnection>();

	private KernelServer myServer;
	private MultiCastListener multicastListener;
	private boolean alive = true;
	
	private AgentAddress kernelAgent;

//	/**
//	 * 
//	 */
//	NetworkAgent() {
//		peers = new ConcurrentHashMap<KernelAddress, KernelConnection>();
//		//		setLogLevel(Level.OFF);
//	}

//	/**
//	 * @return the netConfig
//	 */
//	public KernelServer getNetConfig() {
//		return myServer;
//	}

	/* (non-Javadoc)
	 * @see madkit.kernel.AbstractAgent#activate()
	 */
	@Override
	protected void activate() {
		setName(super.getName()+getKernelAddress());

		requestRole(Roles.LOCAL_COMMUNITY, Roles.NETWORK_GROUP, Roles.NETWORK_ROLE);
//		requestRole(Roles.LOCAL_COMMUNITY, Roles.NETWORK_GROUP, "updater",null);

		kernelAgent = getAgentWithRole(Roles.LOCAL_COMMUNITY, Roles.NETWORK_GROUP, Roles.GROUP_MANAGER_ROLE);
		//black magic here
		myThread.setPriority(Thread.MAX_PRIORITY-3);
		if(kernelAgent == null)
			throw new AssertionError(this+" no kernel agent to with... Please bug report");
//		try {
//			netUpdater = kernel.getRole(Roles.LOCAL_COMMUNITY, Roles.NETWORK_GROUP, "updater").getAgentAddressOf(this);
//		} catch (CGRNotAvailable e) {
//			throw new AssertionError("Kernel Agent initialization problem");
//		}
		//		setLogLevel(Level.FINE);
		setLogLevel(Level.FINER);
		//requestRole(NetworkConfig.NETWORK_COMMUNITY, NetworkConfig.NETWORK_GROUP, )
		

		//build server
		myServer = KernelServer.getNewKernelServer();
		if(myServer == null){
			getLogger().warning("\n\t\t\t\t---- Unable to start the Madkit kernel server: No network will be available ------\n");
			alive = false;
			return;
		}
		myServer.activate(this);
		getLogger().info("\n\t\t\t\t----- MadKit server activated on "+myServer.getIp()+" port "+myServer.getPort()+" ------\n");

		//build multicast listener
		multicastListener = MultiCastListener.getNewMultiCastListener(myServer.getPort());
		if(multicastListener == null){
			getLogger().warning("\n\t\t\t\t---- Unable to start a Multicast Listener... ------\n");
		}
		else{
			multicastListener.activate(this, myServer.getIp(), myServer.getPort());
			getLogger().info("\n\t\t\t\t----- MadKit MulticastListener activated on "+MultiCastListener.ipAddress+" ------\n");
			getLogger().finest("Broadcasting existence");
		}
		GUIToolkit.updateAgentsUI();
		Message m = null;
		final ArrayList<Message> toDoList = new ArrayList<Message>();

		do {
			getLogger().finest("Waiting for some connections first");
			m = waitNextMessage(500);
			if (m != null) {
				if(m.getSender() == null)
					handlePrivateMessage((NetworkMessage<?>) m);
				else
					toDoList.add(m);
			}
		} 
		while (m != null);

		getLogger().finest("Now purge mailbox");
		
		for (Message message : toDoList) {
			handleMessage(message);
		} 
		getLogger().finest("Now activating all connections");
		for (KernelConnection kc : peers.values()) {
			if (! kc.isActivated()) {
				kc.start();
			}
		}
	}

	/* (non-Javadoc)
	 * @see madkit.kernel.Agent#live()
	 */
	@Override
	protected void live() {
		while(alive){
			handleMessage(waitNextMessage());
		}
	}

		@Override
		protected void end() {
			leaveGroup(Roles.LOCAL_COMMUNITY, Roles.NETWORK_GROUP);
			GUIToolkit.updateAgentsUI();
			getLogger().info("\n\t\t\t\t----- Network is being closed on "+getKernelAddress()+" ------\n");
	//		logger.info("\n\t\t\t\t----- Network is being closed on "+myServer.getIp()+" port "+myServer.getPort()+" ------\n");
			getLogger().finer("Closing all connections : "+peers.values());
			for (KernelConnection kc : peers.values()) {
				kc.closeConnection();
			}
			getLogger().finer("Closing multicast listener");
			multicastListener.stop();
			getLogger().finer("Closing kernel server: ");
			myServer.stop();
		}

	@SuppressWarnings("unchecked")
	private void handleMessage(Message m) throws ClassCastException{
		AgentAddress sender = m.getSender();
		if(sender == null){//contacted by my private objects
			handlePrivateMessage((NetworkMessage<?>) m);
		}
		else if(sender.isLocal()){//contacted locally
			if(sender.getRole().equals("updater")){//It is a CGR update
				getLogger().finer("Local CGR update");
				broadcastUpdate(m);
			}
			else if(sender.getRole().equals("emmiter")){//It is a message to send elsewhere role is "emmiter"
				getLogger().finer("Local message to send "+m);
				sendDistantMessage((ObjectMessage<Message>) m);
			}
			else{
				alive = false;
			}
		}
		else{//distant message
			if(sender.getRole().equals("updater")){//It is a distant CGR update
				getLogger().finer("distant CGR update");
				kernel.injectOperation((CGRSynchro) m);
			}
			else{//It is a distant message to inject : role is "emmiter"
				getLogger().finer("Injecting distant message "+getState()+" : "+m);
				kernel.injectMessage((ObjectMessage<Message>) m);
			}
		}
	}

	@SuppressWarnings("unchecked")
	private void handlePrivateMessage(NetworkMessage<?> m) {
			switch (m.getCode()) {
			case NEW_PEER_DETECTED:
				contactPeer(((NetworkMessage<DatagramPacket>) m).getContent());
				break;
			case NEW_PEER_REQUEST:
				addPeer(((NetworkMessage<Socket>) m).getContent());
				break;
			case PEER_DECONNECTED:
				deconnectFromPeer(((NetworkMessage<KernelAddress>) m).getContent());
				break;
			case FAILURE:
				alive = false;
				break;
			default:
				getLogger().info("I did not understand this private message "+m);
				break;
			}
	}

	private void deconnectFromPeer(KernelAddress ka) {
		if(peers.remove(ka) == null)
			return;
		if(logger != null)
			logger.info("\n\t\t\t\t----- "+getKernelAddress()+" deconnected from MadKit kernel "+ka+"------\n");
		kernel.removeAgentsFromDistantKernel(ka);
		System.err.println(getOrganizationSnapShot(false));
	}

	/**
	 * @param s
	 */
	private void addPeer(Socket s) {
		getLogger().fine("Contacted by peer "+s+" -> sending connection message");
		KernelConnection kc = null;
		try {
			kc = new KernelConnection(this,s);
		} catch (IOException e) {
			getLogger().warning("I give up: Unable to contact peer on "+s+" because "+e.getMessage());
			return;
		}
		if(! sendingConnectionInfo(kc))
			return;
		getLogger().fine("Connection info sent, now waiting reply from "+kc.getDistantKernelSocket()+"...");
		KernelAddress dka = gettingConnectionInfo(kc);
		if(dka == null)
			return;
		addConnection(dka,kc, getState().equals(LIVING));		
	}

	/**
	 * @param kc
	 * @param startConnection start to receive message if living 
	 */
	private void addConnection(KernelAddress ka, KernelConnection kc, boolean startConnection) {
		peers.put(ka,kc);
		getLogger().info("\n\t\t\t\t----- "+getKernelAddress()+" now connected with MadKit kernel "+kc.getKernelAddress()+"------\n");
		if (startConnection) {
			kc.start();
		}
	}

	/**
	 * @param packet
	 * @param startConnection start to receive message if living 
	 */
	private void contactPeer(DatagramPacket packet) {
		getLogger().fine("New peer detected on "+packet.getAddress()+" port = "+packet.getPort()+" -> sending connection message");
		KernelConnection kc = null;
		try {
			kc = new KernelConnection(this,packet.getAddress(),packet.getPort());
		} catch (UnknownHostException e) {
			getLogger().warning("I give up: Unable to contact peer on "+packet.getAddress()+" port = "+packet.getPort()+" because "+e.getMessage());
			return;
		} catch (IOException e) {
			getLogger().warning("I give up: Unable to contact peer on "+packet.getAddress()+" port = "+packet.getPort()+" because "+e.getMessage());
			//			e.printStackTrace();
			return;
		}
		KernelAddress dka = gettingConnectionInfo(kc);
		if(dka == null)
			return;
		getLogger().finest("Now replying to "+dka);
		if(! sendingConnectionInfo(kc))
			return;
		addConnection(dka,kc, getState().equals(LIVING));		
	}

	/**
	 * @param kc
	 * @return
	 */
	private KernelAddress gettingConnectionInfo(KernelConnection kc) {
		getLogger().finest("Waiting for distant kernel address info...");
		KernelAddress dka;
		try {
			dka = kc.waitForDistantKernelAddress();
		} catch (IOException e) {
			getLogger().warning("I give up: Unable to get distant kernel address info on "+kc.getInetAddress()+" port = "+kc.getPort()+" because "+e.getMessage());
			return null;
		} catch (ClassNotFoundException e) {
			getLogger().warning("I give up: Unable to get distant kernel address info on "+kc.getInetAddress()+" port = "+kc.getPort()+" because "+e.getMessage());
			return null;
		}
		getLogger().finest("... Distant Kernel Address is "+dka+"\nWaiting for distant organization info...");
		kc.setKernelAddress(dka);
		try {
			getLogger().finest("Now importing org...");
//			SortedMap<String, SortedMap<String, SortedMap<String, Set<AgentAddress>>>> org = kc.waitForDistantOrg();
//		getLogger().finer("... Distant organization received from "+dka+" Org is\n\n"+org+"\n");
			kernel.importDistantOrg(kc.waitForDistantOrg());
		} catch (IOException e) {
			getLogger().warning("I give up: Unable to get distant organization from "+dka+" because "+e.getMessage());
			return null;
		} catch (ClassNotFoundException e) {
			getLogger().warning("I give up: Unable to get distant organization from "+dka+" because "+e.getMessage());
			return null;
		}
		return dka;
	}

	private boolean sendingConnectionInfo(KernelConnection kc){
		getLogger().fine("Sending connection info to "+(kc.getKernelAddress() == null ? kc.getDistantKernelSocket() : kc.getKernelAddress()));
		getLogger().finer("Local org is\n\n"+getOrganizationSnapShot(false)+"\n");
		try {
			kc.sendConnectionInfo(getKernelAddress(), getOrganizationSnapShot(false));
		} catch (IOException e) {
			getLogger().warning("I give up: Unable to send connection info to "+(kc.getKernelAddress() == null ? kc.getDistantKernelSocket() : kc.getKernelAddress())+" because "+e.getMessage());
			return false;
		}
		return true;
	}

	/**
	 * @param message
	 */
	private void broadcastUpdate(Message message) {
		getLogger().finer("Broadcasting "+" to "+peers.values()+message);
		getLogger().finer("Local org is\n\n"+getOrganizationSnapShot(false)+"\n");
		for (KernelConnection kc : peers.values()) {
			kc.sendMessage(message);
		}
	}

	private boolean sendDistantMessage(ObjectMessage<Message> m){
		getLogger().finer("sending to "+m.getContent().getReceiver().getKernelAddress()+m);
		KernelConnection kc = peers.get(m.getContent().getReceiver().getKernelAddress());
		if(kc == null){
			return false;
		}
		kc.sendMessage(m);
		return true;
	}

}