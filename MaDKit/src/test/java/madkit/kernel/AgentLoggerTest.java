/*******************************************************************************
 * MaDKit - Multi-agent systems Development Kit 
 * 
 * Copyright (c) 1998-2025 Fabien Michel, Olivier Gutknecht, Jacques Ferber...
 * 
 * This software is a computer program whose purpose is to
 * provide a lightweight Java API for developing and simulating 
 * Multi-Agent Systems (MAS) using an organizational perspective.
 *
 * This software is governed by the CeCILL-C license under French law and
 * abiding by the rules of distribution of free software.You can use,
 * modify and/ or redistribute the software under the terms of the CeCILL-C
 * license as circulated by CEA, CNRS and INRIA at the following URL
 * "http://www.cecill.info".
 *
 * As a counterpart to the access to the source code and rights to copy,
 * modify and redistribute granted by the license, users are provided only
 * with a limited warranty and the software's author, the holder of the
 * economic rights, and the successive licensors have only limited
 * liability.
 *
 * In this respect, the user's attention is drawn to the risks associated
 * with loading, using, modifying and/or developing or reproducing the
 * software by the user in light of its specific status of free software,
 * that may mean that it is complicated to manipulate, and that also
 * therefore means that it is reserved for developers and experienced
 * professionals having in-depth computer knowledge. Users are therefore
 * encouraged to load and test the software's suitability as regards their
 * requirements in conditions enabling the security of their systems and/or
 * data to be ensured and, more generally, to use and operate it in the
 * same conditions as regards security.
 *
 * The fact that you are presently reading this means that you have had
 * knowledge of the CeCILL-C license and that you accept its terms.
 *******************************************************************************/
package madkit.kernel;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static madkit.kernel.Agent.ReturnCode.SUCCESS;

import madkit.testing.agents.ThreadedTestAgent;

/**
 *
 * @since MaDKit 5.0.0.6
 * @version 0.9
 * 
 */
public class AgentLoggerTest extends MadkitConcurrentTestCase {

	@BeforeMethod
	public void cleanUpLogDirectory() {
		File f = AgentLogger.DEFAULT_LOG_DIRECTORY.toFile();
		System.out.println("cleaning up log directory: " + f.getAbsolutePath());
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
	public void givenNewAgent_whenCreateLogFile_thenLogFileNotNull() {
		runTest(new DefaultTestAgent() {
			@Override
			public void behaviorInActivate() {
				Agent a = new ThreadedTestAgent() {
					@Override
					protected void onActivation() {
						getLogger().createLogFile();
					}
				};
				assertThat(launchAgent(a)).as("launchAgent return code").isEqualTo(SUCCESS);
				File[] files = AgentLogger.DEFAULT_LOG_DIRECTORY.toFile().listFiles(f -> !f.getName().endsWith(".lck"));
				assertThat(files.length).as("log files count after createLogFile").isEqualTo(1);
				resume();
			}
		});
	}

	@Test
	public void givenNewAgentLogFileWithSameName_whenCreateLogFileNoAppend_thenAutoFileLogName() {
		runTest(new DefaultTestAgent() {
			@Override
			public void behaviorInActivate() {
				Agent a1 = new ThreadedTestAgent() {
					@Override
					protected void onActivation() {
						getLogger().createLogFile("test");
					}
				};
				assertThat(launchAgent(a1)).as("launchAgent a1").isEqualTo(SUCCESS);

				Agent a2 = new ThreadedTestAgent() {
					@Override
					protected void onActivation() {
						getLogger().createLogFile("test", AgentLogger.DEFAULT_LOG_DIRECTORY, false);
					}
				};
				assertThat(launchAgent(a2)).as("launchAgent a2").isEqualTo(SUCCESS);

				File[] files = AgentLogger.DEFAULT_LOG_DIRECTORY.toFile().listFiles(f -> !f.getName().endsWith(".lck"));
				assertThat(files.length).as("log files count when no append").isEqualTo(2);
				resume();
			}
		});
	}

	@Test
	public void givenNewAgentLogFileWithSameName_whenCreateLogFileWithAppend_thenAutoFileLogName() {
		runTest(new DefaultTestAgent() {
			@Override
			public void behaviorInActivate() {
				Agent a1 = new ThreadedTestAgent() {
					@Override
					protected void onActivation() {
						getLogger().createLogFile("test", AgentLogger.DEFAULT_LOG_DIRECTORY);
					}
				};
				assertThat(launchAgent(a1)).as("launchAgent a1").isEqualTo(SUCCESS);

				File[] files = AgentLogger.DEFAULT_LOG_DIRECTORY.toFile().listFiles(f -> f.getName().endsWith(".lck"));
				while (files.length > 0) {
					pause(100);
					files = AgentLogger.DEFAULT_LOG_DIRECTORY.toFile().listFiles(f -> f.getName().endsWith(".lck"));
				}

				Agent a2 = new ThreadedTestAgent() {
					@Override
					protected void onActivation() {
						getLogger().createLogFile("test", AgentLogger.DEFAULT_LOG_DIRECTORY, true);
					}
				};
				assertThat(launchAgent(a2)).as("launchAgent a2").isEqualTo(SUCCESS);

				files = AgentLogger.DEFAULT_LOG_DIRECTORY.toFile().listFiles(f -> !f.getName().endsWith(".lck"));
				assertThat(files.length).as("log files count when append").isEqualTo(1);
				resume();
			}
		});
	}

}
