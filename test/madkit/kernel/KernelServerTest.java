package madkit.kernel;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

import org.junit.Test;


public class KernelServerTest {

   private static final String IPADDRESS_PATTERN = 
		"^([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
		"([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
		"([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
		"([01]?\\d\\d?|2[0-4]\\d|25[0-5])$";

	@Test
	public final void testToString() {
		String s = null;
		try {
			BufferedReader in = new BufferedReader(new InputStreamReader(new URL(Madkit.WEB+"/whatismyip.php").openStream()));
			 s = in.readLine();
			 in.close();
			 System.err.println(s);
			 assertTrue(s.matches(IPADDRESS_PATTERN));
		} catch (IOException e) {
			e.printStackTrace();
			fail();
		}
	}

}
