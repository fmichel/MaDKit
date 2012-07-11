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
package madkit.message;

import madkit.action.KernelAction;

/**
 * The brand new version of KernelMessage.
 * For now its purpose is to allow agents to send to the kernel agent
 * some MaDKit commands such as launchAgent.
 * 
 * @author Olivier Gutknecht
 * @author Fabien Michel
 * @version 5.2
 * @since MaDKit 1.0
 *
 */
public class KernelMessage extends EnumMessage<KernelAction> {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 4125669035665965672L;

	public KernelMessage(KernelAction code, Object... commandOptions) {
		super(code, commandOptions);
	}
}
