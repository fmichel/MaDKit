/**
 * 
 */
package madkit.boot.process;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;

import org.junit.AfterClass;
import org.junit.Test;

/**
 * @author fab
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
		String option = "--autoAgentLogDirectory ";
		System.err.println("testing "+option+"\n");
		Process p = Runtime.getRuntime().exec("java -cp bin madkit.kernel.Madkit "+option);
		p.waitFor();
		assertFalse(new File(agentLogFileName).exists());
	}

	@Test
	public void missingSlash() throws IOException, InterruptedException{
		//		String[] argss = {"--autoAgentLogDirectory","bin","--launchAgents","madkit.kernel.AbstractAgent","--agentLogLevel","FINEST"};
		//		Madkit.main(argss);
		String option = "--autoAgentLogDirectory bin"+endOfCommandLine;
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