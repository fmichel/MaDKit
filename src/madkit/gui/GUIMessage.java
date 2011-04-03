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
package madkit.gui;

import madkit.kernel.AbstractAgent;
import madkit.messages.ObjectMessage;

/**
 * @author Fabien Michel
 * @since MadKit 5.0.0.9
 * @version 0.9
 * 
 */
public class GUIMessage extends ObjectMessage<AbstractAgent> {
	
	private static final long serialVersionUID = 4586925325007253999L;
	final private GuiCode code;

	public GUIMessage(GuiCode code, AbstractAgent content) {
		super(content);
		this.code = code;
	}


	public GuiCode getCode() {
		return code;
	}
	
	public enum GuiCode {
		SETUP_GUI,
		
		DISPOSE_GUI, 
		
		SHUTDOWN,
		
//		KILL_AGENT,
	}

}
