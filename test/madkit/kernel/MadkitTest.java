/*
 * Copyright 1997-2012 Fabien Michel, Olivier Gutknecht, Jacques Ferber
 * 
 * This file is part of MaDKit.
 * 
 * MaDKit is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 * 
 * MaDKit is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with MaDKit. If not, see <http://www.gnu.org/licenses/>.
 */
package madkit.kernel;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import madkit.action.KernelAction;
import madkit.kernel.Madkit.BooleanOption;
import madkit.kernel.Madkit.LevelOption;

import org.junit.Test;

/**
 * @author Fabien Michel
 * @since MaDKit 5.0.0.10
 * @version 0.9
 * 
 */
public class MadkitTest {

	static Madkit mk;

	// @Before
	// public void before() {
	// mk = new Madkit(null);
	// }

	/**
	 * 
	 */
	@SuppressWarnings("unused")
	private void testMKlogLevelBoot(String MKLogLevel) {
		System.err.println("\n\n\n\n\n--------------------MK log level = " + MKLogLevel + "-------------------");
		String[] args = { LevelOption.madkitLogLevel.toString(), MKLogLevel };
		new Madkit(args);
		System.err.println("\n\n--------------------MK log level = " + MKLogLevel + "-------------------\n\n\n\n\n");
	}

	@Test
	public void nullArgs() {
		Madkit.main(null);
	}

	@Test
	public void noArg() {
		Madkit m = new Madkit();
		System.err.println(m.args);
	}

	@Test
	public void buildConfigFromArgsTest() {
		Madkit m = new Madkit();
		String[] args = new String[1];
		args[0]="";
		assertTrue(m.buildConfigFromArgs(args).isEmpty());
		assertTrue(m.buildConfigFromArgs(null).isEmpty());
		args = new String[2];
		args[0]="";
		args[1]="";
		assertTrue(m.buildConfigFromArgs(args).isEmpty());
	}

	@Test
	public void mkLogLevelALL() {
		testMKlogLevelBoot("ALL");
	}

	@Test
	public void mkLogLevelFINEST() {
		testMKlogLevelBoot("FINEST");
	}

	@Test
	public void mkLogLevelOFF() {
		testMKlogLevelBoot("OFF");
	}

	@Test
	public void testOptionAutoLogDir() {
		// TODO
	}

	@Test
	public void defaultAgentLogging() throws InterruptedException {
		Madkit m = new Madkit();
		Thread.sleep(100);
		assertNull(m.getKernel().logger);
		String[] args = { BooleanOption.desktop.toString(), "false" };
		m = new Madkit(args);
		Thread.sleep(100);
		assertNull(m.getKernel().logger);
		String[] argss = { Madkit.Option.launchAgents.toString(), "madkit.kernel.Agent" };
		m = new Madkit(argss);
		Thread.sleep(100);
		assertNull(m.getKernel().logger);
	}
	
	@Test
	public void doActionLaunchAgentWithDesktop() throws InterruptedException{
		Madkit m = new Madkit();
		Agent a = new Agent();
		m.doAction(KernelAction.LAUNCH_AGENT, a, true);
		Thread.sleep(100);
		assertTrue(a.isAlive());
		Thread.sleep(1000);
	}

	@Test
	public void doActionLaunchAgentNoDesktop() throws InterruptedException{
		Madkit m = new Madkit(BooleanOption.desktop.toString(),"false");
		Agent a = new Agent();
		m.doAction(KernelAction.LAUNCH_AGENT, a, true);
		Thread.sleep(100);
		assertTrue(a.isAlive());
		Thread.sleep(1000);
		m.doAction(KernelAction.LAUNCH_AGENT, a = new Agent(), true);
		Thread.sleep(2000);
		assertTrue(a.isAlive());
	}

	@SuppressWarnings("unused")
	@Test
	public void buildSessionTest() {
		new Madkit(BooleanOption.desktop.toString(), "false", "--kernelLogLevel", "ALL");
	}

}
