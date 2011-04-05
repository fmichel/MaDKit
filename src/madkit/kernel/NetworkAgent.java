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

import static madkit.kernel.NetworkMessage.*;
import static madkit.kernel.AbstractAgent.State.*;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.SortedMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

import madkit.kernel.Madkit.Roles;
import madkit.messages.ObjectMessage;

/**
 * @author Fabien Michel
 * @version 0.9
 * @since MadKit 5
 *
 */
final public class NetworkAgent extends Agent {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6961163274458902519L;

	final private ConcurrentHashMap<KernelAddress, KernelConnection> peers;

	private KernelServer myServer;
	private MultiCastListener multicastListener;
	private boolean alive = true;
	
	private AgentAddress kernelAgent,netInjecter,netUpdater;

	/**
	 * 
	 */
	public NetworkAgent() {
		peers = new ConcurrentHashMap<KernelAddress, KernelConnection>();
		//		setLogLevel(Level.OFF);
	}

	/**
	 * @return the netConfig
	 */
	public KernelServer getNetConfig() {
		return myServer;
	}

	/* (non-Javadoc)
	 * @see madkit.kernel.AbstractAgent#activate()
	 */
	@Override
	protected void activate() {
		setName(super.getName()+getKernelAddress());

		requestRole(Roles.LOCAL_COMMUNITY, Roles.NETWORK_GROUP, Roles.NETWORK_ROLE);
		requestRole(Roles.LOCAL_COMMUNITY, Roles.NETWORK_GROUP, "updater",null);

		kernelAgent = getAgentWithRole(Roles.LOCAL_COMMUNITY, Roles.NETWORK_GROUP, Roles.GROUP_MANAGER_ROLE);
		//black magic here
		myThread.setPriority(Thread.MAX_PRIORITY-3);
		if(kernelAgent == null)
			throw new AssertionError(this+" no kernel agent to with... Please bug report");
		try {
			netUpdater = kernel.getRole(Roles.LOCAL_COMMUNITY, Roles.NETWORK_GROUP, "updater").getAgentAddressOf(this);
		} catch (CGRNotAvailable e) {
			throw new AssertionError("Kernel Agent initialization problem");
		}
		//		setLogLevel(Level.FINE);
		setLogLevel(Level.FINER);
		//requestRole(NetworkConfig.NETWORK_COMMUNITY, NetworkConfig.NETWORK_GROUP, )
		

		//build server
		myServer = KernelServer.getNewKernelServer();
		if(myServer == null){
			if(logger != null)
				logger.warning("\n\t\t\t\t---- Unable to start the Madkit kernel server: No network will be available ------\n");
			alive = false;
			return;
		}
		myServer.activate(this);
		if(logger != null)
			logger.info("\n\t\t\t\t----- MadKit server activated on "+myServer.getIp()+" port "+myServer.getPort()+" ------\n");

		//build multicast listener
		multicastListener = MultiCastListener.getNewMultiCastListener();
		if(multicastListener == null){
			if(logger != null)
				logger.warning("\n\t\t\t\t---- Unable to start a Multicast Listener... ------\n");
		}
		else{
			multicastListener.activate(this, myServer.getIp(), myServer.getPort());
			if(logger != null){
				logger.info("\n\t\t\t\t----- MadKit MulticastListener activated on "+MultiCastListener.MC_IP+" port "+MultiCastListener.multiCastPort+" ------\n");
				logger.finest("Broadcasting existence");
			}			
		}
		Message m = null;
		final ArrayList<Message> toDoList = new ArrayList<Message>();

		do {
			if(logger != null)
				logger.finest("Waiting for some connections first");
			m = waitNextMessage(500);
			if (m != null) {
				if(m.getSender() != null)
					handlePrivateMessage(m);
				else
					toDoList.add(m);
			}
		} 
		while (m != null);

		if(logger != null)
			logger.finest("Now purge mailbox");
		for (Message message : toDoList) {
			handleMessage(message);
		} 
		if(logger != null)
			logger.finest("Now activating all connections");
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

	@SuppressWarnings("unchecked")
	private void handleMessage(Message m) throws ClassCastException{
		AgentAddress sender = m.getSender();
		if(sender == null){//contacted by my private objects
			handlePrivateMessage(m);
		}
		else if(isLocal(sender)){//contacted locally
			if(sender.getRole().equals("updater")){//It is a CGR update
				if(logger != null)
					logger.finer("Local CGR update");
				broadcastUpdate(m);
			}
			else{//It is a message to send elsewhere role is "emmiter"
				if(logger != null)
					logger.finer("Local message to send "+m);
				sendDistantMessage((ObjectMessage<Message>) m);
			}
		}
		else{//distant message
			if(sender.getRole().equals("updater")){//It is a distant CGR update
				if(logger != null)
					logger.finer("distant CGR update");
				kernel.injectOperation((CGRSynchro) m);
			}
			else{//It is a distant message to inject : role is "emmiter"
				if(logger != null)
					logger.finer("Injecting distant message "+getState()+" : "+m);
				kernel.injectMessage((ObjectMessage<Message>) m);
			}
		}
	}

	@SuppressWarnings("unchecked")
	private void handlePrivateMessage(Message m) {
		if(m instanceof NetworkMessage<?>){
			switch (((NetworkMessage<?>)m).getCode()) {
			case NEW_PEER_DETECTED:
				contactPeer(((NetworkMessage<DatagramPacket>) m).getContent());
				break;
			case NEW_PEER_REQUEST:
				addPeer(((NetworkMessage<Socket>) m).getContent());
				break;
			case PEER_DECONNECTED:
				deconnectFromPeer(((NetworkMessage<KernelAddress>) m).getContent());
				break;
			default:
				if(logger != null)
					logger.info("I did not understand this private message "+m);
				break;
			}
		}
		else{
			if(logger != null)
				logger.fine("Stopping network ");
			alive = false;
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
	synchronized private void addPeer(Socket s) {
		if(logger != null)
			logger.fine("Contacted by peer "+s+" -> sending connection message");
		KernelConnection kc = null;
		try {
			kc = new KernelConnection(this,s);
		} catch (IOException e) {
			if(logger != null)
				logger.warning("I give up: Unable to contact peer on "+s+" because "+e.getMessage());
			return;
		}
		if(! sendingConnectionInfo(kc))
			return;
		if(logger != null)
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
		if(logger != null)
			logger.info("\n\t\t\t\t----- "+getKernelAddress()+" now connected with MadKit kernel "+kc.getKernelAddress()+"------\n");
		if (startConnection) {
			kc.start();
		}
	}

	/**
	 * @param packet
	 * @param startConnection start to receive message if living 
	 */
	private void contactPeer(DatagramPacket packet) {
		if(logger != null)
			logger.fine("New peer detected on "+packet.getAddress()+" port = "+packet.getPort()+" -> sending connection message");
		KernelConnection kc = null;
		try {
			kc = new KernelConnection(this,packet.getAddress(),packet.getPort());
		} catch (UnknownHostException e) {
			if(logger != null)
				logger.warning("I give up: Unable to contact peer on "+packet.getAddress()+" port = "+packet.getPort()+" because "+e.getMessage());
			return;
		} catch (IOException e) {
			if(logger != null)
				logger.warning("I give up: Unable to contact peer on "+packet.getAddress()+" port = "+packet.getPort()+" because "+e.getMessage());
			//			e.printStackTrace();
			return;
		}
		KernelAddress dka = gettingConnectionInfo(kc);
		if(dka == null)
			return;
		if(logger != null){
			logger.finest("Now replying to "+dka);
		}
		if(! sendingConnectionInfo(kc))
			return;
		addConnection(dka,kc, getState().equals(LIVING));		
	}

	/**
	 * @param kc
	 * @return
	 */
	private KernelAddress gettingConnectionInfo(KernelConnection kc) {
		if(logger != null)
			logger.finest("Waiting for distant kernel address info...");
		KernelAddress dka;
		try {
			dka = kc.waitForDistantKernelAddress();
		} catch (IOException e) {
			if(logger != null)
				logger.warning("I give up: Unable to get distant kernel address info on "+kc.getInetAddress()+" port = "+kc.getPort()+" because "+e.getMessage());
			return null;
		} catch (ClassNotFoundException e) {
			if(logger != null)
				logger.warning("I give up: Unable to get distant kernel address info on "+kc.getInetAddress()+" port = "+kc.getPort()+" because "+e.getMessage());
			return null;
		}
		if(logger != null){
			logger.finest("... Distant Kernel Address is "+dka);
			logger.finest("Waiting for distant organization info...");
		}
		kc.setKernelAddress(dka);
		SortedMap<String, SortedMap<String, SortedMap<String, Set<AgentAddress>>>> org;
		try {
			org = kc.waitForDistantOrg();
		} catch (IOException e) {
			if(logger != null)
				logger.warning("I give up: Unable to get distant organization from "+dka+" because "+e.getMessage());
			return null;
		} catch (ClassNotFoundException e) {
			if(logger != null)
				logger.warning("I give up: Unable to get distant organization from "+dka+" because "+e.getMessage());
			return null;
		}
		if(logger != null){
			logger.finer("... Distant organization received from "+dka+" Org is\n\n"+org+"\n");
			logger.finest("Now importing org...");
		}
		if(org == null){
			///TODO something wrong : exit
		}
		importDistantOrg(org);
		return dka;
	}

	private boolean sendingConnectionInfo(KernelConnection kc){
		if(logger != null){
			logger.fine("Sending connection info to "+(kc.getKernelAddress() == null ? kc.getDistantKernelSocket() : kc.getKernelAddress()));
			logger.finer("Local org is\n\n"+getOrganizationSnapShot(false)+"\n");
		}
		try {
			kc.sendConnectionInfo(getKernelAddress(), getOrganizationSnapShot(false));
		} catch (IOException e) {
			if(logger != null)
				logger.warning("I give up: Unable to send connection info to "+(kc.getKernelAddress() == null ? kc.getDistantKernelSocket() : kc.getKernelAddress())+" because "+e.getMessage());
			return false;
		}
		return true;
	}

	/**
	 * @param message
	 */
	private void broadcastUpdate(Message message) {
		if(logger != null && ! peers.isEmpty()){
			logger.finer("Broadcasting "+" to "+peers.values()+message);
			logger.finer("Local org is\n\n"+getOrganizationSnapShot(false)+"\n");
		}
		for (KernelConnection kc : peers.values()) {
			kc.sendMessage(message);
		}
	}

	/**
	 * @param message
	 */
	private void handleNormalMessage(Message message) {
		if(logger != null)
			logger.finer("receiving a non network message "+message);
	}
	
//	@Override
//	void terminate() {
//		new Exception().printStackTrace();
//		super.terminate();
//	}

	/**
	 * @param org
	 */
	synchronized void importDistantOrg(SortedMap<String, SortedMap<String, SortedMap<String, Set<AgentAddress>>>> org) {
		if(logger != null){
			logger.finer("Importing distant org "+org);
			kernel.importDistantOrg(org);
			logger.finer("Local org is now ");
//			kernel.logCurrentOrganization(this.getLogger(),Level.FINER);
		}
		else
			kernel.importDistantOrg(org);
	}

	private synchronized boolean sendDistantMessage(ObjectMessage<Message> m){
		if(logger != null)
			logger.finer("sending to "+m.getContent().getReceiver().getKernelAddress()+m);
		//		System.err.println("\n\n\n"+peers+"\n\n\n");
		KernelConnection kc = peers.get(m.getContent().getReceiver().getKernelAddress());
		if(kc == null){
			//			System.err.println("\n\n\n-------------------PB with"+m);
			return false;
		}
		kc.sendMessage(m);
		return true;
	}

	/* (non-Javadoc)
	 * @see madkit.kernel.AbstractAgent#end()
	 */
	@Override
	synchronized protected void end() {
		if(logger != null)
			logger.info("\n\t\t\t\t----- Network is being closed on "+getKernelAddress()+" ------\n");
//		logger.info("\n\t\t\t\t----- Network is being closed on "+myServer.getIp()+" port "+myServer.getPort()+" ------\n");
		if(logger != null)
			logger.finer("Closing all connections : "+peers.values());
		for (KernelConnection kc : peers.values()) {
			kc.closeConnection();
		}
		if(logger != null)
			logger.finer("Closing multicast listener");
		multicastListener.stop();
		if(logger != null)
			logger.finer("Closing kernel server: ");
		myServer.stop();
		super.end();
	}

}