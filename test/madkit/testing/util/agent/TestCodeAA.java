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
package madkit.testing.util.agent;

import static madkit.kernel.AbstractAgent.ReturnCode.SUCCESS;
import static madkit.kernel.JunitMadKit.COMMUNITY;
import static madkit.kernel.JunitMadKit.GROUP;
import static madkit.kernel.JunitMadKit.ROLE;
import static org.junit.Assert.assertEquals;

import java.util.Arrays;

import madkit.gui.actions.AgentAction;
import madkit.kernel.AAAction;
import madkit.kernel.AbstractAgent;
import madkit.kernel.Agent;
import madkit.kernel.KernelAction;
import madkit.kernel.Madkit.LevelOption;
import madkit.messages.CommandMessage;

/**
 * @author Fabien Michel
 * @since MadKit 5.0.0.14
 * @version 0.9
 * 
 */
public class TestCodeAA extends AbstractAgent {


	public void reload(){
		launchAgent(getClass().getName(),0, true);
		killAgent(this);
	}
//	
//	public TestCodeAA() {
//		// TODO Auto-generated constructor stub
//	}

	@Override
	protected void activate() {
		try {
			System.err.println(this.getClass().getConstructor((Class<?>[])null));
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
//		proceedCommandMessage(new CommandMessage<AAAction>(AAAction.RELOAD));
//		proceedCommandMessage(new CommandMessage<AAAction>(AAAction.LAUNCH_AGENT,"madkit.testing.util.agent.SelfLaunch"));
	}
	
	public static void main(String[] args) {
		String[] argss = {LevelOption.kernelLogLevel.toString(),"ALL",LevelOption.agentLogLevel.toString(),"ALL"};
		executeThisAgent(argss);
	}

}
