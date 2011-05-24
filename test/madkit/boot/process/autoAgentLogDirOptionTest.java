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
package madkit.boot.process;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;

import org.junit.AfterClass;
import org.junit.Test;

/**
 * @author Fabien Michel
 * @since MadKit 5.0.0.6
 * @version 0.9
 * 
 */
public class autoAgentLogDirOptionTest {

	static long fileSize = 0;
	static String agentLogFileName = "[AbstractAgent-3]";
	static String endOfCommandLine = " --agentLogLevel FINEST --launchAgents madkit.kernel.AbstractAgent";

	public String getBinTestDir(){
		return "bin"+File.separator+getClass().getPackage().getName().replace('.', File.separatorChar)+File.separator;
	}

	@Test
	public void noOptionTest() throws IOException, InterruptedException{
		String option = "--autoAgentLogDirectory --desktop false --launchAgents madkit.kernel.AbstractAgent";
		System.err.println("testing "+option+"\n");
		Process p = Runtime.getRuntime().exec("java -cp bin madkit.kernel.Madkit "+option);
		p.waitFor();
		assertFalse(new File(agentLogFileName).exists());
	}

	@Test
	public void missingSlash() throws IOException, InterruptedException{
		//		String[] argss = {"--autoAgentLogDirectory","bin","--launchAgents","madkit.kernel.AbstractAgent","--agentLogLevel","FINEST"};
		//		Madkit.main(argss);
		String option = "--createLogFiles bin"+endOfCommandLine;
		System.err.println("testing "+option+"\n");
		Process p = Runtime.getRuntime().exec("java -cp bin madkit.kernel.Madkit "+option);
		p.waitFor();
		assertTrue(new File("bin/"+agentLogFileName).exists());
	}

	@Test
	public void trueOptionNoDir() throws IOException, InterruptedException{
		String option = "--autoAgentLogDirectory "+endOfCommandLine;
		System.err.println("testing "+option+"\n");
		Process p = Runtime.getRuntime().exec("java -cp bin madkit.kernel.Madkit "+option);
		p.waitFor();
		assertTrue(new File(agentLogFileName).exists());
		fileSize = new File(agentLogFileName).length();
	}

	@Test
	public void trueAppendOptionNoDir() throws IOException, InterruptedException{
		String option = "--autoAgentLogDirectory ;true"+endOfCommandLine;
		System.err.println("testing "+option+"\n");
		Process p = Runtime.getRuntime().exec("java -cp bin madkit.kernel.Madkit "+option);
		p.waitFor();
		assertTrue(new File(agentLogFileName).exists());
		assertTrue(new File(agentLogFileName).length() > fileSize);
	}


	@Test
	public void dirOption() throws IOException, InterruptedException {
		String option = "--autoAgentLogDirectory "+getBinTestDir()+endOfCommandLine;
		System.err.println("testing "+option+"\n");
		Process p = Runtime.getRuntime().exec("java -cp bin madkit.kernel.Madkit "+option);
		p.waitFor();
		Runtime.getRuntime().exec("java -cp bin madkit.kernel.Madkit "+option);
		assertTrue(new File(getBinTestDir()+agentLogFileName).exists());
		fileSize = new File(getBinTestDir()+agentLogFileName).length();
	}


	@Test
	public void dirOptionAppendTrue() throws IOException, InterruptedException {
		String option = "--autoAgentLogDirectory "+getBinTestDir()+";true "+endOfCommandLine;
		System.err.println("testing "+option+"\n");
		Process p = Runtime.getRuntime().exec("java -cp bin madkit.kernel.Madkit "+option);
		p.waitFor();
		assertTrue(new File(getBinTestDir()+agentLogFileName).exists());
		assertTrue(new File(getBinTestDir()+agentLogFileName).length() > fileSize);
		fileSize = new File(getBinTestDir()+agentLogFileName).length();
	}

	@Test
	public void overwriteFileTest() throws IOException, InterruptedException {
		String option = "--autoAgentLogDirectory "+getBinTestDir()+";fAlse "+endOfCommandLine;
		System.err.println("testing "+option+"\n");
		Process p = Runtime.getRuntime().exec("java -cp bin madkit.kernel.Madkit "+option);
		p.waitFor();
		assertTrue(new File(getBinTestDir()+agentLogFileName).exists());
		assertTrue(new File(getBinTestDir()+agentLogFileName).length() < fileSize);
	}

//	@Test
//	public void newFileTest() throws IOException, InterruptedException {
//		String option = "--autoAgentLogDirectory "+getBinTestDir()+endOfCommandLine;
//		System.err.println("testing "+option+"\n");
//		Process p = Runtime.getRuntime().exec("java -cp bin madkit.kernel.Madkit "+option);
//		p.waitFor();
//		assertTrue(new File(getBinTestDir()+agentLogFileName).exists());
//	}

	@AfterClass
	public static void clean(){
		new File(agentLogFileName).deleteOnExit();		
	}

}