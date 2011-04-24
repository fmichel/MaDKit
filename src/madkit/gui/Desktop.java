package madkit.gui;

import java.awt.BorderLayout;
import java.awt.Color;

import javax.swing.JDesktopPane;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JMenuBar;
import javax.swing.JToolBar;

class Desktop extends JFrame {
	
	final private GUIManagerAgent guiManagerAgent;
	private JDesktopPane desktopPane;

	Desktop(GUIManagerAgent guiManager){
		super("MadKit "+guiManager.getMadkitProperty("madkit.version")+" "+guiManager.getKernelAddress());
//		setLocationRelativeTo(null);
		guiManagerAgent = guiManager;
		setSize(800,600);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		buildMenuAndToolbar();
		desktopPane = new JDesktopPane();
		desktopPane.setBackground(Color.BLACK);
		add(desktopPane);
		setVisible(true);
		setExtendedState(JFrame.MAXIMIZED_BOTH);
	}
	
	void addInternalFrame(JInternalFrame jf){
		desktopPane.add(jf);
	}

	private void buildMenuAndToolbar() {
		JMenuBar menuBar = new JMenuBar();
		menuBar.add(new MadkitMenu(guiManagerAgent));
		menuBar.add(MKToolkit.createAgentsMenu(guiManagerAgent));
		menuBar.add(MKToolkit.createDemosMenu(guiManagerAgent));
		JToolBar tb = new MadkitToolBar(guiManagerAgent);
		setJMenuBar(menuBar);
		tb.setRollover(true);
		tb.setFloatable(false);
		this.add(tb,BorderLayout.PAGE_START);
		validate();
	}
}
