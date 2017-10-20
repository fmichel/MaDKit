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
package madkit.kernel;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.net.MalformedURLException;
import java.util.Arrays;

import org.junit.Test;

import madkit.kernel.AbstractAgent.ReturnCode;
import madkit.kernel.Madkit.LevelOption;

/**
 * @author Fabien Michel
 * @since MaDKit 5.0.0.10
 * @version 1
 */

public class MadkitClassLoaderTest extends JunitMadkit {

    @Test
    public void scanFolderForMDKConfigFileAgentClassesTest() {
	addMadkitArgs(LevelOption.kernelLogLevel.toString(), "OFF");
	launchTest(new AbstractAgent() {

	    @Override
	    protected void activate() {
		assertFalse(MadkitClassLoader.getAllAgentClasses().isEmpty());
		System.err.println(MadkitClassLoader.getMDKFiles());
		// ugly : inside Eclipse / outside Eclipse / in travis
		final int numberOfMDKConfigFilesFound = MadkitClassLoader.getMDKFiles().size();
		assertTrue(1 == numberOfMDKConfigFilesFound || 5 == numberOfMDKConfigFilesFound || 4 == numberOfMDKConfigFilesFound);
		System.err.println(MadkitClassLoader.getXMLConfigurations());
		final int numberOfXmlConfigFilesFound = MadkitClassLoader.getXMLConfigurations().size();
		assertTrue(3 == numberOfXmlConfigFilesFound || 16 == numberOfXmlConfigFilesFound|| 13 == numberOfXmlConfigFilesFound);
	    }
	});
    }

    @Test
    public void testLoadJarsFromPath() {
	launchTest(new AbstractAgent() {

	    @Override
	    protected void activate() {
		try {
		    MadkitClassLoader.getLoader().loadClass("madkit.pingpong.PingPong");
		    fail("Not thrown");
		}
		catch(ClassNotFoundException e) {
		    e.printStackTrace();
		}
		System.err.println(System.getProperty("user.dir"));
		MadkitClassLoader.loadJarsFromDirectory(System.getProperty("user.dir") + File.separator + "test");
		try {
		    assertNotNull(MadkitClassLoader.getLoader().loadClass("madkit.pingpong.PingPong"));
		}
		catch(ClassNotFoundException e) {
		    fail(e.getMessage());
		}
	    }
	});
    }

    @Test
    public void testURLs() {
	launchTest(new AbstractAgent() {

	    @Override
	    protected void activate() {
		@SuppressWarnings("resource")
		MadkitClassLoader mcl = MadkitClassLoader.getLoader();
		try {
		    MadkitClassLoader.loadUrl(new File(".").toURI().toURL());
		    int n = mcl.getURLs().length;
		    MadkitClassLoader.loadUrl(new File(".").toURI().toURL());
		    assertEquals(n, mcl.getURLs().length);
		    System.err.println(Arrays.deepToString(mcl.getURLs()));
		}
		catch(MalformedURLException e) {
		    e.printStackTrace();
		}
	    }
	});
    }

    @Test
    public void testLoadClassString() {
	launchTest(new AbstractAgent() {

	    @Override
	    protected void activate() {
		try {
		    assertNotNull(MadkitClassLoader.getLoader().loadClass("madkit.kernel.AbstractAgent"));
		}
		catch(ClassNotFoundException e) {
		    fail(e.getMessage());
		}
		try {
		    MadkitClassLoader.getLoader().loadClass(null);
		    fail("Not thrown");
		}
		catch(ClassNotFoundException e) {
		    fail("Not the one");
		    e.printStackTrace();
		}
		catch(NullPointerException e) {
		    throw e;
		}
	    }
	}, ReturnCode.AGENT_CRASH);
    }

}
