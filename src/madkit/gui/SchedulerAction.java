/*
 * Copyright 1997-2011 Fabien Michel, Olivier Gutknecht, Jacques Ferber
 * 
 * This file is part of MadKit.
 * 
 * MadKit is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 * 
 * MadKit is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with MadKit. If not, see <http://www.gnu.org/licenses/>.
 */
package madkit.gui;

import static java.awt.event.KeyEvent.VK_E;
import static java.awt.event.KeyEvent.VK_F;
import static java.awt.event.KeyEvent.VK_G;
import static java.awt.event.KeyEvent.VK_H;
import static java.awt.event.KeyEvent.VK_I;
import static java.awt.event.KeyEvent.VK_J;
import static java.awt.event.KeyEvent.VK_K;
import static java.awt.event.KeyEvent.VK_N;
import static java.awt.event.KeyEvent.VK_O;
import static java.awt.event.KeyEvent.VK_Q;
import static java.awt.event.KeyEvent.VK_R;
import static java.awt.event.KeyEvent.VK_S;
import static java.awt.event.KeyEvent.VK_T;
import static java.awt.event.KeyEvent.VK_U;
import static java.awt.event.KeyEvent.VK_W;
import static java.awt.event.KeyEvent.VK_Y;
import static java.awt.event.KeyEvent.VK_Z;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JSlider;
import javax.swing.KeyStroke;
import javax.swing.filechooser.FileNameExtensionFilter;

import madkit.kernel.AbstractAgent;
import madkit.kernel.KernelAddress;
import madkit.kernel.Madkit;
import madkit.kernel.Scheduler;
import madkit.kernel.Scheduler.State;
import madkit.messages.ObjectMessage;

/**
 * @author Fabien Michel
 * @since MadKit 5.0.0.9
 * @version 0.9
 * 
 */
public enum SchedulerAction implements MK_Action{

	SCHEDULER_RUN(new ImageIcon(SchedulerAction.class.getResource("images/scheduling/run.png")),VK_Q),
	SCHEDULER_STEP(new ImageIcon(SchedulerAction.class.getResource("images/scheduling/step.png")),VK_S),
	SCHEDULER_SPEEDUP(new ImageIcon(SchedulerAction.class.getResource("images/scheduling/speedUp.png")),VK_F),
	SCHEDULER_SPEEDDOWN(new ImageIcon(SchedulerAction.class.getResource("images/scheduling/speedDown.png")),VK_G);
	
	final private ImageIcon imageIcon;
	
	public ImageIcon getImageIcon() {
		return imageIcon;
	}

	public int getKeyEvent() {
		return keyEvent;
	}

	final private int keyEvent;
	
	private SchedulerAction(ImageIcon ii, int keyEvent){
		imageIcon = ii;
		this.keyEvent = keyEvent;
	}
	
	public Action getAction(final Scheduler agent){
		switch (this) {
		case SCHEDULER_RUN:
			return AgentAction.initAction(this, new AbstractAction("run") {
				public void actionPerformed(ActionEvent e) {
					agent.receiveMessage(new ObjectMessage<Scheduler.State>(State.RUNNING));
				}
			});
		case SCHEDULER_STEP:
			return AgentAction.initAction(this,new AbstractAction("run") {
				public void actionPerformed(ActionEvent e) {
					agent.receiveMessage(new ObjectMessage<Scheduler.State>(State.STEP));
				}
			});
		case SCHEDULER_SPEEDDOWN:
		case SCHEDULER_SPEEDUP:
			return AgentAction.initAction(this,new AbstractAction() {
				public void actionPerformed(ActionEvent e) {
					JSlider s = ((Scheduler) agent).getSpeedSlider();
					s.setValue(s.getValue() + (SchedulerAction.this == SCHEDULER_SPEEDUP ? -50 : 50));
				}
			});
		default:
			throw new AssertionError(this);
		}
	}
}