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

import java.awt.Point;
import java.util.List;

import javafx.scene.paint.Color;
import madkit.gui.UIProperty;
import madkit.kernel.Agent;
import madkit.simulation.PropertyProbe;
import madkit.simulation.viewer.Viewer2D;

/**
 * @version 6.0
 * @author Fabien Michel
 */
public class BeeViewer extends Viewer2D {

	private PropertyProbe<BeeData> beeProbe;
	protected int nbOfBeesToLaunch = 30000;
	BeeEnvironment env;

	@UIProperty(category = "engine", displayName = "trail mode")
	private boolean trailMode = false;

	@UIProperty(category = "engine", displayName = "art mode")
	private boolean artMode = false;

	/**
	 * @return the artMode
	 */
	public boolean isArtMode() {
		return artMode;
	}

	/**
	 * @param artMode the artMode to set
	 */
	public void setArtMode(boolean artMode) {
		this.artMode = artMode;
	}

	/**
	 * @return the trailMode
	 */
	public boolean isTrailMode() {
		return trailMode;
	}

	/**
	 * @param trailMode the trailMode to set
	 */
	public void setTrailMode(boolean trailMode) {
		this.trailMode = trailMode;
	}

	@Override
	protected void onActivation() {
		env = getLauncher().getEnvironment();
//		getLogger().setLevel(Level.ALL);
		super.onActivation();
		beeProbe = new PropertyProbe<>(getModelGroup(), AbstractBee.BEE_ROLE, "data");
		addProbe(beeProbe);
	}

	public void render() {
		if (!artMode) {
			getGraphics().setFill(javafx.scene.paint.Color.BLACK);
			getGraphics().fillRect(0, 0, env.getWidth(), env.getHeight());
		}
		Color lastColor = null;
		List<Agent> currentAgentsList = beeProbe.getAgents();
		for (Agent arg0 : currentAgentsList) {
			BeeData b = beeProbe.getPropertyValue(arg0);
			Color c = b.getBeeColor();
			if (c != lastColor) {
				lastColor = c;
				getGraphics().setStroke(lastColor);
			}
			Point p = b.getCurrentPosition();
			if (trailMode) {
				Point p1 = b.getPreviousPosition();
				getGraphics().strokeLine(p1.x, p1.y, p.x, p.y);
			} else {
				getGraphics().strokeLine(p.x, p.y, p.x, p.y);
			}
		}
	}

}