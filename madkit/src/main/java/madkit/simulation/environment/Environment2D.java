package madkit.simulation.environment;

import madkit.simulation.Environment;

public class Environment2D extends Environment {

	private int width = 600;
	private int height = 600;

	/**
	 * @param width
	 * @param height
	 */
	public Environment2D(int width, int height) {
		super();
		this.width = width;
		this.height = height;
	}

	/**
	 * 
	 */
	public Environment2D() {
		this(600, 600);
	}

	/**
	 * @param width the width to set
	 */
	public void setWidth(int width) {
		this.width = width;
	}

	/**
	 * @return the width
	 */
	public int getWidth() {
		return width;
	}

	/**
	 * @param height the height to set
	 */
	public void setHeight(int height) {
		this.height = height;
	}

	/**
	 * @return the height
	 */
	public int getHeight() {
		return height;
	}

}
