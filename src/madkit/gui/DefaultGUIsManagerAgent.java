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
import madkit.kernel.Agent;
import madkit.kernel.KernelMessage;
import madkit.kernel.Madkit;
import madkit.kernel.Message;

public class DefaultGUIsManagerAgent extends Agent  {

	final private ConcurrentMap<AbstractAgent, JFrame> guis;
	private boolean shuttedDown = false;

	public DefaultGUIsManagerAgent(){
		super(true);
		setLogLevel(Level.INFO);
		guis = new ConcurrentHashMap<AbstractAgent, JFrame>();
	}

	@Override
	protected void activate() {
		requestRole(Madkit.Roles.LOCAL_COMMUNITY, Madkit.Roles.SYSTEM_GROUP, Madkit.Roles.GUI_MANAGER_ROLE);
		setLogLevel(Level.INFO);
	}

	@Override
	protected void live() {
		while (! shuttedDown) {
			Message m = waitNextMessage();
			if(m instanceof GUIMessage){
				handleGUIMessage((GUIMessage) m);
			}
			else{
				if(logger != null)
					logger.warning("I received a message that I do not understang. Discarding "+m);
			}
		}
	}

	private void handleGUIMessage(GUIMessage m) {
		switch (m.getCode()) {
		case SETUP_GUI:
			if(logger != null)
				logger.fine("Setting up GUI of"+m.getContent());
			setupGUIOf(m.getContent());
			sendReply(m, new Message());
			break;
		case DISPOSE_GUI:
			disposeGUIOf(m.getContent());
			break;
		case SHUTDOWN:
			shuttedDown = true;
			for (final JFrame f : guis.values()) {
				f.dispose();
			}
			guis.clear();
			sendReply(m, new Message());
		default:
			break;
		}

	}

	private void setupGUIOf(final AbstractAgent agent) {
		JFrame f = new JFrame(agent.getName());

		f.setJMenuBar(createMenuBarFor(agent));

		f.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		f.addWindowListener(new WindowAdapter() {
			public void windowClosed(java.awt.event.WindowEvent e) {
				if (guis.containsKey(agent)){
					guis.remove(agent);
					killAgent(agent); //TODO !!
				}
			}
		}); 

		f.setSize(400,300);
		f.setLocationRelativeTo(null);

		agent.setupFrame(f);//TODO catch failures because of delegation

		checkLocation(f);
		guis.put(agent, f);
		f.setVisible(true);
	}

	private JMenuBar createMenuBarFor(AbstractAgent a) {
		JMenuBar menuBar = new JMenuBar();
		menuBar.add(madkit.gui.Utils.createLaunchingMenu(a));
		menuBar.add(madkit.gui.Utils.createLogLevelMenu(a));
		return menuBar;
	}

	private void disposeGUIOf(AbstractAgent agent) {
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
