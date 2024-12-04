
package madkit.kernel;

import java.util.concurrent.CompletableFuture;

import javafx.stage.Stage;
import madkit.gui.fx.FXManager;

/**
 * The `AgentFxStage` class extends the JavaFX `Stage` class to provide a stage
 * that is associated with an `Agent`. It includes functionality to handle the
 * stage's close request and optionally close the stage when the agent ends.
 */
public class AgentFxStage extends Stage {

	/**
	 * Constructs an `AgentFxStage` with the specified agent and sets
	 * `autoCloseOnAgentEnd` to <code>true</code>.
	 *
	 * @param agent the agent associated with this stage
	 */
	public AgentFxStage(final Agent agent) {
		this(agent, true);
	}

	/**
	 * Constructs an `AgentFxStage` with the specified agent and the specified
	 * auto-close behavior.
	 *
	 * @param agent               the agent associated with this stage
	 * @param autoCloseOnAgentEnd if <code>true</code>, the stage will automatically
	 *                            close when the agent ends
	 */
	public AgentFxStage(final Agent agent, boolean autoCloseOnAgentEnd) {
		setTitle(agent.getName());
		setOnCloseRequest(ae -> {
			try {
				agent.killAgent(agent, 2);
			} catch (Exception e) {
				e.printStackTrace();
			}
		});
		if (autoCloseOnAgentEnd) {
			CompletableFuture.runAsync(() -> {
				try {
					synchronized (agent.alive) {
						agent.alive.wait();
					}
					FXManager.runLater(() -> close());
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			});
		}
	}
}
