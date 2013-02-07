/*
 * Copyright 2012 Fabien Michel
 * 
 * This file is part of MaDKit.
 * 
 * MaDKit is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * MaDKit is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with MaDKit. If not, see <http://www.gnu.org/licenses/>.
 */
package madkit.classreloading;

import java.util.logging.Level;

import madkit.classreloading.anotherPackage.Fake;
import madkit.kernel.Agent;
import madkit.kernel.Madkit;

/**
 * @author Fabien Michel
 * @since MadKit 5.0.0.18
 * @version 0.9
 * 
 */
public class TestAgent extends Agent {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * 
	 */
	public TestAgent() {
		setLogLevel(Level.ALL);
//		Activator<AbstractAgent> a = new Activator<AbstractAgent>("test", "r", "r") {
//			public void adding(AbstractAgent theAgent) {
//				ActionListener al = new ActionListener() {
//					@Override
//					public void actionPerformed(ActionEvent e) {
//						TestAgent.this.activate();
//					}
//				};
//				al.actionPerformed(null);
//			}
//		};
//		a.execute();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see madkit.kernel.AbstractAgent#activate()
	 */
	@Override
	protected void activate() {
		setLogLevel(Level.ALL);
		super.activate();
		if (logger != null) {
			logger.info("\n\na\n\n");
			FakeObject o = new FakeObject();
			logger.info("\nfake is " + o);
			logger.info("\nfake2 is " + (new  Fake().toString()));
			pause(8000);
			try {
				System.err.println(System.getProperty("java.class.path"));
				getMadkitClassLoader().reloadClass("madkit.classreloading.anotherPackage.Fake");
				logger.info("after reload : "+getMadkitClassLoader().loadClass("madkit.classreloading.anotherPackage.Fake").newInstance().toString());
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			} catch (InstantiationException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
			logger.info("\nfake3 is " + (new Fake().toString()));
			pause(8000);
			try {
				getMadkitClassLoader().reloadClass("madkit.classreloading.anotherPackage.Fake");
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			logger.info("\nfake4 is " + (new Fake().toString()));
		}
	}

	/**
	 * 
	 */
	@Override
	protected void live() {
		if (logger != null)
			logger.info("a");
		pause(1000);
	}

	public static void main(String[] argss) {
		String[] args = { "--agentLogLevel", "INFO", "--MadkitLogLevel", "OFF", "--orgLogLevel", "OFF", "--launchAgents",
				TestAgent.class.getName() + ",true" };
		Madkit.main(args);
	}

}

class FakeObject {
	@Override
	public String toString() {
		return "a";
	}
}