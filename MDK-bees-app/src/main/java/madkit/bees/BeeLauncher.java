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
package madkit.bees;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.IntStream;

import madkit.gui.UIProperty;
import madkit.kernel.Agent;
import madkit.simulation.EngineAgents;
import madkit.simulation.SimuLauncher;
import madkit.simulation.viewer.RolesPopulationLineChartDrawer;

/**
 * The agent that launches the bees simulation. It launches bees and queens and
 * kills them randomly. It uses a custom scheduler, environment and viewer by
 * defining the EngineAgents annotation.
 * 
 * @version 6.0
 */

@EngineAgents(scheduler = BeeScheduler.class, environment = BeeEnvironment.class, viewers = { BeeViewer.class,
		RolesPopulationLineChartDrawer.class }

)
public class BeeLauncher extends SimuLauncher {

	@UIProperty
	private int beesNumber = 200_000;

	@UIProperty
	private boolean randomLaunching = true;

	private ArrayList<Agent> queensList = new ArrayList<>();
	private ArrayList<Agent> beesList = new ArrayList<>(beesNumber * 2);

	@Override
	protected void onLaunchSimulatedAgents() {
		getLogger().info(() -> "Launching bees !");
		launchBees(beesNumber);
		launchQueens(1);
	}

	@Override
	protected void onLive() {
		while (isAlive() && getScheduler().isAlive()) {
			pause(prng().nextInt(1000, 4500));
			if (randomLaunching) {
				killBees(false, 150);
				if (prng().nextDouble() < .8) {
					if (prng().nextDouble() < .5) {
						if (queensList.size() > 1) {
							if (queensList.size() > 7) {
								killBees(true, prng().nextInt(1, 7));
							} else {
								killBees(true, prng().nextInt(1, 2));
							}
						}
					} else if (queensList.size() < 10)
						launchQueens(prng().nextInt(1, 2));
				} else if (prng().nextDouble() < .3) {
					if (beesList.size() < 200000 && Runtime.getRuntime().freeMemory() > 100000) {
						launchBees(prng().nextInt(5000, 15000));
					}
				} else {
					killBees(false, prng().nextInt(1, 500));
				}
			}
		}
	}

	@Override
	protected void onEnd() {
		getLogger().info("I am done. Bye !");
		super.onEnd();
	}

	private void launchBees(int numberOfBees) {
		getLogger().info(() -> "Launching " + numberOfBees + " bees");
		IntStream.range(0, numberOfBees).parallel().forEach(i -> {
			launchAgent(new Bee());
		});
	}

	private void launchQueens(int numberOfQueens) {
		getLogger().info(() -> "Launching " + numberOfQueens + " queen bees");
		for (int i = 0; i < numberOfQueens; i++) {
			final QueenBee newQueen = new QueenBee();
			launchAgent(newQueen);
			queensList.add(newQueen);
		}
	}

	private void killBees(boolean queen, int number) {
		List<Agent> l;
		int j = 0;
		if (queen)
			l = queensList;
		else
			l = beesList;
		for (final Iterator<Agent> i = l.iterator(); i.hasNext() && j < number; j++) {
			if (j % 100 == 0) {
				Thread.yield();
			}
			final Agent a = i.next();
			if (a != null) {
				i.remove();
				killAgent(a);
			} else
				break;
		}
	}

	public int getBeesNumber() {
		return beesNumber;
	}

	public void setBeesNumber(int beesNumber) {
		this.beesNumber = beesNumber;
	}

	public static void main(String[] args) {
		executeThisAgent();
	}

	/**
	 * @return the randomLaunching
	 */
	public boolean isRandomLaunching() {
		return randomLaunching;
	}

	/**
	 * @param randomLaunching the randomLaunching to set
	 */
	public void setRandomLaunching(boolean randomLaunching) {
		this.randomLaunching = randomLaunching;
	}

}
