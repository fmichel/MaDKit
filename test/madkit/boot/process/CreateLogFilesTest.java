/*
 * Copyright or © or Copr. Fabien Michel, Olivier Gutknecht, Jacques Ferber (1997)

fmichel@lirmm.fr
olg@no-distance.net
ferber@lirmm.fr

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
package madkit.boot.process;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;

import madkit.kernel.AbstractAgent;
import madkit.kernel.JunitMadkit;
import madkit.kernel.Madkit;
import madkit.kernel.Madkit.BooleanOption;
import madkit.kernel.Madkit.LevelOption;
import madkit.kernel.Madkit.Option;

import org.junit.AfterClass;
import org.junit.Test;

/**
 * @author Fabien Michel
 * @since MaDKit 5.0.0.10
 * @version 0.9
 * 
 */

public class CreateLogFilesTest extends JunitMadkit {

	File				f;
	public static FilenameFilter	filter	= new FilenameFilter() {

										@Override
										public boolean accept(File dir, String s) {
											return !s.contains(".lck");
										}
									};

	static void delete(File f) throws IOException {
		if (f.isDirectory()) {
			for (File c : f.listFiles())
				delete(c);
		}
		if (!f.delete())
			throw new FileNotFoundException("Failed to delete file: " + f);
	}

	/**
	 * Check that there is one dir per MaDKit instance
	 * @throws IOException
	 */
	@SuppressWarnings("unused")
	@Test
	public void logDirectoryUniqueness()  {
		new JunitMadkit();
		String dir = System.getProperty("java.io.tmpdir") + File.separatorChar
				+ name.getMethodName();
		try {
			delete(new File(dir));
		} catch (IOException e) {
		}
		String[] args = {
				LevelOption.madkitLogLevel.toString(),
				"OFF",
				BooleanOption.desktop.toString(),
				"false",
				Option.launchAgents.toString(),
				"madkit.kernel.AbstractAgent",// to not have the desktop mode by
														// default
														// Option.logDirectory.toString(), getBinTestDir(), LevelOption.agentLogLevel.toString(), "ALL",
				BooleanOption.createLogFiles.toString(),
				LevelOption.kernelLogLevel.toString(), "OFF",
				Option.logDirectory.toString(), dir };
		for (int i = 0; i < 50; i++) {
			new Madkit(args);
		}
		pause(200);
		assertEquals(50,new File(dir).listFiles().length);
	}

	@Test
	public void defaultLogDirectory() {
		addMadkitArgs(BooleanOption.createLogFiles.toString(),
				LevelOption.kernelLogLevel.toString(), "INFO");
		launchTest(new AbstractAgent() {

			@Override
			protected void activate() {
				System.err.println(getMadkitProperty(Option.logDirectory.name()));
				f = new File(getMadkitProperty(Option.logDirectory.name()));
			}
		});
		pause(100);
		System.err.println(f);
		assertTrue(f.exists());
		assertTrue(f.isDirectory());
		assertSame(3, f.listFiles(filter).length);
	}

	@Test
	public void oneAgentLog() {
		launchTest(new AbstractAgent() {

			@Override
			protected void activate() {
				f = new File(getMadkitProperty(Option.logDirectory.name()),
						getLogger().getName());
				assertFalse(f.exists());
				getLogger().createLogFile();
				System.err.println(f);
				assertTrue(f.exists());
				f.delete();
			}
		});
	}

	/**
	 * Creates a log file in {@link Option#logDirectory}
	 */
	@Test
	public void oneAgentLogInConstructor() {
		launchTest(new LogTester() {

			@Override
			protected void activate() {
				f = new File("logs", "[-" + hashCode() + "]");
				System.err.println(f);
				assertTrue(f.exists());
				f.delete();
			}
		});
		pause(100);
	}

	@Test
	public void noLogDirectory() {
		addMadkitArgs(BooleanOption.createLogFiles.toString(), "false",
				LevelOption.kernelLogLevel.toString(), "INFO");
		launchTest(new AbstractAgent() {

			@Override
			protected void activate() {
				System.err.println(getMadkitProperty(Option.logDirectory.name()));
				assertFalse(new File(Option.logDirectory.name()).exists());
			}
		});
		pause(100);
	}

	@Test
	public void absoluteLogDirectory() {
		addMadkitArgs(BooleanOption.createLogFiles.toString(),
				Option.logDirectory.toString(),
				System.getProperty("java.io.tmpdir"),
				LevelOption.kernelLogLevel.toString(), "ALL",
				LevelOption.madkitLogLevel.toString(), "OFF");
		launchTest(new AbstractAgent() {

			@Override
			protected void activate() {
				System.err.println(getMadkitProperty(Option.logDirectory.name()));
				f = new File(getMadkitProperty(Option.logDirectory.name()));
			}
		});
		assertTrue(f.exists());
		assertTrue(f.isDirectory());
		pause(500);
		assertSame(3, f.listFiles(filter).length);
	}

	@Test
	public void noFilesOnLogOFF() {
		addMadkitArgs(BooleanOption.createLogFiles.toString(),
				LevelOption.kernelLogLevel.toString(), "OFF",
				LevelOption.agentLogLevel.toString(), "OFF",
				LevelOption.madkitLogLevel.toString(), "ALL");
		launchTest(new AbstractAgent() {

			@Override
			protected void activate() {
				System.err.println(getMadkitProperty(Option.logDirectory.name()));
				f = new File(getMadkitProperty(Option.logDirectory.name()));
			}
		});
		assertFalse(f.exists());
	}

	@Test
	public void noKernelFile() {
		addMadkitArgs(BooleanOption.createLogFiles.toString(),
				LevelOption.kernelLogLevel.toString(), "OFF");
		launchTest(new AbstractAgent() {

			@Override
			protected void activate() {
				System.err.println(getMadkitProperty(Option.logDirectory.name()));
				f = new File(getMadkitProperty(Option.logDirectory.name()));
				assertTrue(f.exists());
				assertTrue(f.isDirectory());
			}
		});
		pause(500);
		assertSame(2, f.listFiles(filter).length);
	}

	@AfterClass
	public static void clean() {
		System.err.println(new File("logs").getAbsolutePath());
		new File("logs").delete();
	}

}

class LogTester extends AbstractAgent {

	public LogTester() {
		getLogger().createLogFile();
	}

}