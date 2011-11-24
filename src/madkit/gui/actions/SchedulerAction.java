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
package madkit.gui.actions;

import static java.awt.event.KeyEvent.VK_F;
import static java.awt.event.KeyEvent.VK_G;
import static java.awt.event.KeyEvent.VK_Q;
import static java.awt.event.KeyEvent.VK_S;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JSlider;

import madkit.kernel.ActionInfo;
import madkit.kernel.MKAbstractAction;
import madkit.kernel.Scheduler;
import madkit.kernel.Scheduler.State;
import madkit.messages.ObjectMessage;
import madkit.messages.SchedulingMessage;

/**
 * @author Fabien Michel
 * @since MadKit 5.0.0.9
 * @version 0.9
 * 
 */
@SuppressWarnings("serial")
public enum SchedulerAction {

	RUN(VK_Q),
	STEP(VK_S),
	SPEED_UP(VK_F),
	SPEED_DOWN(VK_G);
	
	private ActionInfo actionInfo;
	final private int keyEvent;

	@Override
	public String toString() {
		return MKAbstractAction.enumToMethodName(this);
	}

	private SchedulerAction(int keyEvent){
		this.keyEvent = keyEvent;
	}
	
	/**
	 * @return the actionInfo
	 */
	public ActionInfo getActionInfo() {
		if(actionInfo == null)
			actionInfo = new ActionInfo(this,keyEvent);
		return actionInfo;
	}

	public Action getActionFor(final Scheduler agent, final Object... info){
		return new MKAbstractAction(getActionInfo()){
			@Override
			public void actionPerformed(ActionEvent e) {//TODO I could do the check validity here for logging purpose
				agent.receiveMessage(new SchedulingMessage(SchedulerAction.this,info));//TODO work with AA but this is probably worthless	
			}
	};
	}
	
	public Action getAction(final Scheduler agent){
		switch (this) {
		case RUN:
			return new MKAbstractAction(getActionInfo()) {
				public void actionPerformed(ActionEvent e) {
					agent.receiveMessage(new ObjectMessage<Scheduler.State>(State.RUNNING));
				}
			};
		case STEP:
			return new MKAbstractAction(getActionInfo()) {
				public void actionPerformed(ActionEvent e) {
					agent.receiveMessage(new ObjectMessage<Scheduler.State>(State.STEP));
				}
			};
		case SPEED_DOWN:
		case SPEED_UP:
			return new MKAbstractAction(getActionInfo()) {
				public void actionPerformed(ActionEvent e) {
					JSlider s = ((Scheduler) agent).getSpeedSlider();
					s.setValue(s.getValue() + (SchedulerAction.this == SPEED_UP ? -50 : 50));
				}
			};
		default:
			throw new AssertionError(this);
		}
	}
}