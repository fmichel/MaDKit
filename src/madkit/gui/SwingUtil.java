/*
 * Copyright 1997-2013 Fabien Michel, Olivier Gutknecht, Jacques Ferber
 * 
 * This file is part of MaDKit.
 * 
 * MaDKit is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * MaDKit is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with MaDKit. If not, see <http://www.gnu.org/licenses/>.
 */
package madkit.gui;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.BoundedRangeModel;
import javax.swing.BoxLayout;
import javax.swing.DefaultBoundedRangeModel;
import javax.swing.ImageIcon;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JToggleButton;
import javax.swing.border.TitledBorder;

import madkit.action.ActionInfo;
import madkit.action.GUIManagerAction;
import madkit.action.GlobalAction;
import madkit.action.KernelAction;
import madkit.kernel.AbstractAgent;


/**
 * This class provides some utilities for building
 * swing components.
 * 
 * @author Fabien Michel
 * @since MaDKit 5.0.0.16
 * @version 0.9
 * 
 */
final public class SwingUtil {
	
	final public static ImageIcon MADKIT_LOGO = new ImageIcon(SwingUtil.class.getResource("images/madkit_logo.png"));
	final public static ImageIcon MADKIT_LOGO_SMALL = new ImageIcon(MADKIT_LOGO.getImage().getScaledInstance(14, 14, java.awt.Image.SCALE_SMOOTH));

	/**
	 * Creates a labeled panel containing a slider with default size.
	 * 
	 * @param slider
	 * @param label
	 * @return a panel for the slider
	 */
	public static JPanel createSliderPanel(final JSlider slider, String label) {
		return createSliderPanel(slider, label, 170);
	}

	/**
	 * Creates a labeled panel containing a slider and considering
	 * a particular width
	 * 
	 * @param slider
	 * @param label
	 * @return a panel for the slider
	 */
	public static JPanel createSliderPanel(final JSlider slider, String label, int width) {
		final JPanel p = new JPanel();
		p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
		p.setBorder(new TitledBorder(label));
		p.setPreferredSize(new Dimension(width, 60));
		p.add(slider);
		return p;
	}

	/**
	 * Creates a labeled panel containing a slider built
	 * using an existing {@link DefaultBoundedRangeModel}
	 * 
	 * @param model
	 * @param label
	 * @return a panel for this model
	 */
	public static JPanel createSliderPanel(final BoundedRangeModel model, String label) {
		return createSliderPanel(createJSlider(model), label);
	}

	/**
	 * Creates a JSlider built
	 * using a {@link DefaultBoundedRangeModel} and 
	 * containing a {@link MouseWheelListener} 
	 * and some usual default settings
	 * 
	 * @param model
	 * @return the corresponding {@link JSlider}
	 */
	public static JSlider createJSlider(final BoundedRangeModel model) {
		final JSlider slider = new JSlider(model);
		slider.addMouseWheelListener(new MouseWheelListener() {
			@Override
			public void mouseWheelMoved(MouseWheelEvent e) {
				slider.setValue(-e.getWheelRotation() * model.getMaximum() / 100 + model.getValue());
			}
		});
		slider.setPaintTicks(true);
		slider.setPaintLabels(true);
//		slider.setMajorTickSpacing(model.getMaximum()/2);
//		slider.setMinorTickSpacing(model.getMaximum()/10);
//		slider.setSnapToTicks(true);
		return slider;
	}


	/**
	 * Adds to a menu or toolbar the following actions:
	 * <ul>
	 * <li> {@link KernelAction#EXIT}
	 * <li> {@link KernelAction#COPY}
	 * <li> {@link KernelAction#RESTART}
	 * <li> {@link KernelAction#LAUNCH_NETWORK}
	 * <li> {@link KernelAction#STOP_NETWORK}
	 * <li> {@link GUIManagerAction#CONNECT_TO_IP}
	 * <li> {@link GlobalAction#JCONSOLE}
	 * <li> {@link KernelAction#CONSOLE}
	 * <li> {@link GlobalAction#DEBUG}
	 * <li> {@link GlobalAction#LOAD_LOCAL_DEMOS}
	 * <li> {@link GlobalAction#LOAD_JAR_FILE}
	 * <li> {@link GUIManagerAction#ICONIFY_ALL}
	 * <li> {@link GUIManagerAction#DEICONIFY_ALL}
	 * <li> {@link GUIManagerAction#KILL_AGENTS}
	 * </ul>
	 * 
	 * @param agent the agent for which this menu
	 * will be built.
	 */
	public static void addMaDKitActionsTo(Container menuOrToolBar, AbstractAgent agent){
		try {//this bypasses class incompatibility
			final Class<? extends Container> componentClass = menuOrToolBar.getClass();
			final Method add = componentClass.getMethod("add", Action.class);
			final Method addSeparator = componentClass.getMethod("addSeparator");
			
			add.invoke(menuOrToolBar, KernelAction.EXIT.getActionFor(agent));
			addSeparator.invoke(menuOrToolBar);
			add.invoke(menuOrToolBar, KernelAction.COPY.getActionFor(agent));
			add.invoke(menuOrToolBar, KernelAction.RESTART.getActionFor(agent));
			addSeparator.invoke(menuOrToolBar);
			add.invoke(menuOrToolBar, KernelAction.LAUNCH_NETWORK.getActionFor(agent));
			add.invoke(menuOrToolBar, KernelAction.STOP_NETWORK.getActionFor(agent));
			add.invoke(menuOrToolBar, GUIManagerAction.CONNECT_TO_IP.getActionFor(agent));
			addSeparator.invoke(menuOrToolBar);
			if (! (GlobalAction.jconsolePath == null || ActionInfo.javawsIsOn)) {
				add.invoke(menuOrToolBar, GlobalAction.JCONSOLE);
			}
			add.invoke(menuOrToolBar, KernelAction.CONSOLE.getActionFor(agent));
			addBooleanActionTo(menuOrToolBar,GlobalAction.DEBUG);
			add.invoke(menuOrToolBar, GlobalAction.LOG_FILES);
			addSeparator.invoke(menuOrToolBar);
			add.invoke(menuOrToolBar, GlobalAction.LOAD_LOCAL_DEMOS);
			add.invoke(menuOrToolBar, GlobalAction.LOAD_JAR_FILE);
//			add.invoke(menuOrToolBar, GUIManagerAction.LOAD_JAR_FILE.getActionFor(agent));
			addSeparator.invoke(menuOrToolBar);
			add.invoke(menuOrToolBar, GUIManagerAction.ICONIFY_ALL.getActionFor(agent));
			add.invoke(menuOrToolBar, GUIManagerAction.DEICONIFY_ALL.getActionFor(agent));
			addSeparator.invoke(menuOrToolBar);
			add.invoke(menuOrToolBar, GUIManagerAction.KILL_AGENTS.getActionFor(agent));
		} catch (IllegalArgumentException | NoSuchMethodException | SecurityException | IllegalAccessException | InvocationTargetException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Creates a {@link JCheckBoxMenuItem} for a menu or {@link JToggleButton}
	 * for a tool bar
	 * 
	 * @param menuOrToolBar
	 * @param action 
	 * @throws NoSuchMethodException
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 */
	public static void addBooleanActionTo(Container menuOrToolBar, Action action){
		Method addButton;
		try {
			addButton = Container.class.getMethod("add", Component.class);
			if(menuOrToolBar instanceof JMenu){
				addButton.invoke(menuOrToolBar, new JCheckBoxMenuItem(action));
			}
			else{
				final JToggleButton jToggleButton = new JToggleButton(action);
				jToggleButton.setText(null);
				addButton.invoke(menuOrToolBar, jToggleButton);
			}
		} catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Resizes the icons of all the abstract buttons which are contained in
	 * a container.
	 * 
	 * @param container a container containing abstract buttons
	 * @param size the size which should be used for the icons
	 */
	public static void scaleAllAbstractButtonIcons(Container container, int size){
		for (final Component c : container.getComponents()) {
			if (c instanceof AbstractButton) {
				final ImageIcon i = (ImageIcon) ((AbstractButton) c).getIcon();
				if (i != null) {
					i.setImage(i.getImage().getScaledInstance(size, size, Image.SCALE_SMOOTH));
				}
			}
		}
	}


}
