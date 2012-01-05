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
package madkit.action;

import static java.awt.event.KeyEvent.VK_DOLLAR;
import static java.awt.event.KeyEvent.VK_LEFT;
import static java.awt.event.KeyEvent.VK_P;
import static java.awt.event.KeyEvent.VK_RIGHT;
import static java.awt.event.KeyEvent.VK_SPACE;

import java.awt.event.ActionEvent;
import java.util.ResourceBundle;

import javax.swing.Action;
import javax.swing.JSlider;

import madkit.i18n.I18nUtilities;
import madkit.kernel.Scheduler;
import madkit.message.SchedulingMessage;

/**
 * Enum representing operations which 
 * could be done by a {@link Scheduler} agent.
 * It could be used by an agent to interact with the scheduler
 * by creating {@link Action} using {@link #getActionFor(Scheduler, Object...)}.
 * 
 * @author Fabien Michel
 * @since MadKit 5.0.0.14
 * @version 0.9
 * 
 */
public enum SchedulingAction {

	RUN(VK_P),
	STEP(VK_SPACE),
	SPEED_UP(VK_RIGHT),
	SPEED_DOWN(VK_LEFT),
	SHUTDOWN(VK_DOLLAR);
	
	final static private ResourceBundle messages = I18nUtilities.getResourceBundle(SchedulingAction.class.getSimpleName());

	private ActionInfo actionInfo;
	final private int keyEvent;

	private SchedulingAction(int keyEvent){
		this.keyEvent = keyEvent;
	}
	
	/**
	 * @return the actionInfo
	 */
	public ActionInfo getActionInfo() {
		if(actionInfo == null)
			actionInfo = new ActionInfo(this,keyEvent, messages);
		return actionInfo;
	}

	/**
	 * Builds an action that will make the corresponding 
	 * scheduler do the related operation if possible.
	 * 
	 * @param theScheduler the scheduler on which the
	 * action will be triggered if possible
	 * @param parameters the info 
	 * @return the corresponding action 
	 */
	public Action getActionFor(final Scheduler theScheduler, final Object... parameters){
		switch (this) {
		case RUN:
		case STEP:
		case SHUTDOWN:
		return new MKAbstractAction(getActionInfo()){
			private static final long serialVersionUID = 5434867603425806658L;

			@Override
			public void actionPerformed(ActionEvent e) {//TODO I could do the check validity here for logging purpose
				theScheduler.receiveMessage(new SchedulingMessage(SchedulingAction.this,parameters));//TODO work with AA but this is probably worthless	
			}
		};
		case SPEED_DOWN:
		case SPEED_UP:
			return new MKAbstractAction(getActionInfo()) {
				private static final long serialVersionUID = -4856695289354657928L;

				public void actionPerformed(ActionEvent e) {
					JSlider s = theScheduler.getSpeedSlider();
					s.setValue(s.getValue() + (SchedulingAction.this == SPEED_UP ? -50 : 50));
				}
			};
		default:
			throw new AssertionError(this);
		}
	}
	
//	public Action getAction(final Scheduler agent){
//		switch (this) {
//		case RUN:
//			return new MKAbstractAction(getActionInfo()) {
//				public void actionPerformed(ActionEvent e) {
//					agent.receiveMessage(new ObjectMessage<Scheduler.State>(State.RUNNING));
//				}
//			};
//		case STEP:
//			return new MKAbstractAction(getActionInfo()) {
//				public void actionPerformed(ActionEvent e) {
//					agent.receiveMessage(new ObjectMessage<Scheduler.State>(State.STEP));
//				}
//			};
//		case SPEED_DOWN:
//		case SPEED_UP:
//			return new MKAbstractAction(getActionInfo()) {
//				public void actionPerformed(ActionEvent e) {
//					JSlider s = agent.getSpeedSlider();
//					s.setValue(s.getValue() + (SchedulingAction.this == SPEED_UP ? -50 : 50));
//				}
//			};
//		default:
//			throw new AssertionError(this);
//		}
//	}
}
