/*******************************************************************************
 * MaDKit - Multi-agent systems Development Kit 
 * 
 * Copyright (c) 1998-2025 Fabien Michel, Olivier Gutknecht, Jacques Ferber...
 * 
 * This software is a computer program whose purpose is to
 * provide a lightweight Java API for developing and simulating 
 * Multi-Agent Systems (MAS) using an organizational perspective.
 *
 * This software is governed by the CeCILL-C license under French law and
 * abiding by the rules of distribution of free software.You can use,
 * modify and/ or redistribute the software under the terms of the CeCILL-C
 * license as circulated by CEA, CNRS and INRIA at the following URL
 * "http://www.cecill.info".
 *
 * As a counterpart to the access to the source code and rights to copy,
 * modify and redistribute granted by the license, users are provided only
 * with a limited warranty and the software's author, the holder of the
 * economic rights, and the successive licensors have only limited
 * liability.
 *
 * In this respect, the user's attention is drawn to the risks associated
 * with loading, using, modifying and/or developing or reproducing the
 * software by the user in light of its specific status of free software,
 * that may mean that it is complicated to manipulate, and that also
 * therefore means that it is reserved for developers and experienced
 * professionals having in-depth computer knowledge. Users are therefore
 * encouraged to load and test the software's suitability as regards their
 * requirements in conditions enabling the security of their systems and/or
 * data to be ensured and, more generally, to use and operate it in the
 * same conditions as regards security.
 *
 * The fact that you are presently reading this means that you have had
 * knowledge of the CeCILL-C license and that you accept its terms.
 *******************************************************************************/
package madkit.bees;

import java.util.List;

import madkit.gui.UIProperty;
import madkit.kernel.Agent;
import madkit.kernel.Probe;
import madkit.simulation.EngineAgents;
import madkit.simulation.SimuLauncher;

/**
 * The agent that launches the bees simulation. It launches bees and queens and kills them
 * randomly. It uses a custom scheduler, environment and viewer by defining the
 * EngineAgents annotation.
 * 
 * @version 6.0
 */
@EngineAgents(scheduler = BeeScheduler.class, environment = BeeEnvironment.class, viewers = { BeeViewer.class })
public class BeeLauncher extends SimuLauncher {

	@UIProperty
	private int numberOfStartingFollowers = 100_000;

	@UIProperty
	private boolean randomLaunching = true;

	private Probe followers;
	private Probe queens;

	/**
	 * On launch simulated agents.
	 */
	@Override
	protected void onLaunchSimulatedAgents() {
		getLogger().info(() -> "Launching bees !");
		launchBees(numberOfStartingFollowers);
		launchQueens(1);
	}

	/**
	 * On live.
	 */
	@Override
	protected void onLive() {
		followers = new Probe(getModelGroup(), BeeOrganization.FOLLOWER);
		addProbe(followers);
		queens = new Probe(getModelGroup(), BeeOrganization.QUEEN);
		addProbe(queens);
		while (isAlive() && getScheduler().isAlive()) {
			pause(prng().nextInt(1000, 4500));
			if (randomLaunching) {
				killBees(false, prng().nextInt(1000, 4500));
				if (prng().nextDouble() < .8) {
					if (prng().nextDouble() < .6) {
						if (queens.size() > 1) {
							if (queens.size() > 7) {
								killBees(true, prng().nextInt(1, 7));
							} else {
								killBees(true, prng().nextInt(1, 2));
							}
						}
					} else if (queens.size() < 10) {
						launchQueens(prng().nextInt(1, 2));
					}
				} else if (prng().nextDouble() < .4) {
					if (followers.size() < 200000 || Runtime.getRuntime().freeMemory() > 100000) {
						launchBees(prng().nextInt(10000, 50000));
					}
				} else {
					killBees(false, prng().nextInt(5000, 15000));
				}
			}
		}
	}

	/**
	 * 
	 */
	@Override
	protected void onEnd() {
		getLogger().info("Scheduler done. Quitting!");
		super.onEnd();
	}

	private void launchBees(int numberOfBees) {
		getLogger().info(() -> "Launching " + numberOfBees + " followers");
		for (int i = 0; i < numberOfBees; i++) {
			Bee newBee = new Follower();
			launchAgent(newBee);
		}
	}

	private void launchQueens(int numberOfQueens) {
		getLogger().info(() -> "Launching " + numberOfQueens + " queen bees");
		for (int i = 0; i < numberOfQueens; i++) {
			QueenBee newQueen = new QueenBee();
			launchAgent(newQueen);
		}
	}

	private void killBees(boolean queen, int number) {
		getLogger().info(() -> "Killing " + number + (queen ? " queens" : " followers"));
		List<Agent> bees;
		if (queen) {
			bees = queens.getAgents();
		} else {
			bees = followers.getAgents();
		}
		bees.subList(0, Math.min(number, bees.size())).forEach(this::killAgent);
	}

	/**
	 * Gets the bees number to launch at the beginning of the simulation.
	 *
	 * @return the bees number to launch at the beginning of the simulation
	 */
	public int getNumberOfStartingFollowers() {
		return numberOfStartingFollowers;
	}

	/**
	 * Sets the bees number to launch at the beginning of the simulation.
	 * 
	 * @param n the number of bees to launch at the beginning of the simulation
	 */
	public void setNumberOfStartingFollowers(int n) {
		this.numberOfStartingFollowers = n;
	}

	/**
	 * The main entry point of this simulation application.
	 *
	 * @param args the arguments
	 */
	public static void main(String[] args) {
		executeThisAgent();
	}

	/**
	 * If <code>true</code> bees are launched and killed randomly.
	 * 
	 * @return the randomLaunching
	 */
	public boolean isRandomLaunching() {
		return randomLaunching;
	}

	/**
	 * If <code>true</code> bees are launched and killed randomly.
	 * 
	 * @param randomLaunching the randomLaunching to set
	 */
	public void setRandomLaunching(boolean randomLaunching) {
		this.randomLaunching = randomLaunching;
	}

}
