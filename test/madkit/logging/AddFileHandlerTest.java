package madkit.logging;

import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FilenameFilter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Level;

import org.junit.Test;

import madkit.kernel.AbstractAgent;
import madkit.kernel.AbstractAgent.ReturnCode;
import madkit.kernel.JunitMadkit;


public class AddFileHandlerTest extends JunitMadkit {

	static final String FILE_NAME = "AddFileHandlerTest.test";
	static Path pathToFile = Paths.get("logs",AddFileHandlerTest.FILE_NAME);

	public void testFilesPresence(int number, String name) {
		assertSame(number, Paths.get("logs").toFile().list(new FilenameFilter() {
			@Override
			public boolean accept(File arg0, String s) {
				return ! s.contains(".lck") && s.contains(name);
			}
		}).length);
	}

	@Test
	public void addFileHandlerTest() {
		launchTest(new AbstractAgent() {
			@Override
			protected void activate() {
				getLogger().addFileHandler(pathToFile.getParent(), null, false, true);
				getLogger().info("test");
				testFilesPresence(1, getName());
			}
		}, ReturnCode.SUCCESS);
	}

	@Test
	public void sameFileHandlerForTwoAgentsProduceSeveralFilesTest() {
		launchTest(new AbstractAgent() {
			@Override
			protected void activate() {
				launchAgent(new LogFileAgent());
				launchAgent(new LogFileAgent());
				testFilesPresence(2,FILE_NAME);
			}
		}, ReturnCode.SUCCESS);
	}

	@Test
	public void addMultipleFileHandlerTest() {
		launchTest(new AbstractAgent() {
			@Override
			protected void activate() {
				getLogger().addFileHandler(Paths.get("logs"), "test", true, true);
				getLogger().addFileHandler(Paths.get("logs"), "test_bis", true, true);
				getLogger().info("testing");
				final Path file1 = Paths.get("logs", "test");
				assertTrue(file1.toFile().exists());				
				final Path file2 = Paths.get("logs", "test_bis");
				assertTrue(file2.toFile().exists());				
//				try {
//					assertTrue(Arrays.equals(Files.readAllBytes(file1), Files.readAllBytes(file2)));
//				} catch (IOException e) {
//					e.printStackTrace();
//				}
			}
		}, ReturnCode.SUCCESS);
	}


}

class LogFileAgent extends AbstractAgent{

	public LogFileAgent() {
		setLogLevel(Level.ALL);
		getLogger().addFileHandler(Paths.get("logs"), AddFileHandlerTest.FILE_NAME, true, true);
	}
}