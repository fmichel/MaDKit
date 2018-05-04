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


import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;




/**
 * Represents replies for one conversation
 * 
 * @author Jason Mahdjoub
 * @version 1.0
 * @since MadkitLanEdition 1.0
 */
public class Replies extends Message {


	private Message originalMessage;
	// private final ConversationID originalConversationID;
	private AtomicInteger numberOfReplies = new AtomicInteger(0);
	private List<Message> replies;
	private AtomicBoolean allMessagesSent = new AtomicBoolean(false);

	
	
	
	
	
	Replies(Message originalMessage) {
		if (originalMessage == null)
			throw new NullPointerException("originalMessage");
		this.originalMessage = originalMessage;
		super.setIDFrom(originalMessage);
		// this.originalConversationID=originalMessage.getConversationID();
	
		replies = new ArrayList<>();
	}

	Replies(Message originalMessage, List<Message> replies) {
		if (originalMessage == null)
			throw new NullPointerException("originalMessage");
		this.originalMessage = originalMessage;
		

		super.setIDFrom(originalMessage);
		// this.originalConversationID=originalMessage.getConversationID();

		if (replies == null)
			this.replies = new ArrayList<>();
		else {
			this.replies = new ArrayList<>(replies.size());
			for (Message m : replies) {
				if (m.getClass() != EmptyMessage.class)
			

					this.replies.add(m);
			}
		}
	}

	void setAllMessagesSent(int numberOfReplies) {
		if (numberOfReplies <= 0)
			throw new IllegalArgumentException();
		this.numberOfReplies.addAndGet(numberOfReplies);
		allMessagesSent.set(true);
	}

	@Override
	public boolean equals(Object o) {
		if (o == null)
			return false;
		if (o == this)
			return true;
		if (o.getClass() == Replies.class) {
			return getConversationID().equals(((Replies) o).getConversationID());
		}
		return false;
	}

	@Override
	public int hashCode() {
		return getConversationID().hashCode();
	}

	/**
	 * 
	 * @return the original message initiating the conversation
	 */
	public Message getOriginalMessage() {
		return originalMessage;
	}

	/**
	 * 
	 * @return the replies
	 */
	public List<Message> getReplies() {
		return replies;
	}

	/**
	 * 
	 * @param m
	 * @return true if all replies has been got
	 */
	boolean addReply(Message m) {
		synchronized (this) {
			if (m != null && m.getClass() != EmptyMessage.class)
			{
			

				replies.add(m);
			}

			return numberOfReplies.decrementAndGet() <= 0 && this.allMessagesSent.get();
		}
	}

	public boolean isConcernedBy(Message m) {
		return m.getConversationID().equals(getConversationID());
	}

	boolean areAllRepliesSent() {
		return numberOfReplies.get() <= 0 && allMessagesSent.get();
	}

	@Override
	public String toString() {
		return "Replies[originalConversationID=" + getConversationID() + ", repliesNumber=" + this.getReplies().size()
				+ "]";
	}
}
