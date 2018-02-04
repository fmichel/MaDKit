/*
 * Copyright 1997-2012 Fabien Michel, Olivier Gutknecht, Jacques Ferber
 * 
 * This file is part of MaDKit_Demos.
 * 
 * MaDKit_Demos is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * MaDKit_Demos is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with MaDKit_Demos. If not, see <http://www.gnu.org/licenses/>.
 */
package com.distrimind.madkitdemos.pingpong;

import java.awt.Toolkit;
import java.util.ArrayList;
import java.util.List;

import com.distrimind.madkit.kernel.AbstractAgent;
import com.distrimind.madkit.kernel.Agent;

/**
 * @author Fabien Michel
 * @version 2.0
 */
public class PingPongDemoLauncherAgent extends Agent {

	/**
	 * 
	 */
	private List<AbstractAgent> agentsList = new ArrayList<>();

	@Override
	protected void activate() throws InterruptedException {
		int initialPause = 2000;
		int screenWidth = (int) (Toolkit.getDefaultToolkit().getScreenSize().getWidth() - 200);
		int screenHeight = (int) (Toolkit.getDefaultToolkit().getScreenSize().getHeight() - 200);
		for (int i = 0; i < screenWidth; i = i + 400) {
			for (int j = 0; j < screenHeight; j = j + 300) {
				PingPong p = new PingPong();
				launchAgent(p, true);
				agentsList.add(p);
				getLogger().info("Ping Pong launched");
				p.setFrameLocation(i, j);
				getLogger().info("Ping Pong launched");
				pause((initialPause > 0 ? initialPause : 20));
				initialPause -= Math.random() * 100;
			}
		}
	}

	@Override
	protected void liveCycle() throws InterruptedException {
		pause(6000);
		int initialPause = 2000;
		AbstractAgent a = agentsList.remove((int) (agentsList.size() * Math.random()));
		killAgent(a);
		pause((initialPause > 0 ? initialPause : 100));
		initialPause -= Math.random() * 100;

		if (agentsList.isEmpty())
			this.killAgent(this);
	}

	
	public static void main(String[] args) {
		executeThisAgent(1, false);
	}

}
