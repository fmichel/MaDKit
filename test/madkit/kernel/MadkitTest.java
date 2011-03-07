/**
 * 
 */
package madkit.kernel;



import org.junit.Test;

/**
 * @author fab
 *
 */
public class MadkitTest {

	/**
	 * 
	 */
	private void testMKlogLevelBoot(String MKLogLevel) {
		System.err.println("\n\n\n\n\n--------------------MK log level = "+MKLogLevel+"-------------------");
		String[] args = {"--"+Madkit.MadkitLogLevel,MKLogLevel};
		Madkit m = new Madkit(args);
		System.err.println("\n\n--------------------MK log level = "+MKLogLevel+"-------------------\n\n\n\n\n");
	}

	@Test
	public void mkLogLevelALL(){
		testMKlogLevelBoot("ALL");
	}

	@Test
	public void mkLogLevelFINEST(){
		testMKlogLevelBoot("FINEST");
	}

	@Test
	public void mkLogLevelOFF(){
		testMKlogLevelBoot("OFF");
	}
	
	@Test
	public void testOptionAutoLogDir(){
		
	}

}
