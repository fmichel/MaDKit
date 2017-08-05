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

import java.util.Random;

import org.junit.Assert;
import org.junit.Test;

import com.distrimind.madkit.kernel.JunitMadkit;

/**
 * 
 * @author Jason Mahdjoub
 * @version 1.0
 * @since MadkitLanEdition 1.0
 */
public class StatTests extends JunitMadkit {
	@Test
	public void testTransferSpeedStat() {
		Random rand = new Random(System.currentTimeMillis());
		for (int i = 0; i < 10; i++) {
			int bytesToMean = rand.nextInt(10000) + 500;
			int duration = rand.nextInt(100) + 100;
			TransferSpeedStat tss = new TransferSpeedStat(bytesToMean, bytesToMean / (rand.nextInt(100) + 3), duration);
			Assert.assertFalse(tss.isOneCycleDone());
			tss.newBytesIndentified(bytesToMean, 1);
			Assert.assertTrue(tss.isOneCycleDone());
			assertEpsilonEquals(((double) bytesToMean) / (0.001), tss.getBytesPerSecond(), 1);
			tss.newBytesIndentified(bytesToMean, 1);
			Assert.assertTrue(tss.isOneCycleDone());
			assertEpsilonEquals(((double) bytesToMean) / (0.001), tss.getBytesPerSecond(), 1);
			pause(null, duration + 100);
			Assert.assertFalse(tss.isOneCycleDone());

			assertEpsilonEquals(0, tss.getBytesPerSecond(), 0);
			for (int j = 0; j < 10000; j++) {
				tss.newBytesIndentified(rand.nextInt(bytesToMean * 2), rand.nextInt(duration * 2));
				tss.getBytesPerSecond();

			}
		}
	}

	public void assertEpsilonEquals(double v1, double v2, double epsilon) {
		Assert.assertTrue("expected " + v1 + " but found " + v2, Math.abs(v1 - v2) <= epsilon);
	}

	public void assertEpsilonEquals(int v1, int v2, int epsilon) {
		Assert.assertTrue("expected " + v1 + " but found " + v2, Math.abs(v1 - v2) <= epsilon);
	}

	@Test
	public void testRealTimeTransferStat() {
		Random rand = new Random(System.currentTimeMillis());
		for (int i = 0; i < 10; i++) {
			int duration = rand.nextInt(100) + 100;
			int segment = rand.nextInt(8) + 2;
			RealTimeTransfertStat rtts = new RealTimeTransfertStat(duration, segment);
			Assert.assertEquals(0, rtts.getNumberOfIndentifiedBytes());
			int numberBytes = rand.nextInt(500) + 500;
			int totalBytes = numberBytes;
			rtts.newBytesIndentified(numberBytes);

			Assert.assertFalse(rtts.isOneCycleDone());
			for (int j = 0; j < duration; j += segment) {
				pause(null, segment);
				numberBytes = rand.nextInt(500) + 500;
				rtts.newBytesIndentified(numberBytes);
				totalBytes += numberBytes;
			}
			pause(null, duration % segment);
			rtts.newBytesIndentified(numberBytes);
			totalBytes += numberBytes;
			Assert.assertTrue(rtts.isOneCycleDone());
			Assert.assertTrue(rtts.getNumberOfIndentifiedBytes() < totalBytes);

			for (int j = 0; j < 10000; j++) {
				rtts.newBytesIndentified(rand.nextInt(1000));
				rtts.getNumberOfIndentifiedBytes();

			}
		}

	}
}
