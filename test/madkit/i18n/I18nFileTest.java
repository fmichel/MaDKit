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
package madkit.i18n;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.util.EnumSet;
import java.util.Properties;

import org.junit.Test;

import madkit.action.AgentAction;
import madkit.action.GUIManagerAction;
import madkit.action.KernelAction;
import madkit.action.LoggingAction;
import madkit.action.SchedulingAction;

/**
* @author Fabien Michel
*/
public class I18nFileTest {

	@Test
	public final void testAgentAction() throws IOException {
		testFilePresenceAndContent(AgentAction.class,"fr_FR");
	}

	@Test
	public final void testKernelAction() throws IOException {
		testFilePresenceAndContent(KernelAction.class,"fr_FR");
	}

	@Test
	public final void testGUIManagerAction() throws IOException {
		testFilePresenceAndContent(GUIManagerAction.class,"fr_FR");
	}

	@Test
	public final void testLogLevelAction() throws IOException {
		testFilePresenceAndContent(LoggingAction.class,"fr_FR");
	}

	@Test
	public final void testSchedulingAction() throws IOException {
		testFilePresenceAndContent(SchedulingAction.class,"fr_FR");
	}

	public <E extends Enum<E>> void testFilePresenceAndContent(Class<E> e,String... languages) throws IOException{
		EnumSet<E> set = EnumSet.allOf(e);
		testKeys(e, set,"");
		for (String lang : languages) {
			testKeys(e, set, "_"+lang);
		}
	}

	/**
	 * @param e
	 * @param set
	 * @throws IOException
	 */
	private <E extends Enum<E>> void testKeys(Class<E> e, EnumSet<E> set,String lang) throws IOException {
		System.err.println("\n----------------testing "+e+lang);
		Properties defaultConfig = new Properties();
		defaultConfig.load(getClass().getResourceAsStream("/madkit/i18n/"+e.getSimpleName()+lang+".properties"));
		assertNotNull(defaultConfig);
		assertEquals(set.size(), defaultConfig.size());
		for (E enum1 : set) {
			System.err.println(enum1.name());
			assertNotNull(defaultConfig.getProperty(enum1.name()));
		}
	}
}
