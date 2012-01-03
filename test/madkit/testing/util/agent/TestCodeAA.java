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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Arrays;

import javax.swing.JFrame;

import madkit.action.KernelAction;
import madkit.agr.LocalCommunity;
import madkit.agr.LocalCommunity.Groups;
import madkit.agr.Organization;
import madkit.gui.OutputPanel;
import madkit.kernel.AbstractAgent;
import madkit.kernel.Message;
import madkit.message.EnumMessage;
import madkit.message.ObjectMessage;
import madkit.message.StringMessage;

/**
 * @author Fabien Michel
 * @since MadKit 5.0.0.14
 * @version 0.9
 * 
 */
public class TestCodeAA extends AbstractAgent {


	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public void reload(){
		launchAgent(getClass().getName(),0, true);
		killAgent(this);
	}
//	
//	public TestCodeAA() {
//		// TODO Auto-generated constructor stub
//	}
	
	@Override
	public void setupFrame(JFrame frame) {
		OutputPanel outP;
		frame.add(outP = new OutputPanel(this));
		System.setErr(new PrintStream(outP.getOutputStream()));
		System.setOut(new PrintStream(outP.getOutputStream()));
	}

	@Override
	protected void activate() {
		try {
			Class<?> cl = Class.forName("java.lang.Thread");
			Object o = cl.newInstance();
			o.toString();
		} catch (InstantiationException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IllegalAccessException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		Message m = new Message();
		System.err.println(m);
		sendMessage(LocalCommunity.NAME, Groups.SYSTEM, Organization.GROUP_MANAGER_ROLE, m);
		System.err.println(m);

		m = new ObjectMessage<String>("zd");
		System.err.println(m);
		sendMessage(LocalCommunity.NAME, Groups.SYSTEM, Organization.GROUP_MANAGER_ROLE, m);
		System.err.println(m);

		m = new StringMessage("zd");
		System.err.println(m);
		sendMessage(LocalCommunity.NAME, Groups.SYSTEM, Organization.GROUP_MANAGER_ROLE, m);
		System.err.println(m);

		m = new EnumMessage<KernelAction>(KernelAction.COPY, "kj",3);
		System.err.println(m);
		sendMessage(LocalCommunity.NAME, Groups.SYSTEM, Organization.GROUP_MANAGER_ROLE, m);
		System.err.println(m);

		try {
			System.err.println(this.getClass().getConstructor((Class<?>[])null));
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
//		proceedCommandMessage(new EnumMessage<AgentAction>(AgentAction.RELOAD));
//		proceedCommandMessage(new EnumMessage<AgentAction>(AgentAction.LAUNCH_AGENT,"madkit.testing.util.agent.SelfLaunch"));
	}
	
	public static void main(String[] args) {
		executeThisAgent(args);
//		String[] argss = {Option.launchAgents.toString(),"madkit.kernel.AbstractAgent"};
//		Madkit.main(argss);
	}

}
