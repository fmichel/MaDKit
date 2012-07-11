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
	
	final private static Map<AbstractAgent,AgentStatusPanel> panels = new ConcurrentHashMap<AbstractAgent, AgentStatusPanel>(); 
	
	
	final static private ImageIcon image = new ImageIcon(KernelAction.LAUNCH_NETWORK.getActionInfo().getBigIcon().getImage().getScaledInstance(10, 10, Image.SCALE_SMOOTH));
	final private AbstractAgent myAgent;
	final private JLabel network;
	
	public AgentStatusPanel(AbstractAgent a){
		super(new FlowLayout(FlowLayout.RIGHT));
		myAgent = a;
		network = new JLabel();
		add(network);
		update();
			if (myAgent.hasGUI()) {
				panels.put(myAgent, this);
			}
	}
	
	public static void updateAll(){
		for (AgentStatusPanel panel : panels.values()) {
			panel.update();
		}
	}

	private void update() {
		if(myAgent.isAlive() && myAgent.isKernelOnline()){
			network.setIcon(image);
			network.setToolTipText("Kernel "+myAgent.getKernelAddress()+" is online");
		}
		else{
			network.setIcon(null);
//			network.setToolTipText("Kernel "+myAgent.getKernelAddress()+" is offline");
		}
	}

	public static void remove(AbstractAgent abstractAgent) {
		panels.remove(abstractAgent);
	}
	
}
