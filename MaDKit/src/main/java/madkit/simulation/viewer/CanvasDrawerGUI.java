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

import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import madkit.simulation.Viewer;

/**
 * CanvasDrawerGUI extends {@link ViewerDefaultGUI} so that a canvas is used for rendering
 * the simulation.
 */
public class CanvasDrawerGUI extends ViewerDefaultGUI {

	/** The canvas. */
	private Canvas canvas;

	/** The background. */
	private Color background = Color.BLACK;

	/**
	 * Creates a new CanvasDrawerGUI with the specified viewer.
	 * 
	 * @param viewer the viewer associated with this GUI.
	 */
	public CanvasDrawerGUI(Viewer viewer) {
		super(viewer);
	}

	/**
	 * Creates the central node of the GUI. It returns a canvas.
	 */
	@Override
	protected Node createCenterNode() {
		canvas = new Canvas(600, 600);
		canvas.setOnScroll(e -> {
			double deltaY = e.getDeltaY();
			if (deltaY != 0) {
				double factor = deltaY > 0 ? 0.1 : -0.1;
				if (canvas.getScaleX() + factor > 0.2) {
					canvas.setScaleX(canvas.getScaleX() + factor);
					canvas.setScaleY(canvas.getScaleY() + factor);
				}
			}
		});
		return canvas;
	}

	/**
	 * Gets the graphics context of the canvas.
	 * 
	 * @return the graphics context of the canvas.
	 */
	public GraphicsContext getGraphics() {
		return canvas.getGraphicsContext2D();
	}

	/**
	 * Gets the canvas
	 * 
	 * @return the canvas
	 */
	public Canvas getCanvas() {
		return canvas;
	}

	/**
	 * Clears the canvas by filling it with the background color.
	 */
	public void clearCanvas() {
		getGraphics().setFill(getBackground());
		getGraphics().fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
	}

	/**
	 * Sets the size of the canvas.
	 * 
	 * @param width  the width of the canvas.
	 * @param height the height of the canvas.
	 */
	public void setCanvasSize(int width, int height) {
		canvas.setWidth(width);
		canvas.setHeight(height);
	}

	/**
	 * Sets the background color of the canvas.
	 * 
	 * @return the background color of the canvas.
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
	}

}
