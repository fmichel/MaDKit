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

import static org.junit.Assert.fail;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import madkit.kernel.AbstractAgent.ReturnCode;
import madkit.kernel.Madkit.Roles;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import static org.junit.Assert.*;
import static madkit.kernel.AbstractAgent.ReturnCode.*;

/**
 * @author Fabien Michel
 * @since MadKit 5.0.0.7
 * @version 0.9
 * 
 */
public class JunitMadKit {
	
	@Rule 
	public TestName name = new TestName();

	/**
	 * 
	 */
	public static String aString = "a";
	private static final long serialVersionUID = -2081776239604083402L;
	public static final String COMMUNITY = "Tcommunity";
	public static final String GROUP = "Tgroup";
	public static final String ROLE = "Trole";

	public static String testTitle;
	private Madkit m;

	protected List<String> mkArgs = new ArrayList<String>(Arrays.asList(
			"--"+Madkit.warningLogLevel,"INFO",
			"--"+Madkit.launchAgents,"madkit.kernel.AbstractAgent",
			"--"+Madkit.logDirectory,getBinTestDir(),
			"--"+Madkit.agentLogLevel,"INFO",
			"--"+Madkit.MadkitLogLevel,"INFO"));

	public void launchTest(AbstractAgent a, ReturnCode expected){
		System.err.println("\n\n------------------------ "+name.getMethodName()+" TEST START ---------------------");
		try {
			m = new Madkit((String[]) mkArgs.toArray(new String[mkArgs.size()]));
			AbstractAgent kernelAgent = m.getKernel().getAgentWithRole(null,Roles.LOCAL_COMMUNITY, Roles.SYSTEM_GROUP, Roles.KERNEL_ROLE).getAgent();
			assertEquals(expected, kernelAgent.launchAgent(a));
		} catch (Throwable e) {
			System.err.println("\n\n\n------------------------------------");
			while(e.getCause() != null)
				e = e.getCause();
			e.printStackTrace();
			System.err.println("------------------------------------\n\n\n");
			fail(JunitMadKit.class.getSimpleName());
		}
		finally{
			System.err.println("\n\n------------------------ "+name.getMethodName()+" TEST FINISHED ---------------------\n\n");
		}
	}
	public void noExceptionFailure(){
		fail("Exception not thrown");
	}
	public void launchTest(AbstractAgent a){
		launchTest(a, SUCCESS);
}
	
	public MadkitKernel getKernel(){
		return m.getKernel();
	}
	
	public void addMadkitArgs(String... string){
		mkArgs.addAll(Arrays.asList(string));
	}

	public static String getBinTestDir(){
		return "bin";
	}

	public void test() {
		launchTest(new AbstractAgent());
	}
	
	public static String aa(){
		return aString+="a";
	}

	
	static long time;

	public static void startTimer(){
		time = System.nanoTime();
	}

	public static long stopTimer(String message){
		long t = System.nanoTime()-time;
		System.err.println(message+(t/1000000)+" ms");
		return t;
	}


	static public void printMemoryUsage(){
		//System.gc();
		Long mem = (Runtime.getRuntime().totalMemory()-Runtime.getRuntime().freeMemory());
		System.err.println("\n----used memory: "+mem.toString().substring(0, 3)+" Mo\n");			
	}

	/**
	 * @param i
	 */
	public static void pause(int millis) {
		try {
			Thread.sleep(millis);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

	}


}
