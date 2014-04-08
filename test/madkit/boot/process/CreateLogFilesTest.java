/*
 * Copyright 1997-2012 Fabien Michel, Olivier Gutknecht, Jacques Ferber
 * 
 * This file is part of MaDKit.
 * 
 * MaDKit is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 * 
 * MaDKit is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with MaDKit. If not, see <http://www.gnu.org/licenses/>.
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