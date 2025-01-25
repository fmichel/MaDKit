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

package madkit.kernel;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javafx.scene.image.Image;
import javafx.stage.Stage;
import madkit.gui.FXExecutor;

/**
 * The `FXAgentStage` class extends the JavaFX {@link Stage} class to provide a stage that
 * is associated with an `Agent`. It includes functionality to handle the stage's close
 * request and optionally close the stage when the agent ends.
 */

public class FXAgentStage extends Stage {

	private static final Image MADKIT_LOGO = new Image(
			FXAgentStage.class.getResourceAsStream("/madkit/images/madkit_logo.png"));

	private static final Map<KernelAddress, Set<Agent>> agentsWithStage = new ConcurrentHashMap<>();

	private Agent agent;

	/**
	 * Constructs an `FXAgentStage` with the specified agent and sets `autoCloseOnAgentEnd` to
	 * <code>true</code>.
	 * 
	 * It must be called on the JavaFX thread. See {@link FXExecutor}
	 *
	 * @param agent the agent associated with this stage
	 */
	public FXAgentStage(Agent agent) {
		this(agent, true);
	}

	/**
	 * Constructs an `FXAgentStage` with the specified agent and the specified auto-close
	 * behavior.
	 * 
	 * It must be called on the JavaFX thread. See {@link FXExecutor}
	 *
	 * @param agent               the agent associated with this stage
	 * @param autoCloseOnAgentEnd if <code>true</code>, the stage will automatically close
	 *                            when the agent ends
	 */
	public FXAgentStage(Agent agent, boolean autoCloseOnAgentEnd) {
		this.agent = agent;
		agentsWithStage.computeIfAbsent(agent.getKernelAddress(), _ -> new HashSet<>()).add(agent);
		getIcons().add(MADKIT_LOGO);
		setTitle(agent.getName());
		setOnCloseRequest(_ -> agent.killAgent(agent, 2));
		if (autoCloseOnAgentEnd) {
			Thread.ofVirtual().start(() -> {
				try {
					synchronized (agent.alive) {
						agent.alive.wait();
					}
					FXExecutor.runLater(this::close);
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
				}

			});
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void close() {
		agentsWithStage.values().forEach(agentSet -> agentSet.remove(agent));
		super.close();
	}

	/**
	 * Returns the agents associated with the specified kernel address that have an
	 * `FXAgentStage`.
	 * 
	 * @return the agentsWithStage
	 */
	static Collection<Agent> getAgentsWithStage(KernelAddress ka) {
		return new HashSet<>(agentsWithStage.computeIfAbsent(ka, _ -> new HashSet<>()));
	}

}
