package madkit.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import madkit.kernel.JunitMadkit;

import org.junit.Test;

public class MadkitPropertiesTest {

	@Test
	public void testLoadPropertiesFromMaDKitXML() {
		try {
			final MadkitProperties madkitProperties = new MadkitProperties();
			madkitProperties.loadPropertiesFromMaDKitXML("madkit/xml/success.xml");
			System.err.println(madkitProperties);
			assertEquals(madkitProperties.getProperty("test"), "yes");
			assertEquals(madkitProperties.getProperty("test2"), "good");
			assertEquals(madkitProperties.getProperty("desktop"), "true");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Test
	public void testLoadPropertiesFromPropertiesFile() {
		try {
			final MadkitProperties madkitProperties = new MadkitProperties();
			madkitProperties.loadPropertiesFromPropertiesFile("madkit/boot/process/test2.prop");
			System.err.println(madkitProperties);
			assertEquals(madkitProperties.getProperty("test"), "false");
			assertEquals(madkitProperties.getProperty("test2"), "ok");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Test
	public void testLoadPropertiesFromFile() {
		try {
			new MadkitProperties().loadPropertiesFromFile("notExist");
			JunitMadkit.noExceptionFailure();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * The parsing is OK but the values are not
	 */
	@Test
	public void testLoadPropertiesFromPropertiesFileFailFromWrongFileContent() {
		try {
			final MadkitProperties madkitProperties = new MadkitProperties();
			madkitProperties.loadPropertiesFromPropertiesFile("madkit/xml/bench.xml");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Test
	public void testGetInputStreamFromCP() {
		try (InputStream is = MadkitProperties.getInputStream("madkit/xml/bench.xml")){
			assertNotNull(is);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Test
	public void testGetInputStreamFail() {
		try (InputStream is = MadkitProperties.getInputStream("madkit/xml/notExist.xml")){
			assertNull(is);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Test
	public void testGetInputStreamFromUserDir() {
		try (InputStream is = MadkitProperties.getInputStream("test/madkit/xml/bench.xml")){
			assertNotNull(is);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Test
	public void testGetInputStreamFromAbsolutePath() {
		File f = new File("test/madkit/xml/bench.xml");
		try (InputStream is = MadkitProperties.getInputStream(f.getAbsolutePath())){
			assertNotNull(is);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
