/*
This software is a computer program whose purpose is to 
provide a lightweight Java library for designing and simulating Multi-Agent Systems (MAS).

This software is governed by the CeCILL-C license under French law and
abiding by the rules of distribution of free software.  You can  use, 
modify and/ or redistribute the software under the terms of the CeCILL-C
license as circulated by CEA, CNRS and INRIA at the following URL
"http://www.cecill.info". 

As a counterpart to the access to the source code and  rights to copy,
modify and redistribute granted by the license, users are provided only
with a limited warranty  and the software's author,  the holder of the
economic rights,  and the successive licensors  have only  limited
liability. 

In this respect, the user's attention is drawn to the risks associated
with loading,  using,  modifying and/or developing or reproducing the
software by the user in light of its specific status of free software,
that may mean  that it is complicated to manipulate,  and  that  also
therefore means  that it is reserved for developers  and  experienced
professionals having in-depth computer knowledge. Users are therefore
encouraged to load and test the software's suitability as regards their
requirements in conditions enabling the security of their systems and/or 
data to be ensured and,  more generally, to use and operate it in the 
same conditions as regards security. 

The fact that you are presently reading this means that you have had
knowledge of the CeCILL-C license and that you accept its terms.
 */
package madkit.kernel;

import static madkit.kernel.Agent.ReturnCode.SUCCESS;
import static org.testng.Assert.assertEquals;

import java.io.File;

import org.testng.annotations.Test;

/**
 *
 * @since MaDKit 5.0.0.6
 * @version 0.9
 * 
 */
public class AgentLoggerTest extends JunitMadkit {

	public void cleanUpLogDirectory() {
		File f = AgentLogger.DEFAULT_LOG_DIRECTORY.toFile();
		String[] entries = f.list();
		if (entries != null) {
			for (String s : entries) {
				File currentFile = new File(f.getPath(), s);
				currentFile.delete();
			}
		}
		f.delete();
	}

	@Test
	public void givenNewAgent_whenStarted_thenLoggerIsNull() {
		testBehavior(a -> {
			threadAssertNull(a.logger);
		});
	}

	@Test
	public void givenNewAgent_whenLoggerIsUsed_thenLoggerNotNull() {
		testBehavior(a -> {
			a.getLogger().info("testing");
			threadAssertNotNull(a.logger);
		});
	}

	@Test
	public void givenNewAgent_whenCreateLogFile_thenLogFileNotNull() {
		cleanUpLogDirectory();
		Agent a;
		launchTestedAgent(a = new Agent() {
			@Override
			protected void onLive() {
				getLogger().createLogFile();
//				threadFail();//FIXME this should crash the test
			}
		}, SUCCESS);
		awaitTermination(a, 10000);
		File[] files = AgentLogger.DEFAULT_LOG_DIRECTORY.toFile().listFiles();
		assertEquals(files.length, 1);
	}

	@Test
	public void givenNewAgentLogFileWithSameName_whenCreateLogFileNoAppend_thenAutoFileLogName() {
		cleanUpLogDirectory();
		Agent a;
		launchTestedAgent(a = new Agent() {
			@Override
			protected void onLive() {
				getLogger().createLogFile("test");
			}
		}, SUCCESS);
		awaitTermination(a, 10000);
		launchTestedAgent(a = new Agent() {
			@Override
			protected void onLive() {
				getLogger().createLogFile("test", AgentLogger.DEFAULT_LOG_DIRECTORY, false);
			}
		}, SUCCESS);
		awaitTermination(a, 10000);
		File[] files = AgentLogger.DEFAULT_LOG_DIRECTORY.toFile().listFiles();
		assertEquals(files.length, 2);
	}

	@Test
	public void givenNewAgentLogFileWithSameName_whenCreateLogFileWithAppend_thenAutoFileLogName() {
		cleanUpLogDirectory();
		Agent a;
		launchTestedAgent(a = new Agent() {
			@Override
			protected void onLive() {
				getLogger().createLogFile("test", AgentLogger.DEFAULT_LOG_DIRECTORY);
			}
		}, SUCCESS);
		awaitTermination(a, 10000);
		launchTestedAgent(a = new Agent() {
			@Override
			protected void onLive() {
				getLogger().createLogFile("test", AgentLogger.DEFAULT_LOG_DIRECTORY, true);
			}
		}, SUCCESS);
		awaitTermination(a, 10000);
		File[] files = AgentLogger.DEFAULT_LOG_DIRECTORY.toFile().listFiles();
		assertEquals(files.length, 1);
	}

}