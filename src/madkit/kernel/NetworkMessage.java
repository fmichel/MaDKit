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

import madkit.messages.ObjectMessage;

/**
 * @author Fabien Michel
 * @since MadKit 5
 * @version 0.9
 * 
 */
class NetworkMessage<T> extends ObjectMessage<T> {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -7003030390114277088L;
	final private NetCode code;

	public NetworkMessage(NetCode code, T content) {
		super(content);
		this.code = code;
	}


	public NetCode getCode() {
		return code;
	}
	
	public enum NetCode {
		NEW_PEER_DETECTED,
		PEER_DECONNECTED,
		NEW_PEER_REQUEST,
		FAILURE
	}

}

//class NewPeerMessage extends NetworkMessage<DatagramPacket>{
//	/**
//	 * 
//	 */
//	private static final long serialVersionUID = -1804809279823744173L;
//
//	/**
//	 * @param content
//	 */
//	public NewPeerMessage(DatagramPacket content) {
//		super(NetCode.NEW_PEER_DETECTED, content);
//	}
//}
//
//class NewPeerConnectionRequest extends NetworkMessage<Socket>{
//
//	private static final long serialVersionUID = 6092436677566809561L;
//
//	/**
//	 * @param content
//	 */
//	public NewPeerConnectionRequest(Socket content) {
//		super(NetCode.NEW_PEER_REQUEST,content);
//	}
//	
//}