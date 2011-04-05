/*
 * Copyright 1997-2011 Fabien Michel, Olivier Gutknecht, Jacques Ferber
 * 
 * This file is part of MadKit.
 * 
 * MadKit is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 * 
 * MadKit is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with MadKit. If not, see <http://www.gnu.org/licenses/>.
 */
package madkit.gui;

import java.awt.Color;
import java.util.logging.Level;

import javax.swing.JFrame;
import javax.swing.JPanel;

import madkit.gui.OutputPanel;
import madkit.kernel.Agent;
import madkit.kernel.AgentAddress;
import madkit.kernel.Madkit;
import madkit.messages.ObjectMessage;

/**
 * @author Fabien Michel
 * @since MadKit 5.0.0.9
 * @version 0.9
 * 
 */
public class GUITest extends Agent
{
	private AgentAddress currentPartner = null;
	private String message="yes";


	@Override
	public void activate()
	{
//		setLogLevel(Level.OFF);
		createGroupIfAbsent("ping-pong","room",true, null);
		requestRole("ping-pong","room","player",null);
	}

	@Override
	public void live()
	{
		while(true){
			playing();
		}
	}

	private void playing() {
		while (true) {
			pause(1000);
			getLogger().fine("infoying");
			setLogLevel(Level.INFO);
		}
	}
	
	@Override
	protected void end() {
		if(logger != null)
			logger.info("bye");
	}
	
	public static void main(String[] args) {
		String[] argss = {"--agentLogLevel","ALL","--launchAgents",GUITest.class.getName(),",true"};
		Madkit.main(argss);		
	}

}