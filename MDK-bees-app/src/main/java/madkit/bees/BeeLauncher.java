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

import madkit.kernel.Agent;
import madkit.kernel.Message;
import madkit.simulation.EngineAgents;
import madkit.simulation.Environment;
import madkit.simulation.Parameter;
import madkit.simulation.SimulationEngine;
import madkit.simulation.viewer.RolesPopulationLineChartDrawer;

/**
 * The agent that launches the simulation
 * 
 * @version 2.0.0.3
 * @author Fabien Michel
 */

@EngineAgents(scheduler = BeeScheduler.class, environment = BeeEnvironment.class, viewers = { BeeViewer.class })
public class BeeLauncher extends SimulationEngine {

	@Parameter
	private static int beesNumber = 200_000;

	private ArrayList<Agent> queensList = new ArrayList<>();
	private ArrayList<Agent> beesList = new ArrayList<>(beesNumber * 2);
	private boolean randomMode = true;

	@Override
	protected void launchSimulatedAgents() {
		getLogger().info(() -> "Launching bees !");
		launchBees(beesNumber);
		launchQueens(1);
	}

	/**
	 * So that I can react to {@link BeeLauncherAction#RANDOM_MODE} message
	 */
	@SuppressWarnings("unused")
	private void randomMode(boolean on) {
		randomMode = on;
	}

	enum BeeLauncherAction {

		RANDOM_MODE, LAUNCH_BEES,
	}

	@Override
	protected void onLive() {
		while (isAlive() && getScheduler().isAlive()) {
			Message m = waitNextMessage(prng().nextInt(500, 4500));
			if (m != null) {
//				proceedEnumMessage((EnumMessage<?>) m);
			}
			if (randomMode) {
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
		queensList = null;
		beesList = null;
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

	public static int getBeesNumber() {
		return beesNumber;
	}

	public static void setBeesNumber(int beesNumber) {
		BeeLauncher.beesNumber = beesNumber;
	}

	public static void main(String[] args) {
//		executeThisAgent(1, "--noLog");
		executeThisAgent();
	}

}
