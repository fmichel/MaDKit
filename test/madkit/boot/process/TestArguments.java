/**
 * 
 */
package madkit.boot.process;

import java.util.logging.Level;
import org.junit.Test;

import madkit.kernel.Madkit;

/**
 * @author fab
 *
 */
public class TestArguments{

	/**
	 * @throws InterruptedException 
	 * 
	 */
	private void testMKlogLevelBoot(String MKLogLevel) throws InterruptedException {
		System.err.println("\n\n\n\n\n--------------------MK log level = "+MKLogLevel+"-------------------");
		String[] args = {"--"+Madkit.MadkitLogLevel,MKLogLevel};
		Madkit.main(args);
//		Thread.sleep(1000);
		System.err.println("\n\n--------------------MK log level = "+MKLogLevel+"-------------------\n\n\n\n\n");
	}
	
	@Test
	public void mkLogLevels() throws InterruptedException{
//		testMKlogLevelBoot(Level.OFF.toString());
//		testMKlogLevelBoot(Level.SEVERE.toString());
//		testMKlogLevelBoot(Level.WARNING.toString());
//		testMKlogLevelBoot(Level.INFO.toString());
//		testMKlogLevelBoot(Level.CONFIG.toString());
//		testMKlogLevelBoot(Level.FINE.toString());
//		testMKlogLevelBoot(Level.FINER.toString());
//		testMKlogLevelBoot(Level.FINEST.toString());
		testMKlogLevelBoot(Level.ALL.toString());
	}

}