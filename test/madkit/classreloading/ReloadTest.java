/*
 * Copyright 2012 Fabien Michel
 * 
 * This file is part of MaDKit.
 * 
 * MaDKit is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * MaDKit is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with MaDKit. If not, see <http://www.gnu.org/licenses/>.
 */
package madkit.classreloading;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.channels.FileChannel;

import madkit.kernel.Agent;
import madkit.kernel.JunitMadkit;

import org.junit.Assert;
import org.junit.Test;

/**
 * bin directory should be cleaned before use
 * the .class file is part of the test
 * 
 * @author Fabien Michel
 * @since MadKit 5.0.0.20
 * @version 0.9
 * 
 */
public class ReloadTest extends JunitMadkit {

	@Test
	public void replyWithSameMessage() {
		launchTest(new Agent() {

			protected void activate() {
				final String agentClassName = AgentToReload.class.getName();
				Assert.assertEquals("a", launchAgent(agentClassName).toString());
				replaceFile();
				try {
					getMadkitClassLoader().reloadClass(agentClassName);
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				}
				Assert.assertEquals("reloaded", launchAgent(agentClassName)
						.toString());
			}
		});
	}

	/**
		 * 
		 */
	private void replaceFile() {
		FileChannel source = null;
		FileChannel destination = null;
		try {
			String classPath = '/' + AgentToReload.class.getName()
					.replace('.', '/').concat(".class");
			URL destUrl = getClass().getResource(classPath);
			File destFile = new File(destUrl.getPath());
			pause(1200);
			destFile.delete();
			destFile.createNewFile();

			source = new FileInputStream(new File(System.getProperty("user.dir")
					+ File.separator + "test" + classPath)).getChannel();

			System.err.println(System.getProperty("user.dir"));
			destination = new FileOutputStream(destFile).getChannel();
			long count = 0;
			long size = source.size();
			while ((count += destination.transferFrom(source, count, size - count)) < size)
				;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				if (source != null)
					source.close();
				if (destination != null) {
					destination.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
