/*
 * MadKitLanEdition (created by Jason MAHDJOUB (jason.mahdjoub@distri-mind.fr)) Copyright (c)
 * 2015 is a fork of MadKit and MadKitGroupExtension. 
 * 
 * Copyright or © or Copr. Jason Mahdjoub, Fabien Michel, Olivier Gutknecht, Jacques Ferber (1997)
 * 
 * jason.mahdjoub@distri-mind.fr
 * fmichel@lirmm.fr
 * olg@no-distance.net
 * ferber@lirmm.fr
 * 
 * This software is a computer program whose purpose is to
 * provide a lightweight Java library for designing and simulating Multi-Agent Systems (MAS).
 * This software is governed by the CeCILL-C license under French law and
 * abiding by the rules of distribution of free software.  You can  use,
 * modify and/ or redistribute the software under the terms of the CeCILL-C
 * license as circulated by CEA, CNRS and INRIA at the following URL
 * "http://www.cecill.info".
 * As a counterpart to the access to the source code and  rights to copy,
 * modify and redistribute granted by the license, users are provided only
 * with a limited warranty  and the software's author,  the holder of the
 * economic rights,  and the successive licensors  have only  limited
 * liability.
 * 
 * In this respect, the user's attention is drawn to the risks associated
 * with loading,  using,  modifying and/or developing or reproducing the
 * software by the user in light of its specific status of free software,
 * that may mean  that it is complicated to manipulate,  and  that  also
 * therefore means  that it is reserved for developers  and  experienced
 * professionals having in-depth computer knowledge. Users are therefore
 * encouraged to load and test the software's suitability as regards their
 * requirements in conditions enabling the security of their systems and/or
 * data to be ensured and,  more generally, to use and operate it in the
 * same conditions as regards security.
 * The fact that you are presently reading this means that you have had
 * knowledge of the CeCILL-C license and that you accept its terms.
 */
package com.distrimind.madkit.kernel;

import java.util.concurrent.Callable;

import com.distrimind.madkit.agr.LocalCommunity;

/**
 * This class represent a task to execute according a given time. This task can
 * also be repetitive according a given duration between each execution.
 * 
 * To launch a programmed task, use the function
 * {@link AbstractAgent#scheduleTask(Task, boolean)}
 * 
 * 
 * If you don't specify the TaskAgent name, than the default task agent is used.
 * This last is automically launched with the first added task.
 * 
 * @author Jason Mahdjoub
 * @since MadKitLanEdition 1.0
 * @version 1.0
 * 
 * @see AbstractAgent#scheduleTask(Task)
 * @see AbstractAgent#scheduleTask(Task, boolean)
 * @see AbstractAgent#cancelTask(TaskID, boolean)
 * 
 *
 *
 *
 */
public final class Task<V> implements Cloneable {

	/**
	 * This role is given to internal MadKit agents that manage task execution
	 */
	public static final String TASK_MANAGER_ROLE = "~~TASK_MANAGER";

	/**
	 * This role is automatically given to agents that launch tasks
	 */
	public static final String TASK_ASKER_ROLE = "~~TASK_ASKER";

	/**
	 * The name of the default task manager agent
	 */
	public static final String DEFAULT_TASK_EXECUTOR_NAME = "~~MKLE_DEFAULT_TASK_AGENT";

	public static final Group TASK_AGENTS = LocalCommunity.Groups.SYSTEM_ROOT.getSubGroup(false, new Gatekeeper() {

		@Override
		public boolean allowAgentToTakeRole(Group _group, String _roleName,
				final Class<? extends AbstractAgent> requesterClass, AgentNetworkID _agentNetworkID,
				Object _memberCard) {
			if (_memberCard != null && _memberCard instanceof MemberCard && _memberCard == memberCard)
				return true;
			else
				return false;
		}

		@Override
		public boolean allowAgentToCreateSubGroup(Group _parent_group, Group _sub_group,
				final Class<? extends AbstractAgent> requesterClass, AgentNetworkID _agentNetworkID,
				Object _memberCard) {
			if (_memberCard != null && _memberCard instanceof MemberCard && _memberCard == memberCard)
				return true;
			else
				return false;
		}
	}, true, "~~Tasks Agent");

	final static MemberCard memberCard = new MemberCard();

	static class MemberCard {

	}

	private final Callable<V> callable;
	private V result = null;
	private long time;

	final long duration_between_each_repetition;

	/**
	 * Construct a task to execute at the current time
	 * 
	 * @param _callable
	 *            the runnable to execute
	 * @throws NullPointerException
	 *             if _runnable is null
	 * @see AbstractAgent#scheduleTask(Task, boolean)
	 * @see AbstractAgent#cancelTask(TaskID, boolean)
	 */
	public Task(Callable<V> _callable) {
		this(_callable, System.currentTimeMillis());
	}

	/**
	 * Construct a task to execute at the given time
	 * 
	 * @param _callable
	 *            the runnable to execute
	 * @param _time
	 *            the moment in UTC when the TaskAgent must execute this task
	 * @throws NullPointerException
	 *             if _runnable is null
	 * @see AbstractAgent#scheduleTask(Task, boolean)
	 * @see AbstractAgent#cancelTask(TaskID, boolean)
	 */
	public Task(Callable<V> _callable, long _time) {
		this(_callable, _time, -1);
	}

	/**
	 * Construct a repetitive task to start at a given time
	 * 
	 * @param _callable
	 *            the runnable to execute
	 * @param _time
	 *            the moment in UTC when the TaskAgent must execute this task
	 * @param _duration_between_each_repetition
	 *            the duration between each execution
	 * @throws NullPointerException
	 *             if _runnable is null
	 * @see AbstractAgent#scheduleTask(Task, boolean)
	 * @see AbstractAgent#cancelTask(TaskID, boolean)
	 */
	public Task(Callable<V> _callable, long _time, long _duration_between_each_repetition) {
		if (_callable == null)
			throw new NullPointerException("_runnable");
		callable = _callable;
		time = _time;
		duration_between_each_repetition = _duration_between_each_repetition;
	}

	@Override
	public Task<V> clone() {
		return new Task<V>(callable, time, duration_between_each_repetition);
	}

	public void run() throws Exception {
		result = callable.call();
	}

	public V getResult() {
		return result;
	}

	public long getTimeOfExecution() {
		return time;
	}

	public boolean isRepetitive() {
		return duration_between_each_repetition >= 0;
	}

	public long getDurationBetweenEachRepetition() {
		return duration_between_each_repetition;
	}

	void renewTask() {
		if (isRepetitive())
			time = System.currentTimeMillis() + duration_between_each_repetition;
	}
}
