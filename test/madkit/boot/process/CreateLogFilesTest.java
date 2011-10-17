/*
 * Copyright 1997-2011 Fabien Michel, Olivier Gutknecht, Jacques Ferber
 * 
 * This file is part of MadKit.
 * 
 * MadKit is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 * 
 * MadKit is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with MadKit. If not, see <http://www.gnu.org/licenses/>.
 */
package madkit.boot.process;

import java.io.File;
import java.io.FilenameFilter;

import javax.swing.filechooser.FileFilter;

import madkit.kernel.AbstractAgent;
import madkit.kernel.JunitMadKit;
import madkit.kernel.Madkit.BooleanOption;
import madkit.kernel.Madkit.LevelOption;
import madkit.kernel.Madkit.Option;
import static org.junit.Assert.*;

import org.junit.Test;

/**
 * @author Fabien Michel
 * @since MadKit 5.0.0.10
 * @version 0.9
 * 
 */
@SuppressWarnings("serial")
public class CreateLogFilesTest extends  JunitMadKit{

	File f;
	FilenameFilter filter = new FilenameFilter() {
		@Override
		public boolean accept(File dir, String name) {
			return ! name.contains(".lck");
		}
	};
	
	@Test
	public void defaultLogDirectory() {
		addMadkitArgs(
				BooleanOption.createLogFiles.toString(),
				LevelOption.kernelLogLevel.toString(),"INFO"
		);
		launchTest(new AbstractAgent(){
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
		assertSame(3,f.listFiles(filter).length);
	}

	@Test
	public void noLogDirectory() {
		addMadkitArgs(
				BooleanOption.createLogFiles.toString(),"false"
				,LevelOption.kernelLogLevel.toString(),"INFO"
		);
		launchTest(new AbstractAgent(){
			@Override
			protected void activate() {
				System.err.println(getMadkitProperty(Option.logDirectory.name()));
				assertFalse(getMadkitProperty(Option.logDirectory.name()).contains("."));
			}
		});
		pause(100);
	}

	@Test
	public void absoluteLogDirectory() {
		addMadkitArgs(
				BooleanOption.createLogFiles.toString(),
				Option.logDirectory.toString(),System.getProperty("java.io.tmpdir")
				,LevelOption.kernelLogLevel.toString(),"ALL"
				,LevelOption.madkitLogLevel.toString(),"OFF"
		);
		launchTest(new AbstractAgent(){
			@Override
			protected void activate() {
				System.err.println(getMadkitProperty(Option.logDirectory.name()));
				f = new File(getMadkitProperty(Option.logDirectory.name()));
			}
		});
		assertTrue(f.exists());
		assertTrue(f.isDirectory());
		pause(500);
		assertSame(3,f.listFiles(filter).length);
	}
	
	@Test
	public void noFilesOnLogOFF() {
		addMadkitArgs(
				BooleanOption.createLogFiles.toString()
				,LevelOption.kernelLogLevel.toString(),"OFF"
				,LevelOption.agentLogLevel.toString(),"OFF"
				,LevelOption.madkitLogLevel.toString(),"ALL"
		);
		launchTest(new AbstractAgent(){
			@Override
			protected void activate() {
				System.err.println(getMadkitProperty(Option.logDirectory.name()));
				f = new File(getMadkitProperty(Option.logDirectory.name()));
			}
		});
		assertTrue(f.exists());
		assertTrue(f.isDirectory());
		pause(500);
		assertSame(0,f.listFiles(filter).length);
	}
	
	@Test
	public void noKernelFile() {
		addMadkitArgs(
				BooleanOption.createLogFiles.toString(),
				LevelOption.kernelLogLevel.toString(),"OFF"
		);
		launchTest(new AbstractAgent(){
			@Override
			protected void activate() {
				System.err.println(getMadkitProperty(Option.logDirectory.name()));
				f = new File(getMadkitProperty(Option.logDirectory.name()));
				assertTrue(f.exists());
				assertTrue(f.isDirectory());
			}
		});
		pause(500);
		assertSame(2,f.listFiles(filter).length);
	}


}