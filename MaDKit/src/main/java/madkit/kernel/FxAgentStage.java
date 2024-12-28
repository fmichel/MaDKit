
package madkit.kernel;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

import javafx.scene.image.Image;
import javafx.stage.Stage;
import madkit.gui.FXManager;

/**
 * The `FxAgentStage` class extends the JavaFX `Stage` class to provide a stage
 * that is associated with an `Agent`. It includes functionality to handle the
 * stage's close request and optionally close the stage when the agent ends.
 */

public class FxAgentStage extends Stage {

	static final Image MADKIT_LOGO = new Image(FxAgentStage.class.getResourceAsStream("/madkit/images/madkit_logo.png"));

	private static Map<KernelAddress, Set<Agent>> agentsWithStage = new ConcurrentHashMap<>();

	private Agent agent;

	/**
	 * Constructs an `FxAgentStage` with the specified agent and sets
	 * `autoCloseOnAgentEnd` to <code>true</code>.
	 *
	 * @param agent the agent associated with this stage
	 */
	public FxAgentStage(Agent agent) {
		this(agent, true);
	}

	/**
	 * Constructs an `FxAgentStage` with the specified agent and the specified
	 * auto-close behavior.
	 *
	 * @param agent               the agent associated with this stage
	 * @param autoCloseOnAgentEnd if <code>true</code>, the stage will automatically
	 *                            close when the agent ends
	 */
	public FxAgentStage(Agent agent, boolean autoCloseOnAgentEnd) {
		this.agent = agent;
		agentsWithStage.computeIfAbsent(agent.getKernelAddress(), k -> new HashSet<>()).add(agent);
		getIcons().add(MADKIT_LOGO);
		setTitle(agent.getName());
		setOnCloseRequest(ae -> agent.killAgent(agent, 2));
		if (autoCloseOnAgentEnd) {
			CompletableFuture.runAsync(() -> {
				try {
					synchronized (agent.alive) {
						agent.alive.wait();// NOSONAR
					}
					FXManager.runLater(this::close);
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
				}
			});
		}
	}

	@Override
	public void close() {
		agentsWithStage.values().forEach(agentSet -> agentSet.remove(agent));
		super.close();
	}

	/**
	 * Returns the agents associated with the specified kernel address that have an
	 * `FxAgentStage`.
	 * @return the agentsWithStage
	 */
	static Collection<Agent> getAgentsWithStage(KernelAddress ka) {
		return new HashSet<>(agentsWithStage.computeIfAbsent(ka, k -> new HashSet<>()));
	}

}
