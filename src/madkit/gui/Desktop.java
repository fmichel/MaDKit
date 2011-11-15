package madkit.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;

import javax.swing.JDesktopPane;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JMenuBar;
import javax.swing.JToolBar;

import madkit.gui.menus.MadkitMenu;
import madkit.gui.toolbars.MadkitToolBar;

final class Desktop extends JFrame {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -6051495018095228564L;
	final private JDesktopPane desktopPane;

	Desktop(GUIManagerAgent guiManager){
		super("MadKit "+guiManager.getMadkitProperty("madkit.version")+" Desktop running on kernel "+guiManager.getKernelAddress());
//		setLocationRelativeTo(null);
		buildMenuAndToolbar(guiManager);
		desktopPane = new JDesktopPane();
		desktopPane.setBackground(Color.BLACK);
		desktopPane.setPreferredSize(new Dimension(800,600));
		setPreferredSize(new Dimension(800,600));
//		setSize(800,600);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		add(desktopPane);
		pack();
		setExtendedState(JFrame.MAXIMIZED_BOTH);
		setVisible(true);
	}
	
	@Override
	public Dimension getSize() {
		return desktopPane.getSize();
	}
	
	void addInternalFrame(JInternalFrame jf){
		desktopPane.add(jf);
	}

	private void buildMenuAndToolbar(GUIManagerAgent guiManager) {
		JMenuBar menuBar = new JMenuBar();
		menuBar.add(new MadkitMenu(guiManager));
		menuBar.add(GUIToolkit.createAgentsMenu(guiManager));
		menuBar.add(GUIToolkit.createDemosMenu(guiManager));
		JToolBar tb = new MadkitToolBar(guiManager);
		setJMenuBar(menuBar);
		tb.setRollover(true);
		tb.setFloatable(false);
		this.add(tb,BorderLayout.PAGE_START);
		validate();
	}
}
