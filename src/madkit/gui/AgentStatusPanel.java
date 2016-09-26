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

import java.awt.FlowLayout;
import java.awt.Image;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;

import madkit.action.KernelAction;
import madkit.kernel.AbstractAgent;

/**
 * A panel that displays a flag if the MaDKit kernel is online.
 * 
 * 
 * @author Fabien Michel
 * @since MaDKit 5.0.0.9
 * @version 0.9
 * 
 */
public class AgentStatusPanel extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5745998699827681837L;
	
	final private static Map<AbstractAgent,AgentStatusPanel> panels = new ConcurrentHashMap<>(); 
	
	
	final static private ImageIcon image = new ImageIcon(KernelAction.LAUNCH_NETWORK.getActionInfo().getBigIcon().getImage().getScaledInstance(10, 10, Image.SCALE_SMOOTH));
	final private AbstractAgent myAgent;
	final private JLabel network;
	
	public AgentStatusPanel(AbstractAgent a){
		super(new FlowLayout(FlowLayout.RIGHT));
		myAgent = a;
		network = new JLabel();
		add(network);
		update();
		panels.put(myAgent, this);
	}
	
	public static void updateAll(){
		for (AgentStatusPanel panel : panels.values()) {
			panel.update();
		}
	}

	private void update() {
		if(myAgent.isAlive() && myAgent.isKernelOnline()){
			network.setIcon(image);
			network.setText(myAgent.getKernelAddress().toString());
			network.setToolTipText(network.getText()+" online @ "+myAgent.getServerInfo());
		}
		else{
			network.setIcon(null);
			network.setText(null);
		}
	}

	public static void remove(AbstractAgent abstractAgent) {
		panels.remove(abstractAgent);
	}
	
}
