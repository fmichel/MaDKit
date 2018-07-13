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

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import com.distrimind.util.Timer;

/**
 * This class represent a transfer speed in bytes per seconds. The statistic
 * does not change in real time, but are computed only when a transfer occurs.
 * If no transfer is done, then statistics does not change. This tool measure
 * the speed of the transfered bytes. This class is thread safe.
 * 
 * @author Jason Mahdjoub
 * @version 1.0
 * @since MadkitLanEdition 1.0
 */
public class TransferSpeedStat {
	private final float stats[];
	private double bytes_per_second = 0.0;
	private final long segment;
	private final long bytes_to_mean;
	private long bytes_for_current_cycle;
	private final AtomicBoolean one_cycle_done = new AtomicBoolean(false);
	private final AtomicReference<Timer> timeElapsed;
	private final long durationBeforeObsolescence;

	/**
	 * Construct a metric that compute the average of bytes per second during the
	 * last sent bytes.
	 * 
	 * @param _bytes_to_mean
	 *            the number of bytes to take into account in order to compute the
	 *            statistics. Must be greater or equal than 3.
	 * @param _segment
	 *            the segment. Must be lower than _bytes_to_mean. A lower segment
	 *            size gives statistics that are more precise, but takes more memory
	 *            place (array of
	 *            <code>(_bytes_to_mean-_bytes_to_mean%_segment)/_segment</code>).
	 * @param durationBeforeObsolescence
	 *            duration in milliseconds before the stored data becomes obsolete.
	 */
	public TransferSpeedStat(long _bytes_to_mean, long _segment, long durationBeforeObsolescence) {
		if (_bytes_to_mean < _segment)
			throw new IllegalArgumentException("_duration must be greater or equal than _segment");
		if (_bytes_to_mean < 3)
			throw new IllegalArgumentException("_duration must be greater than 3 ms");

		_bytes_to_mean = _bytes_to_mean - _bytes_to_mean % _segment;
		segment = _segment;
		bytes_to_mean = _bytes_to_mean;
		if (bytes_to_mean / segment > Integer.MAX_VALUE)
			throw new IllegalArgumentException("The value '_duration/_segment' must be lower than Interger.MAX_VALUE");
		if (bytes_to_mean / segment < 3)
			throw new IllegalArgumentException("The value '_duration/_segment' must be greater than 3");
		stats = new float[(int) (bytes_to_mean / segment)];
		timeElapsed = new AtomicReference<>(new Timer(true));
		this.durationBeforeObsolescence = durationBeforeObsolescence;
		reset();
	}

	public long getNumberOfBytesUsedForStatComputation() {
		return bytes_to_mean;
	}

	private void reset() {
		for (int i = 0; i < stats.length; i++)
			stats[i] = 0.0f;
		bytes_for_current_cycle = 0;
		bytes_per_second = 0.0;
		timeElapsed.set(new Timer(true));
	}

	private void reset(float _first_value) {
		for (int i = 0; i < stats.length; i++)
			stats[i] = _first_value;
		bytes_per_second = _first_value;
		bytes_for_current_cycle = 0;
		timeElapsed.set(new Timer(true));
	}

	/**
	 * Inform that new bytes have been transfered
	 * 
	 * @param number
	 *            the number of transfered bytes
	 * @param duration
	 *            the duration taken to transfer bytes.
	 */
	public void newBytesIndentified(int number, long duration) {
		if (duration < 0)
			throw new IllegalArgumentException("duration must be greater than 0");
		if (duration == 0)
			duration = 1;
		float val = (float) (((double) number) / (((double) duration) / 1000.f));

		synchronized (this) {
			if (number >= bytes_to_mean) {
				reset(val);
				one_cycle_done.set(true);
			} else if (number > 0) {
				if (timeElapsed.get().getDeltaMili() > durationBeforeObsolescence)
					reset();
				while (number > 0) {
					int cursor = (int) (bytes_for_current_cycle / segment);
					int used = (int) (bytes_for_current_cycle % segment);
					int free = (int) (segment - used);
					int n = Math.min(number, free);
					float total = used + n;
					double bps = bytes_per_second * ((double) stats.length) - stats[cursor];
					stats[cursor] = stats[cursor] * (((float) used) / total) + val * (((float) n) / total);
					bps = (bps + stats[cursor]) / ((double) stats.length);
					number -= n;
					bytes_for_current_cycle = bytes_for_current_cycle + n;
					if (bytes_for_current_cycle > segment) {
						one_cycle_done.set(true);
						bytes_for_current_cycle = bytes_for_current_cycle % segment;
					}
					bytes_per_second = bps;
				}
			}
		}
	}

	/**
	 * 
	 * @return true if sufficient bytes has been observed to give a correct metrics.
	 */
	public boolean isOneCycleDone() {
		return one_cycle_done.get() && timeElapsed.get().getMili() <= durationBeforeObsolescence;
	}

	/**
	 * 
	 * @return the number of bytes per second detected by this tool
	 */
	public double getBytesPerSecond() {
		synchronized (this) {
			if (timeElapsed.get().getMili() > durationBeforeObsolescence)
				reset();
			return bytes_per_second;
		}
	}

}
