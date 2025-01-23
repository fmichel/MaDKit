package madkit.bees;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import madkit.gui.UIProperty;
import madkit.kernel.Agent;
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
	private int beesNumber = 100_000;

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
				killBees(false, prng().nextInt(1000, 4500));
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
					if (beesList.size() < 200000 || Runtime.getRuntime().freeMemory() > 100000) {
						launchBees(prng().nextInt(10000, 15000));
					}
				} else {
					killBees(false, prng().nextInt(5000, 15000));
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
		for (int i = 0; i < numberOfBees; i++) {
			Bee newBee = new Follower();
			launchAgent(newBee);
			beesList.add(newBee);
		}
	}

	private void launchQueens(int numberOfQueens) {
		getLogger().info(() -> "Launching " + numberOfQueens + " queen bees");
		for (int i = 0; i < numberOfQueens; i++) {
			QueenBee newQueen = new QueenBee();
			launchAgent(newQueen);
			queensList.add(newQueen);
		}
	}

	private void killBees(boolean queen, int number) {
		getLogger().info(() -> "Killing " + number + (queen ? " queens" : " bees"));
		List<Agent> bees;
		if (queen)
			bees = queensList;
		else
			bees = beesList;
		int j = 0;
		for (Iterator<Agent> i = bees.iterator(); i.hasNext() && j < number; j++) {
			killAgent(i.next());
			i.remove();
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
