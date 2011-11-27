package madkit.gui;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.HashSet;
import java.util.Set;

import javax.swing.Action;
import javax.swing.JMenu;
import javax.swing.JMenuItem;

import madkit.action.AgentAction;
import madkit.action.MKAbstractAction;
import madkit.kernel.AbstractAgent;

public class LaunchAgentsMenu extends JMenu {

	/**
	 * 
	 */
	private static final long serialVersionUID = 634377755586801986L;
	final static private Set<LaunchAgentsMenu> menus = new HashSet<LaunchAgentsMenu>(); 
	final private Action myAction;
	final private AbstractAgent myAgent;

	public LaunchAgentsMenu(final AbstractAgent agent) {
		super("Agents");
		setMnemonic(KeyEvent.VK_G);
		myAgent = agent;
		myAction = new  MKAbstractAction(AgentAction.LAUNCH_AGENT.getActionInfo()){
			/**
			 * 
			 */
			private static final long serialVersionUID = 6530886642947530268L;

			@Override
			public void actionPerformed(ActionEvent e) {
				agent.launchAgent(e.getActionCommand(),0,true);
			}
		};
			menus.add(this);
		update();
	}

	public static void updateAllMenus() {
		for (LaunchAgentsMenu menu : menus) {
			menu.update();
		}
	}

	private void addTomenu(Action a, JMenu subMenu, String className, boolean simpleName) {
		JMenuItem name = new JMenuItem(a);
		String displayedName = simpleName ? className.substring(className.lastIndexOf('.')+1, className.length()) : className;
		name.setText(displayedName);
		name.setAccelerator(null);
		name.setActionCommand(className);
		subMenu.add(name);
	}

	private void update() {
		removeAll();
		final Set<String> classesToLaunch = myAgent.getMadkitClassLoader().getAllAgentClasses();
		if (classesToLaunch.size() < 20) {
			for (String string : classesToLaunch) {
				addTomenu(myAction, this, string, false);
			}
		}
		else{
			String pckName = null;
			JMenu subMenu = null;
			for (String string : classesToLaunch) {
				String pck = string.substring(0,string.lastIndexOf('.'));
				if(pck.equals(pckName)){
					addTomenu(myAction, subMenu, string,true);
				}
				else{
					pckName = pck;
					subMenu = new JMenu(pck);
					add(subMenu);
					addTomenu(myAction, subMenu, string,true);
				}
			}
		}
		
	}

}
