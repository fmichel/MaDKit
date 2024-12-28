package madkit.action;

import static java.awt.event.KeyEvent.VK_C;
import static java.awt.event.KeyEvent.VK_DOLLAR;
import static java.awt.event.KeyEvent.VK_O;
import static java.awt.event.KeyEvent.VK_Q;
import static java.awt.event.KeyEvent.VK_R;
import static java.awt.event.KeyEvent.VK_T;
import static java.awt.event.KeyEvent.VK_W;

import java.net.InetAddress;

import org.controlsfx.control.action.Action;

import madkit.agr.DefaultMaDKitRoles;
import madkit.agr.LocalCommunity;
import madkit.agr.LocalCommunity.Groups;
import madkit.gui.FXAction;
import madkit.kernel.Agent;
import madkit.messages.KernelMessage;

/**
 * Enum representing kernel actions. This especially could be used to
 * communicate with the kernel in order to trigger kernel's actions.
 * 
 * @since MaDKit 5.0.0.14
 * @version 6.0
 */

public enum KernelAction {

	/**
	 * Close MaDKit
	 */
	EXIT(VK_Q),
	/**
	 * Clone the MaDKit session with its initial options
	 */
	COPY(VK_C),
	/**
	 * Restart MaDKit with its initial options
	 */
	RESTART(VK_R),
	/**
	 * Start the network
	 */
	LAUNCH_NETWORK(VK_W),
	/**
	 * Stop the network
	 */
	STOP_NETWORK(VK_T),

	/**
	 * Makes a redirection of the out and err to a MaDKit agent.
	 */
	CONSOLE(VK_O),

	// //Actions that need parameters, i.e. not global
	/**
	 * Launch an agent
	 */
	LAUNCH_AGENT(VK_DOLLAR),
	/**
	 * Launch a MAS configuration
	 */
	LAUNCH_MAS(VK_DOLLAR),
	/**
	 * Launch an XML configuration
	 */
	LAUNCH_XML(VK_DOLLAR),
	/**
	 * Kill an agent
	 */
	KILL_AGENT(VK_DOLLAR),
	/**
	 * Connection to the MaDKit web repository
	 */
	CONNECT_WEB_REPO(VK_DOLLAR),
	/**
	 * For connecting kernels in a wide area network. It requires a parameter of
	 * type {@link InetAddress}.
	 */
	CONNECT_TO_IP(VK_DOLLAR);

	private ActionData actionInfo;

	private KernelAction(int keyEvent) {
		actionInfo = new ActionData(name(), keyEvent);
	}

	public Action newActionFor(Agent agent, Object... parameters) {
		return new FXAction(actionInfo, ae -> request(agent, parameters));
	}

	public void request(Agent requester, Object... parameters) {
		if (requester.isAlive()) {
			requester.send(new KernelMessage(KernelAction.this, parameters), LocalCommunity.NAME, Groups.SYSTEM,
					DefaultMaDKitRoles.GROUP_MANAGER_ROLE);
		}
	}

}
