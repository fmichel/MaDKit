/*
 * Copyright 2012 Fabien Michel
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
package madkit.networking;

import madkit.kernel.Agent;
import madkit.kernel.Gatekeeper;
import madkit.kernel.JunitMadkit;
import madkit.kernel.Message;

/**
 * @author Fabien Michel
 * @since MadKit 5.0.0.20
 * @version 0.9
 * 
 */
public class Denier extends Agent{
	@Override
	protected void activate() {
		createGroup(JunitMadkit.COMMUNITY, JunitMadkit.GROUP, true, new Gatekeeper() {
			@Override
			public boolean allowAgentToTakeRole(String requesterID, String roleName, Object memberCard) {
				return "test".equals(memberCard);
			}
		});
	}
	@Override
	protected void live() {
		while(true) {
			final Message m = waitNextMessage(1000);
			if (m != null) {
				sendReply(m, new Message());
			}
		}
	}
}