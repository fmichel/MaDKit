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
import static madkit.kernel.NetworkMessage.TO_INJECT;
import static madkit.kernel.NetworkMessage.TO_SEND;
import static madkit.kernel.AbstractAgent.State.*;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

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
	final public static String NETWORK_COMMUNITY = "system";
	final public static String NETWORK_GROUP = "kernels";
	final public static String NETWORK_ROLE = "net agent";

	final private ConcurrentHashMap<KernelAddress, KernelConnection> peers;

	private KernelServer myServer;
	private MultiCastListener multicastListener;
	private boolean alive = true;

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
		setName(super.getName()+"@"+kernel.getKernelAddress(this).getID());
		//		setLogLevel(Level.FINE);
		setLogLevel(Level.FINER);
		createGroup(NETWORK_COMMUNITY, NETWORK_GROUP, true, null);
		requestRole(NETWORK_COMMUNITY, NETWORK_GROUP, NETWORK_ROLE, null);
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
				if(m instanceof NetworkMessage<?> && (((NetworkMessage<?>)m).getCode() == NEW_PEER_DETECTED || ((NetworkMessage<?>)m).getCode() == NEW_PEER_REQUEST) )
					handleNetworkMessage((NetworkMessage<?>) m);
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

	private void handleMessage(Message m){
		if(m instanceof NetworkMessage<?>)
			handleNetworkMessage((NetworkMessage<?>) m);
		else
			handleNormalMessage(m);
	}

	/**
	 * @param s
	 * @param startConnection start to receive message if living 
	 */
	synchronized private void addPeer(Socket s, boolean startConnection) {
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
		addConnection(dka,kc, startConnection);		
	}

	/**
	 * @param kc
	 * @param startConnection
	 */
	private void addConnection(KernelAddress ka, KernelConnection kc, boolean startConnection) {
		peers.put(ka,kc);
		if(logger != null)
			logger.info("\n\t\t\t\t----- "+kernel.getKernelAddress(this)+" now connected with MadKit kernel "+kc.getKernelAddress()+"------\n");
		if (startConnection) {
			kc.start();
		}
	}

	/**
	 * @param packet
	 * @param startConnection start to receive message if living 
	 */
	private void contactPeer(DatagramPacket packet, boolean startConnection) {
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
		addConnection(dka,kc, startConnection);		
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
		HashMap<String,HashMap<String,HashMap<String,List<AgentAddress>>>> org;
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
			logger.finer("Local org is\n\n"+getLocalOrg()+"\n");
		}
		try {
			kc.sendConnectionInfo(kernel.getKernelAddress(this), getLocalOrg());
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
	private void broadcastUpdate(NetworkMessage<?> message) {
		if(logger != null && ! peers.isEmpty()){
			logger.finer("Broadcasting "+message+" to "+peers.values());
			logger.finer("Local org is\n\n"+getLocalOrg()+"\n");
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

	synchronized HashMap<String, HashMap<String, HashMap<String, List<AgentAddress>>>> getLocalOrg(){
		return kernel.getLocalOrg(this);
	}

	/**
	 * @param org
	 */
	synchronized void importDistantOrg(HashMap<String, HashMap<String, HashMap<String, List<AgentAddress>>>> org) {
		if(logger != null){
			logger.finer("Importing distant org "+org);
			kernel.importDistantOrg(this, org);
			logger.finer("Local org is now ");
//			kernel.logCurrentOrganization(this.getLogger(),Level.FINER);
		}
		else
			kernel.importDistantOrg(this, org);
	}

	private synchronized boolean sendDistantMessage(MessageConveyor m){
		if(logger != null)
			logger.finer("sending "+m+" to "+m.getContent().getReceiver().getKernelAddress());
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
			logger.info("\n\t\t\t\t----- Network is being closed on "+myServer.getIp()+" port "+myServer.getPort()+" ------\n");
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

	/**
	 * @param kernelAddress
	 */
	synchronized void deconnectWith(KernelAddress kernelAddress) {
		if(! alive || peers.remove(kernelAddress) == null)
			return;
		if(logger != null)
			logger.finer("deconnected from "+kernelAddress);
		kernel.removeAgentsFromDistantKernel(this,kernelAddress);
		System.err.println(kernel.getLocalOrg(this));
	}

	/**
	 * @param waitNextMessage
	 */
	private synchronized void handleNetworkMessage(NetworkMessage<?> message) {
		switch (message.getCode()) {
		case TO_SEND:
			if(logger != null)
				logger.finer("Receiving message to send: "+message);
			message.setCode(TO_INJECT);
			//			message.setSender(new AgentAddress(this, role, ka)); //TODO would be nice to set it !
			if(message instanceof MessageConveyor)
				sendDistantMessage((MessageConveyor) message);
			else
				broadcastUpdate(message); // it is a cgr update
			break;
		case TO_INJECT:
			if(message instanceof MessageConveyor){
				if(logger != null)
					logger.finer("Receiving message to inject: "+message);
//				if (((MessageConveyor)message).getContent() != null) {
//					System.err.println(getName() + "\n\n\n\n-------------------- ID = "
//							+ ((MessageConveyor)message).getContent().getID() + "\n\n\n");
//				}
				kernel.injectMessage(this, ((MessageConveyor)message).getContent());
			}
			else {
				final CGRSynchroMessage m = (CGRSynchroMessage) message;
				if(logger != null){
					logger.finer("Receiving message to inject: "+m);
					logger.finer("Local org before update:\n");
//					kernel.logCurrentOrganization(this,Level.FINER);
					kernel.injectOperation(this, m.getOperation(),m.getContent());
					logger.finer("Local org after update:\n");
//					kernel.logCurrentOrganization(this,Level.FINER);
				}
				else
					kernel.injectOperation(this, m.getOperation(),m.getContent());
			}
			break;
		case STOP_NETWORK:
			if(logger != null)
				logger.fine("Stopping network ");
			alive = false;
			break;
		case NEW_PEER_DETECTED:
			contactPeer(((NewPeerMessage) message).getContent(),getState().equals(LIVING));
			break;
		case NEW_PEER_REQUEST:
			addPeer(((NewPeerConnectionRequest) message).getContent(),getState().equals(LIVING));
			break;
		default:
			break;
		}
	}
	//
	//	private void PrintLocalOrg(){
	//		if(logger != null)
	//			logger.finest("local org : "+kernel.getLocalOrg().toString());
	//	}
}
