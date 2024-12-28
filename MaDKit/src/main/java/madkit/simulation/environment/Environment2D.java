
package madkit.simulation.environment;

import madkit.simulation.Environment;

/**
 * A 2D environment for simulations.
 */
public class Environment2D extends Environment {

	private int width = 100;
	private int height = 100;

	/**
	 * Constructs an Environment2D with the specified width and height.
	 *
	 * @param width  the width of the environment
	 * @param height the height of the environment
	 */
	public Environment2D(int width, int height) {
		super();
		this.width = width;
		this.height = height;
	}

	/**
	 * Constructs an Environment2D with default dimensions (600x600).
	 */
	public Environment2D() {
		this(600, 600);
	}

	/**
	 * Sets the width of the environment.
	 *
	 * @param width the width to set
	 */
	public void setWidth(int width) {
		this.width = width;
	}

	/**
	 * Returns the width of the environment.
	 *
	 * @return the width of the environment
	 */
	public int getWidth() {
		return width;
	}

	/**
	 * Sets the height of the environment.
	 *
	 * @param height the height to set
	 */
	public void setHeight(int height) {
		this.height = height;
	}

	/**
	 * Returns the height of the environment.
	 *
	 * @return the height of the environment
	 */
	public int getHeight() {
		return height;
	}
}
