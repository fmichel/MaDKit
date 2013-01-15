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
import static madkit.kernel.AbstractAgent.ReturnCode.NOT_IN_GROUP;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.*;

import java.awt.Component;

import javax.swing.AbstractButton;

import org.junit.Test;

import madkit.gui.menu.LaunchAgentsMenu;
import madkit.kernel.JunitMadkit;
import madkit.kernel.Madkit;
import madkit.kernel.Madkit.BooleanOption;
import madkit.kernel.Madkit.Option;

/**
 * @author Fabien Michel
 * @since MadKit 5.0.0.17
 * @version 0.9
 * 
 */
@SuppressWarnings("serial")
public class NoPackageTest extends JunitMadkit {

	@Test
	public void inMenuTest() {
		addMadkitArgs(BooleanOption.desktop.toString());
		launchTest(new NoPackageAgent() {
			protected void activate() {
				if(logger != null)
					logger.info("w");
				LaunchAgentsMenu m = new LaunchAgentsMenu(this);
				for (Component iterable_element : m.getMenuComponents()) {
					if(((AbstractButton) iterable_element).getText().equals(NoPackageAgent.class.getName()))
						return;
				}
				fail("not in menu");
			}
		});
	}


}
