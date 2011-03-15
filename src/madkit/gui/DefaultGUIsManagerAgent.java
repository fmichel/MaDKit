package madkit.gui;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.WindowAdapter;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.logging.Level;

import javax.swing.JFrame;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

import madkit.kernel.AbstractAgent;
import madkit.kernel.Madkit;

public class DefaultGUIsManagerAgent extends AbstractAgent implements GUIsManagerAgent {

	final private ConcurrentMap<AbstractAgent, JFrame> guis;

	public DefaultGUIsManagerAgent(){
		setLogLevel(Level.INFO);
		guis = new ConcurrentHashMap<AbstractAgent, JFrame>();
	}
	
	@Override
	protected void activate() {
		requestRole(Madkit.Roles.LOCAL_COMMUNITY, Madkit.Roles.SYSTEM_GROUP, Madkit.Roles.GUI_MANAGER_ROLE);
	}
	
	@Override
	public void setupGUIOf(final AbstractAgent agent) {
		JFrame f = new JFrame(agent.getName());

		f.setJMenuBar(createMenuBarFor(agent));
		
		f.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		f.addWindowListener(new WindowAdapter() {
			public void windowClosed(java.awt.event.WindowEvent e) {
				guis.remove(agent);
				if (agent != null)
					killAgent(agent); //TODO !!
			}
		}); 

		f.setSize(-1,-1);
		f.setLocation(-1,-1);
		
		agent.setupFrame(f);//TODO catch failures because of delegation
		
		if(f.getSize().equals(new Dimension(-1,-1))){
			f.setSize(400,300);
		}
		
		if(f.getLocation().equals(new Point(-1,-1))){
			f.setLocationRelativeTo(null);
			checkLocation(f);
		}
		guis.put(agent, f);

		f.setVisible(true);
	}

	private JMenuBar createMenuBarFor(AbstractAgent a) {
		JMenuBar menuBar = new JMenuBar();
		menuBar.add(madkit.gui.Utils.createLaunchingMenu(a));
		menuBar.add(madkit.gui.Utils.createLogLevelMenu(a));
		return menuBar;
	}

	@Override
	public void disposeGUIOf(AbstractAgent agent) {
		final JFrame f = guis.remove(agent);
		if(f != null){
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					f.dispose();
				}
			});
		}
	}

	/**
	 * @param f
	 * @param location
	 */
	private void checkLocation(JFrame f) {//TODO guis repartition
		boolean notGood = true;
		Point location = f.getLocation();
		while(notGood){
			notGood = false;
			for (JFrame f1 : guis.values()) {
				if(location.equals(f1.getLocation())){
					notGood = true;
					location.x += 20;
					location.x = location.x > 0 ? location.x : 0;
					location.y += 20;
					location.y = location.y > 0 ? location.y : 0;
				}
			}
		}
	f.setLocation(location);
	}

}
