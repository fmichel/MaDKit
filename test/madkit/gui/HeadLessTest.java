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
package madkit.gui;

import org.junit.Test;

import madkit.kernel.AbstractAgent;
import madkit.kernel.AbstractAgent.ReturnCode;
import madkit.kernel.JunitMadkit;
import madkit.testing.util.agent.NormalAgent;



public class HeadLessTest extends JunitMadkit {

	@Test
	public void testAA(){
		System.setProperty("java.awt.headless", "true");
		launchTest(new AbstractAgent(){
			@Override
			protected void activate() {
				super.activate();
			}
			@Override
			public void setupFrame(AgentFrame frame) {
				super.setupFrame(frame);
			}
		},ReturnCode.SUCCESS,true);
		pause(100);
		testAgent();
	}

	/**
	 * 
	 */
	@Test
	public void testAgent() {
		System.setProperty("java.awt.headless", "true");
		launchTest(new NormalAgent(){
			@Override
			protected void activate() {
				super.activate();
			}
			@Override
			public void setupFrame(AgentFrame frame) {
				super.setupFrame(frame);
			}
		},ReturnCode.SUCCESS,true);
		pause(100);
	}
}
