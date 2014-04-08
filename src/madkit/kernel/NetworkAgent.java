/*
 * Copyright 1997-2014 Fabien Michel, Olivier Gutknecht, Jacques Ferber
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

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
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
final class NetworkAgent extends Agent {

	final private ConcurrentHashMap<KernelAddress, KernelConnection> peers = new ConcurrentHashMap<>();

	private KernelServer myServer;
	private MultiCastListener multicastListener;
	private boolean running = true;
	private AgentAddress kernelAgent;

	//	/**
	//	 * 
	//	 */
	//	NetworkAgent() {
	//		peers = new ConcurrentHashMap<><KernelAddress, KernelConnection>();
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
//				setLogLevel(Level.INFO);
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
			logger.config("\n\t\t\t\t----- MaDKit server activated on "+myServer+" ------\n");

//		build multicast listener
		try {
			multicastListener = MultiCastListener.getNewMultiCastListener(myServer.getPort());
			multicastListener.activate(this);
			if (logger != null){
				logger.config("\n\t\t\t\t----- MaDKit MulticastListener activated on "+MultiCastListener.ipAddress+" ------\n");
			}
		} catch (final IOException e) {
			if (logger != null){
				logger.warning("\n\t\t\t\t---- Unable to start a Multicast Listener "+e.getClass().getName()+" "+e.getMessage()+" ------\n");
			}
			stopNetwork();
			return false;
		}

		Message m = null;
		final ArrayList<Message> toDoList = new ArrayList<>();

		do {
			if (logger != null) 
				logger.finest("Waiting for some connections first");
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
			logger.info("\n\t\t\t\t----- "+getKernelAddress()+" network started on "+myServer+" ------\n");
		}
		return true;
	}

	/* (non-Javadoc)
	 * @see madkit.kernel.Agent#live()
	 */
	@Override
	protected void live() {
		while(isAlive() && running){
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
			logger.info("\n\t\t\t\t----- "+getKernelAddress()+" network closed ------\n");
			logger.finer("Closing all connections : "+peers.values());
		}
		for(final Map.Entry<KernelAddress, KernelConnection> entry : peers.entrySet()){
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
			myServer = null;
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
		else if(sender.isFrom(getKernelAddress())){//contacted locally
			switch (sender.getRole()) {
			case Roles.UPDATER://It is a CGR update
				broadcastUpdate(m);
				break;
			case Roles.EMMITER://It is a message to send elsewhere
				sendDistantMessage((ObjectMessage<Message>) m);
				break;
			case Roles.KERNEL://message from the kernel
				proceedEnumMessage((EnumMessage<?>) m);
				break;
			default:
				getLogger().severeLog("not understood :\n"+m);
				break;
			}
		}
		else{//distant message
			switch (sender.getRole()) {
			case Roles.UPDATER:////It is a distant CGR update
				if (logger != null){
					final CGRSynchro synchro = (CGRSynchro) m;
					logger.finer("Injecting distant CGR " + synchro.getCode() + " on " + synchro.getContent());
				}
				getMadkitKernel().injectOperation((CGRSynchro) m);
				break;
			case Roles.EMMITER://It is a distant message to inject
				if (logger != null) 
					logger.finer("Injecting distant message "+getState()+" : "+m);
				getMadkitKernel().injectMessage((ObjectMessage<Message>) m);
				break;
			default:
				getLogger().severeLog("not understood :\n"+m);
				break;
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
	private void peerDeconnected(final KernelAddress ka) {
		if (peers.remove(ka) != null) {
			if (logger != null)
				logger.info("\n\t\t\t\t----- " + getKernelAddress() + " deconnected from " + ka + "------\n");
			getMadkitKernel().removeAgentsFromDistantKernel(ka);
//					System.err.println(getOrganizationSnapShot(false));
		}
	}

	/**
	 * @param s
	 */
	private void newPeerRequest(final Socket s) {
		if (logger != null) 
			logger.fine("Contacted by peer "+s+" -> opening kernel connection");
		KernelConnection kc = null;
		try {
			kc = new KernelConnection(this,s);
		} catch (final IOException e) {
			if (logger != null) logger.warning("I give up: Unable to contact peer on "+s+" because "+e.getMessage());
			return;
		}
		if(logger != null)
			logger.finer("KC opened: "+kc+"\n\tsending connection INFO");
		if (sendingConnectionInfo(kc)) {
			if (logger != null)
				logger.fine("Connection info sent, now waiting reply from " + kc.getDistantKernelSocket() + "...");
			final KernelAddress dka = gettingConnectionInfo(kc);
			if (dka != null)
				addConnection(dka, kc, getState().equals(LIVING));
		}		
	}

	/**
	 * @param kc
	 * @param startConnection start to receive message if living 
	 */
	private void addConnection(final KernelAddress ka, final KernelConnection kc, final boolean startConnection) {
		peers.put(ka,kc);
		if (logger != null) logger.info("\n\t\t\t\t----- "+getKernelAddress()+" now connected with "+kc.getKernelAddress()+"------\n");
		if (startConnection) {
			kc.start();
		}
	}

	/**
	 * @param packet
	 * @param startConnection start to receive message if living 
	 */
	private void newPeerDetected(final DatagramPacket packet) {
		if (logger != null) 
			logger.fine("Contacting peer: "+packet.getAddress()+" port = "+packet.getPort()+"\n\t-> opening KernelConnection");
		KernelConnection kc = null;
		try {
			kc = new KernelConnection(this,packet.getAddress(),packet.getPort());
			if(logger != null)
				logger.finer("KC created "+kc);
		} catch (final IOException e) {
			if (logger != null) 
				logger.warning("Unable to contact peer: "+packet.getAddress()+" port = "+packet.getPort()+" because "+e.getMessage());
			return;
		}
		final KernelAddress dka = gettingConnectionInfo(kc);
		if (dka != null) {
			if (logger != null)
				logger.finer("Now replying to " + dka);
			if (sendingConnectionInfo(kc))
				addConnection(dka, kc, getState().equals(LIVING));
		}		
	}
	
	@SuppressWarnings("unused")//used by reflection
	private void connectToIp(final InetAddress ipAddress) throws IOException{
		if (! ipAddress.equals(myServer.getIp())) {
			final ByteArrayOutputStream bos = new ByteArrayOutputStream();  
			final DataOutputStream dos = new DataOutputStream(bos);  
			dos.writeLong(System.nanoTime());
			dos.close();  
			newPeerDetected(new DatagramPacket(bos.toByteArray(), 8, ipAddress, 4444));
		}
	}

	/**
	 * @param kc
	 * @return
	 */
	private KernelAddress gettingConnectionInfo(final KernelConnection kc) {
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
			kernel.getMadkitKernel().importDistantOrg(cleanUp(kc.waitForDistantOrg(),dka));
			return dka;
		} catch (final IOException |  ClassNotFoundException e) {
			if(dka == null)
				getLogger().severeLog("I give up: Unable to get distant kernel address info on "+kc, e);
			else
				getLogger().severeLog("I give up: Unable to get distant organization from "+dka,e);
		}
		return null;
	}

	private boolean sendingConnectionInfo(final KernelConnection kc){
		if (logger != null){
			logger.fine("Sending connection info to "+(kc.getKernelAddress() == null ? kc.getDistantKernelSocket() : kc.getKernelAddress()));
			logger.finer("Local org is\n\n"+getOrganizationSnapShot(false)+"\n");
		}
		try {
			kc.sendConnectionInfo(getKernelAddress(), getOrganizationSnapShot(false));
		} catch (final IOException e) {
			if (logger != null) 
				logger.warning("I give up: Unable to send connection info to "+(kc.getKernelAddress() == null ? kc.getDistantKernelSocket() : kc.getKernelAddress())+" because "+e.getMessage());
			return false;
		}
		return true;
	}

	private Map<String, Map<String, Map<String, Set<AgentAddress>>>> cleanUp(
			Map<String, Map<String, Map<String, Set<AgentAddress>>>> organizationSnapShot, KernelAddress from) {
		for (Iterator<Entry<String, Map<String, Map<String, Set<AgentAddress>>>>> iterator = organizationSnapShot.entrySet().iterator(); iterator.hasNext();) {
			Entry<String, Map<String, Map<String, Set<AgentAddress>>>> org = iterator.next();
			for (Iterator<Entry<String, Map<String, Set<AgentAddress>>>> iterator2 = org.getValue().entrySet().iterator(); iterator2.hasNext();) {
				Entry<String, Map<String, Set<AgentAddress>>> group = iterator2.next();
				for (Iterator<Entry<String, Set<AgentAddress>>> iterator3 = group.getValue().entrySet().iterator(); iterator3.hasNext();) {
					Entry<String, Set<AgentAddress>> role = iterator3.next();
					for (Iterator<AgentAddress> iterator4 = role.getValue().iterator(); iterator4.hasNext();) {
						final KernelAddress dka = iterator4.next().getKernelAddress();
						if(! from.equals(dka) && ! peers.containsKey(dka))
							iterator4.remove();
					}
					if(role.getValue().isEmpty()){
						iterator3.remove();
					}
				}
				if(group.getValue().isEmpty()){
					iterator2.remove();
				}
			}
			if(org.getValue().isEmpty()){
				iterator.remove();
			}
		}
		return organizationSnapShot;
	}

	/**
	 * @param message
	 */
	private void broadcastUpdate(final Message message) {
		if (logger != null){
			logger.finer("Local CGR update\nBroadcasting "+" to "+peers.values()+message);
//			logger.finest("Local org is\n\n"+getOrganizationSnapShot(false)+"\n");
		}
		for (final KernelConnection kc : peers.values()) {
			kc.sendMessage(message);
		}
	}

	private void sendDistantMessage(final ObjectMessage<Message> m){
		if (logger != null) 
			logger.finer("sending to "+m.getContent().getReceiver().getKernelAddress()+m);
		final KernelConnection kc = peers.get(m.getContent().getReceiver().getKernelAddress());
		if(kc != null){
			kc.sendMessage(m);
		}
		else {
//			if (m.getContent().getReceiver().isFrom(getKernelAddress())){//the agent address which is used has been encapsulated and is not up-to-date (agent)
//				getMadkitKernel().injectMessage(m);
//			}
//			else{
//				m.getContent().getSender().getAgent().handleException(Influence.SEND_MESSAGE, new IOException(m.getContent().getReceiver().getKernelAddress().toString()+" is deconnected")); 
//			}
		}
	}
	
		@Override
		public String getServerInfo() {
			if (myServer != null) {
				return myServer.toString();
			}
			return "";
		}

}