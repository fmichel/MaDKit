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
package madkit.simulation.viewer;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import madkit.gui.UIProperty;
import madkit.simulation.Viewer;
import madkit.simulation.environment.Environment2D;

/**
 * A viewer that displays a 2D environment. It uses a {@link CanvasDrawerGUI} to draw the
 * environment on a canvas.
 * 
 */
public abstract class Viewer2D extends Viewer {

	private GraphicsContext graphics;

	@UIProperty(category = "Rendering", displayName = "Paint over")
	private boolean paintOver = false;

	@UIProperty(category = "Rendering", displayName = "Background")
	private Color background = Color.BLACK;

	/**
	 * On activation.
	 */
	@Override
	protected void onActivation() {
		super.onActivation();
		CanvasDrawerGUI gui = new CanvasDrawerGUI(this);
		Environment2D env = getEnvironment();
		gui.setCanvasSize(env.getWidth(), env.getHeight());
		graphics = gui.getGraphics();
		setGUI(gui);
	}

	/**
	 * Gets the graphics context to draw on the canvas.
	 * 
	 * @return the graphics context to draw on the canvas.
	 */
	protected GraphicsContext getGraphics() {
		return graphics;
	}

	/**
	 * Redefines so that the paint over mode is taken into account.
	 */
	@Override
	public void render() {
		if (!paintOver) {
			getGUI().clearCanvas();
		}
	}

	/**
	 * Redefines to benefit from automatic casting.
	 */
	@SuppressWarnings("unchecked")
	@Override
	public CanvasDrawerGUI getGUI() {
		return super.getGUI();
	}

	/**
	 * Sets the paint over mode. When paint over mode is enabled, the canvas is not cleared
	 * before rendering.
	 * 
	 * @param paintOver the paintOver to set
	 */
	public void setPaintOver(boolean paintOver) {
		this.paintOver = paintOver;
	}

	/**
	 * Returns the paint over mode.
	 * 
	 * @return <code>true</code> if the paint over mode is enabled
	 */
	public boolean isPaintOver() {
		return paintOver;
	}

	/**
	 * @return the background
	 */
	public Color getBackground() {
		return background;
	}

	/**
	 * Sets the background color of the canvas.
	 * 
	 * @param background the background to set
	 */
	public void setBackground(Color background) {
		this.background = background;
		if (getGUI() != null) {
			getGUI().setBackground(background);
		}
	}

}
