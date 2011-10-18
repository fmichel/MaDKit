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
package madkit.gui;

import java.awt.FlowLayout;
import java.awt.Image;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;

import madkit.gui.actions.MadkitAction;
import madkit.kernel.AbstractAgent;

/**
 * @author Fabien Michel
 * @since MadKit 5.0.0.9
 * @version 0.9
 * 
 */
public class AgentStatusPanel extends JPanel implements AgentUIComponent {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5745998699827681837L;
	
	final static private ImageIcon image = new ImageIcon(MadkitAction.MADKIT_LAUNCH_NETWORK.getImageIcon().getImage().getScaledInstance(10, 10, Image.SCALE_SMOOTH));
	final private AbstractAgent myAgent;
	final private JLabel network;
	
	AgentStatusPanel(AbstractAgent a){
		super(new FlowLayout(FlowLayout.RIGHT));
		myAgent = a;
		network = new JLabel();
		updateAgentUI();
		add(network);
		GUIToolkit.addUIListenerFor(a, this);
	}

	/* (non-Javadoc)
	 * @see madkit.gui.AgentUIComponent#updateAgentUI()
	 */
	@Override
	public void updateAgentUI() {
		if(myAgent.isAlive() && myAgent.isKernelOnline()){
			network.setIcon(image);
			network.setToolTipText("Kernel "+myAgent.getKernelAddress()+" is online");
		}
		else{
			network.setIcon(null);
//			network.setToolTipText("Kernel "+myAgent.getKernelAddress()+" is offline");
		}
	}

}
