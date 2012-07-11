/*
 * Copyright 1997-2012 Fabien Michel, Olivier Gutknecht, Jacques Ferber
 * 
 * This file is part of MaDKit.
 * 
 * MaDKit is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * MaDKit is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with MaDKit. If not, see <http://www.gnu.org/licenses/>.
 */
package madkit.kernel;

import static madkit.kernel.AbstractAgent.State.LIVING;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import madkit.agr.CloudCommunity;
import madkit.agr.LocalCommunity;
import madkit.agr.LocalCommunity.Groups;
import madkit.agr.LocalCommunity.Roles;
import madkit.agr.Organization;
import madkit.gui.AgentStatusPanel;
import madkit.kernel.Madkit.LevelOption;
import madkit.message.EnumMessage;
import madkit.message.ObjectMessage;

/**
 * @author Fabien Michel
 * @version 0.9
 * @since MaDKit 5
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
	private boolean running = true;
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
		setLogLevel(LevelOption.networkLogLevel.getValue(getMadkitConfig()));
		//		setLogLevel(Level.ALL);
		requestRole(LocalCommunity.NAME, Groups.NETWORK, madkit.agr.LocalCommunity.Roles.NET_AGENT);

		kernelAgent = getAgentWithRole(LocalCommunity.NAME, Groups.NETWORK, Organization.GROUP_MANAGER_ROLE);
		myThread.setPriority(Thread.MAX_PRIORITY-3);
		if(kernelAgent == null)
			throw new AssertionError(this+" no kernel agent to work with... Please bug report");

		//build servers
		running = launchNetwork();
	}

	/**
	 * @return true if servers are launched
	 */
	private boolean launchNetwork() {
		if(ReturnCode.SUCCESS != createGroup(CloudCommunity.NAME, CloudCommunity.Groups.NETWORK_AGENTS,true)){
			return false;
		}
		requestRole(CloudCommunity.NAME, CloudCommunity.Groups.NETWORK_AGENTS, CloudCommunity.Roles.NET_AGENT);
		myServer = KernelServer.getNewKernelServer();
		if(myServer == null) {
			if (logger != null) 
				logger.warning("\n\t\t\t\t---- Unable to start the Madkit kernel server ------\n");
			stopNetwork();
			return false;
		}
		myServer.activate(this);
		if (logger != null) 
			logger.config("\n\t\t\t\t----- MaDKit server activated on "+myServer.getIp()+" port "+myServer.getPort()+" ------\n");

		//build multicast listener
		try {
			multicastListener = MultiCastListener.getNewMultiCastListener(myServer.getPort());
			multicastListener.activate(this);
			if (logger != null){
				logger.config("\n\t\t\t\t----- MaDKit MulticastListener activated on "+MultiCastListener.ipAddress+" ------\n");
				logger.finest("Broadcasting existence ");
			}
		} catch (IOException e) {
			if (logger != null){
				logger.warning("\n\t\t\t\t---- Unable to start a Multicast Listener "+e.getClass().getName()+" "+e.getMessage()+" ------\n");
			}
			stopNetwork();
			return false;
		}

		Message m = null;
		final ArrayList<Message> toDoList = new ArrayList<Message>();

		do {
			if (logger != null) logger.finest("Waiting for some connections first");
			m = waitNextMessage(400);
			if (m != null) {
				if(m.getSender() == null && m instanceof NetworkMessage && ((NetworkMessage)m).getCode() == NetCode.NEW_PEER_REQUEST)
					newPeerRequest((Socket) ((NetworkMessage) m).getContent()[0]);//FIXME verify that this is ok
				else
					toDoList.add(m);
			}
		} 
		while (m != null);

		if (logger != null) 
			logger.finest("Now purge mailbox");

		for (final Message message : toDoList) {
			handleMessage(message);
		} 

		if (logger != null) 
			logger.finest("Now activating all connections");
		for (final KernelConnection kc : peers.values()) {
			if (! kc.isActivated()) {
				kc.start();
			}
		}
		AgentStatusPanel.updateAll();
		if (logger != null){
			logger.info("\n\t\t\t\t----- Network has been started on "+getKernelAddress()+" ------\n");
		}
		return true;
	}

	/* (non-Javadoc)
	 * @see madkit.kernel.Agent#live()
	 */
	@Override
	protected void live() {
		while(running){
			handleMessage(waitNextMessage());
		}
	}

	@Override
	protected void end() {
		stopNetwork();
	}

	/**
	 * 
	 */
	private void stopNetwork() {
		if (logger != null){
			logger.info("\n\t\t\t\t----- Network is being closed on "+getKernelAddress()+" ------\n");
			logger.finer("Closing all connections : "+peers.values());
		}
		for(Map.Entry<KernelAddress, KernelConnection> entry : peers.entrySet()){
			peerDeconnected(entry.getKey());
			entry.getValue().closeConnection();
		}
		peers.clear();
		if (logger != null){
			logger.finer("Closing multicast listener and kernel server");
		}
		if (multicastListener != null) {
			multicastListener.stop();
		}
		if (myServer != null) {
			myServer.stop();
		}
		leaveGroup(CloudCommunity.NAME, CloudCommunity.Groups.NETWORK_AGENTS);
		AgentStatusPanel.updateAll();
	}

	@SuppressWarnings("unchecked")
	private void handleMessage(final Message m) throws ClassCastException{
		final AgentAddress sender = m.getSender();
		if(sender == null){//contacted by my private objects (or by the kernel ? no)
			proceedEnumMessage((EnumMessage<?>) m);
		}
		else if(sender.isLocal()){//contacted locally
			final String senderRole = sender.getRole();
			if(senderRole.equals(Roles.UPDATER)){//It is a CGR update
				broadcastUpdate(m);
			}
			else if(senderRole.equals(Roles.EMMITER)){//It is a message to send elsewhere role is Roles.EMMITER
				sendDistantMessage((ObjectMessage<Message>) m);
			}
			else if(senderRole.equals(Roles.KERNEL)){//message from the kernel
				proceedEnumMessage((EnumMessage<?>) m);
				//				handlePrivateMessage((KernelMessage) m);
			}
		}
		else{//distant message
			if(sender.getRole().equals(Roles.UPDATER)){//It is a distant CGR update
				if (logger != null){
					CGRSynchro synchro = (CGRSynchro) m;
					logger.finer("Injecting distant CGR " + synchro.getCode() + " on " + synchro.getContent());
				}
				kernel.injectOperation((CGRSynchro) m);
			}
			else{//It is a distant message to inject : role is Roles.EMMITER
				if (logger != null) logger.finer("Injecting distant message "+getState()+" : "+m);
				kernel.injectMessage((ObjectMessage<Message>) m);
			}
		}
	}

	@SuppressWarnings("unused")
	private void exit(){
		running = false;
	}

	/**
	 * Removes ka from peers and clean organization accordingly
	 * @param ka
	 */
	private void peerDeconnected(KernelAddress ka) {
		if(peers.remove(ka) == null)//TODO log
			return;
		if(logger != null)
			logger.info("\n\t\t\t\t----- "+getKernelAddress()+" deconnected from MaDKit kernel "+ka+"------\n");
		kernel.removeAgentsFromDistantKernel(ka);
		//		System.err.println(getOrganizationSnapShot(false));
	}

	/**
	 * @param s
	 */
	private void newPeerRequest(Socket s) {
		if (logger != null) 
			logger.fine("Contacted by peer "+s+" -> opening kernel connection");
		KernelConnection kc = null;
		try {
			kc = new KernelConnection(this,s);
		} catch (IOException e) {
			if (logger != null) logger.warning("I give up: Unable to contact peer on "+s+" because "+e.getMessage());
			return;
		}
		if(logger != null)
			logger.finer("KC opened: "+kc+"\n\tsending connection INFO");
		if(! sendingConnectionInfo(kc))
			return;
		if (logger != null) 
			logger.fine("Connection info sent, now waiting reply from "+kc.getDistantKernelSocket()+"...");
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
		if (logger != null) logger.info("\n\t\t\t\t----- "+getKernelAddress()+" now connected with MaDKit kernel "+kc.getKernelAddress()+"------\n");
		if (startConnection) {
			kc.start();
		}
	}

	/**
	 * @param packet
	 * @param startConnection start to receive message if living 
	 */
	@SuppressWarnings("unused")
	private void newPeerDetected(DatagramPacket packet) {
		if (logger != null) 
			logger.fine("New peer detected on "+packet.getAddress()+" port = "+packet.getPort()+"\n\t-> opening KernelConnection");
		KernelConnection kc = null;
		try {
			kc = new KernelConnection(this,packet.getAddress(),packet.getPort());
			if(logger != null)
				logger.finer("KC created "+kc);
		} catch (UnknownHostException e) {//TODO java 7
			if (logger != null) 
				logger.warning("I give up: Unable to contact peer on "+packet.getAddress()+" port = "+packet.getPort()+" because "+e.getMessage());
			return;
		} catch (IOException e) {
			if (logger != null) 
				logger.warning("I give up: Unable to contact peer on "+packet.getAddress()+" port = "+packet.getPort()+" because "+e.getMessage());
			return;
		}
		KernelAddress dka = gettingConnectionInfo(kc);
		if(dka == null)
			return;
		if (logger != null) logger.finer("Now replying to "+dka);
		if(! sendingConnectionInfo(kc))
			return;
		addConnection(dka,kc, getState().equals(LIVING));		
	}

	/**
	 * @param kc
	 * @return
	 */
	private KernelAddress gettingConnectionInfo(KernelConnection kc) {
		if (logger != null) 
			logger.finest("Waiting distant kernel address...");
		KernelAddress dka = null;
		try {
			dka = kc.waitForDistantKernelAddress();
			if (logger != null) 
				logger.finest("... Distant Kernel Address is "+dka+"\nWaiting distant organization info...");
//						Map<String, Map<String, Map<String, Set<AgentAddress>>>> org = null;
//						try {
//							org = kc.waitForDistantOrg();
//							if (logger != null) 
//								logger.finer("... Distant organization received from "+dka+" Org is\n\n"+org+"\n");
//							kernel.importDistantOrg(org);
//						} catch (Throwable e) {
//							e.printStackTrace();
//						}
//						
			kernel.importDistantOrg(kc.waitForDistantOrg());
			return dka;
		} catch (IOException e) {
			if(dka == null)
				getLogger().severeLog("I give up: Unable to get distant kernel address info on "+kc.getInetAddress()+" port = "+kc.getPort(), e);
			else
				getLogger().severeLog("I give up: Unable to get distant organization from "+dka,e);
		} catch (ClassNotFoundException e) {
			if(dka == null)
				getLogger().severeLog("I give up: Unable to get distant kernel address info on "+kc.getInetAddress()+" port = "+kc.getPort(),e);
			else
				getLogger().severeLog("I give up: Unable to get distant organization from "+dka,e);
		}
		return null;
	}

	private boolean sendingConnectionInfo(KernelConnection kc){
		if (logger != null){
			logger.fine("Sending connection info to "+(kc.getKernelAddress() == null ? kc.getDistantKernelSocket() : kc.getKernelAddress()));
			logger.finer("Local org is\n\n"+getOrganizationSnapShot(false)+"\n");
		}
		try {
			kc.sendConnectionInfo(getKernelAddress(), getOrganizationSnapShot(false));
		} catch (IOException e) {
			if (logger != null) 
				logger.warning("I give up: Unable to send connection info to "+(kc.getKernelAddress() == null ? kc.getDistantKernelSocket() : kc.getKernelAddress())+" because "+e.getMessage());
			return false;
		}
		return true;
	}

	/**
	 * @param message
	 */
	private void broadcastUpdate(Message message) {
		if (logger != null){
			logger.finer("Local CGR update\nBroadcasting "+" to "+peers.values()+message);
			logger.finest("Local org is\n\n"+getOrganizationSnapShot(false)+"\n");
		}
		for (KernelConnection kc : peers.values()) {
			kc.sendMessage(message);
		}
	}

	private boolean sendDistantMessage(ObjectMessage<Message> m){
		if (logger != null) logger.finer("sending to "+m.getContent().getReceiver().getKernelAddress()+m);
		KernelConnection kc = peers.get(m.getContent().getReceiver().getKernelAddress());
		if(kc == null){
			return false;
		}
		kc.sendMessage(m);
		return true;
	}

}