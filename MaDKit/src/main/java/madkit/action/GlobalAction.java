package madkit.action;

import java.awt.event.KeyEvent;
import java.io.IOException;

import madkit.kernel.MadkitClassLoader;

/**
 * Global actions that can be triggered from anywhere during execution.
 * 
 * @since MaDKit 6.0
 * @version 6.0
 */
public class GlobalAction {

	private GlobalAction() {
		throw new IllegalAccessError();
	}

	/**
	 * An action that Launches the jconsole tool if available. It is set to
	 * <code>null</code> if jconsole is unavailable: in environments not containing
	 * the JDK.
	 */
	public static final ActionData JCONSOLE;

	static {
		final String jconsolePath = MadkitClassLoader.findJavaExecutable("jconsole");
		if (jconsolePath == null) {
			JCONSOLE = null;
		} else {
			JCONSOLE = new ActionData("JCONSOLE", KeyEvent.VK_L) {
				@Override
				public void doAction() {
					final String pid = java.lang.management.ManagementFactory.getRuntimeMXBean().getName();
					try {
						new ProcessBuilder(jconsolePath, pid.substring(0, pid.indexOf('@'))).start();
					} catch (IOException e1) {
						e1.printStackTrace();
					}
				}
			};
		}
	}
}