package madkit.javadoc;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;

import org.junit.Test;


public class JavadocTest {

	@Test
	public void test() throws IOException, InterruptedException {
		String[] cmd = {"/bin/sh","-c","ant javadoc | grep \"warning\" > /tmp/testJavadoc"};
		Process p = Runtime.getRuntime().exec(cmd);
//		assertEquals(0,p.waitFor());
		p.waitFor();
		
		final File file = new File("/tmp/testJavadoc");
		assertTrue(file.exists());
		assertTrue(file.length() == 0);// no warnings
		file.delete();
	}

}
