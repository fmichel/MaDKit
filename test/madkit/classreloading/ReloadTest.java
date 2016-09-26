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
package madkit.classreloading;

import static org.junit.Assert.assertNotSame;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.channels.FileChannel;

import madkit.action.AgentAction;
import madkit.kernel.AbstractAgent;
import madkit.kernel.JunitMadkit;
import madkit.kernel.MadkitClassLoader;
import madkit.testing.util.agent.NormalAA;
import madkit.testing.util.agent.NormalAgent;

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

//	@Test //TODO one file for each test
//	public void reloadTest() {
//		launchTest(new NormalAgent() {
//
//			/**
//			 * 
//			 */
//			private static final long	serialVersionUID	= 1L;
//
//			protected void activate() {
//				final String agentClassName = AgentToReload.class.getName();
//				Assert.assertEquals("a", launchAgent(agentClassName).toString());
//				replaceFile();
//				try {
//					MadkitClassLoader.reloadClass(agentClassName);
//				} catch (ClassNotFoundException e) {
//					e.printStackTrace();
//				}
//				Assert.assertEquals("reloaded", launchAgent(agentClassName).toString());
//			}
//		});
//	}


	@Test
	public void reloadByGUITest() {//need to clean cp before use
		launchTest(new NormalAgent() {
			protected void activate() {
				final String agentClassName = AgentToReload.class.getName();
				final AbstractAgent launchAgent = launchAgent(agentClassName);
				Assert.assertEquals("a", launchAgent.toString());
				replaceFile();
				AgentAction.RELOAD.getActionFor(launchAgent).actionPerformed(null);
				Assert.assertEquals("reloaded", launchAgent(agentClassName).toString());
			}
		});
	}

	
	@Test
	public void reloadAndLoadTest() {
		launchTest(new AbstractAgent() {
			protected void activate() {
				NormalAA a = new NormalAA();
				try {
					MadkitClassLoader.reloadClass(a.getClass().getName());
					@SuppressWarnings("unchecked")
					Class<AbstractAgent> c = (Class<AbstractAgent>) MadkitClassLoader.getLoader().loadClass(a.getClass().getName());
					assertNotSame(c.getClassLoader(), a.getClass().getClassLoader());
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				}
		}
		});
	}

	/**
		 * 
		 */
	@SuppressWarnings("all")//for the empty control flow statement
	private void replaceFile() {
		String classPath = '/' + AgentToReload.class.getName().replace('.', '/').concat(".class");
		URL destUrl = getClass().getResource(classPath);
		File destFile = new File(destUrl.getPath());
		pause(1200);
		destFile.delete();
		try (FileInputStream inS = new FileInputStream(new File(System.getProperty("user.dir") + File.separator + "test" + classPath)); 
				FileOutputStream outS = new FileOutputStream(destFile);
				FileChannel source = inS.getChannel();
				FileChannel destination = outS.getChannel()){
			destFile.createNewFile();
			System.err.println(System.getProperty("user.dir"));
			long count = 0;
			long size = source.size();
			while ((count += destination.transferFrom(source, count, size - count)) < size);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
