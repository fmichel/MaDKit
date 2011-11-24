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

import madkit.messages.CommandMessage;
import madkit.messages.ObjectMessage;

/**
 * @author Fabien Michel
 * @since MadKit 5
 * @version 1.1
 * 
 */
enum NetCode {
	NEW_PEER_DETECTED,
	PEER_DECONNECTED,
	NEW_PEER_REQUEST;
}

class NetworkMessage extends CommandMessage<NetCode> {
	
	public NetworkMessage(NetCode code, Object... commandOptions) {
		super(code,commandOptions);
	}


}
//class NewPeerMessage extends NetCode<DatagramPacket>{
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
//class NewPeerConnectionRequest extends NetCode<Socket>{
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