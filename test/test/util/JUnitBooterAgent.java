/**
 * 
 */
package test.util;

import static madkit.kernel.AbstractAgent.ReturnCode.SUCCESS;
import static madkit.kernel.AbstractAgent.State.TERMINATED;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import madkit.kernel.AbstractAgent;
import madkit.kernel.Madkit;
import madkit.kernel.Madkit.BooleanOption;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

/**
 * @author fab
 *
 */
public class JUnitBooterAgent extends AbstractAgent {

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

	public static AbstractAgent testAgent;
	public static String testTitle;

	private List<String> mkArgs = Arrays.asList(
			"--"+Madkit.logDirectory,getBinTestDir(),
			"--"+Madkit.launchAgents,getClass().getName());

	public JUnitBooterAgent(){
	}

	public void setTestTitle(String testTitle){
		testTitle = "\n\n------------------------ "+name.getMethodName()+" "+testTitle+" ---------------------\n\n";
	}
	
	public String aa(){
		return aString+="a";
	}

	@Override
	protected void activate(){
		assertEquals(SUCCESS,launchAgent(testAgent));
	}

	public void createTestGroupSuccess(){
		assertEquals(SUCCESS,createGroup(COMMUNITY, GROUP, false,null));
	}

	public void requestRoleTestSuccess(){
		assertEquals(SUCCESS,requestRole(COMMUNITY, GROUP, ROLE));
	}

	public void printTestTitle(){
		System.err.println(testTitle);
	}

	public void addMadkitArgs(String... string){
		mkArgs.addAll(Arrays.asList(string));
	}

	@Test
	public void madkitInit() {
		System.err.println("\n\n------------------------ "+name.getMethodName()+" TEST START ---------------------");
		try {
			Madkit.main((String[]) mkArgs.toArray(new String[mkArgs.size()]));
		} catch (Throwable e) {
			System.err.println("\n\n\n------------------------------------");
			while(e.getCause() != null)
				e = e.getCause();
			e.printStackTrace();
			System.err.println("------------------------------------\n\n\n");
			fail(getClass().getSimpleName());
		}
		finally{
			System.err.println("\n\n------------------------ "+name.getMethodName()+" TEST FINISHED ---------------------\n\n");
		}
	}

	public void launchTest(){
		madkitInit();
	}

	public String getBinTestDir(){
		return "bin"+File.separator+getClass().getPackage().getName().replace('.', File.separatorChar)+File.separator;
	}

	protected void end() {
		System.err.println("\n\t\t\tFINISHED"+testTitle);
	}

	/* (non-Javadoc)
	 * @see madkit.kernel.AbstractMadkitBooter#activate()
	 */

	protected void noAgentConsoleLog(){
		setMadkitProperty(BooleanOption.noAgentConsoleLog.name(), "true");
		assertEquals("true",getMadkitProperty(BooleanOption.noAgentConsoleLog.name()));
	}


	//	protected void setOrgLogLevel(Level level){
	//		setMadkitProperty(Madkit.orgLogLevel, level.toString());
	//		assertEquals(level.toString(),getMadkitProperty(Madkit.orgLogLevel));
	//	}

	protected void noMadkitConsoleLog(){
		setMadkitProperty(Madkit.noMadkitConsoleLog, "true");
		assertEquals("true",getMadkitProperty(Madkit.noMadkitConsoleLog));
	}

	protected void agentsLogFileOn(){
		setMadkitProperty(Madkit.agentsLogFile, "bin/agents_log_file");
		assertEquals("bin/agents_log_file",getMadkitProperty(Madkit.agentsLogFile));
	}

	protected void madkitLogFileOn(){
		setMadkitProperty(Madkit.MadkitLogFile, "bin/madkit_kernel");
		assertEquals("bin/madkit_kernel",getMadkitProperty(Madkit.MadkitLogFile));
	}

	protected void assertAgentIsTerminated(AbstractAgent a) {
		assertEquals(TERMINATED,a.getState());
	}


	static long time;

	public static void startTimer(){
		time = System.nanoTime();
	}

	public static long stopTimer(String message){
		long t = System.nanoTime()-time;
		System.err.println(message+(t/1000000000)+" s");
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
	protected void pause(int millis) {
		try {
			Thread.sleep(millis);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

	}


}