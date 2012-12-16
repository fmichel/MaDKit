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

import madkit.classreloading.anotherPackage.Fake;
import madkit.kernel.Agent;

/**
 * @author Fabien Michel
 * @since MadKit 5.0.0.20
 * @version 0.9
 * 
 */
public class ReloadClass extends Agent {

	@Override
	protected void live() {
		if (logger != null)
			logger.info("\n\n " + new Fake().toString());
		pause(8000);
		try {
			getMadkitClassLoader().reloadClass(Fake.class.getName());
			final Class<?> newestClassVersion = getMadkitClassLoader().getNewestClassVersion(Fake.class.getName());
			logger.info(newestClassVersion.newInstance().toString());
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		executeThisAgent();
	}

}
