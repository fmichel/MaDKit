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
package madkit.messages;

import madkit.gui.actions.MadkitActions;

/**
 * The brand new version of KernelMessage.
 * For now its purpose is to allow agents to send to the kernel agent
 * some MadKit commands such as launchAgent.
 * 
 * @author Fabien Michel
 * @author Olivier Gutknecht
 * @version 5.0
 * @since MadKit 1.0
 *
 */
public class KernelMessage extends CodeMessage<MadkitActions,Object[]> {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 7786756984291853285L;

	public KernelMessage(MadkitActions code, Object... commandOptions) {
		super(code, commandOptions);
	}
}
