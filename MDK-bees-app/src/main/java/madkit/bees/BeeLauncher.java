/*******************************************************************************
 * Copyright (c) 1997, 2025, MaDKit Team
 *
 * This software is a computer program whose purpose is to
 * provide a lightweight Java API for developing and simulating 
 * Multi-Agent Systems (MAS) using an organizational perspective.
 *
 * This software is governed by the CeCILL-C license under French law and
 * abiding by the rules of distribution of free software.  You can  use,
 * modify and/ or redistribute the software under the terms of the CeCILL-C
 * license as circulated by CEA, CNRS and INRIA at the following URL
 * "http://www.cecill.info".
 *
 * As a counterpart to the access to the source code and  rights to copy,
 * modify and redistribute granted by the license, users are provided only
 * with a limited warranty  and the software's author,  the holder of the
 * economic rights,  and the successive licensors  have only  limited
 * liability.
 *
 * In this respect, the user's attention is drawn to the risks associated
 * with loading,  using,  modifying and/or developing or reproducing the
 * software by the user in light of its specific status of free software,
 * that may mean  that it is complicated to manipulate,  and  that  also
 * therefore means  that it is reserved for developers  and  experienced
 * professionals having in-depth computer knowledge. Users are therefore
 * encouraged to load and test the software's suitability as regards their
 * requirements in conditions enabling the security of their systems and/or
 * data to be ensured and,  more generally, to use and operate it in the
 * same conditions as regards security.
 *
 * The fact that you are presently reading this means that you have had
 * knowledge of the CeCILL-C license and that you accept its terms.
 *******************************************************************************/
package madkit.bees;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.IntStream;

import madkit.gui.UIProperty;
import madkit.kernel.Agent;
import madkit.simulation.EngineAgents;
import madkit.simulation.SimuLauncher;

/**
 * The agent that launches the bees simulation. It launches bees and queens and
 * kills them randomly. It uses a custom scheduler, environment and viewer by
 * defining the EngineAgents annotation.
 * 
 * @version 6.0
 */

@EngineAgents(scheduler = BeeScheduler.class, environment = BeeEnvironment.class, viewers = { BeeViewer.class })
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
		IntStream.range(0, numberOfBees).parallel().forEach(_ -> {
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
