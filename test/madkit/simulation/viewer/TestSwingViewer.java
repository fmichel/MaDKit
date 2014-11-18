package madkit.simulation.viewer;

import java.awt.Graphics;


public class TestSwingViewer extends SwingViewer{
	
	
	public TestSwingViewer() {
		setRenderingInterval(-1);
	}

	@Override
	protected void render(Graphics g) {
	}
	
	@Override
	protected void activate() {
		super.activate();
	}
	
	public static void main(String[] args) {
		executeThisAgent();
	}

}
