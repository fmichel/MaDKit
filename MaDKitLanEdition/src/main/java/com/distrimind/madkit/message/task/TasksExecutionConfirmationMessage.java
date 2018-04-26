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
package com.distrimind.madkit.message.task;

import com.distrimind.madkit.kernel.ConversationID;
import com.distrimind.madkit.kernel.Task;
import com.distrimind.madkit.kernel.TaskID;

/**
 * This message mean that the tasks referenced with the current
 * {@link ConversationID} has been executed.
 * 
 * @author Jason Mahdjoub
 * @version 1.0
 * @since MadKitLanEdition 1.0
 * @see #getTask()
 * @see #getDurationOfExecution()
 * @see #getDateOfStartingExecution()
 * @see #getDateOfEndingExecution()
 *
 */
public class TasksExecutionConfirmationMessage extends TaskMessage {

	private final long date_begin_execution;
	private final long date_end_execution;

	/**
	 * @param taskID
	 *            the task identifier
	 * @param _task
	 *            the concerned task
	 * @param _date_begin_execution_utc
	 *            the UTC time in milliseconds of the starting task execution
	 * @param _date_end_execution_utc
	 *            the UTC time in milliseconds of the ending task execution
	 */
	public TasksExecutionConfirmationMessage(TaskID taskID, Task<?> _task, long _date_begin_execution_utc,
			long _date_end_execution_utc) {
		super(taskID, _task);
		date_begin_execution = _date_begin_execution_utc;
		date_end_execution = _date_end_execution_utc;
	}

	/**
	 * 
	 * @return the duration in milliseconds of the task execution
	 */
	public long getDurationOfExecution() {
		return date_end_execution - date_begin_execution;
	}

	/**
	 * 
	 * @return the UTC time in milliseconds of the starting task execution
	 */
	public long getDateOfStartingExecution() {
		return date_begin_execution;
	}

	/**
	 * 
	 * @return the UTC time in milliseconds of the ending task execution
	 */
	public long getDateOfEndingExecution() {
		return date_end_execution;
	}

}
