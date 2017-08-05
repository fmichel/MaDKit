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
package com.distrimind.madkit.kernel.network;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import com.distrimind.madkit.kernel.network.TransferAgent.IDTransfer;

/**
 * Represents a set of transfer statistics, considering a connection/socket, a
 * kernel address, or a big data transfer.
 * 
 * @author Jason Mahdjoub
 * @version 1.0
 * @since MadkitLanEdition 1.0
 */
public class StatsBandwidth {

	private final Map<String, RealTimeTransfertStat> bytes_uploaded_in_real_time = new HashMap<>();
	private final Map<String, RealTimeTransfertStat> bytes_downloaded_in_real_time = new HashMap<>();
	private final Map<String, TransferSpeedStat> bytes_uploaded_in_real_bytes = new HashMap<>();
	private final Map<String, TransferSpeedStat> bytes_downloaded_in_real_bytes = new HashMap<>();

	private final HashMap<Integer, StatsBandwidth> transfer_agents_bandwidth = new HashMap<>();
	private final AtomicReference<StatsBandwidth> statsForDistantKernelAddress = new AtomicReference<>();

	StatsBandwidth() {

	}

	void putTransferAgentStats(IDTransfer id, StatsBandwidth stats) {

		if (stats == null)
			throw new NullPointerException("stats");
		if (id == null)
			throw new NullPointerException("id");
		if (id.equals(TransferAgent.NullIDTransfer))
			throw new IllegalArgumentException();
		if (transfer_agents_bandwidth.containsKey(id.getID()))
			return;
		synchronized (this) {
			transfer_agents_bandwidth.put(new Integer(id.getID()), stats);
		}
	}

	StatsBandwidth removeTransferAgentStats(IDTransfer id) {
		if (id == null)
			throw new NullPointerException("id");
		synchronized (this) {
			StatsBandwidth sb = transfer_agents_bandwidth.remove(new Integer(id.getID()));
			/*
			 * if (sb==null) throw new IllegalArgumentException();
			 */
			return sb;
		}
	}

	void putStateForDistantKernelAddress(StatsBandwidth statsForDistantKernelAddress) {
		if (statsForDistantKernelAddress == null)
			throw new NullPointerException("statsForDistantKernelAddress");
		this.statsForDistantKernelAddress.set(statsForDistantKernelAddress);
	}

	/**
	 * Associate with a key, a {@link RealTimeTransfertStat} for LAN upload
	 * statistics
	 * 
	 * @param key
	 *            the key
	 * @param stat
	 *            the statistic
	 * @see RealTimeTransfertStat
	 * @see NetworkProperties#DEFAULT_TRANSFERT_STAT_IN_REAL_TIME_PER_30_SECONDS_SEGMENTS
	 * @see NetworkProperties#DEFAULT_TRANSFERT_STAT_IN_REAL_TIME_PER_5_MINUTES_SEGMENTS
	 */
	public void putBytesUploadedInRealTime(String key, RealTimeTransfertStat stat) {
		synchronized (this) {
			bytes_uploaded_in_real_time.put(key, stat);
		}
	}

	/**
	 * Remove according a key, a {@link RealTimeTransfertStat} related LAN upload
	 * statistics
	 * 
	 * @param key
	 *            the key
	 * @return the removed {@link RealTimeTransfertStat}
	 * @see RealTimeTransfertStat
	 * @see NetworkProperties#DEFAULT_TRANSFERT_STAT_IN_REAL_TIME_PER_30_SECONDS_SEGMENTS
	 * @see NetworkProperties#DEFAULT_TRANSFERT_STAT_IN_REAL_TIME_PER_5_MINUTES_SEGMENTS
	 */
	public RealTimeTransfertStat removeBytesUploadedInRealTime(String key) {
		synchronized (this) {
			return bytes_uploaded_in_real_time.remove(key);
		}
	}

	/**
	 * Gets according a key, a {@link RealTimeTransfertStat} related LAN upload
	 * statistics
	 * 
	 * @param key
	 *            the key
	 * @see RealTimeTransfertStat
	 * @see NetworkProperties#DEFAULT_TRANSFERT_STAT_IN_REAL_TIME_PER_30_SECONDS_SEGMENTS
	 * @see NetworkProperties#DEFAULT_TRANSFERT_STAT_IN_REAL_TIME_PER_5_MINUTES_SEGMENTS
	 * @return a {@link RealTimeTransfertStat} related LAN upload statistics
	 */
	public RealTimeTransfertStat getBytesUploadedInRealTime(String key) {
		synchronized (this) {
			return bytes_uploaded_in_real_time.get(key);
		}
	}

	/**
	 * Associate with a key, a {@link TransferSpeedStat} for LAN upload statistics
	 * 
	 * @param key
	 *            the key
	 * @param stat
	 *            the statistic
	 * @see TransferSpeedStat
	 * @see NetworkProperties#DEFAULT_STAT_PER_512KB_SEGMENTS
	 */
	public void putBytesUploadedInRealBytes(String key, TransferSpeedStat stat) {
		synchronized (this) {
			bytes_uploaded_in_real_bytes.put(key, stat);
		}
	}

	/**
	 * Remove according a key, a {@link TransferSpeedStat} related LAN upload
	 * statistics
	 * 
	 * @param key
	 *            the key
	 * @return the removed {@link TransferSpeedStat}
	 * @see TransferSpeedStat
	 * @see NetworkProperties#DEFAULT_STAT_PER_512KB_SEGMENTS
	 */
	public TransferSpeedStat removeBytesUploadedInRealBytes(String key) {
		synchronized (this) {
			return bytes_uploaded_in_real_bytes.remove(key);
		}
	}

	/**
	 * Gets according a key, a {@link TransferSpeedStat} related LAN upload
	 * statistics
	 * 
	 * @param key
	 *            the key
	 * @see TransferSpeedStat
	 * @see NetworkProperties#DEFAULT_STAT_PER_512KB_SEGMENTS
	 * @return a {@link TransferSpeedStat} related LAN upload statistics
	 */
	public TransferSpeedStat getBytesUploadedInRealBytes(String key) {
		synchronized (this) {
			return bytes_uploaded_in_real_bytes.get(key);
		}
	}

	/**
	 * Associate with a key, a RealTimeTransfertStat for LAN download statistics
	 * 
	 * @param key
	 *            the key
	 * @param stat
	 *            the statistic
	 * @see RealTimeTransfertStat
	 * @see NetworkProperties#DEFAULT_TRANSFERT_STAT_IN_REAL_TIME_PER_30_SECONDS_SEGMENTS
	 * @see NetworkProperties#DEFAULT_TRANSFERT_STAT_IN_REAL_TIME_PER_5_MINUTES_SEGMENTS
	 */
	public void putBytesDownloadedInRealTime(String key, RealTimeTransfertStat stat) {
		synchronized (this) {
			bytes_downloaded_in_real_time.put(key, stat);
		}
	}

	/**
	 * Remove according a key, a {@link RealTimeTransfertStat} related LAN download
	 * statistics
	 * 
	 * @param key
	 *            the key
	 * @return the removed {@link RealTimeTransfertStat}
	 * @see RealTimeTransfertStat
	 * @see NetworkProperties#DEFAULT_TRANSFERT_STAT_IN_REAL_TIME_PER_30_SECONDS_SEGMENTS
	 * @see NetworkProperties#DEFAULT_TRANSFERT_STAT_IN_REAL_TIME_PER_5_MINUTES_SEGMENTS
	 */
	public RealTimeTransfertStat removeBytesDownloadedInRealTime(String key) {
		synchronized (this) {
			return bytes_downloaded_in_real_time.remove(key);
		}
	}

	/**
	 * Gets according a key, a {@link RealTimeTransfertStat} related LAN download
	 * statistics
	 * 
	 * @param key
	 *            the key
	 * @see RealTimeTransfertStat
	 * @see NetworkProperties#DEFAULT_TRANSFERT_STAT_IN_REAL_TIME_PER_30_SECONDS_SEGMENTS
	 * @see NetworkProperties#DEFAULT_TRANSFERT_STAT_IN_REAL_TIME_PER_5_MINUTES_SEGMENTS
	 * @return a {@link RealTimeTransfertStat} related LAN upload statistics
	 */
	public RealTimeTransfertStat getBytesDownloadedInRealTime(String key) {
		synchronized (this) {
			return bytes_downloaded_in_real_time.get(key);
		}
	}

	/**
	 * Associate with a key, a {@link TransferSpeedStat} for LAN download statistics
	 * 
	 * @param key
	 *            the key
	 * @param stat
	 *            the statistic
	 * @see TransferSpeedStat
	 * @see NetworkProperties#DEFAULT_STAT_PER_512KB_SEGMENTS
	 */
	public void putBytesDownloadedInRealBytes(String key, TransferSpeedStat stat) {
		synchronized (this) {
			bytes_downloaded_in_real_bytes.put(key, stat);
		}
	}

	/**
	 * Remove according a key, a {@link TransferSpeedStat} related LAN download
	 * statistics
	 * 
	 * @param key
	 *            the key
	 * @return the removed {@link TransferSpeedStat}
	 * @see TransferSpeedStat
	 * @see NetworkProperties#DEFAULT_STAT_PER_512KB_SEGMENTS
	 */
	public TransferSpeedStat removeBytesDownloadedInRealBytes(String key) {
		synchronized (this) {
			return bytes_downloaded_in_real_bytes.remove(key);
		}
	}

	/**
	 * Gets according a key, a {@link TransferSpeedStat} related LAN download
	 * statistics
	 * 
	 * @param key
	 *            the key
	 * @see TransferSpeedStat
	 * @see NetworkProperties#DEFAULT_STAT_PER_512KB_SEGMENTS
	 * @return a {@link TransferSpeedStat} related LAN upload statistics
	 */
	public TransferSpeedStat getBytesDownloadedInRealBytes(String key) {
		synchronized (this) {
			return bytes_downloaded_in_real_bytes.get(key);
		}
	}

	void newDataSent(IDTransfer id, int size) {
		StatsBandwidth sb = null;
		synchronized (this) {
			for (RealTimeTransfertStat s : bytes_uploaded_in_real_time.values())
				s.newBytesIndentified(size);
			if (id != null && !id.equals(TransferAgent.NullIDTransfer)) {
				sb = transfer_agents_bandwidth.get(id.getID());
				if (sb == null)
					throw new IllegalArgumentException("Impossible to found stats for IDTransfer " + id);
				if (sb.statsForDistantKernelAddress.get() == statsForDistantKernelAddress.get())
					sb.statsForDistantKernelAddress.set(null);
			}
		}
		if (sb != null) {
			sb.newDataSent(null, size);
		}
		sb = this.statsForDistantKernelAddress.get();
		if (sb != null) {
			sb.newDataSent(null, size);
		}

	}

	void newDataSent(IDTransfer id, int size, long duration) {
		StatsBandwidth sb = null;
		synchronized (this) {
			for (TransferSpeedStat s : bytes_uploaded_in_real_bytes.values())
				s.newBytesIndentified(size, duration);
			if (id != null && !id.equals(TransferAgent.NullIDTransfer)) {
				sb = transfer_agents_bandwidth.get(id.getID());
				if (sb == null)
					throw new IllegalArgumentException("Impossible to found stats for IDTransfer " + id);
				if (sb.statsForDistantKernelAddress.get() == statsForDistantKernelAddress.get())
					sb.statsForDistantKernelAddress.set(null);
			}
		}
		if (sb != null) {
			sb.newDataSent(null, size, duration);
		}
		sb = this.statsForDistantKernelAddress.get();
		if (sb != null) {
			sb.newDataSent(null, size, duration);
		}
	}

	void newDataReceived(Integer id, int size) {
		StatsBandwidth sb = null;
		synchronized (this) {
			for (RealTimeTransfertStat s : bytes_downloaded_in_real_time.values())
				s.newBytesIndentified(size);
			if (id != null && id.intValue() != TransferAgent.NullIDTransfer.getID()) {
				sb = transfer_agents_bandwidth.get(id);
				if (sb == null)
					throw new IllegalArgumentException("Impossible to found stats for IDTransfer " + id);
				if (sb.statsForDistantKernelAddress.get() == statsForDistantKernelAddress.get())
					sb.statsForDistantKernelAddress.set(null);
			}
		}
		if (sb != null) {
			sb.newDataReceived(null, size);
		}
		sb = this.statsForDistantKernelAddress.get();
		if (sb != null) {
			sb.newDataReceived(null, size);
		}

	}

	void newDataReceived(Integer id, int size, long duration) {
		StatsBandwidth sb = null;
		synchronized (this) {
			for (TransferSpeedStat s : bytes_downloaded_in_real_bytes.values())
				s.newBytesIndentified(size, duration);
			if (id != null && id.intValue() != TransferAgent.NullIDTransfer.getID()) {
				sb = transfer_agents_bandwidth.get(id);
				if (sb == null)
					throw new IllegalArgumentException("Impossible to found stats for IDTransfer " + id);
				if (sb.statsForDistantKernelAddress.get() == statsForDistantKernelAddress.get())
					sb.statsForDistantKernelAddress.set(null);
			}
		}
		if (sb != null) {
			sb.newDataReceived(null, size, duration);
		}
		sb = this.statsForDistantKernelAddress.get();
		if (sb != null) {
			sb.newDataReceived(null, size, duration);
		}
	}

}
