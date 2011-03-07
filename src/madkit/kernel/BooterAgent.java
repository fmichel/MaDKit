/*
 * Copyright 1997-2011 Fabien Michel, Olivier Gutknecht, Jacques Ferber
 * 
 * This file is part of MadKit.
 * 
 * MadKit is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * MadKit is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with MadKit. If not, see <http://www.gnu.org/licenses/>.
 */
package madkit.kernel;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.util.HashMap;
import java.util.logging.Level;
import javax.swing.JFrame;
import javax.swing.JMenuBar;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

import madkit.kernel.gui.MadKitGUIsManager;
import madkit.kernel.gui.IOPanel;
import madkit.kernel.gui.AgentGUIModel;

/**
 * @author Fabien Michel
 * @version 0.9
 * @since MadKit 5.0
 *
 */
class BooterAgent extends AbstractAgent implements MadKitGUIsManager{

	private static final long serialVersionUID = 8510009212697885996L;
	final private HashMap<AbstractAgent, JFrame> guis;
	final private HashMap<AbstractAgent, Component> guisComponent;


	public BooterAgent() {
		setLogLevel(Level.INFO);
		guis = new HashMap<AbstractAgent, JFrame>();
		guisComponent = new HashMap<AbstractAgent, Component>();
	}


	public boolean isConnected(){//TODO verify that
		return false;
//		return kernel.get().getNetAgent() != null && kernel.get().getNetAgent().getRunState().get() != AbstractAgent.TERMINATED;
	}


//	public void stopNetwork(){
//		final NetworkMessage<Object> nm = new NetworkMessage<Object>(null);
//		nm.setCode(NetworkMessage.STOP_NETWORK);
//		kernel.getNetAgent().receiveMessage(nm);
//	}

//	protected int launchAgentBucket(String AgentClassName,int bucketSize){
//		return myKernel.launchAgentBucket(AgentClassName, bucketSize);
//	}


	public void setupGUIOf(final AbstractAgent a){
		if(guis.containsKey(a))
			return;
		JFrame f = new JFrame(a.getName());
		Component c = a.getGUIComponent();
		//		a.initGUI(); //TODO why not ?

		if (c == null)
			c = new IOPanel();
		JMenuBar menuBar = new JMenuBar();
		menuBar.add(madkit.kernel.gui.Utils.createLaunchingMenu(a));
		menuBar.add(madkit.kernel.gui.Utils.createLogLevelMenu(a));
		f.setJMenuBar(menuBar);
		f.getContentPane().add("Center",c);
		f.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		f.addWindowListener(new WindowAdapter() {
			public void windowClosed(java.awt.event.WindowEvent e) {
				guis.remove(a);
				if (a != null)
					killAgent(a); //TODO !!
			}
		});
		if ((c.getPreferredSize().width < 0) || (c.getPreferredSize().height < 0))
			c.setPreferredSize(new Dimension(400, 300));
		f.setSize(c.getPreferredSize());
		if(c instanceof AgentGUIModel){
			Point location = ((AgentGUIModel) c).getGUIPreferredlocation();
			if ( (location.x < 0) || (location.y < 0)){
				f.setLocationRelativeTo(null);
				checkLocation(f);
			} 
			else{
				f.setLocation(location);
			}
//			if (a.getLogger() != null) {
//				OutputStream os = ((AgentGUIModel) c).getOutputStream();
//				if(os != null){
//					Handler h = new StreamHandler(os, AgentLogger.agentFileFormatter){
//						@Override
//						public synchronized void publish(LogRecord record) {
//							super.publish(record);
//							flush();
//						}
//					};
//					h.setLevel(a.getLogger().getLevel());
//					a.getLogger().addHandler(h);
//				}
//			}
		}
		f.setVisible(true);
		guisComponent.put(a,c);
		guis.put(a, f);
	}

	/**
	 * @param f
	 * @param location
	 */
	private void checkLocation(JFrame f) {//TODO guis repartition
		int x = f.getX();
		int y = f.getY();
		for (JFrame f1 : guis.values()) {
			if(f1.getLocation().equals(f.getLocation())){
				x += 10;
				x = x > 0 ? x : 0;
				y += 10;
				y = y > 0 ? y : 0;
				f.setLocation(x,y);
			}
		}
	}
	
	public void setGUILocationOf(AbstractAgent a, Point location){
		if(! guis.containsKey(a)){
			return;
		}
		JFrame f = guis.get(a);
		if ( (location.x < 0) || (location.y < 0)){
			f.setLocationRelativeTo(null);
			checkLocation(f);
		} 
		else{
			f.setLocation(location);
		}
	}

	/**
	 * @param abstractAgent
	 */
	public void disposeGUIOf(AbstractAgent abstractAgent) {
		final JFrame f = guis.remove(abstractAgent);
		if(f != null){
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					f.dispose();
				}
			});
		}
		
	}

		/////////////////////////////////////// UTILITIES ////////////////////////////////


		protected void noAgentConsoleLog(){
			setMadkitProperty(Madkit.noAgentConsoleLog, "true");
		}

		protected void setAgentLogLevel(Level level){
			setMadkitProperty(Madkit.agentLogLevel, level.toString());
		}

		protected void setMadkitLogLevel(Level level){
			setMadkitProperty(Madkit.MadkitLogLevel, level.toString());
		}

		protected void noMadkitConsoleLog(){
			setMadkitProperty(Madkit.noMadkitConsoleLog, "true");
		}

		protected void agentsLogFileOn(){
			setMadkitProperty(Madkit.agentsLogFile, "bin/agents_log_file");
		}

//		protected void autoAgentLogFile(){
//			setMadkitProperty(Madkit.autoAgentLogFile, "bin");
//		}

		protected void madkitLogFileOn(){
			setMadkitProperty(Madkit.MadkitLogFile, "bin/madkit_kernel");
		}


		/* (non-Javadoc)
		 * @see madkit.kernel.AgentsGUIManager#getGUIComponentOf(madkit.kernel.AbstractAgent)
		 */
		@Override
		public Component getGUIComponentOf(AbstractAgent abstractAgent) {
			return guisComponent.get(abstractAgent);
		}




	}
